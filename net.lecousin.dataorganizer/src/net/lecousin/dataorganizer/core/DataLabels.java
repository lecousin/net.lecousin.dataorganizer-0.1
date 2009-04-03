package net.lecousin.dataorganizer.core;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.internal.EclipsePlugin;
import net.lecousin.framework.Pair;
import net.lecousin.framework.eclipse.progress.ProgressMonitor_WorkProgressWrapper;
import net.lecousin.framework.event.Event;
import net.lecousin.framework.io.MyByteArrayOutputStream;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.ui.eclipse.dialog.ErrorDlg;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class DataLabels {

	DataLabels(WorkProgress progress, int amount) throws InitializationException {
		progress.setSubDescription(Local.Opening_labels_project.toString());
		project = ResourcesPlugin.getWorkspace().getRoot().getProject("labels");
		progress.progress(amount * 2 / 100);
		if (!project.exists()) {
			progress.setSubDescription(Local.Creating_labels_project.toString());
			try { project.create(null); }
			catch (CoreException e) {
				progress.done();
				throw new InitializationException("Unable to create labels project in the workspace.", e);
			}
			progress.progress(amount * 10 / 100);
		}
		try {
			progress.setSubDescription(Local.Opening_labels_project.toString());
			if (!project.isOpen())
				project.open(null);
			progress.progress(amount * 40 / 100);
			progress.setSubDescription(Local.Refreshing_labels.toString());
			project.refreshLocal(IResource.DEPTH_INFINITE, new ProgressMonitor_WorkProgressWrapper(progress.addSubWork(null, progress.getRemainingWork(), 10000)));
		}
		catch (CoreException e) {
			progress.done();
			throw new InitializationException("Unable to open labels.", e);
		}
		try { root = new Label(null, project); }
		catch (CoreException e) {
			throw new InitializationException("Unable to load labels.", e);
		} finally {
			progress.done();
		}
	}
	
	private IProject project;
	private Label root;
	private Event<Label> labelAdded = new Event<Label>();
	private Event<Label> labelRemoved = new Event<Label>();
	private Event<Label> labelRenamed = new Event<Label>();
	private Event<Pair<Label,Data>> labelAssigned = new Event<Pair<Label,Data>>();
	private Event<Pair<Label,Data>> labelUnassigned = new Event<Pair<Label,Data>>();
	
	public Label root() { return root; }
	
	public Event<Label> labelAdded() { return labelAdded; }
	public Event<Label> labelRemoved() { return labelRemoved; }
	public Event<Label> labelRenamed() { return labelRenamed; }
	public Event<Pair<Label,Data>> labelAssigned() { return labelAssigned; }
	public Event<Pair<Label,Data>> labelUnassigned() { return labelUnassigned; }
	
	public class Label {
		public Label(Label parent, IContainer folder) throws CoreException {
			this.parent = parent;
			this.folder = folder;
			for (IResource res : folder.members())
				if (res instanceof IContainer)
					children.add(new Label(this, (IContainer)res));
		}
		private IContainer folder;
		private Label parent;
		private List<Label> children = new LinkedList<Label>();
		private Set<Data> labeled = null;
		
		public Label getParent() { return parent; }
		public List<Label> getChildren() { return children; }
		public String getName() { return folder instanceof IProject ? Local.Labels.toString() : folder.getName(); }
		
		public Label getChild(String name) {
			for (Label child : children)
				if (child.getName().equals(name))
					return child;
			return null;
		}
		public String getPath() {
			if (parent == null) return "";
			return parent.getPath()+'/'+getName();
		}
		public Label getLabelFromPath(String path) {
			int i = path.indexOf('/');
			String childName = i > 0 ? path.substring(0,i) : path;
			Label child = getChild(childName);
			if (i < 0) return child;
			return child.getLabelFromPath(path.substring(i+1));
		}
		
		public Label newLabel(String name) {
			try {
				IFolder f = folder.getFolder(new Path(name));
				f.create(true, true, null);
				Label child = new Label(this, f);
				children.add(child);
				labelAdded.fire(child);
				return child;
			} catch (CoreException e) {
				ErrorDlg.exception(Local.Create_label.toString(), Local.Unable_to_create_the_new_label.toString(), EclipsePlugin.ID, e);
				return null;
			}
		}
		
		public void move(Label newParent) throws CoreException {
			DataLabels.this.move(this, newParent);
		}
		
		public void rename(String newName) throws CoreException {
			IPath path = parent.folder.getFolder(new Path(newName)).getFullPath();
			folder.move(path, true, null);
			folder = parent.folder.getFolder(new Path(newName));
			labelRenamed.fire(this);
		}
		
		public void remove() throws CoreException {
			folder.delete(true, null);
			parent.children.remove(this);
		}
		
		public Set<Data> getData() {
			if (labeled == null) load();
			Set<Data> result = new HashSet<Data>(labeled);
			for (Label child : children)
				result.addAll(child.getData());
			return result;
		}
		
		public boolean hasData(Data data) {
			if (labeled == null) load();
			if (labeled.contains(data)) return true;
			for (Label child : children)
				if (child.hasData(data))
					return true;
			return false;
		}
		public boolean hasDataItself(Data data) {
			if (labeled == null) load();
			return labeled.contains(data);
		}
		
		public void addData(Data data) {
			if (labeled == null) load();
			if (labeled.add(data)) {
				save();
				labelAssigned.fire(new Pair<Label,Data>(this, data));
			}
		}
		public void removeData(Data data) {
			if (labeled == null) load();
			if (labeled.remove(data)) {
				save();
				labelUnassigned.fire(new Pair<Label,Data>(this, data));
			}
		}
		
		private void load() {
			labeled = new HashSet<Data>();
			IFile file = folder.getFile(new Path("labeled"));
			if (file.exists())
				try { labeled.addAll(read(file.getContents())); }
				catch (Throwable t) {
					if (Log.error(this))
						Log.error(this, "Unable to read labels content from folder " + folder.toString(), t);
				}
		}
		private List<Data> read(InputStream stream) throws IOException {
			List<Data> result = new LinkedList<Data>();
			ObjectInputStream str = new ObjectInputStream(stream);
			do {
				try { 
					long id = str.readLong();
					Data data = DataOrganizer.database().get(id);
					if (data == null) {
						if (Log.warning(this))
							Log.warning(this, "Label " + folder.toString() + " is linked to data id " + id + " but this data doesn't exist!");
					} else
						result.add(data);
				} catch (EOFException e) {
					break;
				}
			} while (true);
			str.close();
			stream.close();
			return result;
		}
		private void save() {
			IFile file = folder.getFile(new Path("labeled"));
			try {
				InputStream source = write();
				if (!file.exists())
					file.create(source, true, null);
				else
					file.setContents(source, true, false, null);
			} catch (IOException e) {
				if (Log.error(this))
					Log.error(this, "Unable to save label file in folder " + folder.toString(), e);
			} catch (CoreException e) {
				if (Log.error(this))
					Log.error(this, "Unable to save label file in folder " + folder.toString(), e);
			}
		}
		private InputStream write() throws IOException {
			MyByteArrayOutputStream buffer = new MyByteArrayOutputStream(labeled.size() * (Long.SIZE/8));
			ObjectOutputStream out = new ObjectOutputStream(buffer);
			for (Data data : labeled)
				out.writeLong(data.getID());
			out.flush();
			out.close();
			byte[] buf = buffer.getBuffer();
			int size = buffer.getBufferSize();
			return new ByteArrayInputStream(buf, 0, size);
		}
	}
	
	public List<Data> getNotLabeledData() {
		List<Data> result = new LinkedList<Data>();
		Set<Data> labeled = root.getData();
		for (Data data : DataOrganizer.database().getAllData())
			if (!labeled.contains(data))
				result.add(data);
		return result;
	}
	
	public List<Label> getLabels(Data data) {
		List<Label> result = new LinkedList<Label>();
		getLabels(root, data, result);
		return result;
	}
	private void getLabels(Label l, Data d, List<Label> r) {
		if (l.hasDataItself(d))
			r.add(l);
		for (Label child : l.children)
			getLabels(child, d, r);
	}
	
	public void move(Label label, Label target) throws CoreException {
		IFolder newFolder = target.folder.getFolder(new Path(label.getName()));
		label.folder.move(newFolder.getFullPath(), true, null);
		label.folder = target.folder.getFolder(new Path(label.getName()));
		label.getParent().children.remove(label);
		target.children.add(label);
		label.parent = target;
	}

	public static String validateName(String name) {
		if (name.indexOf('/') >= 0) return Local.A_label_name_cannot_contain_a_slash +".";
		if (name.indexOf('¤') >= 0) return Local.A_label_name_cannot_contain_the_character__ + ".";
		return null;
	}
	public Label getLabelFromPath(String path) {
		if (path.length() == 0 || path.charAt(0) != '/') return null;
		return root.getLabelFromPath(path.substring(1));
	}
}
