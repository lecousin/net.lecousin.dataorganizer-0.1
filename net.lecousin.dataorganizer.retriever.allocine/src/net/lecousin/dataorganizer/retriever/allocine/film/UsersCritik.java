package net.lecousin.dataorganizer.retriever.allocine.film;

import net.lecousin.dataorganizer.retriever.allocine.AlloCinePage;
import net.lecousin.dataorganizer.retriever.allocine.Local;
import net.lecousin.dataorganizer.video.VideoSourceInfo;
import net.lecousin.framework.Pair;
import net.lecousin.framework.application.Application;
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
		return Local.Public_reviews.toString();
	}
	
	@Override
	protected String firstPageToReload(String page, String url) {
		return null;
	}
	
	public enum STR {
		Header("<b>Public reviews</b>", "<b>Critiques spectateurs</b>"),
		Footer("<b>You can also consult the reviews", "<b>Vous pouvez aussi consulter les critiques"),
		EndOfSectionHeader("</td></tr>", "critiques postées"),
		EndOfSectionBody("src=","<script"),
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
	}	

	@Override
	protected Pair<String,Boolean> parse(String page, String pageURL, VideoSourceInfo info, WorkProgress progress, int work) {
		int pageIndex = 1;
		String nextURL = null;
		int i = pageURL.indexOf("&page=");
		if (i > 0) {
			int j = pageURL.indexOf(".html", i);
			if (j > 0) {
				try { pageIndex = Integer.parseInt(pageURL.substring(i+6,j)); }
				catch (NumberFormatException e) {}
			}
		} else
			i = pageURL.indexOf(".html");
		if (i > 0) {
			String url = pageURL.substring(0, i) + "&page=" + (pageIndex+1) + ".html";
			if (page.indexOf(url) > 0)
				nextURL = url;
		}
		int nbPages = -1;
		if (pageIndex > 1) {
			int j = 0;
			while ((j = page.indexOf(pageURL.substring(0,i)+"&page=", j)) > 0) {
				int k = page.indexOf(".html", j);
				if (k > 0) {
					int num = -1;
					try { num = Integer.parseInt(page.substring(j+i+6, k)); }
					catch (NumberFormatException e){}
					if (num > 0 && num > nbPages)
						nbPages = num;
				}
				j++;
			}
		}
		if (nbPages < pageIndex) nbPages = -1;
		progress.setSubDescription(getDescription()+" ("+Local.Page+" " + pageIndex+(nbPages>0?"/"+nbPages:"")+")");
		
		i = page.indexOf(STR.Header.toString());
		if (i < 0) { progress.progress(work); return new Pair<String,Boolean>(null, false); }
		int j = page.indexOf(STR.Footer.toString(), i);
		if (j < 0) { progress.progress(work); return new Pair<String,Boolean>(null, false); }
		String section = page.substring(i, j);
		
		i = 0;
		do {
			Pair<String,Integer> header = getSection(section, "<td colspan=\"3\" valign=\"top\">", STR.EndOfSectionHeader.toString(), i);
			if (header == null) break;
			Pair<String, Integer> body = getSection(section, "<td colspan=\"3\" valign=\"top\">", STR.EndOfSectionBody.toString(), header.getValue2());
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
		
		progress.progress(work);
		if (i == 0) return new Pair<String,Boolean>(null, true); // no review
		return new Pair<String,Boolean>(nextURL, true);
	}
	
}
