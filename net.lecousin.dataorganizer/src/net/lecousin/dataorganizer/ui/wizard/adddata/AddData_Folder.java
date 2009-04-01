package net.lecousin.dataorganizer.ui.wizard.adddata;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.DataOrganizer;
import net.lecousin.dataorganizer.core.InitializationException;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.VirtualData;
import net.lecousin.dataorganizer.core.database.VirtualDataBase;
import net.lecousin.dataorganizer.core.database.content.ContentType;
import net.lecousin.dataorganizer.core.database.source.DataSource;
import net.lecousin.dataorganizer.internal.EclipsePlugin;
import net.lecousin.framework.Pair;
import net.lecousin.framework.collections.ArrayUtil;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.files.FileType;
import net.lecousin.framework.files.TypedFile;
import net.lecousin.framework.files.TypedFolder;
import net.lecousin.framework.log.Log;
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
	private Button checkCheckLinkedFiles;
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
		
		checkSubFolders = UIUtil.newCheck(panel, Local.Analyze_sub_folders_recursively.toString(), null, null);
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
		
		group = new Group(panel, SWT.SHADOW_IN);
		group.setText(Local.Options.toString());
		UIUtil.gridDataHorizFill(group);
		UIUtil.gridLayout(group, 1);
		checkCheckLinkedFiles = UIUtil.newCheck(group, Local.Try_to_detect_data_even_on_files_already_linked_to_an_existing_data.toString(), null, null);

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
		boolean checkLinked = checkCheckLinkedFiles.getSelection();
		
		Search search = new Search(textFolder.getShell());
		return search.run(rootURI, recurse, types, checkLinked);
	}
	
	private static class Search {
		Search(Shell shell) { this.shell = shell; }
		Shell shell;
		
		Result run(URI rootURI, boolean recurse, List<ContentType> types, boolean checkLinked) {
			WorkProgress mainProgress = new WorkProgress(Local.Searching_data.toString(), 10000, true);
			WorkProgressDialog dlg = new WorkProgressDialog(shell, mainProgress);
			
			int stepAnalyze;
			int stepFilterLinked;
			int stepDetect;
			if (!checkLinked) {
				stepAnalyze = 7500;
				stepFilterLinked = 2000;
				stepDetect = 500;
			} else {
				stepAnalyze = 9000;
				stepFilterLinked = 0;
				stepDetect = 1000;
			}
			
			WorkProgress fileSystemProgress = mainProgress.addSubWork(Local.Analyzing_file_system.toString(), stepAnalyze, 100000);
			WorkProgress filterProgress = null;
			if (!checkLinked)
				filterProgress = mainProgress.addSubWork(Local.Filter_files_already_linked.toString(), stepFilterLinked, 10000);
			WorkProgress detectProgress = mainProgress.addSubWork(Local.Detecting_data_from_analyzed_files.toString(), stepDetect, 10000);

			IFileStore rootFolder;
			try { rootFolder = EFS.getStore(rootURI); }
			catch (CoreException e) {
				// should not happen
				dlg.close();
				return null;
			}

			// sort content types and retrieve eligible file types
			List<ContentType> sortedTypes = new LinkedList<ContentType>();
			Set<FileType> filetypes = new HashSet<FileType>();
			for (ContentType type : types) {
				FileType[] eligible = type.getEligibleFileTypesForDetection();
				for (FileType ft : eligible)
					filetypes.add(ft);
				int index = 0;
				for (ContentType type2 : sortedTypes) {
					FileType[] eligible2 = type2.getEligibleFileTypesForDetection();
					List<FileType> union = ArrayUtil.unionIdentity(eligible, eligible2);
					if (!union.isEmpty()) {
						if (eligible.length < eligible2.length) {
							index++;
							continue;
						}
					}
					sortedTypes.add(index, type);
					break;
				}
				if (index >= sortedTypes.size())
					sortedTypes.add(type);
			}
			
			// analyze file system
			TypedFolder root = new TypedFolder(rootURI, rootFolder, recurse, filetypes, fileSystemProgress, 100000);
			fileSystemProgress.done();

			if (mainProgress.isCancelled()) {
				dlg.close();
				return null;
			}
			
			if (!checkLinked) {
				List<Data> allData = DataOrganizer.database().getAllData();
				List<DataSource> sources = new ArrayList<DataSource>(allData.size()*2);
				for (Data d : allData)
					sources.addAll(d.getSources());
				filterLinked(root, sources, filterProgress, 10000);
				filterProgress.done();
			}
			
			if (mainProgress.isCancelled()) {
				dlg.close();
				return null;
			}
			
			Result result = new Result();
			try { result.db = new VirtualDataBase(detectProgress, 5); }
			catch (InitializationException e) {
				ErrorDlg.exception("Create virtual database", "Unable to create a virtual database to store temporary data", EclipsePlugin.ID, e);
				mainProgress.cancel();
				dlg.close();
				return null;
			}
			
			// detect
			int nb = types.size();
			ArrayList<WorkProgress> progresses = new ArrayList<WorkProgress>(nb);
			int amount = 10000-5;
			for (ContentType type : sortedTypes) {
				int step = amount/nb--;
				amount -= step;
				progresses.add(detectProgress.addSubWork(Local.Detecting_data_for_type+": "+type.getName(), step, 100000));
			}
			for (int i = 0; i < sortedTypes.size(); ++i) {
				ContentType type = sortedTypes.get(i);
				WorkProgress progress = progresses.get(i);
				detect(rootURI, root, type, result, new LinkedList<IFileStore>(), progress, 100000);
				if (mainProgress.isCancelled())
					break;
			}
			root = null;
			detectProgress.done();
			
			if (mainProgress.isCancelled()) {
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

		private boolean filterLinked(TypedFolder folder, List<DataSource> allSources, WorkProgress progress, int amount) {
			if (progress.isCancelled())
				return true;
			int nb = folder.typedFiles.size() + folder.notTypedFiles.size() + folder.subFolders.size()*5;
			List<Pair<IFileStore,TypedFile>> typedToRemove = new LinkedList<Pair<IFileStore,TypedFile>>();
			for (Pair<IFileStore,TypedFile> p : folder.typedFiles) {
				int step = amount/nb--;
				amount -= step;
				IFileStore file = p.getValue1();
				File local;
				try { local = file.toLocalFile(EFS.NONE, null); }
				catch (CoreException e) { local = null; }
				for (DataSource source : allSources) {
					if ((local != null && source.hasLink(local)) || (local == null && source.hasLinkNotLocal(file))) {
						typedToRemove.add(p);
						break;
					}
				}
				progress.progress(step);
				if (progress.isCancelled())
					return true;
			}
			List<IFileStore> notTypedToRemove = new LinkedList<IFileStore>();
			for (IFileStore file : folder.notTypedFiles) {
				int step = amount/nb--;
				amount -= step;
				File local;
				try { local = file.toLocalFile(EFS.NONE, null); }
				catch (CoreException e) { local = null; }
				for (DataSource source : allSources) {
					if ((local != null && source.hasLink(local)) || (local == null && source.hasLinkNotLocal(file))) {
						notTypedToRemove.add(file);
						break;
					}
				}
				progress.progress(step);
				if (progress.isCancelled())
					return true;
			}
			folder.typedFiles.removeAll(typedToRemove); typedToRemove = null;
			folder.notTypedFiles.removeAll(notTypedToRemove); notTypedToRemove = null;
			List<TypedFolder> folderToRemove = new LinkedList<TypedFolder>();
			nb /= 5;
			for (TypedFolder f : folder.subFolders) {
				int step = amount/nb--;
				amount -= step;
				if (!filterLinked(f, allSources, progress, step))
					folderToRemove.add(f);
				if (progress.isCancelled())
					return true;
			}
			folder.subFolders.removeAll(folderToRemove);
			if (folder.subFolders.isEmpty() &&
				folder.typedFiles.isEmpty() &&
				folder.notTypedFiles.isEmpty())
				return false;
			return true;
		}
		
		private void detect(URI rootURI, TypedFolder folder, ContentType type, Result result, List<IFileStore> usedBefore, WorkProgress progress, int amount) {
			progress.setSubDescription(rootURI.relativize(folder.folder.toURI()).toString());
			List<Pair<List<IFileStore>,VirtualData>> data;
			List<IFileStore> used = new LinkedList<IFileStore>();
			used.addAll(usedBefore);
			if (!used.contains(folder.folder)) {
				try { data = type.detectOnFolder(result.db, folder, shell); }
				catch (Throwable t) {
					if (Log.error(this))
						Log.error(this, "Content Type " + type.getName() + " threw an exception while detecting on folder", t);
					data = null;
				}
				if (data != null) {
					for (Pair<List<IFileStore>,VirtualData> p : data) {
						result.toAdd.add(p.getValue2());
						used.addAll(p.getValue1());
					}
				}
			}
			if (progress.isCancelled())
				return;
			int nb = folder.typedFiles.size() + folder.notTypedFiles.size() + folder.subFolders.size()*5;
			for (Pair<IFileStore,TypedFile> p : folder.typedFiles) {
				int step = amount/nb--;
				amount -= step;
				if (!used.contains(p.getValue1())) {
					try { data = type.detectOnFile(result.db, folder, p.getValue1(), p.getValue2(), shell); }
					catch (Throwable t) {
						if (Log.error(this))
							Log.error(this, "Content Type " + type.getName() + " threw an exception while detecting on typed file", t);
						data = null;
					}
					if (data != null) {
						for (Pair<List<IFileStore>,VirtualData> p2 : data) {
							result.toAdd.add(p2.getValue2());
							used.addAll(p2.getValue1());
						}
					}
				}
				progress.progress(step);
				if (progress.isCancelled())
					return;
			}
			for (IFileStore file : folder.notTypedFiles) {
				int step = amount/nb--;
				amount -= step;
				if (!used.contains(file)) {
					try { data = type.detectOnFile(result.db, folder, file, shell); }
					catch (Throwable t) {
						if (Log.error(this))
							Log.error(this, "Content Type " + type.getName() + " threw an exception while detecting on non-typed file", t);
						data = null;
					}
					if (data != null) {
						for (Pair<List<IFileStore>,VirtualData> p : data) {
							result.toAdd.add(p.getValue2());
							used.addAll(p.getValue1());
						}
					}
				}
				progress.progress(step);
				if (progress.isCancelled())
					return;
			}
			nb /= 5;
			for (TypedFolder f : folder.subFolders) {
				int step = amount/nb--;
				amount -= step;
				detect(rootURI, f, type, result, used, progress, step);
				if (progress.isCancelled())
					return;
			}

			for (IFileStore file : folder.notTypedFiles)
				if (!used.contains(file))
					result.notDetectedNotTypesFiles.add(URLDecoder.decode(file.toURI().toString()));
			for (Pair<IFileStore,TypedFile> p : folder.typedFiles)
				if (!used.contains(p.getValue1()))
					result.notDetectedTypesFiles.add(URLDecoder.decode(p.getValue1().toURI().toString()));
			folder.folder = null;
			folder.notTypedFiles.clear();
			folder.notTypedFiles = null;
			folder.typedFiles.clear();
			folder.typedFiles = null;
			folder.subFolders.clear();
			folder.subFolders = null;
		}
		
	}

	
}
