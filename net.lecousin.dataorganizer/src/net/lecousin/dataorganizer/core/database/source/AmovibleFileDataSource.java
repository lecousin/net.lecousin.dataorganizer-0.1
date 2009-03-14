package net.lecousin.dataorganizer.core.database.source;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.swing.filechooser.FileSystemView;

import net.lecousin.dataorganizer.Local;
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
	
	private String amovibleDrivePath;
	private String fileSubPath;
	
	@Override
	public boolean isExactlyTheSame(DataSource src) {
		if (!(src instanceof AmovibleFileDataSource)) return false;
		return fileSubPath.equals(((AmovibleFileDataSource)src).fileSubPath);
	}
	
	private File findFile() {
		File f = new File(amovibleDrivePath + File.pathSeparator + fileSubPath);
		if (f.exists()) return f;
		File[] roots = FileSystemView.getFileSystemView().getRoots();
		for (File root : roots) {
			f = new File(root, fileSubPath);
			if (f.exists()) return f;
		}
		return null;
	}
	
	@Override
	public void removeFromFileSystem() throws Exception {
		ensurePresence();
		File f = findFile();
		if (f == null)
			throw new Exception(Local.Unable_to_locate_file.toString());
		f.delete();
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
	public URI ensurePresenceAndGetURI() {
		ensurePresence();
		File f = findFile();
		if (f == null) return null;
		return f.toURI();
	}
	
	@Override
	public String toString() {
		return Local.Amovible_media+"("+amovibleDrivePath+"):"+fileSubPath;
	}
}
