package net.lecousin.dataorganizer.retriever.amazon;

import net.lecousin.framework.Triple;
import net.lecousin.framework.xml.XmlParsingUtil;
import net.lecousin.framework.xml.XmlParsingUtil.Node;

public class MainPage {

	public static String getDescription(String page) {
		int i = page.indexOf("id=\"productDescription\"");
		if (i < 0) return null;
		i = page.indexOf("<div ", i);
		if (i < 0) return null;
		Triple<Node,Boolean,Integer> t = XmlParsingUtil.parseOpenNode(page, i);
		i = t.getValue3();
		int j = page.indexOf("</div>", i);
		if (j < 0) return null;
		String s = page.substring(i, j).trim();
		return s;
	}
	
}
