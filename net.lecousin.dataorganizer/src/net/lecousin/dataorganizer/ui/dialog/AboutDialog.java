package net.lecousin.dataorganizer.ui.dialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.internal.EclipsePlugin;
import net.lecousin.dataorganizer.ui.application.update.Updater;
import net.lecousin.dataorganizer.ui.application.update.Updater.UpdateException;
import net.lecousin.framework.application.Application;
import net.lecousin.framework.io.IOUtil;
import net.lecousin.framework.ui.eclipse.EclipseImages;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.browser.BrowserWindow;
import net.lecousin.framework.ui.eclipse.control.text.lcml.LCMLText;
import net.lecousin.framework.ui.eclipse.dialog.FlatDialog;
import net.lecousin.framework.xml.XmlUtil;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;

public class AboutDialog extends FlatDialog {

	public AboutDialog() {
		super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Local.About_DataOrganizer.toString(), false, false);
		
		create(null, 0);
		openProgressive(null, OrientationY.BOTTOM);
		modal();
	}
	
	@Override
	protected void createContent(Composite container) {
		UIUtil.gridLayout(container, 1, 0, 0);
		Image img = EclipseImages.getImage(EclipsePlugin.ID, "splash.bmp");
		UIUtil.newImage(container, img);
		Label label;
		try { label = UIUtil.newLabel(container, "DataOrganizer v." + Updater.getCurrentVersion().toString() + " Copyright(c) 2009"); }
		catch (UpdateException e) { label = UIUtil.newLabel(container, "DataOrganizer - unknown version - Copyright (c) 2009"); }
		GridData gd = UIUtil.gridDataHorizFill(label);
		gd.horizontalIndent = 5;
		LCMLText text = new LCMLText(container, false, false);
		text.setText(Local.MESSAGE_About_Licenses.toString());
		text.addLinkListener("gpl", new Runnable() {
			public void run() {
				FlatDialog dlg = new FlatDialog(getShell(), "License", true, true) {
					@Override
					protected void createContent(Composite container) {
						LCMLText text = new LCMLText(container, false, false);
						try {
							FileInputStream in = new FileInputStream(new File(Application.deployPath, "LICENSE.txt"));
							text.setText(XmlUtil.encodeXML(IOUtil.readAllInString(in)));
							in.close();
						} catch (IOException e) {
							text.setText("Unable to read license file !");
						}
						text.setLayoutData(UIUtil.gridData(1, true, 1, false));
					}
				};
				dlg.open(true);
			}
		});
		text.addLinkListener("epl", new Runnable() {
			public void run() {
				BrowserWindow browser = new BrowserWindow("DataOrganizer", null, true, true);
				browser.open();
				try {
					browser.setLocation(new File(Application.deployPath, "epl-v10.html").toURL().toString());
				} catch (MalformedURLException e) {
					// should not happen
					browser.close();
				}
			}
		});
		text.addLinkListener("apache", new Runnable() {
			public void run() {
				BrowserWindow browser = new BrowserWindow("DataOrganizer", null, true, true);
				browser.open();
				browser.setLocation("http://www.apache.org/licenses/");
			}
		});
		
		gd = UIUtil.gridDataHoriz(1, true);
		gd.widthHint = img.getImageData().width - 10;
		gd.horizontalIndent = 5;
		text.setLayoutData(gd);
	}
	
}
