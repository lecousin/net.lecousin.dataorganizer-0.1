package net.lecousin.dataorganizer.ui.action;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.DataOrganizer;
import net.lecousin.framework.ui.eclipse.SharedImages;

import org.eclipse.jface.action.Action;

public class RefreshDataBaseAction extends Action {

	public static final String ID = "net.lecousin.dataorganizer.action.RefreshDataBaseAction";
	
	public RefreshDataBaseAction() {
		super(Local.Refresh_database.toString(), SharedImages.getImageDescriptor(SharedImages.icons.x16.basic.REFRESH));
		setId(ID);
	}

	@Override
	public void run() {
		DataOrganizer.database().refresh();
	}
}
