package net.lecousin.dataorganizer.retriever.amazon;

import java.io.InputStream;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.framework.Pair;
import net.lecousin.framework.Triple;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.xml.XmlParsingUtil;
import net.lecousin.framework.xml.XmlParsingUtil.Node;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.swt.graphics.ImageLoader;

public class ProductImages {

	public static List<Pair<ImageLoader,String>> retrieveImages(String page) {
		int i = 0;
		List<String> urls = new LinkedList<String>();
		do {
			int j = page.indexOf("fetchImage(\"alt_image_", i);
			if (j < 0) break;
			i = j+1;
			
			j = page.indexOf("\", \"", j);
			if (j < 0) continue;
			j += 4;
			int k = page.indexOf('\"', j);
			if (k < 0) continue;
			String url = page.substring(j, k);
			i = k+1;
			urls.add(url);
		} while (true);
		if (urls.isEmpty()) {
			// we may have a single image, so no alternative images list
			i = page.indexOf("<div id=\"imageViewerDiv\">");
			if (i > 0) {
				int j = page.indexOf("<img", i);
				if (j > 0) {
					Triple<Node,Boolean,Integer> t = XmlParsingUtil.parseOpenNode(page, j);
					String url = t.getValue1().attributes.get("src");
					if (url != null)
						urls.add(url);
				}
			}
		}
		
		List<Pair<ImageLoader,String>> images = new LinkedList<Pair<ImageLoader,String>>();
		for (String url : urls) {
			try {
				IFileStore store = EFS.getStore(new URI(url));
				InputStream stream = store.openInputStream(EFS.NONE, null);
				ImageLoader loader = new ImageLoader();
				loader.load(stream);
				stream.close();
				images.add(new Pair<ImageLoader,String>(loader, url));
			} catch (Throwable t) {
				if (Log.warning(ProductImages.class))
					Log.warning(ProductImages.class, "Unable to retrieve image URL '" + url + "'.", t);
			}
		}
		return images;
	}
	
}
