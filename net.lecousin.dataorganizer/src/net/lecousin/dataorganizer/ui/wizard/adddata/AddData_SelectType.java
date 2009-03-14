package net.lecousin.dataorganizer.ui.wizard.adddata;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.database.content.ContentType;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.Radio;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class AddData_SelectType extends WizardPage {

	public AddData_SelectType() {
		super(Local.Select_how_you_want_to_add_data.toString());
		setTitle(Local.Select_how_you_want_to_add_data.toString());
		setDescription(Local.Select_how_you_want_to_add_data.toString());
	}
	
	private Radio radioAddType;
	private Combo comboSingleType;
	
	public void createControl(Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);
		UIUtil.gridLayout(panel, 1);
		radioAddType = new Radio(panel, false);
		comboSingleType = new Combo(radioAddType, SWT.BORDER);
		radioAddType.addOption("single", new Control[] { UIUtil.newLabel(radioAddType, Local.Add_a_data_of_type.toString()), comboSingleType });
		for (ContentType type : ContentType.getAvailableTypes())
			comboSingleType.add(type.getName());
		radioAddType.addOption("folder", Local.Add_data_from_a_folder+" ("+Local.or_any_URI+")");
		setControl(panel);
		
		radioAddType.addSelectionChangedListener(new Listener<String>() {
			public void fire(String event) {
				dialogChanged();
			}
		});
		comboSingleType.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
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
			if (comboSingleType.getText().length() == 0) {
				updateStatus(Local.You_must_select_a_data_type.toString());
				return;
			}
		} else if (option.equals("folder")) {
			
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
			int i = comboSingleType.getSelectionIndex();
			if (i < 0) return null;
			ContentType type = null;
			for (ContentType t : ContentType.getAvailableTypes()) {
				if (i == 0) { type = t; break; }
				i--;
			}
			if (type == null) return null;
			return ((AddDataWizard)getWizard()).getTypePage(type.getID());
		}
		if (option.equals("folder"))
			return ((AddDataWizard)getWizard()).getFolderPage();
		return null;
	}
	
	@Override
	public IWizardPage getPreviousPage() {
		return null;
	}
}
