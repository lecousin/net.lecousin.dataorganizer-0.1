package net.lecousin.dataorganizer.ui.dialog;

import net.lecousin.dataorganizer.core.database.refresh.RefreshOptions;
import net.lecousin.dataorganizer.ui.control.RefreshOptionsControl;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.browser.Local;
import net.lecousin.framework.ui.eclipse.control.buttonbar.OkCancelButtonsPanel;
import net.lecousin.framework.ui.eclipse.dialog.FlatDialog;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public class RefreshDialog extends FlatDialog {

	public RefreshDialog(Shell shell) {
		this(shell, null);
	}
	public RefreshDialog(Shell shell, RefreshOptions options) {
		super(shell, Local.Refresh.toString(), false, true);
		this.options = options != null ? options : new RefreshOptions();
	}
	
	private RefreshOptions options;
	private boolean ok = false;
	
	@Override
	protected void createContent(Composite container) {
		UIUtil.gridLayout(container, 1);
		UIUtil.gridDataHorizFill(new RefreshOptionsControl(container, options));
		UIUtil.newSeparator(container, true, true);
		new OkCancelButtonsPanel(container, true) {
			@Override
			protected boolean handleOk() {
				ok = true;
				return true;
			}
			@Override
			protected boolean handleCancel() {
				ok = false;
				return true;
			}
		}.centerAndFillInGrid();
	}
	
	public RefreshOptions open() {
		super.openProgressive(null, OrientationY.BOTTOM, true);
		if (!ok) return null;
		return options;
	}
}
