package net.lecousin.dataorganizer.core.database.info;

import net.lecousin.framework.collections.SelfMap;
import net.lecousin.framework.xml.XmlWriter;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;

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

	public abstract SelfMap<String,Review> getReviews(String type);
	
	public abstract void merge(SourceInfo info);
	
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
	protected void merge(SelfMap<String,Review> currentReviews, SelfMap<String,Review> newReviews) {
		boolean changed = false;
		for (Review newReview : newReviews) {
			if (!currentReviews.containsKey(newReview.getAuthor())) {
				currentReviews.put(newReview);
				changed = true;
			}
		}
		if (changed) signalModification();
	}
	
}
