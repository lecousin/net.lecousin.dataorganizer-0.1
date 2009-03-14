package net.lecousin.dataorganizer.ui.dataoverview;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.UIControlUtil;
import net.lecousin.framework.ui.eclipse.control.text.lcml.LCMLText;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class ReviewControl extends Composite {

	public ReviewControl(Composite parent, String source, String author, String comment, Integer note, Data data) {
		super(parent, SWT.NONE);
		setBackground(parent.getBackground());
		UIUtil.gridLayout(this, 4, 0, 0);
		UIUtil.newLabel(this, author, true, false);
		UIUtil.newLabel(this, note != null ? note + "/20" : Local.no_rated.toString());
		UIUtil.newLabel(this, Local.Source+":");
		new SourceControl(this, data, source);
		LCMLText text = new LCMLText(this, false, false);
		text.setLayoutData(UIUtil.gridDataHoriz(4, true));
		text.setText(comment);
	}
	
	@Override
	public Point computeSize(int hint, int hint2, boolean changed) {
		hint = 500;
		Point size = super.computeSize(hint, hint2, changed);
		//if (hint == SWT.DEFAULT && size.x < 500) size.x = 500;
		size.x = 500;
		return size;
	}
	
	@Override
	public void setBackground(Color color) {
		for (Control child : getChildren())
			UIControlUtil.setBackground(child, color);
		super.setBackground(color);
	}
}
