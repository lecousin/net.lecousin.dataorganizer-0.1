package net.lecousin.dataorganizer.audio.action;

import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.ui.plugin.Action;

public class ActionProvider implements net.lecousin.dataorganizer.ui.plugin.ActionProvider {

	public ActionProvider() {
	}
	
	public int getPriority() {
		return 1;
	}

	public List<Action> getActions(List<Data> data) {
		List<Action> actions = new LinkedList<Action>();
		getCommonPreActions(data, actions);
		if (data.size() == 1)
			getActionsForSingleVideo(data.get(0), actions);
		else
			getActionsForMultipleVideos(data, actions);
		getCommonPostActions(data, actions);
		return actions;
	}
	
	private void getCommonPreActions(List<Data> data, List<Action> actions) {
		
	}

	private void getCommonPostActions(List<Data> data, List<Action> actions) {
		
	}

	private void getActionsForMultipleVideos(List<Data> data, List<Action> actions) {
		actions.add(new ActionAddInMediaPlayer(data));
	}

	private void getActionsForSingleVideo(Data data, List<Action> actions) {
		actions.add(new ActionOpenInMediaPlayer(data));
		actions.add(new ActionOpenInSystemApplication(data));
		actions.add(new ActionAddInMediaPlayer(data));
	}

}
