package net.lecousin.dataorganizer.ui.dataoverview;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.info.Info;
import net.lecousin.dataorganizer.core.database.info.InfoRetriever;
import net.lecousin.dataorganizer.core.database.info.InfoRetrieverPlugin;
import net.lecousin.dataorganizer.core.database.info.InfoRetrieverPluginRegistry;
import net.lecousin.dataorganizer.ui.control.DataImageControl;
import net.lecousin.dataorganizer.ui.control.RateControl;
import net.lecousin.framework.Pair;
import net.lecousin.framework.thread.RunnableWithData;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.LCGroup;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

public class OverviewPanel extends Composite {

	public OverviewPanel(Composite parent) {
		super(parent, SWT.NONE);
		GridLayout layout = UIUtil.gridLayout(this, 2);
		layout.verticalSpacing = 0;
		setBackground(ColorUtil.getWhite());

		Composite tmpPanel = UIUtil.newGridComposite(this, 2, 0, 1);
		UIUtil.gridDataHorizFill(tmpPanel);
		labelsPanel = new LabelsPanel(tmpPanel);
		labelsPanel.setLayoutData(UIUtil.gridDataHoriz(1, true));
		
		Composite leftPanel = UIUtil.newComposite(this);
		UIUtil.gridLayout(leftPanel, 1);
		leftPanel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		Composite centerPanel = UIUtil.newComposite(this);
		layout = UIUtil.gridLayout(centerPanel, 1);
		layout.marginHeight = 0;
		layout.verticalSpacing = 1;
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
		layout = UIUtil.gridLayout(group.getInnerControl(), 1);
		layout.marginHeight = layout.marginWidth = 0;
		reviewsPanel = group.getInnerControl();
		
		// center
		// + name and sources
		tmpPanel = UIUtil.newGridComposite(centerPanel, 0, 0, 2);
		UIUtil.gridDataHorizFill(tmpPanel);
		UIUtil.newLabel(tmpPanel, Local.Name+":", true, false);
		textName = UIUtil.newText(tmpPanel, "", new NameChanged());
		textName.setLayoutData(UIUtil.gridDataHoriz(1, true));
		UIUtil.newLabel(tmpPanel, Local.Sources+":", true, false);
		sourcesPanel = UIUtil.newComposite(tmpPanel);
		RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
		rowLayout.wrap = true;
		rowLayout.marginHeight = 0;
		sourcesPanel.setLayout(rowLayout);
		buttonRetrieveInfo = new MenuButton(sourcesPanel, SharedImages.getImage(SharedImages.icons.x16.basic.SEARCH), Local.Retrieve_information.toString(), true, new RetrieveInfoMenuProvider());
		// + you
		group = new LCGroup(centerPanel, Local.Your_review.toString(), groupColor);
		group.setBackground(ColorUtil.getWhite());
		UIUtil.gridDataHorizFill(group);
		layout = UIUtil.gridLayout(group.getInnerControl(), 2);
		layout.marginHeight = layout.marginWidth = 0;
		layout.verticalSpacing = 1;
		UIUtil.newLabel(group.getInnerControl(), Local.Rating.toString());
		UIUtil.newLabel(group.getInnerControl(), Local.Comment.toString());
		barRate = new RateControl(group.getInnerControl(), null, true);
		comment = new Text(group.getInnerControl(), SWT.WRAP | SWT.MULTI | SWT.BORDER);
		comment.setLayoutData(UIUtil.gridDataHoriz(1, true));
		comment.addModifyListener(new CommentChanged());
		// + content Type
		group = new LCGroup(centerPanel, Local.Details.toString(), groupColor);
		group.setBackground(ColorUtil.getWhite());
		UIUtil.gridDataHorizFill(group);
		layout = UIUtil.gridLayout(group.getInnerControl(), 1);
		layout.marginHeight = layout.marginWidth = 0;
		contentTypePanel = UIUtil.newComposite(group.getInnerControl());
		UIUtil.gridDataHorizFill(contentTypePanel);
		// + descriptions
		group = new LCGroup(centerPanel, Local.Description+" / "+Local.Resume, groupColor);
		group.setBackground(ColorUtil.getWhite());
		UIUtil.gridDataHorizFill(group);
		layout = UIUtil.gridLayout(group.getInnerControl(), 1);
		layout.marginHeight = layout.marginWidth = 0;
		contentDescriptionPanel = UIUtil.newComposite(group.getInnerControl());
		UIUtil.gridDataHorizFill(contentDescriptionPanel);
	}
	
	private Data data;
	
	private LabelsPanel labelsPanel;
	private DataImageControl imageControl;
	private Text textName;
	private Composite contentTypePanel;
	private Composite reviewsPanel;
	private Composite contentDescriptionPanel;
	private RateControl barRate;
	private Text comment;
	private Composite sourcesPanel;
	private MenuButton buttonRetrieveInfo;
	
	public void refresh(Data data) {
		this.data = data;
		imageControl.setData(data);
		labelsPanel.refresh(data);
		textName.setText(data.getName());
		refreshSources(data);
		barRate.setData(data);
		comment.setText(data.getComment() != null ? data.getComment() : "");
		UIControlUtil.clear(contentTypePanel);
		data.getContent().createOverviewPanel(contentTypePanel);
		refreshReviews(data);
		UIControlUtil.clear(contentDescriptionPanel);
		data.getContent().createDescriptionPanel(contentDescriptionPanel);
		layout(true, true);
		UIControlUtil.resize(this);
	}
	private void refreshSources(Data data) {
		for (Control c : sourcesPanel.getChildren())
			if (c != buttonRetrieveInfo) c.dispose();
		for (String source : data.getContent().getInfo().getSources()) {
			SourceControl c = new SourceControl(sourcesPanel, data, source);
			c.moveAbove(buttonRetrieveInfo);
		}
	}
	private void refreshReviews(Data data) {
		UIControlUtil.clear(reviewsPanel);
		Set<String> types = data.getContent().getInfo().getReviewsTypes();
		if (types == null) return;
		for (String type : types) {
			Map<String,Map<String,Pair<String,Integer>>> reviews = data.getContent().getInfo().getReviews(type);
			if (reviews == null) continue;
			new ReviewsControl(reviewsPanel, data, type, reviews);
		}
	}
	
	private class NameChanged implements ModifyListener {
		public void modifyText(ModifyEvent e) {
			if (textName.getText().equals(data.getName())) return;
			data.setName(textName.getText());
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
				new FlatPopupMenu.Menu(menu, plugin.getName(), plugin.getIcon(), false, false, new RunnableWithData<InfoRetrieverPlugin>(plugin) {
					public void run() {
						InfoRetriever.retrieve(MyDialog.getPlatformShell(), data, data());
						refresh(data);
					}
				});
			}
		}
	}
}
