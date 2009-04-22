package net.lecousin.dataorganizer.datalist;

import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.core.DataOrganizer;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.ui.DataOrganizerDND;
import net.lecousin.framework.event.Event;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.list.LCContentProvider;
import net.lecousin.framework.ui.eclipse.control.list.LCTable;
import net.lecousin.framework.ui.eclipse.control.list.LCTable.ColumnProvider;
import net.lecousin.framework.ui.eclipse.control.list.LCTable.ColumnProviderText;
import net.lecousin.framework.ui.eclipse.control.list.LCTable.TableConfig;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

public class DataListControl extends Composite {

	public DataListControl(Composite parent, DataList list) {
		super(parent, SWT.NONE);
		UIUtil.gridLayout(this, 1);
		this.list = list;
		table = new LCTable<Data>(this, new ContentProvider(), getColumns(), getConfig());
		table.getControl().setLayoutData(UIUtil.gridData(1, true, 1, true));
		table.addAddElementEvent(dataAdded);
		table.addRemoveElementEvent(dataRemoved);
		table.addDropSupport(DND.DROP_LINK, new Transfer[] { TextTransfer.getInstance() }, new DropTargetListener() {
			public void dragEnter(DropTargetEvent event) {
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
				event.detail = DND.DROP_NONE;
			}
			public void dragLeave(DropTargetEvent event) {
			}
			public void dragOperationChanged(DropTargetEvent event) {
				dragEnter(event);
			}
			public void dragOver(DropTargetEvent event) {
			}
			public void drop(DropTargetEvent event) {
				if (!TextTransfer.getInstance().isSupportedType(event.currentDataType))
					return;
				String str = (String)event.data;
				if (!DataOrganizerDND.isData(str))
					return;
				List<Data> data = DataOrganizerDND.getDataDNDFromString(str);
				DataListControl.this.list.addData(data);
			}
			public void dropAccept(DropTargetEvent event) {
			}
		});
		list.dataAdded.addListener(new Listener<Long>() {
			public void fire(Long event) {
				Data d = DataOrganizer.database().get(event);
				if (d != null)
					dataAdded.fire(d);
			}
		});
		list.dataRemoved.addListener(new Listener<Long>() {
			public void fire(Long event) {
				Data d = DataOrganizer.database().get(event);
				if (d != null)
					dataRemoved.fire(d);
			}
		});
	}
	
	private DataList list;
	private LCTable<Data> table;
	private Event<Data> dataAdded = new Event<Data>();
	private Event<Data> dataRemoved = new Event<Data>();
	
	private class ContentProvider implements LCContentProvider<Data> {
		public Iterable<Data> getElements() {
			List<Data> result = new LinkedList<Data>();
			for (long id : list.getDataIDs()) {
				Data d = DataOrganizer.database().get(id);
				if (d != null)
					result.add(d);
			}
			return result;
		}
	}
	@SuppressWarnings("unchecked")
	private ColumnProvider<Data>[] getColumns() {
		return (ColumnProvider<Data>[]) new ColumnProvider[] {
			new ColumnName()	
		};
	}
	private class ColumnName implements ColumnProviderText<Data> {
		public String getTitle() { return Local.Name.toString(); }
		public int getDefaultWidth() { return 350; }
		public int getAlignment() { return SWT.LEFT; }
		public Font getFont(Data element) { return null; }
		public String getText(Data element) { return element.getName(); }
		public Image getImage(Data element) { return element.getContentType().getIcon(); }
		public int compare(Data element1, String text1, Data element2, String text2) { return text1.compareToIgnoreCase(text2); }
	}
	private TableConfig getConfig() {
		TableConfig cfg = new TableConfig();
		cfg.fixedRowHeight = 18;
		cfg.multiSelection = true;
		cfg.sortable = true;
		return cfg;
	}
}
