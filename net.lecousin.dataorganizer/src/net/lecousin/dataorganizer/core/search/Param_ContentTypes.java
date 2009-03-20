package net.lecousin.dataorganizer.core.search;

import java.util.HashSet;
import java.util.Set;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.search.DataSearch.Parameter;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class Param_ContentTypes extends Parameter {

	public Param_ContentTypes() {
		//contentTypes.addAll(ContentType.getAvailableTypesID());
		// do not add all content types to avoid going through all data content types to initialize content type search parameters (i.e. go through all video to get the minimum duration...)
	}
	
	private Set<String> contentTypes = new HashSet<String>(); 
	
	public void setContentType(String id, boolean selected) {
		boolean changed = selected ? contentTypes.add(id) : contentTypes.remove(id);
		if (changed) signalChange();
	}
	public Set<String> getContentTypes() { return contentTypes; }
	
	@Override
	public String getParameterName() { return Local.Content_type.toString(); }
	@Override
	public String getParameterHelp() { return null; }
	@Override
	public Control createControl(Composite parent) { return null; }
	
	@Override
	public Filter getFilter(Filter filter) {
		if (contentTypes.isEmpty()) return filter;
		return new FilterContentType(filter);
	}
	public class FilterContentType extends Filter {
		public FilterContentType(Filter previous) {
			super(previous);
		}
		@Override
		protected boolean _accept(Data data) {
			return contentTypes.contains(data.getContentType().getID());
		}
		@Override
		protected boolean isEnabled(Data data) {
			return true;
		}
	}
	
}
