package net.lecousin.dataorganizer.ui.views;

import net.lecousin.dataorganizer.ui.views.datalist.DataListView;
import net.lecousin.dataorganizer.ui.views.dataoverview.DataOverviewView;
import net.lecousin.dataorganizer.ui.views.search.SearchView;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPlaceholderFolderLayout;

public class DetailsPerspective implements IPerspectiveFactory {

	public static final String ID = "net.lecousin.dataorganizer.DetailsPerspective";
	
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		
		IFolderLayout folderLeftMiddle = layout.createFolder("Left-Middle", IPageLayout.LEFT, 0.4f, editorArea);
		folderLeftMiddle.addView(DataListView.ID);
		IFolderLayout folderLeftTop = layout.createFolder("Left-Top", IPageLayout.TOP, 0.4f, "Left-Middle");
		folderLeftTop.addView(SearchView.ID);
		IPlaceholderFolderLayout folder = layout.createPlaceholderFolder("Plugin", IPageLayout.BOTTOM, 0.65f, "Left-Middle");
		folder.addPlaceholder("*");
		IPlaceholderFolderLayout folder2 = layout.createPlaceholderFolder("Right", IPageLayout.RIGHT, 0.80f, DataOverviewView.ID);
		folder2.addPlaceholder("net.lecousin.dataorganizer.view.label.LabelsView");
		
		layout.addStandaloneView(DataOverviewView.ID, false, IPageLayout.RIGHT, 0.40f, editorArea);
		
		layout.getViewLayout(SearchView.ID).setCloseable(false);
		layout.getViewLayout(DataListView.ID).setCloseable(false);
	}
}
