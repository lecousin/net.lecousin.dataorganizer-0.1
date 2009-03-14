package net.lecousin.dataorganizer.ui.action;

import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.LCProgressBar;
import net.lecousin.framework.ui.eclipse.dialog.MyDialog;
import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class TestAction extends Action {

	public static final String ID = "net.lecousin.dataorganizer.action.TestAction";
	
	public TestAction() {
		super("Test", SharedImages.getImageDescriptor(SharedImages.icons.x16.basic.ERROR));
		setId(ID);
	}

	@Override
	public void run() {
		D d = new D();
		d.open();
	}
	
	private class D extends MyDialog {
		D() {
			super(null);
		}
		@Override
		protected Composite createControl(Composite container) {
			Composite panel = new Composite(container, SWT.NONE);
			UIUtil.gridLayout(panel, 1);
			LCProgressBar pb = new LCProgressBar(panel, LCProgressBar.Style.ROUND, ColorUtil.get(60, 60, 240));
			pb.setMinimum(0);
			pb.setMaximum(100);
			pb.setPosition(75);
			UIUtil.gridDataHorizFill(pb).heightHint = 20;
			return panel;
		}
		public void open() {
			super.open("Test", FLAG_CLOSABLE | FLAG_BORDER | FLAG_RESIZABLE | FLAG_MODAL);
		}
	}
}
