package net.lecousin.dataorganizer.video;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.lecousin.dataorganizer.core.database.info.Info;
import net.lecousin.framework.Pair;
import net.lecousin.framework.application.Application;
import net.lecousin.framework.collections.CollectionUtil;
import net.lecousin.framework.xml.XmlWriter;

import org.w3c.dom.Element;

public class VideoInfo extends Info {

	public VideoInfo(VideoDataType data, String source, String name) { 
		super(data, source, name); 
	}
	public VideoInfo(VideoDataType data, Element root, Loader loader) {
		super(data, root, loader);
		if (root == null) return;
		releaseDate = loader.getReleaseDate(root);
		genre = loader.getGenres(root);
		resumes = loader.getResumes(root);
		directors = loader.getDirectors(root);
		actors = loader.getActors(root);
		productors = loader.getProductors(root);
		scenaristes = loader.getScenaristes(root);
		posters = loader.getPosters(root);
		pressReviews = loader.getPressReviews(root);
		publicReviews = loader.getPublicReviews(root);
	}
	protected void saveInfo(XmlWriter xml) {
		xml.addAttribute("releaseDate", releaseDate);
		for (Genre g : genre)
			xml.openTag("genre").addAttribute("key", g.toString()).closeTag();
		for (String s : resumes.keySet())
			xml.openTag("resume").addAttribute("source", s).addText(resumes.get(s)).closeTag();
		saveListLinks(directors, "director", xml);
		saveListLinks(actors, "actor", xml);
		saveListLinks(productors, "productor", xml);
		saveListLinks(scenaristes, "scenariste", xml);
		for (String s : posters.keySet())
			xml.openTag("poster").addAttribute("source", s).addAttribute("local", posters.get(s)).closeTag();
		saveCritiks(pressReviews, "pressReviews", xml);
		saveCritiks(publicReviews, "publicReviews", xml);
	}
	private void saveListLinks(List<Pair<List<String>,List<DataLink>>> list, String tag, XmlWriter xml) {
		for (Pair<List<String>,List<DataLink>> p : list) {
			xml.openTag(tag);
			for (String role : p.getValue1())
				xml.openTag("role").addAttribute("name", role).closeTag();
			for (DataLink dl : p.getValue2()) {
				xml.openTag("link");
				dl.save(xml);
				xml.closeTag();
			}
			xml.closeTag();
		}
	}
	private void saveCritiks(Map<String,Map<String,Pair<String,Integer>>> critiks, String tag, XmlWriter xml) {
		for (String s : critiks.keySet()) {
			xml.openTag(tag).addAttribute("source", s);
			Map<String,Pair<String,Integer>> m = critiks.get(s);
			for (String a : m.keySet()) {
				Pair<String,Integer> p = m.get(a);
				xml.openTag("critik").addAttribute("author", a).addAttribute("note", p.getValue2()).addText(p.getValue1()).closeTag();
			}
			xml.closeTag();
		}
	}
	
	/** date de sortie */
	private long releaseDate;
	/** genres */
	private List<Genre> genre = new LinkedList<Genre>();
	/** resumes <Source,Resume> */
	private Map<String,String> resumes = new HashMap<String,String>();
	/** realisateurs */
	private List<Pair<List<String>,List<DataLink>>> directors = new LinkedList<Pair<List<String>,List<DataLink>>>();
	/** acteurs <Role,Acteur> */
	private List<Pair<List<String>,List<DataLink>>> actors = new LinkedList<Pair<List<String>,List<DataLink>>>();
	/** production */
	private List<Pair<List<String>,List<DataLink>>> productors = new LinkedList<Pair<List<String>,List<DataLink>>>();
	/** scenario */
	private List<Pair<List<String>,List<DataLink>>> scenaristes = new LinkedList<Pair<List<String>,List<DataLink>>>();
	
	/** posters <SourceURL,File_relative_to_data.getFolder()> */
	private Map<String,String> posters = new HashMap<String,String>();
	
	/** reviews <Source,<Author,<Critik,Note/20>>> */
	private Map<String,Map<String,Pair<String,Integer>>> pressReviews = new HashMap<String,Map<String,Pair<String,Integer>>>();
	/** reviews <Source,<Author,<Critik,Note/20>>> */
	private Map<String,Map<String,Pair<String,Integer>>> publicReviews = new HashMap<String,Map<String,Pair<String,Integer>>>();
	
	public long getReleaseDate() { return releaseDate; }
	public List<Genre> getGenres() { return new ArrayList<Genre>(genre); }
	public Set<String> getResumesSources() { return resumes.keySet(); }
	public String getResume(String source) { return resumes.get(source); }
	
	public List<Pair<List<String>,List<DataLink>>> getDirectors() { return new ArrayList<Pair<List<String>,List<DataLink>>>(directors); }
	public List<Pair<List<String>,List<DataLink>>> getActors() { return new ArrayList<Pair<List<String>,List<DataLink>>>(actors); }
	public List<Pair<List<String>,List<DataLink>>> getProductors() { return new ArrayList<Pair<List<String>,List<DataLink>>>(productors); }
	public List<Pair<List<String>,List<DataLink>>> getScenaristes() { return new ArrayList<Pair<List<String>,List<DataLink>>>(scenaristes); }
	
	public Collection<String> getPostersPaths() { return posters.values(); }
	public boolean hasPosterURL(String url) { return posters.containsKey(url); }
	
	private static Set<String> reviewsTypes = new HashSet<String>(CollectionUtil.list(new String[] {
		Local.Press.toString(), Local.Public.toString()
	}));
	@Override
	public Set<String> getReviewsTypes() { return reviewsTypes; }
	@Override
	public Map<String, Map<String, Pair<String, Integer>>> getReviews(String type) {
		if (type.equals(Local.Press.toString())) return pressReviews;
		if (type.equals(Local.Public.toString())) return publicReviews;
		return null;
	}
	
