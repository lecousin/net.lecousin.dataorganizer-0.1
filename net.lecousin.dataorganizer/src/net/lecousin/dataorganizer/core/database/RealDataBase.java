package net.lecousin.dataorganizer.core.database;

import java.io.ByteArrayInputStream;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.InitializationException;
import net.lecousin.dataorganizer.core.database.content.ContentType;
import net.lecousin.dataorganizer.core.database.refresh.RefreshOptions;
import net.lecousin.dataorganizer.core.database.refresh.Refresher;
import net.lecousin.dataorganizer.core.database.source.DataSource;
import net.lecousin.dataorganizer.core.database.version.VersionLoader;
import net.lecousin.dataorganizer.ui.dialog.RefreshDialog;
import net.lecousin.framework.Pair;
import net.lecousin.framework.Triple;
import net.lecousin.framework.collections.CollectionUtil;
import net.lecousin.framework.event.Event;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.strings.StringUtil;
import net.lecousin.framework.version.Version;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Shell;

public class RealDataBase extends DataBase {

	public static final Version CURRENT_VERSION = new Version(0,1,0);
	
	public RealDataBase(WorkProgress progress, int amount) throws InitializationException {
		super(ResourcesPlugin.getWorkspace().getRoot().getProject("database"), progress, amount * 5 / 100);
		loadDataBase(progress, amount - (amount * 5 / 100));
	}
	
	private void loadDataBase(WorkProgress progress, int amount) throws InitializationException {
		int stepBrowse = amount * 10 / 100;
		int stepLoad = amount - stepBrowse;
		WorkProgress browseProjectProgress = progress.addSubWork(Local.Reading_database_content.toString(), stepBrowse, 10000);
		WorkProgress loadDataProgress = progress.addSubWork(Local.Loading_data.toString(), stepLoad, 10000);

		List<Long> files = load_browseProject(browseProjectProgress, 10000);
		VersionLoader loader;
		try { loader = getVersion(); }
		catch (CoreException e) {
			throw new InitializationException("Unable to read database", e);
		}
		if (loader.getDB() == null) {
			if (Log.warning(this))
				Log.warning(this, "No version information in database: the current version will be used");
			loader.addLoader("db", CURRENT_VERSION);
		}
		try {
			if (!loader.checkVersions())
				throw new InitializationException("Database loading cancelled.", new Exception("User don't want to convert the database."));
		} catch (Exception e) {
			throw new InitializationException("Invalid database version", e);
		}
		loadDataProgress.setAmount(files.size());
		load_loadData(files, loader, loadDataProgress);
		if (loader.isDBPreviousVersion()) {
			for (Data d : getAllData()) {
				((RealData)d).save();
			}
		}
		updateVersions();
	}
	
	
	private List<Long> load_browseProject(WorkProgress progress, int amount) throws InitializationException {
		List<Long> files = new LinkedList<Long>();
		try {
			IResource[] list1 = project.members();
			int nb = list1.length;
			for (IResource res1 : list1) {
				int step = amount / nb--;
				amount -= step;
				if (checkRes(res1, true, true)) {
					IResource[] list2 = ((IFolder)res1).members();
					WorkProgress progress1 = progress.addSubWork(null, step, list2.length);
					for (IResource res2 : list2) {
						if (checkRes(res2, true, false)) {
							IResource[] list3 = ((IFolder)res2).members();
							WorkProgress progress2 = progress1.addSubWork(null, 1, list3.length);
							for (IResource res3 : list3) {
								if (checkRes(res3, true, false)) {
									IResource[] list4 = ((IFolder)res3).members();
									WorkProgress progress3 = progress2.addSubWork(null, 1, list4.length);
									for (IResource res4 : list4) {
										if (res4 instanceof IFile && checkRes(res4, false, false)) {
											String s = res1.getName() + res2.getName() + res3.getName() + res4.getName();
											long id = Long.parseLong(s, 16);
											files.add(id);
										}
										progress3.progress(1);
									}
								} else
									progress2.progress(1);
							}
						} else
							progress1.progress(1);
					}
				} else
					progress.progress(step);
			}
		} catch (CoreException e) {
			progress.done();
			throw new InitializationException("Unable to load database.", e);
		}
		return files;
	}
	private boolean checkRes(IResource res, boolean checkFolder, boolean isRoot) {
		if (checkFolder && !(res instanceof IFolder)) {
			if (isRoot) {
				if (res.getName().equals(".project"))
					return false;
				if (res.getName().startsWith("version."))
					return false;
				if (res.getName().startsWith("opened."))
					return false;
			}
			if (Log.warning(this))
				Log.warning(this, "Unexpected file '" + res.getProjectRelativePath().toString() + "' in database.");
			return false;
		}
		String name = res.getName();
		if (name.length() != 2 || !StringUtil.isHexa(name.charAt(0)) || !StringUtil.isHexa(name.charAt(0))) {
			if (Log.warning(this))
				Log.warning(this, "Unexpected resource '" + res.getProjectRelativePath().toString() + "' in database.");
			return false;
		}
		return true;
	}
	private VersionLoader getVersion() throws CoreException, InitializationException {
		VersionLoader loader = new VersionLoader();
		for (IResource res : project.members()) {
			if (!(res instanceof IFile)) continue;
			String name = res.getName();
			if (!name.startsWith("version.")) continue;
			name = name.substring(8);
			int i = name.indexOf('.');
			if (i <= 0 || i == name.length()-1) continue;
			String type = name.substring(0, i);
			name = name.substring(i+1);
			Version version = new Version(name);
			loader.addLoader(type, version);
		}
		return loader;
	}
	private void updateVersions() {
		try {
			for (IResource res : project.members()) {
				if (!(res instanceof IFile)) continue;
				String name = res.getName();
				if (!name.startsWith("version.")) continue;
				try { res.delete(true, null);}
				catch (CoreException e) {
					if (Log.warning(this))
						Log.warning(this, "Unable to remove version file " + res.getName());
				}
			}		
		} catch (CoreException e) {
			if (Log.warning(this))
				Log.warning(this, "Unable to browse database project");
		}
		IFile file;
		file = project.getFile("version.db." + CURRENT_VERSION.toString());
		if (!file.exists())
			try { file.create(new ByteArrayInputStream(new byte[0]), true, null); }
			catch (CoreException e) {
				if (Log.error(this))
					Log.error(this, "Unable to create DataBase version file marker: this may result to an error the next time the database will be opened.");
			}
		for (ContentType type : ContentType.getAvailableTypes()) {
			file = project.getFile("version." + type.getID() + "." + type.getCurrentVersion().toString());
			if (!file.exists())
				try { file.create(new ByteArrayInputStream(new byte[0]), true, null); }
				catch (CoreException e) {
					if (Log.error(this))
						Log.error(this, "Unable to create version file marker for content type " + type.getID() + ": this may result to an error the next time the database will be opened.");
				}
		}
	}
	
