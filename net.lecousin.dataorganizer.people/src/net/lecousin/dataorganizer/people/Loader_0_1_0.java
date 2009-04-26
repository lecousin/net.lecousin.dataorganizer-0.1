package net.lecousin.dataorganizer.people;

import java.util.HashMap;
import java.util.Map;

import net.lecousin.dataorganizer.core.database.info.SourceInfo;
import net.lecousin.dataorganizer.core.database.info.Info.DataLink;
import net.lecousin.dataorganizer.core.database.info.SourceInfo.Review;
import net.lecousin.dataorganizer.core.database.version.ContentTypeLoader_0_1_0;
import net.lecousin.dataorganizer.people.PeopleSourceInfo.Activity;
import net.lecousin.framework.collections.SelfMap;
import net.lecousin.framework.collections.SelfMapLinkedList;
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
	
	public String getDescription(Element root) {
		Element e = XmlUtil.get_child_element(root, "description");
		if (e == null) return null;
		return XmlUtil.get_inner_text(e);
	}
	
	public SelfMap<String,Activity> getActivities(Element root) {
		SelfMap<String,Activity> activities = new SelfMapLinkedList<String,Activity>(3);
		for (Element e : XmlUtil.get_childs_element(root, "activity")) {
			Activity a = new Activity();
			a.name = e.getAttribute("name");
			for (Element eText : XmlUtil.get_childs_element(e, "text"))
				a.freeTexts.add(XmlUtil.get_inner_text(eText));
			for (Element eLinks : XmlUtil.get_childs_element(e, "links")) {
				Element eLink = XmlUtil.get_child_element(eLinks, "link");
				a.links.add(new DataLink(eLink));
			}
			activities.add(a);
		}
		return activities;
	}
	
	public Map<String,String> getPhotos(Element root) {
		Map<String,String> photos = new HashMap<String,String>();
		for (Element e : XmlUtil.get_childs_element(root, "photo"))
			photos.put(e.getAttribute("source"), e.getAttribute("local"));
		return photos;
	}

	public SelfMap<String,Review> getPublicReviews(SourceInfo source, Element root) {
		return loadReviews(source, "publicReview", root);
	}
}
