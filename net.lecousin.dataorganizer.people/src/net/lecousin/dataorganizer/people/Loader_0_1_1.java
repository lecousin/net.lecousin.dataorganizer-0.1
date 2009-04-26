package net.lecousin.dataorganizer.people;

import net.lecousin.dataorganizer.core.database.info.Info.DataLink;
import net.lecousin.dataorganizer.people.PeopleSourceInfo.Activity;
import net.lecousin.framework.collections.SelfMap;
import net.lecousin.framework.collections.SelfMapLinkedList;
import net.lecousin.framework.version.Version;
import net.lecousin.framework.xml.XmlUtil;

import org.w3c.dom.Element;

public class Loader_0_1_1 extends Loader_0_1_0 {

	private static final Version version = new Version(0,1,1);
	public Version getVersion() { return version; }

	public SelfMap<String,Activity> getActivities(Element root) {
		SelfMap<String,Activity> activities = new SelfMapLinkedList<String,Activity>(3);
		for (Element e : XmlUtil.get_childs_element(root, "activity")) {
			Activity a = new Activity();
			a.name = e.getAttribute("name");
			for (Element eText : XmlUtil.get_childs_element(e, "text"))
				a.freeTexts.add(XmlUtil.get_inner_text(eText));
			for (Element eLink : XmlUtil.get_childs_element(e, "link"))
				a.links.add(new DataLink(eLink));
			activities.add(a);
		}
		return activities;
	}
	
}
