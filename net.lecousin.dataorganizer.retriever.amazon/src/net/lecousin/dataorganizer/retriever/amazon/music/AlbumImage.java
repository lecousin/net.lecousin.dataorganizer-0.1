package net.lecousin.dataorganizer.retriever.amazon.music;

import java.util.List;

import net.lecousin.dataorganizer.audio.AudioDataType;
import net.lecousin.dataorganizer.audio.AudioSourceInfo;
import net.lecousin.dataorganizer.audio.detect.DecidePicturesDialog;
import net.lecousin.dataorganizer.audio.detect.DecidePicturesDialog.Provider;
import net.lecousin.dataorganizer.retriever.amazon.AmazonPage;
import net.lecousin.dataorganizer.retriever.amazon.Local;
import net.lecousin.dataorganizer.retriever.amazon.ProductImages;
import net.lecousin.framework.Pair;
import net.lecousin.framework.progress.WorkProgress;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

public class AlbumImage extends AmazonPage<AudioSourceInfo> {

	@Override
	protected String getPage() {
		return "gp/product/images";
	}
	
	@Override
	protected String getDescription() {
		return Local.AlbumImage.toString();
	}
	
	@Override
	protected String firstPageToReload(String page, String pageURL) {
		return null;
	}
	@Override
	protected Pair<String,Boolean> parse(String page, String pageURL, AudioSourceInfo info, WorkProgress progress, int work) {
		List<Pair<ImageLoader,String>> images = ProductImages.retrieveImages(page);
		progress.progress(work);
		if (images.isEmpty())
			return new Pair<String,Boolean>(null, true);

		AudioDataType data = (AudioDataType)info.getParent().getDataContent();
		
		DecidePicturesDialog<Pair<ImageLoader,String>> dlg = new DecidePicturesDialog<Pair<ImageLoader,String>>(null, Local.process(Local.MESSAGE_Choose_Pictures, data.getData().getName()), images, new ImageProvider(images));
		dlg.open();
		for (Pair<ImageLoader,String> img : dlg.getCoverFront())
			data.saveCoverFront(img.getValue1().data);
		for (Pair<ImageLoader,String> img : dlg.getCoverBack())
			data.saveCoverBack(img.getValue1().data);
		for (Pair<ImageLoader,String> img : dlg.getOthers())
			data.saveImage(img.getValue1().data);
		
		return new Pair<String,Boolean>(null, true);
	}

	private static class ImageProvider implements Provider<Pair<ImageLoader,String>> {
		public ImageProvider(List<Pair<ImageLoader,String>> images) { this.images = images; }
		List<Pair<ImageLoader,String>> images;
		public Image getImage(Pair<ImageLoader,String> element) {
			return new Image(Display.getDefault(), element.getValue1().data[0]);
		}
		public String getText(Pair<ImageLoader,String> element) {
			int i = images.indexOf(element);
			if (i < 0) return Local.Image.toString();
			return Local.Image+" "+(i+1);
		}
	}
}
