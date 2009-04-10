package net.lecousin.dataorganizer.core.database.content;

import java.io.ByteArrayInputStream;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.Data.DuplicateAnalysis;
import net.lecousin.dataorganizer.core.database.info.Info;
import net.lecousin.dataorganizer.core.database.info.InfoRetrieverPluginRegistry;
import net.lecousin.dataorganizer.core.database.info.SourceInfo;
import net.lecousin.dataorganizer.core.database.source.DataSource;
import net.lecousin.dataorganizer.core.database.version.ContentTypeLoader;
import net.lecousin.framework.Pair;
import net.lecousin.framework.collections.SelfMap;
import net.lecousin.framework.eclipse.resource.ResourceUtil;
import net.lecousin.framework.event.ProcessListener;
import net.lecousin.framework.io.FileSystemUtil;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.xml.XmlWriter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.Element;

public abstract class DataContentType implements SelfMap.Entry<Long> {

	public DataContentType(Data data, Element elt, ContentTypeLoader loader) {
		this.data = data;
		this.info = createInfo(loader.getInfo(elt), loader);
	}
	public DataContentType(Data data) {
		this.data = data;
		this.info = createInfo();
		signalModification();
	}
	protected abstract Info createInfo();
	protected abstract Info createInfo(Element elt, ContentTypeLoader loader);
	
	private Data data;
	private Info info;
	
	public Data getData() { return data; }
	public Info getInfo() { return info; }
	public void setData(Data data) { this.data = data; }
	
	public List<Pair<String,Image>> getAllPossibleNames() {
		List<Pair<String,Image>> list = new LinkedList<Pair<String,Image>>();
		for (String source : info.getSources()) {
			String name = info.getSourceName(source);
			if (name == null || name.trim().length() == 0) continue;
			Image icon = InfoRetrieverPluginRegistry.getIconForSource(source, data.getContentType().getID());
			list.add(new Pair<String,Image>(name, icon));
		}
		if (data.getSources().size() == 1) {
			list.add(new Pair<String,Image>(FileSystemUtil.getFileNameWithoutExtension(data.getSources().get(0).getFileName()), SharedImages.getImage(SharedImages.icons.x16.file.FILE)));
		}
		return list;
	}
	
	public String getSourceName(DataSource source) {
		return null;
	}
	
	public Long getHashObject() { return data.getID(); }
	
	public IFolder getFolder() throws CoreException { return data.getFolder(); }
	
	public abstract boolean isContentAvailable();
	
	public static final class DataImageCategory {
		public DataImageCategory(String id, String name, int priority)
		{ this.id = id; this.name = name; this.priority = priority; }
		private String id;
		private String name;
		private int priority;
		public String getID() { return id; }
		public String getName() { return name; }
		public int getPriority() { return priority; }
	}
	public static final class DataImageLoaded {
		public DataImageLoaded(String catID, String name, Image image, String filename)
		{ this.categoryID = catID; this.name = name; this.image = image; this.filename = filename; }
		private String categoryID;
		private String name;
		private Image image;
		private String filename;
		public String getCategoryID() { return categoryID; }
		public String getName() { return name; }
		public Image getImage() { return image; }
		public String getFileName() { return filename; }
	}
	public abstract List<DataImageCategory> getImagesCategories();
	public abstract void getImages(ProcessListener<DataImageLoaded> listener);
	public abstract void removeImage(DataImageLoaded image);
	public abstract Control createImageCategoryControls(Composite parent);
	
	public abstract void createOverviewPanel(Composite panel, SourceInfo sourceInfo);
	public abstract void createDescriptionPanel(Composite panel);
	public abstract boolean isOverviewPanelSupprotingSourceMerge();
	
	public abstract DuplicateAnalysis checkForDuplicateOnContent(Data data);
	public abstract boolean isSame(Info info);
	
	public final void save() {
//		System.out.println("Content: save: Info=" + info.toString() + " Data=" + this.toString() + " Data.Data=" + data.toString());
		XmlWriter xml = new XmlWriter();
		xml.openTag("content");
		saveContent(xml);
		xml.openTag("info");
		info.save(xml);
		xml.closeTag();
		xml.closeTag();
		try {
			IFolder folder = data.getFolder();
			ResourceUtil.createFolderAndParents(folder);
			IFile file = folder.getFile("content.xml");
			ByteArrayInputStream stream = new ByteArrayInputStream(xml.getXML().getBytes());
			if (!file.exists())
				file.create(stream, true, null);
			else
				file.setContents(stream, 0, null);
		} catch (Throwable t) {
			if (Log.error(this))
				Log.error(this, "Unable to save data content", t);
		}
	}
	protected abstract void saveContent(XmlWriter xml);
	
	public IFolder getFolder(String name) throws CoreException {
		return data.getFolder().getFolder(name);
	}
	public IFile getFile(String name) throws CoreException {
		return data.getFolder().getFile(new Path(name));
	}
	
	public void signalModification() {
		data.signalContentModification(this);
	}
}
