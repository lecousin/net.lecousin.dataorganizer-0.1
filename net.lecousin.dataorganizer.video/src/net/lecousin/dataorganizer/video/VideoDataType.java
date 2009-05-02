package net.lecousin.dataorganizer.video;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.Data.DuplicateAnalysis;
import net.lecousin.dataorganizer.core.database.content.DataContentType;
import net.lecousin.dataorganizer.core.database.info.Info;
import net.lecousin.dataorganizer.core.database.info.SourceInfo;
import net.lecousin.dataorganizer.core.database.source.DataSource;
import net.lecousin.dataorganizer.core.database.version.ContentTypeLoader;
import net.lecousin.dataorganizer.util.DataImageLoader;
import net.lecousin.dataorganizer.util.DataImageLoader.FileProvider_FromDataPath;
import net.lecousin.dataorganizer.util.DataImageLoader.FileProvider_ListStartWith;
import net.lecousin.dataorganizer.video.ui.DescriptionPanel;
import net.lecousin.dataorganizer.video.ui.VideoOverviewPanel;
import net.lecousin.framework.Pair;
import net.lecousin.framework.collections.CollectionUtil;
import net.lecousin.framework.eclipse.resource.ResourceUtil;
import net.lecousin.framework.event.ProcessListener;
import net.lecousin.framework.event.SplitProcessListener;
import net.lecousin.framework.geometry.PointInt;
import net.lecousin.framework.io.MyByteArrayOutputStream;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.media.Media;
import net.lecousin.framework.media.MediaPlayer;
import net.lecousin.framework.media.UnsupportedFormatException;
import net.lecousin.framework.media.util.SnapshotTaker;
import net.lecousin.framework.media.util.SnapshotTaker.LinkedChecker;
import net.lecousin.framework.media.util.SnapshotTaker.SameColorChecker;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.ui.eclipse.control.UIControlUtil;
import net.lecousin.framework.xml.XmlWriter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Element;

public class VideoDataType extends DataContentType {

	public VideoDataType(Data data, Element elt, Loader loader) {
		super(data, elt, loader);
		duration = loader.getDuration(elt);
		loaded = loader.getLoaded(elt);
		dimension = loader.getDimension(elt);
	}
	@Override
	protected Info createInfo(Element elt, ContentTypeLoader loader) { return new VideoInfo(this, elt, (Loader)loader); }
	@Override
	protected void saveContent(XmlWriter xml) {
		xml.addAttribute("duration", Long.toString(duration));
		if (dimension != null) {
			xml.addAttribute("width", Long.toString(dimension.x));
			xml.addAttribute("height", Long.toString(dimension.y));
		}
	}
	public VideoDataType(Data data) {
		super(data);
	}
	@Override
	protected Info createInfo() { return new VideoInfo(this); }
	
	private boolean loaded = false;
	private long duration = -1;
	private PointInt dimension = null;
	
	public void setDimension(PointInt dim) {
		if (dim == null) return;
		if (dimension != null && dimension.equals(dim)) return;
		dimension = dim;
		signalModification();
	}
	public PointInt getDimension() {
		return dimension;
	}
	
	private List<Pair<Image,String>> previewImages = null;
	private List<Pair<Image,String>> posterImages = null;
	
	void signalNewPoster() {
		posterImages = null;
	}
	@Override
	public void signalSourceRemoved(SourceInfo source) {
		if (!((VideoSourceInfo)source).getPostersPaths().isEmpty())
			posterImages = null;
		super.signalSourceRemoved(source);
	}
	@Override
	public void signalSourceUpdated(SourceInfo source) {
		if (!((VideoSourceInfo)source).getPostersPaths().isEmpty())
			posterImages = null;
		super.signalSourceUpdated(source);
	}
	
	private static final DataImageCategory[] categories = new DataImageCategory[] {
		new DataImageCategory("posters", Local.Posters.toString(), 10),
		new DataImageCategory("previews", Local.Previews.toString(), 500),
	};
	@Override
	public List<DataImageCategory> getImagesCategories() { return CollectionUtil.list(categories); }
	@Override
	public void getImages(ProcessListener<DataImageLoaded> listener) {
		SplitProcessListener<DataImageLoaded> split = new SplitProcessListener<DataImageLoaded>(listener);
		ProcessListener<DataImageLoaded> posterListener = split.newListener();
		ProcessListener<DataImageLoaded> previewListener = split.newListener();
		loadPosters(posterListener);
		loadPreviews(previewListener);
	}
	
