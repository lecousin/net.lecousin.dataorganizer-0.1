package net.lecousin.dataorganizer.ui.control;

import net.lecousin.dataorganizer.Local;
import net.lecousin.framework.event.Event;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.UIControlUtil;
import net.lecousin.framework.ui.eclipse.dialog.CalloutToolTip;
import net.lecousin.framework.ui.eclipse.dialog.FlatPopupMenu;
import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class RateControl extends Composite {

	public RateControl(Composite parent, byte rate, boolean editable) {
		super(parent, SWT.NONE);
		super.setBackground(parent.getBackground());
		etoile[0] = SharedImages.getImage(SharedImages.icons.x16.basic.STAR_YELLOW_EMPTY);
		etoile[1] = SharedImages.getImage(SharedImages.icons.x16.basic.STAR_YELLOW_QUART);
		etoile[2] = SharedImages.getImage(SharedImages.icons.x16.basic.STAR_YELLOW_HALF);
		etoile[3] = SharedImages.getImage(SharedImages.icons.x16.basic.STAR_YELLOW_3QUART);
		etoile[4] = SharedImages.getImage(SharedImages.icons.x16.basic.STAR_YELLOW_FULL);
		disabled = SharedImages.getImage(SharedImages.icons.x16.basic.STAR_YELLOW_DISABLED);
		UIUtil.gridLayout(this, 5, 2, 2).horizontalSpacing = 0;
		l1 = UIUtil.newImage(this, disabled);
		l2 = UIUtil.newImage(this, disabled);
		l3 = UIUtil.newImage(this, disabled);
		l4 = UIUtil.newImage(this, disabled);
		l5 = UIUtil.newImage(this, disabled);
		l1.setBackground(ColorUtil.getWhite());
		l2.setBackground(ColorUtil.getWhite());
		l3.setBackground(ColorUtil.getWhite());
		l4.setBackground(ColorUtil.getWhite());
		l5.setBackground(ColorUtil.getWhite());
		setRate(rate);
		if (editable)
			UIControlUtil.recursiveMouseListener(this, new Mouse(), true);
		UIControlUtil.recursiveMouseTrackListener(this, new MouseTrack(), true);
	}
	
	private Image[] etoile = new Image[5];
	private Image disabled;
	private Label l1, l2, l3, l4, l5;
	private Event<Byte> rateChanged = new Event<Byte>();
	
	public Event<Byte> rateChanged() { return rateChanged; }
	
	@Override
	public Point computeSize(int hint, int hint2, boolean changed) {
		Point size = new Point(hint, hint2);
		if (hint == SWT.DEFAULT)
			size.x = 5*12+2*2;
		if (hint2 == SWT.DEFAULT)
			size.y = 12+2*2;
		return size;
	}
	@Override
	public void setBackground(Color color) {
//		l1.setBackground(color);
//		l2.setBackground(color);
//		l3.setBackground(color);
//		l4.setBackground(color);
//		l5.setBackground(color);
		super.setBackground(color);
	}
	
	private byte rate = -1;
	public void setRate(byte rate) {
		if (rate == this.rate) return;
		this.rate = rate;
		if (rate < 0) {
			l1.setImage(disabled);
			l2.setImage(disabled);
			l3.setImage(disabled);
			l4.setImage(disabled);
			l5.setImage(disabled);
		} else {
			if (rate < 5)
				l1.setImage(etoile[rate]);
			else
				l1.setImage(etoile[4]);
			if (rate < 5)
				l2.setImage(etoile[0]);
			else if (rate > 7)
				l2.setImage(etoile[4]);
			else
				l2.setImage(etoile[rate-4]);
			if (rate < 9)
				l3.setImage(etoile[0]);
			else if (rate > 11)
				l3.setImage(etoile[4]);
			else
				l3.setImage(etoile[rate-8]);
			if (rate < 13)
				l4.setImage(etoile[0]);
			else if (rate > 15)
				l4.setImage(etoile[4]);
			else
				l4.setImage(etoile[rate-12]);
			if (rate < 17)
				l5.setImage(etoile[0]);
			else if (rate > 19)
				l5.setImage(etoile[4]);
			else
				l5.setImage(etoile[rate-16]);
		}
		rateChanged.fire(rate);
	}
	
	private class Mouse implements MouseListener {
		public void mouseDown(MouseEvent e) {
		}
		public void mouseUp(MouseEvent e) {
			edit();
		}
		public void mouseDoubleClick(MouseEvent e) {
			edit();
		}
	}
	
	public void edit() {
		FlatPopupMenu dlg = new FlatPopupMenu(this, Local.Select_your_rating.toString(), true, true, false, false);
		RateEditPanel rateControl = new RateEditPanel(dlg.getControl(), rate);
		rateControl.rateChanged().addListener(new Listener<Byte>() {
			public void fire(Byte event) {
				setRate(event);
			}
		});
		dlg.show(this, FlatPopupMenu.Orientation.BOTTOM_RIGHT, true);
	}
	
	private class MouseTrack implements MouseTrackListener {
		public void mouseEnter(MouseEvent e) {
		}
		public void mouseExit(MouseEvent e) {
		}
		public void mouseHover(MouseEvent e) {
			String text;
			if (rate < 0) text = Local.Not_rated.toString();
			else text = ""+rate+"/20";
			CalloutToolTip.open(RateControl.this, CalloutToolTip.Orientation.BOTTOM_RIGHT, text, 1500, -1);
		}
	}
}
