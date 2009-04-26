package net.lecousin.dataorganizer.ui.dialog;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.source.DataSource;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.strings.StringUtil;
import net.lecousin.framework.thread.RunnableWithData;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.buttonbar.CloseButtonPanel;
import net.lecousin.framework.ui.eclipse.control.list.LCContentProvider;
import net.lecousin.framework.ui.eclipse.control.list.LCTable;
import net.lecousin.framework.ui.eclipse.control.list.LCTable.ColumnProvider;
import net.lecousin.framework.ui.eclipse.control.list.LCTable.ColumnProviderText;
import net.lecousin.framework.ui.eclipse.control.list.LCTable.LCTableProvider;
import net.lecousin.framework.ui.eclipse.control.list.LCTable.TableConfig;
import net.lecousin.framework.ui.eclipse.dialog.FlatDialog;
import net.lecousin.framework.ui.eclipse.dialog.FlatPopupMenu;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

public class SourcesInfoDialog extends FlatDialog {

	public static void open(Data data) {
		new SourcesInfoDialog(data).open(true);
	}
	
	private SourcesInfoDialog(Data data) {
		super(getPlatformShell(), Local.Sources_information.toString(), false, false);
		this.data = data;
		setMinHeight(300);
		setMinWidth(700);
	}
	
	private Data data;
	
	@Override
	protected void createContent(Composite container) {
		config = new TableConfig();
		config.fixedRowHeight = 18;
		config.sortable = true;
		
		UIUtil.gridLayout(container, 2);
		UIUtil.newLabel(container, Local.Data+":", true, false);
		UIUtil.newLabel(container, data.getName());
		UIUtil.gridDataHorizFill(UIUtil.newLabel(container, Local.Files+":", true, false));
		table = new LCTable<DataSource>(container, provider);
		table.getControl().setLayoutData(UIUtil.gridData(2, true, 1, true));
		new CloseButtonPanel(container, true).centerAndFillInGrid();
		
		table.addRightClickListener(new Listener<DataSource>() {
			public void fire(DataSource event) {
				FlatPopupMenu menu = new FlatPopupMenu(null, Local.File.toString(), false, true, false, false);
				new FlatPopupMenu.Menu(menu, Local.Remove_link.toString(), SharedImages.getImage(SharedImages.icons.x16.basic.DEL), false, false, new RunnableWithData<DataSource>(event) {
					public void run() {
						data.removeSource(data());
						table.refresh(true);
					}
				});
				menu.show(null, Orientation.BOTTOM, true);
			}
		});
	}
	
	private LCTable<DataSource> table;
	private LCTableProvider<DataSource> provider = new LCTableProvider<DataSource>() {
		public TableConfig getConfig() { return config; }
		public ColumnProvider<DataSource>[] getColumns() { return columns; }
		public LCContentProvider<DataSource> getContentProvider() { return contentProvider; }
	};
	private TableConfig config;
	@SuppressWarnings("unchecked")
	private ColumnProvider<DataSource>[] columns = new ColumnProvider[] {
		new ColumnProviderText<DataSource>() {
			public String getTitle() { return Local.Name.toString(); }
			public int getAlignment() { return SWT.LEFT; }
			public int getDefaultWidth() { return 250; }
			public String getText(DataSource element) { return element.getFileName(); }
			public Font getFont(DataSource element) { return null; }
			public Image getImage(DataSource element) { return element.getIcon(); }
			public int compare(DataSource element1, String text1, DataSource element2, String text2) {
				return text1.compareTo(text2);
			}
		},
		new ColumnProviderText<DataSource>() {
			public String getTitle() { return Local.Path.toString(); }
			public int getAlignment() { return SWT.LEFT; }
			public int getDefaultWidth() { return 300; }
			public String getText(DataSource element) { return element.getPathToDisplay(); }
			public Font getFont(DataSource element) { return null; }
			public Image getImage(DataSource element) { return null; }
			public int compare(DataSource element1, String text1, DataSource element2, String text2) {
				return text1.compareTo(text2);
			}
		},
		new ColumnProviderText<DataSource>() {
			public String getTitle() { return Local.Size.toString(); }
			public int getAlignment() { return SWT.RIGHT; }
			public int getDefaultWidth() { return 75; }
			public String getText(DataSource element) { long size = element.getSize(); return size >= 0 ? StringUtil.sizeString(size) : "?"; }
			public Font getFont(DataSource element) { return null; }
			public Image getImage(DataSource element) { return null; }
			public int compare(DataSource element1, String text1, DataSource element2, String text2) {
				return (int)(element1.getSize() - element2.getSize());
			}
		},
	};
	private LCContentProvider<DataSource> contentProvider = new LCContentProvider<DataSource>() {
		public Iterable<DataSource> getElements() { return data.getSources(); }
	};
}
