package net.lecousin.dataorganizer.audio;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.audio.AudioSourceInfo.Track;
import net.lecousin.dataorganizer.core.database.info.SourceInfo;
import net.lecousin.dataorganizer.core.database.info.SourceInfo.Review;
import net.lecousin.dataorganizer.core.database.version.ContentTypeLoader_0_1_0;
import net.lecousin.framework.Pair;
import net.lecousin.framework.collections.SelfMap;
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
	public List<Track> getTracks(Element root, AudioSourceInfo info) {
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
	public List<Pair<String,String>> readImages(Element elt, String nodeName) {
		List<Pair<String,String>> result = new LinkedList<Pair<String,String>>();
		for (Element e : XmlUtil.get_childs_element(elt, nodeName))
			result.add(new Pair<String,String>(e.getAttribute("description"), e.getAttribute("path")));
		return result;
	}
	public List<String> getGenres(Element root) {
		List<String> result = new LinkedList<String>();
		for (Element e : XmlUtil.get_childs_element(root, "genre"))
			result.add(e.getAttribute("name"));
		return result;
	}
	public List<Pair<String,String>> getCoverFront(Element root) {
		return readImages(root, "cover_front");
	}
	public List<Pair<String,String>> getCoverBack(Element root) {
		return readImages(root, "cover_back");
	}
	public List<Pair<String,String>> getImages(Element root) {
		return readImages(root, "image");
	}
	
	public byte[] getMCDI(Element root) {
		Element elt = XmlUtil.get_child_element(root, "mcdi");
		if (elt == null) return null;
		return StringUtil.decodeHexa(XmlUtil.get_inner_text(elt));
	}
	
	public SelfMap<String,Review> getPublicReviews(SourceInfo source, Element root) {
		return loadReviews(source, "publicReview", root);
	}
	
	public String getDescription(Element root) {
		Element e = XmlUtil.get_child_element(root, "description");
		if (e == null) return null;
		return XmlUtil.get_inner_text(e);
	}
}
