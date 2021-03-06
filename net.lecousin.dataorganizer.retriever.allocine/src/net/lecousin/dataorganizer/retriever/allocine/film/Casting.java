package net.lecousin.dataorganizer.retriever.allocine.film;

import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.core.database.info.Info;
import net.lecousin.dataorganizer.people.PeopleContentType;
import net.lecousin.dataorganizer.retriever.allocine.AlloCinePage;
import net.lecousin.dataorganizer.retriever.allocine.AlloCineUtil;
import net.lecousin.dataorganizer.retriever.allocine.Local;
import net.lecousin.dataorganizer.video.VideoSourceInfo;
import net.lecousin.framework.Pair;
import net.lecousin.framework.application.Application;
import net.lecousin.framework.progress.WorkProgress;

public class Casting extends AlloCinePage<VideoSourceInfo> {

	@Override
	protected String getCategory() {
		return "film";
	}
	@Override
	protected String getPage() {
		return "casting";
	}

	@Override
	protected String getDescription() {
		return Local.Casting.toString();
	}
	
	@Override
	protected String firstPageToReload(String page, String pageURL) {
		return null;
	}
	public enum STR {
		Directors("Director", "R�alisation"),
		Actors("Actor(s)", "Acteurs"),
		Producers("Producer", "Production"),
		Writers("Writer", "Sc�nario"),
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
		int nb = 4;
		int step;
		List<Pair<String,Info.DataLink>> list;
		boolean success = false;
		
		step = work/nb--;
		work -= step;
		list = getList(page, STR.Directors.toString());
		if (list != null) {
			info.setDirectors(list);
			success = true;
		}
		progress.progress(step);
		
		step = work/nb--;
		work -= step;
		list = getList(page, STR.Actors.toString());
		if (list != null) {
			info.setActors(list);
			success = true;
		}
		progress.progress(step);
		
		step = work/nb--;
		work -= step;
		list = getList(page, STR.Producers.toString());
		if (list != null) {
			info.setProductors(list);
			success = true;
		}
		progress.progress(step);
		
		step = work/nb--;
		work -= step;
		list = getList(page, STR.Writers.toString());
		if (list != null) {
			info.setScenaristes(list);
			success = true;
		}
		progress.progress(step);
		
		return new Pair<String,Boolean>(null, success);
	}
	
	private List<Pair<String,Info.DataLink>> getList(String page, String sectionName) {
		Pair<String,Integer> p = getSection(page, ">" + sectionName + "<", "</table>", 0);
		if (p == null) return null;
		List<Pair<String,Info.DataLink>> list = new LinkedList<Pair<String,Info.DataLink>>();
		String section = p.getValue1(); 
		int i = 0;
		String role = "";
		do {
			p = getSection(section, "<tr", "</tr>", i);
			if (p == null) break;
			i = p.getValue2();
			String acteurSection = p.getValue1();
			p = getSection(acteurSection, "<td", "</td>", 0);
			if (p == null) continue;
			int k = p.getValue1().indexOf('>');
			if (k >= 0 && k < p.getValue1().length()-1) {
				String s = p.getValue1().substring(k+1).trim(); 
				if (s.equals("&nbsp;") || s.startsWith("<img")) {
					// there is an image, skip this section
					p = getSection(acteurSection, "<td", "</td>", p.getValue2());
					if (p == null) continue;
				}
			}
			int j = p.getValue2();
			p = getSection(p.getValue1(), "<h5>", "</h5>", 0);
			if (p != null)
				role = p.getValue1();
			p = getSection(acteurSection, "<h5>", "</h5>", j);
			if (p == null) continue;
			Pair<Pair<String,String>,Integer> link = getInfoLinked(p.getValue1(), 0);
			if (link == null) continue;
			String peopleName = link.getValue1().getValue1();
			String peopleLink = link.getValue1().getValue2();

			list.add(new Pair<String,Info.DataLink>(role, new Info.DataLink(PeopleContentType.PEOPLE_TYPE, AlloCineUtil.SOURCE_ID, AlloCineUtil.getIDFromURL(peopleLink), peopleName)));
		} while (true);
		return list;
	}
	
}
