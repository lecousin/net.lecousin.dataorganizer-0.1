package net.lecousin.dataorganizer.core.database.version;

import org.w3c.dom.Element;

public interface ContentTypeLoader extends Loader {

	public Element getInfo(Element root);
	
}