	private void loadPosters(ProcessListener<DataImageLoaded> listener) {
		VideoInfo info = (VideoInfo)getInfo();
		if (info == null) {
			listener.started();
			listener.done();
			return;
		}
		synchronized (this) {
			if (posterImages != null) {
				listener.started();
				for (int i = 0; i < posterImages.size(); ++i) {
					Pair<Image,String> p = posterImages.get(i);
					listener.fire(new DataImageLoaded("posters", Local.Poster+" " + (i+1), p.getValue1(), p.getValue2()));
				}
				listener.done();
				return;
			}
		}
		Set<String> paths = new HashSet<String>();
		for (String source : info.getSources()) {
			VideoSourceInfo s = (VideoSourceInfo)info.getSourceInfo(source);
			if (s == null) continue;
			paths.addAll(s.getPostersPaths());
		}
		if (paths.isEmpty()) {
			synchronized (this) {
				posterImages = new LinkedList<Pair<Image,String>>();
			}
			listener.started();
			listener.done();
			return;
		}
		class Listener implements ProcessListener<Pair<Image,String>> {
			Listener(ProcessListener<DataImageLoaded> listener) {
				this.listener = listener;
			}
			private ProcessListener<DataImageLoaded> listener;
			private List<Pair<Image,String>> images = new LinkedList<Pair<Image,String>>();
			public void started() {
				listener.started();
			}
			public void fire(Pair<Image,String> image) {
				listener.fire(new DataImageLoaded("posters", Local.Poster+" " + (images.size()+1), image.getValue1(), image.getValue2()));
				images.add(image);
			}
			public void done() {
				listener.done();
				synchronized (VideoDataType.this) {
					posterImages = images;
					DataImageLoader.release(VideoDataType.this, "posters");
				}
			}
		}
		DataImageLoader.load(this, "posters", new FileProvider_FromDataPath(getData(), paths), new Listener(listener));
	}
	private void loadPreviews(ProcessListener<DataImageLoaded> listener) {
		synchronized (this) {
			if (previewImages != null) {
				listener.started();
				for (int i = 0; i < previewImages.size(); ++i) {
					Pair<Image,String> p = previewImages.get(i);
					listener.fire(new DataImageLoaded("previews", Local.Preview+" " + (i+1), p.getValue1(), p.getValue2()));
				}
				listener.done();
				return;
			}
		}
		class Listener implements ProcessListener<Pair<Image,String>> {
			Listener(ProcessListener<DataImageLoaded> listener) {
				this.listener = listener;
			}
			private ProcessListener<DataImageLoaded> listener;
			private List<Pair<Image,String>> images = new LinkedList<Pair<Image,String>>();
			public void started() {
				listener.started();
			}
			public void fire(Pair<Image,String> image) {
				listener.fire(new DataImageLoaded("previews", Local.Preview+" " + (images.size()+1), image.getValue1(), image.getValue2()));
				images.add(image);
			}
			public void done() {
				listener.done();
				synchronized (VideoDataType.this) {
					previewImages = images;
					DataImageLoader.release(VideoDataType.this, "previews");
				}
			}
		}
		try { DataImageLoader.load(this, "previews", new FileProvider_ListStartWith(getFolder("video"), "preview_", "video"), new Listener(listener)); }
		catch (CoreException e) {
			listener.started();
			listener.done();
			if (Log.error(this))
				Log.error(this, "Unable to load previews", e);
		}
	}

