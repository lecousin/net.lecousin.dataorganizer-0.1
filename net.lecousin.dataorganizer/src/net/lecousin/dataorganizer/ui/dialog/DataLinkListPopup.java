package net.lecousin.dataorganizer.ui.dialog;

import java.util.ArrayList;
import java.util.List;

import net.lecousin.dataorganizer.core.DataOrganizer;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.content.ContentType;
import net.lecousin.dataorganizer.core.database.info.Info.DataLink;
import net.lecousin.dataorganizer.ui.control.DataImageControl;
import net.lecousin.dataorganizer.ui.control.DataLinkListPanel;
import net.lecousin.framework.Pair;
import net.lecousin.framework.Triple;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.event.Event.ListenerData;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.ImageAndTextButton;
import net.lecousin.framework.ui.eclipse.control.LabelButton;
import net.lecousin.framework.ui.eclipse.dialog.FlatPopupMenu;
import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;

public class DataLinkListPopup {

	private DataLinkListPopup() {}
	
	public static void open(String title, String contentTypeID, List<Pair<List<String>,List<DataLink>>> list, Control ctrl, FlatPopupMenu.Orientation orientation) {
		FlatPopupMenu dlg = new FlatPopupMenu(title, true, true, false, true);
		new DataLinkListPanel(dlg.getControl(), new ProviderLinkRole(list));
		dlg.show(ctrl, orientation, true);
	}
	
