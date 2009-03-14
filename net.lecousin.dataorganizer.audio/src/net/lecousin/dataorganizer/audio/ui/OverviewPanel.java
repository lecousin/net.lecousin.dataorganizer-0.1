package net.lecousin.dataorganizer.audio.ui;

import net.lecousin.dataorganizer.audio.AudioDataType;
import net.lecousin.dataorganizer.audio.AudioInfo;
import net.lecousin.dataorganizer.audio.Local;
import net.lecousin.framework.ui.eclipse.UIUtil;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class OverviewPanel {

	public OverviewPanel(Composite panel, AudioDataType data) {
		GridLayout layout = UIUtil.gridLayout(panel, 1);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		
		AudioInfo info = (AudioInfo)data.getInfo();
		
		Composite line = UIUtil.newGridComposite(panel, 0, 0, 6);
		UIUtil.gridDataHorizFill(line);
		UIUtil.newLabel(line, Local.Artist+":", true, false);
		UIUtil.newText(line, info.getArtist(), new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				// TODO
			}
		}).setLayoutData(UIUtil.gridDataHoriz(1, true));
		UIUtil.newLabel(line, Local.Album+":", true, false);
		UIUtil.newText(line, info.getAlbum(), new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				// TODO
			}
		}).setLayoutData(UIUtil.gridDataHoriz(1, true));
		UIUtil.newLabel(line, Local.Year+":", true, false);
		UIUtil.newText(line, info.getYear() > 0 ? Integer.toString(info.getYear()) : "", new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				// TODO
			}
		}).setLayoutData(UIUtil.gridDataHoriz(1, true));
	}
	
}
