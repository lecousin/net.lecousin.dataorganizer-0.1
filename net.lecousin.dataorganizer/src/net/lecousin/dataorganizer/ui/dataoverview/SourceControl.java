package net.lecousin.dataorganizer.ui.dataoverview;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.info.InfoRetrieverPlugin;
import net.lecousin.dataorganizer.core.database.info.InfoRetrieverPluginRegistry;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.browser.BrowserWindow;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.HyperlinkEvent;

public class SourceControl extends Composite {

	public SourceControl(Composite parent, Data data, String source) {
		super(parent, SWT.NONE);
		setBackground(parent.getBackground());
		
		this.data = data;
		this.source = source;
		
		Image icon = InfoRetrieverPluginRegistry.getIconForSource(source, data.getContentType().getID());
		String name = InfoRetrieverPluginRegistry.getNameForSource(source, data.getContentType().getID());
		
		GridLayout layout = UIUtil.gridLayout(this, icon != null ? 2 : 1);
		layout.marginHeight = layout.marginWidth = 0;
		layout.horizontalSpacing = 1;
		if (icon != null)
			UIUtil.newImage(this, icon);
		UIUtil.newLinkSoftNetStyle(this, name, new GoToSource()).setToolTipText(Local.process(Local.Go_to__page_of__, name, data.getName()));
	}
	
	private Data data;
	private String source;
	
	private class GoToSource implements Listener<HyperlinkEvent> {
		public void fire(HyperlinkEvent event) {
			InfoRetrieverPlugin pi = InfoRetrieverPluginRegistry.getPlugin(source, data.getContentType().getID());
			if (pi == null) return;
			String url = pi.getURLForSourceID(data.getContent().getInfo().getSourceID(source));
			BrowserWindow browser = new BrowserWindow("DataOrganizer", null, true, true);
			browser.open();
			browser.setLocation(url);
			
		}
	}
}
