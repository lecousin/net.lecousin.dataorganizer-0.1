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
import net.lecousin.dataorganizer.ui.views.dataoverview.OverviewPanel;
import net.lecousin.dataorganizer.video.Local;
import net.lecousin.dataorganizer.video.VideoDataType;
import net.lecousin.dataorganizer.video.VideoSourceInfo;
import net.lecousin.dataorganizer.video.VideoSourceInfo.Genre;
import net.lecousin.framework.Pair;
import net.lecousin.framework.collections.CollectionUtil;
import net.lecousin.framework.event.Event.ListenerData;
import net.lecousin.framework.time.DateTimeUtil;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.LCGrid;
import net.lecousin.framework.ui.eclipse.control.Row_ExpandableList;
import net.lecousin.framework.ui.eclipse.control.UIControlUtil;
import net.lecousin.framework.ui.eclipse.dialog.FlatPopupMenu;
import net.lecousin.framework.ui.eclipse.dialog.MyDialog;
import net.lecousin.framework.ui.eclipse.event.ControlListenerWithData;
import net.lecousin.framework.ui.eclipse.event.HyperlinkListenerWithData;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;

public class VideoOverviewPanel {

	public VideoOverviewPanel(Composite panel, VideoDataType data, List<SourceInfo> sources, boolean big) {
		this.panel = panel;
		this.big = big;
		this.data = data;
		create(sources);
	}
	private VideoDataType data;
	private Composite panel;
	private boolean big;
	
	private void create(List<SourceInfo> sources) {
		GridLayout layout = UIUtil.gridLayout(panel, 1);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		if (big)
			createBig(panel, data, sources);
		else
			createSmall(panel, data, sources);
	}
	
	private String getDuration(VideoDataType data) {
		return (data.getDuration() < 0 ? "-" : DateTimeUtil.getTimeMinimalString(data.getDuration()));	
	}
	private String getDimension(VideoDataType data) {
		return (data.getDimension() == null ? "-" : (Integer.toString(data.getDimension().x)+'x'+data.getDimension().y));
	}
	private String getReleaseDate(List<SourceInfo> sources) {
		long releaseDate = -1;
		for (SourceInfo i : sources)
			releaseDate = SourceInfoMergeUtil.mergeDate(releaseDate, ((VideoSourceInfo)i).getReleaseDate());
		if (releaseDate <= 0) return null;
		return new SimpleDateFormat("dd MMMM yyyy").format(new Date(releaseDate));
	}
	private List<Genre> getGenres(List<SourceInfo> sources) {
		List<Genre> genres = new LinkedList<Genre>();
		for (SourceInfo i : sources)
			for (Genre g : ((VideoSourceInfo)i).getGenres())
				if (!genres.contains(g)) genres.add(g);
		return genres;
	}
	private List<Pair<List<String>,List<DataLink>>> getDirectors(List<SourceInfo> sources) {
		List<Pair<List<String>,List<DataLink>>> list = new LinkedList<Pair<List<String>,List<DataLink>>>();
		for (SourceInfo i : sources)
			SourceInfoMergeUtil.mergePeopleLists(list, ((VideoSourceInfo)i).getDirectors());
		return list;
	}
	private List<Pair<List<String>,List<DataLink>>> getActors(List<SourceInfo> sources) {
		List<Pair<List<String>,List<DataLink>>> list = new LinkedList<Pair<List<String>,List<DataLink>>>();
		for (SourceInfo i : sources)
			SourceInfoMergeUtil.mergePeopleLists(list, ((VideoSourceInfo)i).getActors());
		return list;
	}
	private List<Pair<List<String>,List<DataLink>>> getProducers(List<SourceInfo> sources) {
		List<Pair<List<String>,List<DataLink>>> list = new LinkedList<Pair<List<String>,List<DataLink>>>();
		for (SourceInfo i : sources)
			SourceInfoMergeUtil.mergePeopleLists(list, ((VideoSourceInfo)i).getProductors());
		return list;
	}
	private List<Pair<List<String>,List<DataLink>>> getWriters(List<SourceInfo> sources) {
		List<Pair<List<String>,List<DataLink>>> list = new LinkedList<Pair<List<String>,List<DataLink>>>();
		for (SourceInfo i : sources)
			SourceInfoMergeUtil.mergePeopleLists(list, ((VideoSourceInfo)i).getScenaristes());
		return list;
	}
	
