package net.lecousin.dataorganizer.audio.detect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.audio.Local;
import net.lecousin.dataorganizer.audio.detect.AlbumDetector.Album;
import net.lecousin.dataorganizer.audio.detect.AlbumHelper.Track;
import net.lecousin.dataorganizer.audio.detect.AlbumHelper.TrackComparator;
import net.lecousin.dataorganizer.audio.internal.EclipsePlugin;
import net.lecousin.framework.collections.SortedList;
import net.lecousin.framework.collections.SortedListTree;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.ui.eclipse.EclipseImages;
import net.lecousin.framework.ui.eclipse.EclipseWorkbenchUtil;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.ValidationControl;
import net.lecousin.framework.ui.eclipse.control.buttonbar.OkCancelButtonsPanel;
import net.lecousin.framework.ui.eclipse.control.list.LCContentProvider;
import net.lecousin.framework.ui.eclipse.control.list.LCTableWithControls;
import net.lecousin.framework.ui.eclipse.control.list.LCTable.ColumnProvider;
import net.lecousin.framework.ui.eclipse.control.list.LCTable.ColumnProviderText;
import net.lecousin.framework.ui.eclipse.control.list.LCTable.TableConfig;
import net.lecousin.framework.ui.eclipse.control.text.lcml.LCMLText;
import net.lecousin.framework.ui.eclipse.dialog.ErrorDlg;
import net.lecousin.framework.ui.eclipse.dialog.FlatDialog;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.FileStoreEditorInput;

public class CreateAlbumsDialog extends FlatDialog {

	public CreateAlbumsDialog(Shell shell, String message, List<Album> albums, Album noName, List<Track> noInfo, IFileStore dir) {
		super(shell, Local.Create_Music_Album.toString(), false, false);
		this.message = message;
		this.albums = albums;
		this.noName = noName;
		this.noInfo = noInfo;
		this.dir = dir;
	}
	
	private String message;
	private List<Album> albums;
	private Album noName;
	private List<Track> noInfo;
	private IFileStore dir;
	
	private LCTableWithControls<Track> notIncludedTable;
	private CTabFolder albumsFolder;
	private ValidationControl validation;
	private OkCancelButtonsPanel closeButton;
	private boolean ok = false;
	
	@Override
	protected void createContent(Composite container) {
		Composite header = UIUtil.newGridComposite(container, 0, 0, 2);
		UIUtil.gridDataHorizFill(header);
		UIUtil.newImage(header, EclipseImages.getImage(EclipsePlugin.ID, "images/audio_128.gif"));
		LCMLText text = new LCMLText(header, false, false);
		text.setText(message);
		text.getControl().setLayoutData(UIUtil.gridDataHoriz(1, true));
		text.addLinkListener("dir", new Runnable() {
			public void run() {
				try {
					EclipseWorkbenchUtil.getPage().openEditor(
							new FileStoreEditorInput(dir), 
							IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID, 
							true);
				} catch (PartInitException e) {
					ErrorDlg.exception("Error", "Unable to open directory", EclipsePlugin.ID, e);
				}
			}
		});
		
		// Tracks not included
		List<Track> tracks = new LinkedList<Track>();
		if (noName != null) {
			tracks.addAll(noName.noNumber);
			for (SortedList<Track> list : noName.sorted)
				for (Track t : list)
					tracks.add(t);
		}
		if (noInfo != null)
			tracks.addAll(noInfo);
		notIncludedTable = new LCTableWithControls<Track>(container, Local.Tracks_not_included_into_an_album.toString(), new TrackProvider(tracks, true), false, false, false, true);
		UIUtil.gridDataHorizFill(notIncludedTable).heightHint = 130;
		notIncludedTable.setButtonAddToolTip(Local.Add_selected_tracks_to_the_album_list_below.toString());
		notIncludedTable.addRequested().addFireListener(new Runnable() {
			public void run() {
				List<Track> sel = notIncludedTable.getSelection();
				if (sel == null || sel.isEmpty()) return;
				CTabItem item = albumsFolder.getSelection();
				if (item == null) return;
				AlbumControl a = (AlbumControl)item.getControl();
				a.addTracks(sel);
				notIncludedTable.removeSelected();
				validate();
			}
		});
		
		// Albums
		header = UIUtil.newGridComposite(container, 0, 0, 2);
		UIUtil.gridDataHorizFill(header);
		UIUtil.newLabel(header, Local.Albums_to_create.toString());
		UIUtil.newImageButton(header, SharedImages.getImage(SharedImages.icons.x16.basic.ADD), new Listener<Object>() {
			public void fire(Object event) {
				newAlbum();
			}
		}, null).setToolTipText(Local.Create_new_album.toString());
		albumsFolder = new CTabFolder(container, SWT.FLAT | SWT.MULTI | SWT.TOP | SWT.BORDER);
		albumsFolder.setBackground(container.getBackground());
		UIUtil.gridDataHorizFill(albumsFolder).heightHint = 350;
		for (Album album : albums)
			createAlbum(album);
		
		UIUtil.newSeparator(container, true, true);
		validation = new ValidationControl(container);
		UIUtil.gridDataHorizFill(validation);
		closeButton = new OkCancelButtonsPanel(container, true) {
			@Override
			protected boolean handleOk() {
				if (validation.isVisible()) return false;
				synchronizeAlbumsFromDialog();
				ok = true;
				return true;
			}
			@Override
			protected boolean handleCancel() {
				ok = false;
				return true;
			}
		};
		closeButton.centerAndFillInGrid();

		if (albums.isEmpty())
			newAlbum();
		else
			validate();
		albumsFolder.setSelection(0);
	}
	
