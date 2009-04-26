package net.lecousin.dataorganizer.retriever.cinefil.star;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;

import net.lecousin.dataorganizer.people.PeopleSourceInfo;
import net.lecousin.dataorganizer.retriever.cinefil.CineFilPage;
import net.lecousin.dataorganizer.retriever.cinefil.CineFilUtil;
import net.lecousin.dataorganizer.retriever.cinefil.Local;
import net.lecousin.framework.Pair;
import net.lecousin.framework.Triple;
import net.lecousin.framework.eclipse.resource.ResourceUtil;
import net.lecousin.framework.io.FileSystemUtil;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.xml.XmlParsingUtil;
import net.lecousin.framework.xml.XmlParsingUtil.Node;

public class People extends CineFilPage<PeopleSourceInfo> {

	@Override
	protected String getDescription() { return Local.People_information.toString(); }
	@Override
	protected String getCategory() { return "star"; }
	@Override
	protected String getPage() { return ""; }

	@Override
	protected String firstPageToReload(String page, String url) {
		return null;
	}
	
	@Override
	protected Pair<String,Boolean> parse(String page, String pageURL, PeopleSourceInfo info, WorkProgress progress, int work) {
		// birth date
		int i = page.indexOf("Né le ");
		if (i < 0)
			i = page.indexOf("Née le ");
		if (i > 0) {
			i = page.indexOf(" : ", i);
			if (i > 0) {
				int j = page.indexOf("<br>", i);
				if (j > 0) {
					try { info.setBirthDay(new SimpleDateFormat("dd/MM/yyyy").parse(page.substring(i,j)).getTime()); }
					catch (ParseException e) {}
				}
			}
		}

		// photo
		i = page.indexOf("<a href=\""+pageURL+"\"><img src=");
		if (i > 0) {
			i = i+9+pageURL.length()+2;
			Triple<Node,Boolean,Integer> t = XmlParsingUtil.parseOpenNode(page, i);
			if (t.getValue1() != null && t.getValue1().name.equalsIgnoreCase("img")) {
				String url = t.getValue1().attributes.get("src");
				if (url.indexOf("://") < 0) url = "http://"+CineFilUtil.getHost()+url;
				if (info.hasPhotoURL(url)) return null;
				try { 
					IFileStore netFile = EFS.getStore(new URI(url));
					IFile file;
					IFolder f = info.getFolder();
					f = f.getFolder("cinefil");
					i = 0;
					while ((file = f.getFile("poster"+i+"."+FileSystemUtil.getFileNameExtension(netFile.getName()))).exists()) i++;
					ResourceUtil.createFolderAndParents(f);
					InputStream in = netFile.openInputStream(EFS.NONE, null);
					file.create(in, true, null);
					info.addPhoto(url, "cinefil/"+file.getName());
				} catch (URISyntaxException e) {
				} catch (CoreException e) {
				}
			}
		}
		
		progress.progress(work);
		return new Pair<String,Boolean>(null, true);
	}
	
}
