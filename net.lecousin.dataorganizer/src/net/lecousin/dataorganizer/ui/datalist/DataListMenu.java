package net.lecousin.dataorganizer.ui.datalist;

import java.io.File;
import java.net.URI;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.content.ContentType;
import net.lecousin.dataorganizer.core.database.source.DataSource;
import net.lecousin.dataorganizer.internal.EclipsePlugin;
import net.lecousin.dataorganizer.ui.dialog.DataLinkPopup;
import net.lecousin.dataorganizer.ui.dialog.OpenWithDialog;
import net.lecousin.dataorganizer.ui.plugin.Action;
import net.lecousin.dataorganizer.ui.plugin.ActionProvider;
import net.lecousin.dataorganizer.ui.plugin.ActionProviderManager;
import net.lecousin.dataorganizer.ui.plugin.ActionUtil;
import net.lecousin.dataorganizer.ui.plugin.Action.Type;
import net.lecousin.framework.Pair;
import net.lecousin.framework.collections.CollectionUtil;
import net.lecousin.framework.collections.SortedListTree;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.io.FileSystemUtil;
import net.lecousin.framework.thread.RunnableWithData;
import net.lecousin.framework.ui.eclipse.EclipseImages;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.dialog.FlatPopupMenu;
import net.lecousin.framework.ui.eclipse.dialog.MyDialog;
import net.lecousin.framework.ui.eclipse.dialog.MyDialog.Orientation;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;

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
		List<ActionProvider> listProviders = ActionProviderManager.getProviders("*");
		boolean menuAdded = false;
		for (ActionProvider provider : listProviders)
			for (Action action : provider.getActions(data)) {
				if (action.getType().equals(Type.LIST_MANAGEMENT) && !addDataListManagementActions)
					continue;
				new FlatPopupMenu.Menu(menu, action.getText(), action.getIcon(), action.isSame(defaultAction), false, action);
				menuAdded = true;
			}
		if (menuAdded)
			new FlatPopupMenu.Separator(menu);
		if (data.size() == 1) {
			Data d = data.get(0);
			List<DataSource> sources = d.getSources();
			if (!sources.isEmpty()) {
				String ext = FileSystemUtil.getFileNameExtension(sources.get(0).getFileName());
				Program p = Program.findProgram(ext);
				if (p != null) {
					new FlatPopupMenu.Menu(menu, Local.Open_with_default_application.toString(), p.getImageData() != null ? new Image(MyDialog.getPlatformShell().getDisplay(), p.getImageData()) : null, false, false, new RunnableWithData<Pair<Data,Program>>(new Pair<Data,Program>(d,p)) {
						public void run() {
							for (DataSource s : data().getValue1().getSources()) {
								try {
									URI uri = s.ensurePresenceAndGetURI();
									File file = new File(uri);
									if (file.exists())
										data().getValue2().execute(file.getAbsolutePath());
								} catch (Throwable t) {
									continue;
								}
							}
						}
					});
				}
				new FlatPopupMenu.Menu(menu, Local.Open_with+"...", EclipseImages.getImage(EclipsePlugin.ID, "icons/application.gif"), false, false, new RunnableWithData<Data>(d) {
					public void run() {
						OpenWithDialog dlg = new OpenWithDialog(MyDialog.getPlatformShell(), data());
						dlg.open();
					}
				});
			}
			new FlatPopupMenu.Menu(menu, Local.Data_information.toString(), SharedImages.getImage(SharedImages.icons.x16.basic.INFO), false, false, new RunnableWithData<Data>(d) {
				public void run() {
					DataLinkPopup.open(data(), null, Orientation.BOTTOM);
				}
			});
			new FlatPopupMenu.Menu(menu, Local.Sources_information.toString(), SharedImages.getImage(SharedImages.icons.x16.file.FILE), false, false, new RunnableWithData<Data>(data.get(0)) {
				public void run() {
					DataListActions.sourcesInfo(data());
				}
			});
		}
		if (addDataListManagementActions) {
			if (data.size() == 1)
				new FlatPopupMenu.Separator(menu);
			String str = Local.Delete.toString();
			if (data.size() > 1) str = str + " " + data.size() + " " + Local.data__s;
			new FlatPopupMenu.Menu(menu, str, SharedImages.getImage(SharedImages.icons.x16.basic.DEL), false, false, new RunnableWithData<List<Data>>(data) {
				public void run() {
					DataListActions.delete(data());
				}
			});
			str = Local.Refresh.toString();
			if (data.size() > 1) str = str + " " + data.size() + " " + Local.data__s;
			new FlatPopupMenu.Menu(menu, str, SharedImages.getImage(SharedImages.icons.x16.basic.REFRESH), false, false, new RunnableWithData<List<Data>>(data) {
				public void run() {
					DataListActions.refresh(data());
				}
			});
			if (data.size() == 2) {
				new FlatPopupMenu.Menu(menu, Local.Merge_the_two_data_to+" "+data.get(0).getName(), SharedImages.getImage(SharedImages.icons.x16.file.FILE_TO_FILE), false, false, new RunnableWithData<List<Data>>(data) {
					public void run() {
						data().get(0).merge(data().get(1), MyDialog.getPlatformShell());
						data().get(1).remove();
					}
				});
				new FlatPopupMenu.Menu(menu, Local.Merge_the_two_data_to+" "+data.get(1).getName(), SharedImages.getImage(SharedImages.icons.x16.file.FILE_TO_FILE), false, false, new RunnableWithData<List<Data>>(data) {
					public void run() {
						data().get(1).merge(data().get(0), MyDialog.getPlatformShell());
						data().get(0).remove();
					}
				});
			}
		}
	}

	public static void fillBar(Composite bar, Data data, boolean addDataListManagementActions) {
		SortedListTree<ActionProvider> providers = new SortedListTree<ActionProvider>(new Comparator<ActionProvider>() {
			public int compare(ActionProvider a1, ActionProvider a2) {
				return a1.getPriority() - a2.getPriority();
			}
		});
		List<ActionProvider> listProviders = ActionProviderManager.getProviders(data.getContentType().getID());
		if (listProviders != null)
			providers.addAll(listProviders);
		listProviders = ActionProviderManager.getProviders("*");
		providers.addAll(listProviders);
		boolean menuAdded = false;
		for (ActionProvider provider : providers)
			for (Action action : provider.getActions(CollectionUtil.single_element_list(data))) {
				if (action.getType().equals(Type.LIST_MANAGEMENT) && !addDataListManagementActions)
					continue;
				UIUtil.newImageButton(bar, action.getIcon(), new Listener<Action>() {
					public void fire(Action event) {
						event.run();
					}
				}, action).setToolTipText(action.getText());
				menuAdded = true;
			}
		if (menuAdded)
			UIUtil.newSeparator(bar, false, false);
		List<DataSource> sources = data.getSources();
		if (!sources.isEmpty()) {
			String ext = FileSystemUtil.getFileNameExtension(sources.get(0).getFileName());
			Program p = Program.findProgram(ext);
			if (p != null) {
				UIUtil.newImageButton(bar, p.getImageData() != null ? new Image(MyDialog.getPlatformShell().getDisplay(), p.getImageData()) : EclipseImages.getImage(EclipsePlugin.ID, "icons/application.gif"), new Listener<Pair<Data,Program>>() {
					public void fire(Pair<Data,Program> event) {
						for (DataSource s : event.getValue1().getSources()) {
							try {
								URI uri = s.ensurePresenceAndGetURI();
								File file = new File(uri);
								if (file.exists())
									event.getValue2().execute(file.getAbsolutePath());
							} catch (Throwable t) {
								continue;
							}
						}
					}
				}, new Pair<Data,Program>(data,p)).setToolTipText(Local.Open_with_default_application.toString());
			}
			UIUtil.newImageButton(bar, EclipseImages.getImage(EclipsePlugin.ID, "icons/application.gif"), new Listener<Data>() {
				public void fire(Data event) {
					OpenWithDialog dlg = new OpenWithDialog(MyDialog.getPlatformShell(), event);
					dlg.open();
				}
			}, data).setToolTipText(Local.Open_with.toString());
		}
		UIUtil.newImageButton(bar, SharedImages.getImage(SharedImages.icons.x16.basic.INFO), new Listener<Data>() {
			public void fire(Data event) {
				DataLinkPopup.open(event, null, null);
			}
		}, data).setToolTipText(Local.Data_information.toString());
		UIUtil.newImageButton(bar, SharedImages.getImage(SharedImages.icons.x16.file.FILE), new Listener<Data>() {
			public void fire(Data event) {
				DataListActions.sourcesInfo(event);
			}
		}, data).setToolTipText(Local.Sources_information.toString());
		if (addDataListManagementActions) {
			UIUtil.newSeparator(bar, false, false);
			String str = Local.Delete.toString();
			UIUtil.newImageButton(bar, SharedImages.getImage(SharedImages.icons.x16.basic.DEL), new Listener<Data>() {
				public void fire(Data event) {
					DataListActions.delete(CollectionUtil.single_element_list(event));
				}
			}, data).setToolTipText(str);
			str = Local.Refresh.toString();
			UIUtil.newImageButton(bar, SharedImages.getImage(SharedImages.icons.x16.basic.REFRESH), new Listener<Data>() {
				public void fire(Data event) {
					DataListActions.refresh(CollectionUtil.single_element_list(event));
				}
			}, data).setToolTipText(str);
		}
	}
	
}
