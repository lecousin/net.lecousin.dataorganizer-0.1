package net.lecousin.dataorganizer.people;

import java.util.Map;

import net.lecousin.dataorganizer.core.database.info.SourceInfo;
import net.lecousin.dataorganizer.core.database.info.SourceInfo.Review;
import net.lecousin.dataorganizer.core.database.version.ContentTypeLoader;
import net.lecousin.dataorganizer.people.PeopleSourceInfo.Activity;
import net.lecousin.framework.collections.SelfMap;

import org.w3c.dom.Element;

public interface Loader extends ContentTypeLoader {

	public long getBirthDay(Element root);
	public String getBirthPlace(Element root);
	public String getDescription(Element root);
	public SelfMap<String,Activity> getActivities(Element root);
	public Map<String,String> getPhotos(Element root);
	public SelfMap<String,Review> getPublicReviews(SourceInfo source, Element root);
	
}
