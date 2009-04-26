package net.lecousin.dataorganizer.retriever.cinefil;

import java.io.IOException;

import javax.net.SocketFactory;

import net.lecousin.dataorganizer.core.database.info.SourceInfo;
import net.lecousin.framework.Pair;
import net.lecousin.framework.net.http.client.HttpClient;
import net.lecousin.framework.net.http.client.HttpRequest;
import net.lecousin.framework.net.http.client.HttpResponse;
import net.lecousin.framework.net.mime.content.MimeContent;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.strings.HTMLAnalyzeUtil;

public abstract class CineFilPage<T extends SourceInfo> extends HTMLAnalyzeUtil {

	public final boolean retrieve(String id, T info, WorkProgress progress, int work) {
		boolean success = false;
		progress.setSubDescription(getDescription());
		String url = "/" + getCategory() + "/" + id + getPage();
		String page = loadPage(url);
		if (page == null) {
			progress.progress(work);
			return false;
		}
		int step = work*80/100;
		progress.progress(step);
		work -= step;
		String nextURL = firstPageToReload(page, url);
		if (nextURL != null) {
			page = loadPage(nextURL);
			step = work/10;
			work -= step;
			progress.progress(step);
		} else
			nextURL = url;
		do {
			step = work/10;
			work -= step;
			Pair<String,Boolean> result = parse(page, nextURL, info, progress, step);
			nextURL = result.getValue1();
			success |= result.getValue2();
			if (nextURL != null)
				page = loadPage(nextURL);
			if (progress.isCancelled()) break;
		} while (nextURL != null && page != null);
		progress.progress(work);
		return success;
	}
	
	private String loadPage(String url) {
		HttpRequest req = new HttpRequest(CineFilUtil.getHost(), 80, url);
		HttpClient client = new HttpClient(SocketFactory.getDefault());
		HttpResponse resp;
		try { resp = client.send(req, true, null, 0); }
		catch (IOException e) { return null; }
		if (resp.getStatusCode() != 200) return null;
		if (resp.getContent() == null) return null;
		MimeContent content = resp.getContent().getContent();
		if (content == null) return null;
		try { return content.getAsString(); }
		catch (IOException e) { return null; }
	}
	
	protected abstract String getDescription();
	protected abstract String getCategory();
	protected abstract String getPage();
	
	protected abstract String firstPageToReload(String page, String url);
	protected abstract Pair<String,Boolean> parse(String page, String pageURL, T info, WorkProgress progress, int work);
	
}
