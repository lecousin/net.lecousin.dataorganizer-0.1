package net.lecousin.dataorganizer.allocine.film;

import net.lecousin.dataorganizer.allocine.AlloCinePage;
import net.lecousin.dataorganizer.video.VideoSourceInfo;
import net.lecousin.framework.Pair;
import net.lecousin.framework.progress.WorkProgress;

public class UsersCritik extends AlloCinePage<VideoSourceInfo> {

	@Override
	protected String getCategory() {
		return "film";
	}
	@Override
	protected String getPage() {
		return "critiquepublic";
	}
	
	@Override
	protected String getDescription() {
		return "Public reviews";
	}
	
	@Override
	protected String firstPageToReload(String page, String url) {
		return null;
	}
	@Override
	protected Pair<String,Boolean> parse(String page, String pageURL, VideoSourceInfo info, WorkProgress progress, int work) {
		int i = page.indexOf("<b>Critiques Spectateurs</b>");
		if (i < 0) { progress.progress(work); return new Pair<String,Boolean>(null, false); }
		int j = page.indexOf("<b>Vous pouvez aussi consulter les critiques", i);
		if (j < 0) { progress.progress(work); return new Pair<String,Boolean>(null, false); }
		String section = page.substring(i, j);
		
		i = 0;
		do {
			Pair<String,Integer> header = getSection(section, "<td colspan=\"3\" valign=\"top\">", "critiques postées", i);
			if (header == null) break;
			Pair<String, Integer> body = getSection(section, "<td colspan=\"3\" valign=\"top\">", "<script", header.getValue2());
			if (body == null) break;
			i = body.getValue2();
			
			Pair<String,Integer> name = getSection(header.getValue1(), "<h4><b>", "</b>", 0);
			if (name == null) continue;
			Pair<String,Integer> note = getSection(header.getValue1(), "class=\"etoile_", "\"", name.getValue2());
			Integer noteI;
			if (note == null) 
				noteI = null;
			else
				try { noteI = Integer.parseInt(note.getValue1()) * 5; }
				catch (NumberFormatException e) { noteI = null; }
			
			Pair<String,Integer> critik = getSection(body.getValue1(), "<h4>", "<img ", 0);
			if (critik == null) continue;
			
			info.setPublicReview(name.getValue1(), critik.getValue1(), noteI);
		} while (true);
		
		if (i == 0) return new Pair<String,Boolean>(null, true); // no review
		int pageIndex = 1;
		i = pageURL.indexOf("&page=");
		if (i > 0) {
			j = pageURL.indexOf(".html", i);
			if (j > 0) {
				try { pageIndex = Integer.parseInt(pageURL.substring(i+6,j)); }
				catch (NumberFormatException e) {}
			}
		} else
			i = pageURL.indexOf(".html");
		if (i > 0) {
			String url = pageURL.substring(0, i) + "&page=" + (pageIndex+1) + ".html";
			if (page.indexOf(url) > 0)
				return new Pair<String,Boolean>(url, true);
		}
		return new Pair<String,Boolean>(null, true);
	}
	
}
