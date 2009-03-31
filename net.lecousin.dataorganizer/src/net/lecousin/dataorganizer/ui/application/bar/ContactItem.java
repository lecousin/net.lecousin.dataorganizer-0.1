package net.lecousin.dataorganizer.ui.application.bar;

import java.io.IOException;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.internal.EclipsePlugin;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.control.ImageAndTextButton;
import net.lecousin.framework.ui.eclipse.dialog.ErrorDlg;
import net.lecousin.framework.ui.eclipse.dialog.MyDialog;

import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class ContactItem extends ControlContribution {

	public static final String ID = "net.lecousin.dataorganizer.ui.application.bar.ContactItem";
	
	public ContactItem() {
		super(ID);
	}

	@Override
	protected Control createControl(Composite parent) {
		ImageAndTextButton button = new ImageAndTextButton(parent, SharedImages.getImage(SharedImages.icons.x16.basic.MAIL), Local.Contact.toString());
		button.addClickListener(new Listener<MouseEvent>() {
			public void fire(MouseEvent event) {
				try {
					Runtime.getRuntime().exec("cmd /C start mailto:support.dataorganizer@gmail.com?subject=[Contact]");
				} catch (IOException e) {
					ErrorDlg.exception(Local.Contact.toString(), "Unable to launch mail client", EclipsePlugin.ID, e);
				}
				MessageDialog.openInformation(MyDialog.getPlatformShell(), Local.Contact.toString(), Local.MESSAGE_Contact.toString());
			}
		});
		return button;
	}
}
