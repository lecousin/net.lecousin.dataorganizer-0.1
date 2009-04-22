package net.lecousin.dataorganizer.ui.dialog;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.config.Applications;
import net.lecousin.dataorganizer.core.config.Applications.RunningApp;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.framework.collections.CollectionUtil;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.thread.RunnableWithData;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.UIControlUtil;
import net.lecousin.framework.ui.eclipse.control.list.LCContentProvider;
import net.lecousin.framework.ui.eclipse.control.list.LCTable;
import net.lecousin.framework.ui.eclipse.control.list.LCTable.ColumnProvider;
import net.lecousin.framework.ui.eclipse.control.list.LCTable.ColumnProviderText;
import net.lecousin.framework.ui.eclipse.control.list.LCTable.TableConfig;
import net.lecousin.framework.ui.eclipse.dialog.FlatDialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class OpenWithDialog extends FlatDialog {

	public OpenWithDialog(Shell parent, Data data) {
		super(parent, Local.Open_with.toString(), false, false);
		this.data = data;
		setMinWidth(300);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void createContent(Composite container) {
		UIUtil.gridLayout(container, 1);
		header = UIUtil.newGridComposite(container, 0, 0, 2);
		UIUtil.newLabel(header, Local.Loading+"...");
		UIUtil.newSeparator(container, true, true);
		TableConfig config = new TableConfig();
		config.fixedRowHeight = 18;
		config.multiSelection = false;
		config.sortable = true;
		Composite panel = UIUtil.newGridComposite(container, 0, 0, 2);
		UIUtil.gridDataHorizFill(panel);
		UIUtil.newLabel(panel, Local.Filter_applications.toString());
		filterText = UIUtil.newText(panel, "", new FilterListener());
		filterText.setLayoutData(UIUtil.gridDataHoriz(1, true));
		filterText.setEnabled(false);
		table = new LCTable<MyProgram>(container, new LCContentProvider<MyProgram>() {
			public Iterable<MyProgram> getElements() { return CollectionUtil.single_element_collection((MyProgram)new LoadingProgram()); }
		}, new ColumnProvider[] { new ColumnProviderText<MyProgram>() {
			public String getTitle() { return Local.Program.toString(); }
			public int getDefaultWidth() { return 280; }
			public int getAlignment() { return SWT.LEFT; }
			public String getText(MyProgram element) { return element.getName(); }
			public Font getFont(MyProgram element) { return null; }
			public Image getImage(MyProgram element) { return element.getIcon(); }
			public int compare(MyProgram element1, String text1, MyProgram element2, String text2) {
				return text1.compareToIgnoreCase(text2);
			}
		}}, config);
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		gd.heightHint = 400;
		table.getControl().setLayoutData(gd);
		UIUtil.newButton(container, Local.Browse+"...", new Listener<Object>() {
			public void fire(Object event) {
				FileDialog dlg = new FileDialog(getShell(), SWT.OPEN);
				dlg.setText(Local.Select_an_application.toString());
				String path = dlg.open();
				if (path == null) return;
				Applications.getInstance().launch(path, data);
				close();
			}
		}, null);
		Thread t = new Thread(new Runnable() {
			public void run() {
				loadApps();
			}
		});
		t.start();
		table.addDoubleClickListener(new Listener<MyProgram>() {
			public void fire(MyProgram event) {
				if (event instanceof LoadingProgram) return;
				Applications.getInstance().launch(((RealProgram)event).getProgram(), data);
				close();
			}
		});
	}
	
	private Data data;
	private Composite header;
	private LCTable<MyProgram> table;
	private Text filterText;
	
	private static interface MyProgram {
		public Program getProgram();
		public String getName();
		public Image getIcon();
	}
	
	private static class LoadingProgram implements MyProgram {
		public Program getProgram() { return null; }
		public String getName() { return Local.Loading+"..."; }
		public Image getIcon() { return SharedImages.getImage(SharedImages.icons.x16.basic.WAIT_ROUND_BAR); }
	}
	private class RealProgram implements MyProgram {
		public RealProgram(Program p) { this.p = p; }
		private Program p;
		public Program getProgram() { return p; }
		public String getName() { return p.getName(); }
		public Image getIcon() { ImageData d = p.getImageData(); return d != null ? new Image(getShell().getDisplay(), d) : null; }
	}
	
	private void loadApps() {
		List<RunningApp> list = Applications.getInstance().getApplications(data);
		
		if (header.isDisposed()) return;
		getShell().getDisplay().asyncExec(new RunnableWithData<List<RunningApp>>(list) {
			public void run() {
				if (header.isDisposed()) return;
				UIControlUtil.clear(header);
				for (RunningApp app : data()) {
					UIUtil.newImageTextButton(header, app.getIcon(), app.getName(), new Listener<RunningApp>() {
						public void fire(RunningApp event) {
							Applications.getInstance().launch(event, data);
							close();
						}
					}, app).setData(app);
					UIUtil.newImageButton(header, SharedImages.getImage(SharedImages.icons.x16.basic.DEL), new Listener<RunningApp>() {
						public void fire(RunningApp event) {
							Applications.getInstance().remove(event, data);
							for (Control c : header.getChildren())
								if (c.getData() == event)
									c.dispose();
						}
					}, app).setData(app);
				}
				table.clear();
				for (Program p : Applications.getInstance().getAllPrograms())
					table.add(new RealProgram(p));
				filterText.setEnabled(true);
				if (data().isEmpty()) {
					GridData gd = new GridData();
					gd.heightHint = 0;
					gd.widthHint = 0;
					header.setLayoutData(gd);
				} else
					UIControlUtil.resize(header);
				resize();
			}
		});
	}
	
	public void open() {
		super.open(true);
	}

	private class FilterListener implements ModifyListener {
		public void modifyText(ModifyEvent e) {
			String[] strs = ((Text)e.widget).getText().split(" ");
			List<String> words = new LinkedList<String>();
			for (String s : strs)
				if (s.trim().length() > 0)
					words.add(s);
			Program[] allPrograms = Applications.getInstance().getAllPrograms();
			List<MyProgram> programs = new ArrayList<MyProgram>(allPrograms.length);
			for (Program p : allPrograms)
				if (filter(words, p))
					programs.add(new RealProgram(p));
			table.setContent(programs, true);
		}
	}
	private boolean filter(List<String> words, Program p) {
		for (String word : words)
			if (p.getName().toLowerCase().indexOf(word.toLowerCase()) < 0)
				return false;
		return true;
	}
}
