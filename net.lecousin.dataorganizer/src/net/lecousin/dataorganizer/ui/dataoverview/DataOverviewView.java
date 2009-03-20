package net.lecousin.dataorganizer.ui.dataoverview;

import net.lecousin.dataorganizer.core.DataOrganizer;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.control.UIControlUtil;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class DataOverviewView extends ViewPart {

	public static final String ID = "net.lecousin.dataorganizer.datadetailView";
	
	public DataOverviewView() {
		DataOrganizer.dataSelectionChanged().addListener(dataChangedListener);
	}

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
		ScrolledComposite scroll = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		overviewPanel = new OverviewPanel(scroll);
		scroll.setContent(overviewPanel);
		scroll.setExpandHorizontal(true);
		scroll.getVerticalBar().setIncrement(20);
		scroll.getVerticalBar().setPageIncrement(100);
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
