package net.lecousin.dataorganizer.ui.control;

import net.lecousin.dataorganizer.core.DataOrganizer;
import net.lecousin.dataorganizer.core.DataLabels.Label;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.ui.dataoverview.LabelsPanel;
import net.lecousin.framework.Pair;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.event.Event.ListenerData;
import net.lecousin.framework.ui.eclipse.control.LabelItem;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class LabelsControl extends Composite {

	public LabelsControl(Composite parent, Data data) {
		super(parent, SWT.NONE);
		this.data = data;
		RowLayout layout = new RowLayout(SWT.HORIZONTAL);
		layout.marginBottom = layout.marginHeight = layout.marginTop = 0;
		layout.marginLeft = layout.marginRight = layout.marginWidth = 0;
		layout.marginHeight = 1;
		setLayout(layout);
		for (Label label : DataOrganizer.labels().getLabels(data)) {
			LabelItem item = new LabelItem(LabelsControl.this, label.getName(), LabelsPanel.LABEL_COLOR, new ListenerData<LabelItem,Data>(data) {
				public void fire(LabelItem event) {
					((Label)event.getData()).removeData(data());
				}
			}, true);
			item.setData(label);
		}
		DataOrganizer.labels().labelAssigned().addListener(new LabelAssigned());
		DataOrganizer.labels().labelUnassigned().addListener(new LabelUnassigned());
	}
	
	private Data data;
	
	private class LabelAssigned implements Listener<Pair<Label,Data>> {
		public void fire(Pair<Label, Data> event) {
			if (LabelsControl.this.isDisposed()) {
				DataOrganizer.labels().labelAssigned().removeListener(this);
				return;
			}
			if (event.getValue2() != data) return;
			Label label = event.getValue1();
			LabelItem item = new LabelItem(LabelsControl.this, label.getName(), LabelsPanel.LABEL_COLOR, new ListenerData<LabelItem,Data>(data) {
				public void fire(LabelItem event) {
					((Label)event.getData()).removeData(data());
				}
			}, true);
			item.setData(label);
			layout(true, true);
		}
	}
	
	private class LabelUnassigned implements Listener<Pair<Label,Data>> {
		public void fire(Pair<Label, Data> event) {
			if (LabelsControl.this.isDisposed()) {
				DataOrganizer.labels().labelAssigned().removeListener(this);
				return;
			}
			if (event.getValue2() != data) return;
			Label label = event.getValue1();
			for (Control c : getChildren())
				if (c.getData() == label)
					c.dispose();
			layout(true, true);
		}
	}
}
