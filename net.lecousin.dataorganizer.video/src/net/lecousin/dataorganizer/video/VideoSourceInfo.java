package net.lecousin.dataorganizer.video;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.lecousin.dataorganizer.core.database.info.SourceInfo;
import net.lecousin.dataorganizer.core.database.info.SourceInfoMergeUtil;
import net.lecousin.dataorganizer.core.database.info.Info.DataLink;
import net.lecousin.framework.Pair;
import net.lecousin.framework.application.Application;
import net.lecousin.framework.collections.SelfMap;
import net.lecousin.framework.collections.SelfMapLinkedList;
import net.lecousin.framework.xml.XmlWriter;

import org.eclipse.core.resources.IFolder;
import org.w3c.dom.Element;

public class VideoSourceInfo extends SourceInfo {

	public VideoSourceInfo(VideoInfo parent) { 
		super(parent); 
	}
	public VideoSourceInfo(VideoInfo parent, Element root, Loader loader) {
		super(parent);
		if (root == null) return;
		releaseDate = loader.getReleaseDate(root);
		genre = loader.getGenres(root);
		resume = loader.getResume(root);
		directors = loader.getDirectors(root);
		actors = loader.getActors(root);
		productors = loader.getProductors(root);
		scenaristes = loader.getScenaristes(root);
		posters = loader.getPosters(root);
		pressReviews = loader.getPressReviews(this, root);
		publicReviews = loader.getPublicReviews(this, root);
	}
	@Override
	protected void saveInfo(XmlWriter xml) {
		xml.addAttribute("releaseDate", releaseDate);
		for (Genre g : genre)
			xml.openTag("genre").addAttribute("key", g.toString()).closeTag();
		if (resume != null)
			xml.openTag("resume").addText(resume).closeTag();
		saveListLinks(directors, "director", xml);
		saveListLinks(actors, "actor", xml);
		saveListLinks(productors, "productor", xml);
		saveListLinks(scenaristes, "scenariste", xml);
		for (String s : posters.keySet())
			xml.openTag("poster").addAttribute("source", s).addAttribute("local", posters.get(s)).closeTag();
		saveCritiks(pressReviews, "pressReview", xml);
		saveCritiks(publicReviews, "publicReview", xml);
	}
	private synchronized void saveListLinks(List<Pair<String,DataLink>> list, String tag, XmlWriter xml) {
		for (Pair<String,DataLink> p : list) {
			xml.openTag(tag);
			xml.openTag("role").addAttribute("name", p.getValue1()).closeTag();
			xml.openTag("link");
			p.getValue2().save(xml);
			xml.closeTag();
			xml.closeTag();
		}
	}
	
	/** date de sortie */
	private long releaseDate;
	/** genres */
	private List<Genre> genre = new LinkedList<Genre>();
	/** resume */
	private String resume = null;
	/** realisateurs */
	private List<Pair<String,DataLink>> directors = new LinkedList<Pair<String,DataLink>>();
	/** acteurs <Role,Acteur> */
	private List<Pair<String,DataLink>> actors = new LinkedList<Pair<String,DataLink>>();
	/** production */
	private List<Pair<String,DataLink>> productors = new LinkedList<Pair<String,DataLink>>();
	/** scenario */
	private List<Pair<String,DataLink>> scenaristes = new LinkedList<Pair<String,DataLink>>();
	
	/** posters <SourceURL,File_relative_to_data.getFolder()> */
	private Map<String,String> posters = new HashMap<String,String>();
	
	/** press reviews */
	private SelfMap<String,Review> pressReviews = new SelfMapLinkedList<String,Review>(5);
	/** public reviews */
	private SelfMap<String,Review> publicReviews = new SelfMapLinkedList<String,Review>(5);
	
	public long getReleaseDate() { return releaseDate; }
	public List<Genre> getGenres() { return new ArrayList<Genre>(genre); }
	public String getResume() { return resume; }
	
	public List<Pair<String,DataLink>> getDirectors() { return new ArrayList<Pair<String,DataLink>>(directors); }
	public List<Pair<String,DataLink>> getActors() { return new ArrayList<Pair<String,DataLink>>(actors); }
	public List<Pair<String,DataLink>> getProductors() { return new ArrayList<Pair<String,DataLink>>(productors); }
	public List<Pair<String,DataLink>> getScenaristes() { return new ArrayList<Pair<String,DataLink>>(scenaristes); }
	
