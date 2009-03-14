package net.lecousin.dataorganizer.people;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.Data.DuplicateAnalysis;
import net.lecousin.dataorganizer.core.database.content.DataContentType;
import net.lecousin.dataorganizer.core.database.info.Info;
import net.lecousin.dataorganizer.core.database.version.ContentTypeLoader;
import net.lecousin.dataorganizer.people.ui.OverviewPanel;
import net.lecousin.dataorganizer.util.DataImageLoader;
import net.lecousin.dataorganizer.util.DataImageLoader.FileProvider_FromDataPath;
import net.lecousin.framework.Triple;
import net.lecousin.framework.event.ProcessListener;
import net.lecousin.framework.event.SplitProcessListener;
import net.lecousin.framework.xml.XmlWriter;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.w3c.dom.Element;

public class PeopleDataType extends DataContentType {

	public PeopleDataType(Data data, Element elt, Loader loader) {
		super(data, elt, loader);
	}

	public PeopleDataType(Data data) {
		super(data);
	}
	@Override
	protected Info createInfo(Element elt, ContentTypeLoader loader) { return new PeopleInfo(this, elt, (Loader)loader); }
	@Override
	protected Info createInfo() { return new PeopleInfo(this, (String)null, null); }

	@Override
	protected void saveContent(XmlWriter xml) {
	}
	
	@Override
	public boolean isContentAvailable() {
		return true;
	}

	@Override
	public DuplicateAnalysis checkForDuplicateOnContent(Data data) {
		Info info = data.getContent().getInfo();
		if (info == null)
			return DuplicateAnalysis.DIFFERENT;
		return isSame(info) ? DuplicateAnalysis.EXACTLY_THE_SAME : DuplicateAnalysis.DIFFERENT;
	}
	@Override
	public boolean isSame(Info info) {
		if (info == null || !(info instanceof PeopleInfo)) return false;
		PeopleInfo i1 = (PeopleInfo)getInfo();
		PeopleInfo i2 = (PeopleInfo)info;
		for (String s : i1.getNamesSources()) {
			String n1 = i1.getName(s);
			String n2 = i2.getName(s);
			if (n2 == null) continue;
			if (!n1.equalsIgnoreCase(n2)) return false;
		}
		if (i1.getBirthDay() == 0) return true;
		if (i2.getBirthDay() == 0) return true; 
		if (i1.getBirthDay() != i2.getBirthDay()) return false;
		return true;
	}

	@Override
	public void createOverviewPanel(Composite panel) {
		new OverviewPanel(panel, this);
	}
	@Override
	public void createDescriptionPanel(Composite panel) {
	}

	private List<Image> photos = null;
	
	void signalNewPhoto() {
		photos = null;
	}
	
	@Override
	public void getImages(ProcessListener<Triple<String, Image, Integer>> listener) {
		SplitProcessListener<Triple<String, Image, Integer>> split = new SplitProcessListener<Triple<String, Image, Integer>>(listener);
		ProcessListener<Triple<String, Image, Integer>> posterListener = split.newListener();
		loadPosters(posterListener);
	}
	
	private void loadPosters(ProcessListener<Triple<String, Image, Integer>> listener) {
		PeopleInfo info = (PeopleInfo)getInfo();
		if (info == null) {
			listener.started();
			listener.done();
			return;
		}
		synchronized (this) {
			if (photos != null) {
				listener.started();
				for (int i = 0; i < photos.size(); ++i)
					listener.fire(new Triple<String, Image, Integer>(Local.Photo+" " + (i+1), photos.get(i), 10));
				listener.done();
				return;
			}
		}
		Collection<String> paths = info.getPhotosPaths();
		if (paths.isEmpty()) {
			synchronized (this) {
				photos = new LinkedList<Image>();
			}
			listener.started();
			listener.done();
			return;
		}
		class Listener implements ProcessListener<Image> {
			Listener(ProcessListener<Triple<String, Image, Integer>> listener) {
				this.listener = listener;
			}
			private ProcessListener<Triple<String, Image, Integer>> listener;
			private List<Image> images = new LinkedList<Image>();
			public void started() {
				listener.started();
			}
			public void fire(Image image) {
				listener.fire(new Triple<String,Image, Integer>(Local.Photo+" " + (images.size()+1), image, 10));
				images.add(image);
			}
			public void done() {
				listener.done();
				synchronized (PeopleDataType.this) {
					photos = images;
					DataImageLoader.release(PeopleDataType.this, "photos");
				}
			}
		}
		DataImageLoader.load(this, "photos", new FileProvider_FromDataPath(getData(), paths), new Listener(listener));
	}

}