	private void createSmall(Composite panel, VideoDataType data, List<SourceInfo> sources) {
		topSmallLine = UIUtil.newComposite(panel);
		UIUtil.gridDataHorizFill(topSmallLine);
		RowLayout l = new RowLayout(SWT.HORIZONTAL);
		l.marginHeight = l.marginWidth = l.marginBottom = l.marginTop = 0;
		l.spacing = 5;
		l.center = true;
		topSmallLine.setLayout(l);
		UIUtil.newLabel(topSmallLine, Local.Duration+":", true, false);
		labelDuration = UIUtil.newLabel(topSmallLine, getDuration(data));
		UIUtil.newLabel(topSmallLine, Local.Dimension+":", true, false);
		labelDimension = UIUtil.newLabel(topSmallLine, getDimension(data));
		refreshReleaseDate(sources);
		refreshGenre(sources);
			
		refreshDirectors(sources);
		refreshActors(sources);
		refreshProducers(sources);
		refreshWriters(sources);
	}
	
	private Row_ExpandableList createPeopleListRow(Composite parent, List<Pair<List<String>,List<DataLink>>> list, String title, String title2) {
		if (list.isEmpty()) return null;
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
				new DataLinkListPanel(dlg.getControl(), new Provider(data().getValue2())).addControlListener(new ControlListenerWithData<MyDialog>(dlg) {
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
		});
		panel.setFooter(link);
		return panel;
	}
	
	private void createBig(Composite panel, VideoDataType data, List<SourceInfo> sources) {
		grid = new LCGrid(panel, 2, 1, 1, OverviewPanel.GRID_BORDER_COLOR);
		
		UIUtil.newLabel(grid.newCell(3, 0, OverviewPanel.GRID_COLOR_1), Local.Duration.toString(), true, false);
		labelDuration = UIUtil.newLabel(grid.newCell(3, 0, OverviewPanel.GRID_COLOR_2), getDuration(data));
		UIUtil.newLabel(grid.newCell(3, 0, OverviewPanel.GRID_COLOR_1), Local.Dimension.toString(), true, false);
		labelDimension = UIUtil.newLabel(cellDimension = grid.newCell(3, 0, OverviewPanel.GRID_COLOR_2), getDimension(data));
		refreshReleaseDate(sources);
		refreshGenre(sources);
		refreshDirectors(sources);
		refreshActors(sources);
		refreshProducers(sources);
		refreshWriters(sources);
	}	

	private Pair<Composite,Composite> createPeopleListBig(LCGrid grid, List<Pair<List<String>,List<DataLink>>> list, String title, String title2) {
		if (list.isEmpty()) return null;
		Composite cell1, cell2;

		UIUtil.newLabel(cell1 = grid.newCell(3, 0, OverviewPanel.GRID_COLOR_1), title, true, false);
		DataLinkListPanel p = new DataLinkListPanel(cell2 = grid.newCell(3, 0, OverviewPanel.GRID_COLOR_2), new Provider(list)); 
		p.addControlListener(new ControlListenerWithData<LCGrid>(grid) {
			public void controlMoved(ControlEvent e) {
			}
			public void controlResized(ControlEvent e) {
				data().layout(true, true);
				UIControlUtil.autoresize(data());
			}
		});
		return new Pair<Composite,Composite>(cell1, cell2);
	}
	private static class Provider implements DataLinkListPanel.ListProvider {
		public Provider(List<Pair<List<String>,List<DataLink>>> list) { this.list = list; }
		private List<Pair<List<String>,List<DataLink>>> list;
		private static String[] titles = new String[] { Local.People.toString(), Local.Role.toString() };
		public String[] getTitles() { return titles; }
		public int getNbRows() { return list.size(); }
		public Object[] getRow(int index) {
			Pair<List<String>,List<DataLink>> p = list.get(index);
			return new Object[] { p.getValue2(), p.getValue1() };
		}
	}
	
