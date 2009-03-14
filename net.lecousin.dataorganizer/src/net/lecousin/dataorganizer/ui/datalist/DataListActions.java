package net.lecousin.dataorganizer.ui.datalist;

import java.util.List;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.internal.EclipsePlugin;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.ui.eclipse.dialog.ErrorDlg;
import net.lecousin.framework.ui.eclipse.dialog.QuestionDlg;
import net.lecousin.framework.ui.eclipse.dialog.QuestionDlg.Answer;
import net.lecousin.framework.ui.eclipse.dialog.QuestionDlg.AnswerSimple;
import net.lecousin.framework.ui.eclipse.progress.WorkProgressDialog;

public class DataListActions {

	public static void delete(List<Data> data) {
		StringBuilder str = new StringBuilder();
		str.append(Local.You_are_requesting_to_remove + " " + data.size() + " " + Local.data__s + ".<br>");
		str.append(Local.What_do_you_want_to_do);
		QuestionDlg dlg = new QuestionDlg(null, Local.Remove_data__s.toString(), null);
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
		WorkProgressDialog progress_dlg = new WorkProgressDialog(null, progress);
		for (Data d : data) {
			if (prog_fs != null) {
				List<Exception> errors = d.removeFromFileSystem();
				if (!errors.isEmpty())
					ErrorDlg.multi_exceptions("Error while removing from filesystem", "Error(s) occured while removing the data from the file system.", EclipsePlugin.ID, errors);
				prog_fs.progress(1);
			}
			d.remove();
			prog_db.progress(1);
		}
		progress_dlg.close();
	}
	
}
