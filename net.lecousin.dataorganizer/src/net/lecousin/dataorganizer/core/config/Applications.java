package net.lecousin.dataorganizer.core.config;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.filechooser.FileSystemView;

import net.lecousin.dataorganizer.core.DataOrganizer;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.source.DataSource;
import net.lecousin.dataorganizer.internal.EclipsePlugin;
import net.lecousin.framework.Pair;
import net.lecousin.framework.collections.CollectionUtil;
import net.lecousin.framework.io.FileSystemUtil;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.ui.eclipse.SWT_AWT_Util;
import net.lecousin.framework.xml.XmlUtil;
import net.lecousin.framework.xml.XmlWriter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Element;

public class Applications {

	private Applications() { load(); }
	private static Applications instance = null;
	public static synchronized Applications getInstance() { return instance != null ? instance : (instance = new Applications()); }

	private Program[] programs;
	private Map<String,Pair<List<String>,List<String>>> extensions = new HashMap<String,Pair<List<String>,List<String>>>();
	private IProject project;
	
	public static interface RunningApp {
		public String getName();
		public Image getIcon();
	}
	private static class RunningProgram implements RunningApp {
		RunningProgram(Program p) { this.p = p; }
		Program p;
		public String getName() { return p.getName(); }
		public Image getIcon() { return p.getImageData() != null ? new Image(Display.getDefault(), p.getImageData()) : null; }
	}
	private static class RunningApplication implements RunningApp {
		RunningApplication(File file) { this.file = file; }
		File file;
		Image icon = null;
		boolean iconLoaded = false;
		public String getName() { return FileSystemUtil.getFileNameWithoutExtension(file.getName()); }
		public Image getIcon() {
			if (iconLoaded) return icon;
			iconLoaded = true;
			Icon i = FileSystemView.getFileSystemView().getSystemIcon(file);
			if (i == null) return null;
			icon = new Image(Display.getDefault(), SWT_AWT_Util.convertToSWT(i));
			return icon;
		}
	}
	
	public List<RunningApp> getApplications(Data data) {
		String ext = getExtension(data);
		if (ext == null) return CollectionUtil.emptyList(RunningApp.class);
		return getExtension(ext);
	}
	private String getExtension(Data data) {
		List<DataSource> sources = data.getSources();
		if (sources.isEmpty()) return null;
		return FileSystemUtil.getExtension(sources.get(0).getFileName());
	}
	
	public List<RunningApp> getExtension(String ext) {
		Pair<List<String>,List<String>> p = extensions.get(ext);
		if (p == null) return CollectionUtil.emptyList(RunningApp.class);
		List<RunningApp> list = new LinkedList<RunningApp>();
		for (String s : p.getValue1()) {
			Program prog = getProgram(s);
			if (prog != null)
				list.add(new RunningProgram(prog));
		}
		for (String s : p.getValue2()) {
			File file = new File(s);
			if (file.exists())
				list.add(new RunningApplication(file));
		}
		return list;
	}
	
	private Program getProgram(String name) {
		for (Program p : programs)
			if (p.getName().equals(name))
				return p;
		return null;
	}
	
	public Program[] getAllPrograms() { return programs; }
	
	public void launch(RunningApp app, Data data) {
		if (app instanceof RunningProgram)
			_launch(((RunningProgram)app).p, data);
		else
			_launch(((RunningApplication)app).file.getAbsolutePath(), data);
	}
	public void launch(Program prog, Data data) {
		String ext = getExtension(data);
		if (ext != null) {
			Pair<List<String>,List<String>> p = extensions.get(ext);
			if (p == null) {
				p = new Pair<List<String>,List<String>>(new LinkedList<String>(),new LinkedList<String>());
				extensions.put(ext, p);
			}
			if (!p.getValue1().contains(prog.getName())) {
				p.getValue1().add(prog.getName());
				save();
			}
		}
		_launch(prog, data);
	}
	public void launch(String appPath, Data data) {
		String ext = getExtension(data);
		if (ext != null) {
			Pair<List<String>,List<String>> p = extensions.get(ext);
			if (p == null) {
				p = new Pair<List<String>,List<String>>(new LinkedList<String>(),new LinkedList<String>());
				extensions.put(ext, p);
			}
			if (!p.getValue2().contains(appPath)) {
				p.getValue2().add(appPath);
				save();
			}
		}
		_launch(appPath, data);
	}
	private void _launch(Program p, Data data) {
		for (DataSource s : data.getSources()) {
			try {
				URI uri = s.ensurePresenceAndGetURI();
				File file = new File(uri);
				if (file.exists())
					p.execute(file.getAbsolutePath());
			} catch (Throwable t) {}
		}
	}
	private void _launch(String appPath, Data data) {
		List<String> files = new LinkedList<String>();
		for (DataSource s : data.getSources()) {
			try {
				URI uri = s.ensurePresenceAndGetURI();
				File file = new File(uri);
				if (file.exists())
					files.add(file.getAbsolutePath());
			} catch (Throwable t) {}
		}
		if (files.isEmpty()) return;
		String[] cmd = new String[files.size()+1];
		cmd[0] = appPath;
		for (int i = 0; i < files.size(); ++i)
			cmd[i+1] = files.get(i);
		try { Runtime.getRuntime().exec(cmd); }
		catch (IOException e) {}
	}
	public void remove(RunningApp app, Data data) {
		String ext = getExtension(data);
		if (ext == null) return;
		Pair<List<String>,List<String>> p = extensions.get(ext);
		if (p == null) return;
		if (app instanceof RunningProgram)
			p.getValue1().remove(((RunningProgram)app).p.getName());
		else
			p.getValue2().remove(((RunningApplication)app).file.getAbsolutePath());
	}
	
	private void load() {
		programs = Program.getPrograms();
		project = DataOrganizer.getProject(EclipsePlugin.ID);
		if (project == null) return;
		IFile file = project.getFile("applications");
		if (!file.exists()) return;
		Element root;
		try { root = XmlUtil.loadFile(file.getContents()); }
		catch (Throwable t) {
			if (Log.error(this))
				Log.error(this, "Unable to load applications file", t);
			return;
		}
		for (Element e : XmlUtil.get_childs_element(root, "extension")) {
			String ext = e.getAttribute("name");
			List<String> extPrograms = new LinkedList<String>();
			List<String> extApps = new LinkedList<String>();
			for (Element e2 : XmlUtil.get_childs_element(e, "program"))
				extPrograms.add(e2.getAttribute("name"));
			for (Element e2 : XmlUtil.get_childs_element(e, "application"))
				extApps.add(e2.getAttribute("path"));
			extensions.put(ext, new Pair<List<String>,List<String>>(extPrograms, extApps));
		}
	}
	private void save() {
		if (project == null) return;
		XmlWriter xml = new XmlWriter();
		xml.openTag("applications");
		for (String ext : extensions.keySet()) {
			xml.openTag("extension");
			xml.addAttribute("name", ext);
			Pair<List<String>,List<String>> p = extensions.get(ext);
			for (String prog : p.getValue1())
				xml.openTag("program").addAttribute("name", prog).closeTag();
			for (String app : p.getValue2())
				xml.openTag("application").addAttribute("path", app).closeTag();
			xml.closeTag();
		}
		xml.closeTag();
		ByteArrayInputStream stream = new ByteArrayInputStream(xml.getXML().getBytes());
		IFile file = project.getFile("applications");
		try {
			if (file.exists())
				file.setContents(stream, true, false, null);
			else
				file.create(stream, true, null);
		} catch (CoreException e) {
			if (Log.error(this))
				Log.error(this, "Unable to save applications file", e);
		}
	}
}
