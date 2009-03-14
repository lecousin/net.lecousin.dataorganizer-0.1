package net.lecousin.dataorganizer.mediaplayer.bar;

import net.lecousin.dataorganizer.mediaplayer.DataOrganizerMediaPlayer;
import net.lecousin.dataorganizer.mediaplayer.Local;
import net.lecousin.framework.ui.eclipse.SharedImages;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class BarContribution extends ContributionItem {

	public static final String ID = "net.lecousin.dataorganizer.mediaplayer.bar";
	
	public BarContribution() {
		this(ID);
	}

	public BarContribution(String id) {
		super(id);
	}

	@Override
	public void fill(ToolBar parent, int index) {
		ToolItem item = new ToolItem(parent, SWT.PUSH);
		item.setImage(SharedImages.getImage(SharedImages.icons.x16.filetypes.MOVIE));
		item.setToolTipText(Local.Open_Media_Player.toString());
		item.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				DataOrganizerMediaPlayer.get();
			}
		});
	}
	
}
