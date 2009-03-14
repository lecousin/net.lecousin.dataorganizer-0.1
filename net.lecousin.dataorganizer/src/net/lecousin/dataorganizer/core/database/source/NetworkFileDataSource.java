package net.lecousin.dataorganizer.core.database.source;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

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
	
	private String uri;
	
	@Override
	public void ensurePresence() {
	}
	
	@Override
	public void removeFromFileSystem() throws Exception {
		IFileStore store = EFS.getStore(new URI(uri));
		store.delete(0, null);
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
	public String toString() {
		return uri;
	}
}
