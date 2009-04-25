package net.lecousin.dataorganizer.retriever.allocine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.net.SocketFactory;

import net.lecousin.dataorganizer.core.database.info.InfoRetrieverPlugin.SearchResult;
import net.lecousin.framework.application.Application;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.net.http.HttpURI;
import net.lecousin.framework.net.http.HttpUtil;
import net.lecousin.framework.net.http.client.HttpClient;
import net.lecousin.framework.net.http.client.HttpRequest;
import net.lecousin.framework.net.http.client.HttpResponse;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.browser.BrowserWindow;
import net.lecousin.framework.ui.eclipse.control.ControlProvider;
import net.lecousin.framework.ui.eclipse.control.text.lcml.LCMLText;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public class Search {
	
	public static class AlloCineSearchResult implements SearchResult {
		public AlloCineSearchResult() {
		}
		public String link;
		public String imageURL;
		public String title;
		public List<String> information = new LinkedList<String>();
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
								File file = File.createTempFile("allocine", "poster");
								HttpRequest req = HttpRequest.fromURL(imageURL, AlloCineUtil.getHost(), 80);
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
					str.append("<a href=\"http://"+AlloCineUtil.getHost()).append(link).append("\">").append(title).append("</a><br/>");
					for (String info : information)
						str.append(info).append("<br/>");
					LCMLText text = new LCMLText(panel, false, true);
					text.setText(str.toString());
					text.setLayoutData(UIUtil.gridDataHoriz(1, true));
					text.addLinkListener(new Listener<String>() {
						public void fire(String href) {
							BrowserWindow browser = new BrowserWindow("DataOrganizer [AlloCine]", null, true, true);
							HttpURI uri = new HttpURI(href);
							if (uri.getHost() == null) {
								uri.setHost(AlloCineUtil.getHost());
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

	public static final int RUB_FILMS = 1;
	public static final int RUB_STARS = 2;
	public static List<SearchResult> search(String name, int rub, WorkProgress progress, int work) {
		List<SearchResult> results = new LinkedList<SearchResult>();
		int pageIndex = 1;
		String page;
		do {
			progress.setSubDescription(Local.Page+" " + pageIndex);
			page = getSearchPage(name, pageIndex, rub);
			results.addAll(getResults(page, rub));
		} while (hasPage(page, rub, ++pageIndex));
		progress.progress(work);
		return results;
	}
	private static String getSearchPage(String name, int pageIndex, int rub) {
		HttpClient client = new HttpClient(SocketFactory.getDefault());
		HttpRequest req = new HttpRequest(AlloCineUtil.getHost(), 80, "/recherche/");
		req.addParameter("motcle", name);
		req.addParameter("rub", rub);
		req.addParameter("page", pageIndex);
		try {
			HttpResponse resp = client.send(req, true, null, 0);
			String page = resp.getContent().getContent().getAsString();
			return page;
		} catch (Throwable t) {
			t.printStackTrace(System.err);
			return null;
		}
	}
	public enum STR {
		NoResult("Sorry, no result", "on n'a pas trouvé"),
		Start("You have searched for: <b>","Recherche : <b>"),
		;
		private STR(String english, String french) {
			this.english = english;
			this.french = french;
		}
		private String english;
		private String french;
		@Override
		public java.lang.String toString() {
			switch (Application.language) {
			case FRENCH: return french;
			default: return english;
			}
		}
	}
	
	private static List<SearchResult> getResults(String page, int rub) {
		List<SearchResult> results = new LinkedList<SearchResult>();
		if (page == null) return results;

		if (page.indexOf(STR.NoResult.toString()) > 0) return results;
		int i = page.indexOf(STR.Start.toString());
		if (i < 0) return results;
		i = page.indexOf("SpBlocTitle", i);
		if (i < 0) return results;
//		i = page.indexOf("</h3>", i+5);
//		if (i < 0) return results;
		
		int endResults = page.indexOf("&rub=" + rub + "&page=");
		if (endResults < 0) {
			endResults = page.indexOf("<script", i);
			if (endResults < 0) return results;
		}
		
		page = page.substring(i, endResults);
		
		int pos = 0;
		do {
			AlloCineSearchResult result = new AlloCineSearchResult();
			
			// link
			i = page.indexOf("<a ", pos);
			if (i < 0) break;
			int hrefStart = page.indexOf("href=\"", i);
			if (hrefStart < 0) break;
			int hrefEnd = page.indexOf('\"', hrefStart+6);
			if (hrefEnd < 0) break;
			result.link = page.substring(hrefStart+6, hrefEnd);

			// imageURL
			int j = page.indexOf("</a>", i);
			if (j < 0) break;
			String s = page.substring(i, j);
			int endImg = s.indexOf(".jpg\"");
			result.imageURL = null;
			if (endImg > 0) {
				int startImg = s.lastIndexOf('\"', endImg);
				if (startImg > 0)
					result.imageURL = s.substring(startImg+1, endImg+4);
			}
			
			// title
			i = page.indexOf("<a href=\"" + result.link, j);
			if (i < 0) break;
			i = page.indexOf("><b>", i);
			if (i < 0) break;
			j = page.indexOf("</a>", i);
			if (j < 0) break;
			result.title = page.substring(i+4, j).replace("<b>", "").replace("</b>", "");
			
			// infos
			int endSection = page.indexOf("</td>", j);
			if (endSection < 0) break;
			String section = page.substring(j, endSection);
			i = 0;
			do {
				int startInfo = section.indexOf("style=\"color: #808080\">", i);
				if (startInfo < 0) break;
				int endInfo = section.indexOf("</", startInfo);
				if (endInfo < 0) break;
				result.information.add(section.substring(startInfo+23, endInfo));
				i = endInfo;
			} while (true);
			
			results.add(result);
			pos = endSection;
		} while (true);
		return results;
	}
	private static boolean hasPage(String page, int rub, int pageIndex) {
		if (page == null) return false;
		return page.indexOf("&rub=" + rub + "&page=" + pageIndex) > 0;
	}
	
}
