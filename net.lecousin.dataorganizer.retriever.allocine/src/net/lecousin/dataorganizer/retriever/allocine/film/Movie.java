package net.lecousin.dataorganizer.retriever.allocine.film;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import net.lecousin.dataorganizer.retriever.allocine.AlloCinePage;
import net.lecousin.dataorganizer.retriever.allocine.AlloCineUtil;
import net.lecousin.dataorganizer.retriever.allocine.Local;
import net.lecousin.dataorganizer.video.VideoSourceInfo;
import net.lecousin.dataorganizer.video.VideoSourceInfo.Genre;
import net.lecousin.framework.Pair;
import net.lecousin.framework.application.Application;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.progress.WorkProgress;

public class Movie extends AlloCinePage<VideoSourceInfo> {

	@Override
	protected String getCategory() {
		return "film";
	}
	@Override
	protected String getPage() {
		return "fichefilm";
	}
	
	@Override
	protected String getDescription() {
		return Local.Movie_information.toString();
	}
	
	@Override
	protected String firstPageToReload(String page, String pageURL) {
		return null;
	}
	public enum STR {
		Genre("Genre:", "Genre :"),
		ReleaseDate("Release date", "Date de sortie"),
		DateFormat("MMMM dd, yyyy", "dd MMMM yyyy"),
		Resume("Synopsis", "Synopsis"),
		;
		private STR(String english, String french) {
			this.english = english;
			this.french = french;
		}
		private String english;
		private String french;
		@Override
		public java.lang.String toString() {
			switch (Application.language) {
			case FRENCH: return french;
			default: return english;
			}
		}
	}	@Override
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
		List<Pair<String,String>> genres = getInfoLinkedList(page, STR.Genre.toString(), "</div>");
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
		
		return new Pair<String,Boolean>(null, success);
	}
	
	private Genre decodeGenre(Pair<String,String> p) {
		String id = AlloCineUtil.getIDFromURL(p.getValue2());
		if (id.equals("13001")) return Genre.Aventure;
		if (id.equals("13002")) return Genre.ComedieDramatique;
		if (id.equals("13004")) return Genre.CourtMetrage;
		if (id.equals("13005")) return Genre.Comedie;
		if (id.equals("13006")) return Genre.DessinAnime;
		if (id.equals("13007")) return Genre.Documentaire;
		if (id.equals("13008")) return Genre.Drame;
		if (id.equals("13009")) return Genre.HorreurEpouvante;
		if (id.equals("13010")) return Genre.Erotique;
		if (id.equals("13012")) return Genre.Fantastique;
		if (id.equals("13013")) return Genre.ComedieMusicale;
		if (id.equals("13014")) return Genre.Guerre;
		if (id.equals("13015")) return Genre.Historique;
		if (id.equals("13016")) return Genre.ArtsMartiaux;
		if (id.equals("13017")) return null; // non reference
		if (id.equals("13018")) return Genre.Policier;
		if (id.equals("13019")) return Genre.Western;
		if (id.equals("13020")) return null; // divers
		if (id.equals("13021")) return Genre.ScienceFiction;
		if (id.equals("13022")) return Genre.Espionnage;
		if (id.equals("13023")) return Genre.Thriller;
		if (id.equals("13024")) return Genre.Romance;
		if (id.equals("13025")) return Genre.Action;
		if (id.equals("13026")) return Genre.Animation;
		if (id.equals("13027")) return Genre.Biographie;
		if (id.equals("13033")) return Genre.Experimental;
		if (id.equals("13035")) return null; // Programme ???
		if (id.equals("13036")) return Genre.Famille;
		if (Log.info(this))
			Log.info(this, "Unknown genre id " + id + " (" + p.getValue1() + ")");
		return null;
	}
	
	private long getReleaseDate(String page) {
		String str = getInfoBoldFrom(page, STR.ReleaseDate.toString());
		if (str != null) str = str.trim();
		if (str == null || str.length() == 0) return 0;
		DateFormat format = new SimpleDateFormat(STR.DateFormat.toString());
		try { 
			Date date = format.parse(str);
			return date.getTime();
		} catch (ParseException e) {
			return 0;
		}
	}
	/*
	protected List<PagePeople> getPeoples(String page, String title) {
		List<PagePeople> list = new LinkedList<PagePeople>();
		int i = page.indexOf("<h4>" + title);
		if (i < 0) return list;
		int j = page.indexOf("</h4>", i);
		if (j < 0) return list;
		int end = i;
		do {
			int start = page.indexOf("<a", end);
			end = page.indexOf("</a>", start);
			if (start < 0 || end < 0 || start > j || end > j) break;
			int hrefStart = page.indexOf("href=\"", start);
			if (hrefStart < 0) break;
			int hrefEnd = page.indexOf("\"", hrefStart+6);
			if (hrefEnd < 0) break;
			int linkEnd = page.indexOf('>', hrefEnd);
			if (linkEnd < 0) break;
			String url = page.substring(hrefStart+6, hrefEnd);
			String name = page.substring(linkEnd+1, end);
			list.add(new PagePeople(name, url));
		} while (end + 4 < j);
		return list;
	}
	
	protected class PagePeople {
		public PagePeople(String name, String url) {
			this.name = name;
			this.id = AlloCineUtil.getIDFromURL(url);
		}
		String name, id;
	}*/
	
	private String getResume(String page) {
		int i = page.indexOf(STR.Resume.toString());
		if (i < 0) return "";
		int start = page.indexOf("<h4>", i);
		if (start < 0) return "";
		int end = page.indexOf("</h4>", start);
		if (end < 0) return "";
		return page.substring(start+4, end);
	}
}
