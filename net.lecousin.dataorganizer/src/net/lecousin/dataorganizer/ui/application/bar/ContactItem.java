package net.lecousin.dataorganizer.ui.application.bar;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.ui.dialog.ContactDialog;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.control.ImageAndTextButton;
import net.lecousin.framework.ui.eclipse.dialog.MyDialog;

import org.eclipse.jface.action.ControlContribution;
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
				new ContactDialog().openProgressive(null, MyDialog.OrientationY.BOTTOM, true);
			}
		});
		return button;
	}
}
