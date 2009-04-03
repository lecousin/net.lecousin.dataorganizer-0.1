package net.lecousin.dataorganizer.core.database.source;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import net.lecousin.framework.collections.ArrayUtil;
import net.lecousin.framework.io.IOUtil;
import net.lecousin.framework.strings.StringUtil;
import net.lecousin.framework.xml.XmlUtil;
import net.lecousin.framework.xml.XmlWriter;

import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Element;

public abstract class FileDataSource extends DataSource {

	private static final int HEADER_SIZE = 256;
	private static final int PART_SIZE = 64;
	private static final int MAX_PARTS = 10;
	
	private long size;
	private byte[] header;
	private FilePart[] parts;
	private long hashcode;
	
	private static class FilePart {
		long offset;
		byte[] data;
	}
	
	FileDataSource(InputStream stream, long size) throws CoreException, IOException {
		this.size = size;
		if (size > 0) {
			int hsize = size > HEADER_SIZE ? HEADER_SIZE : (int)size;
			byte[] buffer = new byte[hsize];
			int i = IOUtil.readAllBuffer(stream, buffer);
			if (i == hsize)
				this.header = buffer;
			else {
				this.header = new byte[i];
				System.arraycopy(buffer, 0, this.header, 0, i);
			}
			if (size == this.header.length) {
				this.hashcode = hash(this.header);
			} else {
				buildPartAndHash(this.header, stream, size);
			}
		} else
			this.hashcode = 0;
	}
	FileDataSource(Element elt) {
		this.size = Long.parseLong(elt.getAttribute("size"));
		Element e = XmlUtil.get_child_element(elt, "identification");
		this.hashcode = Long.parseLong(e.getAttribute("hash"));
		Element e2 = XmlUtil.get_child_element(e, "header");
		if (e2 != null) {
			header = StringUtil.decodeHexa(XmlUtil.get_inner_text(e2));
		}
		List<Element> e_parts = XmlUtil.get_childs_element(e, "part");
		if (!e_parts.isEmpty()) {
			this.parts = new FilePart[e_parts.size()];
			for (int i = 0; i < parts.length; ++i) {
				parts[i] = new FilePart();
				parts[i].offset = Long.parseLong(e_parts.get(i).getAttribute("offset"));
				String s = XmlUtil.get_inner_text(e_parts.get(i));
				if (s != null && s.length() > 0)
					parts[i].data = StringUtil.decodeHexa(s);
			}
		}
	}
	
	@Override
	protected void saveDataSource(XmlWriter xml) {
		xml.addAttribute("size", size);
		saveFileDataSource(xml);
		xml.openTag("identification");
		xml.addAttribute("hash", hashcode);
		if (header != null) {
			xml.openTag("header");
			xml.addText(StringUtil.encodeHexa(header));
			xml.closeTag();
		}
		if (parts != null) {
			for (int i = 0; i < parts.length; ++i) {
				xml.openTag("part");
				xml.addAttribute("offset", parts[i].offset);
				if (parts[i].data != null)
					xml.addText(StringUtil.encodeHexa(parts[i].data));
				xml.closeTag();
			}
		}
		xml.closeTag(); // identification
	}
	protected abstract void saveFileDataSource(XmlWriter xml);
	
	@Override
	public boolean isSameInDifferentLocation(DataSource src) {
		if (isExactlyTheSame(src)) return false; // not in different location...
		if (!(src instanceof FileDataSource)) return false;
		FileDataSource ds = (FileDataSource)src;
		if (size != ds.size) return false;
		if (hashcode != ds.hashcode) return false;
		if (header == null) {
			if (ds.header != null) return false;
		} else {
			if (ds.header == null) return false;
			if (header.length != ds.header.length) return false;
			for (int i = 0; i < header.length; ++i)
				if (header[i] != ds.header[i]) return false;
		}
		if (parts == null) {
			if (ds.parts != null) return false;
		} else {
			if (ds.parts == null) return false;
			if (parts.length != ds.parts.length) return false;
			for (int i = 0; i < parts.length; ++i)
				if (parts[i].offset != ds.parts[i].offset) return false;
				else if (parts[i].data == null) {
					if (ds.parts[i].data != null) return false;
				}
				else if (parts[i].data.length != ds.parts[i].data.length) return false;
				else for (int j = 0; j < parts[i].data.length; ++j)
					if (parts[i].data[j] != ds.parts[i].data[j]) return false;
		}
		return true;
	}
	
	public boolean isSameContent(File file) {
		if (size != file.length()) return false;
		if (size == 0) return true;
		InputStream stream;
		try { stream = new FileInputStream(file); }
		catch (FileNotFoundException e) { return false; }
		try {
			byte[] buffer = new byte[header.length];
			int i = IOUtil.readAllBuffer(stream, buffer);
			if (i != header.length) return false;
			if (!ArrayUtil.equals(header, buffer)) return false;
			long pos = header.length;
			buffer = new byte[PART_SIZE];
			for (FilePart part : parts) {
				if (stream.skip(part.offset-pos) != part.offset-pos) return false;
				pos = part.offset;
				if (part.data == null) continue;
				if (IOUtil.readAllBuffer(stream, buffer, 0, part.data.length) != part.data.length) return false;
				if (!ArrayUtil.equals(part.data, 0, buffer, 0, part.data.length)) return false;
			}
			return true;
		} catch (IOException e) {
			return false;
		} finally {
			try { stream.close(); }
			catch (IOException e) {}
		}
	}
	
	private long hash(byte[] buffer) {
		long hash = 0;
		for (int i = 0; i < buffer.length; i+=Long.SIZE/8)
			hash += hash(buffer, i);
		return hash;
	}
	private long hash(byte[] buffer, int pos) {
		long hash = 0;
		int dec = 0;
		for (int i = 0; i < Long.SIZE/8; ++i, dec += 8)
			if (pos+i >= buffer.length) return hash;
			else hash += (buffer[pos+i]) << dec;
		return hash;
	}
	
	private void buildPartAndHash(byte[] header, InputStream stream, long size) {
		this.hashcode = hash(header);
		long pos = header.length;
		// ideallement, prends MAX_PARTS parts de PART_SIZE, a intervalles reguliers
		int nbPart = MAX_PARTS;
		long interval;
		while (((interval = size / (nbPart+1)) < header.length || interval < PART_SIZE*2) && nbPart > 0)
			nbPart--;
		this.parts = new FilePart[nbPart];
		if (nbPart == 0)
			return;
		for (int i = 0; i < nbPart; ++i) {
			try { 
				pos += stream.skip(interval*(i+1)-pos); 
				this.parts[i] = new FilePart();
				this.parts[i].offset = pos;
				byte[] buffer = new byte[PART_SIZE];
				int read = stream.read(buffer);
				if (read > 0) {
					if (read == PART_SIZE)
						this.parts[i].data = buffer;
					else {
						this.parts[i].data = new byte[read];
						System.arraycopy(buffer, 0, this.parts[i].data, 0, read);
					}
					this.hashcode += hash(this.parts[i].data);
				}
			} catch (IOException e) {
				FilePart[] result = new FilePart[i];
				for (int j = 0; j < i; ++j)
					result[j] = this.parts[j];
				this.parts = result;
				return;
			}
		}
	}
	
}
