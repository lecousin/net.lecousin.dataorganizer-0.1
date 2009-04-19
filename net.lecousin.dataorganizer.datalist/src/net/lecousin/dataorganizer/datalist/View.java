package net.lecousin.dataorganizer.datalist;

import net.lecousin.dataorganizer.datalist.internal.EclipsePlugin;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.ui.eclipse.dialog.ErrorDlg;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Listener;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class View extends ViewPart {

	public static final String ID = "net.lecousin.dataorganizer.datalist";
	
	public static View show() {
		try { 
			return (View)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(ID);
		}
		catch (PartInitException e) {
			ErrorDlg.exception("Data List", "Unable to open view", EclipsePlugin.ID, e);
			return null;
		}
	}
	
	public View() {
	}

	@Override
	public void setInitializationData(IConfigurationElement cfig, String propertyName, Object data) {
		super.setInitializationData(cfig, propertyName, data);
		setPartName(Local.Data_lists.toString());
	}
	
	private CTabFolder folder;
	
	@Override
	public void createPartControl(Composite parent) {
		folder = new CTabFolder(parent, SWT.CLOSE | SWT.TOP | SWT.BORDER);
		for (DataList list : DataLists.getInstance().getLists())
			createList(list);
		DataLists.getInstance().listAdded.addListener(new Listener<DataList>() {
			public void fire(DataList event) {
				createList(event);
			}
		});
		DataLists.getInstance().listRemoved.addListener(new Listener<DataList>() {
			public void fire(DataList event) {
				for (CTabItem item : folder.getItems()) {
					if (item.isDisposed()) continue;
					if (item.getData() != event) continue;
					item.dispose();
					break;
				}
			}
		});
		folder.addCTabFolder2Listener(new CTabFolder2Listener() {
			public void close(CTabFolderEvent event) {
				DataList list = (DataList)((CTabItem)event.item).getData();
				DataLists.getInstance().removeList(list);
			}
			public void maximize(CTabFolderEvent event) {
			}
			public void minimize(CTabFolderEvent event) {
			}
			public void restore(CTabFolderEvent event) {
			}
			public void showList(CTabFolderEvent event) {
			}
		});
	}

	private void createList(DataList list) {
		CTabItem item = new CTabItem(folder, SWT.CLOSE);
		item.setData(list);
		item.setControl(new DataListControl(folder, list));
		item.setText(list.getName());
	}

	@Override
	public void setFocus() {
	}

}
