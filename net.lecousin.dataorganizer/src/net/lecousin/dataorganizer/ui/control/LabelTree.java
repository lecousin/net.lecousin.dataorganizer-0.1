package net.lecousin.dataorganizer.ui.control;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.DataLabels;
import net.lecousin.dataorganizer.core.DataOrganizer;
import net.lecousin.dataorganizer.core.DataLabels.Label;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.internal.EclipsePlugin;
import net.lecousin.dataorganizer.ui.DataOrganizerDND;
import net.lecousin.framework.Pair;
import net.lecousin.framework.event.Event;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.thread.RunnableWithData;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.dialog.ErrorDlg;
import net.lecousin.framework.ui.eclipse.menu.MyMenu;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class LabelTree extends CheckboxTreeViewer {

	public LabelTree(Composite parent, boolean showNotLabeled, int style, List<Label> selected) {
		super(new Tree(parent, (selected != null ? SWT.CHECK : 0) | style));
		setContentProvider(new ContentProvider(showNotLabeled));
		setLabelProvider(new LabelProvider());
		setInput(root);
		//addSelectionChangedListener();
		addCheckStateListener(new SelectionChangedListener());
		getTree().addMouseListener(new Mouse());
		addTreeListener(new TreeListener());
		DataOrganizer.labels().labelAdded().addListener(new Listener<Label>() {
			public void fire(Label label) {
				if (LabelTree.this.getControl().isDisposed()) {
					DataOrganizer.labels().labelAdded().removeListener(this);
					return;
				}
				LabelTree.this.refresh(label.getParent());
			}
		});
		DataOrganizer.labels().labelRemoved().addListener(new Listener<Label>() {
			public void fire(Label label) {
				if (LabelTree.this.getControl().isDisposed()) {
					DataOrganizer.labels().labelRemoved().removeListener(this);
					return;
				}
				LabelTree.this.refresh(label.getParent());
			}
		});
		DataOrganizer.labels().labelRenamed().addListener(new Listener<Label>() {
			public void fire(Label label) {
				if (LabelTree.this.getControl().isDisposed()) {
					DataOrganizer.labels().labelRenamed().removeListener(this);
					return;
				}
				LabelTree.this.refresh(label.getParent(), true);
			}
		});
		Set<Label> toExpand = new HashSet<Label>();
		if (selected != null) {
			setCheckedElements(selected.toArray());
			for (Label l : selected) {
				Label p = l.getParent();
				while (p != null) {
					toExpand.add(p);
					p = p.getParent();
				}
			}
		}
		if (!showNotLabeled)
			toExpand.add(DataOrganizer.labels().root());
		setExpandedElements(toExpand.toArray());
		
		addDragSupport(DND.DROP_MOVE, new Transfer[] {TextTransfer.getInstance()}, new DragSourceListener() {
			public void dragStart(DragSourceEvent event) {
				event.doit = getLabelsForDND() != null;
				event.detail = DND.DROP_MOVE;
			}
			public void dragFinished(DragSourceEvent event) {
			}
			public void dragSetData(DragSourceEvent event) {
				event.data = DataOrganizerDND.getLabelsDNDString(getLabelsForDND());
			}
		});
		addDropSupport(DND.DROP_MOVE | DND.DROP_LINK, new Transfer[] {TextTransfer.getInstance()}, new DropTargetListener() {
			public void dragEnter(DropTargetEvent event) {
				check(event);
			}
			public void dragLeave(DropTargetEvent event) {
			}
			public void dragOver(DropTargetEvent event) {
				check(event);
			}
			public void dragOperationChanged(DropTargetEvent event) {
			}
			public void drop(DropTargetEvent event) {
				if (DataOrganizerDND.isLabels((String)event.data))
					dropLabels(event, DataOrganizerDND.getLabelsDNDFromString((String)event.data));
				else if (DataOrganizerDND.isData((String)event.data))
					dropData(event, DataOrganizerDND.getDataDNDFromString((String)event.data));
			}
			private void dropLabels(DropTargetEvent event, List<Label> sources) {
				Label target = (Label)((TreeItem)event.item).getData();
				
				// remove the sources already at the right place
				List<Label> targetChildren = target.getChildren();
				for (Iterator<Label> it = sources.iterator(); it.hasNext(); )
					if (targetChildren.contains(it.next()))
						it.remove();
				if (sources.isEmpty()) return;
				
				StringBuilder str = new StringBuilder();
				str.append(Local.You_are_about_to_move_labels + ":\r\n");
				for (Label src : sources)
					str.append(src.getPath()).append(" (").append(src.getData().size()).append(" ").append(Local.data_impacted).append(")\r\n");
				str.append(Local.into_the_label + ":\r\n").append(target.getPath()).append("\r\n");
				str.append(Local.Do_you_confirm_this_operation);
				if (!MessageDialog.openConfirm(null, Local.Move_labels.toString(), str.toString()))
					return;
				for (Label src : sources)
					try { DataOrganizer.labels().move(src, target); }
					catch (CoreException e) {
						ErrorDlg.exception(Local.Move_labels.toString(), Local.Unable_to_move_label + " " + src.getPath() + " " + Local.into + " " + target.getPath(), EclipsePlugin.ID, e);
					}
				refresh();
			}
			private void dropData(DropTargetEvent event, List<Data> data) {
				Label target = (Label)((TreeItem)event.item).getData();
				for (Data d : data)
					target.addData(d);
			}
			public void dropAccept(DropTargetEvent event) {
			}
			private void check(DropTargetEvent event) {
				if (!(event.item instanceof TreeItem)) {
					event.detail = DND.DROP_NONE;
					return;
				}
				Object o = ((TreeItem)event.item).getData();
				if (!(o instanceof Label)) {
					event.detail = DND.DROP_NONE;
					return;
				}
				if ((event.operations & DND.DROP_MOVE) != 0) {
					List<Label> sources = getLabelsForDND();
					Label target = (Label)o;
					if (sources.contains(target)) {
						event.detail = DND.DROP_NONE;
						return;
					}
					event.detail = DND.DROP_MOVE;
				} else if ((event.operations & DND.DROP_LINK) != 0)
					event.detail = DND.DROP_LINK;
			}
		});
	}
	
	private List<Label> getLabelsForDND() {
		IStructuredSelection sel = (IStructuredSelection)getSelection();
		if (sel.isEmpty()) return null;
		List<Label> result = new LinkedList<Label>();
		for (Iterator<?> it = sel.iterator(); it.hasNext(); ) {
			Object o = it.next();
			if (!(o instanceof Label) || ((Label)o).getParent() == null)
				return null;
			result.add((Label)o);
		}
		return result;
	}
	
	private static class ContentProvider implements ITreeContentProvider {
		public ContentProvider(boolean showNotLabeled) { this.showNotLabeled = showNotLabeled; }
		private boolean showNotLabeled;
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		public Object[] getElements(Object inputElement) {
			return getChildren(root);
		}
		public Object getParent(Object element) {
			if (element instanceof RootNode) return null;
			if (element instanceof NotLabeledNode) return root;
			Label label = (Label)element;
			return label.getParent() != null ? label.getParent() : root;
		}
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof RootNode)
				return showNotLabeled ? new Object[] { DataOrganizer.labels().root(), notLabeled } : new Object[] { DataOrganizer.labels().root() };
			if (parentElement instanceof NotLabeledNode)
				return new Object[] {};
			return ((Label)parentElement).getChildren().toArray();
		}
		public boolean hasChildren(Object element) {
			if (element instanceof RootNode) return true;
			if (element instanceof NotLabeledNode) return false;
			return !((Label)element).getChildren().isEmpty();
		}
		public void dispose() {
		}
	}
	
	private static class LabelProvider implements ILabelProvider {
		public String getText(Object element) {
			if (element instanceof RootNode) return Local.Labels.toString();
			if (element instanceof NotLabeledNode) return Local.Not_labeled.toString();
			return ((Label)element).getName();
		}
		public Image getImage(Object element) {
			return null;
		}
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}
		public void addListener(ILabelProviderListener listener) {
		}
		public void removeListener(ILabelProviderListener listener) {
		}
		public void dispose() {
		}
	}
	
	private static class RootNode {}
	private static final RootNode root = new RootNode();
	private static class NotLabeledNode {}
	private static final NotLabeledNode notLabeled = new NotLabeledNode();
	
	public Event<Boolean> notLabeledSelectionChanged = new Event<Boolean>();
	public Event<Pair<Label,Boolean>> labelSelectionChanged = new Event<Pair<Label,Boolean>>(); 
	
	private class SelectionChangedListener implements ICheckStateListener {
		public void checkStateChanged(CheckStateChangedEvent event) {
			if (event.getElement() instanceof NotLabeledNode)
				notLabeledSelectionChanged.fire(event.getChecked());
			else
				labelSelectionChanged.fire(new Pair<Label,Boolean>((Label)event.getElement(), event.getChecked()));
		}
	}
	
	private class TreeListener implements ITreeViewerListener {
		public void treeCollapsed(TreeExpansionEvent event) {
			
		}
		public void treeExpanded(TreeExpansionEvent event) {
		}
	}
	
	private class Mouse implements MouseListener {
		public void mouseDoubleClick(MouseEvent e) {
		}
		public void mouseDown(MouseEvent e) {
		}
		public void mouseUp(MouseEvent e) {
	      if (e.button == 3) {
	          Point pt = new Point(e.x, e.y);
	          TreeItem item = getTree().getItem(pt);
	          if (item.getData() instanceof Label)
	        	  handleRightClick((Label)item.getData(), pt);
	      }
		}
	}
	
	public void handleRightClick(Label label, Point pt) {
		MyMenu menu = new MyMenu();
		menu.add(Local.New_label.toString(), 
			SharedImages.getImage(SharedImages.icons.x16.basic.ADD),
			new RunnableWithData<Label>(label) {
			public void run() {
				InputDialog dlg = new InputDialog(null, Local.New_label.toString(), Local.Enter_the_name_of_the_new_label.toString(), "", new IInputValidator() {
					public String isValid(String newText) {
						if (newText.length() == 0) return Local.The_name_cannot_be_empty + ".";
						if (data().getChild(newText) != null) return Local.This_label_already_exists + ".";
						return DataLabels.validateName(newText);
					}
				});
				if (dlg.open() == Window.OK) {
					data().newLabel(dlg.getValue());
					refresh();
				}
			}
		});
		if (label.getParent() != null)
		menu.add(Local.Rename.toString(), 
			SharedImages.getImage(SharedImages.icons.x16.basic.EDIT),
			new RunnableWithData<Label>(label) {
			public void run() {
				InputDialog dlg = new InputDialog(null, Local.Rename_label.toString(), Local.Enter_the_name_of_the_label.toString(), data().getName(), new IInputValidator() {
					public String isValid(String newText) {
						if (newText.length() == 0) return Local.The_name_cannot_be_empty + ".";
						Label c = data().getParent().getChild(newText);
						if (c != null && c != data()) return Local.This_label_already_exists + ".";
						return DataLabels.validateName(newText);
					}
				});
				if (dlg.open() == Window.OK) {
					try { data().rename(dlg.getValue()); }
					catch (CoreException e) {
						ErrorDlg.exception(Local.Rename_label.toString(), Local.Unable_to_rename_the_label.toString(), EclipsePlugin.ID, e);
					}
					refresh();
				}
			}
		});
		if (label.getParent() != null)
		menu.add(Local.Remove.toString(),
			SharedImages.getImage(SharedImages.icons.x16.basic.DEL),
			new RunnableWithData<Label>(label) {
			public void run() {
				StringBuilder str = new StringBuilder();
				str.append(Local.Are_you_sure_you_want_to_remove_the_label).append(" ")
					.append(data().getPath())
					.append(" (")
					.append(data().getData().size())
					.append(" ").append(Local.data_impacted).append(" ?");
				if (!MessageDialog.openConfirm(null, Local.Remove_label.toString(), str.toString()))
					return;
				try { data().remove(); }
				catch (CoreException e) {
					ErrorDlg.exception(Local.Remove_label.toString(), Local.Unable_to_remove_the_label.toString(), EclipsePlugin.ID, e);
				}
				refresh();
			}
		});
		menu.show(getTree(), pt.x, pt.y);
	}
}
