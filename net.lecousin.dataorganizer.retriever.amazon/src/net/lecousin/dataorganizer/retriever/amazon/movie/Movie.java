package net.lecousin.dataorganizer.retriever.amazon.movie;

import net.lecousin.dataorganizer.retriever.amazon.AmazonPage;
import net.lecousin.dataorganizer.retriever.amazon.Local;
import net.lecousin.dataorganizer.retriever.amazon.MainPage;
import net.lecousin.dataorganizer.video.VideoSourceInfo;
import net.lecousin.framework.Pair;
import net.lecousin.framework.progress.WorkProgress;

public class Movie extends AmazonPage<VideoSourceInfo> {

	@Override
	protected String getPage() {
		return "dp";
	}
	
	@Override
	protected String getDescription() {
		return Local.MovieInformation.toString();
	}
	
	@Override
	protected String firstPageToReload(String page, String pageURL) {
		return null;
	}
	@Override
	protected Pair<String,Boolean> parse(String page, String pageURL, VideoSourceInfo info, WorkProgress progress, int work) {
		String s = MainPage.getDescription(page);
		if (s != null) info.setResume(s);
		progress.progress(work);
		return new Pair<String,Boolean>(null, true);
	}
	
}
