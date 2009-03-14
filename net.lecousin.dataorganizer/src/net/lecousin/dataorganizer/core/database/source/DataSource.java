package net.lecousin.dataorganizer.core.database.source;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.filechooser.FileSystemView;

import net.lecousin.framework.Pair;
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
		return get(EFS.getStore(new URI(uri)));
	}
	private static Pair<String,String> getAmovibleSubPaths(File f) {
		File a = getAmovibleFile(f);
		if (a == null) return null;
		String ap = a.getAbsolutePath();
		String fp = f.getAbsolutePath().substring(ap.length());
		if (fp.startsWith(File.pathSeparator))
			fp = fp.substring(File.pathSeparator.length());
		return new Pair<String,String>(ap, fp);
	}
	private static File getAmovibleFile(File f) {
		if (FileSystemView.getFileSystemView().isFloppyDrive(f))
			return f;
		File p = f.getParentFile();
		if (p == null) return null;
		return getAmovibleFile(p);
	}
	
	public abstract boolean isExactlyTheSame(DataSource src);
	public abstract boolean isSameInDifferentLocation(DataSource src);

	public abstract void ensurePresence();
	public abstract void removeFromFileSystem() throws Exception;
	
	public abstract URI ensurePresenceAndGetURI();
}
