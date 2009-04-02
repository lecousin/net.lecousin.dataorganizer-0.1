package net.lecousin.dataorganizer.audio.search;

import java.util.HashSet;
import java.util.Set;

import net.lecousin.dataorganizer.audio.AudioContentType;
import net.lecousin.dataorganizer.audio.AudioInfo;
import net.lecousin.dataorganizer.audio.AudioSourceInfo;
import net.lecousin.dataorganizer.audio.Local;
import net.lecousin.dataorganizer.audio.AudioSourceInfo.Track;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.search.Filter;
import net.lecousin.dataorganizer.core.search.DataSearch.ReversableParameter;
import net.lecousin.framework.strings.StringUtil;
import net.lecousin.framework.ui.eclipse.UIUtil;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

public class Param_TrackName extends ReversableParameter {

	private String trackName = "";

	public void setTrackName(String value) {
		String s = StringUtil.normalizeString(value).trim();
		if (s.equalsIgnoreCase(trackName)) return;
		trackName = s;
		signalChange();
	}
	public String getTrackName() { return trackName; }

	@Override
	public String getParameterName() { return Local.Track.toString(); }
	@Override
	public String getParameterHelp() { return Local.HELP_Search_TrackName.toString(); }
	@Override
	public Control createControl(Composite parent) {
		return UIUtil.newText(parent, getTrackName(), new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setTrackName(((Text)e.widget).getText());
			}
		});
	}
	
	@Override
	public Filter getFilter(Filter filter) {
		if (trackName.trim().length() == 0) return filter;
		return new FilterTrackName(filter);
	}
	public class FilterTrackName extends Filter {
		public FilterTrackName(Filter previous) {
			super(previous);
		}
		@Override
		protected boolean isEnabled(Data data) {
			return data.getContentType().getID().equals(AudioContentType.AUDIO_TYPE);
		}
		@Override
		protected boolean _accept(Data data) {
			AudioInfo info = (AudioInfo)data.getContent().getInfo();
			if (info == null) return false;
			Set<String> values = new HashSet<String>();
			for (String source : info.getSources()) {
				AudioSourceInfo i = info.getSourceInfo(source);
				if (i == null) continue;
				for (Track t : i.getTracks()) {
					String title = t.getTitle();
					if (title != null && title.length() > 0)
						values.add(t.getTitle().toLowerCase());
				}
			}
			if (values.isEmpty()) return false;
			String[] strs = trackName.split(" ");
			// filter words
			int decal = 0;
			for (int i = 0; i < strs.length; ++i) {
				String s = strs[i].trim().toLowerCase();
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
	}
	
}
