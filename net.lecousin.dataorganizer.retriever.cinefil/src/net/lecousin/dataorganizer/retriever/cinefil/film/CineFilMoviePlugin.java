package net.lecousin.dataorganizer.retriever.cinefil.film;

import java.util.List;

import net.lecousin.dataorganizer.core.database.info.Info;
import net.lecousin.dataorganizer.core.database.info.InfoRetrieverPlugin;
import net.lecousin.dataorganizer.retriever.cinefil.CineFilUtil;
import net.lecousin.dataorganizer.retriever.cinefil.Search;
import net.lecousin.dataorganizer.retriever.cinefil.internal.EclipsePlugin;
import net.lecousin.dataorganizer.video.VideoInfo;
import net.lecousin.dataorganizer.video.VideoSourceInfo;
import net.lecousin.framework.application.Application.Language;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.ui.eclipse.EclipseImages;

import org.eclipse.swt.graphics.Image;

public class CineFilMoviePlugin implements InfoRetrieverPlugin {

	public CineFilMoviePlugin() {
	}

	public Image getIcon() { return EclipseImages.getImage(EclipsePlugin.ID, "images/icon.gif"); }
	public int getMaxThreads() { return 3; }
	public String getName() { return "CineFil"; }
	public String getSourceID() { return "CineFil"; }
	public String getURLForSourceID(String id) {
		return "http://"+CineFilUtil.getHost()+"/film/" + id;
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
		VideoInfo mi = (VideoInfo)info;
		VideoSourceInfo source = (VideoSourceInfo)mi.setSource(CineFilUtil.SOURCE_ID, id, name);
		boolean success = false;
		int nb = 4;
		int step = work/nb--;
		work -= step;
		if (progress.isCancelled()) return false;
		success |= new Movie().retrieve(id, source, progress, step);
		step = work/nb--;
		work -= step;
		if (progress.isCancelled()) return false;
		success |= new Casting().retrieve(id, source, progress, step);
		step = work/nb--;
		work -= step;
		if (progress.isCancelled()) return false;
		success |= new PressCritik().retrieve(id, source, progress, step);
		if (progress.isCancelled()) return false;
		success |= new UsersCritik().retrieve(id, source, progress, work);
		return success;
	}

	public List<SearchResult> search(String name, WorkProgress progress, int work) {
		return Search.search(name, Search.CAT_FILM, progress, work);
	}
}

