package net.lecousin.dataorganizer.ui.application.bar;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.DataOrganizer;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.framework.Pair;
import net.lecousin.framework.collections.SortedListTree;
import net.lecousin.framework.thread.RunnableWithData;
import net.lecousin.framework.time.DateTimeUtil;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.control.Blinker;
import net.lecousin.framework.ui.eclipse.control.button.MenuButton;
import net.lecousin.framework.ui.eclipse.control.button.MenuButton.MenuProvider;
import net.lecousin.framework.ui.eclipse.dialog.FlatPopupMenu;
import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

import org.eclipse.jface.action.ControlContribution;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public class DataViewedItem extends ControlContribution {

	public static final String ID = "net.lecousin.dataorganizer.ui.application.bar.DataViewedItem";
	
	public DataViewedItem() {
		super(ID);
		DataOrganizer.database().dataOpenedChanged.addFireListener(new Runnable() {
			public void run() {
				refresh();
			}
		});
		refresh();
	}

	@Override
	protected Control createControl(Composite parent) {
		MenuButton button = new MenuButton(
				parent,
				SharedImages.getImage(SharedImages.icons.x16.basic.EDIT),
				Local.Some_data_have_been_opened_and_need_action.toString(),
				true,
				new Provider()
				);
		Blinker.blink(button, true, Blinker.LONG_NO_BLINK__BLINK_BLINK_BLINK_LITTLE_BLINK__SMOOTH_TRANSITION, null, ColorUtil.get(255, 200, 100));
		return button;
	}
	
	private class Provider implements MenuProvider {
		public String getTitle() {
			return Local.Data_opened.toString();
		}
		public void fill(FlatPopupMenu menu) {
			List<Pair<Data,Long>> opened = DataOrganizer.database().getOpenedData();
			HashMap<Data,List<Long>> byData = new HashMap<Data,List<Long>>();
			SortedListTree<Pair<Data,Long>> sorted = new SortedListTree<Pair<Data,Long>>(new Comparator<Pair<Data,Long>>() {
				public int compare(Pair<Data, Long> arg0, Pair<Data, Long> arg1) {
					return (int)(arg0.getValue2()-arg1.getValue2());
				}
			});
			for (Pair<Data,Long> p : opened) {
				Data data = p.getValue1();
				long date = p.getValue2();
				List<Long> list = byData.get(data);
				if (list == null) {
					list = new LinkedList<Long>();
					byData.put(data, list);
				}
				list.add(date);
				Pair<Data,Long> sortData = null;
				for (Pair<Data,Long> p2 : sorted)
					if (p2.getValue1() == data) { sortData = p2; break; }
				if (sortData == null) {
					sorted.add(p);
				} else {
					if (date > sortData.getValue2()) {
						sorted.remove(sortData);
						sortData.setValue2(date);
						sorted.add(sortData);
					}
				}
			}

			if (byData.size() > 1) {
				new FlatPopupMenu.Menu(menu, Local.Ignore_all.toString(), SharedImages.getImage(SharedImages.icons.x16.basic.IGNORE), false, false, new Runnable() {
					public void run() {
						DataOrganizer.database().ignoreAllDataOpened();
					}
				});
				new FlatPopupMenu.Menu(menu, Local.Take_all_into_account.toString(), SharedImages.getImage(SharedImages.icons.x16.basic.OK), false, false, new Runnable() {
					public void run() {
						DataOrganizer.database().countAllDataOpened();
					}
				});
				new FlatPopupMenu.Separator(menu);
			}
			for (Pair<Data,Long> p : sorted) {
				List<Long> list = byData.get(p.getValue1());
				String text = p.getValue1().getName() + " (";
				if (list.size() == 1)
					text += Local.opened_on + " ";
				else
					text += Local.opened+" "+list.size()+" "+Local.times+", "+Local.last+" "+Local.on__date__+" ";
				text += DateTimeUtil.getDateString(p.getValue2()) + ")";
				new FlatPopupMenu.Menu(menu, text, p.getValue1().getContentType().getIcon(), false, false, new RunnableWithData<Pair<Data,List<Long>>>(new Pair<Data,List<Long>>(p.getValue1(), list)) {
					public void run() {
						DataOrganizer.database().processDataOpened(data().getValue1(), data().getValue2());
					}
				});
			}
		}
	}

	void refresh() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				List<Pair<Data,Long>> opened = DataOrganizer.database().getOpenedData();
				if (opened.isEmpty())
					setVisible(false);
				else
					setVisible(true);
				if (getParent() != null) {
					getParent().markDirty();
					getParent().update(true);
				}
			}
		});
	}
}
