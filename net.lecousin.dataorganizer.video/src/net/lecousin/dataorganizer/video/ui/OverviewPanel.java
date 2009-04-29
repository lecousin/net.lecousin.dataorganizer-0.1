package net.lecousin.dataorganizer.video.ui;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.core.database.info.SourceInfo;
import net.lecousin.dataorganizer.core.database.info.SourceInfoMergeUtil;
import net.lecousin.dataorganizer.core.database.info.Info.DataLink;
import net.lecousin.dataorganizer.people.PeopleContentType;
import net.lecousin.dataorganizer.ui.control.DataLinkListPanel;
import net.lecousin.dataorganizer.ui.dialog.DataLinkPopup;
import net.lecousin.dataorganizer.video.Local;
import net.lecousin.dataorganizer.video.VideoDataType;
import net.lecousin.dataorganizer.video.VideoSourceInfo;
import net.lecousin.dataorganizer.video.VideoSourceInfo.Genre;
import net.lecousin.framework.Pair;
import net.lecousin.framework.collections.CollectionUtil;
import net.lecousin.framework.event.Event.ListenerData;
import net.lecousin.framework.time.DateTimeUtil;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.Row_ExpandableList;
import net.lecousin.framework.ui.eclipse.dialog.FlatPopupMenu;
import net.lecousin.framework.ui.eclipse.dialog.MyDialog;
import net.lecousin.framework.ui.eclipse.event.ControlListenerWithData;
import net.lecousin.framework.ui.eclipse.event.HyperlinkListenerWithData;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;

public class OverviewPanel {

	public OverviewPanel(Composite panel, VideoDataType data, List<SourceInfo> sources) {
		GridLayout layout = UIUtil.gridLayout(panel, 1);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		
		Composite line = UIUtil.newComposite(panel);
		UIUtil.gridDataHorizFill(line);
		RowLayout l = new RowLayout(SWT.HORIZONTAL);
		l.marginHeight = l.marginWidth = l.marginBottom = l.marginTop = 0;
		l.spacing = 5;
		l.center = true;
		line.setLayout(l);
		UIUtil.newLabel(line, Local.Duration+":", true, false);
		UIUtil.newLabel(line, (data.getDuration() < 0 ? "-" : DateTimeUtil.getTimeMinimalString(data.getDuration())));
		UIUtil.newLabel(line, Local.Dimension+":", true, false);
		UIUtil.newLabel(line, (data.getDimension() == null ? "-" : (Integer.toString(data.getDimension().x)+'x'+data.getDimension().y)));
		if (sources != null && !sources.isEmpty()) {
			long releaseDate = -1;
			for (SourceInfo i : sources)
				releaseDate = SourceInfoMergeUtil.mergeDate(releaseDate, ((VideoSourceInfo)i).getReleaseDate());
			if (releaseDate > 0) {
				UIUtil.newSeparator(line, false, true);
				UIUtil.newLabel(line, Local.Release+":", true, false);
				UIUtil.newLabel(line, new SimpleDateFormat("dd MMMM yyyy").format(new Date(releaseDate)));
			}
			
			List<Genre> genres = new LinkedList<Genre>();
			for (SourceInfo i : sources)
				for (Genre g : ((VideoSourceInfo)i).getGenres())
					if (!genres.contains(g)) genres.add(g);
			if (!genres.isEmpty()) {
				line = UIUtil.newComposite(panel);
				UIUtil.gridDataHorizFill(line);
				l = new RowLayout(SWT.HORIZONTAL);
				l.marginHeight = l.marginWidth = l.marginBottom = l.marginTop = 0;
				l.spacing = 0;
				line.setLayout(l);
				UIUtil.newLabel(line, Local.Genre+": ", true, false);
				boolean first = true;
				for (Genre genre : genres) {
					if (first) first = false;
					else UIUtil.newLabel(line, ",");
					UIUtil.newLabel(line, genre.getDescription());
				}
			}
			
			List<Pair<List<String>,List<DataLink>>> list = new LinkedList<Pair<List<String>,List<DataLink>>>();
			for (SourceInfo i : sources)
				SourceInfoMergeUtil.mergePeopleLists(list, ((VideoSourceInfo)i).getDirectors());
			createPeopleList(panel, list, Local.Directed_by.toString(), Local.Direction.toString());
			list = new LinkedList<Pair<List<String>,List<DataLink>>>();
			for (SourceInfo i : sources)
				SourceInfoMergeUtil.mergePeopleLists(list, ((VideoSourceInfo)i).getActors());
			createPeopleList(panel, list, Local.Actors.toString(), Local.Casting.toString());
			list = new LinkedList<Pair<List<String>,List<DataLink>>>();
			for (SourceInfo i : sources)
				SourceInfoMergeUtil.mergePeopleLists(list, ((VideoSourceInfo)i).getProductors());
			createPeopleList(panel, list, Local.Producted_by.toString(), Local.Production.toString());
			list = new LinkedList<Pair<List<String>,List<DataLink>>>();
			for (SourceInfo i : sources)
				SourceInfoMergeUtil.mergePeopleLists(list, ((VideoSourceInfo)i).getScenaristes());
			createPeopleList(panel, list, Local.Writen_by.toString(), Local.Write.toString());
		}
	}
	
