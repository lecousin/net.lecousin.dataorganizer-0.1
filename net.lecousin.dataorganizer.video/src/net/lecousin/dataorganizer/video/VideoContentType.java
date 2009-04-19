package net.lecousin.dataorganizer.video;

import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.VirtualData;
import net.lecousin.dataorganizer.core.database.VirtualDataBase;
import net.lecousin.dataorganizer.core.database.content.ContentType;
import net.lecousin.dataorganizer.core.database.content.DataContentType;
import net.lecousin.dataorganizer.core.database.source.DataSource;
import net.lecousin.dataorganizer.core.database.version.ContentTypeLoader;
import net.lecousin.dataorganizer.core.search.DataSearch.Parameter;
import net.lecousin.dataorganizer.ui.wizard.adddata.AddData_Page;
import net.lecousin.dataorganizer.video.internal.EclipsePlugin;
import net.lecousin.dataorganizer.video.search.Param_Casting;
import net.lecousin.dataorganizer.video.search.Param_Duration;
import net.lecousin.framework.Pair;
import net.lecousin.framework.collections.CollectionUtil;
import net.lecousin.framework.files.FileType;
import net.lecousin.framework.files.TypedFile;
import net.lecousin.framework.files.TypedFolder;
import net.lecousin.framework.files.video.VideoFile;
import net.lecousin.framework.files.video.VideoFileInfo;
import net.lecousin.framework.io.FileSystemUtil;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.media.MediaPlayer;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.time.DateTimeUtil;
import net.lecousin.framework.ui.eclipse.EclipseImages;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.list.LCTable.ColumnProvider;
import net.lecousin.framework.ui.eclipse.control.list.LCTable.ColumnProviderText;
import net.lecousin.framework.ui.eclipse.control.text.lcml.LCMLText;
import net.lecousin.framework.ui.eclipse.dialog.MyDialog;
import net.lecousin.framework.version.Version;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Element;

public class VideoContentType extends ContentType {

	public static final String VIDEO_TYPE = "video";
	
	public VideoContentType() {
	}
	
	private static final Version version = new Version(0,1,0);
	@Override
	public Version getCurrentVersion() { return version; }

	@Override
	public String getID() { return VIDEO_TYPE; }
	@Override
	public String getName() { return Local.Video.toString(); }

	@Override
	public DataContentType create(Data data) { return new VideoDataType(data); }

	@Override
	public DataContentType loadContent(Data data, Element elt) {
		return loadContent(data, elt, new Loader_0_1_0());
	}
	@Override
	public DataContentType loadContent(Data data, Element elt, ContentTypeLoader loader) {
		return new VideoDataType(data, elt, (Loader)loader);
	}
	
	@Override
	public Image getIcon() {
		return SharedImages.getImage(SharedImages.icons.x16.filetypes.MOVIE);
	}
	@Override
	public Image getDefaultTypeImage() {
		return EclipseImages.getImage(EclipsePlugin.ID, "images/film_128.gif");
	}
	
	private static class LoadDataContentContext {
		Composite visual;
		MediaPlayer player;
	}
	@Override
	public Object openLoadDataContentContext(Composite panel) {
		LoadDataContentContext ctx = new LoadDataContentContext();
		Composite c = UIUtil.newGridComposite(panel, 0, 0, 2);
		UIUtil.gridDataHorizFill(c);
		LCMLText text = new LCMLText(c, false, false);
		text.setLayoutData(UIUtil.gridDataHoriz(1, true));
		text.setText(Local.MESSAGE_Take_Previews.toString());
		ctx.visual = new Composite(c, SWT.EMBEDDED) {
			@Override
			public Point computeSize(int hint, int hint2, boolean changed) {
				return new Point(128, 128);
			}
		};
		GridLayout layout = UIUtil.gridLayout(ctx.visual, 1);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		ctx.player = MediaPlayer.create("net.lecousin.media.jvlc");
		if (ctx.player == null)
			ctx.player = MediaPlayer.create("net.lecousin.media.jvlc");
		if (ctx.player == null) {
			c.dispose();
			ctx.visual = null;
			return null;
		}
		Control ctrl = ctx.player.createVisual(ctx.visual);
		ctrl.setLayoutData(UIUtil.gridData(1, true, 1, true));
		ctx.visual.setSize(new Point(120, 120));
		ctx.visual.setSize(new Point(128, 128));
		return ctx;
	}
	@Override
	public void closeLoadDataContentContext(Object context) {
		LoadDataContentContext ctx = (LoadDataContentContext)context;
//		ctx.visual.dispose();
		if (ctx.player != null)
			ctx.player.free();
	}
	@Override
	public void loadDataContent(Data data, Object context, WorkProgress progress, int work) {
		LoadDataContentContext ctx = (LoadDataContentContext)context;
		if (ctx == null || ctx.visual == null || ctx.player == null) {
			progress.progress(work);
			return;
		}
		((VideoDataType)data.getContent()).loadContent(ctx.visual, ctx.player, progress, work);
	}

