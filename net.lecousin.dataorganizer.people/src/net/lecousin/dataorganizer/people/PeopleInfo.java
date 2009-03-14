package net.lecousin.dataorganizer.people;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.lecousin.dataorganizer.core.database.info.Info;
import net.lecousin.framework.Pair;
import net.lecousin.framework.xml.XmlWriter;

import org.w3c.dom.Element;

public class PeopleInfo extends Info {

	public PeopleInfo(PeopleDataType data, String source, String name) {
		super(data, source, name);
	}

	public PeopleInfo(PeopleDataType data, Element elt, Loader loader) {
		super(data, elt, loader);
		birthDay = loader.getBirthDay(elt);
		birthPlace = loader.getBirthPlace(elt);
		activities = loader.getActivities(elt);
		photos = loader.getPhotos(elt);
	}

	@Override
	protected void saveInfo(XmlWriter xml) {
		xml.addAttribute("birthDay", birthDay);
		if (birthPlace != null)
			xml.addAttribute("birthPlace", birthPlace);
		for (String s : activities.keySet()) {
			Pair<List<String>,List<List<DataLink>>> activity = activities.get(s);
			xml.openTag("activity").addAttribute("name", s);
			for (String text : activity.getValue1())
				xml.openTag("text").addText(text).closeTag();
			for (List<DataLink> links : activity.getValue2()) {
				xml.openTag("links");
				for (DataLink link : links) {
					xml.openTag("link");
					link.save(xml);
					xml.closeTag();
				}
				xml.closeTag();
			}
			xml.closeTag();
		}
		for (String s : photos.keySet())
			xml.openTag("photo").addAttribute("source", s).addAttribute("local", photos.get(s)).closeTag();
	}

	private long birthDay = 0;
	private String birthPlace = null;
	/** photos <SourceURL,File_relative_to_data.getFolder()> */
	// TODO liste de photos par source!!!
	private Map<String,String> photos = new HashMap<String,String>();
	/**
	 * Activity: Map(Name, Pair( FreeTexts, Links ))
	 */
	private Map<String,Pair<List<String>,List<List<DataLink>>>> activities = new HashMap<String,Pair<List<String>,List<List<DataLink>>>>();
	
	public static abstract class Activity {
		Map<String,List<String>> details = new HashMap<String,List<String>>();
	}
	public static class LinkedActivity {
		public List<DataLink> links = new LinkedList<DataLink>();
	}
	
	public long getBirthDay() { return birthDay; }
	public void setBirthDay(long date) {
		if (birthDay == date || date == 0) return;
		birthDay = date;
		signalModification();
	}
	
	public String getBirthPlace() { return birthPlace; }
	public void setBirthPlace(String place) {
		if (place == null || place.length() == 0) return;
		if (place.equals(birthPlace)) return;
		birthPlace = place;
		signalModification();
	}
	
	public Collection<String> getPhotosPaths() { return photos.values(); }
	public boolean hasPhotoURL(String url) { return photos.containsKey(url); }
	public void addPhoto(String url, String localPath) {
		photos.put(url, localPath);
		((PeopleDataType)getDataContent()).signalNewPhoto();
		signalModification();
	}
	
	public Map<String,Pair<List<String>,List<List<DataLink>>>> getActivities() { return activities; }
	public void addActivity(String activity, DataLink infoLink) {
		Pair<List<String>,List<List<DataLink>>> p = activities.get(activity);
		if (p == null) {
			p = new Pair<List<String>,List<List<DataLink>>>(new LinkedList<String>(), new LinkedList<List<DataLink>>());
			activities.put(activity, p);
		}
		for (List<DataLink> list : p.getValue2()) {
			// look for the same
			boolean found = false;
			for (DataLink link : list) {
				if (link.merge(infoLink)) {
					found = true;
					break;
				}
			}
			if (found) return;
		}
		List<DataLink> list = new LinkedList<DataLink>();
		list.add(infoLink);
		p.getValue2().add(list);
	}
	public void addActivity(String activity, String freeText) {
		Pair<List<String>,List<List<DataLink>>> p = activities.get(activity);
		if (p == null) {
			p = new Pair<List<String>,List<List<DataLink>>>(new LinkedList<String>(), new LinkedList<List<DataLink>>());
			activities.put(activity, p);
		}
		freeText = freeText.trim();
		for (String text : p.getValue1())
			if (text.equalsIgnoreCase(freeText))
				return;
		p.getValue1().add(freeText);
	}
	
	@Override
	public Map<String, Map<String, Pair<String, Integer>>> getReviews(String type) {
		return null;
	}
	@Override
	public Set<String> getReviewsTypes() { return null; }
}