	@SuppressWarnings("unchecked")
	private static ColumnProvider<Track>[] columns = new ColumnProvider[] {
		new ColumnProviderText<Track>() {
			public String getTitle() { return Local.Name.toString(); }
			public int getAlignment() { return SWT.LEFT; }
			public int getDefaultWidth() { return 200; }
			public String getText(Track element) { return element.file.getName(); }
			public Font getFont(Track element) { return null; }
			public Image getImage(Track element) { return null; }
			public int compare(Track element1, String text1, Track element2, String text2) { return 0; }
		},
		new ColumnProviderText<Track>() {
			public String getTitle() { return Local.Track.toString(); }
			public int getAlignment() { return SWT.CENTER; }
			public int getDefaultWidth() { return 50; }
			public String getText(Track element) { return element.trackNumber > 0 ? Integer.toString(element.trackNumber) : ""; }
			public Font getFont(Track element) { return null; }
			public Image getImage(Track element) { return null; }
			public int compare(Track element1, String text1, Track element2, String text2) { return 0; }
		},
		new ColumnProviderText<Track>() {
			public String getTitle() { return Local.Album.toString(); }
			public int getAlignment() { return SWT.LEFT; }
			public int getDefaultWidth() { return 120; }
			public String getText(Track element) { return element.audio.getInfo() != null && element.audio.getInfo().getAlbum() != null ? element.audio.getInfo().getAlbum() : ""; }
			public Font getFont(Track element) { return null; }
			public Image getImage(Track element) { return null; }
			public int compare(Track element1, String text1, Track element2, String text2) { return 0; }
		},
		new ColumnProviderText<Track>() {
			public String getTitle() { return Local.Artist.toString(); }
			public int getAlignment() { return SWT.LEFT; }
			public int getDefaultWidth() { return 120; }
			public String getText(Track element) { return element.audio.getInfo() != null && element.audio.getInfo().getArtist() != null ? element.audio.getInfo().getArtist() : ""; }
			public Font getFont(Track element) { return null; }
			public Image getImage(Track element) { return null; }
			public int compare(Track element1, String text1, Track element2, String text2) { return 0; }
		},
		new ColumnProviderText<Track>() {
			public String getTitle() { return Local.Year.toString(); }
			public int getAlignment() { return SWT.RIGHT; }
			public int getDefaultWidth() { return 50; }
			public String getText(Track element) { int y = -1; if (element.audio.getInfo() != null) y = element.audio.getInfo().getYear(); return y > 0 ? Integer.toString(y) : ""; }
			public Font getFont(Track element) { return null; }
			public Image getImage(Track element) { return null; }
			public int compare(Track element1, String text1, Track element2, String text2) { return 0; }
		},
	};
	private static TableConfig configSortable;
	static {
		configSortable = new TableConfig();
		configSortable.fixedRowHeight = 18;
		configSortable.multiSelection = true;
		configSortable.sortable = true;
	}
	private static TableConfig configNotSortable;
	static {
		configNotSortable = new TableConfig();
		configNotSortable.fixedRowHeight = 18;
		configNotSortable.multiSelection = true;
		configNotSortable.sortable = false;
	}
	private class TrackProvider implements LCTableWithControls.Provider<Track> {
		private TrackProvider(List<Track> tracks, boolean sortable) {
			contentProvider = new LCContentProvider.StaticList<Track>(tracks);
			config = sortable ? configSortable : configNotSortable;
		}
		private LCContentProvider<Track> contentProvider;
		private TableConfig config;
		public LCContentProvider<Track> getContentProvider() { return contentProvider; }
		public ColumnProvider<Track>[] getColumns() { return columns; }
		public TableConfig getConfig() { return config; }
		public void createElementDetailsControl(Composite parent, Track element) {  }
	}
	
