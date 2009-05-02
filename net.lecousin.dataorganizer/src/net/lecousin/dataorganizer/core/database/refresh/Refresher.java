package net.lecousin.dataorganizer.core.database.refresh;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.DataBase;
import net.lecousin.dataorganizer.core.database.content.DataContentType;
import net.lecousin.dataorganizer.core.database.info.Info;
import net.lecousin.dataorganizer.core.database.info.InfoRetrieverPlugin;
import net.lecousin.dataorganizer.core.database.info.InfoRetrieverPluginRegistry;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.progress.WorkProgressDialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Refresher {

	public static void refresh(Shell shell, DataBase db, List<Data> data, RefreshOptions options) {
		shell = new Shell(Display.getDefault(), SWT.PRIMARY_MODAL);
		
		WorkProgress progress = new WorkProgress(Local.Refreshing.toString(), data.size()*1000, true);
		WorkProgressDialog dlg = new WorkProgressDialog(shell, progress);

		// first, relocate sources if necessary
		if (options.tryToRelocateDataSourceIfNecessary) {
			Refresher_Relocate.relocateSources(shell, data, progress);
			if (progress.isCancelled()) { dlg.close(); return; }
			progress.reset(Local.Refreshing.toString(), data.size()*1000);
		}
		
		List<Data> refreshContent = new LinkedList<Data>();
		Map<InfoRetrieverPlugin,List<Data>> internet = new HashMap<InfoRetrieverPlugin,List<Data>>();
		int nbToRetrieve = 0;
		for (Data d : data) {
			if (needDataContent(d, options))
				refreshContent.add(d);
			List<InfoRetrieverPlugin> retrievers = needRetrieveInfo(d, options);
			if (retrievers != null && !retrievers.isEmpty()) {
				for (InfoRetrieverPlugin pi : retrievers) {
					List<Data> toRetrieve = internet.get(pi);
					if (toRetrieve == null) {
						toRetrieve = new LinkedList<Data>();
						internet.put(pi, toRetrieve);
					}
					toRetrieve.add(d);
					nbToRetrieve++;
				}
			}
		}
		int stepRefreshContent = refreshContent.size()*100;
		int stepRetrieveInternet = nbToRetrieve*50;
		progress.reset(Local.Refreshing.toString(), stepRefreshContent + stepRetrieveInternet);
		WorkProgress progressDataContent = stepRefreshContent > 0 ? progress.addSubWork(Local.Refreshing_data_content.toString(), stepRefreshContent, 1) : null;
		WorkProgress progressInternet = stepRetrieveInternet > 0 ? progress.addSubWork(Local.Retrieve_information_from_Internet.toString(), stepRetrieveInternet, 1) : null; 
		
		if (!progress.isCancelled() && progressDataContent != null) {
			Refresher_DataContent refresherDataContent = Refresher_DataContent.refresh(refreshContent, progressDataContent, dlg);
			while (!refresherDataContent.isDone()) {
				UIUtil.runPendingEvents(dlg.getShell().getDisplay());
				try { Thread.sleep(100); } catch (InterruptedException e) { break; }
				UIUtil.runPendingEvents(dlg.getShell().getDisplay());
			}
		}

		if (!progress.isCancelled() && progressInternet != null) {
			Refresher_Internet refresherInternet = new Refresher_Internet(shell, internet, progressInternet);
			while (!refresherInternet.isDone()) {
				UIUtil.runPendingEvents(dlg.getShell().getDisplay());
				try { Thread.sleep(100); } catch (InterruptedException e) { break; }
				UIUtil.runPendingEvents(dlg.getShell().getDisplay());
			}
		}
		
		dlg.close();
	}
	
	private static boolean needDataContent(Data data, RefreshOptions options) {
		if (options.getDataContent == null) return false;
		switch (options.getDataContent) {
		case ALL: return true;
		case IF_NOT_YET_DONE: return !data.getContent().isContentAvailable(); 
		}
		return false;
	}
	private static List<InfoRetrieverPlugin> needRetrieveInfo(Data data, RefreshOptions options) {
		List<InfoRetrieverPlugin> list = InfoRetrieverPluginRegistry.getRetrievers(data.getContentType().getID());
		if (list.isEmpty()) return null;
		if (options.retrieveInfoFromInternet == null) return null;
		switch (options.retrieveInfoFromInternet) {
		case ALL: return list;
		case MISSING:
			DataContentType content = data.getContent();
			if (content == null) return null;
			Info info = content.getInfo();
			if (info == null) return list;
			for (Iterator<InfoRetrieverPlugin> it = list.iterator(); it.hasNext(); ) {
				InfoRetrieverPlugin pi = it.next();
				if (info.getSources().contains(pi.getSourceID()))
					it.remove();
			}
			return list; 
		}
		return null;
	}
	
}