	private void createPeopleList(Composite parent, List<Pair<List<String>,List<DataLink>>> list, String title, String title2) {
		if (list.isEmpty()) return;
		Row_ExpandableList panel = new Row_ExpandableList(parent);
		UIUtil.gridDataHorizFill(panel);
		
		panel.setHeader(UIUtil.newLabel(panel, title, true, false));
		boolean first = true;
		for (Pair<List<String>,List<DataLink>> p : list) {
			if (first) first = false;
			else UIUtil.newLabel(panel, ",");
			DataLink l = p.getValue2().get(0);
			UIUtil.newLinkSoftNetStyle(panel, l.name, new ListenerData<HyperlinkEvent,DataLink>(l) {
				public void fire(HyperlinkEvent e) {
					DataLinkPopup.open(PeopleContentType.PEOPLE_TYPE, CollectionUtil.single_element_list(data()), (Control)e.widget, FlatPopupMenu.Orientation.TOP_BOTTOM);
				}
			});
		}
		Hyperlink link = UIUtil.newLink(panel, Local.View_all+" (" + list.size() + ")", new HyperlinkListenerWithData<Pair<String,List<Pair<List<String>,List<DataLink>>>>>(new Pair<String,List<Pair<List<String>,List<DataLink>>>>(title2, list)) {
			public void linkActivated(HyperlinkEvent e) {
				FlatPopupMenu dlg = new FlatPopupMenu((Control)e.widget, data().getValue1(), true, true, false, true);
				new DataLinkListPanel(dlg.getControl(), new Provider()).addControlListener(new ControlListenerWithData<MyDialog>(dlg) {
					public void controlMoved(org.eclipse.swt.events.ControlEvent e) {};
					public void controlResized(org.eclipse.swt.events.ControlEvent e) {
						data().resize();
					}
				});
				dlg.show((Control)e.widget, FlatPopupMenu.Orientation.TOP_BOTTOM, false);
			}
			public void linkEntered(HyperlinkEvent e) {
				((Hyperlink)e.widget).setUnderlined(true);
			}
			public void linkExited(HyperlinkEvent e) {
				((Hyperlink)e.widget).setUnderlined(false);
			}
			class Provider implements DataLinkListPanel.ListProvider {
				public String[] getTitles() { return new String[] { Local.People.toString(), Local.Role.toString() }; }
				public int getNbRows() { return data().getValue2().size(); }
				public Object[] getRow(int index) {
					Pair<List<String>,List<DataLink>> p = data().getValue2().get(index);
					return new Object[] { p.getValue2(), p.getValue1() };
				}
			}
		});
		panel.setFooter(link);
	}
}
