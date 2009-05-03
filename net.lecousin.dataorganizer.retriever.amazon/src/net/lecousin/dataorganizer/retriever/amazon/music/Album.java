package net.lecousin.dataorganizer.retriever.amazon.music;

import net.lecousin.dataorganizer.audio.AudioSourceInfo;
import net.lecousin.dataorganizer.retriever.amazon.AmazonPage;
import net.lecousin.dataorganizer.retriever.amazon.Local;
import net.lecousin.dataorganizer.retriever.amazon.MainPage;
import net.lecousin.framework.Pair;
import net.lecousin.framework.progress.WorkProgress;

public class Album extends AmazonPage<AudioSourceInfo> {

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
	protected Pair<String,Boolean> parse(String page, String pageURL, AudioSourceInfo info, WorkProgress progress, int work) {
		String s = MainPage.getDescription(page);
		if (s != null) info.setDescription(s);
		progress.progress(work);
		return new Pair<String,Boolean>(null, true);
	}
	
}
