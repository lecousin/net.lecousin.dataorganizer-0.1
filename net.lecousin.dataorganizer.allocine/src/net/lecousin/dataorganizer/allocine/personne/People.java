package net.lecousin.dataorganizer.allocine.personne;

import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import net.lecousin.dataorganizer.allocine.AlloCinePage;
import net.lecousin.dataorganizer.people.PeopleSourceInfo;
import net.lecousin.framework.Pair;
import net.lecousin.framework.eclipse.resource.ResourceUtil;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.net.http.HttpUtil;
import net.lecousin.framework.net.http.client.HttpRequest;
import net.lecousin.framework.progress.WorkProgress;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;

public class People extends AlloCinePage<PeopleSourceInfo> {

	@Override
	protected String getDescription() { return "People information"; }
	@Override
	protected String getCategory() { return "personne"; }
	@Override
	protected String getPage() { return "fichepersonne"; }

	@Override
	protected String firstPageToReload(String page, String url) {
		return null;
	}
	@Override
	protected Pair<String,Boolean> parse(String page, String pageURL, PeopleSourceInfo info, WorkProgress progress, int work) {
		int i = page.indexOf("Posters<br />et T-shirts");
		if (i < 0) { progress.progress(work); return new Pair<String,Boolean>(null, false); }
		i = page.indexOf("style=\"padding:10 0 0 0\"", i);
		if (i < 0) { progress.progress(work); return new Pair<String,Boolean>(null, false); }
		int j = page.indexOf("</table>", i);
		if (j < 0) { progress.progress(work); return new Pair<String,Boolean>(null, false); }
		String section = page.substring(i, j);
		
		Pair<String,Integer> part = getSection(section, "<td ", "</td>", 0);
		if (part == null) { progress.progress(work); return new Pair<String,Boolean>(null, false); }
		if (part.getValue1().indexOf("<div") < 0) {
			Pair<String,Integer> photo = getSection(part.getValue1(), "<img src=\"", "\"", 0);
			if (photo != null) {
				String url = photo.getValue1();
				if (!info.hasPhotoURL(url)) {
					try {
						IFolder f = info.getFolder();
						f = f.getFolder("allocine");
						i = 0;
						IFile file;
						while ((file = f.getFile("photo"+i+".jpg")).exists()) i++;
						ResourceUtil.createFolderAndParents(f);
						file.create(new ByteArrayInputStream(new byte[0]), true, null);
						HttpRequest req = HttpRequest.fromURL(url, "www.allocine.fr", 80);
						if (HttpUtil.retrieveFile(req, file.getLocation().toFile(), true, null, 0)) {
							info.addPhoto(url, "allocine/"+file.getName());
						} else {
							if (Log.warning(this))
								Log.warning(this, "Unable to retrieve photo URL '" + url + "'.");
							try { file.delete(true, null); }
							catch (CoreException e) {
								if (Log.warning(this))
									Log.warning(this, "After we failed to retrieve the photo, we cannot delete the file.", e);
							}
						}
					} catch (CoreException e) {
						if (Log.warning(this))
							Log.warning(this, "Unable to create file to store the photo", e);
					}
				}
			}
			part = getSection(section, "<td ", "</td>", part.getValue2());
		}

		i = section.indexOf("Né le ");
		if (i < 0) { i = section.indexOf("Née le "); if (i > 0) i += 7; } else i += 6;
		if (i > 0) {
			j = section.indexOf("</h4>", i);
			int k = section.indexOf(" à ", i);
			if (k < 0) k = j;
			try { info.setBirthDay(new SimpleDateFormat("dd MMMM yyy").parse(section.substring(i,k)).getTime()); }
			catch (ParseException e) {}
			if (k != j)
				info.setBirthPlace(section.substring(k+3, j)); 
		}
		progress.progress(work);
		return new Pair<String,Boolean>(null, true);
	}
	
}
