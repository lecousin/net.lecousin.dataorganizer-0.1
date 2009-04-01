package net.lecousin.dataorganizer.updater;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;

public class ErrorDlg extends Frame {

	private static final long serialVersionUID = -4925067003777583026L;

	public ErrorDlg(String message, Throwable t) {
		msg1 = message;
		msg2 = t != null ? t.getMessage() : null;
		setTitle("Error");
		setAlwaysOnTop(true);
		setResizable(false);
		setUndecorated(true);
		setVisible(true);
		createContent();

		while (!closed) {
			try { Thread.sleep(100); } catch (InterruptedException e) { return; }
		}
	}
	
	private String msg1;
	private String msg2;
	private boolean closed = false;
	private Button buttonClose;
	
	private void createContent() {
		FontMetrics fm = getGraphics().getFontMetrics();
		int x1 = getGraphics().getFontMetrics().stringWidth(msg1);
		int x2 = msg2 != null ? getGraphics().getFontMetrics().stringWidth(msg2) : 0;
		int x = Math.max(x1, x2) + 20;
		int y = 5 + fm.getHeight() + 5;
		if (msg2 != null) y += fm.getHeight() + 5;
		buttonClose = new Button(this, "Close", x/2, y);
		buttonClose.click.addFireListener(new Runnable() {
			public void run() {
				closed = true;
				setVisible(false);
			}
		});
		y += 25;
		y += 5;
		setSize(x, y);
		Dimension dim = getToolkit().getScreenSize();
		setLocation(dim.width/2-x/2, dim.height/2-y/2);
		setLayout(null);
	}

	@Override
	public void paint(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		int y = fm.getHeight();
		g.setColor(new Color(0, 0, 0));
		Dimension size = getSize();
		g.drawRect(0, 0, size.width-1, size.height-1);
		g.drawString(msg1, 10, 5+y-3);
		if (msg2 != null)
			g.drawString(msg2, 10, 5+y+5+y-3);
		
		buttonClose.paint(g);

		super.paint(g);
	}
	
}
