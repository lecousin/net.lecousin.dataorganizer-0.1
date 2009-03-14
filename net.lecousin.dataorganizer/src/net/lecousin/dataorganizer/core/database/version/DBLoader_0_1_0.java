package net.lecousin.dataorganizer.core.database.version;

import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.content.ContentType;
import net.lecousin.dataorganizer.core.database.content.DataContentType;
import net.lecousin.dataorganizer.core.database.source.DataSource;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.version.Version;
import net.lecousin.framework.xml.XmlUtil;

import org.w3c.dom.Element;

public class DBLoader_0_1_0 implements DBLoader {

	private static final Version version = new Version(0,1,0);
	public Version getVersion() { return version; }

	public String getName(Element root) {
		return root.getAttribute("name");
	}
	
	public List<DataSource> getSources(Element root) {
		List<DataSource> sources = new LinkedList<DataSource>();
		for (Element e : XmlUtil.get_childs_element(root, "source"))
			sources.add(DataSource.load(e));
		return sources;
	}
	
	public List<Long> getViews(Element root) {
		List<Long> views = new LinkedList<Long>();
		for (Element e : XmlUtil.get_childs_element(root, "view"))
			views.add(Long.parseLong(e.getAttribute("date")));
		return views;
	}
	
	public byte getRate(Element root) {
		if (root.hasAttribute("rate"))
			return Byte.parseByte(root.getAttribute("rate"));
		return -1;
	}
	
	public String getComment(Element root) {
		Element e = XmlUtil.get_child_element(root, "comment");
		if (e != null)
			return XmlUtil.get_inner_text(e);
		return null;
	}
	
	public long getDateAdded(Element root) {
		if (root.hasAttribute("dateAdded"))
			return Long.parseLong(root.getAttribute("dateAdded"));
		return System.currentTimeMillis();
	}
	
	public ContentType getContentType(Element root) {
		return ContentType.getContentType(root.getAttribute("content_type"));
	}
	
	public DataContentType getContent(Data data, Element root, ContentTypeLoader loader) {
		try { return data.getContentType().loadContent(data, XmlUtil.loadFile(data.getFolder().getFile("content.xml").getContents()), loader); }
		catch (Throwable t) {
			if (Log.error(this))
				Log.error(this, "Unable to load data content", t);
			return null;
		}
	}
}
