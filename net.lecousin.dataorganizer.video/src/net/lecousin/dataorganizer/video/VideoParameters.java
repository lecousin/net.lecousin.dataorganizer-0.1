package net.lecousin.dataorganizer.video;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.lecousin.dataorganizer.core.Filter;
import net.lecousin.dataorganizer.core.DataSearch.ContentTypeParameters;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.info.Info.DataLink;
import net.lecousin.framework.Pair;
import net.lecousin.framework.event.Event;
import net.lecousin.framework.math.RangeLong;
import net.lecousin.framework.strings.StringUtil;

public class VideoParameters implements ContentTypeParameters {

	private RangeLong rangeDuration = new RangeLong(0,0);
	private String casting = "";
	
	private Event<String> changed = new Event<String>();
	
	public void setRangeDuration(RangeLong range) {
		if (range.equals(rangeDuration)) return;
		rangeDuration = range;
		changed.fire(VideoContentType.VIDEO_TYPE);
	}
	public RangeLong getRangeDuration() { return new RangeLong(rangeDuration.min, rangeDuration.max); }
	public void setCasting(String value) {
		String s = StringUtil.normalizeString(value).trim();
		if (s.equalsIgnoreCase(casting)) return;
		casting = s;
		changed.fire(VideoContentType.VIDEO_TYPE);
	}
	public String getCasting() { return casting; }
	
	public Event<String> contentTypeSearchChanged() { return changed; }
	public Filter getFilter(Filter previous) {
		Filter filter = previous;
		if (rangeDuration.min != 0 || rangeDuration.max != 0)
			filter = new FilterDuration(filter);
		if (casting.length() > 0)
			filter = new FilterCasting(filter);
		return filter;
	}
	
	public class FilterDuration extends Filter {
		public FilterDuration(Filter previous) {
			super(previous);
		}
		@Override
		protected boolean _accept(Data data) {
			if (!data.getContentType().getID().equals(VideoContentType.VIDEO_TYPE)) return true;
			VideoDataType video = (VideoDataType)data.getContent();
			if (rangeDuration.min > 0 && video.getDuration() < rangeDuration.min) return false;
			if (rangeDuration.max > 0 && video.getDuration() > rangeDuration.max) return false;
			return true;
		}
	}
	
	public class FilterCasting extends Filter {
		public FilterCasting(Filter previous) {
			super(previous);
		}
		@Override
		protected boolean _accept(Data data) {
			if (!data.getContentType().getID().equals(VideoContentType.VIDEO_TYPE)) return true;
			VideoInfo info = (VideoInfo)data.getContent().getInfo();
			Set<String> values = new HashSet<String>();
			addList(info.getActors(), values);
			addList(info.getDirectors(), values);
			addList(info.getProductors(), values);
			addList(info.getScenaristes(), values);
			if (values.isEmpty()) return false;
			String[] strs = casting.split(" ");
			// filter words
			int decal = 0;
			for (int i = 0; i < strs.length; ++i) {
				String s = strs[i].trim();
				if (s.length() == 0) decal++;
				strs[i-decal] = s;
			}
			// for each value of the data, if it contains all words, it is selected
			for (String v : values) {
				int i;
				for (i = 0; i < strs.length - decal; ++i)
					if (!v.contains(strs[i])) break;
				if (i == strs.length - decal) // all words found
					return true;
			}
			return false;
		}
		private void addList(List<Pair<List<String>,List<DataLink>>> list, Set<String> result) {
			for (Pair<List<String>,List<DataLink>> p : list)
				for (DataLink link : p.getValue2()) {
					String s = StringUtil.normalizeString(link.name.trim());
					if (s.length() > 0)
						result.add(s);
				}
		}
	}
}
