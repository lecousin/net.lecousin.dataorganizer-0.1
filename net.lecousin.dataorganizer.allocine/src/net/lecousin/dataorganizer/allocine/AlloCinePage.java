package net.lecousin.dataorganizer.allocine;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.net.SocketFactory;

import net.lecousin.dataorganizer.core.database.info.SourceInfo;
import net.lecousin.framework.Pair;
import net.lecousin.framework.Triple;
import net.lecousin.framework.net.http.client.HttpClient;
import net.lecousin.framework.net.http.client.HttpRequest;
import net.lecousin.framework.net.http.client.HttpResponse;
import net.lecousin.framework.net.mime.content.MimeContent;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.xml.XmlParsingUtil;
import net.lecousin.framework.xml.XmlParsingUtil.Node;

public abstract class AlloCinePage<T extends SourceInfo> {

	public final boolean retrieve(String id, T info, WorkProgress progress, int work) {
		boolean success = false;
		progress.setSubDescription(getDescription());
		String url = "/" + getCategory() + "/" + getPage() + "_gen_c" + getCategory() + "=" + id + ".html";
		String page = loadPage(url);
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
		} while (nextURL != null && page != null);
		progress.progress(work);
		return success;
	}
	
	private String loadPage(String url) {
		HttpRequest req = new HttpRequest("www.allocine.fr", 80, url);
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
	
	protected Pair<String,Integer> getSection(String page, String start, String end, int startPos) {
		int i = page.indexOf(start, startPos);
		if (i < 0) return null;
		int j = page.indexOf(end, i+start.length());
		if (j < 0) return null;
		return new Pair<String,Integer>(page.substring(i+start.length(), j), j+end.length());
	}
	
	protected String getInfoBoldFrom(String page, String startTag) {
		int i = page.indexOf(startTag);
		if (i < 0) return null;
		i = page.indexOf("<b>", i);
		if (i < 0) return null;
		int j = page.indexOf("</b>", i);
		if (j < 0) return null;
		return cleanInfo(page.substring(i+3, j), false);
	}
	
	protected String cleanInfo(String info, boolean keepStyle) {
		int i = 0;
		StringBuilder str = new StringBuilder();
		do {
			int j = info.indexOf('<', i);
			if (j > i)
				str.append(info.substring(i, j));
			else if (j < 0) {
				str.append(info.substring(i));
				break;
			}
			Pair<String,Integer> close = XmlParsingUtil.isClosingNode(info, j);
			if (close != null) {
				String name = close.getValue1();
				i = close.getValue2();
				if (keepStyle && isStyleNode(name))
					str.append(info.substring(j, i));
				continue;
			}
			Triple<Node,Boolean,Integer> open = XmlParsingUtil.parseOpenNode(info, j);
			if (open.getValue1() == null) {
				str.append(info.substring(i, j));
				break;
			}
			String name = open.getValue1().name;
			i = open.getValue3();
			if (keepStyle && isStyleNode(name)) {
				str.append(info.substring(j, i));
				continue;
			}
		} while (i < info.length()-1);
		return str.toString();
	}
	protected boolean isStyleNode(String name) {
		return
			name.equals("b") ||
			name.equals("i") ||
			name.equals("br");
	}
	
	protected Pair<String,String> getInfoLinkedFrom(String page, String startTag) {
		int i = page.indexOf(startTag);
		if (i < 0) return null;
		Pair<Pair<String,String>,Integer> p = getInfoLinked(page, i);
		return p != null ? p.getValue1() : null;
	}
	
	/** <name,url>,endPos */
	protected Pair<Pair<String,String>,Integer> getInfoLinked(String page, int i) {
		i = page.indexOf("<a", i);
		if (i < 0) return null;
		int end = page.indexOf("</a>", i);
		if (end < 0) return null;
		int hrefStart = page.indexOf("href=\"", i);
		if (hrefStart < 0 || hrefStart > end) return null;
		int hrefEnd = page.indexOf('\"', hrefStart+6);
		if (hrefEnd < 0 || hrefEnd > end) return null;
		String url = page.substring(hrefStart+6, hrefEnd);
		int start = page.indexOf('>', hrefEnd);
		if (start < 0 || start > end) return null;
		String name = page.substring(start+1, end);
		return new Pair<Pair<String,String>,Integer>(new Pair<String,String>(name, url), end+4);
	}
	
	protected List<Pair<String,String>> getInfoLinkedList(String page, String startTag, String endTag) {
		int i = page.indexOf(startTag);
		if (i < 0) return null;
		int j = page.indexOf(endTag, i+startTag.length());
		if (j < 0) return null;
		String section = page.substring(i + startTag.length(), j);
		List<Pair<String,String>> list = new LinkedList<Pair<String,String>>();
		i = 0;
		do {
			Pair<Pair<String,String>,Integer> p = getInfoLinked(section, i);
			if (p != null) {
				i = p.getValue2();
				list.add(p.getValue1());
			} else
				break;
		} while (true);
		return list;
	}
	
	protected String[] getColumns(String text) {
		List<String> result = new LinkedList<String>();
		int i = 0;
		do {
			Triple<Node,String,Integer> node = XmlParsingUtil.readNextNode("td", text, i);
			if (node == null) break;
			result.add(node.getValue2());
			i = node.getValue3();
		} while (true);
		return result.toArray(new String[result.size()]);
	}
	
	protected String removeAllTags(String text) {
		return removeAllTags(text, false);
	}
	protected String removeAllTags(String text, boolean keepStyle) {
		StringBuilder str = new StringBuilder();
		int i = 0;
		do {
			int j = text.indexOf('<', i);
			if (j < 0) {
				str.append(text.substring(i));
				break;
			}
			if (j > i)
				str.append(text.substring(i, j));
			Pair<String,Integer> close = XmlParsingUtil.isClosingNode(text, j);
			if (close != null) {
				if (keepStyle && isStyleNode(close.getValue1()))
					str.append(text.substring(j, close.getValue2()));
				i = close.getValue2();
				continue;
			}
			Triple<Node,Boolean,Integer> open = XmlParsingUtil.parseOpenNode(text, j);
			if (keepStyle && isStyle(open.getValue1().name))
				str.append(text.substring(j, open.getValue3()));
			i = open.getValue3();
		} while (i < text.length());
		return str.toString();
	}
	
	protected boolean isStyle(String tagName) {
		return 
			tagName.equalsIgnoreCase("b") ||
			tagName.equalsIgnoreCase("i");
		
	}
}
