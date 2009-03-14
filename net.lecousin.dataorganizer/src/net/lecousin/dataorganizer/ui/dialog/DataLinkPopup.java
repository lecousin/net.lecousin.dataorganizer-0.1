package net.lecousin.dataorganizer.ui.dialog;

import java.util.ArrayList;
import java.util.List;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.DataOrganizer;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.content.ContentType;
import net.lecousin.dataorganizer.core.database.info.InfoRetriever;
import net.lecousin.dataorganizer.core.database.info.Info.DataLink;
import net.lecousin.dataorganizer.ui.dataoverview.OverviewPanel;
import net.lecousin.framework.Pair;
import net.lecousin.framework.Triple;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.UIControlUtil;
import net.lecousin.framework.ui.eclipse.dialog.FlatPopupMenu;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class DataLinkPopup {

	private DataLinkPopup() {}
	
	public static void open(String contentTypeID, List<DataLink> links, Control ctrl, FlatPopupMenu.Orientation orientation) {
		ContentType type = ContentType.getContentType(contentTypeID);
		String linkName = links.get(0).name;
		FlatPopupMenu dlg = new FlatPopupMenu(ctrl, type.getName() + ": " + linkName, true, false, false, true);
		List<Pair<String,String>> ids = new ArrayList<Pair<String,String>>(links.size());
		for (DataLink link : links)
			ids.add(new Pair<String,String>(link.source, link.id));
		Data data = DataOrganizer.database().getDataFromInfoSourceIDs(type, ids);
		if (data == null)
			createNoData(type, links, dlg);
		else
			createData(data, dlg);
		
		dlg.setMaxWidth(700);
		dlg.setMaxHeight(400);
		dlg.show(ctrl, orientation, true);
	}
	
	public static void open(Data data, Control ctrl, FlatPopupMenu.Orientation orientation) {
		FlatPopupMenu dlg = new FlatPopupMenu(ctrl, data.getName(), true, false, false, true);
		createData(data, dlg);
		dlg.setMaxWidth(700);
		dlg.setMaxHeight(400);
		dlg.show(ctrl, orientation, true);
	}
	
	private static void createNoData(ContentType type, List<DataLink> links, FlatPopupMenu dlg) {
		Composite panel = new Composite(dlg.getControl(), SWT.NONE);
		UIUtil.gridLayout(panel, 2);
		UIUtil.newLabel(panel, type.getName() + " " + links.get(0).name + " " + Local.is_not_in_your_database+".");
		UIUtil.newButton(panel, Local.Create_and_retrieve_information.toString(), new Listener<Triple<ContentType,List<DataLink>,FlatPopupMenu>>() {
			public void fire(Triple<ContentType, List<DataLink>, FlatPopupMenu> event) {
				event.getValue3().setAllowClose(false);
				Data data = InfoRetriever.retrieve(event.getValue1(), event.getValue2());
				if (data != null) {
					UIControlUtil.clear(event.getValue3().getControl());
					createData(data, event.getValue3());
					event.getValue3().getControl().layout(true, true);
					event.getValue3().resize();
				}
				event.getValue3().setAllowClose(true);
			}
		}, new Triple<ContentType,List<DataLink>,FlatPopupMenu>(type, links, dlg));
	}
	
	private static void createData(Data data, FlatPopupMenu dlg) {
		OverviewPanel panel = new OverviewPanel(dlg.getControl());
		panel.refresh(data);
	}
	
}
