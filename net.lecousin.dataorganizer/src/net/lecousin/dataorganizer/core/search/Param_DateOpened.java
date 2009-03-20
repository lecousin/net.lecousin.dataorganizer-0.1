package net.lecousin.dataorganizer.core.search;

import java.util.Calendar;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.DataOrganizer;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.search.DataSearch.ReversableParameter;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.math.RangeLong;
import net.lecousin.framework.time.DateTimeUtil;
import net.lecousin.framework.ui.eclipse.control.date.DateRangePanel;
import net.lecousin.framework.ui.eclipse.event.DisposeListenerWithData;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class Param_DateOpened extends ReversableParameter {

	private RangeLong dateOpenedRange = new RangeLong(0, 0);

	public void setDateOpenedRange(RangeLong range) {
		if (dateOpenedRange.equals(range)) return;
		dateOpenedRange = range;
		signalChange();
	}
	
	@Override
	public String getParameterName() { return Local.Opened.toString(); }
	@Override
	public String getParameterHelp() { return Local.HELP_Search_Opened.toString(); }
	@Override
	public Control createControl(Composite parent) {
		DateRangePanel c = new DateRangePanel(parent);
		refresh(c);
		c.rangeChanged().addListener(new Listener<RangeLong>() {
			public void fire(RangeLong event) {
				Calendar c = Calendar.getInstance();
				if (event.min != 0) {
					c.setTimeInMillis(event.min);
					DateTimeUtil.resetHours(c);
					event.min = c.getTimeInMillis();
				}
				if (event.max != 0) {
					c.setTimeInMillis(event.max);
					DateTimeUtil.resetHours(c);
					c.add(Calendar.DAY_OF_MONTH, 1);
					event.max = c.getTimeInMillis();
				}
				setDateOpenedRange(event);
			}
		});
		Events events = new Events(c);
		DataOrganizer.database().dataChanged().addListener(events.dataChanged);
		DataOrganizer.database().dataRemoved().addListener(events.dataRemoved);
		c.addDisposeListener(new DisposeListenerWithData<Events>(events) {
			public void widgetDisposed(DisposeEvent e) {
				DataOrganizer.database().dataChanged().removeListener(data().dataChanged);
				DataOrganizer.database().dataRemoved().removeListener(data().dataRemoved);
			}
		});
		
		return c;
	}
	
	private void refresh(DateRangePanel panel) {
		long min = 0, max = 0;
		for (Data data : DataOrganizer.database().getAllData()) {
			for (Long date : data.getViews()) {
				if (min == 0 || date < min) min = date;
				if (max == 0 || date > max) max = date;
			}
		}
		panel.setMinimum(min);
		panel.setMaximum(max == 0 ? Long.MAX_VALUE : max);
	}
	private class Events {
		Events(DateRangePanel panel) {
			dataChanged = new DataChanged(panel);
			dataRemoved = new DataRemoved(panel);
		}
		DataChanged dataChanged;
		DataRemoved dataRemoved;
	}
	private class DataChanged implements Listener<Data> {
		DataChanged(DateRangePanel panel) { this.panel = panel; }
		DateRangePanel panel;
		public void fire(Data event) {
			refresh(panel);
		}
	}
	private class DataRemoved implements Listener<Data> {
		DataRemoved(DateRangePanel panel) { this.panel = panel; }
		DateRangePanel panel;
		public void fire(Data event) {
			refresh(panel);
		}
	}

	@Override
	public Filter getFilter(Filter filter) {
		if (dateOpenedRange.min == 0 && dateOpenedRange.max == 0) return filter;
		return new FilterDateOpened(filter);
	}
	public class FilterDateOpened extends Filter {
		public FilterDateOpened(Filter previous) {
			super(previous, getReverse());
		}
		@Override
		protected boolean _accept(Data data) {
			for (long date : data.getViews()) {
				if (dateOpenedRange.min > 0 && date < dateOpenedRange.min) continue;
				if (dateOpenedRange.max > 0 && date > dateOpenedRange.max) continue;
				return true;
			}
			return false;
		}
		@Override
		protected boolean isEnabled(Data data) {
			return true;
		}
	}
}
