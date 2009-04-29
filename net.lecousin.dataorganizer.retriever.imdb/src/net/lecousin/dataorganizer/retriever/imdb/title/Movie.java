package net.lecousin.dataorganizer.retriever.imdb.title;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import net.lecousin.dataorganizer.retriever.imdb.IMDBPage;
import net.lecousin.dataorganizer.retriever.imdb.IMDBUtil;
import net.lecousin.dataorganizer.retriever.imdb.Local;
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

public class Movie extends IMDBPage<VideoSourceInfo> {

	@Override
	protected String getCategory() {
		return "title";
	}
	@Override
	protected String getPage() {
		return "/";
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
		int nb = 3;
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
		List<Pair<String,String>> genres = getInfoLinkedList(page, "<h5>Genre:</h5>", "</div>");
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
		Pair<String,String> poster = getPoster(page, info);
		if (poster != null)
			info.addPoster(poster.getValue1(), poster.getValue2());
		progress.progress(step);
		
		return new Pair<String,Boolean>(null, success);
	}
	
	private Genre decodeGenre(Pair<String,String> p) {
		String id = p.getValue2();
		if (!id.startsWith("/Sections/Genres/")) return null;
		id = id.substring(17);
		int i = id.indexOf('/');
		if (i > 0)
			id = id.substring(0, i);
	    if (id.equals("Action")) return Genre.Action;
	    if (id.equals("Adult")) return Genre.Porno;
	    if (id.equals("Adventure")) return Genre.Aventure;
	    if (id.equals("Animation")) return Genre.Animation;
	    if (id.equals("Biography")) return Genre.Biographie;
	    if (id.equals("Comedy")) return Genre.Comedie;
	    if (id.equals("Crime")) return Genre.Policier;
	    if (id.equals("Documentary")) return Genre.Documentaire;
	    if (id.equals("Drama")) return Genre.Drame;
	    if (id.equals("Family")) return Genre.Famille;
	    if (id.equals("Fantasy")) return null; // TODO
	    if (id.equals("Film-Noir")) return Genre.Noir;
	    if (id.equals("Game-Show")) return null; // TODO
	    if (id.equals("History")) return Genre.Historique;
	    if (id.equals("Horror")) return Genre.HorreurEpouvante;
	    if (id.equals("Music")) return null; // TODO
	    if (id.equals("Musical")) return null; // TODO
	    if (id.equals("Mystery")) return null; // TODO
	    if (id.equals("News")) return null; // TODO
	    if (id.equals("Reality-TV")) return null; // TODO
	    if (id.equals("Romance")) return Genre.Romance;
	    if (id.equals("Sci-Fi")) return Genre.ScienceFiction;
	    if (id.equals("Short")) return Genre.CourtMetrage;
	    if (id.equals("Sport")) return null; // TODO
	    if (id.equals("Talk-Show")) return null; // TODO
	    if (id.equals("Thriller")) return Genre.Thriller;
	    if (id.equals("War")) return Genre.Guerre;
	    if (id.equals("Western")) return Genre.Western;
		if (Log.info(this))
			Log.info(this, "Unknown genre id " + id + " (" + p.getValue1() + ")");
		return null;
	}
	
	private long getReleaseDate(String page) {
		int i = page.indexOf("<h5>Release Date:</h5>");
		if (i < 0) return 0;
		int j = page.indexOf("<a ", i);
		if (j < 0) return 0;
		String date = page.substring(i, j);
		i = date.indexOf("(");
		if (i > 0) date = date.substring(0, i);
		date = date.replace("\n", "").replace("\r", "").trim();
		DateFormat format = new SimpleDateFormat("dd MMMM yyyy");
		try { 
			Date d = format.parse(date);
			return d.getTime();
		} catch (ParseException e) {
			return 0;
		}
	}
	
	private Pair<String,String> getPoster(String page, VideoSourceInfo info) {
		int i = page.indexOf("<a name=\"poster\" ");
		if (i < 0) return null;
		Triple<Node,Boolean,Integer> t = XmlParsingUtil.parseOpenNode(page, i);
		if (t.getValue1() == null) return null;
		String url = t.getValue1().attributes.get("href");
		
		String page2 = loadPage(url);
		if (page2 == null) return null;
		i = page2.indexOf("<center><table id=\"principal\">");
		if (i < 0) return null;
		i = page2.indexOf("<img ", i);
		if (i < 0) return null;
		
		t = XmlParsingUtil.parseOpenNode(page2, i);
		if (t.getValue1() == null) return null;
		url = t.getValue1().attributes.get("src");
		if (url.indexOf("://") < 0) url = "http://"+IMDBUtil.getHost()+url;
		if (info.hasPosterURL(url)) return null;
		try { 
			IFileStore netFile = EFS.getStore(new URI(url));
			IFile file;
			IFolder f = info.getFolder();
			f = f.getFolder("imdb");
			i = 0;
			while ((file = f.getFile("poster"+i+"."+FileSystemUtil.getFileNameExtension(netFile.getName()))).exists()) i++;
			ResourceUtil.createFolderAndParents(f);
			InputStream in = netFile.openInputStream(EFS.NONE, null);
			file.create(in, true, null);
			return new Pair<String,String>(url, "imdb/"+file.getName());
		} catch (URISyntaxException e) {
			return null;
		} catch (CoreException e) {
			return null;
		}
	}
}
