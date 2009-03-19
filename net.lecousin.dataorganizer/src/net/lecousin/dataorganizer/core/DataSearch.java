package net.lecousin.dataorganizer.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.lecousin.dataorganizer.core.DataLabels.Label;
import net.lecousin.dataorganizer.core.Filter.FilterContentType;
import net.lecousin.dataorganizer.core.Filter.FilterDateAdded;
import net.lecousin.dataorganizer.core.Filter.FilterDateOpened;
import net.lecousin.dataorganizer.core.Filter.FilterName;
import net.lecousin.dataorganizer.core.Filter.FilterRate;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.content.ContentType;
import net.lecousin.framework.Pair;
import net.lecousin.framework.event.Event;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.math.RangeInteger;
import net.lecousin.framework.math.RangeLong;

public class DataSearch {

	public interface ContentTypeParameters {
		public Event<String> contentTypeSearchChanged();
		public Filter getFilter(Filter previous);
	}
	public class Parameters {
		private Set<Label> labels = new HashSet<Label>();
		private boolean notLabeled = false;
		private Set<String> contentTypes = new HashSet<String>(); 
		private String name = "";
		private RangeInteger rateRange = new RangeInteger(0, 20);
		private RangeLong dateAddedRange = new RangeLong(0, 0);
		private RangeLong dateOpenedRange = new RangeLong(0, 0);
		private Map<String,ContentTypeParameters> contentTypesParameters = new HashMap<String,ContentTypeParameters>();
		
		public Object getParameters(String content_type_id) {
			if (!contentTypesParameters.containsKey(content_type_id)) {
				ContentTypeParameters o = ContentType.getContentType(content_type_id).createSearchParameters();
				contentTypesParameters.put(content_type_id, o);
				o.contentTypeSearchChanged().addListener(new Listener<String>() {
					public void fire(String event) {
						// TODO improve with the type??
						searchChanged.fire(DataSearch.this);
					}
				});
				return o;
			}
			return contentTypesParameters.get(content_type_id);
		}
		
		public void setName(String name) {
			if (this.name.equals(name)) return;
			this.name = name;
			searchChanged.fire(DataSearch.this);
		}
		public void setRateRange(RangeInteger range) {
			if (range.min < 0) range.min = 0;
			if (range.max > 20) range.max = 20;
			if (rateRange.equals(range)) return;
			rateRange = range;
			searchChanged.fire(DataSearch.this);
		}
		public void setDateAddedRange(RangeLong range) {
			if (dateAddedRange.equals(range)) return;
			dateAddedRange = range;
			searchChanged.fire(DataSearch.this);
		}
		public void setDateOpenedRange(RangeLong range) {
			if (dateOpenedRange.equals(range)) return;
			dateOpenedRange = range;
			searchChanged.fire(DataSearch.this);
		}
		public void setContentType(String id, boolean selected) {
			boolean changed = selected ? contentTypes.add(id) : contentTypes.remove(id);
			if (changed) searchChanged.fire(DataSearch.this);
		}
		public void setLabels(Collection<Label> list, boolean notLabeled) {
			labels.clear();
			labels.addAll(list);
			this.notLabeled = notLabeled;
			searchChanged.fire(DataSearch.this);
		}
		public void setNotLabeled(boolean value) {
			if (notLabeled == value) return;
			notLabeled = value;
			searchChanged.fire(DataSearch.this);
		}
		public void setLabel(Label label, boolean selected) {
			boolean changed = selected ? labels.add(label) : labels.remove(label);
			if (changed) searchChanged.fire(DataSearch.this);
		}
		
		public boolean isContentTypesSet() { return !contentTypes.isEmpty(); }
		public boolean isLabelsSet() { return !labels.isEmpty() || notLabeled; }
		public boolean isSet() { return isContentTypesSet() && isLabelsSet(); }
	}
	
	private Parameters parameters = new Parameters();
	private Collection<Data> result = null;
	private Event<DataSearch> searchChanged = new Event<DataSearch>();
	private Event<Data> dataAdded = new Event<Data>();
	private Event<Data> dataRemoved = new Event<Data>();
	
