package net.lecousin.dataorganizer.ui.search;

import java.util.LinkedList;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.DataOrganizer;
import net.lecousin.dataorganizer.core.DataLabels.Label;
import net.lecousin.dataorganizer.core.search.DataSearch.Parameter;
import net.lecousin.dataorganizer.core.search.DataSearch.ReversableParameter;
import net.lecousin.dataorganizer.ui.control.LabelTree;
import net.lecousin.dataorganizer.ui.control.LabelTree.NewItemSelectionProvider;
import net.lecousin.framework.Pair;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.dialog.CalloutToolTip;
import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ExpandItem;

public class SearchLabelsPanel extends Composite {

	public SearchLabelsPanel(ExpandItem item) {
		super(item.getParent(), SWT.NONE);
		this.item = item;
		setBackground(ColorUtil.getWhite());
		UIUtil.gridLayout(this, 1);
		
		Composite header = UIUtil.newGridComposite(this, 0, 0, 2);
		UIUtil.gridDataHorizFill(header);
		Control c = UIUtil.newImageToggleButton(header, SharedImages.getImage(SharedImages.icons.x10.basic.REVERSE), new Reverse(), (ReversableParameter)DataOrganizer.search().getLabelsParameter());
		c.setToolTipText(Local.Reverse_search.toString());
		GridData gd = new GridData();
		gd.horizontalAlignment = SWT.BEGINNING;
		gd.heightHint = 17;
		gd.widthHint = 17;
		c.setLayoutData(gd);
		
		c = UIUtil.newImageButton(header, SharedImages.getImage(SharedImages.icons.x16.basic.HELP), new Help(DataOrganizer.search().getLabelsParameter()), null);
		gd = new GridData();
		gd.horizontalAlignment = SWT.END;
		c.setLayoutData(gd);
		
		
		tree = new LabelTree(this, true, SWT.NO_SCROLL, new LinkedList<Label>(), new NewItemSelectionProvider() {
			public boolean isSelected(Label label) {
				return DataOrganizer.search().getLabelsParameter().getLabels().contains(label);
			}
		});
		UIUtil.gridData(1, true, 1, true);
		
		item.setControl(this);
		item.setHeight(computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item.setExpanded(true);
		tree.addTreeListener(new ITreeViewerListener() {
			public void treeCollapsed(TreeExpansionEvent event) {
				refreshHeight();
			}
			public void treeExpanded(TreeExpansionEvent event) {
				refreshHeight();
			}
		});
		tree.labelSelectionChanged.addListener(new Listener<Pair<Label,Boolean>>() {
			public void fire(Pair<Label,Boolean> event) {
				DataOrganizer.search().getLabelsParameter().setLabel(event.getValue1(), event.getValue2());
			}
		});
		tree.notLabeledSelectionChanged.addListener(new Listener<Boolean>() {
			public void fire(Boolean event) {
				DataOrganizer.search().getLabelsParameter().setNotLabeled(event);
			}
		});
		tree.addFilter(new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (!(element instanceof Label)) return true;
				Label label = (Label)element;
				if (label.getParent() == null) return true;
				return DataOrganizer.search().getLabelsParameter().resultsContainsLabel(label);
			}
		});
		DataOrganizer.labels().labelAssigned().addFireListener(new Runnable() {
			public void run() {
				tree.refresh();
				refreshHeight();
			}
		});
		DataOrganizer.labels().labelUnassigned().addFireListener(new Runnable() {
			public void run() {
				tree.refresh();
				refreshHeight();
			}
		});
		DataOrganizer.search().searchChanged().addFireListener(new Runnable() {
			public void run() {
				tree.refresh();
				refreshHeight();
			}
		});
		
	}
	
	private LabelTree tree;
	private ExpandItem item;
	
	private void refreshHeight() {
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				item.setHeight(computeSize(SWT.DEFAULT, SWT.DEFAULT, true).y);
			}
		});
	}
	
	private class Reverse implements Listener<Pair<ReversableParameter,Boolean>> {
		public void fire(Pair<ReversableParameter, Boolean> event) {
			event.getValue1().setReverse(event.getValue2());
		}
	}
	
	private class Help implements Listener<Object> {
		public Help(Parameter p) { this.param = p; }
		Parameter param;
		public void fire(Object event) {
			CalloutToolTip.open(tree.getTree(), CalloutToolTip.Orientation.TOP_RIGHT, param.getParameterHelp(), 5000, -1);
			tree.getTree().setFocus();
		}
	}
	
}