	private static final int MAX_PREVIEWS = 5;
	private void savePreviewImage(Image image, int index) {
		try {
			IFolder folder = getFolder("video");
			ResourceUtil.createFolderAndParents(folder);
			IFile file = folder.getFile("preview_" + index + ".jpg");
			ImageLoader il = new ImageLoader();
			il.data = new ImageData[] { image.getImageData() };
			MyByteArrayOutputStream stream = new MyByteArrayOutputStream();
			il.save(stream, SWT.IMAGE_JPEG);
			ByteArrayInputStream in = new ByteArrayInputStream(stream.getBuffer(), 0, stream.getBufferSize());
			if (file.exists())
				file.setContents(in, false, false, null);
			else
				file.create(in, true, null);
		} catch (CoreException e) {
			if (Log.error(this))
				Log.error(this, "Error while saving preview image", e);
		}
	}
	private void takePreviewImages(Composite visual, Media media, MediaPlayer player, WorkProgress progress, int work) {
		List<Double> times = new LinkedList<Double>();
		times.add((double)0);
		int nb = MAX_PREVIEWS;
		double step;
		do {
			step = (double)1 / nb;
			if (duration > 0 && step*duration<10) nb--; else break;
		} while (nb > 0);
		for (int i = 1; i < nb; ++i)
			times.add(step*i);

		nb = times.size();
		int index = 0;
		for (double time : times) {
			Image img = 
				SnapshotTaker.take(player, visual, time,  
						new LinkedChecker(new SameColorChecker(0,0,0,0x30,0.95), new SameColorChecker(255,255,255,0x30,0.95))
				);
			if (duration < 0)
				duration = media.getDuration();
			if (img != null) {
				savePreviewImage(img, index++);
			}
			int stepWork = work/nb--;
			work -= stepWork;
			progress.progress(stepWork);
		}
		if (work > 0)
			progress.progress(work);
	}
	
	public long getDuration() { return duration; }
	@Override
	public boolean isContentAvailable() { return loaded; }
	
	void loadContent(Composite visual, MediaPlayer player, WorkProgress progress, int work) {
		loaded = true;
		List<DataSource> sources = getData().getSources();
		if (sources.size() == 0) { progress.progress(work); signalModification(); return; }
		DataSource src = sources.get(0);
		if (src == null) { progress.progress(work); signalModification(); return; }
		URI uri;
		try { uri = src.ensurePresenceAndGetURI(); }
		catch (FileNotFoundException e) { uri = null; }
		if (uri == null) { progress.progress(work); signalModification(); return; }
		try {
			Media media = player.addMedia(uri);
			player.start();
			if (duration < 0)
				duration = media.getDuration();
			previewImages = null;
			takePreviewImages(visual, media, player, progress, work);
			if (duration < 0)
				duration = media.getDuration();
			player.stop();
			player.removeMedia(media);
		} catch (UnsupportedFormatException e) {
			// TODO
		}
		signalModification();
	}
	
	@Override
	public void createOverviewPanel(Composite panel, List<SourceInfo> sources, boolean big) {
		panel.setData(new VideoOverviewPanel(panel, this, sources, big));
	}
	@Override
	public void refreshOverviewPanel(Composite panel, List<SourceInfo> sources) {
		((VideoOverviewPanel)panel.getData()).refresh(sources);
	}
	@Override
	public boolean isOverviewPanelSupportingSourceMerge() {
		return true;
	}
	@Override
	public void createDescriptionPanel(Composite panel) {
		DescriptionPanel.create(this, panel);
	}
	@Override
	public void refreshDescriptionPanel(Composite panel) {
		UIControlUtil.clear(panel);
		createDescriptionPanel(panel);
	}

	@Override
	public DuplicateAnalysis checkForDuplicateOnContent(Data data) {
		return DuplicateAnalysis.DIFFERENT;
	}
	@Override
	public boolean isSame(Info info) {
		return false;
	}
	
	@Override
	public Control createImageCategoryControls(Composite parent) {
		return null;
	}
	
	@Override
	public void removeImage(DataImageLoaded image) {
		synchronized (this) {
			if (image.getCategoryID().equals("posters"))
				for (Iterator<Pair<Image,String>> it = posterImages.iterator(); it.hasNext(); ) {
					Pair<Image,String> p = it.next();
					if (p.getValue1() == image.getImage()) {
						it.remove();
						break;
					}
				}
			else
				for (Iterator<Pair<Image,String>> it = previewImages.iterator(); it.hasNext(); ) {
					Pair<Image,String> p = it.next();
					if (p.getValue1() == image.getImage()) {
						it.remove();
						break;
					}
				}
			try {
				IFile file = getFile(image.getFileName());
				file.delete(true, null);
			} catch (CoreException e) {
				if (Log.error(this))
					Log.error(this, "Unable to remove image file '" + image.getFileName() + "' for data ID " + getData().getID());
			}
		}
	}
	
	@Override
	protected void mergeContent(DataContentType other, Shell shell) {
		VideoDataType o = (VideoDataType)other;
		if (dimension == null)
			dimension = o.dimension;
		if (duration < 0)
			duration = o.duration;
		if (!loaded)
			loaded = o.loaded;
		previewImages = null;
		posterImages = null;
	}
}
