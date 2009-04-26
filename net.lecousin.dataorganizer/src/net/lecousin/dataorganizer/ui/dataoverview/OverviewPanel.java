package net.lecousin.dataorganizer.ui.dataoverview;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.content.DataContentType;
import net.lecousin.dataorganizer.core.database.info.Info;
import net.lecousin.dataorganizer.core.database.info.InfoRetriever;
import net.lecousin.dataorganizer.core.database.info.InfoRetrieverPlugin;
import net.lecousin.dataorganizer.core.database.info.InfoRetrieverPluginRegistry;
import net.lecousin.dataorganizer.core.database.info.SourceInfo;
import net.lecousin.dataorganizer.core.database.info.SourceInfoMergeUtil;
import net.lecousin.dataorganizer.core.database.info.SourceInfo.Review;
import net.lecousin.dataorganizer.ui.control.DataImageControl;
import net.lecousin.dataorganizer.ui.control.RateDataControl;
import net.lecousin.framework.Pair;
import net.lecousin.framework.collections.SelfMap;
import net.lecousin.framework.collections.SelfMapLinkedList;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.thread.RunnableWithData;
import net.lecousin.framework.time.DateTimeUtil;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.LCCombo;
import net.lecousin.framework.ui.eclipse.control.LCGroup;
import net.lecousin.framework.ui.eclipse.control.Separator;
import net.lecousin.framework.ui.eclipse.control.UIControlUtil;
import net.lecousin.framework.ui.eclipse.control.button.MenuButton;
import net.lecousin.framework.ui.eclipse.control.button.MenuButton.MenuProvider;
import net.lecousin.framework.ui.eclipse.dialog.FlatPopupMenu;
import net.lecousin.framework.ui.eclipse.dialog.MyDialog;
import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class OverviewPanel extends Composite {

	public OverviewPanel(Composite parent) {
		super(parent, SWT.NONE);
		UIUtil.gridLayout(this, 2, 2, 2, 2, 2);
		setBackground(ColorUtil.getWhite());

		Composite tmpPanel = UIUtil.newGridComposite(this, 2, 0, 1);
		UIUtil.gridDataHorizFill(tmpPanel);
		labelsPanel = new HeaderPanel(tmpPanel);
		labelsPanel.setLayoutData(UIUtil.gridDataHoriz(1, true));
		
		Separator sep = new Separator(this, true, new Separator.GradientLine(ColorUtil.get(40, 40, 255), ColorUtil.get(255, 255, 255)), 5);
		UIUtil.gridDataHorizFill(sep);
		
		Composite leftPanel = UIUtil.newComposite(this);
		UIUtil.gridLayout(leftPanel, 1, 0, 0);
		leftPanel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		Composite centerPanel = UIUtil.newComposite(this);
		UIUtil.gridLayout(centerPanel, 1, 0, 0, 0, 1);
		GridData gd = UIUtil.gridDataHoriz(1, true);
		gd.verticalAlignment = SWT.BEGINNING;
		centerPanel.setLayoutData(gd);
//		Composite rightPanel = UIUtil.newComposite(this);
//		UIUtil.gridLayout(rightPanel, 1);
		
//		Color groupColor = ColorUtil.get(0, 0, 160);
		Color groupColor = ColorUtil.get(80, 80, 255);
			
		
		// left
		imageControl = new DataImageControl(leftPanel, null, 128, 128);
		LCGroup group = new LCGroup(leftPanel, Local.Reviews.toString(), groupColor);
		group.setBackground(ColorUtil.getWhite());
		gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		group.setLayoutData(gd);
		UIUtil.gridLayout(group.getInnerControl(), 1, 0, 0);
		reviewsPanel = group.getInnerControl();
		
		// center
		// + infos
		tmpPanel = UIUtil.newGridComposite(centerPanel, 0, 0, 2);
		UIUtil.gridDataHorizFill(tmpPanel);
		UIUtil.newLabel(tmpPanel, Local.Name+":", true, false);
		textName = new LCCombo(tmpPanel, null);
		textName.selectionEvent().addListener(new NameChanged());
		textName.setLayoutData(UIUtil.gridDataHoriz(1, true));
		Composite infoPanel = UIUtil.newRowComposite(centerPanel, SWT.HORIZONTAL, 0, 0, 5, true);
		UIUtil.gridDataHorizFill(infoPanel);
		// - note
		tmpPanel = UIUtil.newGridComposite(infoPanel, 0, 0, 2);
		UIUtil.newLabel(tmpPanel, Local.Rate+":", true, false);
		barRate = new RateDataControl(tmpPanel, null, true);
		// - added
		tmpPanel = UIUtil.newGridComposite(infoPanel, 0, 0, 2);
		UIUtil.newLabel(tmpPanel, Local.Added+":", true, false);
		labelAdded = UIUtil.newLabel(tmpPanel, "");
		// - vues
		tmpPanel = UIUtil.newGridComposite(infoPanel, 0, 0, 2);
		UIUtil.newLabel(tmpPanel, Local.Opened+":", true, false);
		labelOpened = UIUtil.newLabel(tmpPanel, "");
		// - last time
		tmpPanel = UIUtil.newGridComposite(infoPanel, 0, 0, 2);
		UIUtil.newLabel(tmpPanel, Local.Last_open+":", true, false);
		labelLastOpen = UIUtil.newLabel(tmpPanel, "");
		// - comment
		tmpPanel = UIUtil.newGridComposite(centerPanel, 0, 0, 2);
		UIUtil.gridDataHorizFill(tmpPanel);
		UIUtil.newLabel(tmpPanel, Local.Comment+":", true, false);
		comment = new Text(tmpPanel, SWT.WRAP | SWT.MULTI | SWT.BORDER);
		comment.setLayoutData(UIUtil.gridDataHoriz(1, true));
		comment.addModifyListener(new CommentChanged());
		
		// + sources
		tmpPanel = UIUtil.newGridComposite(centerPanel, 0, 0, 2);
		UIUtil.newLabel(tmpPanel, Local.Sources+":", true, false);
		sourcesPanel = UIUtil.newComposite(tmpPanel);
		RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
		rowLayout.wrap = true;
		rowLayout.marginHeight = rowLayout.marginTop = rowLayout.marginBottom = 0;
		sourcesPanel.setLayout(rowLayout);
		buttonRetrieveInfo = new MenuButton(sourcesPanel, SharedImages.getImage(SharedImages.icons.x16.basic.SEARCH), Local.Retrieve_information.toString(), true, new RetrieveInfoMenuProvider());
		// + content Type overview
		group = new LCGroup(centerPanel, Local.Details.toString(), groupColor);
		group.setBackground(ColorUtil.getWhite());
		UIUtil.gridDataHorizFill(group);
		UIUtil.gridLayout(group.getInnerControl(), 1, 0, 0);
		contentTypePanel = UIUtil.newComposite(group.getInnerControl());
		UIUtil.gridDataHorizFill(contentTypePanel);
		// + descriptions
		group = new LCGroup(centerPanel, Local.Description+" / "+Local.Resume, groupColor);
		group.setBackground(ColorUtil.getWhite());
		UIUtil.gridDataHorizFill(group);
		UIUtil.gridLayout(group.getInnerControl(), 1, 0, 0);
		contentDescriptionPanel = UIUtil.newComposite(group.getInnerControl());
		UIUtil.gridDataHorizFill(contentDescriptionPanel);
	}
	
	private Data data;
	
	private HeaderPanel labelsPanel;
	private DataImageControl imageControl;
	private LCCombo textName;
	private Composite contentTypePanel;
	private Composite reviewsPanel;
	private Composite contentDescriptionPanel;
	private RateDataControl barRate;
	private Text comment;
	private Label labelOpened, labelLastOpen, labelAdded;
	private Composite sourcesPanel;
	private MenuButton buttonRetrieveInfo;
	
	private Listener<Data> dataChanged = new Listener<Data>() { public void fire(Data event) { refresh(event); } };
	private Listener<DataContentType> dataContentChanged = new Listener<DataContentType>() { public void fire(DataContentType event) { refresh(event.getData()); } };
	
	public void refresh(Data data) {
		if (this.data != null) {
			this.data.modified().removeListener(dataChanged);
			this.data.contentModified().removeListener(dataContentChanged);
		}
		if (isDisposed()) return;
		this.data = data;
		if (data != null) {
			data.modified().addListener(dataChanged);
			data.contentModified().addListener(dataContentChanged);
		}
		imageControl.setData(data);
		labelsPanel.refresh(data);
		textName.clear();
		boolean nameFound = false;
		for (Pair<String,Image> p : data.getContent().getAllPossibleNames()) {
			textName.addItem(p.getValue2(), p.getValue1(), null);
			if (p.getValue1().equals(data.getName()))
				nameFound = true;
		}
		if (!nameFound)
			textName.addItem(null, data.getName(), null);
		textName.setSelection(data.getName());
		labelOpened.setText(Integer.toString(data.getViews().size()));
		labelLastOpen.setText(data.getViews().isEmpty() ? Local.Never.toString() : DateTimeUtil.getDateString(data.getViews().get(data.getViews().size()-1)));
		labelAdded.setText(DateTimeUtil.getDateString(data.getDateAdded()));
		refreshSources(data);
		barRate.setData(data);
		comment.setText(data.getComment() != null ? data.getComment() : "");
		sourceSelectionChanged(false);
		UIControlUtil.clear(contentDescriptionPanel);
		data.getContent().createDescriptionPanel(contentDescriptionPanel);
		layout(true, true);
		UIControlUtil.resize(this);
	}
	private void refreshSources(Data data) {
		for (Control c : sourcesPanel.getChildren())
			if (c != buttonRetrieveInfo) c.dispose();
		for (String source : data.getContent().getInfo().getSources()) {
			SourcePanel c = new SourcePanel(sourcesPanel, data, source);
			c.moveAbove(buttonRetrieveInfo);
		}
	}
	private void refreshReviews(Data data, List<SourceInfo> info) {
		UIControlUtil.clear(reviewsPanel);
		Set<String> types = data.getContent().getInfo().getReviewsTypes();
		if (types == null) return;
		for (String type : types) {
			SelfMap<String,Review> reviews = new SelfMapLinkedList<String, Review>();
			for (SourceInfo i : info)
				SourceInfoMergeUtil.mergeReviews(reviews, i.getReviews(type));
			new ReviewsControl(reviewsPanel, data, type, reviews);
		}
	}
	
	private class SourcePanel extends Composite {
		public SourcePanel(Composite parent, Data data, String source) {
			super(parent, SWT.NONE);
			this.source = source;
			setBackground(parent.getBackground());
			int numcolumns = 3 + (data.getContent().isOverviewPanelSupportingSourceMerge() ? 1 : 0);
			UIUtil.gridLayout(this, numcolumns, 0, 0, 2, 0);
			if (data.getContent().isOverviewPanelSupportingSourceMerge()) {
				button = UIUtil.newCheck(this, "", new Listener<Pair<Boolean,String>>() {
					public void fire(Pair<Boolean, String> event) {
						sourceSelectionChanged(true);
					}
				}, source);
				button.setSelection(true);
			}
			new SourceControl(this, data, source);
			UIUtil.newImageButton(this, SharedImages.getImage(SharedImages.icons.x16.basic.REFRESH), new Listener<Pair<Data,String>>() {
				public void fire(Pair<Data,String> event) {
					InfoRetriever.refresh(event.getValue1(), event.getValue2());
				}
			}, new Pair<Data,String>(data, source)).setToolTipText(Local.process(Local.Refresh_information_from__, InfoRetrieverPluginRegistry.getNameForSource(source, data.getContentType().getID())));
			UIUtil.newImageButton(this, SharedImages.getImage(SharedImages.icons.x16.basic.DEL), new Listener<Pair<Data,String>>() {
				public void fire(Pair<Data,String> event) {
					DataContentType content = event.getValue1().getContent();
					if (content == null) return;
					Info info = content.getInfo();
					if (info == null) return;
					info.removeSourceInfo(event.getValue2());
				}
			}, new Pair<Data,String>(data, source)).setToolTipText(Local.process(Local.Remove_information_from__, InfoRetrieverPluginRegistry.getNameForSource(source, data.getContentType().getID())));
		}
		private Button button;
		private String source;
	}
	
	private void sourceSelectionChanged(boolean layout) {
		UIControlUtil.clear(contentTypePanel);
		List<SourceInfo> infos = new LinkedList<SourceInfo>();
		List<String> selected = getSelectedSources();
		for (String source : selected)
			infos.add(data.getContent().getInfo().getSourceInfo(source));
		if (data.getContent().isOverviewPanelSupportingSourceMerge())
			data.getContent().createOverviewPanel(contentTypePanel, infos);
		else
			data.getContent().createOverviewPanel(contentTypePanel, null);
		refreshReviews(data, infos);
		if (layout) {
			layout(true, true);
			UIControlUtil.resize(this);
		}
	}
	private List<String> getSelectedSources() {
		List<String> sources = new LinkedList<String>();
		for (Control c : sourcesPanel.getChildren())
			if (c instanceof SourcePanel) {
				if (((SourcePanel)c).button.getSelection())
					sources.add(((SourcePanel)c).source);
			}
		return sources;
	}
	
	private class NameChanged implements Listener<Pair<String,Object>> {
		public void fire(Pair<String, Object> event) {
			String s = event.getValue1();
			if (s.equals(data.getName())) return;
			data.setName(s);
		}
	}
	private class CommentChanged implements ModifyListener {
		public void modifyText(ModifyEvent e) {
			String n = comment.getText();
			String p = data.getComment();
			if ((n.length() == 0 && p == null) || n.equals(p)) return;
			data.setComment(n);
			UIControlUtil.autoresize(comment);
		}
	}
	
	private class RetrieveInfoMenuProvider implements MenuProvider {
		public String getTitle() {
			return null;
		}
		public void fill(FlatPopupMenu menu) {
			new FlatPopupMenu.Menu(menu, Local.Retrieve_from_all_sources.toString(), SharedImages.getImage(SharedImages.icons.x16.basic.IMPORT), false, false, new Runnable() {
				public void run() {
					data.retrieveInfo(MyDialog.getPlatformShell());
					refresh(data);
				}
			});
			new FlatPopupMenu.Menu(menu, Local.Retrieve_from_all_missing_sources.toString(), SharedImages.getImage(SharedImages.icons.x16.basic.IMPORT), false, false, new Runnable() {
				public void run() {
					Info info = data.getContent().getInfo();
					List<InfoRetrieverPlugin> plugins = InfoRetrieverPluginRegistry.getRetrievers(data.getContentType().getID());
					for (Iterator<InfoRetrieverPlugin> it = plugins.iterator(); it.hasNext(); ) {
						InfoRetrieverPlugin pi = it.next();
						if (info.getSources().contains(pi.getSourceID()))
							it.remove();
					}
					if (plugins.isEmpty()) {
						MessageDialog.openInformation(MyDialog.getPlatformShell(), Local.Retrieve_information.toString(), Local.Informtion_already_retrieved+".");
						return;
					}
					InfoRetriever.retrieve(MyDialog.getPlatformShell(), data, plugins);
					refresh(data);
				}
			});
			new FlatPopupMenu.Menu(menu, Local.Refresh_all_already_retrieved_sources.toString(), SharedImages.getImage(SharedImages.icons.x16.basic.REFRESH), false, false, new Runnable() {
				public void run() {
					Info info = data.getContent().getInfo();
					List<InfoRetrieverPlugin> plugins = new LinkedList<InfoRetrieverPlugin>();
					for (String source : info.getSources())
						plugins.add(InfoRetrieverPluginRegistry.getPlugin(source, data.getContentType().getID()));
					if (plugins.isEmpty()) {
						MessageDialog.openInformation(MyDialog.getPlatformShell(), Local.Retrieve_information.toString(), Local.None_of_the_known_sources_are_already_retrieved+".");
						return;
					}
					InfoRetriever.retrieve(MyDialog.getPlatformShell(), data, plugins);
					refresh(data);
				}
			});
			List<InfoRetrieverPlugin> plugins = InfoRetrieverPluginRegistry.getRetrievers(data.getContentType().getID());
			for (InfoRetrieverPlugin plugin : plugins) {
				new FlatPopupMenu.Menu(menu, Local.From+" "+plugin.getName(), plugin.getIcon(), false, false, new RunnableWithData<InfoRetrieverPlugin>(plugin) {
					public void run() {
						InfoRetriever.retrieve(MyDialog.getPlatformShell(), data, data());
						refresh(data);
					}
				});
			}
		}
	}
}
