package net.lecousin.dataorganizer.ui.plugin;

import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.swt.graphics.Image;

public interface PreferencePageProvider {

	public IPreferencePage create();
	public String getID();
	/** null or path */
	public String getPath();
	public String getLabelText();
	public Image getLabelImage();
	
}
