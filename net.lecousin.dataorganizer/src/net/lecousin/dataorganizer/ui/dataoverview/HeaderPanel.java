package net.lecousin.dataorganizer.ui.dataoverview;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.DataOrganizer;
import net.lecousin.dataorganizer.core.DataLabels.Label;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.ui.control.LabelTree;
import net.lecousin.dataorganizer.ui.datalist.DataListMenu;
import net.lecousin.framework.Pair;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.LabelButton;
import net.lecousin.framework.ui.eclipse.control.LabelItem;
import net.lecousin.framework.ui.eclipse.control.UIControlUtil;
import net.lecousin.framework.ui.eclipse.dialog.FlatPopupMenu;
import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class HeaderPanel extends Composite {

	public HeaderPanel(Composite parent) {
		super(parent, SWT.NONE);
		setBackground(parent.getBackground());
		RowLayout layout = new RowLayout(SWT.HORIZONTAL);
		layout.wrap = true;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.marginTop = layout.marginBottom = 0;
		layout.center = true;
		setLayout(layout);
		labelImage = UIUtil.newImage(this, SharedImages.getImage(SharedImages.icons.x16.basic.LABEL));
		labelTitle = UIUtil.newLabel(this, Local.Labels + ":", true, false);
		addButton = new LabelButton(this);
		addButton.setImage(SharedImages.getImage(SharedImages.icons.x16.basic.ADD));
		addButton.addClickListener(new Listener<MouseEvent>() {
			public void fire(MouseEvent event) {
				addLabel();
			}
		});
		DataOrganizer.labels().labelAssigned().addListener(labelChanged);
		DataOrganizer.labels().labelUnassigned().addListener(labelChanged);
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				DataOrganizer.labels().labelAssigned().removeListener(labelChanged);
				DataOrganizer.labels().labelUnassigned().removeListener(labelChanged);
			}
		});
	}
	
	private org.eclipse.swt.widgets.Label labelImage, labelTitle;
	private LabelButton addButton;
	private Listener<Pair<Label,Data>> labelChanged = new Listener<Pair<Label,Data>>() {
		public void fire(Pair<Label, Data> event) {
			if (event.getValue2() == data)
				refresh(data);
		}
	};
	
	public static final Color LABEL_COLOR = ColorUtil.get(150, 200, 150);
	
	private Data data;
	
	public void refresh(Data data) {
		this.data = data;
		for (Control c : getChildren())
			if (c != addButton && c != labelImage && c != labelTitle)
				c.dispose();
		
		boolean isInDB = (data.getDataBase() == DataOrganizer.database());
		labelImage.setVisible(isInDB);
		labelTitle.setVisible(isInDB);
		addButton.setVisible(isInDB);
		
		if (data != null) {
			DataListMenu.fillBar(this, data, true);
			boolean iconAdded = false;
			for (Control c : getChildren())
				if (c != addButton && c != labelImage && c != labelTitle) {
					c.moveAbove(labelImage);
					iconAdded = true;
				}
			if (iconAdded) {
				UIUtil.newSeparator(this, false, false).moveAbove(labelImage);
			}
		}
		
		if (isInDB)
			for (Label label : DataOrganizer.labels().getLabels(data)) {
				LabelItem item = new LabelItem(this, label.getName(), LABEL_COLOR, new Listener<LabelItem>() {
					public void fire(LabelItem event) {
						((Label)event.getData()).removeData(HeaderPanel.this.data);
						refresh(HeaderPanel.this.data);
					}
				}, true);
				item.moveAbove(addButton);
				item.setData(label);
			}
		
		UIControlUtil.autoresize(this);
	}
	
	private FlatPopupMenu addMenu = null;
	private void addLabel() {
		if (data == null) return;
		if (addMenu != null) {
			addMenu.close();
			addMenu = null;
			return;
		}
		addMenu = new FlatPopupMenu(addButton, Local.Select_the_labels.toString(), true, true, false, true);
		LabelTree tree = new LabelTree(addMenu.getControl(), false, SWT.NONE, DataOrganizer.labels().getLabels(data), null);
		tree.labelSelectionChanged.addListener(new Listener<Pair<Label,Boolean>>() {
			public void fire(Pair<Label,Boolean> event) {
				if (event.getValue2())
					event.getValue1().addData(data);
				else
					event.getValue1().removeData(data);
				refresh(data);
			}
		});
		tree.addTreeListener(new ITreeViewerListener() {
			public void treeCollapsed(TreeExpansionEvent event) {
				addMenu.resize();
			}
			public void treeExpanded(TreeExpansionEvent event) {
				addMenu.resize();
			}
		});
		GridData gd = new GridData();
		gd.heightHint = 200;
		tree.getTree().setLayoutData(gd);
		addMenu.show(addButton, FlatPopupMenu.Orientation.BOTTOM_RIGHT, true);
		addMenu = null;
		refresh(data);
	}
	
}
