package net.lecousin.dataorganizer.allocine.personne;

import net.lecousin.dataorganizer.allocine.AlloCinePage;
import net.lecousin.dataorganizer.allocine.AlloCineUtil;
import net.lecousin.dataorganizer.core.database.info.Info.DataLink;
import net.lecousin.dataorganizer.people.PeopleInfo;
import net.lecousin.dataorganizer.video.VideoContentType;
import net.lecousin.framework.Pair;
import net.lecousin.framework.progress.WorkProgress;

public class Filmographie extends AlloCinePage<PeopleInfo> {

	@Override
	protected String getDescription() { return "Activities"; }
	@Override
	protected String getCategory() { return "personne"; }
	@Override
	protected String getPage() { return "filmographie"; }

	@Override
	protected String firstPageToReload(String page, String url) {
		return null;
	}
	@Override
	protected Pair<String,Boolean> parse(String page, String pageURL, PeopleInfo info, WorkProgress progress, int work) {
		int i = page.indexOf("<b>Filmographie</b>");
		if (i < 0) { progress.progress(work); return new Pair<String,Boolean>(null, false); }

		Pair<String,Integer> sectionP = getSection(page, "<table", "</table>", i);
		if (sectionP == null) { progress.progress(work); return new Pair<String,Boolean>(null, false); }
		String section = sectionP.getValue1();
		
		i = 0;
		String activity = null;
//		String[] detailsNames = null;
		do {
			Pair<String,Integer> line = getSection(section, "<tr", "</tr>", i);
			if (line == null) break;
			i = line.getValue2();
			
			Pair<String,Integer> nextLine = getSection(section, "<tr", "</tr>", i);
			if (nextLine != null && nextLine.getValue1().contains("<hr")) {
				String[] columns = getColumns(line.getValue1());
				if (columns.length == 0) continue;
				activity = removeAllTags(columns[0]);
//				detailsNames = new String[columns.length-1];
//				for (int j = 1; j < columns.length; ++j)
//					detailsNames[j-1] = removeAllTags(columns[j]);
				continue;
			}
			
			if (activity == null) continue;
			String[] columns = getColumns(line.getValue1());
			if (columns.length == 0) continue;

			Pair<Pair<String,String>,Integer> linkMovie = getInfoLinked(columns[0], 0);
			DataLink link = null;
			if (linkMovie != null ){
				if (linkMovie.getValue1().getValue2().contains("fichefilm")) {
					String dataName = removeAllTags(linkMovie.getValue1().getValue1()).trim();
					String id = AlloCineUtil.getIDFromURL(linkMovie.getValue1().getValue2());
					link = new DataLink(VideoContentType.VIDEO_TYPE, AlloCineUtil.SOURCE_ID, id, dataName);
					info.addActivity(activity, link);
					continue;
				}
			}
			
			String text = removeAllTags(columns[0]).trim();
			if (text.length() == 0) continue;
			info.addActivity(activity, text);
			
		} while (true);

		return new Pair<String,Boolean>(null, true);
	}
	
}
