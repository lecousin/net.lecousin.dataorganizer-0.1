package net.lecousin.dataorganizer.video;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.lecousin.dataorganizer.core.database.info.SourceInfo;
import net.lecousin.dataorganizer.core.database.info.Info.DataLink;
import net.lecousin.dataorganizer.core.database.info.SourceInfo.Review;
import net.lecousin.dataorganizer.core.database.version.ContentTypeLoader_0_1_0;
import net.lecousin.dataorganizer.video.VideoSourceInfo.Genre;
import net.lecousin.framework.Pair;
import net.lecousin.framework.collections.SelfMap;
import net.lecousin.framework.geometry.PointInt;
import net.lecousin.framework.version.Version;
import net.lecousin.framework.xml.XmlUtil;

import org.w3c.dom.Element;


public class Loader_0_1_0 extends ContentTypeLoader_0_1_0 implements Loader {

	public Loader_0_1_0() {
	}

	private static final Version version = new Version(0,1,0);
	public Version getVersion() { return version; }

	public long getDuration(Element root) {
		if (root.hasAttribute("duration"))
			return Long.parseLong(root.getAttribute("duration"));
		return -1;
	}
	public boolean getLoaded(Element root) {
		return Boolean.parseBoolean(root.getAttribute("loaded"));
	}
	public PointInt getDimension(Element root) {
		if (root.hasAttribute("width") && root.hasAttribute("height"))
			return new PointInt(Integer.parseInt(root.getAttribute("width")), Integer.parseInt(root.getAttribute("height")));
		return null;
	}
	
	public long getReleaseDate(Element root) {
		return Long.parseLong(root.getAttribute("releaseDate"));
	}
	
	public List<Genre> getGenres(Element root) {
		List<Genre> list = new LinkedList<Genre>();
		for (Element e : XmlUtil.get_childs_element(root, "genre"))
			list.add(Genre.valueOf(e.getAttribute("key")));
		return list;
	}
	
	public String getResume(Element root) {
		Element e = XmlUtil.get_child_element(root, "resume");
		if (e == null) return null;
		return XmlUtil.get_inner_text(e);
	}
	
	public List<Pair<String,DataLink>> getDirectors(Element root) {
		return loadListLinks("director", root);
	}
	public List<Pair<String,DataLink>> getActors(Element root) {
		return loadListLinks("actor", root);
	}
	public List<Pair<String,DataLink>> getProductors(Element root) {
		return loadListLinks("productor", root);
	}
	public List<Pair<String,DataLink>> getScenaristes(Element root) {
		return loadListLinks("scenariste", root);
	}
	
	public Map<String,String> getPosters(Element root) {
		Map<String,String> posters = new HashMap<String,String>();
		for (Element e : XmlUtil.get_childs_element(root, "poster"))
			posters.put(e.getAttribute("source"), e.getAttribute("local"));
		return posters;
	}
	
	public SelfMap<String,Review> getPressReviews(SourceInfo source, Element root) {
		return loadReviews(source, "pressReview", root);
	}
	public SelfMap<String,Review> getPublicReviews(SourceInfo source, Element root) {
		return loadReviews(source, "publicReview", root);
	}
	
	private List<Pair<String,DataLink>> loadListLinks(String tag, Element elt) {
		List<Pair<String,DataLink>> list = new LinkedList<Pair<String,DataLink>>();
		for (Element e : XmlUtil.get_childs_element(elt, tag)) {
			Element e2 = XmlUtil.get_child_element(e, "role");
			String role = e2.getAttribute("name");
			e2 = XmlUtil.get_child_element(e, "link");
			DataLink link = new DataLink(e2);
			list.add(new Pair<String,DataLink>(role, link));
		}
		return list;
	}
	
}
