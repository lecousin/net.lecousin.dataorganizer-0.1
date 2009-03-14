package net.lecousin.dataorganizer.ui.view.label;

import java.util.LinkedList;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.DataLabels.Label;
import net.lecousin.dataorganizer.internal.EclipsePlugin;
import net.lecousin.dataorganizer.ui.control.LabelTree;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.dialog.ErrorDlg;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class LabelsView extends ViewPart {

	public static final String ID = "net.lecousin.dataorganizer.view.label.LabelsView";
	
	public static LabelsView show() {
		try { 
			return (LabelsView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(LabelsView.ID);
		}
		catch (PartInitException e) {
			ErrorDlg.exception(Local.Labels.toString(), "Unable to open view", EclipsePlugin.ID, e);
			return null;
		}
	}
	
	public LabelsView() {
	}

	@Override
	public void setInitializationData(IConfigurationElement cfig, String propertyName, Object data) {
		super.setInitializationData(cfig, propertyName, data);
		setTitleImage(SharedImages.getImage(SharedImages.icons.x16.basic.LABEL));
		setPartName(Local.Labels.toString());
	}
	
	@Override
	public void createPartControl(Composite parent) {
		LabelTree tree = new LabelTree(parent, false, 0, null);
	}
	
	@Override
	public void setFocus() {
	}
}
