package net.lecousin.dataorganizer.video;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.Data.DuplicateAnalysis;
import net.lecousin.dataorganizer.core.database.content.DataContentType;
import net.lecousin.dataorganizer.core.database.info.Info;
import net.lecousin.dataorganizer.core.database.source.DataSource;
import net.lecousin.dataorganizer.core.database.version.ContentTypeLoader;
import net.lecousin.dataorganizer.util.DataImageLoader;
import net.lecousin.dataorganizer.util.DataImageLoader.FileProvider_FromDataPath;
import net.lecousin.dataorganizer.util.DataImageLoader.FileProvider_ListStartWith;
import net.lecousin.dataorganizer.video.ui.DescriptionPanel;
import net.lecousin.dataorganizer.video.ui.OverviewPanel;
import net.lecousin.framework.Triple;
import net.lecousin.framework.eclipse.resource.ResourceUtil;
import net.lecousin.framework.event.ProcessListener;
import net.lecousin.framework.event.SplitProcessListener;
import net.lecousin.framework.io.MyByteArrayOutputStream;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.media.Media;
import net.lecousin.framework.media.MediaPlayer;
import net.lecousin.framework.media.util.SnapshotTaker;
import net.lecousin.framework.media.util.SnapshotTaker.LinkedChecker;
import net.lecousin.framework.media.util.SnapshotTaker.SameColorChecker;
import net.lecousin.framework.xml.XmlWriter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Composite;
import org.w3c.dom.Element;

public class VideoDataType extends DataContentType {

	public VideoDataType(Data data, Element elt, Loader loader) {
		super(data, elt, loader);
		duration = loader.getDuration(elt);
		loaded = loader.getLoaded(elt);
	}
	@Override
	protected Info createInfo(Element elt, ContentTypeLoader loader) { return new VideoInfo(this, elt, (Loader)loader); }
	@Override
	protected void saveContent(XmlWriter xml) {
		xml.addAttribute("duration", Long.toString(duration));
	}
	public VideoDataType(Data data) {
		super(data);
	}
	@Override
	protected Info createInfo() { return new VideoInfo(this, (String)null, null); }
	
	private boolean loaded = false;
	private long duration = -1;
	
	private List<Image> previewImages = null;
	private List<Image> posterImages = null;
	
	void signalNewPoster() {
		posterImages = null;
	}
	
	@Override
	public void getImages(ProcessListener<Triple<String, Image, Integer>> listener) {
		SplitProcessListener<Triple<String, Image, Integer>> split = new SplitProcessListener<Triple<String, Image, Integer>>(listener);
		ProcessListener<Triple<String, Image, Integer>> posterListener = split.newListener();
		ProcessListener<Triple<String, Image, Integer>> previewListener = split.newListener();
		loadPosters(posterListener);
		loadPreviews(previewListener);
	}
	
	private void loadPosters(ProcessListener<Triple<String, Image, Integer>> listener) {
		VideoInfo info = (VideoInfo)getInfo();
		if (info == null) {
			listener.started();
			listener.done();
			return;
		}
		synchronized (this) {
			if (posterImages != null) {
				listener.started();
				for (int i = 0; i < posterImages.size(); ++i)
					listener.fire(new Triple<String, Image, Integer>(Local.Poster+" " + (i+1), posterImages.get(i), 10));
				listener.done();
				return;
			}
		}
		Collection<String> paths = info.getPostersPaths();
		if (paths.isEmpty()) {
			synchronized (this) {
				posterImages = new LinkedList<Image>();
			}
			listener.started();
			listener.done();
			return;
		}
		class Listener implements ProcessListener<Image> {
			Listener(ProcessListener<Triple<String, Image, Integer>> listener) {
				this.listener = listener;
			}
			private ProcessListener<Triple<String, Image, Integer>> listener;
			private List<Image> images = new LinkedList<Image>();
			public void started() {
				listener.started();
			}
			public void fire(Image image) {
				listener.fire(new Triple<String,Image,Integer>(Local.Poster+" " + (images.size()+1), image, 10));
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
	private void loadPreviews(ProcessListener<Triple<String, Image, Integer>> listener) {
		synchronized (this) {
			if (previewImages != null) {
				listener.started();
				for (int i = 0; i < previewImages.size(); ++i)
					listener.fire(new Triple<String, Image, Integer>(Local.Preview+" " + (i+1), previewImages.get(i), 100));
				listener.done();
				return;
			}
		}
		class Listener implements ProcessListener<Image> {
			Listener(ProcessListener<Triple<String, Image, Integer>> listener) {
				this.listener = listener;
			}
			private ProcessListener<Triple<String, Image, Integer>> listener;
			private List<Image> images = new LinkedList<Image>();
			public void started() {
				listener.started();
			}
			public void fire(Image image) {
				listener.fire(new Triple<String,Image, Integer>(Local.Preview+" " + (images.size()+1), image, 100));
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
		try { DataImageLoader.load(this, "previews", new FileProvider_ListStartWith(getFolder("video"), "preview_"), new Listener(listener)); }
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
	private void takePreviewImages(Composite visual, MediaPlayer player) {
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

		int index = 0;
		for (double time : times) {
			Image img = 
				SnapshotTaker.take(player, visual, time,  
						new LinkedChecker(new SameColorChecker(0,0,0,0x30,0.95), new SameColorChecker(255,255,255,0x30,0.95))
				);
			if (img != null) {
				savePreviewImage(img, index++);
			}
		}
	}
	
	public long getDuration() { return duration; }
	@Override
	public boolean isContentAvailable() { return loaded; }
	
	void loadContent(Composite visual, MediaPlayer player) {
		List<DataSource> sources = getData().getSources();
		if (sources.size() == 0) return;
		DataSource src = sources.get(0);
		URI uri = src.ensurePresenceAndGetURI();
		if (uri == null) return;
		Media media = player.addMedia(uri);
		player.start();
		duration = media.getDuration();
		takePreviewImages(visual, player);
		player.stop();
		player.removeMedia(media);
	}
	
	@Override
	public void createOverviewPanel(Composite panel) {
		new OverviewPanel(panel, this);
	}
	@Override
	public void createDescriptionPanel(Composite panel) {
		DescriptionPanel.create(this, panel);
	}

	@Override
	public DuplicateAnalysis checkForDuplicateOnContent(Data data) {
		return DuplicateAnalysis.DIFFERENT;
	}
	@Override
	public boolean isSame(Info info) {
		return false;
	}
}
