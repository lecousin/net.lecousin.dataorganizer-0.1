package net.lecousin.dataorganizer.video.search;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.info.Info.DataLink;
import net.lecousin.dataorganizer.core.search.Filter;
import net.lecousin.dataorganizer.core.search.DataSearch.ReversableParameter;
import net.lecousin.dataorganizer.video.Local;
import net.lecousin.dataorganizer.video.VideoContentType;
import net.lecousin.dataorganizer.video.VideoInfo;
import net.lecousin.framework.Pair;
import net.lecousin.framework.strings.StringUtil;
import net.lecousin.framework.ui.eclipse.UIUtil;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

public class Param_Casting extends ReversableParameter {

	private String casting = "";

	public void setCasting(String value) {
		String s = StringUtil.normalizeString(value).trim();
		if (s.equalsIgnoreCase(casting)) return;
		casting = s;
		signalChange();
	}
	public String getCasting() { return casting; }

	@Override
	public String getParameterName() { return Local.Casting.toString(); }
	@Override
	public String getParameterHelp() { return Local.HELP_Search_Casting.toString(); }
	@Override
	public Control createControl(Composite parent) {
		return UIUtil.newText(parent, getCasting(), new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setCasting(((Text)e.widget).getText());
			}
		});
	}
	
	@Override
	public Filter getFilter(Filter filter) {
		if (casting.trim().length() == 0) return filter;
		return new FilterCasting(filter);
	}
	public class FilterCasting extends Filter {
		public FilterCasting(Filter previous) {
			super(previous);
		}
		@Override
		protected boolean isEnabled(Data data) {
			return data.getContentType().getID().equals(VideoContentType.VIDEO_TYPE);
		}
		@Override
		protected boolean _accept(Data data) {
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
