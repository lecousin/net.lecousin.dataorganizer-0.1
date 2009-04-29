package net.lecousin.dataorganizer.retriever.imdb;

public class IMDBUtil {

	public static final String SOURCE_ID = "imdb";
	
	public static String getHost() { return "www.imdb.com"; }
	
	public static String getIDFromURL(String url) {
		if (url == null || url.length() == 0) return null;
		if (url.charAt(0) == '/') url = url.substring(1);
		if (url.length() == 0) return null;
		if (url.charAt(url.length()-1) == '/') url = url.substring(0, url.length()-1);
		if (url.length() == 0) return null;
		int i = url.indexOf('/');
		if (i < 0) return null;
		return url.substring(i+1);
	}
}
