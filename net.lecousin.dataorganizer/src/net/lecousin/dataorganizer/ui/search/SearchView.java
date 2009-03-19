package net.lecousin.dataorganizer.ui.search;

import java.util.LinkedList;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.DataOrganizer;
import net.lecousin.dataorganizer.core.DataLabels.Label;
import net.lecousin.dataorganizer.core.database.content.ContentType;
import net.lecousin.dataorganizer.ui.control.LabelTree;
import net.lecousin.framework.Pair;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.ui.eclipse.SharedImages;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.ui.part.ViewPart;


public class SearchView extends ViewPart {
	public static final String ID = "net.lecousin.dataorganizer.searchView";

	private MainCriteriaPanel mainCriteriaPanel;
	private ContentTypesPanel contentTypesPanel;
	private ExpandItem labelItem, contentTypesItem;
	private LabelTree labelTree;
	private ExpandBar panel;
	
	@Override
	public void setInitializationData(IConfigurationElement cfig, String propertyName, Object data) {
		super.setInitializationData(cfig, propertyName, data);
		setTitleImage(SharedImages.getImage(SharedImages.icons.x16.basic.SEARCH));
		setPartName(Local.Search.toString());
	}
	
	@Override
	public void createPartControl(Composite parent) {
		panel = new ExpandBar(parent, SWT.V_SCROLL);
		ExpandItem item;

		item = new ExpandItem(panel, SWT.NONE);
		item.setText(Local.Main_criteria.toString());
		item.setControl(mainCriteriaPanel = new MainCriteriaPanel(panel));
		item.setHeight(mainCriteriaPanel.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item.setExpanded(true);

		item = new ExpandItem(panel, SWT.NONE);
		item.setText(Local.Content_type.toString());
		item.setControl(contentTypesPanel = new ContentTypesPanel(panel));
		item.setHeight(contentTypesPanel.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item.setExpanded(true);

		labelItem = new ExpandItem(panel, SWT.NONE);
		labelItem.setText(Local.Labels.toString());
		labelItem.setControl((labelTree = new LabelTree(panel, true, SWT.NO_SCROLL, new LinkedList<Label>())).getControl());
		labelItem.setHeight(labelTree.getTree().computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		labelItem.setExpanded(true);
		labelTree.addTreeListener(new ITreeViewerListener() {
			public void treeCollapsed(TreeExpansionEvent event) {
				refreshLabelHeight();
			}
			public void treeExpanded(TreeExpansionEvent event) {
				refreshLabelHeight();
			}
		});
		labelTree.labelSelectionChanged.addListener(new Listener<Pair<Label,Boolean>>() {
			public void fire(Pair<Label,Boolean> event) {
				DataOrganizer.search().getParameters().setLabel(event.getValue1(), event.getValue2());
			}
		});
		labelTree.notLabeledSelectionChanged.addListener(new Listener<Boolean>() {
			public void fire(Boolean event) {
				DataOrganizer.search().getParameters().setNotLabeled(event);
			}
		});
		labelTree.addFilter(new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (!(element instanceof Label)) return true;
				Label label = (Label)element;
				if (label.getParent() == null) return true;
				return DataOrganizer.search().containsLabel(label);
			}
		});
		DataOrganizer.labels().labelAssigned().addFireListener(new Runnable() {
			public void run() {
				labelTree.refresh();
				refreshLabelHeight();
			}
		});
		DataOrganizer.labels().labelUnassigned().addFireListener(new Runnable() {
			public void run() {
				labelTree.refresh();
				refreshLabelHeight();
			}
		});
		DataOrganizer.search().searchChanged().addFireListener(new Runnable() {
			public void run() {
				labelTree.refresh();
				refreshLabelHeight();
			}
		});
		
		contentTypesPanel.typeAdded().addListener(new ContentTypeAdded());
		contentTypesPanel.typeRemoved().addListener(new ContentTypeRemoved());
		
		panel.addControlListener(new ControlListener() {
			public void controlMoved(ControlEvent e) {
			}
			public void controlResized(ControlEvent e) {
				ExpandBar panel = (ExpandBar)e.widget;
				for (ExpandItem item : panel.getItems()) {
					if (item.getControl() instanceof Composite)
						((Composite)item.getControl()).layout(true, true);
					Point size = item.getControl().computeSize(panel.getSize().x-panel.getSpacing()*2, SWT.DEFAULT);
					item.getControl().setSize(size);
					item.setHeight(size.y);
				}
			}
		});
	}
	
	private void refreshLabelHeight() {
		labelTree.getTree().getDisplay().asyncExec(new Runnable() {
			public void run() {
				contentTypesItem.setHeight(labelTree.getTree().computeSize(SWT.DEFAULT, SWT.DEFAULT, true).y);
			}
		});
	}
	
	@Override
	public void setFocus() {
	}
	
	private class ContentTypeAdded implements Listener<String> {
		public void fire(String event) {
			ContentType type = ContentType.getContentType(event);
			Control c = type.createSearchPanel(panel);
			if (c == null) return;
			ExpandItem item = new ExpandItem(panel, SWT.NONE);
			item.setText(type.getName());
			item.setControl(c);
			item.setHeight(c.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
			item.setExpanded(true);
			item.setData(type);
		}
	}
	private class ContentTypeRemoved implements Listener<String> {
		public void fire(String event) {
			ContentType type = ContentType.getContentType(event);
			for (ExpandItem item : panel.getItems()) {
				if (item.getData() != null && item.getData() == type) {
					item.getControl().dispose();
					item.dispose();
					return;
				}
			}
		}
	}
}