package net.lecousin.dataorganizer.ui.wizard.adddata;

import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.DataOrganizer;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.VirtualData;
import net.lecousin.dataorganizer.core.database.content.ContentType;
import net.lecousin.dataorganizer.core.database.refresh.RefreshOptions;
import net.lecousin.dataorganizer.core.database.refresh.Refresher;
import net.lecousin.dataorganizer.internal.EclipsePlugin;
import net.lecousin.dataorganizer.ui.dialog.RefreshDialog;
import net.lecousin.dataorganizer.ui.wizard.adddata.AddData_Page.Result;
import net.lecousin.framework.Pair;
import net.lecousin.framework.io.FileSystemUtil;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.thread.RunnableWithData;
import net.lecousin.framework.ui.eclipse.dialog.ErrorDlg;
import net.lecousin.framework.ui.eclipse.dialog.FlatPagedListDialog;
import net.lecousin.framework.ui.eclipse.dialog.LCMLMessageDialog;
import net.lecousin.framework.ui.eclipse.dialog.QuestionDlg;
import net.lecousin.framework.ui.eclipse.dialog.MyDialog.OrientationY;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;


public class AddDataWizard extends Wizard {

	public AddDataWizard() {
        setNeedsProgressMonitor(true);
        setWindowTitle(Local.Add_data.toString());
        setForcePreviousAndNextButtons(true);
	}

	private AddData_SelectType firstPage;
	private Map<String,AddData_Page> typePages = new HashMap<String,AddData_Page>();
	private AddData_Folder folderPage;
	
	@Override
	public void addPages() {
		addPage(firstPage = new AddData_SelectType());
		for (ContentType type : ContentType.getAvailableTypes()) {
			AddData_Page page = type.createAddDataWizardPage();
			if (page != null) {
				page.setPreviousPage(firstPage);
				typePages.put(type.getID(), page);
				addPage(page);
			}
		}
		addPage(folderPage = new AddData_Folder());
	}
	
