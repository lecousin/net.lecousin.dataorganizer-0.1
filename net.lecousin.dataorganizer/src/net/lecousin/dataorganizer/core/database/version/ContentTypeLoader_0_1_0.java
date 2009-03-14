package net.lecousin.dataorganizer.core.database.version;

import java.util.HashMap;
import java.util.Map;

import net.lecousin.framework.xml.XmlUtil;

import org.w3c.dom.Element;

public abstract class ContentTypeLoader_0_1_0 implements ContentTypeLoader {

	public Element getInfo(Element root) {
		return XmlUtil.get_child_element(root, "info");
	}
	
	public Map<String,String> getInfoNames(Element root) {
		Map<String,String> names = new HashMap<String,String>();
		for (Element e : XmlUtil.get_childs_element(root, "name"))
			names.put(e.getAttribute("source"), e.getAttribute("name"));
		return names;
	}
	public Map<String,String> getInfoIDs(Element root) {
		Map<String,String> ids = new HashMap<String,String>();
		for (Element e : XmlUtil.get_childs_element(root, "id"))
			ids.put(e.getAttribute("source"), e.getAttribute("id"));
		return ids;
	}
	
}
