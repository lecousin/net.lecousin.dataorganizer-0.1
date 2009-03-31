package net.lecousin.dataorganizer.ui.dataoverview;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.info.SourceInfo.Review;
import net.lecousin.framework.collections.SelfMap;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.chart.BarChart;
import net.lecousin.framework.ui.eclipse.dialog.FlatPagedListDialog;
import net.lecousin.framework.ui.eclipse.dialog.FlatPagedListDialog.Filter;
import net.lecousin.framework.ui.eclipse.dialog.FlatPagedListDialog.Provider;
import net.lecousin.framework.ui.eclipse.dialog.MyDialog.Orientation;
import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.events.HyperlinkEvent;

public class ReviewsControl extends Composite {

	public ReviewsControl(Composite parent, Data data, String name, SelfMap<String,Review> reviews) {
		super(parent, SWT.NONE);
		this.data = data;
		this.name = name;
		this.reviews = reviews;
		setBackground(parent.getBackground());
		UIUtil.gridLayout(this, 1);
		Composite tmpPanel = UIUtil.newGridComposite(this, 0, 0, 2);
		UIUtil.newLabel(tmpPanel, name, true, false);
		long total = 0, nb = 0;
		for (Review r : reviews) {
			if (r.getRate() == null) continue;
			total += r.getRate();
			nb++;
		}
		if (nb > 0) {
			int note = (int)(total/nb);
			UIUtil.newLabel(tmpPanel, note + "/20 (" + nb + ")");
		} else {
			UIUtil.newLabel(tmpPanel, Local.no_rated.toString());
		}
		
		BarChart chart = new BarChart(this, 0, 20, ColorUtil.getWhite(), ColorUtil.getBlack(), ColorUtil.getOrange());
		for (Review r : reviews) {
			if (r.getRate() == null) continue;
			chart.addValue(r.getRate(), 1);
		}
		GridData gd = new GridData();
		gd.widthHint = 100;
		gd.heightHint = 70;
		chart.setLayoutData(gd);
		
		gd = new GridData();
		gd.horizontalAlignment = SWT.CENTER;
		UIUtil.newLinkSoftNetStyle(this, Local.See_reviews.toString(), new Listener<HyperlinkEvent>() {
			public void fire(HyperlinkEvent e) {
				new ReviewsListDialog((Control)e.widget);
			}
		}).setLayoutData(gd);
	}
	
	private Data data;
	private String name;
	private SelfMap<String,Review> reviews;
	
	private class ReviewsListDialog {
		@SuppressWarnings("unchecked")
		public ReviewsListDialog(Control c) {
			FlatPagedListDialog<Review> dlg = new FlatPagedListDialog<Review>(
					c.getShell(), ReviewsControl.this.name + " " + Local.reviews, reviews, 10, new Provider<Review>() {
						public Control createControl(Composite parent, Review element) {
							return new ReviewControl(parent, element.getAuthor(), element.getReview(), element.getRate(), ReviewsControl.this.data);
						}
					}, new Filter[] {}
				);
			dlg.openRelative(c, Orientation.TOP_BOTTOM, true, false);
		}
	}
//		public ReviewsListDialog(Control c) {
//			dlg = new FlatPopupMenu(c, ReviewsControl.this.name + " " + Local.reviews, true, false, false, true);
//			panel = dlg.getControl();
//			panel.setBackground(ColorUtil.getWhite());
//			UIUtil.gridLayout(panel, 1, 0, 0);
//			list = new LinkedList<Pair<Pair<String,String>,Pair<String,Integer>>>();
//			for (Map.Entry<String,Map<String,Pair<String,Integer>>> source : ReviewsControl.this.reviews.entrySet())
//				for (Map.Entry<String,Pair<String,Integer>> author : source.getValue().entrySet()) {
//					list.add(new Pair<Pair<String,String>, Pair<String,Integer>>(new Pair<String,String>(source.getKey(), author.getKey()), new Pair<String,Integer>(author.getValue().getValue1(), author.getValue().getValue2())));
//				}
//			if (list.isEmpty()) return;
//			showPage(1);
//			dlg.show(c, FlatPopupMenu.Orientation.TOP_BOTTOM, false);
//		}
//
//		FlatPopupMenu dlg;
//		Composite panel;
//		LinkedList<Pair<Pair<String,String>,Pair<String,Integer>>> list;
//		
//		private static final int byPage = 10;
//		
//		private void showPage(int page) {
//			UIControlUtil.clear(panel);
//			if (list.size() > byPage)
//				new PageButtonPanel(page);
//			for (int i = 0; i < byPage; ++i) {
//				if (list.size() <= (page-1)*byPage+i) return;
//				Pair<Pair<String,String>,Pair<String,Integer>> p = list.get((page-1)*byPage+i);
//				if (i > 0)
//					UIUtil.newSeparator(panel, true, true);
//				ReviewControl c = new ReviewControl(panel, p.getValue1().getValue1(), p.getValue1().getValue2(), p.getValue2().getValue1(), p.getValue2().getValue2(), ReviewsControl.this.data);
//				UIUtil.gridDataHorizFill(c);
//			}
//			if (list.size() > byPage)
//				new PageButtonPanel(page);
//			dlg.resize();
//		}
//		private class PageButtonPanel extends Composite {
//			public PageButtonPanel(int page) {
//				super(panel, SWT.NONE);
//				setBackground(panel.getBackground());
//				GridData gd = new GridData();
//				gd.horizontalAlignment = SWT.CENTER;
//				setLayoutData(gd);
//				
//				int nbPages = list.size()/byPage + 1 + (list.size()%byPage > 0 ? 1 : 0); 
//				UIUtil.gridLayout(this, 3);
//				if (page > 1)
//					UIUtil.newImageButton(this, SharedImages.getImage(SharedImages.icons.x16.arrows.LEFT), new Listener<Integer>() {
//						public void fire(Integer event) {
//							showPage(event);
//						}
//					}, page-1);
//				else
//					UIUtil.newLabel(this, "");
//				UIUtil.newLabel(this, Local.Page+" " + page + " "+Local.on+" " + nbPages);
//				if (page < nbPages)
//					UIUtil.newImageButton(this, SharedImages.getImage(SharedImages.icons.x16.arrows.RIGHT), new Listener<Integer>() {
//						public void fire(Integer event) {
//							showPage(event);
//						}
//					}, page+1);
//				else
//					UIUtil.newLabel(this, "");
//			}
//		}
//	}
}
