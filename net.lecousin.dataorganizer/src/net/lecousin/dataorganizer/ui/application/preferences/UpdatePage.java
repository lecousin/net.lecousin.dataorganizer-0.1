package net.lecousin.dataorganizer.ui.application.preferences;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.DataOrganizer;
import net.lecousin.dataorganizer.core.DataOrganizerConfig;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.UIUtil;

import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Spinner;

public class UpdatePage extends PreferencePage implements IPreferencePage {

	public UpdatePage() {
		super(Local.Update.toString(), SharedImages.getImageDescriptor(SharedImages.icons.x16.basic.CONNECTED_GREEN_GREEN));
		setDescription(Local.MESSAGE_Preference_Update.toString());
		config = DataOrganizer.config(); 
		update_check_frequency = config.update_check_frequency;
	}
	
	private DataOrganizerConfig config;
	private int update_check_frequency;
	private Spinner update_check_frequency_spinner;

	@Override
	protected Control createContents(Composite parent) {
		Composite panel = UIUtil.newGridComposite(parent, 0, 0, 2);
		Composite tmpPanel;

		UIUtil.newLabel(panel, Local.Update_check_frequency.toString(), true, false);
		tmpPanel = UIUtil.newGridComposite(panel, 0, 0, 2);
		update_check_frequency_spinner = UIUtil.newSpinner(tmpPanel, 1, 90, 1, update_check_frequency, new Listener<Integer>() {
			public void fire(Integer event) {
				update_check_frequency = event;
			}
		}, true);
		UIUtil.newLabel(tmpPanel, Local.days.toString());

		return panel;
	}

	@Override
	public boolean performOk() {
		config.update_check_frequency = update_check_frequency;
		config.save();
		return true;
	}
	
	@Override
	protected void performDefaults() {
		update_check_frequency_spinner.setSelection(7);
		update_check_frequency = 7;
		super.performDefaults();
	}

}
