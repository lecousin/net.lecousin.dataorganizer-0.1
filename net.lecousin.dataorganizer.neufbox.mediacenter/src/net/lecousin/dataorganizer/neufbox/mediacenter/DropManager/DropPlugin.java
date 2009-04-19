package net.lecousin.dataorganizer.neufbox.mediacenter.DropManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.source.DataSource;
import net.lecousin.dataorganizer.ui.DataOrganizerDND;
import net.lecousin.neufbox.mediacenter.Folder;
import net.lecousin.neufbox.mediacenter.MediaCenter;
import net.lecousin.neufbox.mediacenter.eclipse.DropManager;
import net.lecousin.neufbox.mediacenter.eclipse.DropManager.Plugin;

import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.TreeItem;

public class DropPlugin implements Plugin {

	public DropPlugin() {
	}

	public Collection<Transfer> getTransfers() {
		List<Transfer> list = new LinkedList<Transfer>();
		list.add(TextTransfer.getInstance());
		return list;
	}
	
	public TransferData getTransfer(DropTargetEvent event) {
		TransferData support = null;
		if (TextTransfer.getInstance().isSupportedType(event.currentDataType))
			support = event.currentDataType;
		else {
			for (TransferData d : event.dataTypes)
				if (TextTransfer.getInstance().isSupportedType(d)) {
					support = d;
					break;
				}
		}
		if (support != null)
			return support;
		return null;
	}
	
	public boolean drop(DropTargetEvent event, TreeItem item, MediaCenter mc, DropManager manager) {
		if (TextTransfer.getInstance().isSupportedType(event.currentDataType)) {
			String str = (String)event.data;
			if (DataOrganizerDND.isData(str)) {
				List<Data> list = DataOrganizerDND.getDataDNDFromString(str);
				for (Data data : list) {
					List<File> files = new LinkedList<File>();
					for (DataSource source : data.getSources())
						try { files.add(new File(source.ensurePresenceAndGetURI())); }
						catch (FileNotFoundException e) {}
					if (files.isEmpty()) continue;
					if (files.size() == 1) {
						manager.addFile(mc, item, files.get(0));
					} else {
						Folder folder = manager.addFolder(mc, item, data.getName());
						for (File file : files)
							manager.addFile(folder, file);
					}
				}
				return true;
			}
		}
		return false;
	}
}
