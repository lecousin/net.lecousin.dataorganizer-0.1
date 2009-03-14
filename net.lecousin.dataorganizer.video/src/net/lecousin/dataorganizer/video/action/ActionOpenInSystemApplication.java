package net.lecousin.dataorganizer.video.action;

import java.net.URI;

import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.ui.plugin.Action;
import net.lecousin.dataorganizer.video.Local;
import net.lecousin.dataorganizer.video.internal.EclipsePlugin;
import net.lecousin.framework.ui.eclipse.EclipseWorkbenchUtil;
import net.lecousin.framework.ui.eclipse.dialog.ErrorDlg;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.ide.FileStoreEditorInput;

public class ActionOpenInSystemApplication implements Action {

	public ActionOpenInSystemApplication(Data data) {
		this.data = data;
	}
	
	private Data data;
	
	public Image getIcon() { return null; } // TODO 

	public String getText() { return Local.Open_with_system_application.toString(); }

	public void run() {
		if (data.getSources().size() == 0) {
			ErrorDlg.error(Local.Open.toString(), Local.No_source+".");
			return;
		}
		try { 
			URI uri = data.getSources().get(0).ensurePresenceAndGetURI();
			if (uri == null) {
				ErrorDlg.error(Local.Open.toString(), Local.Unable_to_locate_file+".");
				return;
			}
			IFileStore file = EFS.getStore(uri);
			IFileInfo info = file.fetchInfo();
			if (!info.exists()) {
				ErrorDlg.error(Local.Open.toString(), Local.The_file+" '" + uri.toString() + "' "+Local.doesnt_exist+".");
				return;
			}
			
			EclipseWorkbenchUtil.getPage().openEditor(new FileStoreEditorInput(file), IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID, true);
			data.opened();
		} catch (Throwable t) {
			ErrorDlg.exception(Local.Open.toString(), Local.Unable_to_open_video.toString(), EclipsePlugin.ID, t);
		}
	}

	public Type getType() {
		return Type.OPEN;
	}
	public boolean isSame(Action action) {
		if (action == null || !(action instanceof ActionOpenInSystemApplication)) return false;
		return data == ((ActionOpenInSystemApplication)action).data;
	}
	
}
