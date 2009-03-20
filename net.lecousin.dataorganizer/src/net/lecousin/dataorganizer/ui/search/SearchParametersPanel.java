package net.lecousin.dataorganizer.ui.search;

import java.util.List;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.search.DataSearch.Parameter;
import net.lecousin.dataorganizer.core.search.DataSearch.ReversableParameter;
import net.lecousin.framework.Pair;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.dialog.CalloutToolTip;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class SearchParametersPanel extends Composite {

	public SearchParametersPanel(Composite parent, List<Parameter> parameters) {
		super(parent, SWT.NONE);
		setBackground(parent.getBackground());
		UIUtil.gridLayout(this, 4);
		
		Image help = SharedImages.getImage(SharedImages.icons.x16.basic.HELP);
		Image reverse = SharedImages.getImage(SharedImages.icons.x10.basic.REVERSE);
		
		Label label;
		GridData gd;
		Control c;
		
		for (Parameter p : parameters) {
			if (p instanceof ReversableParameter) {
				c = UIUtil.newImageToggleButton(this, reverse, new Reverse(), (ReversableParameter)p);
				c.setToolTipText(Local.Reverse_search.toString());
				gd = new GridData();
				gd.verticalAlignment = SWT.BEGINNING;
				gd.heightHint = 17;
				gd.widthHint = 17;
				c.setLayoutData(gd);
			} else
				UIUtil.newLabel(this, "");
			
			label = UIUtil.newLabel(this, p.getParameterName());
			gd = new GridData();
			gd.verticalAlignment = SWT.BEGINNING;
			label.setLayoutData(gd);
			
			c = p.createControl(this);
			gd = UIUtil.gridDataHoriz(1, true);
			gd.verticalAlignment = SWT.BEGINNING;
			c.setLayoutData(gd);
			
			c = UIUtil.newImageButton(this, help, new Help(p), c);
			gd = new GridData();
			gd.verticalAlignment = SWT.BEGINNING;
			c.setLayoutData(gd);
		}
	}
	
	private class Reverse implements Listener<Pair<ReversableParameter,Boolean>> {
		public void fire(Pair<ReversableParameter, Boolean> event) {
			event.getValue1().setReverse(event.getValue2());
		}
	}
	
	private class Help implements Listener<Control> {
		public Help(Parameter p) { this.param = p; }
		Parameter param;
		public void fire(Control control) {
			CalloutToolTip.open(control, CalloutToolTip.Orientation.TOP_RIGHT, param.getParameterHelp(), 5000, -1);
			control.setFocus();
		}
	}

}
