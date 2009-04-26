package net.lecousin.dataorganizer.audio.ui;

import java.util.List;

import net.lecousin.dataorganizer.audio.AudioContentType;
import net.lecousin.dataorganizer.audio.AudioDataType;
import net.lecousin.dataorganizer.audio.AudioInfo;
import net.lecousin.dataorganizer.audio.AudioSourceInfo;
import net.lecousin.dataorganizer.audio.Local;
import net.lecousin.dataorganizer.audio.AudioSourceInfo.Track;
import net.lecousin.dataorganizer.core.database.info.InfoRetrieverPluginRegistry;
import net.lecousin.dataorganizer.core.database.info.SourceInfo;
import net.lecousin.dataorganizer.core.database.source.DataSource;
import net.lecousin.framework.Pair;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.event.Event.ListenerData;
import net.lecousin.framework.time.DateTimeUtil;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.LCCombo;
import net.lecousin.framework.ui.eclipse.control.list.LCContentProvider;
import net.lecousin.framework.ui.eclipse.control.list.LCTableWithControls;
import net.lecousin.framework.ui.eclipse.control.list.LCTable.ColumnProvider;
import net.lecousin.framework.ui.eclipse.control.list.LCTable.ColumnProviderText;
import net.lecousin.framework.ui.eclipse.control.list.LCTable.TableConfig;
import net.lecousin.framework.ui.eclipse.event.ModifyListenerWithData;
import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class OverviewPanel {

	public OverviewPanel(Composite panel, AudioDataType data, List<SourceInfo> sourcesNULL) {
		GridLayout layout = UIUtil.gridLayout(panel, 1);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		
		this.info = (AudioInfo)data.getInfo();

		LCCombo combo;
		
		Composite line = UIUtil.newGridComposite(panel, 0, 0, 6);
		UIUtil.gridDataHorizFill(line);
		UIUtil.newLabel(line, Local.Artist+":", true, false);
		combo = new LCCombo(line, null);
		for (String s : info.getSources()) {
			AudioSourceInfo i = (AudioSourceInfo)info.getSourceInfo(s);
			if (i == null || i.getArtist() == null || i.getArtist().length() == 0) continue;
			combo.addItem(InfoRetrieverPluginRegistry.getIconForSource(s, AudioContentType.AUDIO_TYPE), i.getArtist(), null);
		}
		combo.setSelection(info.getArtist() != null ? info.getArtist() : "");
		combo.selectionEvent().addListener(new Listener<Pair<String,Object>>() {
			public void fire(Pair<String, Object> event) {
				info.setArtist(event.getValue1());
			}
		});
		combo.setLayoutData(UIUtil.gridDataHoriz(1, true));

		UIUtil.newLabel(line, Local.Album+":", true, false);
		combo = new LCCombo(line, null);
		for (String s : info.getSources()) {
			AudioSourceInfo i = (AudioSourceInfo)info.getSourceInfo(s);
			if (i == null || i.getAlbum() == null || i.getAlbum().length() == 0) continue;
			combo.addItem(InfoRetrieverPluginRegistry.getIconForSource(s, AudioContentType.AUDIO_TYPE), i.getAlbum(), null);
		}
		combo.setSelection(info.getAlbum() != null ? info.getAlbum() : "");
		combo.selectionEvent().addListener(new Listener<Pair<String,Object>>() {
			public void fire(Pair<String, Object> event) {
				info.setAlbum(event.getValue1());
			}
		});
		combo.setLayoutData(UIUtil.gridDataHoriz(1, true));

		UIUtil.newLabel(line, Local.Year+":", true, false);
		combo = new LCCombo(line, null);
		for (String s : info.getSources()) {
			AudioSourceInfo i = (AudioSourceInfo)info.getSourceInfo(s);
			if (i == null || i.getYear() <= 0) continue;
			combo.addItem(InfoRetrieverPluginRegistry.getIconForSource(s, AudioContentType.AUDIO_TYPE), Integer.toString(i.getYear()), null);
		}
		combo.setSelection(info.getYear() > 0 ? Integer.toString(info.getYear()) : "");
		combo.selectionEvent().addListener(new ListenerData<Pair<String,Object>, LCCombo>(combo) {
			public void fire(Pair<String, Object> event) {
				int year;
				try { 
					year = Integer.parseInt(event.getValue1()); 
					info.setYear(year);
					data().setBackground(ColorUtil.getWhite());
				} catch (NumberFormatException e) {
					data().setBackground(ColorUtil.getOrange());
				}
			}
		});
		combo.setLayoutData(UIUtil.gridDataHoriz(1, true));

		// TODO genres (mais avec add, remove...)
//		line = UIUtil.newGridComposite(panel, 0, 0, 2);
//		UIUtil.newLabel(line, Local.Genres+":", true, false);
//		UIUtil.gridDataHorizFill(line);
//		line = UIUtil.newRowComposite(line, SWT.HORIZONTAL, 0, 0, 5, true);
//		for (String s : info.getGenres())
//			UIUtil.newLabel(line, s);
		
		//TODO si fileInfo ou pas, si info d'Internet ou pas... a propos des tracks: merger...
		fileInfo = info.getSourceInfo(AudioInfo.FILE_SOURCE);
		if (fileInfo != null) {
			table = new LCTableWithControls<Track>(panel, Local.Tracks.toString(), new TrackProvider(), false, true, false, false);
			UIUtil.gridDataHorizFill(table);
			table.moved().addListener(new Listener<Pair<Track,Integer>>() {
				public void fire(Pair<Track, Integer> event) {
					int srcIndex = fileInfo.indexOf(event.getValue1());
					event.getValue1().move(event.getValue2());
					info.getData().moveDataSource(srcIndex, event.getValue2());
					table.refresh(false);
				}
			});
		}
	}
	
	private AudioInfo info;
	private AudioSourceInfo fileInfo;
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
				public Iterable<Track> getElements() { return fileInfo.getTracks(); }
			};
		}
		TableConfig config;
		ColumnProvider<Track>[] columns;
		LCContentProvider<Track> contentProvider;
		private class ColumnIndex implements ColumnProviderText<Track> {
			public String getTitle() { return "N°"; }
			public int getAlignment() { return SWT.RIGHT; }
			public int getDefaultWidth() { return 30; }
			public String getText(Track element) { return Integer.toString(fileInfo.indexOf(element)+1)+'.'; }
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
			DataSource source = info.getData().getSources().get(fileInfo.indexOf(element));
			UIUtil.newLabel(parent, source == null ? Local.No_source.toString() : source.toString());
		}
	}
}
