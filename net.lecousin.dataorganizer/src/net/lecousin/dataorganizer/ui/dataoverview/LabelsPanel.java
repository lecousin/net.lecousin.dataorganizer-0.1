package net.lecousin.dataorganizer.ui.dataoverview;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.DataOrganizer;
import net.lecousin.dataorganizer.core.DataLabels.Label;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.ui.control.LabelTree;
import net.lecousin.framework.Pair;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.LabelButton;
import net.lecousin.framework.ui.eclipse.control.LabelItem;
import net.lecousin.framework.ui.eclipse.control.UIControlUtil;
import net.lecousin.framework.ui.eclipse.dialog.FlatPopupMenu;
import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class LabelsPanel extends Composite {

	public LabelsPanel(Composite parent) {
		super(parent, SWT.NONE);
		setBackground(parent.getBackground());
		GridLayout l = UIUtil.gridLayout(this, 2);
		l.marginHeight = 0;
		l.marginWidth = 0;
		UIUtil.newLabel(this, Local.Labels + ":", true, false);
		panel = UIUtil.newComposite(this);
		panel.setLayoutData(UIUtil.gridDataHoriz(1, true));
		RowLayout layout = new RowLayout(SWT.HORIZONTAL);
		layout.wrap = true;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		panel.setLayout(layout);
		addButton = new LabelButton(panel);
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
	
	private Composite panel;
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
		for (Control c : panel.getChildren())
			if (c != addButton)
				c.dispose();
		
		for (Label label : DataOrganizer.labels().getLabels(data)) {
			LabelItem item = new LabelItem(panel, label.getName(), LABEL_COLOR, new Listener<LabelItem>() {
				public void fire(LabelItem event) {
					((Label)event.getData()).removeData(LabelsPanel.this.data);
					refresh(LabelsPanel.this.data);
				}
			}, true);
			item.moveAbove(addButton);
			item.setData(label);
		}
		
		UIControlUtil.autoresize(panel);
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
		GridData gd = new GridData();
		gd.heightHint = 200;
		tree.getTree().setLayoutData(gd);
		addMenu.show(addButton, FlatPopupMenu.Orientation.BOTTOM_RIGHT, true);
		addMenu = null;
		refresh(data);
	}
	
}
