package net.lecousin.dataorganizer.ui.control;

import net.lecousin.dataorganizer.Local;
import net.lecousin.framework.Pair;
import net.lecousin.framework.event.Event;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.ui.eclipse.UIUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;

public class RateEditPanel extends Composite {

	public RateEditPanel(Composite parent, byte rate) {
		super(parent, SWT.NONE);
		setBackground(parent.getBackground());
		UIUtil.gridLayout(this, 4);
		
		labelRate = UIUtil.newLabel(this, rate >= 0 ? Byte.toString(rate) : Local.No.toString());
		GridData gd = new GridData();
		gd.widthHint = 35;
		gd.horizontalAlignment = SWT.RIGHT;
		labelRate.setLayoutData(gd);
		scaleRate = new Scale(this, SWT.HORIZONTAL);
		scaleRate.setBackground(getBackground());
		gd = new GridData();
		gd.heightHint = 20;
		scaleRate.setLayoutData(gd);
		scaleRate.setMinimum(0);
		scaleRate.setMaximum(20);
		scaleRate.setIncrement(1);
		scaleRate.setSelection(rate >= 0 ? rate : 0);
		scaleRate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (noRate.getSelection())
					noRate.setSelection(false);
				labelRate.setText(Integer.toString(scaleRate.getSelection()));
				rateChanged.fire(getRate());
			}
		});
		//if (rate < 0) scaleRate.setEnabled(false);
		noRate = UIUtil.newCheck(this, Local.Do_not_rate_this_data.toString(), new Listener<Pair<Boolean,Object>>() {
			public void fire(Pair<Boolean,Object> event) {
				//scaleRate.setEnabled(!noRate.getSelection());
				byte rate = (byte)(noRate.getSelection() ? -1 : scaleRate.getSelection());
				labelRate.setText(rate >= 0 ? Byte.toString(rate) : Local.No.toString());
				rateChanged.fire(getRate());
			}
		}, null);
		noRate.setSelection(rate < 0);
	}
	
	private Label labelRate;
	private Scale scaleRate;
	private Button noRate;
	private Event<Byte> rateChanged = new Event<Byte>();
	
	public Event<Byte> rateChanged() { return rateChanged; }
	
	public byte getRate() {
		if (noRate.getSelection()) return -1;
		return (byte)scaleRate.getSelection();
		
	}
}
