package net.lecousin.dataorganizer.core.search;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.lecousin.dataorganizer.core.DataOrganizer;
import net.lecousin.dataorganizer.core.DataLabels.Label;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.content.ContentType;
import net.lecousin.dataorganizer.core.database.content.DataContentType;
import net.lecousin.framework.Pair;
import net.lecousin.framework.event.Event;
import net.lecousin.framework.event.Event.Listener;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class DataSearch {

	public static abstract class Parameter {
		private final Event<Parameter> changed = new Event<Parameter>();
		protected final void signalChange() {
			changed.fire(this);
		}
		public final Event<Parameter> parameterChanged() { return changed; }
		
		public abstract Filter getFilter(Filter filter);
		
		public abstract String getParameterName();
		public abstract String getParameterHelp();
		public abstract Control createControl(Composite parent);
	}
	public static abstract class ReversableParameter extends Parameter {
		private boolean reverse = false;
		public boolean getReverse() { return reverse; }
		public void setReverse(boolean value) {
			if (value == reverse) return;
			reverse = value;
			signalChange();
		}
	}

	private Param_ContentTypes contentTypes;
	private Param_Labels labelsParameter;
	private List<Parameter> mainParameters = new LinkedList<Parameter>();
	private Map<String,List<Parameter>> contentTypeParameters = new HashMap<String,List<Parameter>>();
	private Collection<Data> result = null;
	private Event<DataSearch> searchChanged = new Event<DataSearch>();
	private Event<Data> dataAdded = new Event<Data>();
	private Event<Data> dataRemoved = new Event<Data>();
	
	public DataSearch() {
		registerParameter(labelsParameter = new Param_Labels());
		registerParameter(contentTypes = new Param_ContentTypes());
		addParameter(new Param_Name());
		addParameter(new Param_Rate());
		addParameter(new Param_DateAdded());
		addParameter(new Param_DateOpened());
		for (ContentType type : ContentType.getAvailableTypes()) {
			List<Parameter> params = type.createSearchParameters();
			if (params == null || params.isEmpty()) continue;
			contentTypeParameters.put(type.getID(), params);
			for (Parameter p : params)
				registerParameter(p);
		}
		searchChanged.addFireListener(new Runnable() {
			public void run() {
				synchronized(DataSearch.this) { result = null; }
			}
		});
	}
	private void addParameter(Parameter p) {
		mainParameters.add(p);
		registerParameter(p);
	}
	private void registerParameter(Parameter p) {
		p.parameterChanged().addFireListener(new Runnable() {
			public void run() {
				searchChanged.fire(DataSearch.this);
			}
		});
	}
	public void registerDBEvents() {
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
		DataOrganizer.database().dataContentChanged().addListener(new Listener<DataContentType>() {
			public void fire(DataContentType event) {
				updateData(event.getData());
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
	
	public List<Parameter> getMainParameters() { return mainParameters; }
	public Param_ContentTypes getContentTypes() { return contentTypes; }
	public Param_Labels getLabelsParameter() { return labelsParameter; }
	public List<Parameter> getContentTypeParameters(String contentTypeID) { return contentTypeParameters.get(contentTypeID); }
	public Collection<Data> getResult() {
		synchronized (DataSearch.this) {
			if (result == null) computeResult();
			return result;
		}
	}
	public Event<DataSearch> searchChanged() { return searchChanged; }
	public Event<Data> dataAdded() { return dataAdded; }
	public Event<Data> dataRemoved() { return dataRemoved; }

	private void computeResult() {
		result = DataOrganizer.database().getAllData();
		Filter filter = getFilter();
		if (filter != null)
			for (Iterator<Data> it = result.iterator(); it.hasNext(); ) {
				Data data = it.next();
				if (!filter.accept(data))
					it.remove();
			}
	}
	
	public boolean isMatching(Data data) {
		Filter filter = getFilter();
		return filter == null || filter.accept(data);
	}
	
	private Filter getFilter() {
		Filter filter = contentTypes.getFilter(null);
		filter = labelsParameter.getFilter(filter);
		for (Parameter p : mainParameters)
			filter = p.getFilter(filter);
		for (String type : contentTypes.getContentTypes()) {
			List<Parameter> params = contentTypeParameters.get(type);
			if (params == null) continue;
			for (Parameter p : params)
				filter = p.getFilter(filter);
		}
		return filter;
	}
}
