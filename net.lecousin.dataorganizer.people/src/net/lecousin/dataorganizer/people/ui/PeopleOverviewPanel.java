package net.lecousin.dataorganizer.people.ui;

import java.util.List;

import net.lecousin.dataorganizer.core.database.info.SourceInfo;
import net.lecousin.dataorganizer.core.database.info.SourceInfoMergeUtil;
import net.lecousin.dataorganizer.people.Local;
import net.lecousin.dataorganizer.people.PeopleDataType;
import net.lecousin.dataorganizer.people.PeopleSourceInfo;
import net.lecousin.dataorganizer.people.PeopleSourceInfo.MergedActivity;
import net.lecousin.dataorganizer.ui.control.DataLinkListPanel;
import net.lecousin.framework.collections.SelfMap;
import net.lecousin.framework.collections.SelfMapLinkedList;
import net.lecousin.framework.time.DateTimeUtil;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.UIControlUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public class PeopleOverviewPanel extends Composite {

	public PeopleOverviewPanel(Composite parent, PeopleDataType people, List<SourceInfo> sources, boolean big) {
		super(parent, SWT.NONE);
		setBackground(parent.getBackground());
		GridLayout layout = UIUtil.gridLayout(parent, 1);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		UIUtil.gridLayout(this, 1);
		UIUtil.gridDataHorizFill(this);
		
		labelBirth = UIUtil.newLabel(this, "");
		refreshBirth(sources);
		
		folderActivities = new TabFolder(this, SWT.NONE);
		folderActivities.setBackground(parent.getBackground());
		UIUtil.gridDataHorizFill(folderActivities);
		refreshActivities(sources);
	}
	
	private Label labelBirth;
	private TabFolder folderActivities;
	
	public void refresh(List<SourceInfo> sources) {
		refreshBirth(sources);
		refreshActivities(sources);
	}
	private void refreshBirth(List<SourceInfo> sources) {
		StringBuilder str = new StringBuilder();
		long birthday = -1;
		for (SourceInfo source : sources)
			birthday = SourceInfoMergeUtil.mergeDate(birthday, ((PeopleSourceInfo)source).getBirthDay());
		if (birthday > 0)
			str.append("Né le ").append(DateTimeUtil.getDateString(birthday));
		String birthplace = null;
		for (SourceInfo source : sources)
			birthplace = SourceInfoMergeUtil.mergeString(birthplace, ((PeopleSourceInfo)source).getBirthPlace());
		if (birthplace != null && birthplace.length() > 0) {
			if (str.length() == 0) str.append(Local.Born_at).append(' '); else str.append(' ').append(Local.at).append(' ');
			str.append(birthplace);
		}
		labelBirth.setText(str.toString());
	}
	private void refreshActivities(List<SourceInfo> sources) {
		SelfMap<String,MergedActivity> activities = new SelfMapLinkedList<String,MergedActivity>();
		for (SourceInfo source : sources)
			PeopleSourceInfo.mergeActivities(activities, ((PeopleSourceInfo)source).getActivities());
		for (TabItem item : folderActivities.getItems()) {
			MergedActivity a = activities.get(item.getText());
			if (a == null)
				item.dispose();
			else {
				activities.remove(a);
				refreshActivity(a, item);
			}
		}
		for (MergedActivity a : activities) {
			TabItem item = new TabItem(folderActivities, SWT.NONE);
			item.setText(a.name);
			DataLinkListPanel panel = new DataLinkListPanel(folderActivities, new Provider(a));
			item.setControl(panel);
			panel.addControlListener(new ControlListener() {
				public void controlMoved(ControlEvent e) {
				}
				public void controlResized(ControlEvent e) {
					UIControlUtil.autoresize(PeopleOverviewPanel.this);
				}
			});
		}
	}
	private void refreshActivity(MergedActivity a, TabItem item) {
		((DataLinkListPanel)item.getControl()).resetProvider(new Provider(a));
	}
	
	private static class Provider implements DataLinkListPanel.ListProvider {
		Provider(MergedActivity a) {
			this.a = a;
		}
		private MergedActivity a;
		
		public String[] getTitles() { return new String[] { Local.Activity.toString() }; }
		public int getNbRows() { return a.freeTexts.size() + a.links.size(); }
		public Object[] getRow(int index) {
			if (index < a.links.size())
				return new Object[] { a.links.get(index) };
			return new Object[] { a.freeTexts.get(index-a.links.size()) };
		}
	}
}
