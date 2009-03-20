package net.lecousin.dataorganizer.core.search;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.search.DataSearch.ReversableParameter;
import net.lecousin.framework.strings.StringUtil;
import net.lecousin.framework.ui.eclipse.UIUtil;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

public class Param_Name extends ReversableParameter {

	private String name = "";

	public void setName(String name) {
		if (this.name.equals(name)) return;
		this.name = name;
		signalChange();
	}
	
	@Override
	public String getParameterName() { return Local.Name.toString(); }
	@Override
	public String getParameterHelp() { return Local.HELP_Search_Name.toString(); }
	@Override
	public Control createControl(Composite parent) {
		return UIUtil.newText(parent, name, new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String name = ((Text)e.widget).getText();
				setName(name);
			}
		});
	}

	@Override
	public Filter getFilter(Filter filter) {
		if (name.trim().length() == 0) return filter;
		return new FilterName(name, filter, getReverse());
	}
	public static class FilterName extends Filter {
		public FilterName(String name, Filter previous, boolean reverse) {
			super(previous, reverse);
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
		@Override
		protected boolean isEnabled(Data data) {
			return true;
		}
	}
	
}
