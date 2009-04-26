package net.lecousin.dataorganizer.ui.wizard.adddata;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.database.content.ContentType;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.LCCombo;
import net.lecousin.framework.ui.eclipse.control.Radio;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class AddData_SelectType extends WizardPage {

	public AddData_SelectType() {
		super(Local.Select_how_you_want_to_add_data.toString());
		setTitle(Local.Select_how_you_want_to_add_data.toString());
		setDescription(Local.Select_how_you_want_to_add_data.toString());
	}
	
	private Radio radioAddType;
	private LCCombo comboSingleType;
	
	public void createControl(Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);
		UIUtil.gridLayout(panel, 1);
		radioAddType = new Radio(panel, false);
		comboSingleType = new LCCombo(radioAddType, null);
		comboSingleType.setEditable(false);
		radioAddType.addOption("single", new Control[] { UIUtil.newLabel(radioAddType, Local.Add_a_data_of_type.toString()), comboSingleType });
		for (ContentType type : ContentType.getAvailableTypes())
			if (((AddDataWizard)getWizard()).getTypePage(type.getID()) != null)
				comboSingleType.addItem(type.getIcon(), type.getName(), type.getID());
		radioAddType.addOption("folder", Local.Add_data_from_a_folder+" ("+Local.or_any_URI+")");
		radioAddType.addOption("internet", Local.Add_data_from_internet.toString());
		setControl(panel);
		
		radioAddType.addSelectionChangedListener(new Listener<String>() {
			public void fire(String event) {
				dialogChanged();
			}
		});
		comboSingleType.selectionEvent().addFireListener(new Runnable() {
			public void run() {
				dialogChanged();
			}
		});
		dialogChanged();
	}
	
	private void dialogChanged() {
		String option = radioAddType.getSelection();
		if (option == null) {
			updateStatus(Local.You_must_select_the_way_you_want_to_add_data.toString());
			return;
		}
		if (option.equals("single")) {
			String type = (String)comboSingleType.getSelectionData();
			if (type == null) {
				updateStatus(Local.You_must_select_a_data_type.toString());
				return;
			}
		} else if (option.equals("folder")) {
			
		} else if (option.equals("internet")) {
			
		}
		
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}
	
	@Override
	public IWizardPage getNextPage() {
		String option = radioAddType.getSelection();
		if (option.equals("single")) {
			String typeID = (String)comboSingleType.getSelectionData();
			if (typeID == null) return null;
			return ((AddDataWizard)getWizard()).getTypePage(typeID);
		}
		if (option.equals("folder"))
			return ((AddDataWizard)getWizard()).getFolderPage();
		if (option.equals("internet"))
			return ((AddDataWizard)getWizard()).getInternetPage();
		return null;
	}
	
	@Override
	public IWizardPage getPreviousPage() {
		return null;
	}
}
