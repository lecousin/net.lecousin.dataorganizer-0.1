package net.lecousin.dataorganizer.neufbox.mediacenter;

import java.io.File;
import java.net.URI;
import java.util.List;

import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.source.DataSource;
import net.lecousin.dataorganizer.ui.plugin.Action;
import net.lecousin.neufbox.mediacenter.Folder;
import net.lecousin.neufbox.mediacenter.MediaCenter;
import net.lecousin.neufbox.mediacenter.eclipse.NeufBoxMediaCenter;
import net.lecousin.neufbox.mediacenter.eclipse.SharedDataView;

import org.eclipse.swt.graphics.Image;

public class ActionAdd implements Action {
	public ActionAdd(List<Data> data, int type) { this.data = data; this.type = type; }
	private List<Data> data;
	private int type;
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
			if (sources.size() == 1) {
				DataSource source = sources.get(0);
				if (source == null) continue;
				URI uri = source.ensurePresenceAndGetURI();
				File file = new File(uri);
				mc.getRoot().newMedia(d.getName(), type, file, d);
			} else {
				Folder folder = mc.getRoot().newSubFolder(d.getName());
				for (DataSource source : sources) {
					if (source == null) continue;
					URI uri = source.ensurePresenceAndGetURI();
					File file = new File(uri);
					folder.newMedia(d.getName(), type, file, d);
				}
			}
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
