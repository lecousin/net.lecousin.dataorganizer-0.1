package net.lecousin.dataorganizer.ui.action;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.ui.application.update.Updater;
import net.lecousin.dataorganizer.ui.application.update.Updater.UpdateException;
import net.lecousin.framework.Triple;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.version.Version;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.w3c.dom.Element;

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
			Triple<Version,Element,Element> p = Updater.getLatestVersionInfo();
			Version current = Updater.getCurrentVersion();
			if (p != null && p.getValue1().compareTo(current) > 0) {
				String news = Updater.getNews(p.getValue2(), current, p.getValue1());
				if (Updater.askToUpdate(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), current, p.getValue1(), news)) {
					updated = true;
					Updater.launchUpdate(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), p.getValue3());
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
