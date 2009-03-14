package net.lecousin.dataorganizer.people;

import java.util.List;
import java.util.Map;

import net.lecousin.dataorganizer.core.database.info.Info.DataLink;
import net.lecousin.dataorganizer.core.database.version.ContentTypeLoader;
import net.lecousin.framework.Pair;

import org.w3c.dom.Element;

public interface Loader extends ContentTypeLoader {

	public long getBirthDay(Element root);
	public String getBirthPlace(Element root);
	public Map<String,Pair<List<String>,List<List<DataLink>>>> getActivities(Element root);
	public Map<String,String> getPhotos(Element root);
	
}
