package net.lecousin.dataorganizer.neufbox.mediacenter;

import java.io.File;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.source.DataSource;
import net.lecousin.dataorganizer.ui.plugin.Action;
import net.lecousin.dataorganizer.ui.plugin.ActionProvider;
import net.lecousin.neufbox.mediacenter.Media;
import net.lecousin.neufbox.mediacenter.MediaCenter;
import net.lecousin.neufbox.mediacenter.eclipse.NeufBoxMediaCenter;
import net.lecousin.neufbox.mediacenter.eclipse.SharedDataView;

import org.eclipse.swt.graphics.Image;

public class MediaCenterActionProvider implements ActionProvider {

	public MediaCenterActionProvider() {
	}
	
	public int getPriority() {
		return 1000;
	}

	public List<Action> getActions(List<Data> data) {
		List<Action> list = new LinkedList<Action>();
		list.add(new ActionAdd(data));
		return list;
	}
	
	private static class ActionAdd implements Action {
		public ActionAdd(List<Data> data) { this.data = data; }
		private List<Data> data;
		public Image getIcon() {
			return NeufBoxMediaCenter.getIcon();
		}
		public String getText() {
			return Local.Add_to_media_center.toString();
		}
		public void run() {
			SharedDataView view = SharedDataView.show();
			if (view == null) return;
			MediaCenter mc = view.getMediaCenter();
			if (mc == null) return;
			for (Data d : data) {
				List<DataSource> sources = d.getSources();
				if (sources == null || sources.isEmpty()) continue;
				URI uri = sources.get(0).ensurePresenceAndGetURI();
				File file = new File(uri);
				mc.getRoot().newMedia(d.getName(), Media.TYPE_MOVIE, file, d);
			}
		}
		public Type getType() {
			return Type.SEND;
		}
		public boolean isSame(Action action) {
			if (action == null || !(action instanceof ActionAdd)) return false;
			return data == ((ActionAdd)action).data;
		}
	}

}
