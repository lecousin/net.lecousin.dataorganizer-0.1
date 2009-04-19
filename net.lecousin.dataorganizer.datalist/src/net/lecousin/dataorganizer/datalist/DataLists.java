package net.lecousin.dataorganizer.datalist;

import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.core.DataOrganizer;
import net.lecousin.dataorganizer.datalist.internal.EclipsePlugin;
import net.lecousin.framework.event.Event;
import net.lecousin.framework.io.FileSystemUtil;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.xml.XmlUtil;
import net.lecousin.framework.xml.XmlWriter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Element;

public class DataLists {

	private DataLists() { load(); }
	private static DataLists instance = null;
	public static final DataLists getInstance() { return instance != null ? instance : (instance = new DataLists()); }
	
	private List<DataList> lists = new LinkedList<DataList>();
	public Event<DataList> listAdded = new Event<DataList>();
	public Event<DataList> listRemoved = new Event<DataList>();
	
	public List<DataList> getLists() { return lists; }
	public DataList getList(String name) {
		for (DataList list : lists)
			if (list.getName().equals(name))
				return list;
		return null;
	}
	public DataList createList(String name) {
		DataList list = new DataList(name);
		lists.add(list);
		save(list);
		listAdded.fire(list);
		return list;
	}
	public void removeList(DataList list) {
		IProject p = DataOrganizer.getProject(EclipsePlugin.ID);
		if (p != null) {
			IFile file = p.getFile(list.getName() + ".list");
			if (file.exists())
				try { file.delete(true, null); }
				catch (CoreException e) {
					if (Log.error(this))
						Log.error(this, "Unable to remove data list file " + file.getName(), e);
				}
		}
		lists.remove(list);
		listRemoved.fire(list);
	}
	
	private void load() {
		IProject p = DataOrganizer.getProject(EclipsePlugin.ID);
		if (p == null) return;
		try {
			for (IResource r : p.members()) {
				if (!(r instanceof IFile)) continue;
				if (!FileSystemUtil.getFileNameExtension(r.getName()).equals("list")) continue;
				DataList l = loadList((IFile)r);
				if (l != null)
					lists.add(l);
			}
		} catch (CoreException e) {
			if (Log.error(this))
				Log.error(this, "Unable to load data lists", e);
		}
	}
	private DataList loadList(IFile file) {
		try {
			Element root = XmlUtil.loadFile(file.getContents());
			DataList list = new DataList(FileSystemUtil.getFileNameWithoutExtension(file.getName()));
			for (Element e : XmlUtil.get_childs_element(root, "data"))
				list._addDataID(Long.parseLong(e.getAttribute("id")));
			return list;
		} catch (Throwable t) {
			if (Log.error(this))
				Log.error(this, "Unable to load data list from file " + file.getName(), t);
			return null;
		}
	}
	void save(DataList list) {
		XmlWriter xml = new XmlWriter();
		xml.openTag("data_list");
		for (Long id : list.getDataIDs())
			xml.openTag("data").addAttribute("id", id).closeTag();
		xml.closeTag();
		IProject p = DataOrganizer.getProject(EclipsePlugin.ID);
		if (p == null) return;
		IFile file = p.getFile(list.getName() + ".list");
		try { xml.writeToFile(file.getLocation().toFile().getAbsolutePath()); }
		catch (Throwable t) {
			if (Log.error(this))
				Log.error(this, "Unable to save data list " + list.getName(), t);
		}
	}
	
}
