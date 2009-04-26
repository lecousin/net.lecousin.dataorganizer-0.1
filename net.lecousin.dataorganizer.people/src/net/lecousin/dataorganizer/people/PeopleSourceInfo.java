package net.lecousin.dataorganizer.people;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.lecousin.dataorganizer.core.database.info.SourceInfo;
import net.lecousin.dataorganizer.core.database.info.SourceInfoMergeUtil;
import net.lecousin.dataorganizer.core.database.info.Info.DataLink;
import net.lecousin.framework.collections.CollectionUtil;
import net.lecousin.framework.collections.SelfMap;
import net.lecousin.framework.collections.SelfMapLinkedList;
import net.lecousin.framework.xml.XmlWriter;

import org.eclipse.core.resources.IFolder;
import org.w3c.dom.Element;

public class PeopleSourceInfo extends SourceInfo {

	public PeopleSourceInfo(PeopleInfo parent) {
		super(parent);
	}

	public PeopleSourceInfo(PeopleInfo parent, Element elt, Loader loader) {
		super(parent);
		birthDay = loader.getBirthDay(elt);
		birthPlace = loader.getBirthPlace(elt);
		description = loader.getDescription(elt);
		activities = loader.getActivities(elt);
		photos = loader.getPhotos(elt);
		publicReviews = loader.getPublicReviews(this, elt);
	}

	@Override
	protected void saveInfo(XmlWriter xml) {
		xml.addAttribute("birthDay", birthDay);
		if (birthPlace != null)
			xml.addAttribute("birthPlace", birthPlace);
		if (description != null)
			xml.openTag("description").addText(description).closeTag();
		for (Activity activity : activities) {
			xml.openTag("activity").addAttribute("name", activity.name);
			for (String text : activity.freeTexts)
				xml.openTag("text").addText(text).closeTag();
			for (DataLink link : activity.links) {
				xml.openTag("link");
				link.save(xml);
				xml.closeTag();
			}
			xml.closeTag();
		}
		for (String s : photos.keySet())
			xml.openTag("photo").addAttribute("source", s).addAttribute("local", photos.get(s)).closeTag();
		saveCritiks(publicReviews, "publicReview", xml);
	}

	private long birthDay = 0;
	private String birthPlace = null;
	private String description = null;
	/** photos <SourceURL,File_relative_to_data.getFolder()> */
	private Map<String,String> photos = new HashMap<String,String>();
	/** Activities */
	private SelfMap<String,Activity> activities = new SelfMapLinkedList<String,Activity>(3);
	/** public reviews */
	private SelfMap<String,Review> publicReviews = new SelfMapLinkedList<String,Review>(5);
	
	public static class Activity implements SelfMap.Entry<String> {
		String name;
		List<String> freeTexts = new LinkedList<String>();
		List<DataLink> links = new LinkedList<DataLink>();
		public String getName() { return name; }
		public List<String> getFreeTexts() { return new ArrayList<String>(freeTexts); }
		public List<DataLink> getLinks() { return new ArrayList<DataLink>(links); }
		public String getHashObject() { return name; }
	}
	public static class MergedActivity implements SelfMap.Entry<String> {
		public String name;
		public List<String> freeTexts = new LinkedList<String>();
		public List<List<DataLink>> links = new LinkedList<List<DataLink>>();
		public String getHashObject() { return name; }
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
	
	public String getDescription() { return description; }
	public void setDescription(String descr) {
		if (descr == null || descr.length() == 0) return;
		description = descr;
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
	
	public SelfMap<String,Activity> getActivities() { return activities; }
	public void addActivity(String activity, DataLink infoLink) {
		boolean changed = false;
		Activity a = activities.get(activity);
		if (a == null) {
			a = new Activity();
			a.name = activity;
			activities.add(a);
			changed = true;
		}
		// look for the same link
		for (DataLink link : a.links) {
			if (link.isSame(infoLink)) {
				if (link.merge(infoLink) || changed)
					signalModification();
				return;
			}
		}
		a.links.add(infoLink);
		signalModification();
	}
	public void addActivity(String activity, String freeText) {
		Activity a = activities.get(activity);
		if (a == null) {
			a = new Activity();
			a.name = activity;
			activities.add(a);
		}
		// look for the same link
		freeText = freeText.trim();
		for (String text : a.freeTexts)
			if (text.equalsIgnoreCase(freeText))
				return;
		a.freeTexts.add(freeText);
		signalModification();
	}
	
	@Override
	public SelfMap<String, Review> getReviews(String type) {
		if (type.equals(Local.Public.toString())) return publicReviews;
		return null;
	}

	public void setPublicReview(String author, String review, Integer note) {
		setReview(publicReviews, author, review, note);
	}
	
	public static void mergeActivities(SelfMap<String,MergedActivity> currentActivities, SelfMap<String,Activity> newActivities) {
		for (Activity activity : newActivities) {
			MergedActivity old = currentActivities.get(activity.name);
			if (old == null) {
				old = new MergedActivity();
				old.name = activity.name;
				old.freeTexts.addAll(activity.freeTexts);
				for (DataLink link : activity.links)
					old.links.add(CollectionUtil.single_element_list(new DataLink(link)));
				currentActivities.add(old);
				continue;
			}
			for (String s : activity.freeTexts)
				if (!old.freeTexts.contains(s))
					old.freeTexts.add(s);
			for (DataLink newLink : activity.links) {
				boolean found = false;
				for (List<DataLink> oldList : old.links) {
					for (DataLink oldLink : oldList)
						if (oldLink.isSame(newLink)) {
							oldLink.merge(newLink);
							found = true;
							break;
						}
					if (found) break;
				}
				if (!found)
					old.links.add(CollectionUtil.single_element_list(new DataLink(newLink)));
			}
		}
	}
	

	@Override
	protected void copyLocalFiles(IFolder src, IFolder dst) {
		List<String> toRemove = copyImageFiles(src, dst, photos.values());
		for (String path : toRemove)
			for (Map.Entry<String,String> e : photos.entrySet())
				if (e.getValue().equals(path))
					photos.remove(e.getKey());
	}
	
	@Override
	public void merge(SourceInfo other) {
		PeopleSourceInfo i = (PeopleSourceInfo)other;
		setBirthDay(SourceInfoMergeUtil.mergeDate(birthDay, i.birthDay));
		setBirthPlace(SourceInfoMergeUtil.mergeString(birthPlace, i.birthPlace));
		setDescription(SourceInfoMergeUtil.mergeString(description, i.description));
		for (Map.Entry<String,String> e : i.photos.entrySet())
			addPhoto(e.getKey(), e.getValue());
		for (Activity a : i.activities) {
			for (String s : a.freeTexts)
				addActivity(a.name, s);
			for (DataLink l : a.links)
				addActivity(a.name, l);
		}
		for (Review r : i.publicReviews)
			setPublicReview(r.getAuthor(), r.getReview(), r.getRate());
	}
}
