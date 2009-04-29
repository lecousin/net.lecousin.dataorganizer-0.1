package net.lecousin.dataorganizer.retriever.imdb.title;

import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.core.database.info.Info;
import net.lecousin.dataorganizer.people.PeopleContentType;
import net.lecousin.dataorganizer.retriever.imdb.IMDBPage;
import net.lecousin.dataorganizer.retriever.imdb.IMDBUtil;
import net.lecousin.dataorganizer.retriever.imdb.Local;
import net.lecousin.dataorganizer.video.VideoSourceInfo;
import net.lecousin.framework.Pair;
import net.lecousin.framework.Triple;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.xml.XmlParsingUtil;
import net.lecousin.framework.xml.XmlParsingUtil.Node;

public class Casting extends IMDBPage<VideoSourceInfo> {

	@Override
	protected String getCategory() {
		return "title";
	}
	@Override
	protected String getPage() {
		return "/fullcredits";
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
		int nb = 4;
		int step;
		List<Pair<String,Info.DataLink>> list;
		boolean success = false;
		
		step = work/nb--;
		work -= step;
		list = getList(page, "directors", false);
		if (list != null) {
			info.setDirectors(list);
			success = true;
		}
		progress.progress(step);
		
		step = work/nb--;
		work -= step;
		list = getList(page, "cast", true);
		if (list != null) {
			info.setActors(list);
			success = true;
		}
		progress.progress(step);
		
		step = work/nb--;
		work -= step;
		list = getList(page, "writers", false);
		if (list != null) {
			info.setScenaristes(list);
			success = true;
		}
		progress.progress(step);
		
		step = work/nb--;
		work -= step;
		list = getList(page, "producers", false);
		if (list != null) {
			info.setProductors(list);
			success = true;
		}
		progress.progress(step);
		
		return new Pair<String,Boolean>(null, success);
	}
	
	private List<Pair<String,Info.DataLink>> getList(String page, String sectionName, boolean hasPhoto) {
		int i = 0;
		do {
			int j = page.indexOf("<a ", i);
			if (j < 0) return null;
			i = j+1;
			Triple<Node,Boolean,Integer> t = XmlParsingUtil.parseOpenNode(page, j);
			if (t.getValue1() == null) continue;
			if (!sectionName.equals(t.getValue1().attributes.get("name"))) continue;
			Pair<String,Integer> p = getSection(page, "<table", "</table>", i);
			if (p == null) return null;
			return readList(p.getValue1(), hasPhoto);
		} while (true);
	}
	
	private List<Pair<String,Info.DataLink>> readList(String page, boolean hasPhoto) {
		List<Pair<String,Info.DataLink>> list = new LinkedList<Pair<String,Info.DataLink>>();
		int i = 0;
		do {
			Pair<String,Integer> p = getSection(page, "<tr", "</tr>", i);
			if (p == null) return list;
			i = p.getValue2();
			
			String[] cols = getColumns(p.getValue1());
			if (cols.length != (hasPhoto ? 4 : 3)) continue;
			String name = cols[hasPhoto ? 1 : 0];
			String role = cols[hasPhoto ? 3 : 2]; 
			Pair<Pair<String,String>,Integer> pl = getInfoLinked(name, 0);
			if (pl == null) continue;
			String url = pl.getValue1().getValue2();
			name = pl.getValue1().getValue1();
			role = cleanInfo(role, false);
			
			list.add(new Pair<String,Info.DataLink>(role, new Info.DataLink(PeopleContentType.PEOPLE_TYPE, IMDBUtil.SOURCE_ID, IMDBUtil.getIDFromURL(url), name)));
		} while (true);
	}	
}
