package net.lecousin.dataorganizer.ui.control;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.database.refresh.RefreshOptions;
import net.lecousin.dataorganizer.core.database.refresh.RefreshOptions.GetDataContent;
import net.lecousin.dataorganizer.core.database.refresh.RefreshOptions.RetrieveInfoFromInternet;
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
	
		UIUtil.newCheck(this, Local.Load_information_from_the_data_sources.toString(), loadInfo.checkListener, null).setSelection(options.getDataContent != null);
		loadInfo.checked = options.getDataContent != null;
		radioDataContent = UIUtil.newRadio(this, new String[] { Local.Only_if_not_yet_done.toString(), Local.Always.toString() }, loadInfo.radioListener, null);
		UIUtil.indentOnGrid(radioDataContent, 10);
		if (options.getDataContent != null)
			switch (options.getDataContent) {
			case ALL: radioDataContent.setSelection(Local.Always.toString()); break;
			case IF_NOT_YET_DONE: radioDataContent.setSelection(Local.Only_if_not_yet_done.toString()); break;
			}
		
		UIUtil.newCheck(this, Local.Try_to_relocate_sources_if_necessary.toString(), new RelocateSources(), null).setSelection(options.tryToRelocateDataSourceIfNecessary);

		UIUtil.newCheck(this, Local.Retrieve_information_from_Internet.toString(), retrieveInfo.checkListener, null).setSelection(options.retrieveInfoFromInternet != null);
		retrieveInfo.checked = options.retrieveInfoFromInternet != null;
		radioRetrieveInternet = UIUtil.newRadio(this, new String[] { Local.Only_from_missing_sources.toString(), Local.Always.toString() }, retrieveInfo.radioListener, null);
		UIUtil.indentOnGrid(radioRetrieveInternet, 10);
		if (options.retrieveInfoFromInternet != null)
			switch (options.retrieveInfoFromInternet) {
			case ALL: radioRetrieveInternet.setSelection(Local.Always.toString()); break;
			case MISSING: radioRetrieveInternet.setSelection(Local.Only_from_missing_sources.toString()); break;
			}
	}
	
	private RefreshOptions options;
	
	private LoadInformation loadInfo = new LoadInformation();
	private RetrieveInformation retrieveInfo = new RetrieveInformation();
	private Radio radioDataContent;
	private Radio radioRetrieveInternet;
	
	private class LoadInformation {
		boolean checked = false;
		String sel = null;
		public Listener<Pair<Boolean,Object>> checkListener = new Listener<Pair<Boolean,Object>>() {
			public void fire(Pair<Boolean, Object> event) {
				checked = event.getValue1();
				if (sel == null)
					radioDataContent.setSelection(Local.Only_if_not_yet_done.toString());
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
					if (sel.equals(Local.Only_if_not_yet_done.toString()))
						options.getDataContent = GetDataContent.IF_NOT_YET_DONE;
					else
						options.getDataContent = GetDataContent.ALL;
				} else
					options.getDataContent = null;
			} else
				options.getDataContent = null;
		}
	}
	private class RetrieveInformation {
		boolean checked = false;
		String sel = null;
		public Listener<Pair<Boolean,Object>> checkListener = new Listener<Pair<Boolean,Object>>() {
			public void fire(Pair<Boolean, Object> event) {
				checked = event.getValue1();
				if (sel == null)
					radioRetrieveInternet.setSelection(Local.Only_from_missing_sources.toString());
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
					if (sel.equals(Local.Only_from_missing_sources.toString()))
						options.retrieveInfoFromInternet = RetrieveInfoFromInternet.MISSING;
					else
						options.retrieveInfoFromInternet = RetrieveInfoFromInternet.ALL;
				} else
					options.retrieveInfoFromInternet = null;
			} else
				options.retrieveInfoFromInternet = null;
		}
	}
	
	private class RelocateSources implements Listener<Pair<Boolean,Object>> {
		public void fire(Pair<Boolean, Object> event) {
			options.tryToRelocateDataSourceIfNecessary = event.getValue1();
		}
	}
}
