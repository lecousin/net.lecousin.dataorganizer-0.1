package net.lecousin.dataorganizer.retriever.imdb.title;

import net.lecousin.dataorganizer.retriever.imdb.IMDBPage;
import net.lecousin.dataorganizer.retriever.imdb.Local;
import net.lecousin.dataorganizer.video.VideoSourceInfo;
import net.lecousin.framework.Pair;
import net.lecousin.framework.progress.WorkProgress;

public class Synopsis extends IMDBPage<VideoSourceInfo> {

	@Override
	protected String getCategory() {
		return "title";
	}
	@Override
	protected String getPage() {
		return "/plotsummary";
	}

	@Override
	protected String getDescription() {
		return Local.Synopsis.toString();
	}
	
	@Override
	protected String firstPageToReload(String page, String pageURL) {
		return null;
	}

	@Override
	protected Pair<String,Boolean> parse(String page, String pageURL, VideoSourceInfo info, WorkProgress progress, int work) {
		int i = page.indexOf("<p class=\"plotpar\">");
		if (i < 0) { progress.progress(work); return new Pair<String,Boolean>(null, false); }
		int j = page.indexOf("<hr/>", i);
		if (j < 0) { progress.progress(work); return new Pair<String,Boolean>(null, false); }
		String resume = cleanInfo(page.substring(i, j), true);
		info.setResume(resume);
		
		progress.progress(work);
		return new Pair<String,Boolean>(null, true);
	}
	
}
