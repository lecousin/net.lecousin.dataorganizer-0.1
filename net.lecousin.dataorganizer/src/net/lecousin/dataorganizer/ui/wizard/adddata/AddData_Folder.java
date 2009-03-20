package net.lecousin.dataorganizer.ui.wizard.adddata;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.InitializationException;
import net.lecousin.dataorganizer.core.database.VirtualData;
import net.lecousin.dataorganizer.core.database.VirtualDataBase;
import net.lecousin.dataorganizer.core.database.content.ContentType;
import net.lecousin.dataorganizer.internal.EclipsePlugin;
import net.lecousin.framework.Pair;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.io.FileSystemUtil;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.dialog.ErrorDlg;
import net.lecousin.framework.ui.eclipse.dialog.MyDialog;
import net.lecousin.framework.ui.eclipse.progress.WorkProgressDialog;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class AddData_Folder extends WizardPage implements AddData_Page {

	public AddData_Folder() {
		super(Local.Add_data_from_a_folder.toString());
		setTitle(Local.Add_data_from_a_folder.toString());
		setDescription(Local.Add_data_from_a_folder__description.toString());
	}
	
	private Text textFolder;
	private Button checkSubFolders;
	private List<Button> checkContentTypes = new LinkedList<Button>();
	
	public void createControl(Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);
		UIUtil.gridLayout(panel, 3);
		
		UIUtil.newLabel(panel, Local.Folder_URI.toString());
		textFolder = UIUtil.newText(panel, "", new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		textFolder.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String s = textFolder.getText();
				try { new URI(s); }
				catch (URISyntaxException uri_ex) {
					// try to check for local path
					File f = new File(s);
					if (f.exists())
						textFolder.setText(f.toURI().toString());
				}
			}
		});
		textFolder.setLayoutData(UIUtil.gridDataHoriz(1, true));
		UIUtil.newButton(panel, Local.Browse_local+"...", new Listener<Object>() {
			public void fire(Object event) {
				DirectoryDialog dlg = new DirectoryDialog(MyDialog.getModalShell(), SWT.NONE);
				String s = dlg.open();
				if (s != null)
					textFolder.setText(s);
			}
		}, null);
		
		checkSubFolders = new Button(panel, SWT.CHECK);
		checkSubFolders.setText(Local.Analyze_sub_folders_recursively.toString());
		UIUtil.gridDataHorizFill(checkSubFolders);

		Group group = new Group(panel, SWT.SHADOW_IN);
		group.setText(Local.Content_types_to_detect.toString());
		UIUtil.gridDataHorizFill(group);
		UIUtil.gridLayout(group, 2);
		UIUtil.newButton(group, Local.Select_all.toString(), new Listener<Object>() {
			public void fire(Object event) {
				for (Button button : checkContentTypes)
					button.setSelection(true);
				dialogChanged();
			}
		}, null);
		UIUtil.newButton(group, Local.Deselect_all.toString(), new Listener<Object>() {
			public void fire(Object event) {
				for (Button button : checkContentTypes)
					button.setSelection(false);
				dialogChanged();
			}
		}, null);
		for (ContentType type : ContentType.getAvailableTypes()) {
			Button button = UIUtil.newCheck(group, type.getName(), new Listener<Pair<Boolean,Object>>() {
				public void fire(Pair<Boolean,Object> event) {
					dialogChanged();
				}
			}, null);
			UIUtil.gridDataHorizFill(button);
			button.setData(type.getID());
			checkContentTypes.add(button);
		}

		setControl(panel);
		dialogChanged();
	}
	
	private void dialogChanged() {
		if (textFolder.getText().length() == 0) {
			updateStatus(Local.Please_specify_a_folder_to_analyze+".");
			return;
		}
		try {
			IFileStore folder = EFS.getStore(new URI(textFolder.getText()));
			IFileInfo infos = folder.fetchInfo();
			if (!infos.exists()) {
				updateStatus(Local.The_specified_folder_doesnt_exist+".");
				return;
			}
			if (!infos.isDirectory()) {
				updateStatus(Local.The_specified_folder_is_not_a_folder+".");
				return;
			}
		} catch (CoreException e) {
			updateStatus(Local.The_specified_folder_URI_is_not_valid+": " + e.getMessage());
			return;
		} catch (URISyntaxException e) {
			updateStatus(Local.The_specified_folder_URI_is_malformed+": " + e.getReason());
			return;
		}
		
		boolean contentTypeSelected = false;
		for (Button b : checkContentTypes)
			if (b.getSelection()) {
				contentTypeSelected = true;
				break;
			}
		if (!contentTypeSelected) {
			updateStatus(Local.Please_select_at_least_one_content_type+".");
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
	
	@Override
	public IWizardPage getPreviousPage() {
		return ((AddDataWizard)getWizard()).getStartingPage();
	}
	
	public boolean canFinish() {
		return isPageComplete();
	}
	public Result performFinish() {
		List<ContentType> types = new LinkedList<ContentType>();
		for (Button b : checkContentTypes)
			if (b.getSelection())
				types.add(ContentType.getContentType((String)b.getData()));
		URI rootURI;
		try { rootURI = new URI(textFolder.getText()); }
		catch (URISyntaxException e) {
			// should not happen
			return null;
		}
		boolean recurse = checkSubFolders.getSelection();

		Search search = new Search(textFolder.getShell());
		return search.run(rootURI, recurse, types);
	}
	
	private static class Search {
		Search(Shell shell) { this.shell = shell; }
		Shell shell;
		
		LinkedList<IFileStore> files;
		LinkedList<IFileStore> folders;
		WorkProgress mainProgress;
		Result result = new Result();
		
		Result run(URI rootURI, boolean recurse, List<ContentType> types) {
			mainProgress = new WorkProgress(Local.Searching_data.toString(), 10000, true);
			WorkProgressDialog dlg = new WorkProgressDialog(shell, mainProgress);
			
			WorkProgress fileSystemProgress = mainProgress.addSubWork(Local.Analyzing_file_system.toString(), 500, 10000);
			WorkProgress analyzeProgress = mainProgress.addSubWork(Local.Analyzing_selected_files_and_folders.toString(), 9500, 10000);

			files = new LinkedList<IFileStore>();
			folders = new LinkedList<IFileStore>();
			IFileStore root;
			try { root = EFS.getStore(rootURI); }
			catch (CoreException e) {
				// should not happen
				dlg.close();
				return null;
			}
			browseFolder(root, recurse, fileSystemProgress, fileSystemProgress, rootURI);
			analyze(analyzeProgress, rootURI, types);

			if (mainProgress.isCancelled()) {
				if (result.db != null)
					result.db.close();
				dlg.close();
				return null;
			}
			
			dlg.close();
			if (mainProgress.isCancelled()) { 
				if (result.db != null)
					result.db.close();
				return null;
			}
			return result;
		}

		
		private void browseFolder(IFileStore folder, boolean recurse, WorkProgress progress, WorkProgress mainProgress, URI rootURI) {
			if (progress.isCancelled()) return;
			mainProgress.setSubDescription(rootURI.relativize(folder.toURI()).toString());
			addFolder(folder);
			IFileStore[] children;
			try { children = folder.childStores(EFS.NONE, null); }
			catch (CoreException e) {
				progress.progress(progress.getAmount());
				return;
			}
			List<IFileStore> subfolders = new LinkedList<IFileStore>();
			if (children != null) // prevent from access denied
			for (IFileStore f : children) {
				IFileInfo info = f.fetchInfo();
				if (!info.isDirectory())
					addFile(f);
				else
					subfolders.add(f);
			}
			if (subfolders.isEmpty()) {
				progress.progress(progress.getAmount());
				return;
			}
			int work = progress.getAmount();
			int step = work * 5 / 100;
			work -= step;
			int nb = subfolders.size();
			progress.progress(step);
			for (IFileStore f : subfolders) {
				if (progress.isCancelled()) return;
				step = work / nb;
				work -= step;
				nb--;
				if (recurse) {
					browseFolder(f, true, progress.addSubWork(null, step, 100000), mainProgress, rootURI);
				} else {
					addFolder(f);
					progress.progress(step);
				}
			}
		}
		private void addFile(IFileStore f) {
			files.add(f);
			mainProgress.setSubDescription(Local.Folders_found+": " + folders.size()+", "+Local.Files_found+": " + files.size());
		}
		private void addFolder(IFileStore f) {
			folders.add(f);
			mainProgress.setSubDescription(Local.Folders_found+": " + folders.size()+", "+Local.Files_found+": " + files.size());
		}
		
		private void analyze(WorkProgress progress, URI rootURI, List<ContentType> types) {
			int totalFiles = files.size();
			int totalFolders = folders.size();
			progress.setAmount(totalFiles + totalFolders + 5);
			try { result.db = new VirtualDataBase(progress, 5); }
			catch (InitializationException e) {
				ErrorDlg.exception("Create virtual database", "Unable to create a virtual database to store temporary data", EclipsePlugin.ID, e);
				mainProgress.cancel();
				return;
			}
			String rootStr = rootURI.toString();
			while (!folders.isEmpty()) {
				IFileStore f = folders.removeFirst();
				if (progress.isCancelled()) break;
				progress.setSubDescription(FileSystemUtil.makePathRelative(rootStr, f.toURI().toString()));
				analyze(f, types, result.db, progress);
				mainProgress.setSubDescription(Local.Folders_found+": " + totalFolders +", "+Local.Files_found+": " + totalFiles + ", "+Local.analyzed_folders+": " + (totalFolders - folders.size()+", "+Local.analyzed_files+": " + (totalFiles - files.size())));
			}
			while (!files.isEmpty()) {
				IFileStore f = files.removeFirst();
				if (progress.isCancelled()) break;
				progress.setSubDescription(FileSystemUtil.makePathRelative(rootStr, f.toURI().toString()));
				analyze(f, types, result.db, progress);
				mainProgress.setSubDescription(Local.Folders_found+": " + totalFolders +", "+Local.Files_found+": " + totalFiles + ", "+Local.analyzed_folders+": " + (totalFolders - folders.size()+", "+Local.analyzed_files+": " + (totalFiles - files.size())));
			}
		}
		
		private void analyze(IFileStore file, List<ContentType> types, VirtualDataBase db, WorkProgress progress) {
			boolean det = false;
			for (ContentType type : types) {
				if (progress.isCancelled()) return;
				int nbFolders = folders.size();
				int nbFiles = files.size();
				try {
					List<VirtualData> detected = type.detect(db, file, folders, files, shell);
					if (detected != null && !detected.isEmpty()) {
						result.toAdd.addAll(detected);
						det = true;
					}
				} catch (Throwable t) {
					ErrorDlg.exception(Local.Add_data.toString(), "Internal error", EclipsePlugin.ID, t);
				}
				progress.progress((nbFolders - folders.size()) + (nbFiles - files.size()));
			}
			progress.progress(1);
			if (!det) {
				if (!file.fetchInfo().isDirectory())
					result.noDetectedFiles.add(file);
			}
		}
		
	}

	
}