	public Collection<String> getPostersPaths() { return posters.values(); }
	public boolean hasPosterURL(String url) { return posters.containsKey(url); }
	
	@Override
	public SelfMap<String, Review> getReviews(String type) {
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
	public void setResume(String text) {
		if (resume != null && resume.equals(text)) return;
		if (resume == null || text.length() > 0) {
			resume = text;
			signalModification();
		}
	}
	
	public synchronized void setDirectors(List<Pair<String,DataLink>> list) {
		mergeNewLinks(directors, list);
	}
	public synchronized void setActors(List<Pair<String,DataLink>> list) {
		mergeNewLinks(actors, list);
	}
	public synchronized void setProductors(List<Pair<String,DataLink>> list) {
		mergeNewLinks(productors, list);
	}
	public synchronized void setScenaristes(List<Pair<String,DataLink>> list) {
		mergeNewLinks(scenaristes, list);
	}
	
	public void addPoster(String url, String localPath) {
		posters.put(url, localPath);
		if (getParent() != null)
			((VideoDataType)getParent().getDataContent()).signalNewPoster();
		signalModification();
	}
	
	public synchronized void setPublicReview(String author, String review, Integer note) {
		setReview(publicReviews, author, review, note);
	}
	public synchronized void setPressReview(String author, String review, Integer note) {
		setReview(pressReviews, author, review, note);
	}
	
	private void mergeNewLinks(List<Pair<String,DataLink>> currentList, List<Pair<String,DataLink>> newList) {
		boolean changed = false;
		for (Pair<String,DataLink> pNew : newList) {
			String newName = pNew.getValue1();
			DataLink newLink = pNew.getValue2();
			// looking for the same link
			boolean linkFound = false;
			for (Pair<String,DataLink> pOld : currentList) {
				if (pOld.getValue2().isSame(newLink)) {
					linkFound = true;
					changed |= pOld.getValue2().merge(newLink);
					if (pOld.getValue1().length() == 0 && newName.length() > 0) {
						changed = true;
						pOld.setValue1(newName);
					}
					break;
				}
			}
			if (linkFound) continue;
			// add the new link
			currentList.add(pNew);
			changed = true;
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
		Experimental("Experimental", "Expérimental"),
		Catastrophe("Disaster", "Catastrophe"),
		ComedieRomantique("Romantic comedy", "Comédie romantique"),
		ConferenceFilmee("Conference", "Conference"),
		DessinAnimeAdulte("Adult Cartoon", "Dessin animé adulte"),
		DramePsychologique("Psychologic drama", "Drame psychologique"),
		Noir("Film noir", "Film noir"),
		GrandSpectacle("Big show", "Grand spectacle"),
		Politique("Political", "Politique"),
		Porno("Porno", "Porno"),
		;
		
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

	@Override
	protected void copyLocalFiles(IFolder src, IFolder dst) {
		List<String> toRemove = copyImageFiles(src, dst, posters.values());
		for (String path : toRemove)
			for (Map.Entry<String,String> e : posters.entrySet())
				if (e.getValue().equals(path))
					posters.remove(e.getKey());
	}
	
	@Override
	public void merge(SourceInfo other) {
		VideoSourceInfo i = (VideoSourceInfo)other;
		setReleaseDate(SourceInfoMergeUtil.mergeDate(releaseDate, i.releaseDate));
		for (Genre g : i.genre)
			addGenre(g);
		setResume(SourceInfoMergeUtil.mergeString(resume, i.resume));
		for (Map.Entry<String,String> e : i.posters.entrySet())
			addPoster(e.getKey(), e.getValue());
		setDirectors(i.directors);
		setActors(i.actors);
		setProductors(i.productors);
		setScenaristes(i.scenaristes);
		for (Review r : i.publicReviews)
			setPublicReview(r.getAuthor(), r.getReview(), r.getRate());
		for (Review r : i.pressReviews)
			setPressReview(r.getAuthor(), r.getReview(), r.getRate());
	}
}
