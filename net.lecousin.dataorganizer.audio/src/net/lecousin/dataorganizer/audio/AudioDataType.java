package net.lecousin.dataorganizer.audio;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.audio.AudioSourceInfo.Track;
import net.lecousin.dataorganizer.audio.ui.OverviewPanel;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.Data.DuplicateAnalysis;
import net.lecousin.dataorganizer.core.database.content.DataContentType;
import net.lecousin.dataorganizer.core.database.info.Info;
import net.lecousin.dataorganizer.core.database.info.SourceInfo;
import net.lecousin.dataorganizer.core.database.source.DataSource;
import net.lecousin.dataorganizer.core.database.version.ContentTypeLoader;
import net.lecousin.dataorganizer.util.DataImageLoader;
import net.lecousin.dataorganizer.util.DataImageLoader.FileProvider;
import net.lecousin.framework.Pair;
import net.lecousin.framework.collections.CollectionUtil;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
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
	}

	@Override
	protected Info createInfo() { return new AudioInfo(this); }
	@Override
	protected Info createInfo(Element elt, ContentTypeLoader loader) { return new AudioInfo(this, elt, loader); }
	
	@Override
	public String getSourceName(DataSource source) {
		int index = getData().getSources().indexOf(source);
		AudioSourceInfo sourceInfo = ((AudioInfo)getInfo()).getSourceInfo(AudioInfo.FILE_SOURCE);
		List<Track> tracks = sourceInfo.getTracks();
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
	public void createOverviewPanel(Composite panel, List<SourceInfo> sources) {
		new OverviewPanel(panel, this, sources);
	}
	@Override
	public boolean isOverviewPanelSupportingSourceMerge() {
		return false;
	}

	private List<Pair<Image,String>> coverFront = null; 
	private List<Pair<Image,String>> coverBack = null; 
	private List<Pair<Image,String>> otherImages = null; 

	private static final DataImageCategory[] categories = new DataImageCategory[] {
		new DataImageCategory("cover.front", Local.Cover_front_pictures.toString(), 10),
		new DataImageCategory("cover.back", Local.Cover_back_pictures.toString(), 50),
		new DataImageCategory("others", Local.Other_album_pictures.toString(), 200),
	};
	@Override
	public List<DataImageCategory> getImagesCategories() { return CollectionUtil.list(categories); }
	@Override
	public void getImages(ProcessListener<DataImageLoaded> listener) {
		SplitProcessListener<DataImageLoaded> split = new SplitProcessListener<DataImageLoaded>(listener);
		ProcessListener<DataImageLoaded> frontListener = split.newListener();
		ProcessListener<DataImageLoaded> backListener = split.newListener();
		ProcessListener<DataImageLoaded> othersListener = split.newListener();
		loadCoverFronts(frontListener);
		loadCoverBacks(backListener);
		loadOtherImages(othersListener);
	}
	
	private void loadCoverFronts(ProcessListener<DataImageLoaded> listener) {
		load(listener, new CoverFrontProvider(), "cover_front_", Local.Cover_front_picture.toString(), "cover.front");
	}
	private void loadCoverBacks(ProcessListener<DataImageLoaded> listener) {
		load(listener, new CoverBackProvider(), "cover_back_", Local.Cover_back_picture.toString(), "cover.back");
	}
	private void loadOtherImages(ProcessListener<DataImageLoaded> listener) {
		load(listener, new OtherImagesProvider(), "image_", Local.Other_album_picture.toString(), "others");
	}
	private static interface ImageListProvider {
		public List<Pair<Image,String>> get();
		public void set(List<Pair<Image,String>> images);
	}
	private class CoverFrontProvider implements ImageListProvider {
		public List<Pair<Image,String>> get() { return coverFront; }
		public void set(List<Pair<Image,String>> images) { coverFront = images; } 
	}
	private class CoverBackProvider implements ImageListProvider {
		public List<Pair<Image,String>> get() { return coverBack; }
		public void set(List<Pair<Image,String>> images) { coverBack = images; } 
	}
	private class OtherImagesProvider implements ImageListProvider {
		public List<Pair<Image,String>> get() { return otherImages; }
		public void set(List<Pair<Image,String>> images) { otherImages = images; } 
	}
	
	private void load(ProcessListener<DataImageLoaded> listener, ImageListProvider provider, String prefix, String name, String categoryID) {
		synchronized (this) {
			List<Pair<Image,String>> images = provider.get();
			if (images != null) {
				listener.started();
				for (int i = 0; i < images.size(); ++i) {
					Pair<Image,String> p = images.get(i);
					listener.fire(new DataImageLoaded(categoryID, name+" " + (i+1), p.getValue1(), p.getValue2()));
				}
				listener.done();
				return;
			}
		}
		class Listener implements ProcessListener<Pair<Image,String>> {
			Listener(ProcessListener<DataImageLoaded> listener, ImageListProvider provider, String prefix, String name, String categoryID) {
				this.listener = listener;
				this.provider = provider;
				this.prefix = prefix;
				this.name = name;
				this.categoryID = categoryID;
			}
			private ProcessListener<DataImageLoaded> listener;
			private ImageListProvider provider;
			private String prefix;
			private String name;
			private String categoryID;
			private List<Pair<Image,String>> images = new LinkedList<Pair<Image,String>>();
			public void started() {
				listener.started();
			}
			public void fire(Pair<Image,String> image) {
				listener.fire(new DataImageLoaded(categoryID, name+" " + (images.size()+1), image.getValue1(), image.getValue2()));
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
		DataImageLoader.load(this, prefix, new FileProvider_Prefix(prefix), new Listener(listener, provider, prefix, name, categoryID));
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
		public Pair<File,String> next() {
			File file = members[index].getLocation().toFile();
			index++;
			goToNext();
			return new Pair<File,String>(file,"audio/"+file.getName());
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
			int index = 0;
			int type = data[0].type;
			IFile file;
			do {
				index++;
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
	public Control createImageCategoryControls(Composite parent) {
		return null;
	}
	
	@Override
	public void removeImage(DataImageLoaded image) {
		synchronized (this) {
			if (image.getCategoryID().equals("cover.front"))
				for (Iterator<Pair<Image,String>> it = coverFront.iterator(); it.hasNext(); ) {
					Pair<Image,String> p = it.next();
					if (p.getValue1() == image.getImage()) {
						it.remove();
						break;
					}
				}
			else if (image.getCategoryID().equals("cover.back"))
				for (Iterator<Pair<Image,String>> it = coverBack.iterator(); it.hasNext(); ) {
					Pair<Image,String> p = it.next();
					if (p.getValue1() == image.getImage()) {
						it.remove();
						break;
					}
				}
			else
				for (Iterator<Pair<Image,String>> it = otherImages.iterator(); it.hasNext(); ) {
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
	public boolean isContentAvailable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void mergeContent(DataContentType other, Shell shell) {
		coverFront = null;
		coverBack = null;
		otherImages = null;
	}
}
