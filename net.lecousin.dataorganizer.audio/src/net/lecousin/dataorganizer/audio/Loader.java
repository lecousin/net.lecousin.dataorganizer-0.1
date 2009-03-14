package net.lecousin.dataorganizer.audio;

import java.util.List;

import net.lecousin.dataorganizer.audio.AudioInfo.Track;
import net.lecousin.dataorganizer.core.database.version.ContentTypeLoader;
import net.lecousin.framework.Triple;

import org.w3c.dom.Element;

public interface Loader extends ContentTypeLoader {

	public String getAlbum(Element root);
	public String getArtist(Element root);
	public int getYear(Element root);
	public List<Track> getTracks(Element root, AudioInfo info);
	public List<String> getGenres(Element root);
	public List<Triple<String,String,String>> getCoverFront(Element root);
	public List<Triple<String,String,String>> getCoverBack(Element root);
	public List<Triple<String,String,String>> getImages(Element root);
	
}
