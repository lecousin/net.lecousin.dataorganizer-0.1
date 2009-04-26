package net.lecousin.dataorganizer.audio;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.core.database.info.SourceInfo;
import net.lecousin.dataorganizer.core.database.version.ContentTypeLoader;
import net.lecousin.framework.Pair;
import net.lecousin.framework.collections.ArrayUtil;
import net.lecousin.framework.collections.SelfMap;
import net.lecousin.framework.strings.StringUtil;
import net.lecousin.framework.xml.XmlWriter;

import org.eclipse.core.resources.IFolder;
import org.w3c.dom.Element;

public class AudioSourceInfo extends SourceInfo {

	public AudioSourceInfo(AudioInfo parent) {
		super(parent);
	}

	public AudioSourceInfo(AudioInfo parent, Element elt, ContentTypeLoader loader) {
		super(parent);
		Loader l = (Loader)loader;
		album = l.getAlbum(elt);
		artist = l.getArtist(elt);
		year = l.getYear(elt);
		tracks = l.getTracks(elt, this);
		genres = l.getGenres(elt);
		cover_front = l.getCoverFront(elt);
		cover_back = l.getCoverBack(elt);
		images = l.getImages(elt);
		mcdi = l.getMCDI(elt);
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
				for (Pair<String,String> t : track.images)
					xml.openTag("image").addAttribute("description", t.getValue1()).addAttribute("path", t.getValue2()).closeTag();
				xml.closeTag();
			}
		for (String genre : genres)
			xml.openTag("genre").addAttribute("name", genre).closeTag();
		for (Pair<String,String> t : cover_front)
			xml.openTag("cover_front").addAttribute("description", t.getValue1()).addAttribute("path", t.getValue2()).closeTag();
		for (Pair<String,String> t : cover_back)
			xml.openTag("cover_back").addAttribute("description", t.getValue1()).addAttribute("path", t.getValue2()).closeTag();
		for (Pair<String,String> t : images)
			xml.openTag("image").addAttribute("description", t.getValue1()).addAttribute("path", t.getValue2()).closeTag();
		if (mcdi != null)
			xml.openTag("mcdi").addText(StringUtil.encodeHexa(mcdi)).closeTag();
	}

	@Override
	public SelfMap<String, Review> getReviews(String type) {
		return null;
	}
	
	private String album = null;
	private String artist = null;
	private List<Track> tracks = null;
	private List<String> genres = new LinkedList<String>();
	private int year = -1;
	private byte[] mcdi = null;
	private List<Pair<String,String>> cover_front = new LinkedList<Pair<String,String>>();
	private List<Pair<String,String>> cover_back = new LinkedList<Pair<String,String>>();
	private List<Pair<String,String>> images = new LinkedList<Pair<String,String>>();
	
	public class Track {
		String title = null;
		long length = -1;
		/* Description, Path */
		List<Pair<String,String>> images = new LinkedList<Pair<String,String>>();
		
		public String getTitle() { return title; }
		public void setTitle(String name) { if (name == null || name.length()==0 || name.equals(title)) return; title = name; signalModification(); }
		public long getLength() { return length; }
		public void setLength(long l) { if (l < 0 || l == length) return; length = l; signalModification(); }
		public List<Pair<String,String>> getImages() { return new ArrayList<Pair<String,String>>(images); }
		public void addImage(String description, String path) {
			images.add(new Pair<String,String>(description, path));
			signalModification();
		}
		
		public void move(int index) {
			if (tracks.indexOf(this) == index) return;
			if (index < 0) return;
			if (index >= tracks.size()) return;
			tracks.remove(this);
			tracks.add(index, this);
			signalModification();
		}
	}

	public String getAlbum() { return album; }
	public void setAlbum(String name) { if (name == null || name.length() == 0 || name.equals(album)) return; album = name; signalModification(); }

	public String getArtist() { return artist; }
	public void setArtist(String name) { if (name == null || name.length() == 0 || name.equals(artist)) return; artist = name; signalModification(); }
	
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
	public Track getTrack(int index) { return tracks.get(index); }
	public int indexOf(Track track) { return tracks.indexOf(track); }
	
	public List<String> getGenres() { return new ArrayList<String>(genres); }
	public void addGenre(String genre) { if (genres.contains(genre)) return; genres.add(genre); signalModification(); }
	
	public int getYear() { return year; }
	public void setYear(int y) { if (y <= 0 || y == year) return; year = y; signalModification(); }

	public byte[] getMCDI() { return mcdi; }
	public void setMCDI(byte[] mcdi) { 
		if (mcdi == null) return;
		if (mcdi != null && this.mcdi != null && ArrayUtil.equals(mcdi, this.mcdi)) return;
		this.mcdi = mcdi;
		signalModification();
	}
	
	@Override
	public void merge(SourceInfo info) {
		AudioSourceInfo i = (AudioSourceInfo)info;
		if (album == null) setAlbum(i.getAlbum());
		if (artist == null) setArtist(i.getArtist());
		if (year < 0) setYear(i.getYear());
		if (mcdi == null) setMCDI(i.getMCDI());
		for (String genre : i.getGenres())
			addGenre(genre);
		boolean changed = false;
		changed |= mergeImages(cover_front, i.cover_front);
		changed |= mergeImages(cover_back, i.cover_back);
		changed |= mergeImages(images, i.images);
		if (i.tracks != null) {
			if (tracks == null) {
				tracks = i.tracks;
				changed = true;
			} else {
				for (Track tNew : i.tracks) {
					Track tOld = null;
					for (Track t : tracks)
						if (t.title != null && t.title.equals(tNew.title)) {
							tOld = t;
							break;
						}
					if (tOld == null) {
						tracks.add(tNew);
						changed = true;
						continue;
					}
					if (tOld.length < 0) tOld.setLength(tNew.length);
					changed |= mergeImages(tOld.images, tNew.images);
				}
			}
		}
		if (changed) signalModification();
	}
	private boolean mergeImages(List<Pair<String,String>> currentList, List<Pair<String,String>> newList) {
		boolean changed = false;
		for (Pair<String,String> pNew : newList) {
			boolean found = false;
			for (Pair<String,String> pOld : currentList)
				if (pOld.getValue2().equals(pNew.getValue2())) {
					found = true;
					break;
				}
			if (!found) {
				currentList.add(pNew);
				changed = true;
			}
		}
		return changed;
	}
	

	@Override
	protected void copyLocalFiles(IFolder src, IFolder dst) {
		removeImages(copyImageFiles(src, dst, getImagesPaths(cover_front)), cover_front);
		removeImages(copyImageFiles(src, dst, getImagesPaths(cover_back)), cover_back);
		removeImages(copyImageFiles(src, dst, getImagesPaths(images)), images);
		for (Track t : tracks)
			removeImages(copyImageFiles(src, dst, getImagesPaths(t.images)), t.images);
	}
	private List<String> getImagesPaths(List<Pair<String,String>> images) {
		List<String> result = new ArrayList<String>(images.size());
		for (Pair<String,String> p : images)
			result.add(p.getValue2());
		return result;
	}
	private void removeImages(List<String> paths, List<Pair<String,String>> images) {
		for (Iterator<Pair<String,String>> it = images.iterator(); it.hasNext(); ) {
			Pair<String,String> p = it.next();
			if (paths.contains(p.getValue2()))
				it.remove();
		}
	}

}
