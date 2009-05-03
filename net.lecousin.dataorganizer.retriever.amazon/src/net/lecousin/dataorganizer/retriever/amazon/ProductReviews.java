package net.lecousin.dataorganizer.retriever.amazon;

import java.util.LinkedList;
import java.util.List;

import net.lecousin.framework.Triple;
import net.lecousin.framework.strings.HTMLAnalyzeUtil;
import net.lecousin.framework.xml.XmlParsingUtil;
import net.lecousin.framework.xml.XmlUtil;
import net.lecousin.framework.xml.XmlParsingUtil.Node;

public class ProductReviews {

	public static List<Triple<String,String,Integer>> getReviews(String page) {
		List<Triple<String,String,Integer>> reviews = new LinkedList<Triple<String,String,Integer>>();
		int i = page.indexOf("<table id=\"productReviews\"");
		if (i < 0) return reviews;
		i = i+1;
		
		do {
			int j = page.indexOf("<a name=\"", i);
			if (j < 0) break;
			j = page.indexOf("<table", j);
			if (j < 0) break;
			int k = page.indexOf("</table>", j);
			if (k < 0) break;
			String header = page.substring(j, k);
			int jHeader = j;
			j = page.indexOf("<div ", k);
			if (j < 0) break;
			String body = page.substring(k+8, j);
			i = j;
			
			j = header.indexOf("<span ", 0);
			if (j < 0) continue;
			k = header.indexOf("<img", j);
			Integer note = null;
			if (k > 0) {
				Triple<Node,Boolean,Integer> t = XmlParsingUtil.parseOpenNode(header, k);
				String url = t.getValue1().attributes.get("src");
				if (url != null) {
					k = url.indexOf("stars-", 0);
					if (k > 0) {
						char c = url.charAt(k+6);
						if (c >= '0' && c <= '5')
							note = ((int)(c-'0'))*4;
					}
				}
			}
			k = header.indexOf("<b>", j);
			if (k < 0) continue;
			j = header.indexOf("</b>", k+3);
			if (j < 0) continue;
			String title = XmlUtil.decodeXML(HTMLAnalyzeUtil.cleanInfo(header.substring(k+3, j),false));
			
			String author;
			j = header.indexOf("<div ", j);
			if (j < 0) continue;
			k = header.indexOf("</div>", j);
			if (k < 0) {
				j = header.indexOf("<table", j);
				if (j < 0) continue;
				j = header.indexOf("<span", j);
				if (j < 0) continue;
				k = header.indexOf("</span>", j);
				if (k < 0) continue;
				Triple<Node,Boolean,Integer> t = XmlParsingUtil.parseOpenNode(header, j);
				author = XmlUtil.decodeXML(HTMLAnalyzeUtil.cleanInfo(header.substring(t.getValue3(), k),false));
			} else {
				author = Local.UnknownAuthor.toString();
				j = header.indexOf("<div ", k);
				if (j < 0)
					j = header.indexOf("<table ", k);
				if (j < 0) continue;
				body = page.substring(jHeader+k+6, jHeader+j);
			}
			
			String review = HTMLAnalyzeUtil.cleanInfo(body, true);
			if (title.trim().length() > 0)
				review = "<b>"+title+"</b><br/>"+review;
			reviews.add(new Triple<String,String,Integer>(author,review,note));
		} while(true);
		return reviews;
	}
	
	public static int getPageIndex(String pageURL) {
		int pageIndex = 1;
		int i = pageURL.indexOf("pageNumber=");
		if (i > 0) {
			int j = pageURL.indexOf('&', i);
			if (j < 0) j = pageURL.length();
			try { pageIndex = Integer.parseInt(pageURL.substring(i+11,j)); }
			catch (NumberFormatException e) {}
		}
		return pageIndex;
	}
	
	public static String getNextPageURL(String page, String pageURL, int pageIndex) {
		if (page.indexOf("pageNumber="+pageIndex) < 0) return null;
		int i = pageURL.indexOf("pageNumber=");
		if (i > 0)
			return pageURL.substring(0, i)+"pageNumber="+pageIndex;
		i = pageURL.indexOf('?');
		if (i < 0)
			return pageURL+"?pageNumber="+pageIndex;
		return pageURL+"&pageNumber="+pageIndex;
	}
}
