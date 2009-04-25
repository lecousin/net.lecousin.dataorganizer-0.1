package net.lecousin.dataorganizer.people.ui;

import java.util.Set;

import net.lecousin.dataorganizer.core.database.info.InfoRetrieverPluginRegistry;
import net.lecousin.dataorganizer.people.PeopleContentType;
import net.lecousin.dataorganizer.people.PeopleDataType;
import net.lecousin.dataorganizer.people.PeopleInfo;
import net.lecousin.dataorganizer.people.PeopleSourceInfo;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.text.lcml.LCMLText;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public class DescriptionPanel {

	public static void create(PeopleDataType data, Composite parent) {
		PeopleInfo i = (PeopleInfo)data.getInfo();
		if (i == null) return;
		Set<String> sources = i.getSources();
		boolean hasResume = false;
		for (String source : sources) {
			PeopleSourceInfo vsi = i.getSourceInfo(source);
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
				item.setText(InfoRetrieverPluginRegistry.getNameForSource(source, PeopleContentType.PEOPLE_TYPE));
				item.setImage(InfoRetrieverPluginRegistry.getIconForSource(source, PeopleContentType.PEOPLE_TYPE));
				LCMLText text = new LCMLText(folder, false, false);
				text.setText(resume);
				item.setControl(text.getControl());
			}
		}
	}
	
}
