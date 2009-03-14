package net.lecousin.dataorganizer.core.database.info;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.content.DataContentType;
import net.lecousin.dataorganizer.core.database.version.ContentTypeLoader;
import net.lecousin.framework.Pair;
import net.lecousin.framework.event.Event;
import net.lecousin.framework.xml.XmlWriter;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Element;

public abstract class Info {

	public Info(DataContentType data, String source, String name) {
		this.data = data;
		if (source != null && name != null)
			this.names.put(source, name);
		signalModification();
	}
	public Info(DataContentType data, Element elt, ContentTypeLoader loader) {
		this.data = data;
		if (elt == null) return;
		names = loader.getInfoNames(elt);
		ids = loader.getInfoIDs(elt);
	}
	public final void save(XmlWriter xml) {
		saveInfo(xml);
		for (String s : names.keySet())
			xml.openTag("name").addAttribute("source", s).addAttribute("name", names.get(s)).closeTag();
		for (String s : ids.keySet())
			xml.openTag("id").addAttribute("source", s).addAttribute("id", ids.get(s)).closeTag();
	}
	protected abstract void saveInfo(XmlWriter xml);
	
	private DataContentType data;
	/** names <Source,Name> */
	private Map<String,String> names = new HashMap<String,String>();
	/** source IDs <Source,ID> */
	private Map<String,String> ids = new HashMap<String,String>();
	
	private Event<Info> modified = new Event<Info>();
	
	public Event<Info> modified() { return modified; }
	
	public DataContentType getDataContent() { return data; }
	public Data getData() { return data.getData(); }
	
	public Set<String> getSources() { return ids.keySet(); }
	public String getSourceID(String source) { return ids.get(source); }
	
	public Set<String> getNamesSources() { return names.keySet(); }
	public String getName(String source) { return names.get(source); }
	
	public void setID(String source, String id) {
		String old = ids.get(source);
		if (old == null || !old.equals(id)) {
			ids.put(source, id);
			signalModification();
		}
	}
	public void setName(String source, String name) {
		String old = names.get(source);
		if (old == null || !old.equals(name)) {
			names.put(source, name);
			signalModification();
		}
	}
	
	public abstract Set<String> getReviewsTypes();
	/** reviews <Source,<Author,<Critik,Note/20>>> */
	public abstract Map<String,Map<String,Pair<String,Integer>>> getReviews(String type);
	
	protected void signalModification() {
		data.signalModification();
		modified.fire(this);
	}
	
	public IFolder getFolder() throws CoreException { return data.getFolder(); }
	
	public static class DataLink {
		public DataLink(String contentTypeID, String source, String id, String name) {
			this.contentTypeID = contentTypeID;
			this.source = source;
			this.id = id;
			this.name = name.trim();
		}
		public DataLink(Element elt) {
			this.contentTypeID = elt.getAttribute("content_type_id");
			this.source = elt.getAttribute("source");
			this.id = elt.getAttribute("id");
			this.name = elt.getAttribute("name");
		}
		public String contentTypeID;
		public String source;
		public String id;
		public String name;
		
		public boolean merge(DataLink link) {
			if (!link.contentTypeID.equals(contentTypeID)) return false;
			if (!link.source.equals(source)) return false;
			if (!link.id.equals(id)) return false;
			if (link.name.length() > 0 && name.length() == 0)
				name = link.name;
			return true;
		}
		
		public void save(XmlWriter xml) {
			xml.addAttribute("content_type_id", contentTypeID).addAttribute("source", source).addAttribute("id", id).addAttribute("name", name);
		}
	}
}
