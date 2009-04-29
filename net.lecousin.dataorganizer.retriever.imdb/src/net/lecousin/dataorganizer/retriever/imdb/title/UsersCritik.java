package net.lecousin.dataorganizer.retriever.imdb.title;

import net.lecousin.dataorganizer.retriever.imdb.IMDBPage;
import net.lecousin.dataorganizer.retriever.imdb.Local;
import net.lecousin.dataorganizer.video.VideoSourceInfo;
import net.lecousin.framework.Pair;
import net.lecousin.framework.Triple;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.xml.XmlParsingUtil;
import net.lecousin.framework.xml.XmlParsingUtil.Node;

public class UsersCritik extends IMDBPage<VideoSourceInfo> {

	@Override
	protected String getCategory() {
		return "title";
	}
	@Override
	protected String getPage() {
		return "/usercomments";
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
		int i = pageURL.indexOf("&start=");
		if (i > 0) {
			try { pageIndex = (Integer.parseInt(pageURL.substring(i+7))/10)+1; }
			catch (NumberFormatException e) {}
		}
		i = page.indexOf("&start="+(pageIndex*10));
		if (i > 0) {
			nextURL = "/"+getCategory()+getPage()+"?start="+(pageIndex*10);
		}

		progress.setSubDescription(getDescription()+" ("+Local.Page+" " + pageIndex+")");
		
		i = page.indexOf("comments in total");
		if (i < 0) { progress.progress(work); return new Pair<String,Boolean>(null, false); }
		
		do {
			int j = page.indexOf("<p>", i);
			if (j < 0) break;
			int k = page.indexOf("</p>", j);
			if (k < 0) break;
			String header = page.substring(j, k);
			i = k;
			j = page.indexOf("<p>", i);
			if (j < 0) break;
			k = page.indexOf("</p>", j);
			if (k < 0) break;
			String body = page.substring(j+3, k);
			i = k;

			// Header
			// + Title
			Pair<String,Integer> p = getSection(header, "<b>", "</b>", 0);
			if (p == null) continue;
			String title = p.getValue1();
			j = header.indexOf("<img", p.getValue2());
			if (j < 0) continue;
			// + Note
			Integer note = null;
			Triple<Node,Boolean,Integer> t = XmlParsingUtil.parseOpenNode(header, j);
			if (t.getValue1() != null) {
				String s = t.getValue1().attributes.get("alt");
				if (s != null) {
					j = s.indexOf("/10");
					if (j > 0) {
						try { note = Integer.parseInt(s.substring(0,j))*2; }
						catch (NumberFormatException e) {}
					}
				}
				j = t.getValue3();
			}
			// + Author
			j = header.indexOf("<a href=\"/user/", j);
			if (j < 0) continue;
			Pair<Pair<String,String>,Integer> pl = getInfoLinked(header, j);
			if (pl == null) continue;
			String author = pl.getValue1().getValue1();
			
			body = "<b>"+title+"</b><br/>"+cleanInfo(body, true);
			
			info.setPublicReview(author, body, note);
		} while (true);
		
		progress.progress(work);
		return new Pair<String,Boolean>(nextURL, true);
	}
	
}
