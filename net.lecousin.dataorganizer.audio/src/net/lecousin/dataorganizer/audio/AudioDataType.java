package net.lecousin.dataorganizer.audio;

import net.lecousin.dataorganizer.audio.ui.OverviewPanel;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.Data.DuplicateAnalysis;
import net.lecousin.dataorganizer.core.database.content.DataContentType;
import net.lecousin.dataorganizer.core.database.info.Info;
import net.lecousin.dataorganizer.core.database.version.ContentTypeLoader;
import net.lecousin.framework.Triple;
import net.lecousin.framework.event.ProcessListener;
import net.lecousin.framework.xml.XmlWriter;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.w3c.dom.Element;

public class AudioDataType extends DataContentType {

	public AudioDataType(Data data) {
		super(data);
	}
	public AudioDataType(Data data, Element elt, ContentTypeLoader loader) {
		super(data, elt, loader);
	}
	@Override
	protected void saveContent(XmlWriter xml) {
		// TODO Auto-generated method stub

	}

	@Override
	protected Info createInfo() { return new AudioInfo(this, (String)null, (String)null); }
	@Override
	protected Info createInfo(Element elt, ContentTypeLoader loader) { return new AudioInfo(this, elt, loader); }
	

	@Override
	public DuplicateAnalysis checkForDuplicateOnContent(Data data) {
		// TODO Auto-generated method stub
		return DuplicateAnalysis.DIFFERENT;
	}
	@Override
	public boolean isSame(Info info) {
		// TODO Auto-generated method stub
		return false;
	}

	
	@Override
	public void createDescriptionPanel(Composite panel) {
		// TODO Auto-generated method stub

	}
	@Override
	public void createOverviewPanel(Composite panel) {
		new OverviewPanel(panel, this);
	}

	@Override
	public void getImages(ProcessListener<Triple<String, Image, Integer>> listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isContentAvailable() {
		// TODO Auto-generated method stub
		return false;
	}

}
