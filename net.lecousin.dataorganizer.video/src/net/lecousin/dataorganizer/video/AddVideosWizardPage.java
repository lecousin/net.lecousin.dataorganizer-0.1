package net.lecousin.dataorganizer.video;

import java.io.File;

import net.lecousin.dataorganizer.core.InitializationException;
import net.lecousin.dataorganizer.core.database.VirtualData;
import net.lecousin.dataorganizer.core.database.VirtualDataBase;
import net.lecousin.dataorganizer.core.database.content.ContentType;
import net.lecousin.dataorganizer.core.database.source.DataSource;
import net.lecousin.dataorganizer.ui.wizard.adddata.AddData_Page;
import net.lecousin.dataorganizer.video.internal.EclipsePlugin;
import net.lecousin.framework.collections.CollectionUtil;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.io.FileSystemUtil;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.dialog.ErrorDlg;
import net.lecousin.framework.ui.eclipse.dialog.MyDialog;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.List;

public class AddVideosWizardPage extends WizardPage implements AddData_Page {

	public AddVideosWizardPage() {
		super(Local.Add_videos.toString());
		setTitle(Local.Add_videos.toString());
		setDescription(Local.Select_the_videos_you_want_to_add.toString());
	}

	private List list;
	
	public void createControl(Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);
		UIUtil.gridLayout(panel, 2);
		
		list = new List(panel, SWT.BORDER | SWT.MULTI);
		list.setLayoutData(UIUtil.gridData(1, true, 10, true));
		UIUtil.newButton(panel, Local.Add_files+"...", new Listener<Object>() {
			public void fire(Object event) {
				FileDialog dlg = new FileDialog(MyDialog.getModalShell(), SWT.OPEN | SWT.MULTI);
				dlg.setText(Local.Select_the_video_files_to_add.toString());
				dlg.open();
				for (String s : dlg.getFileNames()) {
					File file;
					if (dlg.getFilterPath().length() > 0)
						file = new File(new File(dlg.getFilterPath()), s);
					else
						file = new File(s);
					list.add(file.getAbsolutePath());
				}
				dialogChanged();
			}
		}, null);
		
		setControl(panel);
		dialogChanged();
	}

	private void dialogChanged() {
		if (list.getItemCount() == 0) {
			updateStatus(Local.Add_at_least_one_file_to_the_list.toString());
			return;
		}
		updateStatus(null);
	}
	
	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}
	
	@Override
	public IWizardPage getNextPage() {
		return null;
	}
	
	public boolean canFinish() {
		return isPageComplete();
	}
	public Result performFinish() {
		Result result = new Result();
		try { result.db = new VirtualDataBase(new WorkProgress("", 1, false), 1); }
		catch (InitializationException e) {
			ErrorDlg.exception(Local.Add_videos.toString(), "Unable to create virtual database to add videos", EclipsePlugin.ID, e);
			return null;
		}
		for (String path : list.getItems()) {
			File file = new File(path);
			try {
				result.toAdd.add((VirtualData)result.db.addData(
						FileSystemUtil.getFileNameWithoutExtension(file.getName()), 
						ContentType.getContentType(VideoContentType.VIDEO_TYPE), 
						CollectionUtil.single_element_list(DataSource.get(file))));
			} catch (Throwable t) {
				ErrorDlg.exception(Local.Add_videos.toString(), "Unable to add videos", EclipsePlugin.ID, t);
			}
		}
		return result;
	}
	public boolean finished() {
		return true;
	}
}
