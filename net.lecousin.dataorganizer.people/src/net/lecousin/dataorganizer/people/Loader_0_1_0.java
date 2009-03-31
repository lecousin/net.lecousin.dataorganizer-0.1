package net.lecousin.dataorganizer.people;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.lecousin.dataorganizer.core.database.info.Info.DataLink;
import net.lecousin.dataorganizer.core.database.version.ContentTypeLoader_0_1_0;
import net.lecousin.framework.Pair;
import net.lecousin.framework.version.Version;
import net.lecousin.framework.xml.XmlUtil;

import org.w3c.dom.Element;

public class Loader_0_1_0 extends ContentTypeLoader_0_1_0 implements Loader {

	private static final Version version = new Version(0,1,0);
	public Version getVersion() { return version; }

	public long getBirthDay(Element root) {
		return Long.parseLong(root.getAttribute("birthDay"));
	}
	
	public String getBirthPlace(Element root) {
		if (root.hasAttribute("birthPlace"))
			return root.getAttribute("birthPlace");
		return null;
	}
	
	public Map<String,Pair<List<String>,List<List<DataLink>>>> getActivities(Element root) {
		Map<String,Pair<List<String>,List<List<DataLink>>>> activities = new HashMap<String,Pair<List<String>,List<List<DataLink>>>>();
		for (Element e : XmlUtil.get_childs_element(root, "activity")) {
			String name = e.getAttribute("name");
			List<String> texts = new LinkedList<String>();
			for (Element eText : XmlUtil.get_childs_element(e, "text"))
				texts.add(XmlUtil.get_inner_text(eText));
			List<List<DataLink>> links = new LinkedList<List<DataLink>>();
			for (Element eLinks : XmlUtil.get_childs_element(e, "links")) {
				List<DataLink> list = new LinkedList<DataLink>();
				for (Element eLink : XmlUtil.get_childs_element(eLinks, "link"))
					list.add(new DataLink(eLink));
				links.add(list);
			}
			Pair<List<String>,List<List<DataLink>>> p = new Pair<List<String>,List<List<DataLink>>>(texts, links);
			activities.put(name, p);
		}
		return activities;
	}
	
	public Map<String,String> getPhotos(Element root) {
		Map<String,String> photos = new HashMap<String,String>();
		for (Element e : XmlUtil.get_childs_element(root, "photo"))
			photos.put(e.getAttribute("source"), e.getAttribute("local"));
		return photos;
	}

}