	private class AlbumControl extends ScrolledComposite {
		public AlbumControl(Album album, CTabItem parentItem) {
			super(albumsFolder, SWT.V_SCROLL);
			this.parent = parentItem;
			Composite panel = UIUtil.newGridComposite(this, 0, 0, 1, 0, 1);
			setContent(panel);
			setExpandHorizontal(true);
			
			validation = new ValidationControl(panel);
			UIUtil.gridDataHorizFill(validation);
			
			Composite header = UIUtil.newGridComposite(panel, 0, 0, 2);
			UIUtil.gridDataHorizFill(header);
			UIUtil.newLabel(header, Local.Album_name.toString());
			textAlbumName = UIUtil.newText(header, album.name, new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					parent.setText(((Text)e.widget).getText());
					validate();
				}
			});
			textAlbumName.setLayoutData(UIUtil.gridDataHoriz(1, true));
			
			// not numbered
			tableNotNumbered = new LCTableWithControls<Track>(panel, Local.Not_numbered_tracks+":", new TrackProvider(album.noNumber, true), false, true, true, true);
			UIUtil.gridDataHorizFill(tableNotNumbered).heightHint = 125;
			tableNotNumbered.setButtonRemoveToolTip(Local.Remove_selected_tracks_from_the_album.toString());
			tableNotNumbered.setButtonAddToolTip(Local.Add_selected_tracks_to_the_album_list_below.toString());
			tableNotNumbered.removeRequested().addFireListener(new Runnable() {
				public void run() {
					List<Track> tracks = tableNotNumbered.removeSelected();
					if (tracks == null || tracks.isEmpty()) return;
					notIncludedTable.add(tracks);
					validate();
				}
			});
			tableNotNumbered.addRequested().addFireListener(new Runnable() {
				public void run() {
					List<Track> sel = tableNotNumbered.getSelection();
					if (sel == null || sel.isEmpty()) return;
					addTracks(sel);
					tableNotNumbered.removeSelected();
					validate();
				}
			});

			// different lists
			UIUtil.newLabel(panel, Local.Lists_of_numbered_tracks.toString());
			folder = new CTabFolder(panel, SWT.TOP | SWT.MULTI | SWT.BORDER);
			UIUtil.gridDataHorizFill(folder).heightHint = 250;
			int index = 1;
			if (album.sorted.isEmpty()) {
				album.sorted.add(new SortedListTree<Track>(new TrackComparator()));
			}
			for (SortedList<Track> list : album.sorted) {
				CTabItem item = new CTabItem(folder, SWT.CLOSE);
				item.setText(Local.List+" "+(index++));
				ArrayList<Track> tracks = new ArrayList<Track>(list.size());
				for (Track t : list) tracks.add(t);
				LCTableWithControls<Track> table = new LCTableWithControls<Track>(folder, Local.Tracks+":", new TrackProvider(tracks, false), false, true, true, false);
				item.setControl(table);
				item.setShowClose(album.sorted.size() > 1);
				item.addDisposeListener(new DisposeListener() {
					@SuppressWarnings("unchecked")
					public void widgetDisposed(DisposeEvent e) {
						if (notIncludedTable.isDisposed()) return;
						CTabItem item = (CTabItem)e.widget;
						LCTableWithControls<Track> table = (LCTableWithControls<Track>)item.getControl();
						List<Track> tracks = table.getElements();
						notIncludedTable.add(tracks);
						if (folder.getItemCount() == 1)
							folder.getItem(0).setShowClose(false);
						validate();
					}
				});
				table.setButtonRemoveToolTip(Local.Remove_selected_tracks_from_the_album.toString());
				table.removeRequested().addListener(new Listener<LCTableWithControls<Track>>() {
					public void fire(LCTableWithControls<Track> table) {
						List<Track> tracks = table.removeSelected();
						if (tracks == null || tracks.isEmpty()) return;
						notIncludedTable.add(tracks);
						validate();
					}
				});
			}
			folder.setSelection(0);
			
