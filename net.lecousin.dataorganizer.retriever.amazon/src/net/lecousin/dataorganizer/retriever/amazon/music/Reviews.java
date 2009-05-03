package net.lecousin.dataorganizer.retriever.amazon.music;

import java.util.List;

import net.lecousin.dataorganizer.audio.AudioSourceInfo;
import net.lecousin.dataorganizer.retriever.amazon.AmazonPage;
import net.lecousin.dataorganizer.retriever.amazon.Local;
import net.lecousin.dataorganizer.retriever.amazon.ProductReviews;
import net.lecousin.framework.Pair;
import net.lecousin.framework.Triple;
import net.lecousin.framework.progress.WorkProgress;

public class Reviews extends AmazonPage<AudioSourceInfo> {

	@Override
	protected String getPage() {
		return "product-reviews";
	}
	
	@Override
	protected String getDescription() {
		return Local.PublicReviews.toString();
	}
	
	@Override
	protected String firstPageToReload(String page, String pageURL) {
		return null;
	}
	@Override
	protected Pair<String,Boolean> parse(String page, String pageURL, AudioSourceInfo info, WorkProgress progress, int work) {
		int pageIndex = ProductReviews.getPageIndex(pageURL);
		progress.setSubDescription(getDescription()+" ("+Local.Page+" " + pageIndex+")");
		
		List<Triple<String,String,Integer>> reviews = ProductReviews.getReviews(page);
		for (Triple<String,String,Integer> t : reviews)
			info.setPublicReview(t.getValue1(), t.getValue2(), t.getValue3());
		
		progress.progress(work);
		return new Pair<String,Boolean>(reviews.isEmpty() ? null : ProductReviews.getNextPageURL(page, pageURL, pageIndex+1), true);
	}
}
