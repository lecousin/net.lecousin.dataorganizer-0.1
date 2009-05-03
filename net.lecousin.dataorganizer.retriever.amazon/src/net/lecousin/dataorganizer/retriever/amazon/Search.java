package net.lecousin.dataorganizer.retriever.amazon;

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
import net.lecousin.framework.xml.XmlUtil;
import net.lecousin.framework.xml.XmlParsingUtil.Node;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public class Search {

	public enum SearchType {
		DVD,
		MUSIC
	}

	public static List<SearchResult> search(String name, SearchType type, WorkProgress progress, int work) {
		List<SearchResult> results = new LinkedList<SearchResult>();
		int pageIndex = 1;
		String page;
		do {
			progress.setSubDescription(Local.Page+" " + pageIndex);
			page = getSearchPage(name, pageIndex, type);
			results.addAll(getResults(page));
		} while (hasPage(page, ++pageIndex));
		progress.progress(work);
		return results;
	}
	private static String getSearchPage(String name, int pageIndex, SearchType type) {
		HttpClient client = new HttpClient(SocketFactory.getDefault());
		String url;
		switch (type) {
		case DVD: url = "/s/ref=nb_ss_d"; break;
		case MUSIC: url = "/s/ref=nb_ss_m"; break;
		default: return null;
		}
		HttpRequest req = new HttpRequest(AmazonUtil.getHost(), 80, url);
		switch (type) {
		case DVD: url = "dvd"; break;
		case MUSIC: url = "popular"; break;
		}
		req.addParameter("url", "search-alias="+url);
		req.addParameter("field-keywords", name);
		req.addParameter("page", pageIndex);
		req.setHeader("User-Agent", "User-Agent: Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.0.10) Gecko/2009042316 Firefox/3.0.10");
		try {
			HttpResponse resp = client.send(req, true, null, 0);
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
		
		int resultID = 0;
		int pos = 0;
		do {
			int i = page.indexOf("id=\"result_"+resultID+"\"", pos);
			if (i < 0) break;
			
			Pair<List<Pair<Node,String>>,Integer> nodes = HTMLAnalyzeUtil.getAllSubNodes(page, i, "div");
			if (nodes.getValue2() == pos)
				pos = i+1;
			else
				pos = nodes.getValue2();
			resultID++;
			
			Pair<Node,String> productImage = null;
			Pair<Node,String> productTitle = null;
			Pair<Node,String> stars = null;
			for (Pair<Node,String> p : nodes.getValue1()) {
				String c = p.getValue1().attributes.get("class");
				if (c == null) continue;
				if (c.equals("productImage")) productImage = p;
				else if (c.equals("productTitle")) productTitle = p;
				else if (c.equals("stars")) stars = p;
			}
			if (productTitle == null) continue;
			String imageURL = null;
			if (productImage != null) {
				i = productImage.getValue2().indexOf("<img");
				if (i >= 0) {
					Triple<Node,Boolean,Integer> t = XmlParsingUtil.parseOpenNode(productImage.getValue2(), i);
					imageURL = t.getValue1().attributes.get("src");
				}
			}
			Pair<Pair<String,String>,Integer> pl = HTMLAnalyzeUtil.getInfoLinked(productTitle.getValue2(), 0);
			if (pl == null) continue;
			String name = XmlUtil.decodeXML(HTMLAnalyzeUtil.cleanInfo(pl.getValue1().getValue1(), false));
			String url = pl.getValue1().getValue2();
			String id = AmazonUtil.getIDFromDPURL(url);
			if (id == null) continue;
			String text = productTitle.getValue2().substring(pl.getValue2());
			text = HTMLAnalyzeUtil.cleanInfo(text, true);
			
			if (stars != null) {
				i = stars.getValue2().indexOf("(<a ");
				if (i > 0) {
					int j = stars.getValue2().indexOf("</a>", i);
					if (j > 0) {
						int k = stars.getValue2().lastIndexOf('>', j-1);
						if (k > 0) {
							text += "<br/>"+Local.PublicReviews+": "+stars.getValue2().substring(k+1,j);
						}
					}
				}
			}
			
			results.add(new AmazonSearchResult(name, id, imageURL, text));
		} while (true);
		return results;
	}
	private static boolean hasPage(String page, int pageIndex) {
		if (page == null) return false;
		return page.indexOf("&page=" + pageIndex) > 0;
	}
	
	public static class AmazonSearchResult implements SearchResult {
		public AmazonSearchResult(String title, String id, String imageURL, String info) {
			this.title = title;
			this.id = id;
			this.imageURL = imageURL;
			this.info = info;
		}
		public String id;
		public String imageURL;
		public String title;
		public String info;
		public Image image;
		public String getName() {
			return title;
		}
		public ControlProvider getDescriptionControlProvider() {
			return new ControlProvider() {
				public Control create(Composite parent) {
					Composite panel = new Composite(parent, SWT.NONE);
					if (imageURL != null) {
						if (image == null) {
							try {
								File file = File.createTempFile("amazon", "poster");
								HttpRequest req = HttpRequest.fromURL(imageURL, AmazonUtil.getHost(), 80);
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
					str.append("<a href=\"http://"+AmazonUtil.getHost()).append(AmazonUtil.getPageURLForID(id, "dp")).append("\">").append(title).append("</a><br/>");
					str.append(info);
					LCMLText text = new LCMLText(panel, false, true);
					text.setText(str.toString());
					text.setLayoutData(UIUtil.gridDataHoriz(1, true));
					text.addLinkListener(new Listener<String>() {
						public void fire(String href) {
							BrowserWindow browser = new BrowserWindow("DataOrganizer [Amazon]", null, true, true);
							HttpURI uri = new HttpURI(href);
							if (uri.getHost() == null) {
								uri.setHost(AmazonUtil.getHost());
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
