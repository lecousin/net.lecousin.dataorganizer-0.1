package net.lecousin.dataorganizer.core.database.source;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

import net.lecousin.framework.Pair;
import net.lecousin.framework.io.FileSystemUtil;
import net.lecousin.framework.xml.XmlWriter;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Element;

public abstract class DataSource {

	public static DataSource load(Element elt) {
		String type = elt.getAttribute("type");
		if (type.equals("amovible"))
			return new AmovibleFileDataSource(elt);
		if (type.equals("network"))
			return new NetworkFileDataSource(elt);
		if (type.equals("local"))
			return new LocalFileDataSource(elt);
		return null;
	}
	
	public final void save(XmlWriter xml) {
		if (this instanceof AmovibleFileDataSource)
			xml.addAttribute("type", "amovible");
		else if (this instanceof NetworkFileDataSource)
			xml.addAttribute("type", "network");
		else if (this instanceof LocalFileDataSource)
			xml.addAttribute("type", "local");
		else
			throw new UnsupportedOperationException("Saving DataSource type " + getClass().getName() + " is not implemented.");
		saveDataSource(xml);
	}
	protected abstract void saveDataSource(XmlWriter xml);
	
	public static DataSource get(IFileStore file) throws IOException, CoreException {
		long size = file.fetchInfo().getLength();
		InputStream stream = file.openInputStream(0, null);
		File f = file.toLocalFile(0, null);
		if (f == null)
			// not local
			return new NetworkFileDataSource(file.toURI().toString(), stream, size);
		// local
		return get(f, stream, size);
	}
	public static DataSource get(File file) throws FileNotFoundException, CoreException, IOException {
		InputStream stream = new FileInputStream(file);
		long size = file.length();
		return get(file, stream, size);
	}
	private static DataSource get(File f, InputStream stream, long size) throws CoreException, IOException {
		Pair<String,String> amovible = getAmovibleSubPaths(f);
		if (amovible != null)
			// on amovible media
			return new AmovibleFileDataSource(amovible.getValue1(), amovible.getValue2(), stream, size);
		return new LocalFileDataSource(f.getAbsolutePath(), stream, size);
	}
	public static DataSource get(String uri) throws URISyntaxException, CoreException, IOException {
		return get(new URI(uri));
	}
	public static DataSource get(URI uri) throws URISyntaxException, CoreException, IOException {
		return get(EFS.getStore(uri));
	}
	private static Pair<String,String> getAmovibleSubPaths(File f) {
		File a = FileSystemUtil.getAmovibleDrive(f);
		if (a == null) return null;
		String ap = a.getAbsolutePath();
		String fp = f.getAbsolutePath().substring(ap.length());
		if (fp.startsWith(File.separator))
			fp = fp.substring(File.separator.length());
		if (ap.endsWith(File.separator))
			ap = ap.substring(0, ap.length()-File.separator.length());
		return new Pair<String,String>(ap, fp);
	}
	
	public boolean hasLink(IFileStore file) {
		File f = null;
		try { f = file.toLocalFile(0, null); }
		catch (CoreException e) {}
		if (f == null) {
			// not local
			return hasLinkNotLocal(file);
		}
		// local
		return hasLink(f);
	}
	public boolean hasLinkNotLocal(IFileStore file) {
		if (!(this instanceof NetworkFileDataSource)) return false;
		if (((NetworkFileDataSource)this).uri.equals(file.toURI().toString())) return true;
		return false;
	}
	public boolean hasLink(File file) {
		if (this instanceof LocalFileDataSource) {
			if (((LocalFileDataSource)this).path.equals(file.getAbsolutePath())) return true;
			return false;
		}
		Pair<String,String> amovible = getAmovibleSubPaths(file);
		if (amovible != null) {
			// on amovible media
			if (!(this instanceof AmovibleFileDataSource)) return false;
			if (((AmovibleFileDataSource)this).fileSubPath.equals(amovible.getValue2())) return true;
			return false;
		}
		return false;
	}
	public boolean hasLink(String uri) {
		try { return hasLink(new URI(uri)); }
		catch (URISyntaxException e) { return false; }
	}
	public boolean hasLink(URI uri) {
		try { return hasLink(EFS.getStore(uri)); }
		catch (CoreException e) { return false; }
	}
	
	
	public abstract boolean isExactlyTheSame(DataSource src);
	public abstract boolean isSameInDifferentLocation(DataSource src);

	public abstract void ensurePresence();
	public abstract List<File> removeFromFileSystem() throws Exception;
	/** return true if the data source do not contain any files after the unlink operation */
	public abstract boolean unlink(Collection<File> files);
	public abstract List<File> getLinkedFiles();
	
	public abstract String getFileName();
	
	public abstract URI ensurePresenceAndGetURI() throws FileNotFoundException;
}
