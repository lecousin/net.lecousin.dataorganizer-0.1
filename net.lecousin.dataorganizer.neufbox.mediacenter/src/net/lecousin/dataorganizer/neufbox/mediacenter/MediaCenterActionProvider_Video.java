package net.lecousin.dataorganizer.neufbox.mediacenter;

import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.ui.plugin.Action;
import net.lecousin.dataorganizer.ui.plugin.ActionProvider;
import net.lecousin.neufbox.mediacenter.Media;

public class MediaCenterActionProvider_Video implements ActionProvider {

	public MediaCenterActionProvider_Video() {
	}
	
	public int getPriority() {
		return 1000;
	}

	public List<Action> getActions(List<Data> data) {
		List<Action> list = new LinkedList<Action>();
		list.add(new ActionAdd(data, Media.TYPE_MOVIE));
		return list;
	}

}
