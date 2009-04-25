package net.lecousin.dataorganizer.retrieve.cinefil.film;

import net.lecousin.dataorganizer.retrieve.cinefil.CineFilPage;
import net.lecousin.dataorganizer.retrieve.cinefil.Local;
import net.lecousin.dataorganizer.video.VideoSourceInfo;
import net.lecousin.framework.Pair;
import net.lecousin.framework.Triple;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.xml.XmlParsingUtil;
import net.lecousin.framework.xml.XmlParsingUtil.Node;

public class PressCritik extends CineFilPage<VideoSourceInfo> {

	@Override
	protected String getCategory() {
		return "film";
	}
	@Override
	protected String getPage() {
		return "/critiques-presse";
	}
	
	@Override
	protected String getDescription() {
		return Local.Press_reviews.toString();
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
		
		i = 0;
		do {
			int j = page.indexOf("<hr><a name=", i);
			if (j < 0) break;
			int k = page.indexOf("<hr>", j+4);
			if (k < 0) break;
			String section = page.substring(j, k);
			i = k-4;
			
			j = section.indexOf("<div id=\"Divers_profil_h4\">");
			if (j < 0) continue;
			k = section.indexOf("<br>", j);
			if (k < 0) continue;
			String author = cleanInfo(section.substring(j+27, k).replace("</div>", ""), true);
			
			int kk = j;
			Integer note = null;
			j = section.indexOf("<div class=\"avis\"><div class=\"c", k);
			if (j > 0) {
				k = section.indexOf("0T\"", j);
				if (k > 0) {
					try { note = (Integer.parseInt(section.substring(j+31,k))-1)*4; }
					catch (NumberFormatException e){}
				}
			}
			
			j = section.indexOf("</div></div></div>", kk);
			k = section.indexOf("<div", j);
			if (k < 0) k = section.length();
			String review = cleanInfo(section.substring(j+18,k), true);
			
			info.setPressReview(author, review, note);
		} while (true);
		
		progress.progress(work);
		return new Pair<String,Boolean>(nextURL, true);
	}
	
}
