package net.lecousin.dataorganizer.ui.wizard.adddata;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.InitializationException;
import net.lecousin.dataorganizer.core.database.VirtualData;
import net.lecousin.dataorganizer.core.database.VirtualDataBase;
import net.lecousin.dataorganizer.core.database.content.ContentType;
import net.lecousin.dataorganizer.core.database.info.InfoRetriever;
import net.lecousin.dataorganizer.core.database.info.InfoRetrieverPlugin;
import net.lecousin.dataorganizer.core.database.info.InfoRetrieverPluginRegistry;
import net.lecousin.dataorganizer.core.database.info.InfoRetriever.FeedBackImpl;
import net.lecousin.dataorganizer.core.database.source.DataSource;
import net.lecousin.dataorganizer.internal.EclipsePlugin;
import net.lecousin.framework.Pair;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.LCCombo;
import net.lecousin.framework.ui.eclipse.control.LCGroup;
import net.lecousin.framework.ui.eclipse.dialog.ErrorDlg;
import net.lecousin.framework.ui.eclipse.progress.WorkProgressDialog;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

public class AddData_Internet extends WizardPage implements AddData_Page {

	public AddData_Internet() {
		super(Local.Add_data_from_internet.toString());
		setTitle(Local.Add_data_from_internet.toString());
		setDescription(Local.Add_data_from_internet__description.toString());
	}
	
	private LCCombo comboType;
	private Text textName;
	private LCGroup groupSources;
	private List<Button> buttonSources = new LinkedList<Button>();
	
	public void createControl(Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);
		UIUtil.gridLayout(panel, 2);
		
		UIUtil.newLabel(panel, Local.Content_type.toString());
		comboType = new LCCombo(panel, null);
		comboType.setEditable(false);
		for (ContentType type : ContentType.getAvailableTypes())
			comboType.addItem(type.getIcon(), type.getName(), type.getID());
		comboType.selectionEvent().addFireListener(new Runnable() {
			public void run() {
				updateSources();
				dialogChanged();
			}
		});
		
		UIUtil.newLabel(panel, Local.Name.toString());
		textName = UIUtil.newText(panel, "", new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		textName.setLayoutData(UIUtil.gridDataHoriz(1, true));
		
		groupSources = new LCGroup(panel, Local.Sources.toString());
		UIUtil.gridDataHorizFill(groupSources);
		
		setControl(panel);
		dialogChanged();
	}
	
	private void dialogChanged() {
		String typeID = (String)comboType.getSelectionData();
		if (typeID == null) {
			updateStatus(Local.Please_specify_a_content_type+".");
			return;
		}
		String name = textName.getText();
		if (name == null || name.length() == 0) {
			updateStatus(Local.The_name_cannot_be_empty+".");
			return;
		}
		List<String> selectedSources = getSelectedSources();
		if (selectedSources.isEmpty()) {
			updateStatus(Local.You_must_select_at_least_one_source.toString());
			return;
		}

		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}
	
	private List<String> getSelectedSources() {
		List<String> result = new LinkedList<String>();
		for (Button b : buttonSources)
			if (b.getSelection())
				result.add((String)b.getData());
		return result;
	}
	private void updateSources() {
		for (Control c : groupSources.getInnerControl().getChildren())
			c.dispose();
		buttonSources.clear();
		UIUtil.gridLayout(groupSources.getInnerControl(), 1);
		String typeID = (String)comboType.getSelectionData();
		if (typeID == null) return;
		for (InfoRetrieverPlugin pi : InfoRetrieverPluginRegistry.getRetrievers(typeID)) {
			Button b = UIUtil.newCheck(groupSources.getInnerControl(), pi.getIcon(), pi.getName(), new Listener<Pair<Boolean,InfoRetrieverPlugin>>() {
				public void fire(Pair<Boolean,InfoRetrieverPlugin> event) {
					dialogChanged();
				}
			}, pi);
			b.setData(pi.getSourceID());
			buttonSources.add(b);
		}
		UIUtil.resize(groupSources.getInnerControl());
		groupSources.layout(true, true);
	}
	
	@Override
	public IWizardPage getNextPage() {
		return null;
	}
	
	@Override
	public IWizardPage getPreviousPage() {
		return ((AddDataWizard)getWizard()).getStartingPage();
	}
	
	public boolean canFinish() {
		return isPageComplete();
	}
	public Result performFinish() {
		Result result = new Result();
		result.showRefreshAfterAdd = false;
		WorkProgress progress = new WorkProgress(Local.Add_data_from_internet.toString(), 10000, true);
		WorkProgressDialog dlg = new WorkProgressDialog(textName.getShell(), progress);
		try { result.db = new VirtualDataBase(progress, 100); }
		catch (InitializationException e) {
			ErrorDlg.exception(Local.Add_data.toString(), "Unable to create virtual database", EclipsePlugin.ID, e);
			dlg.close();
			return null;
		}
		String typeID = (String)comboType.getSelectionData();
		try {
			VirtualData d = (VirtualData)result.db.addData(textName.getText(), ContentType.getContentType(typeID), new LinkedList<DataSource>());
			List<String> sources = getSelectedSources();
			List<InfoRetrieverPlugin> plugins = new ArrayList<InfoRetrieverPlugin>(sources.size());
			for (String source : sources)
				plugins.add(InfoRetrieverPluginRegistry.getPlugin(source, typeID));
			InfoRetriever.retrieve(textName.getShell(), d, plugins, new FeedBackImpl(d), progress, 9900, true);
			if (!d.getContent().getInfo().getSources().isEmpty() && !progress.isCancelled())
				result.toAdd.add(d);
		} catch (CoreException e) {
			ErrorDlg.exception(Local.Add_data.toString(), "Unable to add data", EclipsePlugin.ID, e);
			progress.cancel();
		}
		if (progress.isCancelled()) {
			result.db.close();
			result = null;
		}
		dlg.close();
		return result;
	}
	public boolean finished() {
		return !MessageDialog.openQuestion(textName.getShell(), Local.Add_data.toString(), Local.MESSAGE_Add_Data_Continue_On_Internet.toString());
	}
	
}
