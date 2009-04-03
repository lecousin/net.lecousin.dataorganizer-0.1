package net.lecousin.dataorganizer.ui.datalist;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.DataOrganizer;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.refresh.RefreshOptions;
import net.lecousin.dataorganizer.core.database.refresh.Refresher;
import net.lecousin.dataorganizer.internal.EclipsePlugin;
import net.lecousin.dataorganizer.ui.dialog.RefreshDialog;
import net.lecousin.framework.Pair;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.ui.eclipse.dialog.ErrorDlg;
import net.lecousin.framework.ui.eclipse.dialog.MyDialog;
import net.lecousin.framework.ui.eclipse.dialog.QuestionDlg;
import net.lecousin.framework.ui.eclipse.dialog.QuestionDlg.Answer;
import net.lecousin.framework.ui.eclipse.dialog.QuestionDlg.AnswerSimple;
import net.lecousin.framework.ui.eclipse.dialog.QuestionDlg.ContextualOptions;
import net.lecousin.framework.ui.eclipse.progress.WorkProgressDialog;

public class DataListActions {

	public static void delete(List<Data> data) {
		StringBuilder str = new StringBuilder();
		str.append(Local.You_are_requesting_to_remove + " " + data.size() + " " + Local.data__s + ".<br>");
		str.append(Local.What_do_you_want_to_do);
		QuestionDlg dlg = new QuestionDlg(MyDialog.getPlatformShell(), Local.Remove_data__s.toString(), null);
		dlg.setMessage(str.toString());
		dlg.setAnswers(new Answer[] {
				new AnswerSimple("remove_database", Local.Remove_only_from_database.toString()),
				new AnswerSimple("remove_filesystem", Local.Remove_from_database_and_filesystem.toString()),
		});
		dlg.show();
		String answer = dlg.getAnswerID();
		if (answer == null) return;
		WorkProgress progress = new WorkProgress(Local.Removing_data.toString(), data.size() * (answer.equals("remove_database") ? 2 : 3), false);
		WorkProgress prog_db = progress.addSubWork(null, 2 * data.size(), data.size());
		WorkProgress prog_fs = answer.equals("remove_database") ? null : progress.addSubWork(null, data.size(), data.size());
		WorkProgressDialog progress_dlg = new WorkProgressDialog(MyDialog.getPlatformShell(), progress);
		List<File> files = new LinkedList<File>();
		for (Data d : data) {
			if (prog_fs != null) {
				Pair<List<Exception>,List<File>> p = d.removeFromFileSystem(); 
				List<Exception> errors = p.getValue1();
				files.addAll(p.getValue2());
				if (!errors.isEmpty())
					ErrorDlg.multi_exceptions("Error while removing from filesystem", "Error(s) occured while removing the data from the file system.", EclipsePlugin.ID, errors);
				prog_fs.progress(1);
			}
			d.remove();
			prog_db.progress(1);
		}
		List<Data> toRemove = new LinkedList<Data>();
		if (!files.isEmpty()) {
			List<Data> list = DataOrganizer.database().getAllData();
			progress.reset(Local.Analyzing_database.toString(), list.size());
			ContextualOptions options = new ContextualOptions();
			options.addOption(new ContextualOptions.Option("always", Local.Do_action_for_same_situation.toString(), false));
			String defaultAction = null;
			for (Data d : list) {
				Set<File> linked = d.getLinkedFiles(files);
				if (!linked.isEmpty()) {
					String action;
					if (options.getOption("always").selection)
						action = defaultAction;
					else {
						QuestionDlg qdlg = new QuestionDlg(MyDialog.getPlatformShell(), Local.Remove.toString(), options);
						StringBuilder message = new StringBuilder();
						for (File file : linked)
							message.append(" - ").append(file.getAbsolutePath()).append("<br/>");
						qdlg.setMessage(Local.process(Local.MESSAGE_Files_removed, message.toString(), d.getName()));
						qdlg.setAnswers(new Answer[] {
							new QuestionDlg.AnswerSimple("nothing", Local.Keep_data_with_links_on_removed_files.toString()),
							new QuestionDlg.AnswerSimple("unlink", Local.Remove_links_to_removed_files.toString()),
							new QuestionDlg.AnswerSimple("remove", Local.Remove_data.toString()),
						});
						qdlg.show();
						action = qdlg.getAnswerID();
						if (action != null && options.getOption("always").selection)
							defaultAction = action;
						if (action == null) action = "nothing";
					}
					if (action.equals("unlink"))
						d.unlinkSources(linked);
					else if (action.equals("remove"))
						toRemove.add(d);
				}
				progress.progress(1);
			}
		}
		progress_dlg.close();
		if (!toRemove.isEmpty())
			delete(toRemove);
	}
	
	public static void refresh(List<Data> data) {
		RefreshDialog dlg = new RefreshDialog(MyDialog.getPlatformShell());
		RefreshOptions options = dlg.open();
		if (options == null) return;
		Refresher.refresh(MyDialog.getPlatformShell(), DataOrganizer.database(), data, options);
	}
}
