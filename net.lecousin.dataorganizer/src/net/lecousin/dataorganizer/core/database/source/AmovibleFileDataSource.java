package net.lecousin.dataorganizer.core.database.source;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.Local;
import net.lecousin.framework.collections.CollectionUtil;
import net.lecousin.framework.io.FileSystemUtil;
import net.lecousin.framework.io.FileSystemUtil.Drive;
import net.lecousin.framework.xml.XmlWriter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.w3c.dom.Element;

public class AmovibleFileDataSource extends FileDataSource {

	AmovibleFileDataSource(String amovibleDrivePath, String fileSubPath, InputStream stream, long size) throws IOException, CoreException {
		super(stream, size);
		this.amovibleDrivePath = amovibleDrivePath;
		this.fileSubPath = fileSubPath;
	}
	AmovibleFileDataSource(Element elt) {
		super(elt);
		this.amovibleDrivePath = elt.getAttribute("amovibleDrivePath");
		this.fileSubPath = elt.getAttribute("fileSubPath");
	}
	@Override
	protected void saveFileDataSource(XmlWriter xml) {
		xml.addAttribute("amovibleDrivePath", amovibleDrivePath);
		xml.addAttribute("fileSubPath", fileSubPath);
	}
	
	String amovibleDrivePath;
	String fileSubPath;
	
	@Override
	public boolean isExactlyTheSame(DataSource src) {
		if (!(src instanceof AmovibleFileDataSource)) return false;
		return fileSubPath.equals(((AmovibleFileDataSource)src).fileSubPath);
	}
	
	private File findFile() {
		File f = new File(amovibleDrivePath + File.separator + fileSubPath);
		if (f.exists()) return f;
		for (Drive drive : FileSystemUtil.getAmovibleDrives()) {
			f = new File(drive.getRoot(), fileSubPath);
			if (f.exists()) return f;
		}
		return null;
	}
	
	@Override
	public List<File> removeFromFileSystem() throws Exception {
		ensurePresence();
		File f = findFile();
		if (f == null)
			throw new Exception(Local.Unable_to_locate_file.toString());
		f.delete();
		return CollectionUtil.single_element_list(f);
	}
	@Override
	public List<File> getLinkedFiles() {
		File file = findFile();
		if (file == null) return new LinkedList<File>();
		return CollectionUtil.single_element_list(file);
	}
	@Override
	public boolean unlink(Collection<File> files) {
		File file = findFile();
		if (file == null) return false;
		if (!files.contains(file)) return false;
		return true;
	}
	
	@Override
	public void ensurePresence() {
		do {
			File f = findFile();
			if (f != null) return;
			if (!MessageDialog.openConfirm(null, Local.Amovible_media.toString(), Local.Please_insert_the_amovible_media_containing+" '"+fileSubPath+"' ."))
				return;
		} while (true);
	}
	
	@Override
	public URI ensurePresenceAndGetURI() throws FileNotFoundException {
		ensurePresence();
		File f = findFile();
		if (f == null) throw new FileNotFoundException(toString());
		return f.toURI();
	}
	
	@Override
	public String getFileName() {
		return FileSystemUtil.getFileName(fileSubPath);
	}
	
	@Override
	public String toString() {
		return Local.Amovible_media+"("+amovibleDrivePath+"):"+fileSubPath;
	}
}
