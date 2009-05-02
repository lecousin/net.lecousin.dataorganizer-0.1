package net.lecousin.dataorganizer.ui.control;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.core.DataOrganizer;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.content.ContentType;
import net.lecousin.dataorganizer.core.database.info.Info.DataLink;
import net.lecousin.dataorganizer.ui.dialog.DataLinkPopup;
import net.lecousin.framework.Pair;
import net.lecousin.framework.collections.ArrayUtil;
import net.lecousin.framework.event.Event.ListenerData;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.LCGrid;
import net.lecousin.framework.ui.eclipse.control.UIControlUtil;
import net.lecousin.framework.ui.eclipse.dialog.FlatPopupMenu;
import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.HyperlinkEvent;

public class DataLinkListPanel extends LCGrid {

	public DataLinkListPanel(Composite parent, ListProvider provider) {
		super(parent, provider.getTitles().length+1, 0, 0, borderColor);
		this.provider = provider;
		titles = provider.getTitles();
		newEmptyCell(titleBgColor);
		for (String title : titles)
			UIUtil.newLabel(newCell(3,0,titleBgColor), title, true, true).setForeground(titleFgColor);
		getDisplay().asyncExec(shower);
	}
	
	private ListProvider provider;
	private String[] titles;
	private Shower shower = new Shower();
	private class Shower implements Runnable {
		private boolean stop = false;
		private boolean finished = false;
		private int pos = 0;
		public void run() {
			if (stop) { finished = true; return; }
			if (DataLinkListPanel.this.isDisposed()) { finished = true; return; }
			int nb = provider.getNbRows();
			int start = pos;
			for (; pos < nb && pos-start<5; pos++) {
				Color bg = (pos%2)==0 ? rowBgColor1 : rowBgColor2;
				Color fg = (pos%2)==0 ? rowFgColor1 : rowFgColor2;
				Object[] o = provider.getRow(pos);
				Data data = getData(o);
				if (data == null)
					newEmptyCell(bg);
				else
					new DataImageControl(newCell(1,0,bg), data, 64, 50);
				int j;
				for (j = 0; j < o.length; ++j)
					createControl(o[j], bg, fg);
				while (j++ < titles.length)
					newEmptyCell(bg);
			}
			UIUtil.resize(DataLinkListPanel.this);
			if (pos < nb)
				getDisplay().asyncExec(this);
			else
				finished = true;
		}
	};
	
	public static interface ListProvider {
		public String[] getTitles();
		public int getNbRows();
		public Object[] getRow(int index);
	}
	
	private static Color borderColor = ColorUtil.get(180, 180, 180);
	private static Color titleBgColor = ColorUtil.get(230, 230, 230);
	private static Color titleFgColor = ColorUtil.getBlack();
	private static Color rowBgColor1 = ColorUtil.getWhite();
	private static Color rowFgColor1 = ColorUtil.getBlack();
	private static Color rowBgColor2 = ColorUtil.get(220, 220, 255);
	private static Color rowFgColor2 = ColorUtil.getBlack();

	private void newLabel(String text, Color bg, Color fg, Object data) {
		Composite cell = newCell(3,0,bg);
		cell.setData(data);
		Label l = UIUtil.newLabel(cell, text);
		l.setForeground(fg);
	}
	private void newLink(String text, String contentTypeID, List<DataLink> links, Color bg, Color fg, Object data) {
		Composite cell = newCell(3,0,bg);
		cell.setData(data);
		UIUtil.newLinkSoftNetStyle(cell, text, new ListenerData<HyperlinkEvent,Pair<String,List<DataLink>>>(new Pair<String,List<DataLink>>(contentTypeID, links)) {
			public void fire(HyperlinkEvent e) {
				DataLinkPopup.open(data().getValue1(), data().getValue2(), (Control)e.widget, FlatPopupMenu.Orientation.TOP_BOTTOM);
			}
		});
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
					List<String> texts = new LinkedList<String>();
					for (String s : (List<String>)list) {
						if (s.trim().length() == 0) continue;
						boolean ok = true;
						for (Iterator<String> it = texts.iterator(); it.hasNext(); ) {
							String s2 = it.next();
							if (s2.toLowerCase().contains(s.toLowerCase())) { ok = false; break; }
							if (s.toLowerCase().contains(s2.toLowerCase())) { it.remove(); }
						}
						if (ok)
							texts.add(s);
					}
					text = "";
					for (String s : texts) {
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
			newLabel(text, bg, fg, o);
		else
			newLink(text, links.get(0).contentTypeID, links, bg, fg, o);
	}
	
	public void resetProvider(ListProvider p) {
		provider = p;
		if (!ArrayUtil.equals(p.getTitles(), titles)) {
			resetAll();
			return;
		}
		Control[] children = getChildren();
		int index = titles.length+1;
		int nb = provider.getNbRows();
		boolean toAdd = false;
		for (int i = 0; i < nb; ++i) {
			if (index >= children.length) { toAdd = true; break; }
			Object[] row = provider.getRow(i);
			index++;//skip image
			int j;
			for (j = 0; j < row.length; ++j) {
				if (index >= children.length) { toAdd = true; break; }
				Object co = children[index++].getData(); 
				if (co == null || !co.equals(row[j])) {
					resetAll();
					return;
				}
			}
			while (j++ < titles.length)
				index++;
		}
		if (index < children.length) {
			resetAll();
			return;
		}
		if (toAdd) {
			if (shower.finished) {
				shower.finished = false;
				getDisplay().asyncExec(shower);
			}
		}
	}
	private void resetAll() {
		shower.stop = true;
		shower = new Shower();
		UIControlUtil.clear(this);
		titles = provider.getTitles();
		newEmptyCell(titleBgColor);
		for (String title : titles)
			UIUtil.newLabel(newCell(3,0,titleBgColor), title, true, true).setForeground(titleFgColor);
		getDisplay().asyncExec(shower);
	}
}
