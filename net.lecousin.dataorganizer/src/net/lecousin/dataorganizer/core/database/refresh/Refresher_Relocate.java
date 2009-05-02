package net.lecousin.dataorganizer.core.database.refresh;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.source.DataSource;
import net.lecousin.dataorganizer.core.database.source.LocalFileDataSource;
import net.lecousin.dataorganizer.ui.dialog.DataLinkPopup;
import net.lecousin.dataorganizer.util.DO_UIUtil;
import net.lecousin.framework.Pair;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.thread.RunnableWithData;
import net.lecousin.framework.ui.eclipse.control.list.LCContentProvider;
import net.lecousin.framework.ui.eclipse.control.list.LCTable;
import net.lecousin.framework.ui.eclipse.control.list.LCTable.ColumnProvider;
import net.lecousin.framework.ui.eclipse.control.list.LCTable.TableConfig;
import net.lecousin.framework.ui.eclipse.dialog.LCMLMessageDialog;
import net.lecousin.framework.ui.eclipse.dialog.LCTableDialog;

import org.eclipse.swt.widgets.Shell;

class Refresher_Relocate {

	static void relocateSources(Shell shell, List<Data> data, WorkProgress progress) {
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
		if (!toFind.isEmpty()) {
			LCTable.LCTableProvider<Pair<LocalFileDataSource,Data>> provider = new LCTable.LCTableProvider_List<Pair<LocalFileDataSource,Data>>(toFind) {
				public LCContentProvider<Pair<LocalFileDataSource, Data>> getContentProvider() { return new LCContentProvider.StaticList<Pair<LocalFileDataSource,Data>>(list); }
				public TableConfig getConfig() {
					TableConfig cfg = new TableConfig();
					cfg.fixedRowHeight = 18;
					cfg.multiSelection = false;
					cfg.sortable = true;
					return cfg;
				}
				@SuppressWarnings("unchecked")
				public ColumnProvider<Pair<LocalFileDataSource, Data>>[] getColumns() {
					return new ColumnProvider[] { new DO_UIUtil.ColumnData(new DO_UIUtil.ProviderPair2<LocalFileDataSource,Data>()), new DO_UIUtil.ColumnDataSource(new DO_UIUtil.ProviderPair1<LocalFileDataSource,Data>()) }; 
				}
			};
			LCTableDialog<Pair<LocalFileDataSource,Data>> dlg = new LCTableDialog<Pair<LocalFileDataSource,Data>>(shell, Local.Relocating_sources.toString(), Local.MESSAGE_Missing_Files_After_Relocate.toString(), provider, 600);
			dlg.open();
		}
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
