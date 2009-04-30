package net.lecousin.dataorganizer.allcdcovers.retriever.music;

import java.util.List;

import net.lecousin.dataorganizer.allcdcovers.retriever.ACDCUtil;
import net.lecousin.dataorganizer.allcdcovers.retriever.Search;
import net.lecousin.dataorganizer.allcdcovers.retriever.internal.EclipsePlugin;
import net.lecousin.dataorganizer.core.database.info.Info;
import net.lecousin.dataorganizer.core.database.info.InfoRetrieverPlugin;
import net.lecousin.framework.application.Application.Language;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.ui.eclipse.EclipseImages;

import org.eclipse.swt.graphics.Image;

public class AllCDCoversMusicPlugin implements InfoRetrieverPlugin {

	public AllCDCoversMusicPlugin() {
	}

	public Image getIcon() { return EclipseImages.getImage(EclipsePlugin.ID, "images/icon.gif"); }
	public int getMaxThreads() { return 3; }
	public String getName() { return "AllCDCovers"; }
	public String getSourceID() { return ACDCUtil.SOURCE_ID; }
	public String getURLForSourceID(String id) {
		return "http://"+ACDCUtil.getHost()+"/show/" + id;
	}
	public boolean isSupportingLanguage(Language lang) { return true; }

	public void retrieve(SearchResult result, Info info, WorkProgress progress,	int work) {
	}

	public boolean retrieve(String id, String name, Info info, WorkProgress progress, int work) {
	}

	public List<SearchResult> search(String name, WorkProgress progress, int work) {
		return Search.search(name, Search.MUSIC, progress, work);
	}
}