	private static class ProviderLinkRole implements DataLinkListPanel.ListProvider {
		ProviderLinkRole(List<Pair<List<String>,List<DataLink>>> list) { this.list = list; }
		private List<Pair<List<String>,List<DataLink>>> list;
		public String[] getTitles() { return new String[] { ""
			// TODO Auto-generated method stub
			return null;
		}
		public int getNbRows() {
			// TODO Auto-generated method stub
			return 0;
		}
		public Object[] getRow(int index) {
			// TODO Auto-generated method stub
			return null;
		}
	}
	/*
	private static class Panel extends Composite {
		private Image retrieveImage;
		private Image refreshImage;
		private Composite buttons;
		private ImageAndTextButton buttonRetrieveAll; 
		private ImageAndTextButton buttonRefreshAll; 
		public Panel(FlatPopupMenu dlg, String title, String contentTypeID, List<Pair<List<String>,List<DataLink>>> list) {
			super(dlg.getControl(), SWT.NONE);
			ContentType type = ContentType.getContentType(contentTypeID);
			GridLayout layout = UIUtil.gridLayout(this, 5);
			layout.horizontalSpacing = 0;
			layout.verticalSpacing = 0;
			GridData gd;
			retrieveImage = SharedImages.getImage(SharedImages.icons.x16.basic.IMPORT);
			refreshImage = SharedImages.getImage(SharedImages.icons.x16.basic.REFRESH);
			
			boolean hasRetrieve = false;
			boolean hasRefresh = false;
			List<Data> dataList = new ArrayList<Data>(list.size());
			for (Pair<List<String>,List<DataLink>> p : list) {
				List<Pair<String,String>> ids = new ArrayList<Pair<String,String>>(p.getValue2().size());
				for (DataLink link : p.getValue2())
					ids.add(new Pair<String,String>(link.source, link.id));
				Data data = DataOrganizer.database().getDataFromInfoSourceIDs(type, ids);
				dataList.add(data);
				if (data == null)
					hasRetrieve = true;
				else
					hasRefresh = true;
			}
			
			buttons = UIUtil.newGridComposite(this, 0, 0, hasRetrieve ? hasRefresh ? 2 : 1 : 1);
			((GridLayout)buttons.getLayout()).marginBottom = 3;
			gd = UIUtil.gridDataHorizFill(buttons);
			gd.exclude = !(hasRetrieve || hasRefresh);
			buttonRetrieveAll = new ImageAndTextButton(buttons, retrieveImage, "Import all missing", 0, 0);
			buttonRetrieveAll.addClickListener(new Listener<MouseEvent>() {
				public void fire(MouseEvent event) {
					// TODO
				}
			});
			gd = new GridData();
			gd.horizontalAlignment = SWT.CENTER;
			gd.exclude = !hasRetrieve;
			buttonRetrieveAll.setLayoutData(gd);
			buttonRefreshAll = new ImageAndTextButton(buttons, refreshImage, "Refresh all");
			buttonRefreshAll.addClickListener(new Listener<MouseEvent>() {
				public void fire(MouseEvent event) {
					// TODO
				}
			});
			gd = new GridData();
			gd.horizontalAlignment = SWT.CENTER;
			gd.exclude = !hasRefresh;
			buttonRefreshAll.setLayoutData(gd);
			
			
			int i = 0;
			for (Pair<List<String>,List<DataLink>> p : list) {
				Color bg = (i % 2) == 0 ? ColorUtil.getWhite() : ColorUtil.get(220, 220, 255);
				StringBuilder role = new StringBuilder();
				boolean first = true;
				for (String s : p.getValue1()) {
					if (first) first = false;
					else role.append("\r\n");
					role.append(s);
				}
				StringBuilder people = new StringBuilder();
				first = true;
				for (DataLink l : p.getValue2()) {
					if (first) first = false;
					else people.append("\r\n");
					people.append(l.name);
				}
				Data data = dataList.get(i);
				if (data == null) {
					Label label = UIUtil.newLabel(this, "");
					label.setBackground(bg);
					gd = new GridData();
					gd.horizontalAlignment = SWT.FILL;
					gd.verticalAlignment = SWT.FILL;
					gd.horizontalIndent = gd.verticalIndent = 0;
					label.setLayoutData(gd);
				} else {
					DataImageControl c = new DataImageControl(this, data, 64, 50);
					c.setBackground(bg);
					gd = new GridData();
					gd.horizontalAlignment = SWT.FILL;
					gd.verticalAlignment = SWT.FILL;
					gd.horizontalIndent = gd.verticalIndent = 0;
					c.setLayoutData(gd);
				}
				Label label = UIUtil.newLabel(this, role.toString());
				gd = new GridData();
				gd.horizontalAlignment = SWT.FILL;
				gd.verticalAlignment = SWT.FILL;
				gd.horizontalIndent = gd.verticalIndent = 0;
				label.setLayoutData(gd);
				label.setBackground(bg);
				label = UIUtil.newLabel(this, " "); // separation
				gd = new GridData();
				gd.horizontalAlignment = SWT.FILL;
				gd.verticalAlignment = SWT.FILL;
				gd.horizontalIndent = gd.verticalIndent = 0;
				label.setLayoutData(gd);
				label.setBackground(bg);
				Hyperlink link = UIUtil.newLinkSoftNetStyle(this, people.toString(), new ListenerData<HyperlinkEvent,Triple<String,List<DataLink>,FlatPopupMenu>>(new Triple<String,List<DataLink>,FlatPopupMenu>(contentTypeID, p.getValue2(), dlg)) {
					public void fire(HyperlinkEvent e) {
						data().getValue3().setAllowClose(false);
						DataLinkPopup.open(data().getValue1(), data().getValue2(), (Control)e.widget, FlatPopupMenu.Orientation.TOP_BOTTOM);
						data().getValue3().setAllowClose(true);
					}
				});
				link.setBackground(bg);
				gd = new GridData();
				gd.horizontalAlignment = SWT.FILL;
				gd.verticalAlignment = SWT.FILL;
				gd.horizontalIndent = gd.verticalIndent = 0;
				link.setLayoutData(gd);
				LabelButton button;
				if (data == null)
					button = UIUtil.newImageButton(this, retrieveImage, new Listener<List<DataLink>>() {
						public void fire(List<DataLink> event) {
							// TODO Auto-generated method stub
						}
					}, p.getValue2());
				else
					button = UIUtil.newImageButton(this, refreshImage, new Listener<List<DataLink>>() {
						public void fire(List<DataLink> event) {
							// TODO quand refresh Data, cherche les DataLink, pour en avoir d'autres...
							// TODO plus generallement, quand il faut, detecter aue 2 data sont les meme... merger...
							// TODO Auto-generated method stub
						}
					}, p.getValue2());
				button.setBackground(bg);
				gd = new GridData();
				gd.horizontalAlignment = SWT.FILL;
				gd.verticalAlignment = SWT.FILL;
				gd.horizontalIndent = gd.verticalIndent = 0;
				button.setLayoutData(gd);
	
				i++;
			}
		}
	}*/
}
