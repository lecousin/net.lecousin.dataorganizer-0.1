package net.lecousin.dataorganizer.mediaplayer;

import org.eclipse.swt.widgets.Composite;

import net.lecousin.framework.media.ui.MediaPlayerControl;
import net.lecousin.framework.media.ui.PlayList;

public class DataOrganizerMediaPlayerControl extends MediaPlayerControl {

	public DataOrganizerMediaPlayerControl(Composite parent) {
		super(parent);
	}

	@Override
	protected PlayList createPlayList(Composite parent) {
		return new DataOrganizerPlayList(parent, this);
	}
	
	@Override
	public DataOrganizerPlayList getPlayList() {
		return (DataOrganizerPlayList)super.getPlayList();
	}
}
