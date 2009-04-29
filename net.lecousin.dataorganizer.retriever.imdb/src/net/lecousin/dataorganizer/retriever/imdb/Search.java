package net.lecousin.dataorganizer.retriever.imdb;

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

	public static final String CAT_TITLE = "tt";
	public static final String CAT_NAME = "nm";
	
	public static List<SearchResult> search(String name, String cat, WorkProgress progress, int work) {
		List<SearchResult> results = new LinkedList<SearchResult>();
		String page = getSearchPage(name, cat);
		if (page.startsWith("*OK*"))
			results.add(new IMDBSearchResult(name, page.substring(4), null, name));
		results.addAll(getResults(page));
		progress.progress(work);
		return results;
	}
	private static String getSearchPage(String name, String cat) {
		HttpClient client = new HttpClient(SocketFactory.getDefault());
		HttpRequest req = new HttpRequest(IMDBUtil.getHost(), 80, "/find");
		req.addParameter("q", name);
		req.addParameter("s", cat);
		try {
			HttpResponse resp = client.send(req, true, null, 0);
			if (resp.getRequest() != req)
				return "*OK*"+resp.getRequest().getPath();
			String page = resp.getContent().getContent().getAsString();
			return page;
		} catch (Throwable t) {
			t.printStackTrace(System.err);
			return null;
		}
	}
	private static List<SearchResult> getResults(String page) {
		List<SearchResult> results = new LinkedList<SearchResult>();
		if (page == null) return results;
		Pair<String,Integer> p = HTMLAnalyzeUtil.getSection(page, "Displaying", "<script type=\"text/javascript\">", 0);
		if (p == null) return results;
		String section = p.getValue1();
		int i = 0;
		do {
			Pair<String,Integer> response = HTMLAnalyzeUtil.getSection(section, "<tr>", "</tr>", i);
			if (response == null) break;
			i = response.getValue2();
			String[] cols = HTMLAnalyzeUtil.getColumns(response.getValue1());
			if (cols.length != 3) continue;
			String imageURL = null;
			int j = cols[0].indexOf("<img ");
			if (j > 0) {
				Triple<Node,Boolean,Integer> t = XmlParsingUtil.parseOpenNode(cols[0], j);
				if (t.getValue1() != null) {
					String url = t.getValue1().attributes.get("src");
					if (!url.equals("/images/b.gif"))
						imageURL = url;
				}
			}
			
			j = cols[2].indexOf("<a href");
			if (j < 0) continue;
			Pair<Pair<String,String>,Integer> pl = HTMLAnalyzeUtil.getInfoLinked(cols[2], j);
			if (pl == null) continue;
			String url = pl.getValue1().getValue2();
			String name = pl.getValue1().getValue1();
			String info = cols[2].substring(pl.getValue2());

			results.add(new IMDBSearchResult(name, url, imageURL, info));
		} while (true);
		return results;
	}
	
	public static class IMDBSearchResult implements SearchResult {
		public IMDBSearchResult(String name, String url, String imageURL, String info) {
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
								File file = File.createTempFile("imdb", "poster");
								HttpRequest req = HttpRequest.fromURL(imageURL, IMDBUtil.getHost(), 80);
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
					str.append("<a href=\"http://"+IMDBUtil.getHost()).append(url).append("\">").append(name).append("</a><br/>");
					str.append(info);
					LCMLText text = new LCMLText(panel, false, true);
					text.setText(str.toString());
					text.setLayoutData(UIUtil.gridDataHoriz(1, true));
					text.addLinkListener(new Listener<String>() {
						public void fire(String href) {
							BrowserWindow browser = new BrowserWindow("DataOrganizer [IMDB]", null, true, true);
							HttpURI uri = new HttpURI(href);
							if (uri.getHost() == null) {
								uri.setHost(IMDBUtil.getHost());
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