	private void load_loadData(List<Long> files, VersionLoader loader, WorkProgress progress) throws InitializationException {
		for (Long id : files) {
			try { 
				RealData data = new RealData(this, id, loader);
				dataAdded(data);
			} catch (Throwable t) {
				if (Log.error(this))
					Log.error(this, "Unable to load data id " + StringUtil.toStringHex(id, 8), t);
			}
			progress.progress(1);
		}
	}
	
	public void close() {
		
	}
	
	@Override
	protected RealData createData(long id, String name, ContentType type, List<DataSource> sources) throws CoreException {
		return new RealData(this, id, name, type, sources);
	}
	
	public synchronized void refresh(Shell shell) {
		RefreshDialog dlg = new RefreshDialog(shell);
		RefreshOptions options = dlg.open();
		if (options == null) return;
		Refresher.refresh(shell, this, getAllData(), options);
	}

	public Event<Triple<Data,List<Long>,Boolean>> dataOpenedChanged = new Event<Triple<Data,List<Long>,Boolean>>();
	void signalDataOpened(Data data, long date) {
		IFile file = project.getFile("opened." + data.id + '.' + date);
		if (file.exists()) return;
		try { file.create(new ByteArrayInputStream(new byte[0]), true, null); dataOpenedChanged.fire(new Triple<Data,List<Long>,Boolean>(data, CollectionUtil.single_element_list(date), true)); }
		catch (CoreException e) {
			if (Log.error(this))
				Log.error(this, "Unable to create file to signal a data has been opened.", e);
		}
	}
	private Pair<Data,Long> getOpenedDataFromFile(IResource res) {
		if (!(res instanceof IFile)) return null;
		if (!res.getName().startsWith("opened.")) return null;
		int i = res.getName().indexOf('.');
		int j = res.getName().lastIndexOf('.');
		if (i < 0 || j < 0 || i == j || j == res.getName().length()-1 || j == i+1) {
			if (Log.warning(this))
				Log.warning(this, "Invalid opened marker file: " + res.getName());
			return null;
		}
		String s1 = res.getName().substring(i+1, j);
		String s2 = res.getName().substring(j+1);
		int id;
		long date;
		try {
			id = Integer.parseInt(s1);
			date = Long.parseLong(s2);
		} catch (NumberFormatException e) {
			if (Log.warning(this))
				Log.warning(this, "Invalid opened marker file: " + res.getName());
			return null;
		}
		Data data = get(id);
		if (data == null) {
			if (Log.warning(this))
				Log.warning(this, "Invalid opened marker file: " + res.getName() + ": data does not exist => remove the marker.");
			try { res.delete(true, null); }
			catch (CoreException e) {
				if (Log.error(this))
					Log.error(this, "Unable to remove file " + res.getName(), e);
			}
			return null;
		}
		return new Pair<Data,Long>(data, date);
	}
	public List<Pair<Data,Long>> getOpenedData() {
		List<Pair<Data,Long>> result = new LinkedList<Pair<Data,Long>>();
		IResource[] members;
		try { members = project.members(); }
		catch (CoreException e) {
			if (Log.error(this))
				Log.error(this, "Unable to get list of files under database project.", e);
			return result;
		}
		for (IResource res : members) {
			Pair<Data,Long> p = getOpenedDataFromFile(res);
			if (p != null)
				result.add(p);
		}
		return result;
	}
	public void processDataOpened(Data data, List<Long> dates) {
		if (!data.openedDialog(dates)) return;
		for (long date : dates) {
			IFile file = project.getFile("opened." + data.id + "." + date);
			if (file.exists())
				try { file.delete(true, null); }
				catch (CoreException e) {
					if (Log.error(this))
						Log.error(this, "Unable to remove data opened marker file", e);
				}
		}
		dataOpenedChanged.fire(new Triple<Data, List<Long>, Boolean>(data, dates, false));
	}
	public void removeDataOpened(Data data, long date, boolean count) {
		IFile file = project.getFile("opened." + data.id + "." + date);
		if (!file.exists()) return;
		try { file.delete(true, null); }
		catch (CoreException e) {
			if (Log.error(this))
				Log.error(this, "Unable to remove data opened marker file", e);
		}
		if (count) {
			data.views.add(date);
			data.signalModification();
		}
		dataOpenedChanged.fire(new Triple<Data, List<Long>, Boolean>(data, CollectionUtil.single_element_list(date), false));
	}
	public void countDataOpened(Data data, long date) {
		removeDataOpened(data, date, true);
	}
	public void ignoreDataOpened(Data data, long date) {
		removeDataOpened(data, date, false);
	}
	public void ignoreAllDataOpened() {
		IResource[] members;
		try { members = project.members(); }
		catch (CoreException e) {
			if (Log.error(this))
				Log.error(this, "Unable to get list of files under database project.", e);
			return;
		}
		for (IResource res : members) {
			Pair<Data,Long> p = getOpenedDataFromFile(res);
			if (p == null) continue;
			try { res.delete(true, null); }
			catch (CoreException e) {
				if (Log.error(this))
					Log.error(this, "Unable to remove data opened marker file " + res.getName());
			}
			dataOpenedChanged.fire(new Triple<Data, List<Long>, Boolean>(p.getValue1(), CollectionUtil.single_element_list(p.getValue2()), false));
		}
	}
	public void countAllDataOpened() {
		IResource[] members;
		try { members = project.members(); }
		catch (CoreException e) {
			if (Log.error(this))
				Log.error(this, "Unable to get list of files under database project.", e);
			return;
		}
		for (IResource res : members) {
			Pair<Data,Long> p = getOpenedDataFromFile(res);
			if (p == null) continue;
			try { res.delete(true, null); }
			catch (CoreException e) {
				if (Log.error(this))
					Log.error(this, "Unable to remove data opened marker file " + res.getName());
			}
			p.getValue1().views.add(p.getValue2());
			dataOpenedChanged.fire(new Triple<Data, List<Long>, Boolean>(p.getValue1(), CollectionUtil.single_element_list(p.getValue2()), false));
		}
	}
	
	@Override
	synchronized void internal_removeData(Data data) {
		super.internal_removeData(data);
		try {
			for (IResource res : project.members()) {
				Pair<Data,Long> p = getOpenedDataFromFile(res);
				if (p == null) continue;
				if (p.getValue1() == data) {
					try { res.delete(true, null); }
					catch (CoreException e) {
						if (Log.warning(this))
							Log.warning(this, "Unable to remove data opened marker file", e);
					}
					dataOpenedChanged.fire(new Triple<Data, List<Long>, Boolean>(p.getValue1(), CollectionUtil.single_element_list(p.getValue2()), false));
				}
			}
		} catch (CoreException e) {
			if (Log.warning(this))
				Log.warning(this, "Unable to list files from database project: unable to remove data opened marker files for the removed data.", e);
		}
	}
}
