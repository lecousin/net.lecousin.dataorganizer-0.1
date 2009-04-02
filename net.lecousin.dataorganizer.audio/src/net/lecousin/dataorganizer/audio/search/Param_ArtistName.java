package net.lecousin.dataorganizer.audio.search;

import net.lecousin.dataorganizer.audio.AudioContentType;
import net.lecousin.dataorganizer.audio.AudioInfo;
import net.lecousin.dataorganizer.audio.Local;
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

public class Param_ArtistName extends ReversableParameter {

	private String artist = "";

	public void setArtist(String value) {
		String s = StringUtil.normalizeString(value).trim();
		if (s.equalsIgnoreCase(artist)) return;
		artist = s;
		signalChange();
	}
	public String getArtist() { return artist; }

	@Override
	public String getParameterName() { return Local.Artist.toString(); }
	@Override
	public String getParameterHelp() { return Local.HELP_Search_Artist.toString(); }
	@Override
	public Control createControl(Composite parent) {
		return UIUtil.newText(parent, getArtist(), new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setArtist(((Text)e.widget).getText());
			}
		});
	}
	
	@Override
	public Filter getFilter(Filter filter) {
		if (artist.trim().length() == 0) return filter;
		return new FilterArtist(filter);
	}
	public class FilterArtist extends Filter {
		public FilterArtist(Filter previous) {
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
			String value = info.getArtist();
			if (value == null || value.length() == 0) return false;
			value = value.toLowerCase();
			String[] strs = artist.split(" ");
			// filter words
			int decal = 0;
			for (int i = 0; i < strs.length; ++i) {
				String s = strs[i].trim().toLowerCase();
				if (s.length() == 0) decal++;
				strs[i-decal] = s;
			}
			// for each value of the data, if it contains all words, it is selected
			int i;
			for (i = 0; i < strs.length - decal; ++i)
				if (!value.contains(strs[i])) break;
			if (i == strs.length - decal) // all words found
				return true;
			return false;
		}
	}
	
}
