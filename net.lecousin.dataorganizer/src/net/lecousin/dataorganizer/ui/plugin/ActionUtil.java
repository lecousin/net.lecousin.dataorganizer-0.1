package net.lecousin.dataorganizer.ui.plugin;

import java.util.Comparator;
import java.util.List;

import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.ui.plugin.Action.Type;
import net.lecousin.framework.collections.CollectionUtil;
import net.lecousin.framework.collections.SortedListTree;

public class ActionUtil {

	public static Action getDefaultAction(Data data) {
		SortedListTree<ActionProvider> providers = new SortedListTree<ActionProvider>(new Comparator<ActionProvider>() {
			public int compare(ActionProvider a1, ActionProvider a2) {
				return a1.getPriority() - a2.getPriority();
			}
		});
		List<ActionProvider> listProviders = ActionProviderManager.getProviders(data.getContentType().getID());
		if (listProviders == null) return null;
			providers.addAll(listProviders);
		for (ActionProvider provider : providers) {
			List<Action> actions = provider.getActions(CollectionUtil.single_element_list(data));
			for (Action action : actions)
				if (action.getType().equals(Type.OPEN))
					return action;
		}
		return null;
	}
	
}
