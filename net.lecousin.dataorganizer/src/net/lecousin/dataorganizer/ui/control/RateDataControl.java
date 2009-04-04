

package net.lecousin.dataorganizer.ui.control;

import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.framework.event.Event.Listener;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;

public class RateDataControl extends RateControl {

	public RateDataControl(Composite parent, Data data, boolean editable) {
		super(parent, data != null ? data.getRate() : -1, editable);
		this.data = data;
		if (data != null)
			data.modified().addListener(dataChangedListener);
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (RateDataControl.this.data != null)
					RateDataControl.this.data.modified().removeListener(dataChangedListener);
			}
		});
		rateChanged().addListener(new Listener<Byte>() {
			public void fire(Byte event) {
				if (RateDataControl.this.data.getRate() == event) return;
				RateDataControl.this.data.setRate(event);
			}
		});
	}
	
	private Data data;
	private Listener<Data> dataChangedListener = new Listener<Data>() {
		public void fire(Data event) {
			setRate(event.getRate());
		}
	};
	
	public void setData(Data data) {
		if (this.data != null)
			this.data.modified().removeListener(dataChangedListener);
		this.data = data;
		if (data == null)
			setRate((byte)-1);
		else {
			setRate(data.getRate());
			data.modified().addListener(dataChangedListener);
		}
	}
	
	public void edit() {
		if (data == null) return;
		super.edit();
	}
}