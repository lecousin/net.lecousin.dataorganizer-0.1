package net.lecousin.dataorganizer.core.database.source;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.framework.collections.CollectionUtil;
import net.lecousin.framework.io.FileSystemUtil;
import net.lecousin.framework.xml.XmlWriter;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Element;

public class NetworkFileDataSource extends FileDataSource {

	NetworkFileDataSource(String uri, InputStream stream, long size) throws IOException, CoreException {
		super(stream, size);
		this.uri = uri;
	}
	NetworkFileDataSource(Element elt) {
		super(elt);
		this.uri = elt.getAttribute("uri");
	}
	@Override
	protected void saveFileDataSource(XmlWriter xml) {
		xml.addAttribute("uri", uri);
	}
	
	String uri;
	
	@Override
	public void ensurePresence() {
	}
	
	@Override
	public List<File> removeFromFileSystem() throws Exception {
		IFileStore store = EFS.getStore(new URI(uri));
		store.delete(0, null);
		File file = store.toLocalFile(EFS.NONE, null);
		if (file == null) return new LinkedList<File>();
		return CollectionUtil.single_element_list(file);
	}
	@Override
	public List<File> getLinkedFiles() {
		try {
			IFileStore store = EFS.getStore(new URI(uri));
			File file = store.toLocalFile(EFS.NONE, null);
			if (file != null) 
				return CollectionUtil.single_element_list(file);
		} catch (Throwable t) {}
		return new LinkedList<File>();
	}
	@Override
	public boolean unlink(Collection<File> files) {
		try {
			IFileStore store = EFS.getStore(new URI(uri));
			File file = store.toLocalFile(EFS.NONE, null);
			if (file == null) return false;
			if (!files.contains(file)) return false;
			return true;
		} catch (Throwable t) { return false; }
	}
	
	@Override
	public boolean isExactlyTheSame(DataSource src) {
		if (!(src instanceof NetworkFileDataSource)) return false;
		return ((NetworkFileDataSource)src).uri.equals(uri);
	}
	
	@Override
	public URI ensurePresenceAndGetURI() {
		try { return new URI(uri); }
		catch (URISyntaxException e) { return null; }
	}
	
	@Override
	public String getFileName() {
		try { return FileSystemUtil.getFileName(new URI(uri).getPath()); }
		catch (URISyntaxException e) { return FileSystemUtil.getFileName(uri); }
	}
	
	@Override
	public String toString() {
		return uri;
	}
}
