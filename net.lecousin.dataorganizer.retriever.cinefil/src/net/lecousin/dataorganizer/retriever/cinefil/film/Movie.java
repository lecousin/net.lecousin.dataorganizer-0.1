package net.lecousin.dataorganizer.retriever.cinefil.film;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import net.lecousin.dataorganizer.retriever.cinefil.CineFilPage;
import net.lecousin.dataorganizer.retriever.cinefil.CineFilUtil;
import net.lecousin.dataorganizer.retriever.cinefil.Local;
import net.lecousin.dataorganizer.video.VideoSourceInfo;
import net.lecousin.dataorganizer.video.VideoSourceInfo.Genre;
import net.lecousin.framework.Pair;
import net.lecousin.framework.Triple;
import net.lecousin.framework.eclipse.resource.ResourceUtil;
import net.lecousin.framework.io.FileSystemUtil;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.xml.XmlParsingUtil;
import net.lecousin.framework.xml.XmlParsingUtil.Node;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;

public class Movie extends CineFilPage<VideoSourceInfo> {

	@Override
	protected String getCategory() {
		return "film";
	}
	@Override
	protected String getPage() {
		return "";
	}
	
	@Override
	protected String getDescription() {
		return Local.Movie_information.toString();
	}
	
	@Override
	protected String firstPageToReload(String page, String pageURL) {
		return null;
	}
	protected Pair<String,Boolean> parse(String page, String pageURL, VideoSourceInfo info, WorkProgress progress, int work) {
		int nb = 4;
		int step;
		boolean success = false;
		
		step = work/nb--;
		work -= step;
		long date = getReleaseDate(page);
		if (date > 0) {
			info.setReleaseDate(date);
			success = true;
		}
		progress.progress(step);
		
		step = work/nb--;
		work -= step;
		List<Pair<String,String>> genres = getInfoLinkedList(page, "Genre :", "<br>");
		if (genres != null) {
			success = true;
			for (Pair<String,String> p : genres) {
				Genre g = decodeGenre(p);
				if (g != null)
					info.addGenre(g);
			}
		}
		progress.progress(step);

		step = work/nb--;
		work -= step;
		String resume = getResume(page);
		if (resume != null) {
			info.setResume(resume);
			success = true;
		}
		progress.progress(step);
		
		step = work/nb--;
		work -= step;
		Pair<String,String> poster = getPoster(page, info);
		if (poster != null)
			info.addPoster(poster.getValue1(), poster.getValue2());
		progress.progress(step);
		
		return new Pair<String,Boolean>(null, success);
	}
	
	private Genre decodeGenre(Pair<String,String> p) {
		String id = CineFilUtil.getIDFromURL(p.getValue2());
		if (id.equals("action")) return Genre.Action;
		if (id.equals("arts-martiaux")) return Genre.ArtsMartiaux;
		if (id.equals("aventure")) return Genre.Aventure;
		if (id.equals("biographie")) return Genre.Biographie;
		if (id.equals("catastrophe")) return Genre.Catastrophe;
		if (id.equals("comedie")) return Genre.Comedie;
		if (id.equals("comedie-dramatique")) return Genre.ComedieDramatique;
		if (id.equals("comedie-romantique")) return Genre.ComedieRomantique;
		if (id.equals("conference-filmee")) return Genre.ConferenceFilmee;
		if (id.equals("court-metrage")) return Genre.CourtMetrage;
		if (id.equals("dessins-animes")) return Genre.DessinAnime;
		if (id.equals("dessins-animes-adultes")) return Genre.DessinAnimeAdulte;
		if (id.equals("documentaire")) return Genre.Documentaire;
		if (id.equals("drame")) return Genre.Drame;
		if (id.equals("drame-psychologique")) return Genre.DramePsychologique;
		if (id.equals("enfants")) return Genre.Famille;
		if (id.equals("epouvante")) return Genre.HorreurEpouvante;
		if (id.equals("erotique")) return Genre.Erotique;
		if (id.equals("espionnage")) return Genre.Espionnage;
		if (id.equals("essai")) return Genre.Experimental;
		if (id.equals("fantastique")) return Genre.Fantastique;
		if (id.equals("film-d-animation")) return Genre.Animation;
		if (id.equals("film-noir")) return Genre.Noir;
		if (id.equals("grand-spectacle")) return Genre.GrandSpectacle;
		if (id.equals("guerre")) return Genre.Guerre;
		if (id.equals("historique")) return Genre.Historique;
		if (id.equals("horreur")) return Genre.HorreurEpouvante;
		if (id.equals("musical")) return Genre.ComedieMusicale;
		if (id.equals("peplum")) return null;
		if (id.equals("policier")) return Genre.Policier;
		if (id.equals("politique")) return Genre.Politique;
		if (id.equals("pornographique")) return Genre.Porno;
		if (id.equals("romance")) return Genre.Romance;
		if (id.equals("science-fiction")) return Genre.ScienceFiction;
		if (id.equals("thriller")) return Genre.Thriller;
		if (id.equals("western")) return Genre.Western;
		if (Log.info(this))
			Log.info(this, "Unknown genre id " + id + " (" + p.getValue1() + ")");
		return null;
	}
	
	private long getReleaseDate(String page) {
		int i = page.indexOf("Date de sortie : ");
		if (i < 0) return 0;
		int j = page.indexOf("<br>", i);
		if (j < 0) return 0;
		String date = page.substring(i, j);
		i = date.indexOf("\">");
		if (i < 0) return 0;
		j = date.indexOf("</a>");
		if (j < 0) return 0;
		date = date.substring(i+2, j);
		DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		try { 
			Date d = format.parse(date);
			return d.getTime();
		} catch (ParseException e) {
			return 0;
		}
	}
	
	private String getResume(String page) {
		Pair<String,Integer> section = getSection(page, "<h2>Synopsis</h2>", "</div>", 0);
		if (section == null) return "";
		return cleanInfo(section.getValue1(), true);
	}
	
	private Pair<String,String> getPoster(String page, VideoSourceInfo info) {
		Pair<String,Integer> p = getSection(page, "<div id=\"Corps_profil\">", "</div>", 0); 
		if (p == null) return null;
		p = getSection(p.getValue1(), "<td ", "</td>", 0);
		if (p == null) return null;
		int i = p.getValue1().indexOf("<img ");
		if (i < 0) return null;
		Triple<Node,Boolean,Integer> t = XmlParsingUtil.parseOpenNode(p.getValue1(), i);
		if (t.getValue1() == null) return null;
		String url = t.getValue1().attributes.get("src");
		if (url.indexOf("://") < 0) url = "http://"+CineFilUtil.getHost()+url;
		if (info.hasPosterURL(url)) return null;
		try { 
			IFileStore netFile = EFS.getStore(new URI(url));
			IFile file;
			IFolder f = info.getFolder();
			f = f.getFolder("cinefil");
			i = 0;
			while ((file = f.getFile("poster"+i+"."+FileSystemUtil.getFileNameExtension(netFile.getName()))).exists()) i++;
			ResourceUtil.createFolderAndParents(f);
			InputStream in = netFile.openInputStream(EFS.NONE, null);
			file.create(in, true, null);
			return new Pair<String,String>(url, "cinefil/"+file.getName());
		} catch (URISyntaxException e) {
			return null;
		} catch (CoreException e) {
			return null;
		}
	}
}