	public DataSearch() {
		searchChanged.addFireListener(new Runnable() {
			public void run() {
				synchronized(DataSearch.this) { result = null; }
			}
		});
	}
	void registerDBEvents() {
		DataOrganizer.database().dataAdded().addListener(new Listener<Data>() {
			public void fire(Data event) {
				if (result == null) return;
				if (isMatching(event)) {
					synchronized (DataSearch.this) {
						result.add(event);
					}
					dataAdded.fire(event);
				}
			}
		});
		DataOrganizer.database().dataChanged().addListener(new Listener<Data>() {
			public void fire(Data event) {
				updateData(event);
			}
		});
		DataOrganizer.database().dataRemoved().addListener(new Listener<Data>() {
			public void fire(Data event) {
				if (result == null) return;
				boolean removed;
				synchronized (DataSearch.this) {
					removed = result.remove(event);
				}
				if (removed) dataRemoved.fire(event);
			}
		});
		DataOrganizer.labels().labelAssigned().addListener(new Listener<Pair<Label,Data>>() {
			public void fire(Pair<Label,Data> event) {
				updateData(event.getValue2());
			}
		});
		DataOrganizer.labels().labelUnassigned().addListener(new Listener<Pair<Label,Data>>() {
			public void fire(Pair<Label,Data> event) {
				updateData(event.getValue2());
			}
		});
	}
	private void updateData(Data data) {
		if (result == null) return;
		boolean match = isMatching(data);
		boolean added = false;
		boolean removed = false;
		synchronized (DataSearch.this) {
			if (match) {
				if (!result.contains(data)) {
					result.add(data);
					added = true;
				}
			} else if (result.remove(data))
				removed = true;
		}
		if (added) dataAdded.fire(data);
		else if (removed) dataRemoved.fire(data);
	}
	
	public Parameters getParameters() { return parameters; }
	public Collection<Data> getResult() {
		synchronized (DataSearch.this) {
			if (result == null) computeResult();
			return result;
		}
	}
	public Event<DataSearch> searchChanged() { return searchChanged; }
	public Event<Data> dataAdded() { return dataAdded; }
	public Event<Data> dataRemoved() { return dataRemoved; }

	public boolean containsLabel(Label label) {
		Collection<Data> result = getResult();
		for (Data data : result)
			if (label.hasData(data))
				return true;
		return false;
	}
	public boolean containsContentType(ContentType type) {
		Collection<Data> result = getResult();
		for (Data data : result)
			if (data.getContentType() == type)
				return true;
		return false;
	}

	private void computeResult() {
		// retrieve by labels
		if (!parameters.isLabelsSet())
			result = DataOrganizer.database().getAllData();
		else {
			if (parameters.notLabeled)
				result = DataOrganizer.labels().getNotLabeledData();
			else
				result = DataOrganizer.database().getAllData();
			
			for (Label label : parameters.labels)
				for (Iterator<Data> it = result.iterator(); it.hasNext(); )
					if (!label.hasData(it.next()))
						it.remove();
		}
		
		Filter filter = getFilter();
		if (filter != null)
			for (Iterator<Data> it = result.iterator(); it.hasNext(); ) {
				Data data = it.next();
				if (!filter.accept(data))
					it.remove();
			}
	}
	
	public boolean isMatching(Data data) {
		if (parameters.isLabelsSet()) {
			List<Label> labels = DataOrganizer.labels().getLabels(data);
			if (labels.isEmpty()) {
				if (!parameters.notLabeled) return false;
			} else {
				if (parameters.labels.isEmpty() && parameters.notLabeled) return false;
				if (!labels.containsAll(parameters.labels)) return false;
			}
		}
		Filter filter = getFilter();
		return filter == null || filter.accept(data);
	}
	
	private Filter getFilter() {
		Filter filter = null;
		// content type
		if (parameters.isContentTypesSet())
			filter = new FilterContentType(parameters.contentTypes, filter);
		// date added
		if (parameters.dateAddedRange.min > 0 || parameters.dateAddedRange.max > 0)
			filter = new FilterDateAdded(parameters.dateAddedRange, filter);
		// date opened
		if (parameters.dateOpenedRange.min > 0 || parameters.dateOpenedRange.max > 0)
			filter = new FilterDateOpened(parameters.dateOpenedRange, filter);
		// name
		if (parameters.name.length() > 0)
			filter = new FilterName(parameters.name, filter);
		// rate
		if (parameters.rateRange.min > 0 || parameters.rateRange.max < 20)
			filter = new FilterRate(parameters.rateRange, filter);
		if (parameters.isContentTypesSet()) {
			for (String type : parameters.contentTypes) {
				ContentTypeParameters o = parameters.contentTypesParameters.get(type);
				if (o != null)
					filter = o.getFilter(filter);
			}
		}
		return filter;
	}
}
