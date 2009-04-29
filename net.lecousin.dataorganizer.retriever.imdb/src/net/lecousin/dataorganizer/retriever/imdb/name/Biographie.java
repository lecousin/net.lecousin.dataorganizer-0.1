package net.lecousin.dataorganizer.retriever.imdb.name;

import net.lecousin.dataorganizer.people.PeopleSourceInfo;
import net.lecousin.dataorganizer.retriever.imdb.IMDBPage;
import net.lecousin.dataorganizer.retriever.imdb.Local;
import net.lecousin.framework.Pair;
import net.lecousin.framework.progress.WorkProgress;

public class Biographie extends IMDBPage<PeopleSourceInfo> {

	@Override
	protected String getCategory() {
		return "name";
	}
	@Override
	protected String getPage() {
		return "/bio";
	}

	@Override
	protected String getDescription() {
		return Local.Biographie.toString();
	}
	
	@Override
	protected String firstPageToReload(String page, String pageURL) {
		return null;
	}

	@Override
	protected Pair<String,Boolean> parse(String page, String pageURL, PeopleSourceInfo info, WorkProgress progress, int work) {
		int i = page.indexOf("<h5>Mini Biography</h5>");
		if (i < 0) { progress.progress(work); return new Pair<String,Boolean>(null, false); }
		Pair<String,Integer> p = getSection(page, "<p>", "</p>", i);
		if (p == null) { progress.progress(work); return new Pair<String,Boolean>(null, false); }
		String bio = cleanInfo(p.getValue1(), true);
		info.setDescription(bio);
		
		progress.progress(work);
		return new Pair<String,Boolean>(null, true);
	}
}
