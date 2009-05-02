package net.lecousin.dataorganizer.ui.views;

import net.lecousin.dataorganizer.ui.views.datalist.DataListView;
import net.lecousin.dataorganizer.ui.views.dataoverview.DataOverviewView;
import net.lecousin.dataorganizer.ui.views.search.SearchView;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPlaceholderFolderLayout;

public class ClassicPerspective implements IPerspectiveFactory {

	public static final String ID = "net.lecousin.dataorganizer.ClassicPerspective";
	
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		
		layout.addStandaloneView(SearchView.ID,  true, IPageLayout.LEFT, 0.25f, editorArea);
		layout.addStandaloneView(DataListView.ID, false, IPageLayout.TOP, 0.60f, editorArea);
		layout.addStandaloneView(DataOverviewView.ID, false, IPageLayout.BOTTOM, 0.40f, editorArea);
		IPlaceholderFolderLayout folder = layout.createPlaceholderFolder("Plugin", IPageLayout.BOTTOM, 0.65f, SearchView.ID);
		folder.addPlaceholder("*");
		IPlaceholderFolderLayout folder2 = layout.createPlaceholderFolder("Right", IPageLayout.RIGHT, 0.80f, DataListView.ID);
		folder2.addPlaceholder("net.lecousin.dataorganizer.view.label.LabelsView");
		
		layout.getViewLayout(SearchView.ID).setCloseable(false);
		layout.getViewLayout(DataListView.ID).setCloseable(false);
	}
}
