package net.lecousin.dataorganizer.ui.action;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.ui.views.label.LabelsView;
import net.lecousin.framework.ui.eclipse.SharedImages;

import org.eclipse.jface.action.Action;

public class ShowLabelsViewAction extends Action {

	public static final String ID = "net.lecousin.dataorganizer.action.ShowLabelsViewAction";
	
	public ShowLabelsViewAction() {
		super(Local.Labels.toString(), SharedImages.getImageDescriptor(SharedImages.icons.x16.basic.LABEL));
		setId(ID);
	}

	@Override
	public void run() {
		LabelsView.show();
	}
}
