package net.lecousin.dataorganizer.retriever.cinefil.star;

import net.lecousin.dataorganizer.core.database.info.Info.DataLink;
import net.lecousin.dataorganizer.people.PeopleContentType;
import net.lecousin.dataorganizer.people.PeopleSourceInfo;
import net.lecousin.dataorganizer.retriever.cinefil.CineFilPage;
import net.lecousin.dataorganizer.retriever.cinefil.CineFilUtil;
import net.lecousin.dataorganizer.retriever.cinefil.Local;
import net.lecousin.framework.Pair;
import net.lecousin.framework.Triple;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.xml.XmlParsingUtil;
import net.lecousin.framework.xml.XmlParsingUtil.Node;

public class Filmographie extends CineFilPage<PeopleSourceInfo> {

	@Override
	protected String getCategory() {
		return "star";
	}
	@Override
	protected String getPage() {
		return "/filmographie";
	}

	@Override
	protected String getDescription() {
		return Local.Filmographie.toString();
	}
	
	@Override
	protected String firstPageToReload(String page, String pageURL) {
		return null;
	}

	@Override
	protected Pair<String,Boolean> parse(String page, String pageURL, PeopleSourceInfo info, WorkProgress progress, int work) {
		int i = 0;
		do {
			int j = page.indexOf("<h3><a href=", i);
			if (j < 0) break;
			Triple<Node,Boolean,Integer> t = XmlParsingUtil.parseOpenNode(page, j+4);
			i = t.getValue3();
			if (t.getValue1() == null) continue;
			String url = t.getValue1().attributes.get("href");
			String name = t.getValue1().attributes.get("title");
			if (name == null)
				name = "";
			else
				name = name.trim();
			info.addActivity("Filmographie", new DataLink(PeopleContentType.PEOPLE_TYPE, CineFilUtil.SOURCE_ID, CineFilUtil.getIDFromURL(url), name));
		} while (true);

		progress.progress(work);
		return new Pair<String,Boolean>(null, true);
	}
	
}
