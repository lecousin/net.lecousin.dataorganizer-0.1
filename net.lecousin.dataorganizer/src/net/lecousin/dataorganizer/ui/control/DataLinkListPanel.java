package net.lecousin.dataorganizer.ui.control;

import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.core.DataOrganizer;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.content.ContentType;
import net.lecousin.dataorganizer.core.database.info.Info.DataLink;
import net.lecousin.dataorganizer.ui.dialog.DataLinkPopup;
import net.lecousin.framework.Pair;
import net.lecousin.framework.event.Event.ListenerData;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.dialog.FlatPopupMenu;
import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;

public class DataLinkListPanel extends Composite {

	public DataLinkListPanel(Composite parent, ListProvider provider) {
		super(parent, SWT.NONE);
		this.provider = provider;
		titles = provider.getTitles();
		GridLayout layout = UIUtil.gridLayout(this, titles.length*2, 0, 0);
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		newLabel("", titleBgColor, titleFgColor);
		boolean first = true;
		for (String title : titles) {
			if (first) first = false;
			else newLabel(" ", titleBgColor, titleFgColor);
			newLabel(title, titleBgColor, titleFgColor);
		}
		getDisplay().asyncExec(shower);
	}
	
	private ListProvider provider;
	private int pos = 0;
	private String[] titles;
	private Runnable shower = new Runnable() {
		public void run() {
			if (DataLinkListPanel.this.isDisposed()) return;
			int nb = provider.getNbRows();
			int start = pos;
			for (; pos < nb && pos-start<5; pos++) {
				Color bg = (pos%2)==0 ? rowBgColor1 : rowBgColor2;
				Color fg = (pos%2)==0 ? rowFgColor1 : rowFgColor2;
				Object[] o = provider.getRow(pos);
				Data data = getData(o);
				if (data == null)
					newLabel("", bg, fg);
				else {
					DataImageControl c = new DataImageControl(DataLinkListPanel.this, data, 64, 50);
					c.setBackground(bg);
					setGD(c);
				}
				boolean first = true;
				int j;
				for (j = 0; j < o.length; ++j) {
					if (first) first = false;
					else newLabel("", bg, fg);
					createControl(o[j], bg, fg);
				}
				while (j++ < titles.length) {
					newLabel("", bg, fg);
					newLabel("", bg, fg);
				}
			}
			UIUtil.resize(DataLinkListPanel.this);
			if (pos < nb)
				getDisplay().asyncExec(this);
		}
	};
	
	public static interface ListProvider {
		public String[] getTitles();
		public int getNbRows();
		public Object[] getRow(int index);
	}
	
	private static Color titleBgColor = ColorUtil.get(180, 180, 180);
	private static Color titleFgColor = ColorUtil.getBlack();
	private static Color rowBgColor1 = ColorUtil.getWhite();
	private static Color rowFgColor1 = ColorUtil.getBlack();
	private static Color rowBgColor2 = ColorUtil.get(220, 220, 255);
	private static Color rowFgColor2 = ColorUtil.getBlack();

	private void newLabel(String text, Color bg, Color fg) {
		Label label = UIUtil.newLabel(this, text);
		label.setBackground(bg);
		label.setForeground(fg);
		setGD(label);
	}
	private void newLink(String text, String contentTypeID, List<DataLink> links, Color bg, Color fg) {
		Hyperlink link = UIUtil.newLinkSoftNetStyle(this, text, new ListenerData<HyperlinkEvent,Pair<String,List<DataLink>>>(new Pair<String,List<DataLink>>(contentTypeID, links)) {
			public void fire(HyperlinkEvent e) {
				DataLinkPopup.open(data().getValue1(), data().getValue2(), (Control)e.widget, FlatPopupMenu.Orientation.TOP_BOTTOM);
			}
		});
		link.setBackground(bg);
		setGD(link);
	}
	private void setGD(Control c) {
		GridData gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		gd.horizontalIndent = gd.verticalIndent = 0;
		c.setLayoutData(gd);
	}
	
	private Data getData(Object[] o) {
		String contentTypeID = null;
		List<Pair<String,String>> ids = new LinkedList<Pair<String,String>>();
		for (int i = 0; i < o.length; ++i) {
			if (o[i] instanceof DataLink) {
				contentTypeID = ((DataLink)o[i]).contentTypeID;
				ids.add(new Pair<String,String>(((DataLink)o[i]).source, ((DataLink)o[i]).id));
				break;
			} else if (o[i] instanceof List) {
				List<?> list = (List<?>)o[i];
				if (list.isEmpty()) continue;
				Object obj = list.get(0);
				if (obj instanceof DataLink) {
					contentTypeID = ((DataLink)obj).contentTypeID;
					ids.add(new Pair<String,String>(((DataLink)obj).source, ((DataLink)obj).id));
					break;
				}
			}
		}
		if (ids.isEmpty()) return null;
		return DataOrganizer.database().getDataFromInfoSourceIDs(ContentType.getContentType(contentTypeID), ids);
	}
	
	@SuppressWarnings("unchecked")
	private void createControl(Object o, Color bg, Color fg) {
		String text = null;
		List<DataLink> links = new LinkedList<DataLink>();
		if (o instanceof String)
			text = (String)o;
		else if (o instanceof DataLink) {
			links.add((DataLink)o);
			text = ((DataLink)o).name;
		} else if (o instanceof List) {
			List<?> list = (List<?>)o;
			if (!list.isEmpty()) {
				Object oo = list.get(0);
				if (oo instanceof String) {
					boolean first = true;
					text = "";
					for (String s : (List<String>)list) {
						if (first) first = false;
						else text += "\r\n";
						text += s;
					}
				} else if (oo instanceof DataLink) {
					text = ((DataLink)oo).name;
					for (DataLink link : (List<DataLink>)list) {
						links.add(link);
						if (text.length() == 0)
							text = link.name;
					}
				}
			}
		}
		if (links.isEmpty())
			newLabel(text, bg, fg);
		else
			newLink(text, links.get(0).contentTypeID, links, bg, fg);
	}
}
