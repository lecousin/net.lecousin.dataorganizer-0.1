package net.lecousin.dataorganizer.updater;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class WaitDataOrganizerToBeClose {

	public static boolean waitClose() {
		frame = new Frame() {
			private static final long serialVersionUID = 3074078246008719537L;
			@Override
			public void paint(Graphics g) {
				g.setColor(new Color(0, 0, 0));
				g.drawRect(0, 0, 399, 69);
				g.drawString("Please close all the instances of DataOrganizer before to continue.", 18, 20);
				
				g.setColor(new Color(230, 230, 230));
				if (onContinue)
					g.fillRect(95, 35, 65, 20);
				if (onCancel)
					g.fillRect(215, 35, 50, 20);
				
				g.setColor(new Color(0, 0, 0));
				g.drawString("Continue", 100, 50);
				g.drawString("Cancel", 220, 50);
				
				if (pushContinue)
					drawDown(g, 95, 35, 65, 20);
				else
					drawUp(g, 95, 35, 65, 20);
				if (pushCancel)
					drawDown(g, 215, 35, 50, 20);
				else
					drawUp(g, 215, 35, 50, 20);

				super.paint(g);
			}
		};
		frame.setTitle("DataOrganizer Updater");
		// TODO icon
		frame.setAlwaysOnTop(true);
		frame.setResizable(false);
		frame.setUndecorated(true);
		frame.setSize(400, 70);
		Dimension dim = frame.getToolkit().getScreenSize();
		frame.setLocation(dim.width/2-200, dim.height/2-50);
		frame.setLayout(null);
		frame.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
			}
			public void mouseEntered(MouseEvent e) {
			}
			public void mouseExited(MouseEvent e) {
				onContinue = onCancel = false;
				frame.repaint();
			}
			public void mousePressed(MouseEvent e) {
				boolean cont = in(e.getX(), e.getY(), 95, 35, 65, 20);
				boolean cancel = in(e.getX(), e.getY(), 215, 35, 50, 20);
				if (cont != pushContinue || cancel != pushCancel) {
					pushContinue = cont;
					pushCancel = cancel;
					frame.repaint();
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (pushContinue) {
					pushContinue = false;
					frame.repaint();
					result = true;
					frame.setVisible(false);
				} else if (pushCancel) {
					pushCancel = false;
					frame.repaint();
					result = false;
					frame.setVisible(false);
				}
			}
		});
		frame.addMouseMotionListener(new MouseMotionListener() {
			public void mouseDragged(MouseEvent e) {
			}
			public void mouseMoved(MouseEvent e) {
				boolean cont = in(e.getX(), e.getY(), 95, 35, 65, 20);
				boolean cancel = in(e.getX(), e.getY(), 215, 35, 50, 20);
				if (cont != onContinue || cancel != onCancel) {
					onContinue = cont;
					onCancel = cancel;
					frame.repaint();
				}
			}
		});

		frame.setVisible(true);
		while (result == null) {
			try { Thread.sleep(100); } catch (InterruptedException e) { return false; }
		}
		return result;
	}
	
	static Frame frame;
	static Boolean result = null;
	static boolean onContinue = false;
	static boolean onCancel = false;
	static boolean pushContinue = false;
	static boolean pushCancel = false;
	
    static final Color SHADOW_DOWN = new Color(128, 128, 128);
    static final Color SHADOW_UP = new Color(240, 240, 240);
	
    static void drawUp(Graphics g, int x, int y, int w, int h) {
        g.setColor(SHADOW_UP);
        g.drawLine(x, y, x+w-1, y);
        g.drawLine(x, y, x, y+h-2);
        g.setColor(SHADOW_DOWN);
        g.drawLine(x+w-1, y+1, x+w-1, y+h-1);
        g.drawLine(x, y+h-1, x+w-1, y+h-1);
    }
    
    static void drawDown(Graphics g, int x, int y, int w, int h) {
        g.setColor(SHADOW_DOWN);
        g.drawLine(x, y, x+w-1, y);
        g.drawLine(x, y, x, y+h-2);
        g.setColor(SHADOW_UP);
        g.drawLine(x+w-1, y+1, x+w-1, y+h-1);
        g.drawLine(x, y+h-1, x+w-1, y+h-1);
    }
    
    static boolean in(int posx, int posy, int x, int y, int w, int h) {
    	return posx >= x && posx <= (x+w) && posy >= y && posy <= (y+h);
    }
}
