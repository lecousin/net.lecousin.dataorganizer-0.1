package net.lecousin.dataorganizer.ui.dialog;

import java.io.IOException;

import org.eclipse.swt.widgets.Composite;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.internal.EclipsePlugin;
import net.lecousin.framework.ui.eclipse.browser.BrowserWindow;
import net.lecousin.framework.ui.eclipse.control.text.lcml.LCMLText;
import net.lecousin.framework.ui.eclipse.dialog.ErrorDlg;
import net.lecousin.framework.ui.eclipse.dialog.FlatDialog;

public class ContactDialog extends FlatDialog {

	public ContactDialog() {
		super(getPlatformShell(), Local.Contact.toString(), false, false);
	}

	@Override
	protected void createContent(Composite container) {
		LCMLText text = new LCMLText(container, false, false);
		text.setText(Local.MESSAGE_Contact.toString());
		text.addLinkListener("mail", new Runnable() {
			public void run() {
				try {
					Runtime.getRuntime().exec("cmd /C start mailto:support.dataorganizer@gmail.com?subject=[Contact]");
				} catch (IOException e) {
					ErrorDlg.exception(Local.Contact.toString(), "Unable to launch mail client", EclipsePlugin.ID, e);
				}
			}
		});
		text.addLinkListener("forum", new Runnable() {
			public void run() {
				BrowserWindow browser = new BrowserWindow("DataOrganizer", null, true, true);
				browser.open();
				browser.setLocation("http://dataorganizer.webhop.net/xmb/");
			}
		});
	}
}
