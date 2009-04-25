package net.lecousin.dataorganizer.retrieve.cinefil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.net.SocketFactory;

import net.lecousin.dataorganizer.core.database.info.InfoRetrieverPlugin.SearchResult;
import net.lecousin.framework.Pair;
import net.lecousin.framework.Triple;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.net.http.HttpURI;
import net.lecousin.framework.net.http.HttpUtil;
import net.lecousin.framework.net.http.client.HttpClient;
import net.lecousin.framework.net.http.client.HttpRequest;
import net.lecousin.framework.net.http.client.HttpResponse;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.strings.HTMLAnalyzeUtil;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.browser.BrowserWindow;
import net.lecousin.framework.ui.eclipse.control.ControlProvider;
import net.lecousin.framework.ui.eclipse.control.text.lcml.LCMLText;
import net.lecousin.framework.xml.XmlParsingUtil;
import net.lecousin.framework.xml.XmlParsingUtil.Node;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public class Search {

	public static final String CAT_FILM = "FILMS";
	public static final String CAT_STAR = "ARTISTES";
	
	public static List<SearchResult> search(String name, String cat, WorkProgress progress, int work) {
		List<SearchResult> results = new LinkedList<SearchResult>();
		int pageIndex = 1;
		String page;
		do {
			progress.setSubDescription(Local.Page+" " + pageIndex);
			page = getSearchPage(name, cat, pageIndex);
			results.addAll(getResults(page, cat));
		} while (hasPage(page, ++pageIndex));
		progress.progress(work);
		return results;
	}
	private static String getSearchPage(String name, String cat, int pageIndex) {
		HttpClient client = new HttpClient(SocketFactory.getDefault());
		HttpRequest req = new HttpRequest(CineFilUtil.getHost(), 80, "/findall");
		req.addParameter("KEYWORDS", name);
		req.addParameter("CAT", cat);
		req.addParameter("N", pageIndex);
		try {
			HttpResponse resp = client.send(req, true, null, 0);
			String page = resp.getContent().getContent().getAsString();
			return page;
		} catch (Throwable t) {
			t.printStackTrace(System.err);
			return null;
		}
	}
	private static List<SearchResult> getResults(String page, String cat) {
		if (cat.equals(CAT_FILM))
			return getFilmResults(page);
		if (cat.equals(CAT_STAR))
			return getStarResults(page);
		return new LinkedList<SearchResult>();
	}
	private static List<SearchResult> getFilmResults(String page) {
		List<SearchResult> results = new LinkedList<SearchResult>();
		if (page == null) return results;
		int i = 0;
		do {
			Pair<String,Integer> response = HTMLAnalyzeUtil.getSection(page, "<div class=\"reponse\"", "</div>", i);
			if (response == null) break;
			i = response.getValue2();
			String[] cols = HTMLAnalyzeUtil.getColumns(response.getValue1());
			if (cols.length == 0) continue;
			String imageURL = null;
			if (cols.length > 1) {
				int j = cols[0].indexOf("<img ");
				if (j > 0) {
					Triple<Node,Boolean,Integer> t = XmlParsingUtil.parseOpenNode(cols[0], j);
					if (t.getValue1() != null) {
						imageURL = t.getValue1().attributes.get("src");
					}
				}
			}
			Triple<Node,Boolean,Integer> t = XmlParsingUtil.parseOpenNode(cols[cols.length-1], 0);
			if (t.getValue1() == null || !t.getValue1().name.equalsIgnoreCase("a")) continue;
			String url = t.getValue1().attributes.get("href");
			Pair<String,Integer> section = HTMLAnalyzeUtil.getSection(cols[cols.length-1], "<h3>", "</h3>", 0);
			if (section == null) continue;
			String name = HTMLAnalyzeUtil.removeAllTags(section.getValue1());
			String info = HTMLAnalyzeUtil.cleanInfo(cols[cols.length-1].substring(section.getValue2()), true);
			results.add(new CineFilSearchResult(name, url, imageURL, info));
		} while (true);
		return results;
	}
	private static List<SearchResult> getStarResults(String page) {
		List<SearchResult> results = new LinkedList<SearchResult>();
		if (page == null) return results;
		int i = 0;
		do {
			Pair<String,Integer> response = HTMLAnalyzeUtil.getSection(page, "<div class=\"reponseArt\"", "</div>", i);
			if (response == null) break;
			i = response.getValue2();
			int j = response.getValue1().indexOf("<a ");
			if (j < 0) continue;
			Triple<Node,Boolean,Integer> t = XmlParsingUtil.parseOpenNode(response.getValue1(), j);
			if (t.getValue1() == null || !t.getValue1().name.equalsIgnoreCase("a")) continue;
			String url = t.getValue1().attributes.get("href");
			Pair<String,Integer> section = HTMLAnalyzeUtil.getSection(response.getValue1(), "<h3>", "</h3>", 0);
			if (section == null) continue;
			String name = HTMLAnalyzeUtil.removeAllTags(section.getValue1());
			results.add(new CineFilSearchResult(name, url, null, ""));
		} while (true);
		return results;
	}
	private static boolean hasPage(String page, int pageIndex) {
		if (page == null) return false;
		return page.indexOf("DESC&N=" + pageIndex) > 0;
	}
	
	public static class CineFilSearchResult implements SearchResult {
		public CineFilSearchResult(String name, String url, String imageURL, String info) {
			this.name = name;
			this.url = url;
			this.imageURL = imageURL;
			this.info = info;
		}
		public String name;
		public String url;
		public String imageURL;
		public String info;
		public Image image;
		public String getName() { return name; }
		public ControlProvider getDescriptionControlProvider() {
			return new ControlProvider() {
				public Control create(Composite parent) {
					Composite panel = new Composite(parent, SWT.NONE);
					if (imageURL != null) {
						if (image == null) {
							try {
								File file = File.createTempFile("cinefil", "poster");
								HttpRequest req = HttpRequest.fromURL(imageURL, CineFilUtil.getHost(), 80);
								if (HttpUtil.retrieveFile(req, file, true, null, 0))
									image = new Image(Display.getCurrent(), new FileInputStream(file));
								file.delete();
							} catch (IOException e) {
								if (Log.warning(this))
									Log.warning(this, "Unable to retrieve poster", e);
							} catch (Throwable t) {
								if (Log.warning(this))
									Log.warning(this, "Unable to retrieve poster", t);
							}
						}
					}
					UIUtil.gridLayout(panel, image != null ? 2 : 1);
					if (image != null)
						UIUtil.newImage(panel, image).setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
					StringBuilder str = new StringBuilder();
					str.append("<a href=\"http://"+CineFilUtil.getHost()).append(url).append("\">").append(name).append("</a><br/>");
					str.append(info);
					LCMLText text = new LCMLText(panel, false, true);
					text.setText(str.toString());
					text.setLayoutData(UIUtil.gridDataHoriz(1, true));
					text.addLinkListener(new Listener<String>() {
						public void fire(String href) {
							BrowserWindow browser = new BrowserWindow("DataOrganizer [CineFil]", null, true, true);
							HttpURI uri = new HttpURI(href);
							if (uri.getHost() == null) {
								uri.setHost(CineFilUtil.getHost());
								uri.setPort(80);
								uri.setProtocol("http");
							}
							browser.open();
							browser.setLocation(uri.toString());
						}
					});
					return panel;
				}
			};
		}
	}
	
}
