package net.lecousin.dataorganizer.retriever.amazon.movie;

import java.util.List;

import net.lecousin.dataorganizer.retriever.amazon.AmazonPage;
import net.lecousin.dataorganizer.retriever.amazon.Local;
import net.lecousin.dataorganizer.retriever.amazon.ProductImages;
import net.lecousin.dataorganizer.video.VideoSourceInfo;
import net.lecousin.framework.Pair;
import net.lecousin.framework.eclipse.resource.ResourceUtil;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.progress.WorkProgress;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageLoader;

public class Poster extends AmazonPage<VideoSourceInfo> {

	@Override
	protected String getPage() {
		return "gp/product/images";
	}
	
	@Override
	protected String getDescription() {
		return Local.Poster.toString();
	}
	
	@Override
	protected String firstPageToReload(String page, String pageURL) {
		return null;
	}
	@Override
	protected Pair<String,Boolean> parse(String page, String pageURL, VideoSourceInfo info, WorkProgress progress, int work) {
		List<Pair<ImageLoader,String>> images = ProductImages.retrieveImages(page);
		if (images.isEmpty()) {
			progress.progress(work);
			return new Pair<String,Boolean>(null, true);
		}
		
		IFolder folder;
		try {
			folder = info.getFolder();
			folder = folder.getFolder("amazon");
			ResourceUtil.createFolderAndParents(folder);
		} catch (CoreException e) {
			if (Log.error(this))
				Log.error(this, "Unable to create amazon folder", e);
			progress.progress(work);
			return new Pair<String,Boolean>(null, true);
		}
		for (Pair<ImageLoader,String> img : images) {
			if (info.hasPosterURL(img.getValue2())) continue;
			IFile file;
			int i = 0;
			while ((file = folder.getFile("poster"+i+".jpg")).exists()) i++;
			try { img.getValue1().save(file.getLocation().toFile().getAbsolutePath(), SWT.IMAGE_JPEG); }
			catch (Throwable t) {
				if (Log.warning(this))
					Log.warning(this, "Unable to save image", t);
			}
			
			info.addPoster(img.getValue2(), "amazon/"+file.getName());
		}
		
		progress.progress(work);
		return new Pair<String,Boolean>(null, true);
	}
}
