package net.lecousin.dataorganizer.ui.action;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.ui.application.update.Updater;
import net.lecousin.dataorganizer.ui.application.update.Updater.UpdateException;
import net.lecousin.framework.ui.eclipse.SharedImages;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

public class UpdateAction extends Action {

	public static final String ID = "net.lecousin.dataorganizer.action.UpdateAction";
	
	public UpdateAction() {
		super(Local.Update_application.toString(), SharedImages.getImageDescriptor(SharedImages.icons.x16.basic.INTERNET));
		setId(ID);
	}

	@Override
	public void run() {
		try {
			boolean updated = false;
			Updater.Update update = Updater.getLatestVersionInfo();
			if (update != null) {
				if (Updater.askToUpdate(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), update)) {
					updated = true;
					Updater.launchUpdate(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), update);
				}
			}
			if (!updated)
				MessageDialog.openInformation(null, Local.Application_update.toString(), Local.No_update_installed + ".");
			else {
				PlatformUI.getWorkbench().close();
			}
		} catch (UpdateException e) {
			MessageDialog.openError(null, Local.Application_update.toString(), e.getMessage());
		}
	}
}
