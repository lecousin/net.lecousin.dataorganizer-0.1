package net.lecousin.dataorganizer.mediaplayer;

import java.net.URI;
import java.util.List;

import net.lecousin.dataorganizer.core.DataOrganizer;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.source.DataSource;
import net.lecousin.dataorganizer.ui.DataOrganizerDND;
import net.lecousin.dataorganizer.ui.control.DataImageControl;
import net.lecousin.dataorganizer.ui.control.RateControl;
import net.lecousin.dataorganizer.ui.dialog.DataLinkPopup;
import net.lecousin.framework.Pair;
import net.lecousin.framework.Triple;
import net.lecousin.framework.collections.SelfMapUniqueLong;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.event.Event.ListenerData;
import net.lecousin.framework.media.ui.PlayList;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.BorderStyle;
import net.lecousin.framework.ui.eclipse.control.Separator;
import net.lecousin.framework.ui.eclipse.control.UIControlUtil;
import net.lecousin.framework.ui.eclipse.dialog.FlatPopupMenu;
import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.forms.events.HyperlinkEvent;

public class DataOrganizerPlayList extends PlayList {

	public DataOrganizerPlayList(Composite parent, DataOrganizerMediaPlayerControl player) {
		super(parent, player);
	}
	
	private Composite customizeSection;
	private Composite infoPanel;
	private Composite openedPanel;
	
