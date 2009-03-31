package net.lecousin.dataorganizer.audio;

import java.util.Set;

import net.lecousin.dataorganizer.core.database.info.Info;
import net.lecousin.dataorganizer.core.database.info.SourceInfo;
import net.lecousin.dataorganizer.core.database.version.ContentTypeLoader;
import net.lecousin.framework.xml.XmlWriter;

import org.w3c.dom.Element;

public class AudioInfo extends Info {

	public static final String FILE_SOURCE = "File";
	
	public AudioInfo(AudioDataType data) {
		super(data);
	}

	public AudioInfo(AudioDataType data, Element elt, ContentTypeLoader loader) {
		super(data, elt, loader);
		if (elt.hasAttribute("album"))
			album = elt.getAttribute("album");
		if (elt.hasAttribute("artist"))
			artist = elt.getAttribute("artist");
		if (elt.hasAttribute("year"))
			year = Integer.parseInt(elt.getAttribute("year"));
	}
	
	@Override
	protected SourceInfo createSourceInfo(Info parent) {
		return new AudioSourceInfo((AudioInfo)parent);
	}
	@Override
	protected SourceInfo createSourceInfo(Info parent, Element elt, ContentTypeLoader loader) {
		return new AudioSourceInfo((AudioInfo)parent, elt, (Loader)loader);
	}
	
	@Override
	public AudioSourceInfo getSourceInfo(String source) {
		return (AudioSourceInfo)super.getSourceInfo(source);
	}

	@Override
	public Set<String> getReviewsTypes() {
		return null;
	}
	
	private String album = null;
	private String artist = null;
	private int year = -1;
	
	public String getAlbum() {
		return album;
	}
	public String getArtist() {
		return artist;
	}
	public int getYear() {
		return year;
	}
	public void setAlbum(String album) {
		if (album == null || album.length() == 0 || album.equals(this.album)) return;
		this.album = album;
		signalModification();
	}
	public void setArtist(String artist) {
		if (artist == null || artist.length() == 0 || artist.equals(this.artist)) return;
		this.artist = artist;
		signalModification();
	}
	public void setYear(int year) {
		if (year <= 0 || year == this.year) return;
		this.year = year;
		signalModification();
	}
	
	@Override
	protected void saveInfo(XmlWriter xml) {
		if (album != null) xml.addAttribute("album", album);
		if (artist != null) xml.addAttribute("artist", artist);
		if (year > 0) xml.addAttribute("year", year);
	}
	
	@Override
	protected void signalModification() {
		for (String source : getSources()) {
			AudioSourceInfo i = getSourceInfo(source);
			if (i == null) continue;
			if (album == null && i.getAlbum() != null)
				album = i.getAlbum();
			if (artist == null && i.getArtist() != null)
				artist = i.getArtist();
			if (year < 0 && i.getYear() > 0)
				year = i.getYear();
		}
		super.signalModification();
	}
}
