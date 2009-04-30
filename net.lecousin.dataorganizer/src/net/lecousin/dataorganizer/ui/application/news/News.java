package net.lecousin.dataorganizer.ui.application.news;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.net.SocketFactory;

import net.lecousin.dataorganizer.core.DataOrganizer;
import net.lecousin.dataorganizer.ui.application.update.Updater;
import net.lecousin.framework.Pair;
import net.lecousin.framework.application.Application;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.net.http.client.HttpClient;
import net.lecousin.framework.net.http.client.HttpRequest;
import net.lecousin.framework.net.http.client.HttpResponse;
import net.lecousin.framework.thread.RunnableWithData;
import net.lecousin.framework.ui.eclipse.browser.BrowserWindow;
import net.lecousin.framework.version.Version;
import net.lecousin.framework.xml.XmlUtil;

import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Element;

public class News extends Thread {

	public static void check() {
		new News().start();
	}
	
	private News() {}
	@Override
	public void run() {
		List<New> news = getNews();
		if (!news.isEmpty())
			showNews(news);
	}
	
	private static class New {
		New(String id, long date) { this.id = id; this.date = date; }
		private String id;
		private long date;
	}
	
	private List<New> getNews() {
		List<New> result = new LinkedList<New>();
		HttpClient client = new HttpClient(SocketFactory.getDefault());
		HttpRequest req = new HttpRequest("dataorganizer.webhop.net", 80, "/"+getLangPath()+"/news/news.xml");
		Element root;
		Version current;
		try { 
			HttpResponse resp = client.send(req, true, null, 0); 
			String xml = resp.getContentAsString();
			if (xml == null) {
				if (Log.error(this))
					Log.error(this, "Unable to retrieve news information: no xml content");
				return result;
			}
			root = XmlUtil.parse(xml);
			current = Updater.getCurrentVersion();
		} catch (Throwable e) {
			if (Log.error(this))
				Log.error(this, "Unable to retrieve news information", e);
			return result;
		}
		long lastDate = getLastNewShown();
		for (Element e : XmlUtil.get_childs_element(root, "new")) {
			long date;
			try { date = new SimpleDateFormat("dd/MM/yyyy").parse(e.getAttribute("date")).getTime(); }
			catch (ParseException ex) {
				if (Log.error(this))
					Log.error(this, "Invalid news date: " + e.getAttribute("date"), ex);
				continue;
			}
			if (e.hasAttribute("debug")) {
				if (!Updater.isMySelf()) continue;
			} else {
				if (date <= lastDate) continue;
				Version from = null, to = null;
				if (e.hasAttribute("from"))
					from = new Version(e.getAttribute("from"));
				if (e.hasAttribute("to"))
					from = new Version(e.getAttribute("to"));
				if ((from != null && current.compareTo(from) < 0) || (to != null && current.compareTo(to) > 0)) continue;
			}
			result.add(new New(e.getAttribute("id"), date));
		}
		return result;
	}
	
	private String getLangPath() {
		switch (Application.language) {
		default:
		case ENGLISH: return "en";
		case FRENCH: return "fr";
		}
	}
	
	private void showNews(List<New> news) {
		String url = "http://dataorganizer.webhop.net/"+getLangPath()+"/news/show.php?ids=";
		boolean first = true;
		long last = 0;
		Set<String> done = new HashSet<String>();
		for (New n : news) {
			if (!done.contains(n.id)) {
				if (first) first = false; else url += ",";
				url += n.id;
				done.add(n.id);
			}
			if (n.date > last)
				last = n.date;
		}
		Display.getDefault().asyncExec(new RunnableWithData<Pair<String,Long>>(new Pair<String,Long>(url,last)) {
			public void run() {
				BrowserWindow browser = new BrowserWindow("DataOrganizer [News]", null, true, true);
				browser.open();
				browser.setLocation(data().getValue1());
				setLastNewShown(data().getValue2());
			}
		});
	}
	
	private long getLastNewShown() {
		return DataOrganizer.config().last_new_shown;
	}
	private void setLastNewShown(long date) {
		DataOrganizer.config().last_new_shown = date;
		DataOrganizer.config().save();
	}
}
