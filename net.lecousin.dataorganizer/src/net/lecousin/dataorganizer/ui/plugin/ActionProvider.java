package net.lecousin.dataorganizer.ui.plugin;

import java.util.List;

import net.lecousin.dataorganizer.core.database.Data;

public interface ActionProvider {

	public int getPriority();
	public List<Action> getActions(List<Data> data);
	
}
