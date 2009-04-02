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

public class Param_AlbumName extends ReversableParameter {

	private String album = "";

	public void setAlbum(String value) {
		String s = StringUtil.normalizeString(value).trim();
		if (s.equalsIgnoreCase(album)) return;
		album = s;
		signalChange();
	}
	public String getAlbum() { return album; }

	@Override
	public String getParameterName() { return Local.Album.toString(); }
	@Override
	public String getParameterHelp() { return Local.HELP_Search_Album.toString(); }
	@Override
	public Control createControl(Composite parent) {
		return UIUtil.newText(parent, getAlbum(), new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setAlbum(((Text)e.widget).getText());
			}
		});
	}
	
	@Override
	public Filter getFilter(Filter filter) {
		if (album.trim().length() == 0) return filter;
		return new FilterAlbum(filter);
	}
	public class FilterAlbum extends Filter {
		public FilterAlbum(Filter previous) {
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
			String value = info.getAlbum();
			if (value == null || value.length() == 0) return false;
			value = value.toLowerCase();
			String[] strs = album.split(" ");
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
