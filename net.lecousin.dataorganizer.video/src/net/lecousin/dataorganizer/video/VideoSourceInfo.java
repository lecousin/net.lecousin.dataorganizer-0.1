package net.lecousin.dataorganizer.video;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.lecousin.dataorganizer.core.database.info.SourceInfo;
import net.lecousin.dataorganizer.core.database.info.Info.DataLink;
import net.lecousin.framework.Pair;
import net.lecousin.framework.application.Application;
import net.lecousin.framework.collections.SelfMap;
import net.lecousin.framework.collections.SelfMapLinkedList;
import net.lecousin.framework.xml.XmlWriter;

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
	
	/** date de sortie */
	private long releaseDate;
	/** genres */
	private List<Genre> genre = new LinkedList<Genre>();
	/** resume */
	private String resume = null;
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
	
	/** press reviews */
	private SelfMap<String,Review> pressReviews = new SelfMapLinkedList<String,Review>(5);
	/** public reviews */
	private SelfMap<String,Review> publicReviews = new SelfMapLinkedList<String,Review>(5);
	
	public long getReleaseDate() { return releaseDate; }
	public List<Genre> getGenres() { return new ArrayList<Genre>(genre); }
	public String getResume() { return resume; }
	
	public List<Pair<List<String>,List<DataLink>>> getDirectors() { return new ArrayList<Pair<List<String>,List<DataLink>>>(directors); }
	public List<Pair<List<String>,List<DataLink>>> getActors() { return new ArrayList<Pair<List<String>,List<DataLink>>>(actors); }
	public List<Pair<List<String>,List<DataLink>>> getProductors() { return new ArrayList<Pair<List<String>,List<DataLink>>>(productors); }
	public List<Pair<List<String>,List<DataLink>>> getScenaristes() { return new ArrayList<Pair<List<String>,List<DataLink>>>(scenaristes); }
	
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
	
	public void setDirectors(List<Pair<String,DataLink>> list) {
		mergeNewLinks(directors, list);
	}
	public void setActors(List<Pair<String,DataLink>> list) {
		mergeNewLinks(actors, list);
	}
	public void setProductors(List<Pair<String,DataLink>> list) {
		mergeNewLinks(productors, list);
	}
	public void setScenaristes(List<Pair<String,DataLink>> list) {
		mergeNewLinks(scenaristes, list);
	}
	
	public void addPoster(String url, String localPath) {
		posters.put(url, localPath);
		if (getParent() != null)
			((VideoDataType)getParent().getDataContent()).signalNewPoster();
		signalModification();
	}
	
	public void setPublicReview(String author, String review, Integer note) {
		setReview(publicReviews, author, review, note);
	}
	public void setPressReview(String author, String review, Integer note) {
		setReview(pressReviews, author, review, note);
	}
	
	private void mergeNewLinks(List<Pair<List<String>,List<DataLink>>> currentList, List<Pair<String,DataLink>> newList) {
		boolean changed = false;
		for (Pair<String,DataLink> pNew : newList) {
			String newName = pNew.getValue1();
			DataLink newLink = pNew.getValue2();
			// looking for the same link
			boolean linkFound = false;
			for (Pair<List<String>,List<DataLink>> pOld : currentList) {
				for (DataLink oldLink : pOld.getValue2()) {
					if (!oldLink.isSame(newLink)) continue;
					linkFound = true;
					changed |= oldLink.merge(newLink);
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
	private void mergeTwoLists(List<Pair<List<String>,List<DataLink>>> currentList, List<Pair<List<String>,List<DataLink>>> newList) {
		boolean changed = false;
		for (Pair<List<String>,List<DataLink>> pNew : newList) {
			List<String> newNames = pNew.getValue1();
			List<DataLink> newLinks = pNew.getValue2();
			// looking for the same link
			Pair<List<String>,List<DataLink>> linkFound = null;
			for (DataLink newLink : newLinks) {
				for (Pair<List<String>,List<DataLink>> pOld : currentList) {
					for (DataLink oldLink : pOld.getValue2()) {
						if (!oldLink.isSame(newLink)) continue;
						linkFound = pOld;
						break;
					}
					if (linkFound != null) break;
				}
				if (linkFound != null) break;
			}
			if (linkFound == null) {
				// looking for the same name in a link
				for (String newName : newNames) {
					for (Pair<List<String>,List<DataLink>> pOld : currentList) {
						for (DataLink oldLink : pOld.getValue2()) {
							if (oldLink.name == null) continue;
							if (!oldLink.name.equals(newName)) continue;
							linkFound = pOld;
							break;
						}
						if (linkFound != null) break;
					}
					if (linkFound != null) break;
				}
			}
			if (linkFound != null) {
				// we found a link => merge
				List<String> oldNames = linkFound.getValue1();
				List<DataLink> oldLinks = linkFound.getValue2();
				for (String newName : newNames)
					if (!oldNames.contains(newName)) {
						oldNames.add(newName);
						changed = true;
					}
				for (DataLink newLink : newLinks) {
					boolean found = false;
					for (DataLink oldLink : oldLinks) {
						if (oldLink.isSame(newLink)) {
							found = true;
							changed |= oldLink.merge(newLink);
							break;
						}
					}
					if (!found) {
						oldLinks.add(newLink);
						changed = true;
					}
				}
				continue;
			}
			// neither link or same people name found, we cannot assume this is the same people => add it
			currentList.add(new Pair<List<String>,List<DataLink>>(newNames, newLinks));
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
	
	@Override
	public void merge(SourceInfo info) {
		VideoSourceInfo i = (VideoSourceInfo)info;
		if (releaseDate <= 0) setReleaseDate(i.getReleaseDate());
		for (Genre g : i.getGenres()) addGenre(g);
		if (resume == null && i.getResume() != null) setResume(i.getResume());
		mergeTwoLists(directors, i.getDirectors());
		mergeTwoLists(actors, i.getActors());
		mergeTwoLists(productors, i.getProductors());
		mergeTwoLists(scenaristes, i.getScenaristes());
		for (String url : i.posters.keySet())
			addPoster(url, i.posters.get(url));
		merge(pressReviews, i.pressReviews);
		merge(publicReviews, i.publicReviews);
	}
}
