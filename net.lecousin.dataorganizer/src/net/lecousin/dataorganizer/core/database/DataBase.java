package net.lecousin.dataorganizer.core.database;

import java.util.ArrayList;
import java.util.List;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.InitializationException;
import net.lecousin.dataorganizer.core.database.Data.DuplicateAnalysis;
import net.lecousin.dataorganizer.core.database.content.ContentType;
import net.lecousin.dataorganizer.core.database.content.DataContentType;
import net.lecousin.dataorganizer.core.database.info.Info;
import net.lecousin.dataorganizer.core.database.refresh.RefreshOptions;
import net.lecousin.dataorganizer.core.database.refresh.Refresher;
import net.lecousin.dataorganizer.core.database.source.DataSource;
import net.lecousin.dataorganizer.ui.datalist.DataListMenu;
import net.lecousin.framework.IDManager;
import net.lecousin.framework.Pair;
import net.lecousin.framework.collections.SelfMapUniqueLong;
import net.lecousin.framework.eclipse.progress.ProgressMonitor_WorkProgressWrapper;
import net.lecousin.framework.event.Event;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.strings.StringUtil;
import net.lecousin.framework.thread.RunnableWithData;
import net.lecousin.framework.ui.eclipse.dialog.QuestionDlg;
import net.lecousin.framework.ui.eclipse.dialog.QuestionDlg.Answer;
import net.lecousin.framework.ui.eclipse.dialog.QuestionDlg.AnswerSimple;
import net.lecousin.framework.ui.eclipse.dialog.QuestionDlg.ContextualOptions.Option;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Shell;

public abstract class DataBase {

	public DataBase(IProject project, WorkProgress progress, int amount) throws InitializationException {
		this.project = project; 
		ids = new IDManager();
		WorkProgress openProjectProgress = progress.addSubWork(Local.Opening_database.toString(), amount, 100);
		load_openProject(openProjectProgress, 100);
	}
	
	private void load_openProject(WorkProgress progress, int amount) throws InitializationException {
		progress.progress(amount*2/100);
		if (!project.exists()) {
			progress.setSubDescription(Local.No_database_exist + ": " + Local.creation);
			try { 
				project.create(null); 
				progress.progress(amount*5/100);
			}
			catch (CoreException e) {
				progress.done();
				throw new InitializationException("Unable to create database project in the workspace.", e);
			}
		}
		try { 
			if (!project.isOpen())
				project.open(null);
			progress.progress(amount*15/100);
			progress.setSubDescription(Local.Refreshing_database_content.toString());
			project.refreshLocal(IResource.DEPTH_INFINITE, new ProgressMonitor_WorkProgressWrapper(progress.addSubWork(null, progress.getRemainingWork(), 100)));
		}
		catch (CoreException e) {
			progress.done();
			throw new InitializationException("Unable to open database.", e);
		}
	}
	
	protected IProject project;
	private IDManager ids;
	private SelfMapUniqueLong<Data> data = new SelfMapUniqueLong<Data>();
	
	private Event<Data> dataChanged = new Event<Data>();
	private Event<DataContentType> dataContentChanged = new Event<DataContentType>();
	private Event<Data> dataAdded = new Event<Data>();
	private Event<Data> dataRemoved = new Event<Data>();
	private Event.Listener<Data> dataChangedListener = new Event.Listener<Data>() {
		public void fire(Data event) {
			dataChanged.fire(event);
		}
	};
	private Event.Listener<DataContentType> dataContentChangedListener = new Event.Listener<DataContentType>() {
		public void fire(DataContentType event) {
			dataContentChanged.fire(event);
		}
	};
	
	public synchronized Data get(long id) { return data.get(id); }
	public synchronized List<Data> getAllData() { return new ArrayList<Data>(data); }
	
	public Event<Data> dataChanged() { return dataChanged; }
	public Event<DataContentType> dataContentChanged() { return dataContentChanged; }
	public Event<Data> dataAdded() { return dataAdded; }
	public Event<Data> dataRemoved() { return dataRemoved; }
	
	public abstract void close();
	
