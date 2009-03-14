package net.lecousin.dataorganizer.core.database.version;

import java.util.List;

import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.content.ContentType;
import net.lecousin.dataorganizer.core.database.content.DataContentType;
import net.lecousin.dataorganizer.core.database.source.DataSource;

import org.w3c.dom.Element;

public interface DBLoader extends Loader {

	public String getName(Element root);
	public List<DataSource> getSources(Element root);
	public List<Long> getViews(Element root);
	public byte getRate(Element root);
	public String getComment(Element root);
	public long getDateAdded(Element root);
	public ContentType getContentType(Element root);
	
	public DataContentType getContent(Data data, Element root, ContentTypeLoader loader);
	
}
