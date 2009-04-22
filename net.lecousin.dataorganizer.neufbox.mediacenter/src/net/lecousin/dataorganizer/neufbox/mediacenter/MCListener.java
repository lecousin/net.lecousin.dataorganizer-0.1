package net.lecousin.dataorganizer.neufbox.mediacenter;

import java.io.ByteArrayInputStream;
import java.io.File;

import net.lecousin.dataorganizer.core.DataOrganizer;
import net.lecousin.dataorganizer.neufbox.mediacenter.internal.EclipsePlugin;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.xml.XmlUtil;
import net.lecousin.framework.xml.XmlWriter;
import net.lecousin.neufbox.mediacenter.Folder;
import net.lecousin.neufbox.mediacenter.Media;
import net.lecousin.neufbox.mediacenter.MediaCenter;
import net.lecousin.neufbox.mediacenter.eclipse.SharedDataView;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Element;

public class MCListener implements Listener<SharedDataView> {

	public MCListener() {
	}

	private MediaCenter mc = null;
	
	public void fire(SharedDataView event) {
		mc = event.getMediaCenter();
		load();
		mc.itemAdded().addFireListener(new Runnable() {
			public void run() {
				save();
			}
		});
		mc.itemRemoved().addFireListener(new Runnable() {
			public void run() {
				save();
			}
		});
	}
	
	private void save() {
		IProject p = DataOrganizer.getProject(EclipsePlugin.ID);
		if (p == null) return;
		XmlWriter xml = new XmlWriter();
		xml.openTag("mediacenter");
		save(mc.getRoot(), xml);
		xml.closeTag();
		IFile file = p.getFile("content");
		ByteArrayInputStream stream = new ByteArrayInputStream(xml.getXML().getBytes());
		try {
			if (!file.exists())
				file.create(stream, true, null);
			else
				file.setContents(stream, true, false, null); 
		} catch (CoreException e) {
			if (Log.error(this))
				Log.error(this, "Unable to save MediaCenter content", e);
		}
	}
	private void save(Folder f, XmlWriter xml) {
		for (Media m : f.getMedias()) {
			xml.openTag("media");
			xml.addAttribute("type", m.getType());
			xml.addAttribute("name", m.getName());
			xml.addAttribute("path", m.getFile().getAbsolutePath());
			xml.closeTag();
		}
		for (Folder sf : f.getSubFolders()) {
			xml.openTag("folder");
			xml.addAttribute("name", sf.getName());
			save(sf, xml);
			xml.closeTag();
		}
	}
	
	private void load() {
		IProject p = DataOrganizer.getProject(EclipsePlugin.ID);
		if (p == null) return;
		IFile file = p.getFile("content");
		if (!file.exists()) return;
		Element root;
		try { root = XmlUtil.loadFile(file.getContents()); }
		catch (Throwable t) {
			if (Log.error(this))
				Log.error(this, "Unable to load MediaCenter content", t);
			return;
		}
		load(root, mc.getRoot());
	}
	private void load(Element node, Folder f) {
		for (Element e : XmlUtil.get_childs_element(node, "media")) {
			int type = Integer.parseInt(e.getAttribute("type"));
			String name = e.getAttribute("name");
			String path = e.getAttribute("path");
			f.newMedia(name, type, new File(path), null);
		}
		for (Element e : XmlUtil.get_childs_element(node, "folder")) {
			String name = e.getAttribute("name");
			Folder sf = f.newSubFolder(name);
			load(e, sf);
		}
	}
}
