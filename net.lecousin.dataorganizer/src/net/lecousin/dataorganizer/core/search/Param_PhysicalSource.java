package net.lecousin.dataorganizer.core.search;

import java.util.List;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.source.DataSource;
import net.lecousin.dataorganizer.core.search.DataSearch.ReversableParameter;
import net.lecousin.framework.Pair;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.ui.eclipse.UIUtil;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

public class Param_PhysicalSource extends ReversableParameter {

	private boolean onlyLinked = false;
	private String name = "";

	public void setOnlyLinked(boolean value) {
		if (onlyLinked == value) return;
		onlyLinked = value;
		signalChange();
	}
	public void setName(String name) {
		if (this.name.equals(name)) return;
		this.name = name;
		signalChange();
	}

	@Override
	public String getParameterName() { return Local.Files.toString(); }
	@Override
	public String getParameterHelp() { return Local.HELP_Search_Files.toString(); }
	@Override
	public Control createControl(Composite parent) {
		Composite panel = UIUtil.newGridComposite(parent, 0, 0, 1);
		UIUtil.newCheck(panel, Local.Only_data_linked_to_files.toString(), new Listener<Pair<Boolean,Object>>() {
			public void fire(Pair<Boolean, Object> event) {
				setOnlyLinked(event.getValue1());
			}
		}, null).setSelection(onlyLinked);
		Composite p2 = UIUtil.newGridComposite(panel, 0, 0, 2);
		p2.setLayoutData(UIUtil.gridDataHoriz(1, true));
		UIUtil.newLabel(p2, Local.Name.toString());
		UIUtil.newText(p2, name, new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setName(((Text)e.widget).getText());
			}
		}).setLayoutData(UIUtil.gridDataHoriz(1, true));
		return panel;
	}

	@Override
	public Filter getFilter(Filter filter) {
		if (!onlyLinked && name.length() == 0) return filter;
		return new FilterFile(filter);
	}
	public class FilterFile extends Filter {
		public FilterFile(Filter previous) {
			super(previous, getReverse());
		}
		@Override
		protected boolean _accept(Data data) {
			List<DataSource> sources = data.getSources();
			if (onlyLinked && sources.isEmpty()) return false;
			if (name.length() == 0) return true;
			for (DataSource s : sources)
				if (s.getFileName().contains(name))
					return true;
			return false;
		}
		@Override
		protected boolean isEnabled(Data data) {
			return true;
		}
	}
	
}
