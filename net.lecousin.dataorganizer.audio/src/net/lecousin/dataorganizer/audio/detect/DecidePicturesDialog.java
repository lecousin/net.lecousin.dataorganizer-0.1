package net.lecousin.dataorganizer.audio.detect;

import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.audio.Local;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.framework.ui.eclipse.EclipseImages;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.Radio;
import net.lecousin.framework.ui.eclipse.control.buttonbar.OkCancelButtonsPanel;
import net.lecousin.framework.ui.eclipse.control.text.lcml.LCMLText;
import net.lecousin.framework.ui.eclipse.dialog.FlatDialog;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public class DecidePicturesDialog<T> extends FlatDialog {

	public DecidePicturesDialog(Shell shell, String message, List<T> images, Provider<T> provider) {
		super(shell, Local.Create_Music_Album.toString(), false, true);
		this.message = message;
		this.images = images;
		this.provider = provider;
	}
	
	public static interface Provider<T> {
		public Image getImage(T element);
		public String getText(T element);
	}
	
	private String message;
	private List<T> images;
	private Provider<T> provider;
	private List<PicturePanel<T>> panels = new LinkedList<PicturePanel<T>>();
	private List<T> coverFront = new LinkedList<T>();
	private List<T> coverBack = new LinkedList<T>();
	private List<T> others = new LinkedList<T>();
	private boolean ok = false;
	
	@Override
	protected void createContent(Composite container) {
		UIUtil.gridLayout(container, 1);
		LCMLText text = new LCMLText(container, false, false);
		text.setText(message);
		text.setLayoutData(UIUtil.gridDataHoriz(1, true));

		for (T p : images) {
			panels.add(new PicturePanel<T>(container, p, provider));
			UIUtil.newSeparator(container, true, true);
		}
		
		new OkCancelButtonsPanel(container, true) {
			protected boolean handleOk() {
				for (PicturePanel<T> p : panels) {
					String s = p.getOption();
					if (s == null) {
						coverFront.clear();
						coverBack.clear();
						others.clear();
						MessageDialog.openError(getShell(), Local.Create_Music_Album.toString(), Local.Please_select_an_option_for_each_picture.toString());
						return false;
					}
					if (s.equals("front"))
						coverFront.add(p.picture);
					else if (s.equals("back"))
						coverBack.add(p.picture);
					else if (s.equals("other"))
						others.add(p.picture);
				}
				ok = true;
				return true;
			}
			protected boolean handleCancel() {
				ok = false;
				return true;
			}
		};
	}
	
	private static class PicturePanel<T> extends Composite {
		public PicturePanel(Composite parent, T picture, Provider<T> provider) {
			super(parent, SWT.NONE);
			this.picture = picture;
			setBackground(parent.getBackground());
			UIUtil.gridDataHorizFill(this);
			UIUtil.gridLayout(this, 2);
			Image image = provider.getImage(picture);
			UIUtil.newImage(this, EclipseImages.resizeMax(image, 256, 256));
			radio = new Radio(this, false);
			radio.addOption("front", Local.Cover_front_picture.toString());
			radio.addOption("back", Local.Cover_back_picture.toString());
			radio.addOption("other", Local.Other_album_picture.toString());
			radio.addOption("no", Local.Do_not_attach_picture.toString());
			radio.setLayoutData(UIUtil.gridData(1, true, 2, false));
			LCMLText text = new LCMLText(this, false, false);
			text.setText(provider.getText(picture));
		}
		T picture;
		Radio radio;
		
		String getOption() { return radio.getSelection(); }
	}
	
	public boolean open() {
		super.openProgressive(null, OrientationY.BOTTOM, true);
		return ok;
	}
	
	public List<T> getCoverFront() { return coverFront; }
	public List<T> getCoverBack() { return coverBack; }
	public List<T> getOthers() { return others; }
}
