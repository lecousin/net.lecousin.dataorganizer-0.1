package net.lecousin.dataorganizer.core.database.info;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.content.DataContentType;
import net.lecousin.dataorganizer.core.database.version.ContentTypeLoader;
import net.lecousin.framework.Triple;
import net.lecousin.framework.event.Event;
import net.lecousin.framework.xml.XmlUtil;
import net.lecousin.framework.xml.XmlWriter;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Element;

public abstract class Info {

	public Info(DataContentType data) {
		this.data = data;
		signalModification();
	}
	public Info(DataContentType data, Element elt, ContentTypeLoader loader) {
		this.data = data;
		if (elt == null) return;
		for (Element e : XmlUtil.get_childs_element(elt, "source")) {
			String source = e.getAttribute("source");
			String id = e.getAttribute("id");
			String name = e.getAttribute("name");
			Element infoNode = XmlUtil.get_child_element(e, "info");
			SourceInfo info = null;
			if (infoNode != null)
				info = createSourceInfo(this, infoNode, loader);
			sources.put(source, new Triple<String,String,SourceInfo>(id, name, info));
		}
	}
	public final void save(XmlWriter xml) {
		saveInfo(xml);
		for (String s : sources.keySet()) {
			Triple<String,String,SourceInfo> t = sources.get(s);
			xml.openTag("source").addAttribute("source", s).addAttribute("id", t.getValue1()).addAttribute("name", t.getValue2());
			if (t.getValue3() != null) {
				xml.openTag("info");
				t.getValue3().save(xml);
				xml.closeTag();
			}
			xml.closeTag();
		}
	}
	protected abstract void saveInfo(XmlWriter xml);
	
	private DataContentType data;
	/** sources <Source,<ID,Name,SourceInfo>> */
	private Map<String,Triple<String,String,SourceInfo>> sources = new HashMap<String,Triple<String,String,SourceInfo>>();
	
	private Event<Info> modified = new Event<Info>();
	
	public Event<Info> modified() { return modified; }
	
	public DataContentType getDataContent() { return data; }
	public Data getData() { return data.getData(); }
	
	public Set<String> getSources() { return sources.keySet(); }
	public String getSourceID(String source) { Triple<String,String,SourceInfo> t = sources.get(source); if (t == null) return null; return t.getValue1(); }
	public String getSourceName(String source) { Triple<String,String,SourceInfo> t = sources.get(source); if (t == null) return null; return t.getValue2(); }
	public SourceInfo getSourceInfo(String source) { Triple<String,String,SourceInfo> t = sources.get(source); if (t == null) return null; return t.getValue3(); }
	
	public SourceInfo getMergedInfo(Iterable<String> sources) {
		SourceInfo result = createSourceInfo(null);
		for (String source : sources) {
			Triple<String,String,SourceInfo> t = this.sources.get(source);
			if (t == null || t.getValue3() == null) continue;
			result.merge(t.getValue3());
		}
		return result;
	}
	
	public SourceInfo setSource(String source, String id, String name) {
		Triple<String,String,SourceInfo> t = sources.get(source);
		SourceInfo info;
		if (t == null) {
			info = createSourceInfo(this);
			t = new Triple<String,String,SourceInfo>(id, name, info);
			sources.put(source, t);
			signalModification();
		} else {
			boolean changed = false;
			if (!t.getValue1().equals(id)) { t.setValue1(id); changed = true; }
			if (name != null && !name.equals(t.getValue2())) { t.setValue2(name); changed = true; }
			info = t.getValue3();
			if (info == null) {
				info = createSourceInfo(this);
				t.setValue3(info);
				changed = true;
			}
			if (changed) signalModification();
		}
		return info;
	}
	
	protected abstract SourceInfo createSourceInfo(Info parent);
	protected abstract SourceInfo createSourceInfo(Info parent, Element elt, ContentTypeLoader loader);
	
	public abstract Set<String> getReviewsTypes();
	
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
		
		/** return true if the merge made a change */
		public boolean merge(DataLink link) {
			if (link.source == null || link.id == null) return false;
			if (!link.contentTypeID.equals(contentTypeID)) return false;
			if (!link.source.equals(source)) return false;
			if (!link.id.equals(id)) return false;
			if (link.name != null && link.name.length() > 0 && name.length() == 0) {
				name = link.name;
				return true;
			}
			return false;
		}
		
		public boolean isSame(DataLink link) {
			if (!link.contentTypeID.equals(contentTypeID)) return false;
			if (link.source == null || link.id == null) return false;
			if (source == null || id == null) return false;
			if (!link.source.equals(source)) return false;
			if (!link.id.equals(id)) return false;
			return true;
		}
		
		public void save(XmlWriter xml) {
			xml.addAttribute("content_type_id", contentTypeID).addAttribute("source", source).addAttribute("id", id).addAttribute("name", name);
		}
		
		/** return true if at least one link is common (in the isSame method sense) */
		public static boolean isMergeable(List<DataLink> list1, List<DataLink> list2) {
			for (DataLink l1 : list1)
				for (DataLink l2 : list2)
					if (l1.isSame(l2)) return true;
			return false;
		}
		/** merge the 2 lists into list1, and return true if a changed has been made */
		public static boolean merge(List<DataLink> list1, List<DataLink> list2) {
			boolean changed = false;
			for (DataLink newLink : list2) {
				boolean found = false;
				for (DataLink oldLink : list1) {
					if (oldLink.isSame(newLink)) {
						changed |= oldLink.merge(newLink);
						found = true;
						break;
					}
				}
				if (!found) {
					list1.add(newLink);
					changed = true;
				}
			}
			return changed;
		}
	}
}
