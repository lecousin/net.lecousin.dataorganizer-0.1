package net.lecousin.dataorganizer.video;

import java.util.HashSet;
import java.util.Set;

import net.lecousin.dataorganizer.core.database.info.Info;
import net.lecousin.dataorganizer.core.database.info.SourceInfo;
import net.lecousin.dataorganizer.core.database.version.ContentTypeLoader;
import net.lecousin.framework.collections.CollectionUtil;
import net.lecousin.framework.xml.XmlWriter;

import org.w3c.dom.Element;

public class VideoInfo extends Info {

	public VideoInfo(VideoDataType data) { 
		super(data); 
	}
	public VideoInfo(VideoDataType data, Element root, Loader loader) {
		super(data, root, loader);
	}
	
	@Override
	protected void saveInfo(XmlWriter xml) {
	}
	
	@Override
	protected SourceInfo createSourceInfo(Info parent) {
		return new VideoSourceInfo((VideoInfo)parent);
	}
	@Override
	protected SourceInfo createSourceInfo(Info parent, Element elt,	ContentTypeLoader loader) {
		return new VideoSourceInfo((VideoInfo)parent, elt, (Loader)loader);
	}
	
	@Override
	public VideoSourceInfo getSourceInfo(String source) {
		return (VideoSourceInfo)super.getSourceInfo(source);
	}
	
	static Set<String> reviewsTypes = new HashSet<String>(CollectionUtil.list(new String[] {
			Local.Press.toString(), Local.Public.toString()
		}));
	@Override
	public Set<String> getReviewsTypes() { return reviewsTypes; }
}
