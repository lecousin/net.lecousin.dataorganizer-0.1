package net.lecousin.dataorganizer.audio.ui;

import net.lecousin.dataorganizer.audio.AudioDataType;
import net.lecousin.dataorganizer.audio.AudioInfo;
import net.lecousin.dataorganizer.audio.Local;
import net.lecousin.dataorganizer.audio.AudioInfo.Track;
import net.lecousin.dataorganizer.core.database.source.DataSource;
import net.lecousin.framework.Pair;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.time.DateTimeUtil;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.list.LCContentProvider;
import net.lecousin.framework.ui.eclipse.control.list.LCTableWithControls;
import net.lecousin.framework.ui.eclipse.control.list.LCTable.ColumnProvider;
import net.lecousin.framework.ui.eclipse.control.list.LCTable.ColumnProviderText;
import net.lecousin.framework.ui.eclipse.control.list.LCTable.TableConfig;
import net.lecousin.framework.ui.eclipse.event.ModifyListenerWithData;
import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class OverviewPanel {

	public OverviewPanel(Composite panel, AudioDataType data) {
		GridLayout layout = UIUtil.gridLayout(panel, 1);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		
		this.info = (AudioInfo)data.getInfo();
		
		Composite line = UIUtil.newGridComposite(panel, 0, 0, 6);
		UIUtil.gridDataHorizFill(line);
		UIUtil.newLabel(line, Local.Artist+":", true, false);
		UIUtil.newText(line, info.getArtist(), new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				info.setArtist(((Text)e.widget).getText());
			}
		}).setLayoutData(UIUtil.gridDataHoriz(1, true));
		UIUtil.newLabel(line, Local.Album+":", true, false);
		UIUtil.newText(line, info.getAlbum(), new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				info.setAlbum(((Text)e.widget).getText());
			}
		}).setLayoutData(UIUtil.gridDataHoriz(1, true));
		UIUtil.newLabel(line, Local.Year+":", true, false);
		UIUtil.newText(line, info.getYear() > 0 ? Integer.toString(info.getYear()) : "", new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String s = ((Text)e.widget).getText();
				if (s.length() == 0) {
					info.setYear(-1);
					((Text)e.widget).setBackground(ColorUtil.getWhite());
				} else
					try {
						int year = Integer.parseInt(s);
						info.setYear(year);
						((Text)e.widget).setBackground(ColorUtil.getWhite());
					} catch (NumberFormatException ex) {
						((Text)e.widget).setBackground(ColorUtil.getOrange());
					}
			}
		}).setLayoutData(UIUtil.gridDataHoriz(1, true));

		// TODO genres (mais avec add, remove...)
//		line = UIUtil.newGridComposite(panel, 0, 0, 2);
//		UIUtil.newLabel(line, Local.Genres+":", true, false);
//		UIUtil.gridDataHorizFill(line);
//		line = UIUtil.newRowComposite(line, SWT.HORIZONTAL, 0, 0, 5, true);
//		for (String s : info.getGenres())
//			UIUtil.newLabel(line, s);
		
		table = new LCTableWithControls<Track>(panel, Local.Tracks.toString(), new TrackProvider(), false, true, false, false);
		UIUtil.gridDataHorizFill(table);
		table.moved().addListener(new Listener<Pair<Track,Integer>>() {
			public void fire(Pair<Track, Integer> event) {
				int srcIndex = info.indexOf(event.getValue1());
				event.getValue1().move(event.getValue2());
				info.getData().moveDataSource(srcIndex, event.getValue2());
				table.refresh(false);
			}
		});
	}
	
	private AudioInfo info;
	private LCTableWithControls<Track> table;
	
	private class TrackProvider implements LCTableWithControls.Provider<Track> {
		@SuppressWarnings("unchecked")
		public TrackProvider() {
			config = new TableConfig();
			config.fixedRowHeight = 18;
			config.multiSelection = true;
			config.sortable = false;
			columns = new ColumnProvider[] { new ColumnIndex(), new ColumnTitle(), new ColumnLength() };
			contentProvider = new LCContentProvider<Track>() {
				public Iterable<Track> getElements() { return info.getTracks(); }
			};
		}
		TableConfig config;
		ColumnProvider<Track>[] columns;
		LCContentProvider<Track> contentProvider;
		private class ColumnIndex implements ColumnProviderText<Track> {
			public String getTitle() { return "N°"; }
			public int getAlignment() { return SWT.RIGHT; }
			public int getDefaultWidth() { return 30; }
			public String getText(Track element) { return Integer.toString(info.indexOf(element)+1)+'.'; }
			public Image getImage(Track element) { return null; }
			public Font getFont(Track element) { return null; }
			public int compare(Track element1, String text1, Track element2, String text2) { return 0; }
		}
		private class ColumnTitle implements ColumnProviderText<Track> {
			public String getTitle() { return Local.Title.toString(); }
			public int getAlignment() { return SWT.LEFT; }
			public int getDefaultWidth() { return 200; }
			public String getText(Track element) { return element.getTitle() != null ? element.getTitle() : ""; }
			public Image getImage(Track element) { return null; }
			public Font getFont(Track element) { return null; }
			public int compare(Track element1, String text1, Track element2, String text2) { return 0; }
		}
		private class ColumnLength implements ColumnProviderText<Track> {
			public String getTitle() { return Local.Duration.toString(); }
			public int getAlignment() { return SWT.RIGHT; }
			public int getDefaultWidth() { return 80; }
			public String getText(Track element) { return element.getLength() >= 0 ? DateTimeUtil.getTimeMinimalString(element.getLength()) : "?"; }
			public Image getImage(Track element) { return null; }
			public Font getFont(Track element) { return null; }
			public int compare(Track element1, String text1, Track element2, String text2) { return 0; }
		}
		public TableConfig getConfig() { return config; }
		public ColumnProvider<Track>[] getColumns() { return columns; }
		public LCContentProvider<Track> getContentProvider() { return contentProvider; }
		public void createElementDetailsControl(Composite parent, Track element) {
			UIUtil.gridLayout(parent, 2);
			UIUtil.newLabel(parent, Local.Title+":", true, false);
			Text text = UIUtil.newText(parent, element.getTitle(), new ModifyListenerWithData<Track>(element) {
				public void modifyText(ModifyEvent e) {
					data().setTitle(((Text)e.widget).getText());
					table.refresh(data());
				}
			});
			text.setLayoutData(UIUtil.gridDataHoriz(1, true));
			UIUtil.newLabel(parent, Local.File+":", true, false);
			DataSource source = info.getData().getSources().get(info.indexOf(element));
			UIUtil.newLabel(parent, source == null ? Local.No_source.toString() : source.toString());
		}
	}
}
