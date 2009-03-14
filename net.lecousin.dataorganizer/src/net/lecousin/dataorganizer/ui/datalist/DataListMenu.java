package net.lecousin.dataorganizer.ui.datalist;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.content.ContentType;
import net.lecousin.dataorganizer.ui.plugin.Action;
import net.lecousin.dataorganizer.ui.plugin.ActionProvider;
import net.lecousin.dataorganizer.ui.plugin.ActionProviderManager;
import net.lecousin.dataorganizer.ui.plugin.ActionUtil;
import net.lecousin.dataorganizer.ui.plugin.Action.Type;
import net.lecousin.framework.collections.CollectionUtil;
import net.lecousin.framework.collections.SortedListTree;
import net.lecousin.framework.thread.RunnableWithData;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.dialog.FlatPopupMenu;

public class DataListMenu {

	public static void menu(Data data, boolean addDataListManagementActions) {
		menu(CollectionUtil.single_element_list(data), addDataListManagementActions);
	}
	
	public static void openDefault(Data data) {
		Action defaultAction = ActionUtil.getDefaultAction(data);
		if (defaultAction != null)
			defaultAction.run();
	}

	public static void menu(List<Data> data, boolean addDataListManagementActions) {
		String name;
		if (data.size() == 1)
			name = data.get(0).getName();
		else
			name = ""+data.size() + " " + Local.data__s;
		FlatPopupMenu dlg = new FlatPopupMenu(null, name, true, false, false, true);
		fillMenu(dlg, data, addDataListManagementActions);
		dlg.show(null, FlatPopupMenu.Orientation.TOP_BOTTOM, true);
	}
	
	private static void fillMenu(FlatPopupMenu menu, List<Data> data, boolean addDataListManagementActions) {
		Map<ContentType,List<Data>> types = new HashMap<ContentType,List<Data>>();
		for (Data d : data) {
			ContentType type = d.getContentType();
			List<Data> list = types.get(type);
			if (list == null) {
				list = new LinkedList<Data>();
				types.put(type, list);
			}
			list.add(d);
		}
		Action defaultAction = data.size() == 1 ? ActionUtil.getDefaultAction(data.get(0)) : null;
		for (ContentType type : types.keySet()) {
			List<Data> list = types.get(type);
			if (data.size() > 1)
				new FlatPopupMenu.Title(menu, type.getName() + ": " + list.size() + " " + Local.data__s);
			SortedListTree<ActionProvider> providers = new SortedListTree<ActionProvider>(new Comparator<ActionProvider>() {
				public int compare(ActionProvider a1, ActionProvider a2) {
					return a1.getPriority() - a2.getPriority();
				}
			});
			List<ActionProvider> listProviders = ActionProviderManager.getProviders(type.getID());
			if (listProviders != null)
				providers.addAll(listProviders);
			boolean menuAdded = false;
			for (ActionProvider provider : providers)
				for (Action action : provider.getActions(list)) {
					if (action.getType().equals(Type.LIST_MANAGEMENT) && !addDataListManagementActions)
						continue;
					new FlatPopupMenu.Menu(menu, action.getText(), action.getIcon(), action.isSame(defaultAction), false, action);
					menuAdded = true;
				}
			if (data.size() > 1 || menuAdded)
				new FlatPopupMenu.Separator(menu);
		}
		if (addDataListManagementActions) {
			String str = Local.Delete.toString();
			if (data.size() > 1) str = str + " " + data.size() + " " + Local.data__s;
			new FlatPopupMenu.Menu(menu, str, SharedImages.getImage(SharedImages.icons.x16.basic.DEL), false, false, new RunnableWithData<List<Data>>(data) {
				public void run() {
					DataListActions.delete(data());
				}
			});
		}
	}
	
}
