package net.lecousin.dataorganizer.core.database.info;

import java.util.List;

import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.ui.eclipse.control.ControlProvider;

import org.eclipse.swt.graphics.Image;

public interface InfoRetrieverPlugin {

	public String getName();
	public String getSourceID();
	public Image getIcon();
	
	public String getURLForSourceID(String id);
	
	public interface SearchResult {
		public String getName();
		public ControlProvider getDescriptionControlProvider();
	}

	public List<SearchResult> search(String name, WorkProgress progress, int work);
	public void retrieve(SearchResult result, Info info, WorkProgress progress, int work);
	
	public boolean retrieve(String id, String name, Info info, WorkProgress progress, int work);
}
