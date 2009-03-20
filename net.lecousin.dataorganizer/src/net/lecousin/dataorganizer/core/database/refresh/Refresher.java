package net.lecousin.dataorganizer.core.database.refresh;

import java.util.List;
import java.util.Map;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.DataBase;
import net.lecousin.dataorganizer.core.database.content.ContentType;
import net.lecousin.dataorganizer.core.database.info.InfoRetriever;
import net.lecousin.dataorganizer.core.database.util.DataUtil;
import net.lecousin.dataorganizer.internal.EclipsePlugin;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.dialog.ErrorDlg;
import net.lecousin.framework.ui.eclipse.progress.WorkProgressDialog;

import org.eclipse.swt.widgets.Shell;

public class Refresher {

	public static void refresh(Shell shell, DataBase db, List<Data> data, RefreshOptions options) {
		WorkProgress progress = new WorkProgress(Local.Refreshing.toString(), data.size()*1000, true);
		WorkProgressDialog dlg = new WorkProgressDialog(shell, progress);
		Map<ContentType,List<Data>> byContent = DataUtil.splitByContent(data);
		try {
			for (ContentType type : byContent.keySet()) {
				UIUtil.runPendingEvents(dlg.getShell().getDisplay());
				if (progress.isCancelled()) break;
				List<Data> list = byContent.get(type);
				Context ctx = new Context(type, dlg);
				for (Data d : list) {
					UIUtil.runPendingEvents(dlg.getShell().getDisplay());
					if (progress.isCancelled()) break;
					progress.setSubDescription(d.getName());
					dlg.forceRefresh();
					refresh(shell, d, ctx, options, progress, 1000);
					if (progress.isCancelled()) break;
				}
				ctx.close();
				if (progress.isCancelled()) break;
			}
		} catch (Throwable t) {
			ErrorDlg.exception("Refreshing", "A fatal error occured while refreshing data", EclipsePlugin.ID, t);
		}
		dlg.close();
	}
	
	private static class Context {
		Context(ContentType type, WorkProgressDialog dlg) { this.type = type; this.dlg = dlg; }
		ContentType type;
		WorkProgressDialog dlg;
		
		Object getDataContentContext = null;
		
		void initGetDataContent() {
			if (getDataContentContext == null)
				getDataContentContext = type.openLoadDataContentContext(dlg.getCustomizePanel());
		}
		
		void close() {
			if (getDataContentContext != null)
				type.closeLoadDataContentContext(getDataContentContext);
		}
	}
	
	private static int cleanNameAmount = 1;
	private static int relocateSourcesAmount = 5000;
	private static int dataContentAmount = 100;
	private static int infoFromInternet = 500;
	private static void refresh(Shell shell, Data data, Context ctx, RefreshOptions options, WorkProgress progress, int amount) {
		int totalAmount = 0;
		if (options.cleanName) totalAmount += cleanNameAmount;
		if (options.tryToRelocateDataSourceIfNecessary) totalAmount += relocateSourcesAmount;
		if (needDataContent(data, options)) totalAmount += dataContentAmount;
		if (options.retrieveInfoFromInternet) totalAmount += infoFromInternet;
		progress.setSubDescription(Local.Refreshing+" " + data.getName());
		WorkProgress p = progress.addSubWork(null, amount, totalAmount);
		
		if (options.cleanName)
			cleanName(data, p, cleanNameAmount);
		if (options.tryToRelocateDataSourceIfNecessary)
			relocateSources(data, p, relocateSourcesAmount);
		if (needDataContent(data, options))
			getDataContent(data, ctx, p, dataContentAmount);
		if (options.retrieveInfoFromInternet)
			retrieveFromInternet(shell, data, p, infoFromInternet);
		p.done();
	}
	
	private static boolean needDataContent(Data data, RefreshOptions options) {
		boolean getDataContent = options.refreshAllDataContent;
		if (!getDataContent && options.getDataContentIfNotYetDone)
			getDataContent = !data.getContent().isContentAvailable();
		return getDataContent;
	}
	private static void getDataContent(Data data, Context ctx, WorkProgress progress, int work) {
		ctx.initGetDataContent();
		ctx.dlg.ensureCustomPanelVisibleIfNeeded();
		ctx.type.loadDataContent(data, ctx.getDataContentContext, progress, work);
	}
	
	public static void cleanName(Data data, WorkProgress progress, int work) {
		// TODO
		progress.progress(work);
	}
	public static void relocateSources(Data data, WorkProgress progress, int work) {
		// TODO
		progress.progress(work);
	}
	public static void retrieveFromInternet(Shell shell, Data data, WorkProgress progress, int work) {
		InfoRetriever.retrieve(shell, data, progress, work);
	}
}
