package net.lecousin.dataorganizer.ui.action;

import net.lecousin.dataorganizer.Local;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.dialog.MyDialog;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.ui.PlatformUI;

public class ConfigAction extends Action {

	public static final String ID = "net.lecousin.dataorganizer.action.ConfigAction";
	
	public ConfigAction() {
		super(Local.MENU_ITEM_Options_Configuration.toString(), SharedImages.getImageDescriptor(SharedImages.icons.x16.basic.CONFIGURATION));
		setId(ID);
	}

	@Override
	public void run() {
		PreferenceDialog dlg = new PreferenceDialog(MyDialog.getPlatformShell(), PlatformUI.getWorkbench().getPreferenceManager());
		dlg.open();
	}
}
