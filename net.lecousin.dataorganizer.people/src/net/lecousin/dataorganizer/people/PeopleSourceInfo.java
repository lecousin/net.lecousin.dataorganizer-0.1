package net.lecousin.dataorganizer.people;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.lecousin.dataorganizer.core.database.info.SourceInfo;
import net.lecousin.dataorganizer.core.database.info.Info.DataLink;
import net.lecousin.framework.Pair;
import net.lecousin.framework.collections.SelfMap;
import net.lecousin.framework.xml.XmlWriter;

import org.w3c.dom.Element;

public class PeopleSourceInfo extends SourceInfo {

	public PeopleSourceInfo(PeopleInfo parent) {
		super(parent);
	}

	public PeopleSourceInfo(PeopleInfo parent, Element elt, Loader loader) {
		super(parent);
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
		if (getParent() != null)
			((PeopleDataType)getParent().getDataContent()).signalNewPhoto();
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
			for (DataLink link : list) {
				if (link.isSame(infoLink)) {
					if (link.merge(infoLink))
						signalModification();
					return;
				}
			}
		}
		List<DataLink> list = new LinkedList<DataLink>();
		list.add(infoLink);
		p.getValue2().add(list);
		signalModification();
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
		signalModification();
	}
	
	@Override
	public SelfMap<String, Review> getReviews(String type) {
		return null;
	}
	
	@Override
	public void merge(SourceInfo info) {
		PeopleSourceInfo i = (PeopleSourceInfo)info;
		if (birthDay <= 0) setBirthDay(i.getBirthDay());
		if (birthPlace == null || birthPlace.length() == 0) setBirthPlace(i.getBirthPlace());
		for (String url : i.photos.keySet()) {
			if (photos.containsKey(url)) continue;
			addPhoto(url, i.photos.get(url));
		}
		boolean changed = false;
		for (String activity : i.activities.keySet()) {
			Pair<List<String>,List<List<DataLink>>> oldA = activities.get(activity);
			if (oldA == null) {
				activities.put(activity, i.activities.get(activity));
				changed = true;
				continue;
			}
			Pair<List<String>,List<List<DataLink>>> newA = i.activities.get(activity);
			for (String s : newA.getValue1())
				if (!oldA.getValue1().contains(s)) {
					oldA.getValue1().add(s);
					changed = true;
				}
			List<List<DataLink>> toAdd = new LinkedList<List<DataLink>>();
			for (List<DataLink> newList : newA.getValue2()) {
				boolean found = false;
				for (List<DataLink> oldList : oldA.getValue2()) {
					if (DataLink.isMergeable(oldList, newList)) {
						changed |= DataLink.merge(oldList, newList);
						found = true;
					}
				}
				if (!found)
					toAdd.add(newList);
			}
			if (!toAdd.isEmpty()) {
				newA.getValue2().addAll(toAdd);
				changed = true;
			}
		}
		if (changed) signalModification();
	}
}
