package net.lecousin.dataorganizer.retriever.imdb.name;

import java.util.List;

import net.lecousin.dataorganizer.core.database.info.Info;
import net.lecousin.dataorganizer.core.database.info.InfoRetrieverPlugin;
import net.lecousin.dataorganizer.people.PeopleInfo;
import net.lecousin.dataorganizer.people.PeopleSourceInfo;
import net.lecousin.dataorganizer.retriever.imdb.IMDBUtil;
import net.lecousin.dataorganizer.retriever.imdb.Search;
import net.lecousin.dataorganizer.retriever.imdb.internal.EclipsePlugin;
import net.lecousin.framework.application.Application.Language;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.ui.eclipse.EclipseImages;

import org.eclipse.swt.graphics.Image;

public class IMDBNamePlugin implements InfoRetrieverPlugin {

	public IMDBNamePlugin() {
	}

	public Image getIcon() { return EclipseImages.getImage(EclipsePlugin.ID, "images/icon.gif"); }
	public int getMaxThreads() { return 3; }
	public String getName() { return "IMDb"; }
	public String getSourceID() { return IMDBUtil.SOURCE_ID; }
	public String getURLForSourceID(String id) {
		return "http://"+IMDBUtil.getHost()+"/name/" + id;
	}
	public boolean isSupportingLanguage(Language lang) {
		switch (lang) {
		case ENGLISH: return true;
		}
		return false;
	}

	public void retrieve(SearchResult result, Info info, WorkProgress progress,	int work) {
		retrieve(IMDBUtil.getIDFromURL(((Search.IMDBSearchResult)result).url), result.getName(), info, progress, work);
	}

	public boolean retrieve(String id, String name, Info info, WorkProgress progress, int work) {
		PeopleInfo pi = (PeopleInfo)info;
		PeopleSourceInfo source = (PeopleSourceInfo)pi.setSource(IMDBUtil.SOURCE_ID, id, name);
		boolean success = false;
		int nb = 2;
		int step = work/nb--;
		work -= step;
		if (progress.isCancelled()) return false;
		success |= new People().retrieve(id, source, progress, step);
		step = work/nb--;
		work -= step;
		if (progress.isCancelled()) return false;
		success |= new Biographie().retrieve(id, source, progress, step);
		return success;
	}

	public List<SearchResult> search(String name, WorkProgress progress, int work) {
		return Search.search(name, Search.CAT_NAME, progress, work);
	}
}