	public final synchronized Data addDataFromOtherDB(Data newData) throws CoreException {
		Data data = addData(newData.getName(), newData.getContentType(), newData.getSources());
		try {
			IFolder srcFolder = newData.getFolder();
			IFolder dstFolder = data.getFolder();
			if (srcFolder.exists()) {
				if (!dstFolder.exists())
					srcFolder.move(dstFolder.getFullPath(), true, null);
				else {
					IResource[] members = srcFolder.members();
					for (IResource res : members)
						if (res instanceof IFile)
							res.move(dstFolder.getFile(res.getName()).getFullPath(), true, null);
						else
							res.move(dstFolder.getFolder(res.getName()).getFullPath(), true, null);
				}
			}
		} catch (CoreException e) {
			if (Log.warning(this))
				Log.warning(this, "Unable to move data from source DB to target DB.", e);
		}
		newData.getContent().setData(data);
		data.setContent(newData.getContent());
		return data;
	}
	public final synchronized Data addData(String name, ContentType type, List<DataSource> sources) throws CoreException {
		long id = ids.allocate();
		try {
			Data d = createData(id, name, type, sources);
			data.add(d);
			dataAdded.fire(d);
			d.modified().addListener(dataChangedListener);
			d.contentModified().addListener(dataContentChangedListener);
			return d;
		} catch (CoreException e) {
			ids.free(id);
			throw e;
		}
	}
	protected abstract Data createData(long id, String name, ContentType type, List<DataSource> sources) throws CoreException;
	protected void dataAdded(Data data) {
		ids.allocate(data.id);
		this.data.add(data);
		data.modified().addListener(dataChangedListener);
		data.contentModified().addListener(dataContentChangedListener);
	}
	
	public final void removeData(Data data) {
		data.remove();
	}

	synchronized void internal_removeData(Data data) {
		this.data.remove(data);
		try {
			IFile file = getFile(data.id);
			if (file != null && file.exists()) file.delete(true, null);
			IFolder folder = getFolder(data.id);
			if (folder != null && folder.exists()) folder.delete(true, null);
			removeIfEmpty(file.getParent());
		} catch (CoreException e) {
			if (Log.error(this))
				Log.error(this, "Unable to remove data cleanly: some files may stay in the database. A clean database should be launched.");
		}
		ids.free(data.id);
		dataRemoved.fire(data);
	}
	
