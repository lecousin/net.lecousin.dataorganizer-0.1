package net.lecousin.dataorganizer.allocine;

import net.lecousin.framework.application.Application;


public class AlloCineUtil {

	public static final String SOURCE_ID = "AlloCine";
	
	public static String getIDFromURL(String url) {
		if (url == null) return null;
		int start = url.lastIndexOf('/');
		if (start < 0) start = 0;
		int i = url.indexOf('=', start);
		if (i < 0) return null;
		int j = url.indexOf(".html",i);
		if (j < 0) return null;
		int k = url.indexOf('&', i);
		if (k > 0 && k < j)
			j = k;
		return url.substring(i+1,j);
	}
	
	public static String getHost() {
		switch (Application.language) {
		default:
		case ENGLISH: return "www.screenrush.co.uk";
		case FRENCH: return "www.allocine.fr";
		}
	}
}
