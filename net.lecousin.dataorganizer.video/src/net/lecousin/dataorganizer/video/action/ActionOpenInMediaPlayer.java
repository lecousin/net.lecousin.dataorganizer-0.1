package net.lecousin.dataorganizer.video.action;

import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.mediaplayer.DataOrganizerMediaPlayer;
import net.lecousin.dataorganizer.ui.plugin.Action;
import net.lecousin.dataorganizer.video.Local;
import net.lecousin.framework.media.Icons;

import org.eclipse.swt.graphics.Image;

public class ActionOpenInMediaPlayer implements Action {

	public ActionOpenInMediaPlayer(Data data) {
		this.data = data;
	}
	
	private Data data;
	
	public Image getIcon() { return Icons.getIconPlay(); } 

	public String getText() { return Local.Open_in_Media_Player.toString(); }

	public void run() {
		DataOrganizerMediaPlayer player = DataOrganizerMediaPlayer.get();
		player.getPlayer().getPlayList().addAndStart(data);
	}

	public Type getType() {
		return Type.OPEN;
	}
	public boolean isSame(Action action) {
		if (action == null || !(action instanceof ActionOpenInMediaPlayer)) return false;
		return data == ((ActionOpenInMediaPlayer)action).data;
	}
}
