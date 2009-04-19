package net.lecousin.dataorganizer.datalist;

import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.ui.plugin.Action;
import net.lecousin.dataorganizer.ui.plugin.ActionProvider;

public class DataListActionProvider implements ActionProvider {

	public DataListActionProvider() {
	}

	public List<Action> getActions(List<Data> data) {
		List<Action> result = new LinkedList<Action>();
		result.add(new AddToListAction(data));
		return result;
	}

	public int getPriority() {
		return 10000;
	}

}
