package net.lecousin.dataorganizer.retriever.cinefil.star;

import net.lecousin.dataorganizer.people.PeopleSourceInfo;
import net.lecousin.dataorganizer.retriever.cinefil.CineFilPage;
import net.lecousin.dataorganizer.retriever.cinefil.Local;
import net.lecousin.framework.Pair;
import net.lecousin.framework.progress.WorkProgress;

public class Biographie extends CineFilPage<PeopleSourceInfo> {

	@Override
	protected String getCategory() {
		return "star";
	}
	@Override
	protected String getPage() {
		return "/biographie";
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
		int i = page.indexOf("<h2>Biographie</h2>");
		if (i < 0) { progress.progress(work); return new Pair<String,Boolean>(null, false); }
		Pair<String,Integer> p = getSection(page, "<p>", "</p>", i);
		if (p == null) { progress.progress(work); return new Pair<String,Boolean>(null, false); }
		String bio = cleanInfo(p.getValue1(), true);
		info.setDescription(bio);
		
		progress.progress(work);
		return new Pair<String,Boolean>(null, true);
	}
}
