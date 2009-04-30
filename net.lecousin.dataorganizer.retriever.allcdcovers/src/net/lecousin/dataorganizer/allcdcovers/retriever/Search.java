package net.lecousin.dataorganizer.allcdcovers.retriever;

import java.util.LinkedList;
import java.util.List;

import javax.net.SocketFactory;

import net.lecousin.dataorganizer.core.database.info.InfoRetrieverPlugin.SearchResult;
import net.lecousin.dataorganizer.retriever.allocine.AlloCineUtil;
import net.lecousin.dataorganizer.retriever.allocine.Local;
import net.lecousin.framework.net.http.client.HttpClient;
import net.lecousin.framework.net.http.client.HttpRequest;
import net.lecousin.framework.net.http.client.HttpResponse;
import net.lecousin.framework.progress.WorkProgress;

public class Search {
	
	public static final String MOVIES = "movies/films";
	public static final String MUSIC = "music/albums";

	public static List<SearchResult> search(String name, String cat, WorkProgress progress, int work) {
		List<SearchResult> results = new LinkedList<SearchResult>();
		int pageIndex = 1;
		String page;
		do {
			progress.setSubDescription(Local.Page+" " + pageIndex);
			page = getSearchPage(name, pageIndex, cat);
			results.addAll(getResults(page));
		} while (hasPage(page, name, ++pageIndex));
		progress.progress(work);
		return results;
	}

	private static String getSearchPage(String name, int pageIndex, String cat) {
		HttpClient client = new HttpClient(SocketFactory.getDefault());
		HttpRequest req = new HttpRequest(ACDCUtil.getHost(), 80, "/search/"+cat+"/"+name+"/"+pageIndex);
		try {
			HttpResponse resp = client.send(req, true, null, 0);
			String page = resp.getContent().getContent().getAsString();
			return page;
		} catch (Throwable t) {
			t.printStackTrace(System.err);
			return null;
		}
	}
	
	private static boolean hasPage(String page, String search, int pageIndex) {
		if (page == null) return false;
		return page.indexOf("/search/all/all/" + search + "/" + pageIndex) > 0;
	}
	
	private static List<SearchResult> getResults(String page) {
		List<SearchResult> results = new LinkedList<SearchResult>();
		if (page == null) return results;
		
	}	
}
