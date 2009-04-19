package net.lecousin.dataorganizer.datalist;

import java.util.List;

import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.datalist.internal.EclipsePlugin;
import net.lecousin.dataorganizer.ui.plugin.Action;
import net.lecousin.framework.thread.RunnableWithData;
import net.lecousin.framework.ui.eclipse.EclipseImages;
import net.lecousin.framework.ui.eclipse.dialog.FlatPopupMenu;
import net.lecousin.framework.ui.eclipse.dialog.MyDialog;
import net.lecousin.framework.ui.eclipse.dialog.MyDialog.Orientation;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;

public class AddToListAction implements Action {

	public AddToListAction(List<Data> data) {
		this.data = data;
	}
	
	private List<Data> data;
	
	public String getText() {
		return Local.Add_to_list+"...";
	}
	public Image getIcon() {
		return EclipseImages.getImage(EclipsePlugin.ID, "images/list.gif");
	}
	public Type getType() { return Type.SEND; }
	
	public boolean isSame(Action action) {
		return (action instanceof AddToListAction) && data.equals(((AddToListAction)action).data);
	}
	
	public void run() {
		FlatPopupMenu menu = new FlatPopupMenu(null, Local.Add_to_list.toString(), false, true, false, false);
		for (DataList list : DataLists.getInstance().getLists())
			new FlatPopupMenu.Menu(menu, list.getName(), null, false, false, new RunnableWithData<DataList>(list) {
				public void run() {
					data().addData(data);
					View.show();
				}
			});
		if (!DataLists.getInstance().getLists().isEmpty())
			new FlatPopupMenu.Separator(menu);
		new FlatPopupMenu.Menu(menu, Local.Create_new_list.toString(), null, false, false, new Runnable() {
			public void run() {
				InputDialog dlg = new InputDialog(MyDialog.getPlatformShell(), Local.Create_new_list.toString(), Local.Enter_the_new_list_name.toString(), "", new IInputValidator() {
					public String isValid(String newText) {
						if (newText.length() == 0)
							return Local.The_name_cannot_be_empty.toString();
						if (DataLists.getInstance().getList(newText) != null)
							return Local.A_list_already_exists_with_the_same_name.toString();
						return DataList.validateName(newText);
					}
				});
				if (dlg.open() == Window.OK) {
					String name = dlg.getValue();
					DataList list = DataLists.getInstance().createList(name);
					list.addData(data);
					View.show();
				}
			}
		});
		menu.show(null, Orientation.BOTTOM, true);
	}
	
}
