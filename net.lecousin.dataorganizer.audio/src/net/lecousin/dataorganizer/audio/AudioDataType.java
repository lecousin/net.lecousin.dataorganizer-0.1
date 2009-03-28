package net.lecousin.dataorganizer.audio;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.audio.AudioInfo.Track;
import net.lecousin.dataorganizer.audio.ui.OverviewPanel;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.Data.DuplicateAnalysis;
import net.lecousin.dataorganizer.core.database.content.DataContentType;
import net.lecousin.dataorganizer.core.database.info.Info;
import net.lecousin.dataorganizer.core.database.source.DataSource;
import net.lecousin.dataorganizer.core.database.version.ContentTypeLoader;
import net.lecousin.dataorganizer.util.DataImageLoader;
import net.lecousin.dataorganizer.util.DataImageLoader.FileProvider;
import net.lecousin.framework.Triple;
import net.lecousin.framework.eclipse.resource.ResourceUtil;
import net.lecousin.framework.event.ProcessListener;
import net.lecousin.framework.event.SplitProcessListener;
import net.lecousin.framework.io.MyByteArrayOutputStream;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.xml.XmlWriter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Composite;
import org.w3c.dom.Element;

public class AudioDataType extends DataContentType {

	public AudioDataType(Data data) {
		super(data);
	}
	public AudioDataType(Data data, Element elt, ContentTypeLoader loader) {
		super(data, elt, loader);
	}
	@Override
	protected void saveContent(XmlWriter xml) {
		// TODO Auto-generated method stub

	}

	@Override
	protected Info createInfo() { return new AudioInfo(this, (String)null, (String)null); }
	@Override
	protected Info createInfo(Element elt, ContentTypeLoader loader) { return new AudioInfo(this, elt, loader); }
	
	@Override
	public String getSourceName(DataSource source) {
		int index = getData().getSources().indexOf(source);
		List<Track> tracks = ((AudioInfo)getInfo()).getTracks();
		if (index < 0 || index >= tracks.size()) return null;
		return tracks.get(index).getTitle();
	}

	@Override
	public DuplicateAnalysis checkForDuplicateOnContent(Data data) {
		// TODO Auto-generated method stub
		return DuplicateAnalysis.DIFFERENT;
	}
	@Override
	public boolean isSame(Info info) {
		// TODO Auto-generated method stub
		return false;
	}

	
	@Override
	public void createDescriptionPanel(Composite panel) {
		// TODO Auto-generated method stub

	}
	@Override
	public void createOverviewPanel(Composite panel) {
		new OverviewPanel(panel, this);
	}

	private List<Image> coverFront = null; 
	private List<Image> coverBack = null; 
	private List<Image> otherImages = null; 
	
	@Override
	public void getImages(ProcessListener<Triple<String, Image, Integer>> listener) {
		SplitProcessListener<Triple<String, Image, Integer>> split = new SplitProcessListener<Triple<String, Image, Integer>>(listener);
		ProcessListener<Triple<String, Image, Integer>> frontListener = split.newListener();
		ProcessListener<Triple<String, Image, Integer>> backListener = split.newListener();
		ProcessListener<Triple<String, Image, Integer>> othersListener = split.newListener();
		loadCoverFronts(frontListener);
		loadCoverBacks(backListener);
		loadOtherImages(othersListener);
	}
	
	private void loadCoverFronts(ProcessListener<Triple<String, Image, Integer>> listener) {
		load(listener, new CoverFrontProvider(), "cover_front_", Local.Cover_front_picture.toString(), 10);
	}
	private void loadCoverBacks(ProcessListener<Triple<String, Image, Integer>> listener) {
		load(listener, new CoverBackProvider(), "cover_back_", Local.Cover_back_picture.toString(), 50);
	}
	private void loadOtherImages(ProcessListener<Triple<String, Image, Integer>> listener) {
		load(listener, new OtherImagesProvider(), "image_", Local.Other_album_picture.toString(), 200);
	}
	private static interface ImageListProvider {
		public List<Image> get();
		public void set(List<Image> images);
	}
	private class CoverFrontProvider implements ImageListProvider {
		public List<Image> get() { return coverFront; }
		public void set(List<Image> images) { coverFront = images; } 
	}
	private class CoverBackProvider implements ImageListProvider {
		public List<Image> get() { return coverBack; }
		public void set(List<Image> images) { coverBack = images; } 
	}
	private class OtherImagesProvider implements ImageListProvider {
		public List<Image> get() { return otherImages; }
		public void set(List<Image> images) { otherImages = images; } 
	}
	
