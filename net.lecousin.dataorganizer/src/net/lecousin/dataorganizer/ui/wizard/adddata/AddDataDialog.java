package net.lecousin.dataorganizer.ui.wizard.adddata;

import java.net.URLDecoder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.database.VirtualData;
import net.lecousin.dataorganizer.internal.EclipsePlugin;
import net.lecousin.dataorganizer.ui.dialog.DataLinkPopup;
import net.lecousin.dataorganizer.ui.wizard.adddata.AddData_Page.Result;
import net.lecousin.framework.Pair;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.files.TypedFile;
import net.lecousin.framework.io.FileSystemUtil;
import net.lecousin.framework.thread.RunnableWithData;
import net.lecousin.framework.ui.eclipse.EclipseImages;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.buttonbar.OkCancelButtonsPanel;
import net.lecousin.framework.ui.eclipse.control.list.LCTableWithControls;
import net.lecousin.framework.ui.eclipse.control.list.LCTableWithControls.Provider_SimpleText;
import net.lecousin.framework.ui.eclipse.control.text.lcml.LCMLText;
import net.lecousin.framework.ui.eclipse.dialog.FlatPagedListDialog;
import net.lecousin.framework.ui.eclipse.dialog.MyDialog;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public class AddDataDialog extends MyDialog {

	public AddDataDialog(Shell shell, Result result) {
		super(shell);
		this.result = result;
		setMaxWidth(800);
	}
	
	private Result result;
	
	@Override
	protected Composite createControl(Composite container) {
		Composite panel = UIUtil.newGridComposite(container, 2, 2, 1);
		
		Composite header = UIUtil.newGridComposite(panel, 0, 0, 2);
		UIUtil.gridDataHorizFill(header);
		UIUtil.newImage(header, EclipseImages.getImage(EclipsePlugin.ID, "cd-dvd.jpg"));
		LCMLText text = new LCMLText(header, false, false);
		text.setLayoutData(UIUtil.gridDataHoriz(1, true));
		text.setText(Local.process(Local.MESSAGE_Add_Data, result.toAdd.size()));
		
		LCTableWithControls<VirtualData> table = new LCTableWithControls<VirtualData>(panel, null, new Provider_SimpleText<VirtualData>(result.toAdd, Local.Name.toString(), SWT.LEFT, true) {
			public void createElementDetailsControl(Composite parent, VirtualData element) {
				UIUtil.gridLayout(parent, 1);
				LCMLText text = new LCMLText(parent, false, false);
				text.setText(Local.Details+": <a href=\"link\">"+element.getName()+"</a>");
				text.setLayoutData(UIUtil.gridDataHoriz(1, true));
				text.addLinkListener("link", new RunnableWithData<Pair<Composite,VirtualData>>(new Pair<Composite,VirtualData>(parent, element)) {
					public void run() {
						DataLinkPopup.open(data().getValue2(), data().getValue1(), Orientation.TOP_BOTTOM);
					}
				});
			}
			@Override
			public Image getImage(VirtualData element) {
				return element.getContentType().getIcon();
			}
			@Override
			public String getText(VirtualData element) {
				return element.getName();
			}
		}, false, false, true, false);
		UIUtil.gridDataHorizFill(table);
		table.removeRequested().addListener(new Listener<LCTableWithControls<VirtualData>>() {
			public void fire(LCTableWithControls<VirtualData> event) {
				List<VirtualData> list = event.getSelection();
				if (list == null) return;
				for (VirtualData d : list) {
					event.remove(d);
					result.toAdd.remove(d);
				}
			}
		});
		
		text = new LCMLText(panel, false, false);
		text.setLayoutData(UIUtil.gridDataHoriz(1, true));
		StringBuilder str = new StringBuilder();
		if (!result.notDetectedNotTypesFiles.isEmpty() || !result.notDetectedTypesFiles.isEmpty()) {
			str.append(Local.MESSAGE_Not_Detected_Header);
			if (!result.notDetectedTypesFiles.isEmpty()) {
				str.append("<p marginTop=5>");
				str.append(Local.process(Local.MESSAGE_Not_Detected_Typed_Files, result.notDetectedTypesFiles.size()));
				str.append("</p>");
			}
			if (!result.notDetectedNotTypesFiles.isEmpty()) {
				str.append("<p marginTop=5>");
				str.append(Local.process(Local.MESSAGE_Not_Detected_Not_Typed_Files, result.notDetectedNotTypesFiles.size()));
				str.append("</p>");
			}
		}
		str.append(Local.MESSAGE_Not_Detected_Footer);
		text.setText(str.toString());
		text.addLinkListener("typed", new Runnable() {
			@SuppressWarnings("unchecked")
			public void run() {
	    		Set<String> extensions = new HashSet<String>();
	    		if (!result.notDetectedTypesFiles.isEmpty())
	    			for (Pair<IFileStore,TypedFile> file : result.notDetectedTypesFiles)
	    				extensions.add(FileSystemUtil.getFileNameExtension(file.getValue1().getName()).toLowerCase());
				FlatPagedListDialog.Filter<Pair<IFileStore,TypedFile>>[] filters = new FlatPagedListDialog.Filter[1];
				filters[0] = new FlatPagedListDialog.FilterListPossibilities<Pair<IFileStore,TypedFile>, String>(extensions) {
					public String getName() {
						return Local.Extensions.toString();
					}
					@Override
					protected String getName(String possibility) {
						return possibility;
					}
					@Override
					protected boolean accept(Pair<IFileStore,TypedFile> element, String possibility) {
						return element.getValue1().getName().endsWith(possibility);
					}
				};
				FlatPagedListDialog<Pair<IFileStore,TypedFile>> dlg = new FlatPagedListDialog<Pair<IFileStore,TypedFile>>(getShell(), Local.Files_not_detected.toString(), result.notDetectedTypesFiles, 20, new FlatPagedListDialog.TextProvider<Pair<IFileStore,TypedFile>>() {
					@Override
					protected String getText(Pair<IFileStore,TypedFile> element) {
						return URLDecoder.decode(element.getValue1().toURI().toString());
					}
				}, filters);
				dlg.openProgressive(null, OrientationY.BOTTOM, false);
			}
		});
		text.addLinkListener("not_typed", new Runnable() {
			@SuppressWarnings("unchecked")
			public void run() {
	    		Set<String> extensions = new HashSet<String>();
	    		if (!result.notDetectedNotTypesFiles.isEmpty())
	    			for (IFileStore file : result.notDetectedNotTypesFiles)
	    				extensions.add(FileSystemUtil.getFileNameExtension(file.getName()).toLowerCase());
				FlatPagedListDialog.Filter<IFileStore>[] filters = new FlatPagedListDialog.Filter[1];
				filters[0] = new FlatPagedListDialog.FilterListPossibilities<IFileStore, String>(extensions) {
					public String getName() {
						return Local.Extensions.toString();
					}
					@Override
					protected String getName(String possibility) {
						return possibility;
					}
					@Override
					protected boolean accept(IFileStore element, String possibility) {
						return element.getName().endsWith(possibility);
					}
				};
				FlatPagedListDialog<IFileStore> dlg = new FlatPagedListDialog<IFileStore>(getShell(), Local.Files_not_detected.toString(), result.notDetectedNotTypesFiles, 20, new FlatPagedListDialog.TextProvider<IFileStore>() {
					@Override
					protected String getText(IFileStore element) {
						return URLDecoder.decode(element.toURI().toString());
					}
				}, filters);
				dlg.openProgressive(null, OrientationY.BOTTOM, false);
			}
		});
		
		new OkCancelButtonsPanel(panel, true) {
			@Override
			protected boolean handleOk() {
				ok = true;
				return true;
			}
			@Override
			protected boolean handleCancel() {
				ok = false;
				return true;
			}
		}.centerAndFillInGrid();

		return panel;
	}
	
	private boolean ok = false;
	
	public boolean open() {
		super.open(Local.Add_data.toString(), MyDialog.FLAGS_MODAL_DIALOG);
		return ok;
	}
}
