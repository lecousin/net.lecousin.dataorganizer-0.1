package net.lecousin.dataorganizer.people.ui;

import java.util.List;

import net.lecousin.dataorganizer.core.database.info.Info.DataLink;
import net.lecousin.dataorganizer.people.Local;
import net.lecousin.dataorganizer.people.PeopleDataType;
import net.lecousin.dataorganizer.people.PeopleSourceInfo;
import net.lecousin.dataorganizer.ui.control.DataLinkListPanel;
import net.lecousin.framework.Pair;
import net.lecousin.framework.time.DateTimeUtil;
import net.lecousin.framework.ui.eclipse.UIUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public class OverviewPanel extends Composite {

	public OverviewPanel(Composite parent, PeopleDataType people, PeopleSourceInfo source) {
		super(parent, SWT.NONE);
		setBackground(parent.getBackground());
		GridLayout layout = UIUtil.gridLayout(parent, 1);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		UIUtil.gridLayout(this, 1);
		UIUtil.gridDataHorizFill(this);
		
		StringBuilder str = new StringBuilder();
		if (source.getBirthDay() != 0)
			str.append("Né le ").append(DateTimeUtil.getDateString(source.getBirthDay()));
		if (source.getBirthPlace() != null) {
			if (str.length() == 0) str.append(Local.Born_at).append(' '); else str.append(' ').append(Local.at).append(' ');
			str.append(source.getBirthPlace());
		}
		UIUtil.newLabel(this, str.toString());
		
		TabFolder folder = new TabFolder(this, SWT.NONE);
		folder.setBackground(parent.getBackground());
		UIUtil.gridDataHorizFill(folder);
		for (String name : source.getActivities().keySet()) {
			TabItem item = new TabItem(folder, SWT.NONE);
			item.setText(name);
			DataLinkListPanel panel = new DataLinkListPanel(folder, new Provider(source.getActivities().get(name)));
			item.setControl(panel);
		}
	}
	
	private static class Provider implements DataLinkListPanel.ListProvider {
		Provider(Pair<List<String>,List<List<DataLink>>> p) {
			links = p.getValue2();
			freeTexts = p.getValue1();
		}
		private List<List<DataLink>> links;
		private List<String> freeTexts;
		
		public String[] getTitles() { return new String[] { Local.Activity.toString() }; }
		public int getNbRows() { return links.size() + freeTexts.size(); }
		public Object[] getRow(int index) {
			if (index < links.size())
				return new Object[] { links.get(index) };
			return new Object[] { freeTexts.get(index-links.size()) };
		}
	}
}
