package net.lecousin.dataorganizer.core.database;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.lecousin.dataorganizer.core.database.content.ContentType;
import net.lecousin.dataorganizer.core.database.content.DataContentType;
import net.lecousin.dataorganizer.core.database.info.InfoRetriever;
import net.lecousin.dataorganizer.core.database.source.DataSource;
import net.lecousin.dataorganizer.core.database.source.LocalFileDataSource;
import net.lecousin.dataorganizer.ui.dialog.EditDataInfosDlg;
import net.lecousin.framework.Pair;
import net.lecousin.framework.collections.SelfMap;
import net.lecousin.framework.event.Event;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public abstract class Data implements SelfMap.Entry<Long> {

	protected Data(DataBase db, long id, String name, ContentType content, List<DataSource> sources) throws CoreException {
		this.db = db;
		this.id = id;
		this.name = name;
		this.sources = sources;
		this.contentType = content;
		storeContent(content.create(this));
	}
	protected Data(DataBase db, long id) {
		this.db = db;
		this.id = id;
	}
	protected abstract void storeContent(DataContentType content);
	
	protected DataBase db;
	long id;
	protected String name;
	protected List<DataSource> sources;
	protected ContentType contentType;
	protected List<Long> views = new LinkedList<Long>();
	protected byte rate = -1;
	protected String comment = null;
	protected long dateAdded = System.currentTimeMillis();
	
	public DataBase getDataBase() { return db; }
	public List<Long> getViews() { return new ArrayList<Long>(views); }
	public byte getRate() { return rate; }
	public String getComment() { return comment; }
	public long getDateAdded() { return dateAdded; }
	
	private Event<Data> modified = new Event<Data>();
	private Event<DataContentType> contentModified = new Event<DataContentType>();
	
	public Long getHashObject() { return id; }
	public long getID() { return id; }

	public String getName() { return name; }
	/** may contain <code>null</code> elements */
	public List<DataSource> getSources() { return sources; }
	public abstract DataContentType getContent();
	public ContentType getContentType() { return contentType; }
	
	public void moveDataSource(int srcIndex, int dstIndex) {
		if (srcIndex < 0) return;
		if (srcIndex >= sources.size()) return;
		DataSource source = sources.remove(srcIndex);
		sources.add(dstIndex, source);
		signalModification();
	}
	
	public Event<Data> modified() { return modified; }
	public Event<DataContentType> contentModified() { return contentModified; }
	
	public IFolder getFolder() throws CoreException { return db.getFolder(id); }
	
	void setContent(DataContentType content) {
		storeContent(content);
	}
	
	public void setRate(byte rate) {
		if (this.rate == rate) return;
		this.rate = rate;
		signalModification();
	}
	public void setComment(String text) {
		if (text.length() == 0) {
			if (comment == null) return;
			comment = null;
		} else {
			if (text.equals(comment)) return;
			comment = text;
		}
		signalModification();
	}

	public long opened() {
		long time = System.currentTimeMillis();
		db.signalDataOpened(this, time);
		return time;
	}
	
	boolean openedDialog(List<Long> dates) {
		EditDataInfosDlg dlg = new EditDataInfosDlg(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), this, dates);
		if (dlg.open()) {
			views.addAll(dlg.getSelectedDates());
			rate = dlg.getRate();
			comment = dlg.getComment();
			signalModification();
			return true;
		} else 
			return dlg.isIgnoring();
	}
	
	public enum DuplicateAnalysis {
		DIFFERENT,
		EXACTLY_THE_SAME,
		SAME_IN_DIFFERENT_LOCATION,
		SEEMS_THE_SAME_BUT_DIFFERENT_SOURCES
	}
	
	public DuplicateAnalysis checkForDuplicate(Data data) {
		// check if exactly the same or same content
		List<DataSource> listCurrent = new ArrayList<DataSource>(sources);
		List<DataSource> listNew = new ArrayList<DataSource>(data.getSources());
		boolean changeLocation = false;
		for (DataSource n : data.getSources()) {
			if (n == null) { listNew.remove(null); continue; }
			for (DataSource c : sources)
				if (c == null) {
					listCurrent.remove(null);
				} else if (c.isExactlyTheSame(n)) {
					listCurrent.remove(c);
					listNew.remove(n);
					break;
				} else if (c.isSameInDifferentLocation(n)) {
					listCurrent.remove(c);
					listNew.remove(n);
					changeLocation = true;
					break;
				}
		}
		if (listCurrent.isEmpty() && listNew.isEmpty()) 
			return changeLocation ? DuplicateAnalysis.SAME_IN_DIFFERENT_LOCATION : DuplicateAnalysis.EXACTLY_THE_SAME;
		return getContent().checkForDuplicateOnContent(data);
	}
	
	public boolean checkExistsOnLocal() {
		for (DataSource src : sources) {
			if (!(src instanceof LocalFileDataSource)) return true;
			File f = ((LocalFileDataSource)src).getLocalFile();
			if (!f.exists()) return false;
		}
		return true;
	}
	
	public void setName(String name) {
		if (name.equals(this.name)) return;
		this.name = name;
		signalModification();
	}
	public void updateSources(List<DataSource> sources) {
		this.sources = sources;
		signalModification();
	}
	
	public void signalModification() {
		modified.fire(this);
	}
	public void signalContentModification(DataContentType content) {
		contentModified.fire(content);
	}
	
	public void retrieveInfo(Shell shell) {
		InfoRetriever.retrieve(shell, this);
	}
	
	public void remove() {
		db.internal_removeData(this);
	}
	
	public Pair<List<Exception>,List<File>> removeFromFileSystem() {
		List<Exception> errors = new LinkedList<Exception>();
		List<File> files = new LinkedList<File>();
		for (DataSource source : sources)
			try { files.addAll(source.removeFromFileSystem()); }
			catch (Exception e) { errors.add(e); }
		return new Pair<List<Exception>,List<File>>(errors,files);
	}
	public void unlinkSources(Collection<File> files) {
		List<DataSource> toRemove = new LinkedList<DataSource>();
		for (DataSource source : sources) {
			if (source.unlink(files))
				toRemove.add(source);
		}
		sources.removeAll(toRemove);
	}
	public Set<File> getLinkedFiles() {
		Set<File> files = new HashSet<File>();
		for (DataSource source : sources)
			files.addAll(source.getLinkedFiles());
		return files;
	}
	public Set<File> getLinkedFiles(List<File> files) {
		Set<File> links = getLinkedFiles();
		links.retainAll(files);
		return links;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (!(obj instanceof Data)) return false;
		return id == ((Data)obj).id;
	}
	@Override
	public int hashCode() {
		return (int)id;
	}
}
