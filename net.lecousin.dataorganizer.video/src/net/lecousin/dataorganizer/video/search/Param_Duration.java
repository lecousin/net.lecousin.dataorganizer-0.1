package net.lecousin.dataorganizer.video.search;

import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.search.Filter;
import net.lecousin.dataorganizer.core.search.DataSearch.ReversableParameter;
import net.lecousin.dataorganizer.video.Local;
import net.lecousin.dataorganizer.video.VideoContentType;
import net.lecousin.dataorganizer.video.VideoDataType;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.math.RangeLong;
import net.lecousin.framework.ui.eclipse.control.date.TimeRangePanel;

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
		c.setRange(getRangeDuration());
		c.rangeChanged().addListener(new Listener<RangeLong>() {
			public void fire(RangeLong event) {
				setRangeDuration(event);
			}
		});
		return c;
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
