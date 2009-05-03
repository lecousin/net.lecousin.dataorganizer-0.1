package net.lecousin.dataorganizer.retriever.amazon.music;

import java.util.List;

import net.lecousin.dataorganizer.audio.AudioInfo;
import net.lecousin.dataorganizer.audio.AudioSourceInfo;
import net.lecousin.dataorganizer.core.database.info.Info;
import net.lecousin.dataorganizer.core.database.info.InfoRetrieverPlugin;
import net.lecousin.dataorganizer.retriever.amazon.AmazonUtil;
import net.lecousin.dataorganizer.retriever.amazon.Search;
import net.lecousin.dataorganizer.retriever.amazon.internal.EclipsePlugin;
import net.lecousin.framework.application.Application.Language;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.ui.eclipse.EclipseImages;

import org.eclipse.swt.graphics.Image;

public class AmazonMusicRetriever implements InfoRetrieverPlugin {

	public AmazonMusicRetriever() {
	}

	public String getName() {
		return "Amazon";
	}
	public String getSourceID() {
		return AmazonUtil.SOURCE_ID;
	}
	public Image getIcon() {
		return EclipseImages.getImage(EclipsePlugin.ID, "images/icon.gif");
	}
	public String getURLForSourceID(String id) {
		return "http://"+AmazonUtil.getHost()+AmazonUtil.getPageURLForID(id, "dp");
	}
	public boolean isSupportingLanguage(Language lang) {
		switch (lang) {
		case FRENCH:
		case ENGLISH: return true;
		}
		return false;
	}
	public int getMaxThreads() {
		return 3;
	}

	public void retrieve(SearchResult search, Info info, WorkProgress progress, int work) {
		retrieve(((Search.AmazonSearchResult)search).id, search.getName(), info, progress, work);
	}

	public List<SearchResult> search(String name, WorkProgress progress, int work) {
		return Search.search(name, Search.SearchType.MUSIC, progress, work);
	}
	
	public boolean retrieve(String id, String name, Info info, WorkProgress progress, int work) {
		AudioInfo mi = (AudioInfo)info;
		AudioSourceInfo source = (AudioSourceInfo)mi.setSource(AmazonUtil.SOURCE_ID, id, name);
		boolean success = false;
		int nb = 3;
		int step = work/nb--;
		work -= step;
		if (progress.isCancelled()) return false;
		success |= new AlbumImage().retrieve(id, source, progress, step);
		step = work/nb--;
		work -= step;
		if (progress.isCancelled()) return false;
		success |= new Reviews().retrieve(id, source, progress, step);
		step = work/nb--;
		work -= step;
		if (progress.isCancelled()) return false;
		success |= new Album().retrieve(id, source, progress, step);
		return success;
	}
}
