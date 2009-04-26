package net.lecousin.dataorganizer.retriever.cinefil;

public class CineFilUtil {

	public static final String SOURCE_ID = "CineFil";
	
	public static String getHost() { return "www.cinefil.com"; }
	
	public static String getIDFromURL(String url) {
		if (url == null || url.length() == 0) return null;
		int i = url.charAt(0)=='/' ? url.indexOf('/', 1) : url.indexOf('/');
		if (i < 0) return null;
		return url.substring(i+1);
	}
}
