package net.lecousin.dataorganizer.retrieve.cinefil.film;

import net.lecousin.dataorganizer.retrieve.cinefil.CineFilPage;
import net.lecousin.dataorganizer.retrieve.cinefil.Local;
import net.lecousin.dataorganizer.video.VideoSourceInfo;
import net.lecousin.framework.Pair;
import net.lecousin.framework.Triple;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.xml.XmlParsingUtil;
import net.lecousin.framework.xml.XmlParsingUtil.Node;

public class UsersCritik extends CineFilPage<VideoSourceInfo> {

	@Override
	protected String getCategory() {
		return "film";
	}
	@Override
	protected String getPage() {
		return "/critiques";
	}
	
	@Override
	protected String getDescription() {
		return Local.Public_reviews.toString();
	}
	
	@Override
	protected String firstPageToReload(String page, String url) {
		return null;
	}
	
	@Override
	protected Pair<String,Boolean> parse(String page, String pageURL, VideoSourceInfo info, WorkProgress progress, int work) {
		int pageIndex = 1;
		String nextURL = null;
		int i = pageURL.indexOf("&page=");
		if (i > 0) {
			try { pageIndex = Integer.parseInt(pageURL.substring(i+6)); }
			catch (NumberFormatException e) {}
		}
		i = page.indexOf("&page="+(pageIndex+1));
		if (i > 0) {
			int j = page.lastIndexOf("<A ", i);
			Triple<Node,Boolean,Integer> t = XmlParsingUtil.parseOpenNode(page, j);
			if (t.getValue1() != null)
				nextURL = t.getValue1().attributes.get("href");
		}

		progress.setSubDescription(getDescription()+" ("+Local.Page+" " + pageIndex+")");
		
		i = page.indexOf("<h2>les avis des internautes");
		if (i < 0) { progress.progress(work); return new Pair<String,Boolean>(null, false); }
		int j = page.indexOf("<div id=\"Divers_profil\"><br>", i);
		if (j < 0) { progress.progress(work); return new Pair<String,Boolean>(null, false); }
		i = j;
		
		do {
			j = page.indexOf("<hr>", i);
			if (j < 0) break;
			if (!page.substring(i, i+4).equalsIgnoreCase("<h4>")) break;
			String section = page.substring(i+4, j);
			i = j+4;
			
			j = section.indexOf("</h4>");
			if (j < 0) break;
			String title = section.substring(0, j);
			
			String author;
			int k = section.indexOf("Déposé par", j);
			if (k > 0) {
				int k2 = section.indexOf("<a ", k);
				if (k2 > 0) {
					Triple<Node,Boolean,Integer> t = XmlParsingUtil.parseOpenNode(section, k2);
					int k3 = section.indexOf("</a>", t.getValue3());
					if (k3 > 0)
						author = cleanInfo(section.substring(t.getValue3(),k3), false).trim();
					else
						continue;
				} else
					continue;
			} else
				continue;

			Integer note = null;
			k = section.indexOf("<div class=\"avis\"><div class=\"c", j);
			if (k > 0) {
				int k2 = section.indexOf("0\"", k);
				if (k2 > 0) {
					try { note = (Integer.parseInt(section.substring(k+31,k2))-1)*4; }
					catch (NumberFormatException e){}
				}
			}
			
			k = section.indexOf("</span><br>", j);
			if (k < 0) continue;
			int k2 = section.indexOf("</p>", k);
			if (k2 < 0) continue;
			String review = "<b>"+title+"</b><br/>" + cleanInfo(section.substring(k+11,k2), true);
			
			info.setPublicReview(author, review, note);
		} while (true);
		
		progress.progress(work);
		return new Pair<String,Boolean>(nextURL, true);
	}
	
}
