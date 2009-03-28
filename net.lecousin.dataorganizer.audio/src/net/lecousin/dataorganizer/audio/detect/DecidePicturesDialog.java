package net.lecousin.dataorganizer.audio.detect;

import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.audio.Local;
import net.lecousin.dataorganizer.audio.detect.AlbumHelper.PictureFile;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.framework.ui.eclipse.EclipseImages;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.Radio;
import net.lecousin.framework.ui.eclipse.control.buttonbar.OkCancelButtonsPanel;
import net.lecousin.framework.ui.eclipse.control.text.lcml.LCMLText;
import net.lecousin.framework.ui.eclipse.dialog.FlatDialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public class DecidePicturesDialog extends FlatDialog {

	public DecidePicturesDialog(Shell shell, Data data, List<PictureFile> images) {
		super(shell, Local.Create_Music_Album.toString(), false, true);
		this.data = data;
		this.images = images;
	}
	
	private Data data;
	private List<PictureFile> images;
	private List<PicturePanel> panels = new LinkedList<PicturePanel>();
	private List<PictureFile> coverFront = new LinkedList<PictureFile>();
	private List<PictureFile> coverBack = new LinkedList<PictureFile>();
	private List<PictureFile> others = new LinkedList<PictureFile>();
	private boolean ok = false;
	
	@Override
	protected void createContent(Composite container) {
		UIUtil.gridLayout(container, 1);
		LCMLText text = new LCMLText(container, false, false);
		text.setText(Local.process(Local.MESSAGE_Pictures, data.getName()));
		text.setLayoutData(UIUtil.gridDataHoriz(1, true));

		for (PictureFile p : images) {
			panels.add(new PicturePanel(container, p));
			UIUtil.newSeparator(container, true, true);
		}
		
		new OkCancelButtonsPanel(container, true) {
			protected boolean handleOk() {
				for (PicturePanel p : panels) {
					String s = p.getOption();
					if (s == null) {
						coverFront.clear();
						coverBack.clear();
						others.clear();
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
	
	private static class PicturePanel extends Composite {
		public PicturePanel(Composite parent, PictureFile picture) {
			super(parent, SWT.NONE);
			this.picture = picture;
			setBackground(parent.getBackground());
			UIUtil.gridDataHorizFill(this);
			UIUtil.gridLayout(this, 2);
			Image image = new Image(parent.getDisplay(), picture.image.getInfo().getData()[0]);
			UIUtil.newImage(this, EclipseImages.resizeMax(image, 256, 256));
			radio = new Radio(this, false);
			radio.addOption("front", Local.Cover_front_picture.toString());
			radio.addOption("back", Local.Cover_back_picture.toString());
			radio.addOption("other", Local.Other_album_picture.toString());
			radio.addOption("no", Local.Do_not_attach_picture.toString());
			radio.setLayoutData(UIUtil.gridData(1, true, 2, false));
			LCMLText text = new LCMLText(this, false, false);
			ImageData d = picture.image.getInfo().getData()[0];
			text.setText(Local.File+": "+picture.file.getName()+" ("+Local.size+':'+d.width+'x'+d.height+')');
		}
		PictureFile picture;
		Radio radio;
		
		String getOption() { return radio.getSelection(); }
	}
	
	public boolean open() {
		super.openProgressive(null, OrientationY.BOTTOM, true);
		return ok;
	}
	
	public List<PictureFile> getCoverFront() { return coverFront; }
	public List<PictureFile> getCoverBack() { return coverBack; }
	public List<PictureFile> getOthers() { return others; }
}
