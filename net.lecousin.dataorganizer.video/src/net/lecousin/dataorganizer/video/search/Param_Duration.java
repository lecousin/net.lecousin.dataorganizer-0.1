package net.lecousin.dataorganizer.video.search;

import net.lecousin.dataorganizer.core.DataOrganizer;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.search.Filter;
import net.lecousin.dataorganizer.core.search.DataSearch.ReversableParameter;
import net.lecousin.dataorganizer.video.Local;
import net.lecousin.dataorganizer.video.VideoContentType;
import net.lecousin.dataorganizer.video.VideoDataType;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.math.RangeLong;
import net.lecousin.framework.ui.eclipse.control.date.TimeRangePanel;
import net.lecousin.framework.ui.eclipse.event.DisposeListenerWithData;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class Param_Duration extends ReversableParameter {

	private RangeLong rangeDuration = new RangeLong(0,0);

	public void setRangeDuration(RangeLong range) {
		if (range.equals(rangeDuration)) return;
		rangeDuration = range;
		signalChange();
	}
	public RangeLong getRangeDuration() { return new RangeLong(rangeDuration.min, rangeDuration.max); }
	
	@Override
	public String getParameterName() { return Local.Duration.toString(); }
	@Override
	public String getParameterHelp() { return Local.HELP_Search_Duration.toString(); }
	@Override
	public Control createControl(Composite parent) {
		TimeRangePanel c = new TimeRangePanel(parent);
		refresh(c);
		c.setRange(getRangeDuration());
		c.rangeChanged().addListener(new Listener<RangeLong>() {
			public void fire(RangeLong event) {
				setRangeDuration(event);
			}
		});
		Events events = new Events(c);
		DataOrganizer.database().dataAdded().addListener(events.dataAdded);
		DataOrganizer.database().dataRemoved().addListener(events.dataRemoved);
		DataOrganizer.database().dataChanged().addListener(events.dataChanged);
		c.addDisposeListener(new DisposeListenerWithData<Events>(events) {
			public void widgetDisposed(DisposeEvent e) {
				DataOrganizer.database().dataAdded().removeListener(data().dataAdded);
				DataOrganizer.database().dataRemoved().removeListener(data().dataRemoved);
				DataOrganizer.database().dataChanged().removeListener(data().dataChanged);
			}
		});
		return c;
	}

	private void refresh(TimeRangePanel panel) {
		long min = 0, max = 0;
		for (Data data : DataOrganizer.database().getAllData()) {
			if (!data.getContentType().getID().equals(VideoContentType.VIDEO_TYPE)) continue;
			long duration = ((VideoDataType)data.getContent()).getDuration();
			if (duration <= 0) continue;
			if (min == 0 || duration < min) min = duration;
			if (max == 0 || duration > max) max = duration;
		}
		panel.setMinimum(min);
		panel.setMaximum(max == 0 ? Long.MAX_VALUE : max);
	}
	private class Events {
		Events(TimeRangePanel panel) {
			dataAdded = new DataAdded(panel);
			dataRemoved = new DataRemoved(panel);
			dataChanged = new DataChanged(panel);
		}
		DataAdded dataAdded;
		DataRemoved dataRemoved;
		DataChanged dataChanged;
	}
	private class DataAdded implements Listener<Data> {
		DataAdded(TimeRangePanel panel) { this.panel = panel; }
		TimeRangePanel panel;
		public void fire(Data event) {
			if (!event.getContentType().getID().equals(VideoContentType.VIDEO_TYPE)) return;
			long duration = ((VideoDataType)event.getContent()).getDuration();
			if (duration <= 0) return;
			if (duration < panel.getMinimum())
				panel.setMinimum(duration);
			else if (duration > panel.getMaximum())
				panel.setMaximum(duration);
		}
	}
	private class DataRemoved implements Listener<Data> {
		DataRemoved(TimeRangePanel panel) { this.panel = panel; }
		TimeRangePanel panel;
		public void fire(Data event) {
			if (!event.getContentType().getID().equals(VideoContentType.VIDEO_TYPE)) return;
			refresh(panel);
		}
	}
	private class DataChanged implements Listener<Data> {
		DataChanged(TimeRangePanel panel) { this.panel = panel; }
		TimeRangePanel panel;
		public void fire(Data event) {
			if (!event.getContentType().getID().equals(VideoContentType.VIDEO_TYPE)) return;
			refresh(panel);
		}
	}
	
	@Override
	public Filter getFilter(Filter filter) {
		if (rangeDuration.min == 0 && rangeDuration.max == 0) return filter;
		return new FilterDuration(filter);
	}
	public class FilterDuration extends Filter {
		public FilterDuration(Filter previous) {
			super(previous);
		}
		@Override
		protected boolean _accept(Data data) {
			VideoDataType video = (VideoDataType)data.getContent();
			if (rangeDuration.min > 0 && video.getDuration() < rangeDuration.min) return false;
			if (rangeDuration.max > 0 && video.getDuration() > rangeDuration.max) return false;
			return true;
		}
		@Override
		protected boolean isEnabled(Data data) {
			return data.getContentType().getID().equals(VideoContentType.VIDEO_TYPE);
		}
	}
	
}
