package net.lecousin.dataorganizer.video;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.lecousin.dataorganizer.core.database.info.Info.DataLink;
import net.lecousin.dataorganizer.core.database.version.ContentTypeLoader_0_1_0;
import net.lecousin.dataorganizer.video.VideoInfo.Genre;
import net.lecousin.framework.Pair;
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
	
	public long getReleaseDate(Element root) {
		return Long.parseLong(root.getAttribute("releaseDate"));
	}
	
	public List<Genre> getGenres(Element root) {
		List<Genre> list = new LinkedList<Genre>();
		for (Element e : XmlUtil.get_childs_element(root, "genre"))
			list.add(Genre.valueOf(e.getAttribute("key")));
		return list;
	}
	
	public Map<String,String> getResumes(Element root) {
		Map<String,String> resumes = new HashMap<String,String>();
		for (Element e : XmlUtil.get_childs_element(root, "resume"))
			resumes.put(e.getAttribute("source"), XmlUtil.get_inner_text(e));
		return resumes;
	}
	
	public List<Pair<List<String>,List<DataLink>>> getDirectors(Element root) {
		return loadListLinks("director", root);
	}
	public List<Pair<List<String>,List<DataLink>>> getActors(Element root) {
		return loadListLinks("actor", root);
	}
	public List<Pair<List<String>,List<DataLink>>> getProductors(Element root) {
		return loadListLinks("productor", root);
	}
	public List<Pair<List<String>,List<DataLink>>> getScenaristes(Element root) {
		return loadListLinks("scenariste", root);
	}
	
	public Map<String,String> getPosters(Element root) {
		Map<String,String> posters = new HashMap<String,String>();
		for (Element e : XmlUtil.get_childs_element(root, "poster"))
			posters.put(e.getAttribute("source"), e.getAttribute("local"));
		return posters;
	}
	
	public Map<String,Map<String,Pair<String,Integer>>> getPressReviews(Element root) {
		return loadReviews("pressReviews", root);
	}
	public Map<String,Map<String,Pair<String,Integer>>> getPublicReviews(Element root) {
		return loadReviews("publicReviews", root);
	}
	
	private List<Pair<List<String>,List<DataLink>>> loadListLinks(String tag, Element elt) {
		List<Pair<List<String>,List<DataLink>>> list = new LinkedList<Pair<List<String>,List<DataLink>>>();
		for (Element e : XmlUtil.get_childs_element(elt, tag)) {
			List<String> roles = new LinkedList<String>();
			for (Element e2 : XmlUtil.get_childs_element(e, "role"))
				roles.add(e2.getAttribute("name"));
			List<DataLink> links = new LinkedList<DataLink>();
			for (Element e2 : XmlUtil.get_childs_element(e, "link"))
				links.add(new DataLink(e2));
			list.add(new Pair<List<String>,List<DataLink>>(roles, links));
		}
		return list;
	}
	
	private Map<String,Map<String,Pair<String,Integer>>> loadReviews(String tag, Element elt) {
		Map<String,Map<String,Pair<String,Integer>>> reviews = new HashMap<String,Map<String,Pair<String,Integer>>>();
		for (Element e : XmlUtil.get_childs_element(elt, tag)) {
			String source = e.getAttribute("source");
			Map<String,Pair<String,Integer>> m = new HashMap<String,Pair<String,Integer>>();
			for (Element e2 : XmlUtil.get_childs_element(e, "critik")) {
				String author = e2.getAttribute("author");
				Integer note = Integer.parseInt(e2.getAttribute("note"));
				String critik = XmlUtil.get_inner_text(e2);
				m.put(author, new Pair<String,Integer>(critik, note));
			}
			reviews.put(source, m);
		}
		return reviews;
	}
	
}