	private Composite topSmallLine;
	private LCGrid grid;
	private Label labelDuration, labelDimension;
	private Composite cellDimension;
	
	public void refresh(List<SourceInfo> sources) {
		labelDuration.setText(getDuration(data));
		labelDimension.setText(getDimension(data));
		refreshReleaseDate(sources);
		refreshGenre(sources);
		refreshDirectors(sources);
		refreshActors(sources);
		refreshProducers(sources);
		refreshWriters(sources);
		UIControlUtil.autoresize(labelDuration);
	}
	
	private Label labelRelease1, labelRelease2, labelRelease3;
	private Composite cellRelease1, cellRelease2;
	private void refreshReleaseDate(List<SourceInfo> sources) {
		String releaseDate = sources != null ? getReleaseDate(sources) : null;
		if (releaseDate != null) {
			if (big) {
				if (cellRelease1 == null) {
					UIUtil.newLabel(cellRelease1 = grid.newCell(3, 0, OverviewPanel.GRID_COLOR_1), Local.Release.toString(), true, false);
					labelRelease1 = UIUtil.newLabel(cellRelease2 = grid.newCell(3, 0, OverviewPanel.GRID_COLOR_2), releaseDate);
					cellRelease1.moveBelow(cellDimension);
					cellRelease2.moveBelow(cellRelease1);
				} else {
					labelRelease1.setText(releaseDate);
				}
			} else {
				if (labelRelease1 == null) {
					labelRelease1 = UIUtil.newSeparator(topSmallLine, false, true); labelRelease1.moveBelow(labelDuration);
					labelRelease2 = UIUtil.newLabel(topSmallLine, Local.Release+":", true, false); labelRelease2.moveBelow(labelRelease1);
					labelRelease3 = UIUtil.newLabel(topSmallLine, releaseDate); labelRelease3.moveBelow(labelRelease2);
				} else {
					labelRelease3.setText(releaseDate);
				}
			}
		} else {
			if (big) {
				if (cellRelease1 != null) {
					cellRelease1.dispose(); cellRelease1 = null;
					cellRelease2.dispose(); cellRelease2 = null;
					labelRelease1 = null;
				}
			} else {
				if (labelRelease1 != null) {
					labelRelease1.dispose(); labelRelease1 = null;
					labelRelease2.dispose(); labelRelease2 = null;
					labelRelease2.dispose(); labelRelease3 = null;
				}
			}
		}
	}

