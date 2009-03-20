package net.lecousin.dataorganizer.core.search;

import net.lecousin.dataorganizer.core.database.Data;

public abstract class Filter {

	public Filter(Filter previous) { this(previous, false); }
	public Filter(Filter previous, boolean reverse) {
		this.previous = previous;
		this.reverse = reverse;
	}
	private Filter previous;
	private boolean reverse;
	
	public final boolean accept(Data data) {
		if (previous != null) {
			if (!previous.accept(data)) return false;
		}
		return !isEnabled(data) || reverse != _accept(data);
	}
	protected abstract boolean _accept(Data data);
	protected abstract boolean isEnabled(Data data);
}
