package net.lecousin.dataorganizer.retrieve.cinefil.star;

import java.util.List;

import net.lecousin.dataorganizer.core.database.info.Info;
import net.lecousin.dataorganizer.core.database.info.InfoRetrieverPlugin;
import net.lecousin.dataorganizer.people.PeopleInfo;
import net.lecousin.dataorganizer.people.PeopleSourceInfo;
import net.lecousin.dataorganizer.retrieve.cinefil.CineFilUtil;
import net.lecousin.dataorganizer.retrieve.cinefil.Search;
import net.lecousin.dataorganizer.retrieve.cinefil.internal.EclipsePlugin;
import net.lecousin.framework.application.Application.Language;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.ui.eclipse.EclipseImages;

import org.eclipse.swt.graphics.Image;

public class CineFilStarPlugin implements InfoRetrieverPlugin {

	public CineFilStarPlugin() {
	}

	public Image getIcon() { return EclipseImages.getImage(EclipsePlugin.ID, "images/icon.gif"); }
	public int getMaxThreads() { return 3; }
	public String getName() { return "CineFil"; }
	public String getSourceID() { return CineFilUtil.SOURCE_ID; }
	public String getURLForSourceID(String id) {
		return "http://"+CineFilUtil.getHost()+"/star/" + id;
	}
	public boolean isSupportingLanguage(Language lang) {
		switch (lang) {
		case FRENCH: return true;
		}
		return false;
	}

	public void retrieve(SearchResult result, Info info, WorkProgress progress,	int work) {
		retrieve(CineFilUtil.getIDFromURL(((Search.CineFilSearchResult)result).url), result.getName(), info, progress, work);
	}

	public boolean retrieve(String id, String name, Info info, WorkProgress progress, int work) {
		PeopleInfo pi = (PeopleInfo)info;
		PeopleSourceInfo source = (PeopleSourceInfo)pi.setSource(CineFilUtil.SOURCE_ID, id, name);
		boolean success = false;
		int nb = 4;
		int step = work/nb--;
		work -= step;
		if (progress.isCancelled()) return false;
		success |= new People().retrieve(id, source, progress, step);
		step = work/nb--;
		work -= step;
		if (progress.isCancelled()) return false;
		success |= new Biographie().retrieve(id, source, progress, step);
		step = work/nb--;
		work -= step;
		if (progress.isCancelled()) return false;
		success |= new Filmographie().retrieve(id, source, progress, step);
		step = work/nb--;
		work -= step;
		if (progress.isCancelled()) return false;
		success |= new Avis().retrieve(id, source, progress, step);
		return success;
	}

	public List<SearchResult> search(String name, WorkProgress progress, int work) {
		return Search.search(name, Search.CAT_STAR, progress, work);
	}
}

