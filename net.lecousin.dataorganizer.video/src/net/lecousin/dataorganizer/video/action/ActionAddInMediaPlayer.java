package net.lecousin.dataorganizer.video.action;

import java.util.List;

import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.mediaplayer.DataOrganizerMediaPlayer;
import net.lecousin.dataorganizer.ui.plugin.Action;
import net.lecousin.dataorganizer.video.Local;
import net.lecousin.framework.collections.CollectionUtil;
import net.lecousin.framework.media.Icons;

import org.eclipse.swt.graphics.Image;

public class ActionAddInMediaPlayer implements Action {

	public ActionAddInMediaPlayer(Data data) {
		this.data = CollectionUtil.single_element_list(data);
	}
	public ActionAddInMediaPlayer(List<Data> data) {
		this.data = data;
	}
	
	private List<Data> data;
	
	public Image getIcon() { return Icons.getIconAdd(); } 

	public String getText() { return Local.Add_to_Media_Player_List.toString(); }

	public void run() {
		DataOrganizerMediaPlayer player = DataOrganizerMediaPlayer.get();
		for (Data d : data)
			player.getPlayer().getPlayList().add(d);
	}

	public Type getType() {
		return Type.SEND;
	}
	
	public boolean isSame(Action action) {
		if (action == null || !(action instanceof ActionAddInMediaPlayer)) return false;
		return data.equals(((ActionAddInMediaPlayer)action).data);
	}
}
