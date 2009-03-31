package net.lecousin.dataorganizer.core.database.version;

import net.lecousin.dataorganizer.core.database.info.SourceInfo;
import net.lecousin.dataorganizer.core.database.info.SourceInfo.Review;
import net.lecousin.framework.collections.SelfMap;
import net.lecousin.framework.collections.SelfMapLinkedList;
import net.lecousin.framework.xml.XmlUtil;
import net.lecousin.framework.xml.XmlWriter;

import org.w3c.dom.Element;

public abstract class ContentTypeLoader_0_1_0 implements ContentTypeLoader {

	public final Element getInfo(Element root) {
		return XmlUtil.get_child_element(root, "info");
	}

	protected SelfMap<String,Review> loadReviews(SourceInfo source, String tag, Element elt) {
		SelfMap<String,Review> reviews = new SelfMapLinkedList<String,Review>(5);
		for (Element e : XmlUtil.get_childs_element(elt, tag)) {
			String author = e.getAttribute("author");
			Integer note = Integer.parseInt(e.getAttribute("rate"));
			String review = XmlUtil.get_inner_text(e);
			reviews.put(source.new Review(author, review, note));
		}
		return reviews;
	}
	protected void saveReviews(SelfMap<String,Review> reviews, String tag, XmlWriter xml) {
		for (Review review : reviews) {
			xml.openTag(tag).addAttribute("author", review.getAuthor());
			if (review.getRate() != null)
				xml.addAttribute("rate", review.getRate());
			xml.addText(review.getReview());
			xml.closeTag();
		}
	}
	
}
