package net.lecousin.dataorganizer.people;

import java.util.Set;

import net.lecousin.dataorganizer.core.database.info.Info;
import net.lecousin.dataorganizer.core.database.info.SourceInfo;
import net.lecousin.dataorganizer.core.database.version.ContentTypeLoader;
import net.lecousin.framework.xml.XmlWriter;

import org.w3c.dom.Element;

public class PeopleInfo extends Info {

	public PeopleInfo(PeopleDataType data) {
		super(data);
	}
	public PeopleInfo(PeopleDataType data, Element elt, Loader loader) {
		super(data, elt, loader);
	}
	
	@Override
	protected void saveInfo(XmlWriter xml) {
	}
	
	@Override
	protected SourceInfo createSourceInfo(Info parent) {
		return new PeopleSourceInfo((PeopleInfo)parent);
	}
	@Override
	protected SourceInfo createSourceInfo(Info parent, Element elt, ContentTypeLoader loader) {
		return new PeopleSourceInfo((PeopleInfo)parent, elt, (Loader)loader);
	}
	
	@Override
	public PeopleSourceInfo getSourceInfo(String source) {
		return (PeopleSourceInfo)super.getSourceInfo(source);
	}
	
	@Override
	public Set<String> getReviewsTypes() { return null; }
}
