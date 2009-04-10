package net.lecousin.dataorganizer.ui.control;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.content.DataContentType.DataImageCategory;
import net.lecousin.dataorganizer.core.database.content.DataContentType.DataImageLoaded;
import net.lecousin.framework.Triple;
import net.lecousin.framework.collections.SortedListTree;
import net.lecousin.framework.event.ProcessListener;
import net.lecousin.framework.event.Event.ListenerData;
import net.lecousin.framework.thread.RunnableWithData;
import net.lecousin.framework.ui.eclipse.EclipseImages;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.LCGroup;
import net.lecousin.framework.ui.eclipse.control.UIControlUtil;
import net.lecousin.framework.ui.eclipse.dialog.FlatPopupMenu;
import net.lecousin.framework.ui.eclipse.dialog.MyDialog;
import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

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
				synchronized (categories) {
					for (ImageCategory cat : categories) {
						if (cat.images.isEmpty()) continue;
						LCGroup group = new LCGroup(dlg.getControl(), cat.cat.getName(), ColorUtil.get(80,80,255));
						UIUtil.gridLayout(group.getInnerControl(), 1, 0, 0);
						Control c = DataImageControl.this.data.getContent().createImageCategoryControls(group.getInnerControl());
						if (c != null)
							c.setData("cat.header");
						for (DataImage i : cat.images) {
							Composite header = UIUtil.newGridComposite(group.getInnerControl(), 0, 0, 2, 10, 0);
							Label label = UIUtil.newImage(group.getInnerControl(), i.image.getImage());
							UIUtil.newLabel(header, i.image.getName());
							UIUtil.newImageButton(header, SharedImages.getImage(SharedImages.icons.x16.basic.DEL), new ListenerData<DataImage,Triple<LCGroup,Composite,Label>>(new Triple<LCGroup,Composite,Label>(group, header, label)) {
								public void fire(DataImage event) {
									event.data.getContent().removeImage(event.image);
									data().getValue2().dispose();
									data().getValue3().dispose();
									Composite parent = data().getValue1().getParent();
									boolean found = false;
									for (Control c : data().getValue1().getChildren())
										if (c.getData() instanceof String && ((String)c.getData()).equals("cat.header"))
											continue;
										else {
											found = true;
											break;
										}
									if (!found) {
										data().getValue1().dispose();
									}
									parent.layout(true, true);
									MyDialog.resizeDialog(parent);
								}
							}, i);
							// TODO edit??
						}
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
	private SortedListTree<ImageCategory> categories = new SortedListTree<ImageCategory>(new ImageCategory.Comp());
	private boolean imagesTaken = false;
	private int buttonHeight;
	private int marginHeight;
	
	private static class ImageCategory {
		DataImageCategory cat;
		List<DataImage> images = new LinkedList<DataImage>();
		private static class Comp implements Comparator<ImageCategory> {
			public int compare(ImageCategory o1, ImageCategory o2) {
				return o1.cat.getPriority() - o2.cat.getPriority();
			}
		}
	}
	private static class DataImage {
		ImageCategory category;
		DataImageLoaded image;
		Image resized;
		Data data;
	}
	
	public void setData(Data data) {
		this.data = data;
		createFromData();
	}
	
	private void createFromData() {
		defaultImage = data != null ? EclipseImages.resizeMax(data.getContentType().getDefaultTypeImage(), maxWidth, maxHeight) : null;
		labelImage.setImage(defaultImage);
		linkName.setText(Local.No_image.toString());
		synchronized (categories) {
			categories.clear();
			if (data != null)
				for (DataImageCategory c : data.getContent().getImagesCategories()) {
					ImageCategory cat = new ImageCategory();
					cat.cat = c;
					categories.add(cat);
				}
		}
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
	private class ImagesListener implements ProcessListener<DataImageLoaded> {
		public ImagesListener(Data data) {
			this.data = data;
		}
		private Data data;
		public void fire(DataImageLoaded event) {
			if (labelImage.isDisposed()) return;
			if (this.data != DataImageControl.this.data) return;
			DataImage i = null;
			synchronized(categories) {
				ImageCategory cat = null;
				for (ImageCategory c : categories)
					if (c.cat.getID().equals(event.getCategoryID())) {
						cat = c;
						break;
					}
				if (cat != null) {
					i = new DataImage();
					i.image = event;
					i.category = cat;
					i.data = data;
					i.resized = EclipseImages.resizeMax(event.getImage(), maxWidth, maxHeight);
					cat.images.add(i);
				}
			}
			if (i != null)
				labelImage.getDisplay().asyncExec(new RunnableWithData<DataImage>(i) {
					public void run() {
						if (labelImage.isDisposed()) return;
						if (ImagesListener.this.data != DataImageControl.this.data) return;
						if (labelImage.getImage() == defaultImage || getIndex(data()) == 0)
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
		int i = getCurrentIndex();
		if (i > 0)
			setImage(i-1);
	}
	
	private void next() {
		int i = getCurrentIndex();
		if (i >= 0 && i < getNbImages()-2)
			setImage(i+1);
	}
	
	private int getIndex(DataImage img) {
		int i = 0;
		synchronized (categories) {
			for (ImageCategory cat : categories)
				for (DataImage di : cat.images) {
					if (di == img) return i;
					i++;
				}
		}
		return -1;
	}
	private int getCurrentIndex() {
		Image img = labelImage.getImage();
		if (img == null) return -1;
		int i = 0;
		synchronized (categories) {
			for (ImageCategory cat : categories)
				for (DataImage di : cat.images) {
					if (di.resized == img) return i;
					i++;
				}
		}
		return -1;
	}
	private int getNbImages() {
		int i = 0;
		synchronized (categories) {
			for (ImageCategory cat : categories)
				i += cat.images.size();
		}
		return i;
	}
	private DataImage getImageAt(int index) {
		int i = 0;
		synchronized (categories) {
			for (ImageCategory cat : categories)
				for (DataImage di : cat.images) {
					if (i == index)
						return di;
					i++;
				}
		}
		return null;
	}
	
	private void setImage(int index) {
		setImage(getImageAt(index));
	}
	private void setImage(DataImage i) {
		labelImage.setImage(i != null ? i.resized : defaultImage);
		linkName.setText(i != null ? i.image.getName() : Local.No_image.toString());
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
