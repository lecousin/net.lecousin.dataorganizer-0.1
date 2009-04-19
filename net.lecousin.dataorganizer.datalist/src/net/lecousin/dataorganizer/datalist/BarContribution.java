package net.lecousin.dataorganizer.datalist;

import net.lecousin.dataorganizer.datalist.internal.EclipsePlugin;
import net.lecousin.framework.ui.eclipse.EclipseImages;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class BarContribution extends ContributionItem {

	public static final String ID = "net.lecousin.dataorganizer.datalist.bar";
	
	public BarContribution() {
		this(ID);
	}

	public BarContribution(String id) {
		super(id);
	}

	@Override
	public void fill(ToolBar parent, int index) {
		ToolItem item = new ToolItem(parent, SWT.PUSH);
		item.setImage(EclipseImages.getImage(EclipsePlugin.ID, "images/list.gif"));
		item.setToolTipText(Local.Open_data_lists.toString());
		item.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				View.show();
			}
		});
	}
	
}
