package net.lecousin.dataorganizer.people;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.Data.DuplicateAnalysis;
import net.lecousin.dataorganizer.core.database.content.DataContentType;
import net.lecousin.dataorganizer.core.database.info.Info;
import net.lecousin.dataorganizer.core.database.info.SourceInfo;
import net.lecousin.dataorganizer.core.database.info.SourceInfoMergeUtil;
import net.lecousin.dataorganizer.core.database.version.ContentTypeLoader;
import net.lecousin.dataorganizer.people.ui.DescriptionPanel;
import net.lecousin.dataorganizer.people.ui.PeopleOverviewPanel;
import net.lecousin.dataorganizer.util.DataImageLoader;
import net.lecousin.dataorganizer.util.DataImageLoader.FileProvider_FromDataPath;
import net.lecousin.framework.Pair;
import net.lecousin.framework.collections.CollectionUtil;
import net.lecousin.framework.event.ProcessListener;
import net.lecousin.framework.event.SplitProcessListener;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.ui.eclipse.control.UIControlUtil;
import net.lecousin.framework.xml.XmlWriter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
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
	protected Info createInfo() { return new PeopleInfo(this); }

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
		for (String s : i1.getSources()) {
			String n1 = i1.getSourceName(s);
			String n2 = i2.getSourceName(s);
			if (n2 == null) continue;
			if (!n1.equalsIgnoreCase(n2)) return false;
		}
		long bd1 = -1, bd2 = -1;
		for (String s : i1.getSources()) {
			PeopleSourceInfo pi = i1.getSourceInfo(s);
			if (pi == null) continue;
			bd1 = SourceInfoMergeUtil.mergeDate(bd1, pi.getBirthDay());
		}
		for (String s : i2.getSources()) {
			PeopleSourceInfo pi = i2.getSourceInfo(s);
			if (pi == null) continue;
			bd2 = SourceInfoMergeUtil.mergeDate(bd2, pi.getBirthDay());
		}
		if (bd1 <= 0) return true;
		if (bd2 <= 0) return true; 
		if (bd1 != bd2) return false;
		return true;
	}

	@Override
	public void createOverviewPanel(Composite panel, List<SourceInfo> sources, boolean big) {
		panel.setData(new PeopleOverviewPanel(panel, this, sources, big));
	}
	@Override
	public void refreshOverviewPanel(Composite panel, List<SourceInfo> sources) {
		((PeopleOverviewPanel)panel.getData()).refresh(sources);
	}
	@Override
	public boolean isOverviewPanelSupportingSourceMerge() {
		return true;
	}
	@Override
	public void createDescriptionPanel(Composite panel) {
		DescriptionPanel.create(this, panel);
	}
	@Override
	public void refreshDescriptionPanel(Composite panel) {
		UIControlUtil.clear(panel);
		createDescriptionPanel(panel);
	}

	private List<Pair<Image,String>> photos = null;
	
	void signalNewPhoto() {
		photos = null;
	}
	
	private static final DataImageCategory[] categories = new DataImageCategory[] {
		new DataImageCategory("photos", Local.Photos.toString(), 10),
	};
	@Override
	public List<DataImageCategory> getImagesCategories() { return CollectionUtil.list(categories); }
	@Override
	public void getImages(ProcessListener<DataImageLoaded> listener) {
		SplitProcessListener<DataImageLoaded> split = new SplitProcessListener<DataImageLoaded>(listener);
		ProcessListener<DataImageLoaded> posterListener = split.newListener();
		loadPosters(posterListener);
	}
	
	private void loadPosters(ProcessListener<DataImageLoaded> listener) {
		PeopleInfo info = (PeopleInfo)getInfo();
		if (info == null) {
			listener.started();
			listener.done();
			return;
		}
		synchronized (this) {
			if (photos != null) {
				listener.started();
				for (int i = 0; i < photos.size(); ++i) {
					Pair<Image,String> p = photos.get(i);
					listener.fire(new DataImageLoaded("photos", Local.Photo+" " + (i+1), p.getValue1(), p.getValue2()));
				}
				listener.done();
				return;
			}
		}
		Set<String> paths = new HashSet<String>();
		for (String source : info.getSources())
			if (info.getSourceInfo(source) != null)
				paths.addAll(info.getSourceInfo(source).getPhotosPaths());
		if (paths.isEmpty()) {
			synchronized (this) {
				photos = new LinkedList<Pair<Image,String>>();
			}
			listener.started();
			listener.done();
			return;
		}
		class Listener implements ProcessListener<Pair<Image,String>> {
			Listener(ProcessListener<DataImageLoaded> listener) {
				this.listener = listener;
			}
			private ProcessListener<DataImageLoaded> listener;
			private List<Pair<Image,String>> images = new LinkedList<Pair<Image,String>>();
			public void started() {
				listener.started();
			}
			public void fire(Pair<Image,String> image) {
				listener.fire(new DataImageLoaded("photos", Local.Photo+" " + (images.size()+1), image.getValue1(), image.getValue2()));
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

	@Override
	public Control createImageCategoryControls(Composite parent) {
		return null;
	}
	
	@Override
	public void removeImage(DataImageLoaded image) {
		synchronized (this) {
			for (Iterator<Pair<Image,String>> it = photos.iterator(); it.hasNext(); ) {
				Pair<Image,String> p = it.next();
				if (p.getValue1() == image.getImage()) {
					it.remove();
					break;
				}
			}
			try {
				IFile file = getFile(image.getFileName());
				file.delete(true, null);
			} catch (CoreException e) {
				if (Log.error(this))
					Log.error(this, "Unable to remove image file '" + image.getFileName() + "' for data ID " + getData().getID());
			}
		}
	}
	
	@Override
	protected void mergeContent(DataContentType other, Shell shell) {
		photos = null;
	}
}
