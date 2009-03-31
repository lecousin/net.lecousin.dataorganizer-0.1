package net.lecousin.dataorganizer.ui.application.preferences;

import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.swt.graphics.Image;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.ui.plugin.PreferencePageProvider;
import net.lecousin.framework.ui.eclipse.SharedImages;

public class UpdatePageProvider implements PreferencePageProvider {

	public UpdatePageProvider() {
	}

	public String getID() {
		return "net.lecousin.dataorganizer.ui.application.preferences.UpdatePage";
	}
	public String getPath() {
		return null;
	}
	public String getLabelText() {
		return Local.Update.toString();
	}
	public Image getLabelImage() {
		return SharedImages.getImage(SharedImages.icons.x16.basic.CONNECTED_GREEN_GREEN);
	}
	public IPreferencePage create() {
		return new UpdatePage();
	}
}
