package net.lecousin.dataorganizer.core.database.info;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.framework.collections.SelfMap;
import net.lecousin.framework.eclipse.resource.ResourceUtil;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.xml.XmlWriter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

public abstract class SourceInfo {

	public SourceInfo(Info parent) {
		this.parent = parent;
	}
	
	final void save(XmlWriter xml) {
		saveInfo(xml);
	}
	protected abstract void saveInfo(XmlWriter xml);
	
	private Info parent;
	
	public Info getParent() { return parent; }
	
	void setParent(Info newParent) {
		try { copyLocalFiles(parent.getFolder(), newParent.getFolder()); }
		catch (CoreException e) {
			if (Log.error(this))
				Log.error(this, "Unable to copy info files from a data to antoher", e);
		}
		parent = newParent;
	}
	protected abstract void copyLocalFiles(IFolder src, IFolder dst);

	public abstract SelfMap<String,Review> getReviews(String type);
	
	protected void signalModification() {
		if (parent != null)
			parent.signalModification();
	}
	public IFolder getFolder() throws CoreException { return parent != null ? parent.getFolder() : null; }
	
	public class Review implements SelfMap.Entry<String> {
		public Review(String author, String review, Integer rate)
		{ this.author = author; this.review = review; this.rate = rate; }
		private String author;
		private String review;
		private Integer rate;
		public String getAuthor() { return author; }
		public String getReview() { return review; }
		public Integer getRate() { return rate; }
		public String getHashObject() { return author; }
		public void setReview(String text) {
			if (text == null || text.length() == 0) return;
			if (text.equals(review)) return;
			review = text;
			signalModification();
		}
		public void setRate(Integer value) {
			if (value == null || value.equals(rate)) return;
			rate = value;
			signalModification();
		}
	}
	
	
	protected void saveCritiks(SelfMap<String, Review> reviews, String tag, XmlWriter xml) {
		for (Review review : reviews) {
			xml.openTag(tag).addAttribute("author", review.getAuthor());
			if (review.getRate() != null)
				xml.addAttribute("rate", review.getRate());
			xml.addText(review.getReview());
			xml.closeTag();
		}
	}
	
	protected void setReview(SelfMap<String,Review> reviews, String author, String review, Integer note) {
		boolean changed = false;
		Review authorReview = reviews.get(author);
		if (authorReview == null) {
			authorReview = new Review(author, review, note);
			reviews.put(authorReview);
			changed = true;
		} else {
			if (!authorReview.getReview().equals(review) && review.length() > 0)
				authorReview.setReview(review);
			if (note != null && (authorReview.getRate() == null || !authorReview.getRate().equals(note)))
				authorReview.setRate(note);
		}
		if (changed)
			signalModification();
	}
	
	protected List<String> copyImageFiles(IFolder src, IFolder dst, Iterable<String> paths) {
		List<String> toRemove = new LinkedList<String>();
		for (String p : paths) {
			Path path = new Path(p);
			IFile file = src.getFile(path);
			if (!file.exists()) {
				toRemove.add(p);
				continue;
			}
			IFile file2 = dst.getFile(path);
			if (file2.exists()) {
				toRemove.add(p);
				continue;
			}
			try {
				ResourceUtil.createNecessaryParentFolders(file2);
				InputStream in = file.getContents();
				file2.create(in, true, null);
				in.close();
			} catch (Throwable ex) {
				if (Log.error(this))
					Log.error(this, "Unable to copy image file", ex);
			}
		}
		return toRemove;
	}
	
	public abstract void merge(SourceInfo other);
}