	public void setReleaseDate(long date) {
		if (releaseDate == date) return;
		releaseDate = date;
		signalModification();
	}
	public void addGenre(Genre g) {
		if (genre.contains(g)) return;
		genre.add(g);
		signalModification();
	}
	public void setResume(String source, String resume) {
		String previous = resumes.get(source);
		if (previous != null && previous.equals(resume)) return;
		if (previous == null || resume.length() > 0) {
			resumes.put(source, resume);
			signalModification();
		}
	}
	
	public void setDirectors(List<Pair<String,DataLink>> list) {
		merge(directors, list);
	}
	public void setActors(List<Pair<String,DataLink>> list) {
		merge(actors, list);
	}
	public void setProductors(List<Pair<String,DataLink>> list) {
		merge(productors, list);
	}
	public void setScenaristes(List<Pair<String,DataLink>> list) {
		merge(scenaristes, list);
	}
	
	public void addPoster(String url, String localPath) {
		posters.put(url, localPath);
		((VideoDataType)getDataContent()).signalNewPoster();
		signalModification();
	}
	
	public void setPublicReview(String source, String author, String review, Integer note) {
		setReview(publicReviews, source, author, review, note);
	}
	public void setPressReview(String source, String author, String review, Integer note) {
		setReview(pressReviews, source, author, review, note);
	}
	
	private void merge(List<Pair<List<String>,List<DataLink>>> currentList, List<Pair<String,DataLink>> newList) {
		boolean changed = false;
		for (Pair<String,DataLink> pNew : newList) {
			String newName = pNew.getValue1();
			DataLink newLink = pNew.getValue2();
			// looking for the same link
			boolean linkFound = false;
			for (Pair<List<String>,List<DataLink>> pOld : currentList) {
				for (DataLink oldLink : pOld.getValue2()) {
					if (!oldLink.merge(newLink)) continue;
					linkFound = true;
					break;
				}
				if (!linkFound) continue;
				if (!pOld.getValue1().contains(newName)) {
					pOld.getValue1().add(newName);
					changed = true;
				}
				break;
			}
			if (linkFound) continue;
			// looking for the same name in a link
			linkFound = false;
			for (Pair<List<String>,List<DataLink>> pOld : currentList) {
				for (DataLink oldLink : pOld.getValue2()) {
					if (!oldLink.name.equals(newLink.name)) continue;
					// same people name: add the new link
					pOld.getValue2().add(newLink);
					changed = true;
					linkFound = true;
					break;
				}
				if (!linkFound) continue;
				if (!pOld.getValue1().contains(newName)) {
					pOld.getValue1().add(newName);
					changed = true;
				}
				break;
			}
			if (linkFound) continue;
			// neither link or same people name found, we cannot assume this is the same people => add it
			List<String> list1 = new LinkedList<String>();
			list1.add(newName);
			List<DataLink> list2 = new LinkedList<DataLink>();
			list2.add(newLink);
			currentList.add(new Pair<List<String>,List<DataLink>>(list1, list2));
			changed = true;
		}
		if (changed)
			signalModification();
	}
	
	private void setReview(Map<String,Map<String,Pair<String,Integer>>> reviews, String source, String author, String review, Integer note) {
		boolean changed = false;
		Map<String,Pair<String,Integer>> sourceReviews = reviews.get(source);
		if (sourceReviews == null) {
			sourceReviews = new HashMap<String,Pair<String,Integer>>();
			reviews.put(source, sourceReviews);
			changed = true;
		}
		Pair<String,Integer> authorReview = sourceReviews.get(author);
		if (authorReview == null) {
			authorReview = new Pair<String,Integer>(review, note);
			sourceReviews.put(author, authorReview);
			changed = true;
		} else {
			if (!authorReview.getValue1().equals(review) && review.length() > 0) {
				authorReview.setValue1(review);
				changed = true;
			}
			if (note != null && (authorReview.getValue2() == null || !authorReview.getValue2().equals(note))) {
				authorReview.setValue2(note);
				changed = true;
			}
		}
		if (changed)
			signalModification();
	}
	
	public enum Genre {
		Documentaire("Documentary", "Documentaire"),
		Action("Action", "Action"),
		Aventure("Adventure", "Aventure"),
		ScienceFiction("Science-Fiction", "Science-Fiction"),
		Fantastique("Fantiastic", "Fantastique"),
		Policier("Crime", "Policier"),
		Thriller("Thriller", "Thriller"),
		Comedie("Comedy", "Comédie"),
		ComedieMusicale("Musical", "Comédie musicale"),
		Drame("Drama", "Drame"),
		ComedieDramatique("Comedy-Drama", "Comédie dramatique"),
		Romance("Romance", "Romance"),
		DessinAnime("Cartoon", "Dessin Animé"),
		Animation("Animation", "Animation"),
		HorreurEpouvante("Horror", "Horreur-Epouvante"),
		Guerre("War", "Guerre"),
		Historique("Historic", "Historique"),
		Biographie("Biography", "Biographie"),
		Erotique("Erotic", "Erotique"),
		Western("Western", "Western"),
		CourtMetrage("Short Film", "Court Métrage"),
		ArtsMartiaux("Martial arts", "Arts Martiaux"),
		Espionnage("Spy", "Espionnage"),
		Famille("Family", "Famille"),
		Experimental("Experimental", "Expérimental");
		
		Genre(String english, String french) 
		{ this.english = english; this.french = french; }
		private String english;
		private String french;
		public String getDescription() {
			switch (Application.language) {
			case FRENCH: return french;
			default: return english;
			}
		}
	}
}