	public final synchronized Data getDataFromInfoSourceID(ContentType type, String source, String id) {
		String typeID = type.getID();
		for (Data d : data) {
			if (!d.getContentType().getID().equals(typeID)) continue;
			Info info = d.getContent().getInfo();
			if (info == null) continue;
			String sourceID = info.getSourceID(source);
			if (id.equals(sourceID)) return d;
		}
		return null;
	}
	public final synchronized Data getDataFromInfoSourceIDs(ContentType type, List<Pair<String,String>> sourceIDs) {
		String typeID = type.getID();
		for (Data d : data) {
			if (!d.getContentType().getID().equals(typeID)) continue;
			DataContentType dct = d.getContent();
			Info info = dct.getInfo();
			if (info == null) continue;
			for (Pair<String,String> source : sourceIDs) {
				String sourceID = info.getSourceID(source.getValue1());
				if (source.getValue2().equals(sourceID)) return d;
			}
		}
		return null;
	}
	
	
	public final synchronized boolean checkForDuplicate(Shell shell, Data newData, QuestionDlg.ContextualOptions context, WorkProgress progress) {
		progress.setAmount(this.data.size());
		try {
		for (Data data : this.data) {
			progress.progress(1);
			if (data.getContentType() != newData.getContentType()) continue;
			DuplicateAnalysis result = data.checkForDuplicate(newData);
			switch (result) {
			case DIFFERENT: break;
			case EXACTLY_THE_SAME: return false;
			case SAME_IN_DIFFERENT_LOCATION: {
				String optionID;
				String endOfMessage;
				if (data.checkExistsOnLocal()) {
					optionID = "same_exist";
					endOfMessage = "";
				} else {
					optionID = "same_move";
					endOfMessage = "<br><b>" + Local.current_data_no_more_in_database + ".</b>";
				}
				String answer = null;
				Option ctxOpt = context.getOption(optionID); 
				if (ctxOpt == null)
					context.addOption(new Option(optionID, Local.Do_action_for_same_situation.toString(), false));
				else {
					if (ctxOpt.selection)
						answer = (String)ctxOpt.data;
				}
				if (answer == null) {
					QuestionDlg dlg = new QuestionDlg(shell, Local.Duplicate_data.toString(), context);
					StringBuilder s1 = new StringBuilder();
					for (DataSource s : newData.getSources())
						s1.append(" - ").append(s.toString()).append("<br>");
					StringBuilder s2 = new StringBuilder();
					for (DataSource s : data.getSources())
						s2.append(" - ").append(s.toString()).append("<br>");
					dlg.setMessage(
							Local.The_new_data + "<br><a href=\"new_data\">" + newData.getName() + "</a>:<br>" +
							s1.toString() +
							Local.is_exactly_the_same_as_data + "<br><a href=\"old_data\">" + data.getName() + "</a>:<br>" +
							s2.toString() +
							Local.that_is_currently_present_in_the_database + ".<br>" +
							endOfMessage + "<br>" +
							Local.What_do_you_want_to_do);
					dlg.handleHyperlinkMessage("new_data", new RunnableWithData<Data>(newData) {
						public void run() {
							DataListMenu.menu(data(), false);
						}
					});
					dlg.handleHyperlinkMessage("old_data", new RunnableWithData<Data>(data) {
						public void run() {
							DataListMenu.menu(data(), false);
						}
					});
					dlg.setAnswers(new Answer[] {
							new AnswerSimple("add", Local.Add_the_new_data_anyway.toString()),
							new AnswerSimple("update", Local.Update_current_data_with_new_location.toString()),
							new AnswerSimple("replace", Local.Replace_completly_the_current_data.toString()),
							new AnswerSimple("remove_new_from_fs", Local.Remove_the_new_data_from_the_filesystem.toString()),
							new AnswerSimple("remove_old_from_fs", Local.Remove_the_old_data_from_the_filesystem_and_replace_by_new.toString()),
							new AnswerSimple("nothing", Local.Do_not_do_anything__skip_new_data.toString()),
					});
					dlg.show();
					answer = dlg.getAnswerID();
					if (answer == null) 
						progress.cancel();
					else {
						context.getOption(optionID).data = answer;
					}
				}
				
				if (answer == null) return false;
				if (answer.equals("add")) return true;
				if (answer.equals("update")) { data.updateSources(newData.getSources()); return false; }
				if (answer.equals("replace")) { removeData(data); return true; }
				if (answer.equals("remove_new_from_fs")) { newData.removeFromFileSystem(); return false; }
				if (answer.equals("remove_old_from_fs")) { data.removeFromFileSystem(); data.updateSources(newData.getSources()); return false; }
				return false;
			}
			case SEEMS_THE_SAME_BUT_DIFFERENT_SOURCES:
				// TODO
				break;
			}
		}
		return true;
		} finally {
			progress.done();
		}
	}

	public void refresh(RefreshOptions options) {
		refresh(getAllData(), options);
	}
	public void refresh(List<Data> toRefresh, RefreshOptions options) {
		Refresher.refresh(this, toRefresh, options);
	}
	
	final IFile getFile(long id) throws CoreException {
		IFolder parent = getParentFolder(id);
		return parent.getFile(StringUtil.toStringHex(id & 0xFF, 2));
	}
	final IFolder getFolder(long id) throws CoreException {
		IFolder parent = getParentFolder(id);
		return parent.getFolder(StringUtil.toStringHex(id & 0xFF, 2)+".data");
	}
	private IFolder getParentFolder(long id) throws CoreException {
		IFolder f = getSubFolder(project, StringUtil.toStringHex((id & 0xFF000000) >> 24, 2));
		f = getSubFolder(f, StringUtil.toStringHex((id & 0xFF0000) >> 16, 2));
		f = getSubFolder(f, StringUtil.toStringHex((id & 0xFF00) >> 8, 2));
		return f;
	}
	private void removeIfEmpty(IContainer container) {
		if (!(container instanceof IFolder)) return;
		IFolder folder = (IFolder)container;
		if (folder.exists()) {
			try {
				if (folder.members().length == 0) {
					folder.delete(true, false, null);
				}
			} catch (CoreException e) {
			}
		}
		removeIfEmpty(folder.getParent());
	}
	
	private IFolder getSubFolder(IContainer parent, String name) throws CoreException {
		IFolder folder = parent.getFolder(new Path(name));
		if (!folder.exists())
			folder.create(true, true, null);
		return folder;
	}
	
	void signalDataOpened(Data data, long date) {
		// nothing by default
	}
}
