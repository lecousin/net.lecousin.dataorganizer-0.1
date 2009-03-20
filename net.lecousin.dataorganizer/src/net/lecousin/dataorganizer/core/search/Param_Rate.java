package net.lecousin.dataorganizer.core.search;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.search.DataSearch.ReversableParameter;
import net.lecousin.framework.math.RangeInteger;
import net.lecousin.framework.strings.StringUtil;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

public class Param_Rate extends ReversableParameter {

	private RangeInteger rateRange = null;

	public void setRateRange(RangeInteger range) {
		if (range == null) {
			if (rateRange == null) return;
			rateRange = null;
		} else {
			if (range.min < 0) range.min = 0;
			if (range.max > 20) range.max = 20;
			if (range.equals(rateRange)) return;
			rateRange = range;
		}
		signalChange();
	}

	@Override
	public String getParameterName() { return Local.Rate.toString(); }
	@Override
	public String getParameterHelp() { return Local.HELP_Search_Rate.toString(); }
	@Override
	public Control createControl(Composite parent) {
		return UIUtil.newText(parent, rateRange == null ? "" : Integer.toString(rateRange.min)+'-'+rateRange.max, new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				Text text = (Text)e.widget;
				String s = text.getText();
				if (s.length() == 0)
					setRateRange(null);
				else
					try { 
						setRateRange(StringUtil.toRangeInteger(s)); 
						text.setBackground(ColorUtil.getWhite());
					} catch (NumberFormatException ex) {
						text.setBackground(ColorUtil.getOrange());
					}
			}
		});
	}

	@Override
	public Filter getFilter(Filter filter) {
		if (rateRange == null) return filter;
		return new FilterRate(filter);
	}
	public class FilterRate extends Filter {
		public FilterRate(Filter previous) {
			super(previous, getReverse());
		}
		@Override
		protected boolean _accept(Data data) {
			byte r = data.getRate();
			if (r < rateRange.min) return false;
			if (r > rateRange.max) return false;
			return true;
		}
		@Override
		protected boolean isEnabled(Data data) {
			return true;
		}
	}
	
}