	public IWizardPage getTypePage(String type_id) {
		return typePages.get(type_id);
	}
	public IWizardPage getFolderPage() {
		return folderPage;
	}
	
	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		return page.getNextPage();
	}
	@Override
	public IWizardPage getPreviousPage(IWizardPage page) {
		return page.getPreviousPage();
	}
	
	@Override
	public boolean canFinish() {
		if (!firstPage.isPageComplete()) return false;
		IWizardPage page = firstPage.getNextPage();
		if (!(page instanceof AddData_Page)) return false;
		return ((AddData_Page)page).canFinish();
	}
	
	@Override
	public boolean performFinish() {
		if (!firstPage.isPageComplete()) return false;
		IWizardPage page = firstPage.getNextPage();
		if (!(page instanceof AddData_Page)) return false;
		Result result = ((AddData_Page)page).performFinish();
		if (result == null) return false;
		
        try {
            getContainer().run(false, true, new AddData(getShell(), result));
        } catch (InterruptedException e) {
            return false;
        } catch (InvocationTargetException e) {
            Throwable realException = e.getTargetException();
            ErrorDlg.exception("Error", "An internal error occured when adding data", EclipsePlugin.ID, realException);
            return false;
        }
		
		return true;
	}
	
	private static class AddData implements IRunnableWithProgress {
		public AddData(Shell shell, Result result) {
			this.result = result;
			this.shell = shell;
		}
		
		private Shell shell;
		private Result result;
		
    	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
    		monitor.beginTask(Local.Add+" " + result.toAdd.size() + " "+Local.data__s_to_database+"...", result.toAdd.size());
    		List<Data> added = new LinkedList<Data>();
    		QuestionDlg.ContextualOptions context = new QuestionDlg.ContextualOptions();
    		for (VirtualData data : result.toAdd) {
    			monitor.subTask(data.getName());
    			try {
    				WorkProgress fake = new WorkProgress(null, 1, true);
    				boolean dupOk = DataOrganizer.database().checkForDuplicate(shell, data, context, fake);
    				if (fake.isCancelled())
    					monitor.setCanceled(true);
    				if (monitor.isCanceled()) break;
    				if (dupOk) {
    					Data d = DataOrganizer.database().addDataFromOtherDB(data);
    					added.add(d);
    				}
    			} catch (CoreException e) {
    				ErrorDlg.exception("Add data", "Unable to add data '" + data.getName() + "'.", EclipsePlugin.ID, e);
    			}
    			monitor.worked(1);
    			if (monitor.isCanceled()) break;
    		}

    		if (monitor.isCanceled()) {
    			if (!added.isEmpty()) {
    				if (MessageDialog.openQuestion(shell, Local.Operation_cancelled.toString(), "" + added.size() + " "+Local.data_have_been_added_before_cancel)) {
    					monitor.beginTask("Removing data from database", added.size());
    					for (Data data : added) {
    						monitor.subTask(data.getName());
    						DataOrganizer.database().removeData(data);
    						monitor.worked(1);
    					}
    				}
    			}
    			throw new InterruptedException();
    		}
    		result.db.close();
    		RefreshOptions options = new RefreshOptions();
    		options.cleanName = true;
    		options.getDataContentIfNotYetDone = true;
    		options.retrieveInfoFromInternet = true;
    		RefreshDialog rd = new RefreshDialog(shell, options);
    		if (rd.open() != null)
    			Refresher.refresh(shell, DataOrganizer.database(), added, options);
    		StringBuilder message = new StringBuilder();
    		message.append(added.size()).append(' ').append(Local.data_successfully_added).append('.');
    		Set<String> extensions = new HashSet<String>();
    		if (!result.noDetectedFiles.isEmpty()) {
    			for (IFileStore file : result.noDetectedFiles)
    				extensions.add(FileSystemUtil.getFileNameExtension(file.getName()).toLowerCase());
    			message.append("<br> <br>");
    			message.append(Local.process(Local.MESSAGE_Not_detected, result.noDetectedFiles.size(), extensions.size()));
    		}
    		LCMLMessageDialog dlg = new LCMLMessageDialog(shell, message.toString(), LCMLMessageDialog.Type.INFORMATION);
    		dlg.addLinkListener("files", new RunnableWithData<Pair<List<IFileStore>,Set<String>>>(new Pair<List<IFileStore>,Set<String>>(result.noDetectedFiles, extensions)) {
    			@SuppressWarnings("unchecked")
				public void run() {
    				FlatPagedListDialog.Filter<IFileStore>[] filters = new FlatPagedListDialog.Filter[1];
    				filters[0] = new FlatPagedListDialog.FilterListPossibilities<IFileStore, String>(data().getValue2()) {
						public String getName() {
							return Local.Extensions.toString();
						}
						@Override
						protected String getName(String possibility) {
							return possibility;
						}
						@Override
						protected boolean accept(IFileStore element, String possibility) {
							return element.getName().endsWith(possibility);
						}
					};
    				FlatPagedListDialog<IFileStore> dlg = new FlatPagedListDialog<IFileStore>(shell, Local.Files_not_detected.toString(), data().getValue1(), 20, new FlatPagedListDialog.TextProvider<IFileStore>() {
    					@Override
    					protected String getText(IFileStore element) {
    						return URLDecoder.decode(element.toURI().toString());
    					}
    				}, filters);
    				dlg.openProgressive(null, OrientationY.BOTTOM, false);
    			}
    		});
    		dlg.addLinkListener("extensions", new RunnableWithData<List<String>>(new ArrayList<String>(extensions)) {
    			public void run() {
    				FlatPagedListDialog<String> dlg = new FlatPagedListDialog<String>(shell, Local.Extensions.toString(), data(), 25, new FlatPagedListDialog.TextProvider<String>() {
    					@Override
    					protected String getText(String element) {
    						return element;
    					}
    				}, null);
    				dlg.openProgressive(null, OrientationY.BOTTOM, false);
    			}
    		});
    		dlg.open(Local.Data_added.toString());
    	}
	}
}
