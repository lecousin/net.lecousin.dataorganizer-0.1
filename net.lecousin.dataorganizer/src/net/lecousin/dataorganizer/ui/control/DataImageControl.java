package net.lecousin.dataorganizer.ui.control;

import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.framework.Pair;
import net.lecousin.framework.Triple;
import net.lecousin.framework.event.ProcessListener;
import net.lecousin.framework.thread.RunnableWithData;
import net.lecousin.framework.ui.eclipse.EclipseImages;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.UIControlUtil;
import net.lecousin.framework.ui.eclipse.dialog.FlatPopupMenu;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Hyperlink;

public class DataImageControl extends Composite {

	public DataImageControl(Composite parent, Data data, int maxWidth, int maxHeight) {
		super(parent, SWT.NONE);
		setBackground(parent.getBackground());
		this.data = data;
		this.maxWidth = maxWidth;
		this.maxHeight = maxHeight;
		GridLayout layout = UIUtil.gridLayout(this, 3);
		layout.marginHeight = 1;
		layout.verticalSpacing = 1;
		marginHeight = layout.marginHeight*2 + layout.verticalSpacing;
		GridData gd;
		labelImage = UIUtil.newImage(this, null);
		labelImage.setAlignment(SWT.CENTER);
		gd = UIUtil.gridDataHorizFill(labelImage);
		buttonPrevious = UIUtil.newImage(this, SharedImages.getImage(maxWidth >= 100 && maxHeight >= 100 ? SharedImages.icons.x16.arrows.LEFT : SharedImages.icons.x11.arrows.LEFT));
		linkName = UIUtil.newLink(this, "", new IHyperlinkListener() {
			public void linkActivated(HyperlinkEvent e) {
				FlatPopupMenu dlg = new FlatPopupMenu((Control)e.widget, DataImageControl.this.data.getName(), true, false, false, true);
				synchronized (images) {
					for (int i = 0; i < images.size(); ++i) {
						Pair<Pair<String,Integer>,Pair<Image,Image>> p = images.get(i);
						UIUtil.newLabel(dlg.getControl(), p.getValue1().getValue1());
						UIUtil.newImage(dlg.getControl(), p.getValue2().getValue2());
					}
				}
				dlg.show(linkName, FlatPopupMenu.Orientation.TOP_BOTTOM, true);
			}
			public void linkEntered(HyperlinkEvent e) {
			}
			public void linkExited(HyperlinkEvent e) {
			}
		});
		if (maxWidth < 100 && maxHeight < 100)
			UIControlUtil.increaseFontSize(linkName, -1);
		Image img = SharedImages.getImage(maxWidth >= 100 && maxHeight >= 100 ? SharedImages.icons.x16.arrows.RIGHT : SharedImages.icons.x11.arrows.RIGHT);
		buttonHeight = img.getBounds().height;
		buttonNext = UIUtil.newImage(this, img);
		gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.CENTER;
		linkName.setLayoutData(gd);
		buttonPrevious.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent e) {
			}
			public void mouseDown(MouseEvent e) {
			}
			public void mouseUp(MouseEvent e) {
				if (e.button == 1)
					previous();
			}
		});
		buttonNext.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent e) {
			}
			public void mouseDown(MouseEvent e) {
			}
			public void mouseUp(MouseEvent e) {
				if (e.button == 1)
					next();
			}
		});
		labelImage.addPaintListener(new ImagesTaker());
		createFromData();
	}
	
	public Point computeSize(int hint, int hint2, boolean changed) {
		return new Point(hint == SWT.DEFAULT ? maxWidth : hint, hint2 == SWT.DEFAULT ? maxHeight+buttonHeight+marginHeight : hint2);
	}
	
	private Data data;
	private int maxWidth, maxHeight;
	private Label labelImage;
	private Label buttonPrevious, buttonNext;
	private Hyperlink linkName;
	private Image defaultImage;
	private List<Pair<Pair<String,Integer>,Pair<Image,Image>>> images = new LinkedList<Pair<Pair<String,Integer>,Pair<Image,Image>>>();
	private boolean imagesTaken = false;
	private int buttonHeight;
	private int marginHeight;
	
	public void setData(Data data) {
		this.data = data;
		createFromData();
	}
	
	private void createFromData() {
		defaultImage = data != null ? EclipseImages.resizeMax(data.getContentType().getDefaultTypeImage(), maxWidth, maxHeight) : null;
		labelImage.setImage(defaultImage);
		linkName.setText(Local.No_image.toString());
		images.clear();
		imagesTaken = false;
		redraw();
	}
	
	private class ImagesTaker implements PaintListener {
		public void paintControl(PaintEvent e) {
			if (imagesTaken) return;
			imagesTaken = true;
			if (data == null) return;
			data.getContent().getImages(new ImagesListener(data));
		}
	}
	private class ImagesListener implements ProcessListener<Triple<String,Image,Integer>> {
		public ImagesListener(Data data) {
			this.data = data;
		}
		private Data data;
		public void fire(Triple<String,Image,Integer> event) {
			if (labelImage.isDisposed()) return;
			if (this.data != DataImageControl.this.data) return;
			int i;
			synchronized(images) {
				for (i = 0; i < images.size(); ++i)
					if (images.get(i).getValue1().getValue2() > event.getValue3()) break;
				images.add(i, new Pair<Pair<String,Integer>,Pair<Image,Image>>(
						new Pair<String,Integer>(event.getValue1(), event.getValue3()),
						new Pair<Image,Image>(EclipseImages.resizeMax(event.getValue2(), maxWidth, maxHeight), event.getValue2())));
			}
			labelImage.getDisplay().asyncExec(new RunnableWithData<Integer>(i) {
				public void run() {
					if (labelImage.isDisposed()) return;
					if (ImagesListener.this.data != DataImageControl.this.data) return;
					if (labelImage.getImage() == defaultImage || data() == 0)
						setImage(0);
				}
			});
		}
		public void done() {
		}
		public void started() {
		}
	}
	private void previous() {
		int i;
		for (i = 0; i < images.size(); ++i)
			if (images.get(i).getValue2().getValue1() == labelImage.getImage())
				break;
		if (i == 0) return;
		if (i < images.size())
			setImage(i-1);
	}
	
	private void next() {
		int i;
		for (i = 0; i < images.size(); ++i)
			if (images.get(i).getValue2().getValue1() == labelImage.getImage())
				break;
		if (i >= images.size()-1) return;
		setImage(i+1);
	}
	
	private void setImage(int index) {
		Pair<Pair<String,Integer>,Pair<Image,Image>> image = images.get(index);
		labelImage.setImage(image.getValue2().getValue1());
		linkName.setText(image.getValue1().getValue1());
		layout(true, true);
	}
	
	@Override
	public void setBackground(Color color) {
		super.setBackground(color);
		if (labelImage == null) return;
		labelImage.setBackground(color);
		linkName.setBackground(color);
		buttonPrevious.setBackground(color);
		buttonNext.setBackground(color);
	}
}
