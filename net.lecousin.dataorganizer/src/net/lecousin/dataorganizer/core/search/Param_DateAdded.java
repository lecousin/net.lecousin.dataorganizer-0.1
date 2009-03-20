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

public class Param_DateAdded extends ReversableParameter {

	private RangeLong dateAddedRange = new RangeLong(0, 0);

	public void setDateAddedRange(RangeLong range) {
		if (dateAddedRange.equals(range)) return;
		dateAddedRange = range;
		signalChange();
	}

	@Override
	public String getParameterName() { return Local.Added.toString(); }
	@Override
	public String getParameterHelp() { return Local.HELP_Search_Added.toString(); }
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
				setDateAddedRange(event);
			}
		});
		Events events = new Events(c);
		DataOrganizer.database().dataAdded().addListener(events.dataAdded);
		DataOrganizer.database().dataRemoved().addListener(events.dataRemoved);
		c.addDisposeListener(new DisposeListenerWithData<Events>(events) {
			public void widgetDisposed(DisposeEvent e) {
				DataOrganizer.database().dataAdded().removeListener(data().dataAdded);
				DataOrganizer.database().dataRemoved().removeListener(data().dataRemoved);
			}
		});
		
		return c;
	}
	
	private void refresh(DateRangePanel panel) {
		long min = 0, max = 0;
		for (Data data : DataOrganizer.database().getAllData()) {
			long date = data.getDateAdded();
			if (min == 0 || date < min) min = date;
			if (max == 0 || date > max) max = date;
		}
		panel.setMinimum(min);
		panel.setMaximum(max);
	}
	private class Events {
		Events(DateRangePanel panel) {
			dataAdded = new DataAdded(panel);
			dataRemoved = new DataRemoved(panel);
		}
		DataAdded dataAdded;
		DataRemoved dataRemoved;
	}
	private class DataAdded implements Listener<Data> {
		DataAdded(DateRangePanel panel) { this.panel = panel; }
		DateRangePanel panel;
		public void fire(Data event) {
			long date = event.getDateAdded();
			if (date != 0 && date < panel.getMinimum())
				panel.setMinimum(date);
			else if (date != 0 && date > panel.getMaximum())
				panel.setMaximum(date);
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
		if (dateAddedRange.min == 0 && dateAddedRange.max == 0) return filter;
		return new FilterDateAdded(filter);
	}
	public class FilterDateAdded extends Filter {
		public FilterDateAdded(Filter previous) {
			super(previous, getReverse());
		}
		@Override
		protected boolean _accept(Data data) {
			long date = data.getDateAdded();
			if (dateAddedRange.min > 0 && date < dateAddedRange.min) return false;
			if (dateAddedRange.max > 0 && date > dateAddedRange.max) return false;
			return true;
		}
		@Override
		protected boolean isEnabled(Data data) {
			return true;
		}
	}
	
}
