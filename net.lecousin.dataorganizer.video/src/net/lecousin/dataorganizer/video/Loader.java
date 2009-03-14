package net.lecousin.dataorganizer.video;

import java.util.List;
import java.util.Map;

import net.lecousin.dataorganizer.core.database.info.Info.DataLink;
import net.lecousin.dataorganizer.core.database.version.ContentTypeLoader;
import net.lecousin.dataorganizer.video.VideoInfo.Genre;
import net.lecousin.framework.Pair;

import org.w3c.dom.Element;

public interface Loader extends ContentTypeLoader {

	public long getDuration(Element root);
	public boolean getLoaded(Element root);
	
	public long getReleaseDate(Element root);
	public List<Genre> getGenres(Element root);
	public Map<String,String> getResumes(Element root);
	public List<Pair<List<String>,List<DataLink>>> getDirectors(Element root);
	public List<Pair<List<String>,List<DataLink>>> getActors(Element root);
	public List<Pair<List<String>,List<DataLink>>> getProductors(Element root);
	public List<Pair<List<String>,List<DataLink>>> getScenaristes(Element root);
	public Map<String,String> getPosters(Element root);
	public Map<String,Map<String,Pair<String,Integer>>> getPressReviews(Element root);
	public Map<String,Map<String,Pair<String,Integer>>> getPublicReviews(Element root);
	
}
