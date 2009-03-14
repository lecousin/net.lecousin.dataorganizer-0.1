package net.lecousin.dataorganizer.ui.application.bar;

import net.lecousin.dataorganizer.Local;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.browser.BrowserWindow;
import net.lecousin.framework.ui.eclipse.control.ImageAndTextButton;

import org.eclipse.jface.action.ControlContribution;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class DonateItem extends ControlContribution {

	public static final String ID = "net.lecousin.dataorganizer.ui.application.bar.DonateItem";
	
	public DonateItem() {
		super(ID);
	}

	@Override
	protected Control createControl(Composite parent) {
		ImageAndTextButton button = new ImageAndTextButton(parent, SharedImages.getImage(SharedImages.icons.x16.misc.HEART), Local.Support_the_project.toString());
		button.addClickListener(new Listener<MouseEvent>() {
			public void fire(MouseEvent event) {
				BrowserWindow browser = new BrowserWindow("DataOrganizer", null, true, true);
				browser.open();
				browser.setLocation("http://www.pledgie.com/campaigns/3345");
			}
		});
		return button;
	}
}
