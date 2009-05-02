package net.lecousin.dataorganizer.ui.views.dataoverview;

import net.lecousin.dataorganizer.core.DataOrganizer;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.internal.EclipsePlugin;
import net.lecousin.dataorganizer.ui.application.bar.PerspectiveButtons;
import net.lecousin.dataorganizer.ui.views.DetailsPerspective;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.control.UIControlUtil;
import net.lecousin.framework.ui.eclipse.dialog.ErrorDlg;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class DataOverviewView extends ViewPart {

	public static final String ID = "net.lecousin.dataorganizer.dataDetailView";
	
	public static void show() {
		try { 
			DataOverviewView view = (DataOverviewView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(DataOverviewView.ID);
			view.reset(isBig());
		}
		catch (PartInitException e) {
			ErrorDlg.exception("Internal error", "Unable to show detail view", EclipsePlugin.ID, e);
		}
	}
	public static void hide() {
		IViewReference[] views = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences();
		for (IViewReference view : views)
			if (view.getId().equals(DataOverviewView.ID))
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().hideView(view);
	}
	public static void reset() {
		IViewReference[] views = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences();
		for (IViewReference view : views)
			if (view.getId().equals(DataOverviewView.ID)) {
				DataOverviewView v = (DataOverviewView)view.getView(false);
				if (v == null) return;
				v.reset(isBig());
				return;
			}
	}
	private static boolean isBig() {
		String id = PerspectiveButtons.getSelectedPerspective();
		boolean big = false;
		if (id != null) {
			if (id.equals(DetailsPerspective.ID))
				big = true;
		}
		return big;
	}
	
	public DataOverviewView() {
		DataOrganizer.dataSelectionChanged().addListener(dataChangedListener);
	}

	private ScrolledComposite scroll;
	private OverviewPanel overviewPanel;
	private Listener<Data> dataChangedListener = new Listener<Data>() {
		public void fire(Data event) {
			refresh(event);
		}
	};
	
	@Override
	public void setInitializationData(IConfigurationElement cfig, String propertyName, Object data) {
		super.setInitializationData(cfig, propertyName, data);
		setTitleImage(SharedImages.getImage(SharedImages.icons.x16.basic.INFO));
	}
	
	@Override
	public void createPartControl(Composite parent) {
		scroll = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		scroll.setExpandHorizontal(true);
		scroll.getVerticalBar().setIncrement(20);
		scroll.getVerticalBar().setPageIncrement(100);
	}
	private boolean big;
	private boolean initialized = false;
	public void reset(boolean big) {
		if (initialized && big == this.big) return;
		this.big = big;
		initialized = true;
		if (overviewPanel != null && !overviewPanel.isDisposed())
			overviewPanel.dispose();
		overviewPanel = new OverviewPanel(scroll, big);
		scroll.setContent(overviewPanel);
		overviewPanel.setData(new UIControlUtil.TopLevelResize());
		refresh(DataOrganizer.getSelectedData());
	}

	@Override
	public void setFocus() {
	}
	
	@Override
	public void dispose() {
		DataOrganizer.dataSelectionChanged().removeListener(dataChangedListener);
		super.dispose();
	}

	private void refresh(Data data) {
		if (data == null) return;
		overviewPanel.refresh(data);
	}
}
