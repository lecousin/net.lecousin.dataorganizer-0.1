package net.lecousin.dataorganizer.core.database.version;

import java.util.Map;

import org.w3c.dom.Element;

public interface ContentTypeLoader extends Loader {

	public Element getInfo(Element root);
	
	public Map<String,String> getInfoNames(Element root);
	public Map<String,String> getInfoIDs(Element root);
	
}
