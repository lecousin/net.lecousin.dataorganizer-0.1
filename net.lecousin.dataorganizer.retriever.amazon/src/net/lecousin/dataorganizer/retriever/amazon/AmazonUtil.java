package net.lecousin.dataorganizer.retriever.amazon;

import net.lecousin.framework.application.Application;


public class AmazonUtil {

	public static final String SOURCE_ID = "Amazon";
	
	public static String getIDFromDPURL(String url) {
		int i = url.indexOf("/dp/");
		if (i < 0) return null;
		int j = url.indexOf('/', i+4);
		if (j < 0) return null;
		return url.substring(i+4, j);
	}
	public static String getPageURLForID(String id, String page) {
		return "/"+page+"/"+id+"/";
	}
	
	public static String getHost() {
		switch (Application.language) {
		default:
		case ENGLISH: return "www.amazon.com";
		case FRENCH: return "www.amazon.fr";
		}
	}
}