	private void load(ProcessListener<Triple<String, Image, Integer>> listener, ImageListProvider provider, String prefix, String name, int priority) {
		synchronized (this) {
			List<Image> images = provider.get();
			if (images != null) {
				listener.started();
				for (int i = 0; i < images.size(); ++i)
					listener.fire(new Triple<String, Image, Integer>(name+" " + (i+1), images.get(i), priority));
				listener.done();
				return;
			}
		}
		class Listener implements ProcessListener<Image> {
			Listener(ProcessListener<Triple<String, Image, Integer>> listener, ImageListProvider provider, String prefix, String name, int priority) {
				this.listener = listener;
				this.provider = provider;
				this.prefix = prefix;
				this.name = name;
				this.priority = priority;
			}
			private ProcessListener<Triple<String, Image, Integer>> listener;
			private ImageListProvider provider;
			private String prefix;
			private String name;
			private int priority;
			private List<Image> images = new LinkedList<Image>();
			public void started() {
				listener.started();
			}
			public void fire(Image image) {
				listener.fire(new Triple<String,Image,Integer>(name+" " + (images.size()+1), image, priority));
				images.add(image);
			}
			public void done() {
				listener.done();
				synchronized (AudioDataType.this) {
					provider.set(images);
					DataImageLoader.release(AudioDataType.this, prefix);
				}
			}
		}
		DataImageLoader.load(this, prefix, new FileProvider_Prefix(prefix), new Listener(listener, provider, prefix, name, priority));
	}
	private class FileProvider_Prefix implements FileProvider {
		public FileProvider_Prefix(String prefix) {
			this.prefix = prefix;
			try {
				IFolder folder = getFolder("audio");
				ResourceUtil.createFolderAndParents(folder);
				members = folder.members();
				goToNext();
			} catch (CoreException e) {
				members = null;
			}
		}
		private String prefix;
		private int index = 0;
		private IResource[] members;
		public boolean hasNext() {
			if (members == null) return false;
			if (index >= members.length) return false;
			return true;
		}
		public File next() {
			File file = members[index].getLocation().toFile();
			index++;
			goToNext();
			return file;
		}
		private void goToNext() {
			while (index < members.length) {
				if ((members[index] instanceof IFile)) {
					if (members[index].getName().startsWith(prefix))
						break;
				}
				index++;
			}
		}
	}
	
	public void saveCoverFront(ImageData[] image) {
		saveImage(image, "cover_front_");
	}
	public void saveCoverBack(ImageData[] image) {
		saveImage(image, "cover_back_");
	}
	public void saveImage(ImageData[] image) {
		saveImage(image, "image_");
	}
	private void saveImage(ImageData[] data, String prefix) {
		try {
			IFolder folder = getFolder("audio");
			ResourceUtil.createFolderAndParents(folder);
			int index = 1;
			int type = data[0].type;
			IFile file;
			do {
				file = folder.getFile(prefix + index + getImageExtension(type));
			} while (file.exists() && index < 100);
			if (file.exists()) return;
			ImageLoader il = new ImageLoader();
			il.data = data;
			MyByteArrayOutputStream stream = new MyByteArrayOutputStream();
			il.save(stream, type);
			ByteArrayInputStream in = new ByteArrayInputStream(stream.getBuffer(), 0, stream.getBufferSize());
			if (file.exists())
				file.setContents(in, false, false, null);
			else
				file.create(in, true, null);
		} catch (CoreException e) {
			if (Log.error(this))
				Log.error(this, "Error while saving image", e);
		}
	}
	private String getImageExtension(int type) {
		switch (type) {
		case SWT.IMAGE_BMP:
		case SWT.IMAGE_OS2_BMP:
		case SWT.IMAGE_BMP_RLE: return ".bmp";
		case SWT.IMAGE_GIF: return ".gif";
		case SWT.IMAGE_ICO: return ".ico";
		case SWT.IMAGE_JPEG: return ".jpg";
		case SWT.IMAGE_PNG: return ".png";
		case SWT.IMAGE_TIFF: return ".tif";
		}
		return ".jpeg"; // TODO???
	}

	@Override
	public boolean isContentAvailable() {
		// TODO Auto-generated method stub
		return false;
	}

}