	@Override
	public AddData_Page createAddDataWizardPage() {
		return new AddVideosWizardPage();
	}

	@Override
	public List<Parameter> createSearchParameters() {
		List<Parameter> list = new LinkedList<Parameter>();
		list.add(new Param_Duration());
		list.add(new Param_Casting());
		return list;
	}
	
	@Override
	public List<ColumnProvider<Data>> getColumns() {
		if (dateFont == null)
			dateFont = UIUtil.increaseFontSize(MyDialog.getPlatformShell().getFont(), -2);
		List<ColumnProvider<Data>> list = new LinkedList<ColumnProvider<Data>>();
		list.add(new ColumnDuration());
		list.add(new ColumnReleaseDate());
		return list;
	}
	private static Font dateFont = null;
	private static class ColumnReleaseDate implements ColumnProviderText<Data> {
		public String getTitle() { return Local.Video.toString()+':'+Local.Release.toString(); }
		public int getDefaultWidth() { return 65; }
		public int getAlignment() { return SWT.RIGHT; }
		public Font getFont(Data element) { return dateFont; }
		public String getText(Data element) {
			if (!element.getContentType().getID().equals(VIDEO_TYPE)) return "";
			VideoDataType content = (VideoDataType)element.getContent();
			VideoInfo info = (VideoInfo)content.getInfo();
			if (info == null) return "";
			VideoSourceInfo i = (VideoSourceInfo)info.getMergedInfo(info.getSources());
			long date = i.getReleaseDate();
			if (date <= 0) return "";
			return DateTimeUtil.getDateString(date);
		}
		public Image getImage(Data element) { return null; }
		public int compare(Data element1, String text1, Data element2, String text2) {
			if (text1.length() == 0)
				return text2.length() == 0 ? 0 : 1;
			if (text2.length() == 0) return -1;
			long date1 = DateTimeUtil.getDateFromString(text1);
			long date2 = DateTimeUtil.getDateFromString(text2);
			return date1 < date2 ? -1 : date1 > date2 ? 1 : 0;
		}
	}
	private static class ColumnDuration implements ColumnProviderText<Data> {
		public String getTitle() { return Local.Video.toString()+':'+Local.Duration.toString(); }
		public int getDefaultWidth() { return 65; }
		public int getAlignment() { return SWT.RIGHT; }
		public Font getFont(Data element) { return null; }
		public String getText(Data element) {
			if (!element.getContentType().getID().equals(VIDEO_TYPE)) return "";
			VideoDataType content = (VideoDataType)element.getContent();
			long duration = content.getDuration();
			if (duration <= 0) return "";
			return DateTimeUtil.getTimeMinimalString(duration);
		}
		public Image getImage(Data element) { return null; }
		public int compare(Data element1, String text1, Data element2, String text2) {
			if (text1.length() == 0)
				return text2.length() == 0 ? 0 : 1;
			if (text2.length() == 0) return -1;
			long date1 = DateTimeUtil.getTimeFromMinimalString(text1);
			long date2 = DateTimeUtil.getTimeFromMinimalString(text2);
			return date1 < date2 ? -1 : date1 > date2 ? 1 : 0;
		}
	}
	
	private static final FileType[] filetypes = new FileType[] { VideoFile.FILE_TYPE };
	public FileType[] getEligibleFileTypesForDetection() {
		return filetypes;
	}
	@Override
	public List<Pair<List<IFileStore>,VirtualData>> detectOnFolder(VirtualDataBase db, TypedFolder folder, Shell shell) {
		return null;
	}
	@Override
	public List<Pair<List<IFileStore>,VirtualData>> detectOnFile(VirtualDataBase db, TypedFolder folder, IFileStore file, Shell shell) {
		return null;
	}
	@Override
	public List<Pair<List<IFileStore>,VirtualData>> detectOnFile(VirtualDataBase db, TypedFolder folder, IFileStore file, TypedFile typedFile,	Shell shell) {
		if (!(typedFile instanceof VideoFile)) return null;
		try {
			VirtualData data = (VirtualData)db.addData(FileSystemUtil.removeFileNameExtension(file.getName()), this, CollectionUtil.single_element_list(DataSource.get(file)));
			VideoFileInfo vi = (VideoFileInfo)typedFile.getInfo();
			if (vi != null) {
				VideoDataType d = (VideoDataType)data.getContent();
				if (vi.getDimension() != null)
					d.setDimension(vi.getDimension());
			}
			return CollectionUtil.single_element_list(new Pair<List<IFileStore>,VirtualData>(CollectionUtil.single_element_list(file), data));
		} catch (Exception e) {
			if (Log.warning(this))
				Log.warning(this, "Unable to add virtual data during detection", e);
			return null;
		}
	}
}