	private Composite cellGenre1, cellGenre2;
	private Composite genreLine;
	private Label labelGenre;
	private void refreshGenre(List<SourceInfo> sources) {
		List<Genre> genres = getGenres(sources);
		if (!genres.isEmpty()) {
			if (big) {
				if (cellGenre1 == null) {
					UIUtil.newLabel(cellGenre1 = grid.newCell(3, 0, OverviewPanel.GRID_COLOR_1), Local.Genre.toString(), true, false);
					createGenreLine(cellGenre2 = grid.newCell(3, 0, OverviewPanel.GRID_COLOR_2));
					if (cellRelease2 != null)
						cellGenre1.moveBelow(cellRelease2);
					else
						cellGenre1.moveBelow(cellDimension);
					cellGenre2.moveBelow(cellGenre1);
					refreshGenreLine(genres);
				} else {
					refreshGenreLine(genres);
				}
			} else {
				if (labelGenre == null) {
					createGenreLine(panel);
					labelGenre = UIUtil.newLabel(genreLine, Local.Genre+": ", true, false);
					refreshGenreLine(genres);
				} else {
					refreshGenreLine(genres);
				}
			}
		} else {
			if (big) {
				if (cellGenre1 != null) {
					cellGenre1.dispose(); cellGenre1 = null;
					cellGenre2.dispose(); cellGenre2 = null;
					genreLine = null;
				}
			} else {
				if (labelGenre != null) {
					genreLine.dispose();
					genreLine = null;
					labelGenre = null;
				}
			}
		}
	}		
	private void createGenreLine(Composite parent) {
		genreLine = UIUtil.newComposite(parent);
		RowLayout l = new RowLayout(SWT.HORIZONTAL);
		l.marginHeight = l.marginWidth = l.marginBottom = l.marginTop = 0;
		l.spacing = 0;
		genreLine.setLayout(l);
	}
	private void refreshGenreLine(List<Genre> genres) {
		for (Control c : genreLine.getChildren())
			if (c != labelGenre)
				c.dispose();
		boolean first = true;
		for (Genre genre : genres) {
			if (first) first = false;
			else UIUtil.newLabel(genreLine, ",");
			UIUtil.newLabel(genreLine, " "+genre.getDescription());
		}
	}

	private Row_ExpandableList rowDirectors, rowActors, rowProducers, rowWriters;
	private Pair<Composite,Composite> cellsDirectors, cellsActors, cellsProducers, cellsWriters;
	private void refreshDirectors(List<SourceInfo> sources) {
		if (big)
			cellsDirectors = refreshPeopleListBig(cellsDirectors, getDirectors(sources), Local.Directed_by.toString(), Local.Direction.toString());
		else
			rowDirectors = refreshPeopleListSmall(rowDirectors, getDirectors(sources), Local.Directed_by.toString(), Local.Direction.toString());
	}
	private void refreshActors(List<SourceInfo> sources) {
		if (big)
			cellsActors = refreshPeopleListBig(cellsActors, getActors(sources), Local.Actors.toString(), Local.Casting.toString());
		else
			rowActors = refreshPeopleListSmall(rowActors, getActors(sources), Local.Actors.toString(), Local.Casting.toString());
	}
	private void refreshProducers(List<SourceInfo> sources) {
		if (big)
			cellsProducers = refreshPeopleListBig(cellsProducers, getProducers(sources), Local.Producted_by.toString(), Local.Production.toString());
		else
			rowProducers = refreshPeopleListSmall(rowProducers, getProducers(sources), Local.Producted_by.toString(), Local.Production.toString());
	}
	private void refreshWriters(List<SourceInfo> sources) {
		if (big)
			cellsWriters = refreshPeopleListBig(cellsWriters, getWriters(sources), Local.Writen_by.toString(), Local.Write.toString());
		else
			rowWriters = refreshPeopleListSmall(rowWriters, getWriters(sources), Local.Writen_by.toString(), Local.Write.toString());
	}
	private Pair<Composite,Composite> refreshPeopleListBig(Pair<Composite,Composite> cells, List<Pair<List<String>,List<DataLink>>> list, String title, String title2) {
		if (!list.isEmpty()) {
			if (cells == null)
				return createPeopleListBig(grid, list, title, title2);
			((DataLinkListPanel)cells.getValue2().getChildren()[0]).resetProvider(new Provider(list));
			return cells;
		} else {
			if (cells != null) {
				cells.getValue1().dispose();
				cells.getValue2().dispose();
			}
			return null;
		}
	}
	private Row_ExpandableList refreshPeopleListSmall(Row_ExpandableList row, List<Pair<List<String>,List<DataLink>>> list, String title, String title2) {
		if (row != null)
			row.dispose();
		return createPeopleListRow(panel, list, title, title2);
	}		
}
