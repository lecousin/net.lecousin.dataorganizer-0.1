package net.lecousin.dataorganizer.retriever.imdb.name;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import net.lecousin.dataorganizer.core.database.info.Info.DataLink;
import net.lecousin.dataorganizer.people.PeopleSourceInfo;
import net.lecousin.dataorganizer.retriever.imdb.IMDBPage;
import net.lecousin.dataorganizer.retriever.imdb.IMDBUtil;
import net.lecousin.dataorganizer.retriever.imdb.Local;
import net.lecousin.dataorganizer.video.VideoContentType;
import net.lecousin.framework.Pair;
import net.lecousin.framework.Triple;
import net.lecousin.framework.eclipse.resource.ResourceUtil;
import net.lecousin.framework.io.FileSystemUtil;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.xml.XmlParsingUtil;
import net.lecousin.framework.xml.XmlUtil;
import net.lecousin.framework.xml.XmlParsingUtil.Node;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;

public class People extends IMDBPage<PeopleSourceInfo> {

	@Override
	protected String getDescription() { return Local.People_information.toString(); }
	@Override
	protected String getCategory() { return "name"; }
	@Override
	protected String getPage() { return ""; }

	@Override
	protected String firstPageToReload(String page, String url) {
		return null;
	}
	
	@Override
	protected Pair<String,Boolean> parse(String page, String pageURL, PeopleSourceInfo info, WorkProgress progress, int work) {
		getBirthInfo(page, info);
		getActivities(page, info);
		getPhoto(page, info);
		
		progress.progress(work);
		return new Pair<String,Boolean>(null, true);
	}
	
	private void getBirthInfo(String page, PeopleSourceInfo info) {
		List<Pair<String,String>> list = getInfoLinkedList(page, "Date of Birth:", "</div>");
		if (list == null) return;
		String day = null;
		String year = null;
		String place = null;
		for (Pair<String,String> p : list) {
			if (p.getValue2().startsWith("/OnThisDay"))
				day = p.getValue1();
			else if (p.getValue2().startsWith("/BornInYear"))
				year = p.getValue1();
			else if (p.getValue2().startsWith("/BornWhere"))
				place = p.getValue1();
		}
		if (day != null && year != null) {
			try { info.setBirthDay(new SimpleDateFormat("dd MMMM yyyy").parse(day+" "+year).getTime()); }
			catch (ParseException e) {}
		}
		if (place != null)
			info.setBirthPlace(place);
	}
	
	private void getActivities(String page, PeopleSourceInfo info) {
		int i = 0;
		do {
			Pair<String,Integer> p = getSection(page, "<div class=\"filmo\">", "</div>", i);
			if (p == null) break;
			i = p.getValue2();
			
			int j = p.getValue1().indexOf("<a name=\"");
			if (j < 0) continue;
			int k = p.getValue1().indexOf("\"", j+9);
			if (k < 0) continue;
			String activity = p.getValue1().substring(j+9, k);
			
			p = getSection(p.getValue1(), "<ol>", "</ol>", k);
			if (p == null) continue;
			
			j = 0;
			do {
				Pair<String,Integer> p2 = getSection(p.getValue1(), "<li>", "</li>", j);
				if (p2 == null) break;
				j = p2.getValue2();
				Pair<Pair<String,String>,Integer> pl = getInfoLinked(p2.getValue1(), 0);
				if (pl == null) continue;
				String name = XmlUtil.decodeXML(pl.getValue1().getValue1());
				String url = pl.getValue1().getValue2();
				String id = IMDBUtil.getIDFromURL(url);
				if (id == null) continue;
				info.addActivity(activity, new DataLink(VideoContentType.VIDEO_TYPE, IMDBUtil.SOURCE_ID, id, name));
			} while (true);
		} while (true);
	}

	private void getPhoto(String page, PeopleSourceInfo info) {
		int i = page.indexOf("<a name=\"headshot\" ");
		if (i < 0) return;
		Triple<Node,Boolean,Integer> t = XmlParsingUtil.parseOpenNode(page, i);
		if (t.getValue1() == null) return;
		String url = t.getValue1().attributes.get("href");
		
		String page2 = loadPage(url);
		if (page2 == null) return;
		i = page2.indexOf("<center><table id=\"principal\">");
		if (i < 0) return;
		i = page2.indexOf("<img ", i);
		if (i < 0) return;
		
		t = XmlParsingUtil.parseOpenNode(page2, i);
		if (t.getValue1() == null) return;
		url = t.getValue1().attributes.get("src");
		if (url.indexOf("://") < 0) url = "http://"+IMDBUtil.getHost()+url;
		if (info.hasPhotoURL(url)) return;
		try { 
			IFileStore netFile = EFS.getStore(new URI(url));
			IFile file;
			IFolder f = info.getFolder();
			f = f.getFolder("imdb");
			i = 0;
			while ((file = f.getFile("photo"+i+"."+FileSystemUtil.getFileNameExtension(netFile.getName()))).exists()) i++;
			ResourceUtil.createFolderAndParents(f);
			InputStream in = netFile.openInputStream(EFS.NONE, null);
			file.create(in, true, null);
			info.addPhoto(url, "imdb/"+file.getName());
		} catch (URISyntaxException e) {
			return;
		} catch (CoreException e) {
			return;
		}
	}
}
