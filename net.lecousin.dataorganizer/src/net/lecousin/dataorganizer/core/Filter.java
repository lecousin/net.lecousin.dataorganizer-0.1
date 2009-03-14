package net.lecousin.dataorganizer.core;

import java.util.Set;

import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.framework.math.RangeInteger;
import net.lecousin.framework.math.RangeLong;
import net.lecousin.framework.strings.StringUtil;

public abstract class Filter {

	public Filter(Filter previous) {
		this.previous = previous;
	}
	private Filter previous;
	
	public final boolean accept(Data data) {
		if (previous != null) {
			if (!previous.accept(data)) return false;
		}
		return _accept(data);
	}
	protected abstract boolean _accept(Data data);
	
	
	public static class FilterContentType extends Filter {
		public FilterContentType(Set<String> types, Filter previous) {
			super(previous);
			this.types = types;
		}
		private Set<String> types;
		@Override
		protected boolean _accept(Data data) {
			return types.contains(data.getContentType().getID());
		}
	}
	
	public static class FilterDateAdded extends Filter {
		public FilterDateAdded(RangeLong range, Filter previous) {
			super(previous);
			this.range = range;
		}
		private RangeLong range;
		@Override
		protected boolean _accept(Data data) {
			long date = data.getDateAdded();
			if (range.min > 0 && date < range.min) return false;
			if (range.max > 0 && date > range.max) return false;
			return true;
		}
	}
	
	public static class FilterDateOpened extends Filter {
		public FilterDateOpened(RangeLong range, Filter previous) {
			super(previous);
			this.range = range;
		}
		private RangeLong range;
		@Override
		protected boolean _accept(Data data) {
			for (long date : data.getViews()) {
				if (range.min > 0 && date < range.min) continue;
				if (range.max > 0 && date > range.max) continue;
				return true;
			}
			return false;
		}
	}
	
	public static class FilterName extends Filter {
		public FilterName(String name, Filter previous) {
			super(previous);
			this.name = StringUtil.normalizeString(name).split(" ");
		}
		private String[] name;
		@Override
		protected boolean _accept(Data data) {
			String dname = StringUtil.normalizeString(data.getName());
			for (int i = 0; i < name.length; ++i)
				if (name[i].length() > 0 && !dname.contains(name[i]))
					return false;
			return true;
		}
	}
	
	public static class FilterRate extends Filter {
		public FilterRate(RangeInteger rate, Filter previous) {
			super(previous);
			this.rate = rate;
		}
		private RangeInteger rate;
		@Override
		protected boolean _accept(Data data) {
			byte r = data.getRate();
			if (r < rate.min) return false;
			if (r > rate.max) return false;
			return true;
		}
	}
}
