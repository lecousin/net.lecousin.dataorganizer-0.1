package net.lecousin.dataorganizer.core.database.content;

import java.io.ByteArrayInputStream;

import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.Data.DuplicateAnalysis;
import net.lecousin.dataorganizer.core.database.info.Info;
import net.lecousin.dataorganizer.core.database.version.ContentTypeLoader;
import net.lecousin.framework.Triple;
import net.lecousin.framework.collections.SelfMap;
import net.lecousin.framework.eclipse.resource.ResourceUtil;
import net.lecousin.framework.event.ProcessListener;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.xml.XmlWriter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
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
	
	public Long getHashObject() { return data.getID(); }
	
	public IFolder getFolder() throws CoreException { return data.getFolder(); }
	
	public abstract boolean isContentAvailable();
	
	public abstract void getImages(ProcessListener<Triple<String,Image,Integer>> listener);
	public abstract void createOverviewPanel(Composite panel);
	public abstract void createDescriptionPanel(Composite panel);
	
	public abstract DuplicateAnalysis checkForDuplicateOnContent(Data data);
	public abstract boolean isSame(Info info);
	
	public final void save() {
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
	
	public void signalModification() {
		data.signalContentModification(this);
	}
}
