package net.lecousin.dataorganizer.updater;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;

import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.progress.WorkProgress;

public class ProgressDlg extends Frame {

	private static final long serialVersionUID = 3057986129121436032L;

	public ProgressDlg(WorkProgress progress) {
		this.progress = progress;
		setTitle("DataOrganizer Updater");
		setAlwaysOnTop(true);
		setResizable(false);
		setUndecorated(true);
		setSize(600, 50);
		Dimension dim = getToolkit().getScreenSize();
		setLocation(dim.width/2-300, dim.height/2-25);
		setLayout(null);
		setVisible(true);
		progress.addProgressListener(new WorkListener());
	}
	
	private WorkProgress progress;
	
	public void close() {
		setVisible(false);
	}
	
	@Override
	public void paint(Graphics g) {
		g.setColor(new Color(0, 0, 0));
		g.drawRect(0, 0, 599, 49);
		g.drawString(progress.getDescription(), 10, 17);

		int w = (int)((long)progress.getPosition()*580/progress.getAmount());
		g.setColor(new Color(40, 40, 255));
		g.fillRect(10, 23, w, 15);
		g.setColor(new Color(0, 0, 0));
		g.drawRect(10, 23, 580, 15);

		super.paint(g);
	}
	
	private class WorkListener implements Listener<WorkProgress> {
		public void fire(WorkProgress event) {
			repaint();
		}
	}
}
