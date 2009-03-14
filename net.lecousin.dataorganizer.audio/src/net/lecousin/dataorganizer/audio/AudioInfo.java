package net.lecousin.dataorganizer.audio;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.lecousin.dataorganizer.core.database.info.Info;
import net.lecousin.dataorganizer.core.database.version.ContentTypeLoader;
import net.lecousin.framework.Pair;
import net.lecousin.framework.Triple;
import net.lecousin.framework.xml.XmlWriter;

import org.w3c.dom.Element;

public class AudioInfo extends Info {

	public static final String FILE_SOURCE = "File";
	
	public AudioInfo(AudioDataType data, String source, String name) {
		super(data, source, name);
	}

	public AudioInfo(AudioDataType data, Element elt, ContentTypeLoader loader) {
		super(data, elt, loader);
		Loader l = (Loader)loader;
		album = l.getAlbum(elt);
		artist = l.getArtist(elt);
		year = l.getYear(elt);
		tracks = l.getTracks(elt, this);
		genres = l.getGenres(elt);
		cover_front = l.getCoverFront(elt);
		cover_back = l.getCoverBack(elt);
		images = l.getImages(elt);
	}

	@Override
	protected void saveInfo(XmlWriter xml) {
		if (album != null)
			xml.addAttribute("album", album);
		if (artist != null)
			xml.addAttribute("artist", artist);
		if (year > 0)
			xml.addAttribute("year", year);
		if (tracks != null)
			for (Track track : tracks) {
				xml.openTag("track");
				if (track.title != null)
					xml.addAttribute("title", track.title);
				if (track.length > 0)
					xml.addAttribute("length", track.length);
				for (Triple<String,String,String> t : track.images)
					xml.openTag("image").addAttribute("source", t.getValue1()).addAttribute("description", t.getValue2()).addAttribute("path", t.getValue3()).closeTag();
				xml.closeTag();
			}
		for (String genre : genres)
			xml.openTag("genre").addAttribute("name", genre).closeTag();
		for (Triple<String,String,String> t : cover_front)
			xml.openTag("cover_front").addAttribute("source", t.getValue1()).addAttribute("description", t.getValue2()).addAttribute("path", t.getValue3()).closeTag();
		for (Triple<String,String,String> t : cover_back)
			xml.openTag("cover_back").addAttribute("source", t.getValue1()).addAttribute("description", t.getValue2()).addAttribute("path", t.getValue3()).closeTag();
		for (Triple<String,String,String> t : images)
			xml.openTag("image").addAttribute("source", t.getValue1()).addAttribute("description", t.getValue2()).addAttribute("path", t.getValue3()).closeTag();
	}

	@Override
	public Map<String, Map<String, Pair<String, Integer>>> getReviews(String type) {
		return null;
	}

	@Override
	public Set<String> getReviewsTypes() {
		return new HashSet<String>();
	}
	
	private String album = null;
	private String artist = null;
	private List<Track> tracks = null;
	private List<String> genres = new LinkedList<String>();
	private int year = -1;
	private List<Triple<String,String,String>> cover_front = new LinkedList<Triple<String,String,String>>();
	private List<Triple<String,String,String>> cover_back = new LinkedList<Triple<String,String,String>>();
	private List<Triple<String,String,String>> images = new LinkedList<Triple<String,String,String>>();
	
	public class Track {
		String title = null;
		long length = -1;
		/* Source, Description, Path */
		List<Triple<String,String,String>> images = new LinkedList<Triple<String,String,String>>();
		
		public String getTitle() { return title; }
		public void setTitle(String name) { if (name.equals(title)) return; title = name; signalModification(); }
		public long getLength() { return length; }
		public void setLength(long l) { if (l == length) return; length = l; signalModification(); }
		public List<Triple<String,String,String>> getImages() { return new ArrayList<Triple<String,String,String>>(images); }
		public void addImage(String source, String description, String path) {
			images.add(new Triple<String,String,String>(source, description, path));
			signalModification();
		}
	}

	public String getAlbum() { return album; }
	public void setAlbum(String name) { if (name.equals(album)) return; album = name; signalModification(); }

	public String getArtist() { return artist; }
	public void setArtist(String name) { if (name.equals(artist)) return; artist = name; signalModification(); }
	
	public Track newTrack(String title, long length) {
		Track track = new Track();
		track.title = title;
		track.length = length;
		if (tracks == null)
			tracks = new LinkedList<Track>();
		tracks.add(track);
		signalModification();
		return track;
	}
	public Track newTrack(String title) { return newTrack(title, -1); }
	public Track newTrack(long length) { return newTrack(null, length); }
	public Track newTrack() { return newTrack(null, -1); }
	public List<Track> getTracks() { return new ArrayList<Track>(tracks); }
	
	public List<String> getGenres() { return new ArrayList<String>(genres); }
	public void addGenre(String genre) { if (genres.contains(genre)) return; genres.add(genre); signalModification(); }
	
	public int getYear() { return year; }
	public void setYear(int y) { if (y == year) return; year = y; signalModification(); }

}
