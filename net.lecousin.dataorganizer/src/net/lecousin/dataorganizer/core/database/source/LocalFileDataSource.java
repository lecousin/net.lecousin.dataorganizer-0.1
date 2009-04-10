package net.lecousin.dataorganizer.core.database.source;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.List;

import net.lecousin.dataorganizer.Local;
import net.lecousin.framework.collections.CollectionUtil;
import net.lecousin.framework.io.FileSystemUtil;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.xml.XmlWriter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.w3c.dom.Element;

public class LocalFileDataSource extends FileDataSource {

	LocalFileDataSource(String path, InputStream stream, long size) throws IOException, CoreException {
		super(stream, size);
		this.path = path;
	}
	LocalFileDataSource(Element elt) {
		super(elt);
		this.path = elt.getAttribute("path");
	}
	@Override
	protected void saveFileDataSource(XmlWriter xml) {
		xml.addAttribute("path", path);
	}
	
	String path;
	
	public String getLocalPath() { return path; }
	public File getLocalFile() { return new File(path); }
	public void setLocation(File file) { path = file.getAbsolutePath(); }
	public void setLocation(String path) { this.path = path; }
	
	@Override
	public void ensurePresence() {
	}
	
	@Override
	public List<File> removeFromFileSystem() throws Exception {
		File f = new File(path);
		if (!f.exists())
			throw new Exception(Local.Unable_to_locate_file.toString());
		f.delete();
		return CollectionUtil.single_element_list(f);
	}
	
	@Override
	public boolean isExactlyTheSame(DataSource src) {
		if (!(src instanceof LocalFileDataSource)) return false;
		return ((LocalFileDataSource)src).path.equals(path);
	}
	
	@Override
	public URI ensurePresenceAndGetURI() throws FileNotFoundException {
		File file = getLocalFile();
		if (!file.exists()) throw new FileNotFoundException(path);
		return file.toURI();
	}
	
	@Override
	public List<File> getLinkedFiles() {
		return CollectionUtil.single_element_list(new File(path));
	}
	@Override
	public boolean unlink(Collection<File> files) {
		File f = new File(path);
		if (files.contains(f))
			return true;
		return false;
	}
	
	@Override
	public String getFileName() {
		return FileSystemUtil.getFileName(path);
	}
	@Override
	public String getPathToDisplay() { return FileSystemUtil.getPath(path); }
	@Override
	public long getSize() {
		File f = new File(path);
		if (!f.exists()) return -1;
		return f.length();
	}
	@Override
	public Image getIcon() {
		return SharedImages.getImage(SharedImages.icons.x16.file.HARD_DRIVE);
	}
	
	@Override
	public String toString() {
		return path;
	}
}
