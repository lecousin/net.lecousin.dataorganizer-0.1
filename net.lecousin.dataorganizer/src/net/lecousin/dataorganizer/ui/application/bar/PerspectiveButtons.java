package net.lecousin.dataorganizer.ui.application.bar;

import net.lecousin.dataorganizer.ui.views.dataoverview.DataOverviewView;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.ui.eclipse.event.SelectionListenerWithData;

import org.eclipse.jface.action.ControlContribution;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

public class PerspectiveButtons extends ControlContribution {

	public static final String ID = "net.lecousin.dataorganizer.ui.application.bar.PerspectiveButtons";

	public PerspectiveButtons() {
		super(ID);
	}
	
	@Override
	protected Control createControl(Composite parent) {
		bar = new ToolBar(parent, SWT.HORIZONTAL | SWT.FLAT | SWT.RIGHT);
		ToolItem item;
		for (IPerspectiveDescriptor desc : PlatformUI.getWorkbench().getPerspectiveRegistry().getPerspectives()) {
			item = new ToolItem(bar, SWT.CHECK);
			item.setImage(desc.getImageDescriptor().createImage());
			item.setText(desc.getLabel());
			item.setToolTipText(desc.getDescription());
			item.setSelection(desc.getId().equals(PlatformUI.getWorkbench().getPerspectiveRegistry().getDefaultPerspective()));
			item.setData(desc.getId());
			item.addSelectionListener(new SelectionListenerWithData<String>(desc.getId()) {
				public void widgetSelected(SelectionEvent e) {
					selectPerspective(data());
				}
			});
		}
		return bar;
	}
	
	private static ToolBar bar;

	private void selectPerspective(String id) {
		for (ToolItem item : bar.getItems())
			item.setSelection(((String)item.getData()).equals(id));
		try { 
			PlatformUI.getWorkbench().showPerspective(id, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
			DataOverviewView.reset();
		}
		catch (WorkbenchException e) {
			if (Log.error(this))
				Log.error(this, "Unable to change perspective", e);
		}
	}
	
	public static String getSelectedPerspective() {
		if (bar == null || bar.isDisposed()) return null;
		for (ToolItem item : bar.getItems())
			if (item.getSelection())
				return (String)item.getData();
		return null;
	}
}
