package net.lecousin.dataorganizer.core.database;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import net.lecousin.dataorganizer.core.AutoSaver;
import net.lecousin.dataorganizer.core.database.content.ContentType;
import net.lecousin.dataorganizer.core.database.content.DataContentType;
import net.lecousin.dataorganizer.core.database.source.DataSource;
import net.lecousin.dataorganizer.core.database.version.DBLoader;
import net.lecousin.dataorganizer.core.database.version.VersionLoader;
import net.lecousin.framework.application.Application;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.memory.AutoFreeMemoryGroup.AutoFreeGroup;
import net.lecousin.framework.memory.AutoFreeMemoryGroup.AutoFreeObject;
import net.lecousin.framework.memory.AutoFreeMemoryGroup.Loader;
import net.lecousin.framework.xml.XmlUtil;
import net.lecousin.framework.xml.XmlWriter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class RealData extends Data {
	
	private static AutoFreeGroup<DataContentType> autoGroup = null;
	private static final int MAX_CONTENT_IN_MEMORY = 1000;
	
	RealData(RealDataBase db, long id, VersionLoader loader) throws SAXException, ParserConfigurationException, IOException, CoreException {
		super(db, id);
		Element root = XmlUtil.loadFile(db.getFile(id).getContents());
		DBLoader l = loader.getDB();
		name = l.getName(root);
		sources = l.getSources(root);
		views = l.getViews(root);
		rate = l.getRate(root);
		comment = l.getComment(root);
		dateAdded = l.getDateAdded(root);
		contentType = l.getContentType(root);
		Loader<DataContentType> contentLoader = new Loader<DataContentType>() {
			public DataContentType load() {
				DataContentType content = AutoSaver.getContent(RealData.this);
				if (content != null) return content;
				try { return contentType.loadContent(RealData.this, XmlUtil.loadFile(RealData.this.db.getFolder(RealData.this.id).getFile("content.xml").getContents())); }
				catch (Throwable t) {
					if (Log.error(this))
						Log.error(this, "Unable to load data content", t);
					return null;
				}
			}
		};
		if (autoGroup == null)
			autoGroup = Application.getAutoFreeMemoryGroup().createGroup("RealData.content", MAX_CONTENT_IN_MEMORY);
		if (loader.isPreviousVersion(contentType.getID())) {
			DataContentType c = l.getContent(this, root, loader.get(contentType.getID()));
			c.save();
			content = autoGroup.create(c, contentLoader); 
		} else
			content = autoGroup.create(null, contentLoader); 
	}
	RealData(RealDataBase db, long id, String name, ContentType content, List<DataSource> sources) throws CoreException {
		super(db, id, name, content, sources);
		save();
	}
	@Override
	protected void storeContent(DataContentType c) {
		if (this.content != null) {
			AutoSaver.replaced(this.content.get());
			this.content.free();
		}
		if (autoGroup == null)
			autoGroup = Application.getAutoFreeMemoryGroup().createGroup("RealData.content", MAX_CONTENT_IN_MEMORY);
		this.content = autoGroup.create(c, new Loader<DataContentType>() {
			public DataContentType load() {
				DataContentType content = AutoSaver.getContent(RealData.this);
				if (content != null) return content;
				try { return contentType.loadContent(RealData.this, XmlUtil.loadFile(RealData.this.db.getFolder(RealData.this.id).getFile("content.xml").getContents())); }
				catch (Throwable t) {
					if (Log.error(this))
						Log.error(this, "Unable to load data content", t);
					return null;
				}
			}
		});
		c.save();
	}
	
	private AutoFreeObject<DataContentType> content;
	public DataContentType getContent() { return content.get(); }
	private boolean removed = false;
	
	public void signalModification() {
		if (removed) throw new IllegalStateException("Data " + name + " has been removed.");
		AutoSaver.modified(this);
		super.signalModification();
	}
	@Override
	public void signalContentModification(DataContentType content) {
		AutoSaver.modified(content);
		super.signalContentModification(content);
	}
	
	public void save() {
		if (removed) throw new IllegalStateException("Data " + name + " has been removed.");
		XmlWriter xml = new XmlWriter();
		xml.openTag("data");
		if (name == null) name = "";
		xml.addAttribute("name", name);
		xml.addAttribute("content_type", contentType.getID());
		xml.addAttribute("dateAdded", dateAdded);
		if (rate >= 0)
			xml.addAttribute("rate", Byte.toString(rate));
		for (DataSource source : sources) {
			xml.openTag("source");
			if (source != null)
				source.save(xml);
			xml.closeTag();
		}
		for (Long view : views)
			xml.openTag("view").addAttribute("date", Long.toString(view)).closeTag();
		if (comment != null)
			xml.openTag("comment").addText(comment).closeTag();
		xml.closeTag();
		ByteArrayInputStream stream = new ByteArrayInputStream(xml.getXML().toString().getBytes());
		try {
			IFile file = db.getFile(id);
			if (!file.exists())
				file.create(stream, true, null);
			else
				file.setContents(stream, true, false, null);
		} catch (CoreException e) {
			if (Log.error(this))
				Log.error(this, "Unable to save data", e);
		}
	}
	
	public void remove() {
		removed = true;
		AutoSaver.removed(this);
		super.remove();
		content.free();
	}

}