	@Override
	protected boolean createCustomizePanel(Composite parent) {
		customizeSection = parent;
		UIUtil.gridLayout(parent, 1, 0, 0, 0, 0);
		parent.setBackground(ColorUtil.getBlack());
		infoPanel = UIUtil.newComposite(parent);
		UIUtil.gridLayout(infoPanel, 2, 0, 0, 1, 0).marginBottom = 1;
		infoPanel.setLayoutData(UIUtil.gridData(1, true, 1, false));
		Separator sep = new Separator(parent, true, Separator.Style.SIMPLE_LINE, 0);
		sep.setForeground(getPlayer().getControlSeparatorColor());
		UIUtil.gridDataHorizFill(sep);
		openedPanel = UIUtil.newGridComposite(parent, 0, 0, 1, 0, 0);
		GridData gd = UIUtil.gridData(1, true, 1, false);
		gd.exclude = true;
		openedPanel.setLayoutData(gd);
		Label label = new Label(openedPanel, SWT.NONE);
		label.setText(Local.Opened_data.toString());
		UIControlUtil.increaseFontSize(label, -1);
		label.setForeground(ColorUtil.getWhite());
		label.setBackground(ColorUtil.get(40, 40, 100));
		label.setLayoutData(UIUtil.gridDataHoriz(1, true));
		sep = new Separator(parent, true, Separator.Style.SIMPLE_LINE, 0);
		sep.setForeground(getPlayer().getControlSeparatorColor());
		UIUtil.gridDataHorizFill(sep);
		
		listenerOpened = new Listener<Triple<Data,List<Long>,Boolean>>() {
			public void fire(Triple<Data,java.util.List<Long>,Boolean> event) {
				if (event.getValue3()) return;
				boolean changed = false;
				int remaining = 0;
				for (Control c : openedPanel.getChildren()) {
					if (!(c instanceof OpenedRow)) continue;
					OpenedRow row = (OpenedRow)c;
					remaining++;
					if (row.data.getValue1() != event.getValue1()) continue;
					if (!event.getValue2().contains(row.data.getValue2())) continue;
					remaining--;
					row.dispose();
					changed = true;
				}
				if (changed) {
					if (remaining == 0) {
						GridData gd = (GridData)openedPanel.getLayoutData();
						gd.exclude = true;
					}
					UIControlUtil.resize(openedPanel);
					customizeSection.getParent().layout(true, true);
				}
			}
		};		
		DataOrganizer.database().dataOpenedChanged.addListener(listenerOpened);
		openedPanel.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				DataOrganizer.database().dataOpenedChanged.removeListener(listenerOpened);
			}
		});
		
		return true;
	}
	private Listener<Triple<Data,List<Long>,Boolean>> listenerOpened;
	
	private void show(Data data) {
		UIControlUtil.clear(infoPanel);
		new DataImageControl(infoPanel, data, 64, 64);
		Composite panel = UIUtil.newGridComposite(infoPanel, 0, 0, 1);
		Label label = new Label(panel, SWT.NONE);
		label.setText(data.getName());
		label.setBackground(panel.getBackground());
		label.setForeground(ColorUtil.getYellow());
		RateControl rate = new RateControl(panel, data, true);
		BorderStyle.attach(rate, new BorderStyle.SimpleLine(ColorUtil.get(150, 150, 255), 1), 0);
		UIUtil.newLink(panel, Local.Show_details.toString(), new ListenerData<HyperlinkEvent,Data>(data) {
			public void fire(HyperlinkEvent event) {
				DataLinkPopup.open(data(), (Control)event.widget, FlatPopupMenu.Orientation.BOTTOM);
			}
		}, ColorUtil.get(150, 150, 255), ColorUtil.getWhite());
		UIUtil.resize(infoPanel);
		customizeSection.getParent().layout(true, true);
	}

	@Override
	protected TreeItem createCustomizedItem(Object media, int index) {
		if (media instanceof Data)
			return createItem((Data)media, index);
		return null;
	}
	
	private TreeItem createItem(Data data, int index) {
		TreeItem item = new TreeItem(getTree(), SWT.NONE, index);
		item.setText(data.getName());
		item.setImage(data.getContentType().getIcon());
		if (data.getSources().size() > 1) {
			for (DataSource source : data.getSources()) {
				URI uri = source.ensurePresenceAndGetURI();
				TreeItem subitem = new TreeItem(item, SWT.NONE);
				String name = uri.getPath();
				int i = name.lastIndexOf('/');
				if (i >= 0) name = name.substring(i+1);
				subitem.setText(name);
				subitem.setImage(data.getContentType().getIcon());
				subitem.setData(new DataSourceItem(data, source, uri));
				finalizeItem(subitem);
			}
		}
		return item;
	}
	
	@Override
	protected URI getURI(Object media) {
		if (media instanceof Data)
			return getURI((Data)media);
		else if (media instanceof DataSourceItem)
			return ((DataSourceItem)media).uri;
		return super.getURI(media);
	}
	
	private URI getURI(Data data) {
		if (data.getSources().size() == 1) {
			return data.getSources().get(0).ensurePresenceAndGetURI();
		}
		return null;
	}
	
	private SelfMapUniqueLong<Data> openedData = new SelfMapUniqueLong<Data>(5);
	@Override
	protected void startMedia(Object media, TreeItem item) {
		if (media instanceof Data)
			showAndOpenedData((Data)media);
		else if (media instanceof DataSourceItem)
			showAndOpenedData(((DataSourceItem)media).data);
		super.startMedia(media, item);
	}

	private void showAndOpenedData(Data data) {
		show(data);
		if (!openedData.contains(data)) {
			long time = data.opened();
			openedData.add(data);
			OpenedRow row = new OpenedRow(openedPanel, new Pair<Data,Long>(data,time));
			UIUtil.gridDataHorizFill(row);
			GridData gd = (GridData)openedPanel.getLayoutData();
			gd.exclude = false;
			UIControlUtil.resize(openedPanel);
			customizeSection.getParent().layout(true, true);
		}
	}	
	
	@Override
	protected List<Transfer> getDropTransfer() {
		List<Transfer> list = super.getDropTransfer();
		list.add(TextTransfer.getInstance());
		return list;
	}
	@Override
	protected void drop_dragEnter(DropTargetEvent event) {
		TransferData support = null;
		if (TextTransfer.getInstance().isSupportedType(event.currentDataType))
			support = event.currentDataType;
		else {
			for (TransferData d : event.dataTypes)
				if (TextTransfer.getInstance().isSupportedType(d)) {
					support = d;
					break;
				}
		}
		if (support != null) {
			event.currentDataType = support;
			event.detail = DND.DROP_LINK;
			return;
		}
		super.drop_dragEnter(event);
	}
	@Override
	protected void drop_drop(DropTargetEvent event, TreeItem item) {
		if (TextTransfer.getInstance().isSupportedType(event.currentDataType)) {
			String str = (String)event.data;
			if (DataOrganizerDND.isData(str)) {
				List<Data> list = DataOrganizerDND.getDataDNDFromString(str);
				for (Data data : list)
					if (item == null)
						add(data);
					else
						insert(data, getTree().indexOf(item));
				return;
			}
		}
		super.drop_drop(event, item);
	}
	
	private static class DataSourceItem {
		DataSourceItem(Data data, DataSource source, URI uri)
		{ this.data = data; this.source = source; this.uri = uri; }
		Data data;
		DataSource source;
		URI uri;
	}
	
	private static class OpenedRow extends Composite {
		OpenedRow(Composite parent, Pair<Data,Long> data) {
			super(parent, SWT.NONE);
			this.data = data;
			UIUtil.gridLayout(this, 3, 0, 0, 1, 0);
			Label label = UIUtil.newLabel(this, data.getValue1().getName());
			label.setLayoutData(UIUtil.gridDataHoriz(1, true));
			UIUtil.newImageButton(this, SharedImages.getImage(SharedImages.icons.x16.basic.VALIDATE), new Listener<Pair<Data,Long>>() {
				public void fire(Pair<Data,Long> event) {
					DataOrganizer.database().countDataOpened(event.getValue1(), event.getValue2());
				}
			}, data);
			UIUtil.newImageButton(this, SharedImages.getImage(SharedImages.icons.x16.basic.IGNORE), new Listener<Pair<Data,Long>>() {
				public void fire(Pair<Data,Long> event) {
					DataOrganizer.database().ignoreDataOpened(event.getValue1(), event.getValue2());
				}
			}, data);
		}
		Pair<Data,Long> data;
	}
}
