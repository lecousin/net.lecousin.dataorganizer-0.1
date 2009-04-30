package net.lecousin.dataorganizer.core;

import java.io.File;
import java.io.IOException;

import net.lecousin.framework.application.Application;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.xml.XmlUtil;
import net.lecousin.framework.xml.XmlWriter;

import org.w3c.dom.Element;

public class DataOrganizerConfig {

	public int update_check_frequency = 7;
	public long last_update_check = 0;
	public long last_new_shown = 0;
	public long launchCount = 0;
	
	DataOrganizerConfig() {}
	DataOrganizerConfig(Element elt) {
		Element e = XmlUtil.get_child_element(elt, "update_check");
		if (e != null) {
			if (e.hasAttribute("frequency"))
				update_check_frequency = getInt(e.getAttribute("frequency"), update_check_frequency);
			if (e.hasAttribute("last_time"))
				last_update_check = getLong(e.getAttribute("last_time"), last_update_check);
		}
		e = XmlUtil.get_child_element(elt, "news");
		if (e != null) {
			if (e.hasAttribute("last_shown"))
				last_new_shown = getLong(e.getAttribute("last_shown"), last_new_shown);
		}
		e = XmlUtil.get_child_element(elt, "misc");
		if (e != null) {
			if (e.hasAttribute("launchCount"))
				launchCount = getLong(e.getAttribute("launchCount"), launchCount);
		}
	}
	
	public void save() {
		XmlWriter xml = new XmlWriter();
		xml.openTag("dataorganizer");
		xml.openTag("update_check").addAttribute("frequency", update_check_frequency).addAttribute("last_time", last_update_check).closeTag();
		xml.openTag("news").addAttribute("last_shown", last_new_shown).closeTag();
		xml.openTag("misc").addAttribute("launchCount", launchCount).closeTag();
		xml.closeTag();
		try { xml.writeToFile(new File(Application.deployPath, "DataOrganizer.xml").getAbsolutePath()); }
		catch (IOException e) {
			if (Log.error(this))
				Log.error(this, "Unable to save DataOrganizer configuration file", e);
		}
	}
	
	private static int getInt(String s, int defValue) {
		try { return Integer.parseInt(s); }
		catch (NumberFormatException e) { return defValue; }
	}
	private static long getLong(String s, long defValue) {
		try { return Long.parseLong(s); }
		catch (NumberFormatException e) { return defValue; }
	}
}
