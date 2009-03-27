package net.lecousin.dataorganizer.audio;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.audio.AudioInfo.Track;
import net.lecousin.dataorganizer.core.database.version.ContentTypeLoader_0_1_0;
import net.lecousin.framework.Triple;
import net.lecousin.framework.strings.StringUtil;
import net.lecousin.framework.version.Version;
import net.lecousin.framework.xml.XmlUtil;

import org.w3c.dom.Element;


public class Loader_0_1_0 extends ContentTypeLoader_0_1_0 implements Loader {

	public Loader_0_1_0() {
	}

	private static final Version version = new Version(0,1,0);
	public Version getVersion() { return version; }

	public String getAlbum(Element root) {
		return root.hasAttribute("album") ? root.getAttribute("album") : null;
	}
	public String getArtist(Element root) {
		return root.hasAttribute("artist") ? root.getAttribute("artist") : null;
	}
	public int getYear(Element root) {
		return root.hasAttribute("year") ? Integer.parseInt(root.getAttribute("year")) : -1;
	}
	public List<Track> getTracks(Element root, AudioInfo info) {
		List<Track> result = new LinkedList<Track>();
		for (Element e : XmlUtil.get_childs_element(root, "track")) {
			Track t = info.new Track();
			result.add(t);
			if (e.hasAttribute("title")) t.title = e.getAttribute("title");
			if (e.hasAttribute("length")) t.length = Long.parseLong(e.getAttribute("length"));
			t.images.addAll(readImages(e, "image"));
		}
		return result;
	}
	public List<Triple<String,String,String>> readImages(Element elt, String nodeName) {
		List<Triple<String,String,String>> result = new LinkedList<Triple<String,String,String>>();
		for (Element e : XmlUtil.get_childs_element(elt, nodeName))
			result.add(new Triple<String,String,String>(e.getAttribute("source"), e.getAttribute("description"), e.getAttribute("path")));
		return result;
	}
	public List<String> getGenres(Element root) {
		List<String> result = new LinkedList<String>();
		for (Element e : XmlUtil.get_childs_element(root, "genre"))
			result.add(e.getAttribute("name"));
		return result;
	}
	public List<Triple<String,String,String>> getCoverFront(Element root) {
		return readImages(root, "cover_front");
	}
	public List<Triple<String,String,String>> getCoverBack(Element root) {
		return readImages(root, "cover_back");
	}
	public List<Triple<String,String,String>> getImages(Element root) {
		return readImages(root, "image");
	}
	
	public byte[] getMCDI(Element root) {
		Element elt = XmlUtil.get_child_element(root, "mcdi");
		if (elt == null) return null;
		return StringUtil.decodeHexa(XmlUtil.get_inner_text(elt));
	}
}
