package net.lecousin.dataorganizer.ui.application.preferences;

import net.lecousin.dataorganizer.ui.plugin.PreferencePageProvider;

import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.swt.graphics.Image;

public class DOPreferenceNode extends PreferenceNode {

	public DOPreferenceNode(PreferencePageProvider provider) {
		super(provider.getID());
		this.provider = provider;
	}
	
	PreferencePageProvider provider;
	IPreferencePage page = null;
	
	@Override
	public String getLabelText() { return provider.getLabelText(); }
	@Override
	public Image getLabelImage() { return provider.getLabelImage(); }
	@Override
	public IPreferencePage getPage() {
		if (page == null || (page.getControl() != null && page.getControl().isDisposed()))
			page = provider.create();
		return page;
	}
	
	
}
