package net.lecousin.dataorganizer.ui.action;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.ui.wizard.adddata.AddDataWizard;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.dialog.MyDialog;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;

public class AddDataAction extends Action {

	public static final String ID = "net.lecousin.dataorganizer.action.AddDataAction";
	
	public AddDataAction() {
		super(Local.Add_data.toString(), SharedImages.getImageDescriptor(SharedImages.icons.x16.basic.ADD));
		setId(ID);
	}

	@Override
	public void run() {
		WizardDialog dlg = new WizardDialog(MyDialog.getModalShell(), new AddDataWizard());
		dlg.open();
	}
}
