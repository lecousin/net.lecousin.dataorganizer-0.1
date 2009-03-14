package net.lecousin.dataorganizer.ui;

import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.core.DataOrganizer;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.internal.EclipsePlugin;
import net.lecousin.dataorganizer.ui.datalist.DataListView;
import net.lecousin.dataorganizer.ui.dataoverview.DataOverviewView;
import net.lecousin.dataorganizer.ui.search.SearchView;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.ui.eclipse.dialog.ErrorDlg;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPlaceholderFolderLayout;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class Perspective implements IPerspectiveFactory {

	public static final String ID = "net.lecousin.dataorganizer.perspective";
	
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

		DataOrganizer.dataSelectionChanged().addListener(new Listener<Data>() {
			public void fire(Data data) {
				if (data == null) {
					IViewReference[] views = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences();
					for (IViewReference view : views)
						if (view.getId().equals(DataOverviewView.ID) || detailsViews.contains(view.getId()))
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().hideView(view);
				} else {
					try { PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(DataOverviewView.ID); }
					catch (PartInitException e) {
						ErrorDlg.exception("Internal error", "Unable to show detail view", EclipsePlugin.ID, e);
					}
				}
			}
		});

	}
	
	private List<String> detailsViews = new LinkedList<String>();
}
