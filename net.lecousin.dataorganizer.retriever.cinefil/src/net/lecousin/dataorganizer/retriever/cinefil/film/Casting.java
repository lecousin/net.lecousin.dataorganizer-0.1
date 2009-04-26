package net.lecousin.dataorganizer.retriever.cinefil.film;

import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.core.database.info.Info;
import net.lecousin.dataorganizer.people.PeopleContentType;
import net.lecousin.dataorganizer.retriever.cinefil.CineFilPage;
import net.lecousin.dataorganizer.retriever.cinefil.CineFilUtil;
import net.lecousin.dataorganizer.retriever.cinefil.Local;
import net.lecousin.dataorganizer.video.VideoSourceInfo;
import net.lecousin.framework.Pair;
import net.lecousin.framework.progress.WorkProgress;

public class Casting extends CineFilPage<VideoSourceInfo> {

	@Override
	protected String getCategory() {
		return "film";
	}
	@Override
	protected String getPage() {
		return "/casting";
	}

	@Override
	protected String getDescription() {
		return Local.Casting.toString();
	}
	
	@Override
	protected String firstPageToReload(String page, String pageURL) {
		return null;
	}

	@Override
	protected Pair<String,Boolean> parse(String page, String pageURL, VideoSourceInfo info, WorkProgress progress, int work) {
		int nb = 2;
		int step;
		List<Pair<String,Info.DataLink>> list;
		boolean success = false;
		
		step = work/nb--;
		work -= step;
		list = getList(page, "Réalisation");
		if (list != null) {
			info.setDirectors(list);
			success = true;
		}
		progress.progress(step);
		
		step = work/nb--;
		work -= step;
		list = getList(page, "Acteurs");
		if (list != null) {
			info.setActors(list);
			success = true;
		}
		progress.progress(step);
		
		return new Pair<String,Boolean>(null, success);
	}
	
	private List<Pair<String,Info.DataLink>> getList(String page, String sectionName) {
		Pair<String,Integer> p = getSection(page, "<h2>" + sectionName + "</h2>", "<script", 0);
		if (p == null) return null;
		int i = p.getValue1().indexOf("<h2>");
		if (i > 0)
			p.setValue1(p.getValue1().substring(0, i));
		List<Pair<String,Info.DataLink>> list = new LinkedList<Pair<String,Info.DataLink>>();
		String section = p.getValue1(); 
		i = 0;
		do {
			int j = section.indexOf("<br clear=\"left\"><br>", i);
			if (j < 0) break;
			String peopleSection = section.substring(i, j);
			i = j+21;

			p = getSection(peopleSection, "<h3>", "</h3>", 0);
			if (p == null) continue;
			Pair<Pair<String,String>,Integer> pl = getInfoLinked(p.getValue1(), 0);
			if (pl == null) continue;
			String name = pl.getValue1().getValue1();
			String url = pl.getValue1().getValue2();
			j = pl.getValue2();
			
			String role = "";
			int k = peopleSection.indexOf("Rôle dans ce film :", j);
			if (k > 0) {
				int l = peopleSection.indexOf("</li>", k);
				if (l > 0) {
					role = peopleSection.substring(k+19, l).trim();
				}
			}
			
			list.add(new Pair<String,Info.DataLink>(role, new Info.DataLink(PeopleContentType.PEOPLE_TYPE, CineFilUtil.SOURCE_ID, CineFilUtil.getIDFromURL(url), name)));
		} while (true);
		return list;
	}
	
}
