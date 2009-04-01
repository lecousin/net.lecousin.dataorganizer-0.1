package net.lecousin.dataorganizer.updater;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import net.lecousin.framework.event.Event;

public class Button {

	public Button(Frame frame, String text, int x, int y) {
		this.frame = frame;
		this.text = text;
		FontMetrics fm = frame.getGraphics().getFontMetrics();
		int w = fm.stringWidth(text);
		int h = fm.getHeight();
		bound = new Rectangle(x-w/2-MARGIN_WIDTH, y, w+MARGIN_WIDTH*2, h+MARGIN_HEIGHT*2);
		Mouse mouse = new Mouse();
		frame.addMouseListener(mouse);
		frame.addMouseMotionListener(mouse);
	}
	private static final int MARGIN_WIDTH = 3;
	private static final int MARGIN_HEIGHT = 2;
	
	private Frame frame;
	private String text;
	private boolean onButton = false;
	private boolean pushButton = false;
	private Rectangle bound;
	public Event<Button> click = new Event<Button>();
	
	public void paint(Graphics g) {
		g.setColor(new Color(230, 230, 230));
		if (onButton)
			g.fillRect(bound.x, bound.y, bound.width, bound.height);
		
		g.setColor(new Color(0, 0, 0));
		g.drawString(text, bound.x+MARGIN_WIDTH, bound.y+bound.height-MARGIN_HEIGHT-3);
		
		if (pushButton)
			UIUtil.drawDown(g, bound.x, bound.y, bound.width, bound.height);
		else
			UIUtil.drawUp(g, bound.x, bound.y, bound.width, bound.height);
	}
	
	private class Mouse implements MouseListener, MouseMotionListener {
		public void mouseClicked(MouseEvent e) {
		}
		public void mouseEntered(MouseEvent e) {
		}
		public void mouseExited(MouseEvent e) {
			onButton = false;
			frame.repaint();
		}
		public void mousePressed(MouseEvent e) {
			boolean state = UIUtil.in(e.getX(), e.getY(), bound.x, bound.y, bound.width, bound.height);
			if (state != pushButton) {
				pushButton = state;
				frame.repaint();
			}
		}
		public void mouseReleased(MouseEvent e) {
			if (pushButton) {
				pushButton = false;
				frame.repaint();
				click.fire(Button.this);
			}
		}
		public void mouseDragged(MouseEvent e) {
		}
		public void mouseMoved(MouseEvent e) {
			boolean state = UIUtil.in(e.getX(), e.getY(), bound.x, bound.y, bound.width, bound.height);
			if (state != onButton) {
				onButton = state;
				frame.repaint();
			}
		}
	}
}