			panel.setSize(panel.computeSize(SWT.DEFAULT, SWT.DEFAULT, true));
		}
		private CTabItem parent;
		private Text textAlbumName;
		private ValidationControl validation;
		private CTabFolder folder;
		private LCTableWithControls<Track> tableNotNumbered;
		@Override
		protected void checkSubclass() {
		}
		
		@SuppressWarnings("unchecked")
		void addTracks(List<Track> tracks) {
			CTabItem item = folder.getSelection();
			LCTableWithControls<Track> table = (LCTableWithControls<Track>)item.getControl();
			table.add(tracks);
		}
	}
	
	public boolean open() {
		super.openProgressive(null, OrientationY.BOTTOM, true);
		return ok;
	}

	private void newAlbum() {
		Album album = new Album();
		albums.add(album);
		createAlbum(album);
		validate();
	}
	private void createAlbum(Album album) {
		CTabItem item = new CTabItem(albumsFolder, SWT.CLOSE);
		if (album.name == null) album.name = "";
		item.setText(album.name);
		item.setControl(new AlbumControl(album, item));
		item.addDisposeListener(new DisposeListener() {
			@SuppressWarnings("unchecked")
			public void widgetDisposed(DisposeEvent e) {
				if (notIncludedTable.isDisposed()) return;
				CTabItem item = (CTabItem)e.widget;
				AlbumControl a = (AlbumControl)item.getControl();
				notIncludedTable.add(a.tableNotNumbered.getElements());
				for (CTabItem i : a.folder.getItems()) {
					LCTableWithControls<Track> table = (LCTableWithControls<Track>)i.getControl();
					notIncludedTable.add(table.getElements());
				}
				if (albumsFolder.getItemCount() == 1)
					albumsFolder.getItem(0).setShowClose(false);
				validate();
			}
		});
		boolean closable = albumsFolder.getItemCount() > 1;
		for (CTabItem i : albumsFolder.getItems())
			i.setShowClose(closable);
	}
	
	@SuppressWarnings("unchecked")
	private void validate() {
		int nbInvalid = 0;
		for (CTabItem item : albumsFolder.getItems()) {
			AlbumControl a = (AlbumControl)item.getControl();
			if (a.textAlbumName.getText().length() == 0) {
				a.validation.updateValidation(Local.The_name_cannot_be_empty.toString());
				item.setImage(SharedImages.getImage(SharedImages.icons.x16.basic.ERROR));
				nbInvalid++;
				continue;
			}
			if (!a.tableNotNumbered.getElements().isEmpty()) {
				a.validation.updateValidation(Local.There_are_remaining_tracks_in_the_not_numbered_list.toString());
				item.setImage(SharedImages.getImage(SharedImages.icons.x16.basic.ERROR));
				nbInvalid++;
				continue;
			}
			if (a.folder.getItemCount() > 1) {
				a.validation.updateValidation(Local.You_must_merge_or_remove_lists.toString());
				item.setImage(SharedImages.getImage(SharedImages.icons.x16.basic.ERROR));
				nbInvalid++;
				continue;
			}
			LCTableWithControls<Track> table = (LCTableWithControls<Track>)a.folder.getItem(0).getControl();
			if (table.getElements().isEmpty()) {
				a.validation.updateValidation(Local.The_album_cannot_be_empty.toString());
				item.setImage(SharedImages.getImage(SharedImages.icons.x16.basic.ERROR));
				nbInvalid++;
				continue;
			}
			a.validation.updateValidation(null);
			item.setImage(SharedImages.getImage(SharedImages.icons.x16.basic.VALIDATE));
		}
		if (nbInvalid > 0)
			updateValidation(Local.process(Local.There_are_XX_albums_containing_errors, nbInvalid));
		else
			updateValidation(null);
	}
	private void updateValidation(String message) {
		validation.updateValidation(message);
		closeButton.enableOk(message == null);
	}
	
	@SuppressWarnings("unchecked")
	private void synchronizeAlbumsFromDialog() {
		List<Track> notIncluded = notIncludedTable.getElements();
		if (noName != null) {
			for (Iterator<Track> it = noName.noNumber.iterator(); it.hasNext(); ) {
				Track t = it.next();
				if (notIncluded.contains(t))
					notIncluded.remove(t);
				else
					it.remove();
			}
			for (Iterator<SortedListTree<Track>> itList = noName.sorted.iterator(); itList.hasNext(); ) {
				SortedListTree<Track> list = itList.next();
				for (Iterator<Track> itTrack = list.iterator(); itTrack.hasNext(); ) {
					Track t = itTrack.next();
					if (notIncluded.contains(t))
						notIncluded.remove(t);
					else
						itTrack.remove();
				}
				if (list.isEmpty())
					itList.remove();
			}
		}
		if (!notIncluded.isEmpty()) {
			noInfo.addAll(notIncluded);
		}
		
		int i;
		for (i = 0; i < albumsFolder.getItemCount(); ++i) {
			CTabItem item = albumsFolder.getItem(i);
			Album album;
			if (i >= albums.size()) {
				album = new Album();
				albums.add(album);
			} else
				album = albums.get(i);
			AlbumControl c = (AlbumControl)item.getControl();
			album.name = c.textAlbumName.getText();
			album.nameIsFromUser = true;
			album.noNumber.clear();
			album.sorted.clear();
			SortedListTree<Track> list = new SortedListTree<Track>(new TrackComparator());
			List<Track> tracks = ((LCTableWithControls<Track>)c.folder.getItem(0).getControl()).getElements();
			for (int ti = 0; ti < tracks.size(); ++ti) {
				Track t = tracks.get(ti);
				t.trackNumber = ti+1;
				list.add(t);
			}
			album.sorted.add(list);
		}
		while (albums.size() > i)
			albums.remove(i);
	}
}
