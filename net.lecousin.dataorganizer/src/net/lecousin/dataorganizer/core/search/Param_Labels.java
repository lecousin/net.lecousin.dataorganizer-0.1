package net.lecousin.dataorganizer.core.search;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.DataOrganizer;
import net.lecousin.dataorganizer.core.DataLabels.Label;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.search.DataSearch.ReversableParameter;

public class Param_Labels extends ReversableParameter {

	private Set<Label> labels = new HashSet<Label>();
	private boolean notLabeled = false;

	public void setLabels(Collection<Label> list, boolean notLabeled) {
		labels.clear();
		labels.addAll(list);
		this.notLabeled = notLabeled;
		signalChange();
	}
	public void setNotLabeled(boolean value) {
		if (notLabeled == value) return;
		notLabeled = value;
		signalChange();
	}
	public void setLabel(Label label, boolean selected) {
		boolean changed = selected ? labels.add(label) : labels.remove(label);
		if (changed) signalChange();
	}
	public Set<Label> getLabels() { return labels; }

	@Override
	public String getParameterName() { return Local.Labels.toString(); }
	@Override
	public String getParameterHelp() { return Local.HELP_Search_Labels.toString(); }
	@Override
	public Control createControl(Composite parent) { return null; }
	
	public boolean resultsContainsLabel(Label label) {
	Collection<Data> result = DataOrganizer.search().getResult();
	for (Data data : result)
		if (label.hasData(data))
			return true;
	return false;
}
	
	@Override
	public Filter getFilter(Filter filter) {
		if (labels.isEmpty() && !notLabeled) return filter;
		return new FilterLabels(filter);
	}
	private class FilterLabels extends Filter {
		public FilterLabels(Filter previous) {
			super(previous, getReverse());
			labeled = DataOrganizer.labels().root().getData();
		}
		Set<Data> labeled;
		@Override
		protected boolean _accept(Data data) {
			boolean hasLabel = labeled.contains(data);
			if (!hasLabel) return notLabeled;
			for (Label label : labels)
				if (label.hasData(data)) return true;
			return false;
		}
		@Override
		protected boolean isEnabled(Data data) {
			return true;
		}
	}
}
