package net.lecousin.dataorganizer.ui.control;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.database.refresh.RefreshOptions;
import net.lecousin.framework.Pair;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.Radio;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class RefreshOptionsControl extends Composite {

	public RefreshOptionsControl(Composite parent, RefreshOptions options) {
		super(parent, SWT.NONE);
		this.options = options;
		setBackground(parent.getBackground());
		UIUtil.gridLayout(this, 1);
	
		boolean checked = options.getDataContentIfNotYetDone || options.refreshAllDataContent;
		UIUtil.newCheck(this, Local.Load_information_from_the_data_sources.toString(), loadInfo.checkListener, null).setSelection(checked);
		loadInfo.checked = checked;
		Radio radio = UIUtil.newRadio(this, new String[] { Local.Only_if_not_yet_done.toString(), Local.Always.toString() }, loadInfo.radioListener, null);
		UIUtil.indentOnGrid(radio, 10);
		radio.setSelection(options.refreshAllDataContent ? Local.Always.toString() : options.getDataContentIfNotYetDone ? Local.Only_if_not_yet_done.toString() : null);
		
		UIUtil.newCheck(this, Local.Try_to_relocate_sources_if_necessary.toString(), new RelocateSources(), null).setSelection(options.tryToRelocateDataSourceIfNecessary);

		UIUtil.newCheck(this, Local.Retrieve_information_from_Internet.toString(), new RetrieveInfo(), null).setSelection(options.retrieveInfoFromInternet);
	}
	
	private RefreshOptions options;
	
	private LoadInformation loadInfo = new LoadInformation();
	
	private class LoadInformation {
		boolean checked = false;
		String sel = null;
		public Listener<Pair<Boolean,Object>> checkListener = new Listener<Pair<Boolean,Object>>() {
			public void fire(Pair<Boolean, Object> event) {
				checked = event.getValue1();
				refresh();
			}
		};
		public Listener<Pair<String,Object>> radioListener = new Listener<Pair<String,Object>>() {
			public void fire(Pair<String, Object> event) {
				sel = event.getValue1();
				refresh();
			}
		};
		private void refresh() {
			if (checked) {
				if (sel != null) {
					if (sel.equals(Local.Only_if_not_yet_done)) {
						options.getDataContentIfNotYetDone = true;
						options.refreshAllDataContent = false;
					} else {
						options.getDataContentIfNotYetDone = false;
						options.refreshAllDataContent = true;
					}
				}
			} else {
				options.getDataContentIfNotYetDone = false;
				options.refreshAllDataContent = false;
			}
		}
	}
	
	private class RelocateSources implements Listener<Pair<Boolean,Object>> {
		public void fire(Pair<Boolean, Object> event) {
			options.tryToRelocateDataSourceIfNecessary = event.getValue1();
		}
	}

	private class RetrieveInfo implements Listener<Pair<Boolean,Object>> {
		public void fire(Pair<Boolean, Object> event) {
			options.retrieveInfoFromInternet = event.getValue1();
		}
	}
}
