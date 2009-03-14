package net.lecousin.dataorganizer.allocine.film;

import java.io.ByteArrayInputStream;

import net.lecousin.dataorganizer.allocine.AlloCinePage;
import net.lecousin.dataorganizer.video.VideoInfo;
import net.lecousin.framework.Pair;
import net.lecousin.framework.eclipse.resource.ResourceUtil;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.net.http.HttpUtil;
import net.lecousin.framework.net.http.client.HttpRequest;
import net.lecousin.framework.progress.WorkProgress;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;

public class Poster extends AlloCinePage<VideoInfo> {

	@Override
	protected String getCategory() {
		return "film";
	}
	@Override
	protected String getPage() {
		return "galerievignette";
	}
	
	@Override
	protected String getDescription() {
		return "Poster";
	}
	
	@Override
	protected String firstPageToReload(String page, String pageURL) {
		return null;
	}
	@Override
	protected Pair<String,Boolean> parse(String page, String pageURL, VideoInfo info, WorkProgress progress, int work) {
		int i = page.indexOf("id='divPhotoNormal'");
		if (i < 0) { progress.progress(work); return new Pair<String,Boolean>(null, false); }
		i = page.indexOf("<img id='imgNormal'", i);
		if (i < 0) { progress.progress(work); return new Pair<String,Boolean>(null, false); }
		int j = page.indexOf("/>", i);
		if (j < 0) { progress.progress(work); return new Pair<String,Boolean>(null, false); }
		String s = page.substring(i, j);
		i = s.indexOf("src=\'");
		if (i < 0) { progress.progress(work); return new Pair<String,Boolean>(null, false); }
		j = s.indexOf('\'', i + 5);
		if (j < 0) { progress.progress(work); return new Pair<String,Boolean>(null, false); }
		String url = s.substring(i,j);
		if (info.hasPosterURL(url)) { progress.progress(work); return new Pair<String,Boolean>(null, true); }
		IFile file;
		try {
			IFolder f = info.getFolder();
			f = f.getFolder("allocine");
			i = 0;
			while ((file = f.getFile("poster"+i+".jpg")).exists()) i++;
			ResourceUtil.createFolderAndParents(f);
			file.create(new ByteArrayInputStream(new byte[0]), true, null);
		} catch (CoreException e) {
			if (Log.warning(this))
				Log.warning(this, "Unable to create file to store the poster", e);
			progress.progress(work);
			return new Pair<String,Boolean>(null, false);
		}
		HttpRequest req = HttpRequest.fromURL(url, "www.allocine.fr", 80);
		if (HttpUtil.retrieveFile(req, file.getLocation().toFile(), true, progress, work)) {
			info.addPoster(url, "allocine/"+file.getName());
		} else {
			progress.progress(work);
			if (Log.warning(this))
				Log.warning(this, "Unable to retrieve poster URL '" + url + "'.");
			try { file.delete(true, null); }
			catch (CoreException e) {
				if (Log.warning(this))
					Log.warning(this, "After we failed to retrieve the poster, we cannot delete the file.", e);
			}
		}
		return new Pair<String,Boolean>(null, true);
	}
	
}
