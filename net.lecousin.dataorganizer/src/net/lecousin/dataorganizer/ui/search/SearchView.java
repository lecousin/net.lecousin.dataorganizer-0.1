package net.lecousin.dataorganizer.ui.search;

import java.util.List;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.DataOrganizer;
import net.lecousin.dataorganizer.core.database.content.ContentType;
import net.lecousin.dataorganizer.core.search.DataSearch.Parameter;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.event.ControlListenerWithData;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.ui.part.ViewPart;


public class SearchView extends ViewPart {
	public static final String ID = "net.lecousin.dataorganizer.searchView";

	private ExpandBar panel;
	
	@Override
	public void setInitializationData(IConfigurationElement cfig, String propertyName, Object data) {
		super.setInitializationData(cfig, propertyName, data);
		setTitleImage(SharedImages.getImage(SharedImages.icons.x16.basic.SEARCH));
		setPartName(Local.Search.toString());
	}
	
	@Override
	public void createPartControl(Composite parent) {
		panel = new ExpandBar(parent, SWT.V_SCROLL);
		ExpandItem item;

		Control c = new SearchParametersPanel(panel, DataOrganizer.search().getMainParameters());
		item = new ExpandItem(panel, SWT.NONE);
		item.setText(Local.Main_criteria.toString());
		item.setControl(c);
		item.setExpanded(true);
		register(c, item);

		ContentTypesPanel contentTypesPanel = new ContentTypesPanel(panel);
		item = new ExpandItem(panel, SWT.NONE);
		item.setText(Local.Content_type.toString());
		item.setControl(contentTypesPanel);
		item.setHeight(contentTypesPanel.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item.setExpanded(true);

		item = new ExpandItem(panel, SWT.NONE);
		item.setText(Local.Labels.toString());
		new SearchLabelsPanel(item);

		ContentTypeAdded typeAdded = new ContentTypeAdded();
		contentTypesPanel.typeAdded().addListener(typeAdded);
		contentTypesPanel.typeRemoved().addListener(new ContentTypeRemoved());
		for (String type : DataOrganizer.search().getContentTypes().getContentTypes())
			typeAdded.fire(type);
		
		panel.addControlListener(new ControlListener() {
			public void controlMoved(ControlEvent e) {
			}
			public void controlResized(ControlEvent e) {
				ExpandBar panel = (ExpandBar)e.widget;
				for (ExpandItem item : panel.getItems()) {
					if (item.getControl() instanceof Composite)
						((Composite)item.getControl()).layout(true, true);
					Point size = item.getControl().computeSize(panel.getSize().x-panel.getSpacing()*2, SWT.DEFAULT);
					item.getControl().setSize(size);
					item.setHeight(size.y);
				}
			}
		});
	}
	
	@Override
	public void setFocus() {
	}
	
	private class ContentTypeAdded implements Listener<String> {
		public void fire(String event) {
			ContentType type = ContentType.getContentType(event);
			List<Parameter> params = DataOrganizer.search().getContentTypeParameters(type.getID());
			if (params == null || params.isEmpty()) return;
			Control c = new SearchParametersPanel(panel, params);
			ExpandItem item = new ExpandItem(panel, SWT.NONE);
			item.setText(type.getName());
			item.setControl(c);
			item.setExpanded(true);
			item.setData(type);
			register(c, item);
		}
	}
	private class ContentTypeRemoved implements Listener<String> {
		public void fire(String event) {
			ContentType type = ContentType.getContentType(event);
			for (ExpandItem item : panel.getItems()) {
				if (item.getData() != null && item.getData() == type) {
					item.getControl().dispose();
					item.dispose();
					return;
				}
			}
		}
	}
	
	private void register(Control c, ExpandItem item) {
		item.setHeight(c.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		c.addControlListener(new ControlListenerWithData<ExpandItem>(item) {
			public void controlMoved(ControlEvent e) {
			}
			public void controlResized(ControlEvent e) {
				int h1 = ((Control)e.widget).getSize().y;
				int h2 = data().getHeight();
				if (h1 != h2)
					data().setHeight(h1);
			}
		});
	}
}