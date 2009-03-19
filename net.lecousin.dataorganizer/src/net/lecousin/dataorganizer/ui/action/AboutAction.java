package net.lecousin.dataorganizer.ui.action;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.ui.dialog.AboutDialog;
import net.lecousin.framework.ui.eclipse.SharedImages;

import org.eclipse.jface.action.Action;

public class AboutAction extends Action {

	public static final String ID = "net.lecousin.dataorganizer.action.AboutAction";
	
	public AboutAction() {
		super(Local.MENU_ITEM_About_DataOrganizer.toString(), SharedImages.getImageDescriptor(SharedImages.icons.x16.basic.HELP));
		setId(ID);
	}

	@Override
	public void run() {
		new AboutDialog();
	}
}
