package net.lecousin.dataorganizer.audio.ui;

import java.util.Set;

import net.lecousin.dataorganizer.audio.AudioContentType;
import net.lecousin.dataorganizer.audio.AudioDataType;
import net.lecousin.dataorganizer.audio.AudioInfo;
import net.lecousin.dataorganizer.audio.AudioSourceInfo;
import net.lecousin.dataorganizer.core.database.info.InfoRetrieverPluginRegistry;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.text.lcml.LCMLText;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public class DescriptionPanel {

	public static void create(AudioDataType data, Composite parent) {
		AudioInfo i = (AudioInfo)data.getInfo();
		if (i == null) return;
		Set<String> sources = i.getSources();
		boolean hasResume = false;
		for (String source : sources) {
			AudioSourceInfo vsi = i.getSourceInfo(source);
			if (vsi != null) {
				String s = vsi.getDescription();
				if (s != null && s.length() > 0) { hasResume = true; break; }
			}
		}
		if (hasResume) {
			GridLayout layout = UIUtil.gridLayout(parent, 1);
			layout.marginHeight = layout.marginWidth = 0;
			TabFolder folder = new TabFolder(parent, SWT.NONE);
			folder.setBackground(parent.getBackground());
			UIUtil.gridDataHorizFill(folder);
			for (String source : sources) {
				String resume = i.getSourceInfo(source).getDescription();
				if (resume == null || resume.length() == 0) continue;
				TabItem item = new TabItem(folder, SWT.NONE);
				item.setText(InfoRetrieverPluginRegistry.getNameForSource(source, AudioContentType.AUDIO_TYPE));
				item.setImage(InfoRetrieverPluginRegistry.getIconForSource(source, AudioContentType.AUDIO_TYPE));
				LCMLText text = new LCMLText(folder, false, true);
				text.setText(resume);
				item.setControl(text.getControl());
			}
		}
	}
	
}
