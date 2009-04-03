package net.lecousin.dataorganizer.core.database.refresh;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.DataBase;
import net.lecousin.dataorganizer.core.database.content.ContentType;
import net.lecousin.dataorganizer.core.database.info.InfoRetriever;
import net.lecousin.dataorganizer.core.database.source.DataSource;
import net.lecousin.dataorganizer.core.database.source.LocalFileDataSource;
import net.lecousin.dataorganizer.core.database.util.DataUtil;
import net.lecousin.dataorganizer.internal.EclipsePlugin;
import net.lecousin.dataorganizer.ui.dialog.DataLinkPopup;
import net.lecousin.framework.Pair;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.thread.RunnableWithData;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.dialog.ErrorDlg;
import net.lecousin.framework.ui.eclipse.dialog.LCMLMessageDialog;
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
			if (!progress.isCancelled()) {
				if (options.tryToRelocateDataSourceIfNecessary)
					relocateSources(shell, data, progress);
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
	private static int dataContentAmount = 100;
	private static int infoFromInternet = 500;
	private static void refresh(Shell shell, Data data, Context ctx, RefreshOptions options, WorkProgress progress, int amount) {
		int totalAmount = 0;
		if (options.cleanName) totalAmount += cleanNameAmount;
		if (needDataContent(data, options)) totalAmount += dataContentAmount;
		if (options.retrieveInfoFromInternet) totalAmount += infoFromInternet;
		progress.setSubDescription(Local.Refreshing+" " + data.getName());
		WorkProgress p = progress.addSubWork(null, amount, totalAmount);
		
		if (options.cleanName)
			cleanName(data, p, cleanNameAmount);
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
	public static void retrieveFromInternet(Shell shell, Data data, WorkProgress progress, int work) {
		InfoRetriever.retrieve(shell, data, progress, work);
	}
	
	private static void relocateSources(Shell shell, List<Data> data, WorkProgress progress) {
		progress.reset(Local.Relocating_sources+"...", 10100);
		List<Pair<LocalFileDataSource,Data>> toFind = new LinkedList<Pair<LocalFileDataSource,Data>>();
		for (Data d : data)
			for (DataSource s : d.getSources())
				if (s instanceof LocalFileDataSource) {
					File file = ((LocalFileDataSource)s).getLocalFile();
					if (!file.exists())
						toFind.add(new Pair<LocalFileDataSource,Data>((LocalFileDataSource)s, d));
				}
		progress.progress(100);
		progress.setDescription(progress.getDescription()+" "+toFind.size()+" "+Local.files_found_as_missing.toString());
		if (toFind.isEmpty()) {
			progress.progress(10000);
			return;
		}
		List<File> roots = new LinkedList<File>();
		for (File root : File.listRoots())
			if (root.exists())
				roots.add(root);
		int nb = roots.size();
		int work = 10000;
		for (File root : roots) {
			int step = work/nb--;
			work -= step;
			relocateInDir(shell, toFind, root, progress, step);
		}
		progress.done();
	}
	private static void relocateInDir(Shell shell, List<Pair<LocalFileDataSource,Data>> toFind, File dir, WorkProgress progress, int work) {
		progress.setSubDescription(dir.getAbsolutePath());
		List<File> files = new LinkedList<File>();
		List<File> dirs = new LinkedList<File>();
		try {
			for (File f : dir.listFiles())
				if (f.isDirectory()) dirs.add(f); else files.add(f);
		} catch (Throwable t) {
			progress.progress(work);
			return;
		}
		int nb = files.size()+dirs.size()*5;
		for (File file : files) {
			int step = work/nb--;
			work -= step;
			relocateInFile(shell, toFind, file, progress, step);
			if (progress.isCancelled()) return;
			if (toFind.isEmpty()) return;
		}
		nb /= 5;
		for (File subdir : dirs) {
			int step = work/nb--;
			work -= step;
			relocateInDir(shell, toFind, subdir, progress, step);
			if (progress.isCancelled()) return;
			if (toFind.isEmpty()) return;
		}
		progress.progress(work);
	}
	private static void relocateInFile(Shell shell, List<Pair<LocalFileDataSource,Data>> toFind, File file, WorkProgress progress, int work) {
		for (Iterator<Pair<LocalFileDataSource,Data>> it = toFind.iterator(); it.hasNext(); ) {
			Pair<LocalFileDataSource,Data> p = it.next();
			if (p.getValue1().isSameContent(file))
				if (askToReplace(shell, p.getValue2(), p.getValue1(), file)) {
					p.getValue1().setLocation(file);
					p.getValue2().signalModification();
					it.remove();
					if (toFind.isEmpty()) return;
				}
			if (progress.isCancelled()) return;
		}
	}
	private static boolean askToReplace(Shell shell, Data data, LocalFileDataSource previous, File newFile) {
		LCMLMessageDialog dlg = new LCMLMessageDialog(shell, Local.process(Local.MESSAGE_RELOCATE_File_found, data.getName(), previous.getLocalPath(), newFile.getAbsolutePath()), LCMLMessageDialog.Type.QUESTION);
		dlg.addLinkListener("data", new RunnableWithData<Data>(data) {
			public void run() {
				DataLinkPopup.open(data(), null, null);
			}
		});
		return dlg.open(Local.Refreshing.toString()) == LCMLMessageDialog.RESULT_QUESTION_YES;
	}
}
