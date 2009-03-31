package net.lecousin.dataorganizer.allocine.personne;

import java.util.List;

import net.lecousin.dataorganizer.allocine.AlloCineUtil;
import net.lecousin.dataorganizer.allocine.Search;
import net.lecousin.dataorganizer.allocine.internal.EclipsePlugin;
import net.lecousin.dataorganizer.core.database.info.Info;
import net.lecousin.dataorganizer.core.database.info.InfoRetrieverPlugin;
import net.lecousin.dataorganizer.people.PeopleInfo;
import net.lecousin.dataorganizer.people.PeopleSourceInfo;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.ui.eclipse.EclipseImages;

import org.eclipse.swt.graphics.Image;

public class AlloCinePeopleRetriever implements InfoRetrieverPlugin {

	public AlloCinePeopleRetriever() {
	}

	public String getName() {
		return "AlloCine";
	}
	public String getSourceID() {
		return AlloCineUtil.SOURCE_ID;
	}
	public Image getIcon() {
		return EclipseImages.getImage(EclipsePlugin.ID, "images/icon.gif");
	}
	public String getURLForSourceID(String id) {
		return "http://www.allocine.fr/personne/fichepersonne_gen_cpersonne=" + id + ".html";
	}
	
	public List<SearchResult> search(String name, WorkProgress progress, int work) {
		return Search.search(name, Search.RUB_STARS, progress, work);
	}
	public void retrieve(SearchResult search, Info info, WorkProgress progress, int work) {
		retrieve(AlloCineUtil.getIDFromURL(((Search.AlloCineSearchResult)search).link), search.getName(), info, progress, work);
	}
	
	public boolean retrieve(String id, String name, Info info, WorkProgress progress,	int work) {
		PeopleInfo pi = (PeopleInfo)info;
		PeopleSourceInfo source = (PeopleSourceInfo)pi.setSource(AlloCineUtil.SOURCE_ID, id, name);
		boolean success = false;
		int nb = 2;
		int step = work/nb--;
		work -= step;
		success |= new People().retrieve(id, source, progress, step);
		step = work/nb--;
		work -= step;
		success |= new Filmographie().retrieve(id, source, progress, step);
		return success;
	}
}
