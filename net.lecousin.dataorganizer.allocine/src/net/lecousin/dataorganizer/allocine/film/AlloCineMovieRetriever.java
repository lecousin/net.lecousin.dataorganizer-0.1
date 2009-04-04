package net.lecousin.dataorganizer.allocine.film;

import java.util.List;

import net.lecousin.dataorganizer.allocine.AlloCineUtil;
import net.lecousin.dataorganizer.allocine.Search;
import net.lecousin.dataorganizer.allocine.internal.EclipsePlugin;
import net.lecousin.dataorganizer.core.database.info.Info;
import net.lecousin.dataorganizer.core.database.info.InfoRetrieverPlugin;
import net.lecousin.dataorganizer.video.VideoInfo;
import net.lecousin.dataorganizer.video.VideoSourceInfo;
import net.lecousin.framework.application.Application.Language;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.ui.eclipse.EclipseImages;

import org.eclipse.swt.graphics.Image;

public class AlloCineMovieRetriever implements InfoRetrieverPlugin {

	public AlloCineMovieRetriever() {
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
		return "http://"+AlloCineUtil.getHost()+"/film/fichefilm_gen_cfilm=" + id + ".html";
	}
	public boolean isSupportingLanguage(Language lang) {
		switch (lang) {
		case FRENCH:
		case ENGLISH: return true;
		}
		return false;
	}

	public void retrieve(SearchResult search, Info info, WorkProgress progress, int work) {
		retrieve(AlloCineUtil.getIDFromURL(((Search.AlloCineSearchResult)search).link), search.getName(), info, progress, work);
	}

	public List<SearchResult> search(String name, WorkProgress progress, int work) {
		return Search.search(name, Search.RUB_FILMS, progress, work);
	}
	
	public boolean retrieve(String id, String name, Info info, WorkProgress progress, int work) {
		VideoInfo mi = (VideoInfo)info;
		VideoSourceInfo source = (VideoSourceInfo)mi.setSource(AlloCineUtil.SOURCE_ID, id, name);
		boolean success = false;
		int nb = 5;
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
		success |= new Poster().retrieve(id, source, progress, step);
		step = work/nb--;
		work -= step;
		if (progress.isCancelled()) return false;
		success |= new PressCritik().retrieve(id, source, progress, step);
		if (progress.isCancelled()) return false;
		success |= new UsersCritik().retrieve(id, source, progress, work);
		return success;
	}
}
