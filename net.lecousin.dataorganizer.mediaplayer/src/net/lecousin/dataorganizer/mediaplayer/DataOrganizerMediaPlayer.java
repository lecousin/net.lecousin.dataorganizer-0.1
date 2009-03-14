package net.lecousin.dataorganizer.mediaplayer;

import net.lecousin.framework.media.ui.MediaPlayerControl;
import net.lecousin.framework.media.ui.MediaPlayerWindow;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;

public class DataOrganizerMediaPlayer extends MediaPlayerWindow {

	private static DataOrganizerMediaPlayer instance = null;
	public static DataOrganizerMediaPlayer get() {
		if (instance == null) {
			instance = new DataOrganizerMediaPlayer();
			instance.open();
			instance.getShell().addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					instance = null;
				}
			});
		}
		return instance;
	}
	
	private DataOrganizerMediaPlayer() {
	}

	@Override
	protected MediaPlayerControl createMediaPlayer(Composite parent) {
		return new DataOrganizerMediaPlayerControl(parent);
	}
	@Override
	protected String getTitle() {
		return "DataOrganizer Media Player";
	}
	
	@Override
	public DataOrganizerMediaPlayerControl getPlayer() {
		return (DataOrganizerMediaPlayerControl)super.getPlayer();
	}
	
}
