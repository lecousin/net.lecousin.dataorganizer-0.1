package net.lecousin.dataorganizer.ui.search;

import java.util.Calendar;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.DataOrganizer;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.math.RangeLong;
import net.lecousin.framework.strings.StringUtil;
import net.lecousin.framework.time.DateTimeUtil;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.date.DateRangePanel;
import net.lecousin.framework.ui.eclipse.dialog.CalloutToolTip;
import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class MainCriteriaPanel extends Composite {

	public MainCriteriaPanel(Composite parent) {
		super(parent, SWT.NONE);
		setBackground(parent.getBackground());
		UIUtil.gridLayout(this, 3);
		
		Image help = SharedImages.getImage(SharedImages.icons.x16.basic.HELP);
		Text text;
		
		UIUtil.newLabel(this, Local.Name.toString());
		text = UIUtil.newText(this, "", new NameModified());
		text.setLayoutData(UIUtil.gridDataHoriz(1, true));
		UIUtil.newImageButton(this, help, new HelpName(), text);
		
		UIUtil.newLabel(this, Local.Rate.toString());
		text = UIUtil.newText(this, "0-20", new RateModified());
		text.setLayoutData(UIUtil.gridDataHoriz(1, true));
		UIUtil.newImageButton(this, help, new HelpRate(), text);
		
		UIUtil.newLabel(this, Local.Added.toString());
		panelAdded = new DateRangePanel(this);
		panelAdded.setLayoutData(UIUtil.gridDataHoriz(1, true));
		UIUtil.newImageButton(this, help, new HelpAdded(), panelAdded);

		UIUtil.newLabel(this, Local.Opened.toString());
		panelOpened = new DateRangePanel(this);
		panelOpened.setLayoutData(UIUtil.gridDataHoriz(1, true));
		UIUtil.newImageButton(this, help, new HelpOpened(), panelOpened);
		
		// configure panelAdded
		refreshAdded();
		panelAdded.rangeChanged().addListener(new AddedModified());
		
		// configure panelOpened
		refreshOpened();
		panelOpened.rangeChanged().addListener(new OpenedModified());
		
		// register events
		DataOrganizer.database().dataAdded().addListener(dataAddedListener);
		DataOrganizer.database().dataRemoved().addListener(dataRemovedListener);
		DataOrganizer.database().dataChanged().addListener(dataChangedListener);
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				DataOrganizer.database().dataAdded().removeListener(dataAddedListener);
				DataOrganizer.database().dataRemoved().removeListener(dataRemovedListener);
				DataOrganizer.database().dataChanged().removeListener(dataChangedListener);
			}
		});
	}
	
	private DateRangePanel panelAdded;
	private DateRangePanel panelOpened;
	private DataAdded dataAddedListener = new DataAdded();
	private DataRemoved dataRemovedListener = new DataRemoved();
	private DataChanged dataChangedListener = new DataChanged();
	
	private void refreshAdded() {
		long min = 0, max = 0;
		for (Data data : DataOrganizer.database().getAllData()) {
			long date = data.getDateAdded();
			if (min == 0 || date < min) min = date;
			if (max == 0 || date > max) max = date;
		}
		panelAdded.setMinimum(min);
		panelAdded.setMaximum(max);
	}
	private void refreshOpened() {
		long min = 0, max = 0;
		for (Data data : DataOrganizer.database().getAllData()) {
			for (Long date : data.getViews()) {
				if (min == 0 || date < min) min = date;
				if (max == 0 || date > max) max = date;
			}
		}
		panelOpened.setMinimum(min);
		panelOpened.setMaximum(max == 0 ? Long.MAX_VALUE : max);
	}
	
	private class NameModified implements ModifyListener {
		public void modifyText(ModifyEvent e) {
			String name = ((Text)e.widget).getText();
			DataOrganizer.search().getParameters().setName(name);
		}
	}
	private class RateModified implements ModifyListener {
		public void modifyText(ModifyEvent e) {
			Text text = (Text)e.widget;
			String s = text.getText();
			try { 
				DataOrganizer.search().getParameters().setRateRange(StringUtil.toRangeInteger(s)); 
				text.setBackground(ColorUtil.getWhite());
			} catch (NumberFormatException ex) {
				text.setBackground(ColorUtil.getOrange());
			}
		}
	}
	private class AddedModified implements Listener<RangeLong> {
		public void fire(RangeLong event) {
			Calendar c = Calendar.getInstance();
			if (event.min != 0) {
				c.setTimeInMillis(event.min);
				DateTimeUtil.resetHours(c);
				event.min = c.getTimeInMillis();
			}
			if (event.max != 0) {
				c.setTimeInMillis(event.max);
				DateTimeUtil.resetHours(c);
				c.add(Calendar.DAY_OF_MONTH, 1);
				event.max = c.getTimeInMillis();
			}
			DataOrganizer.search().getParameters().setDateAddedRange(event);
		}
	}
	private class OpenedModified implements Listener<RangeLong> {
		public void fire(RangeLong event) {
			Calendar c = Calendar.getInstance();
			if (event.min != 0) {
				c.setTimeInMillis(event.min);
				DateTimeUtil.resetHours(c);
				event.min = c.getTimeInMillis();
			}
			if (event.max != 0) {
				c.setTimeInMillis(event.max);
				DateTimeUtil.resetHours(c);
				c.add(Calendar.DAY_OF_MONTH, 1);
				event.max = c.getTimeInMillis();
			}
			DataOrganizer.search().getParameters().setDateOpenedRange(event);
		}
	}
	
	private class HelpName implements Listener<Text> {
		public void fire(Text text) {
			CalloutToolTip.open(text, CalloutToolTip.Orientation.TOP_RIGHT, Local.HELP_Search_Name.toString(), 5000, -1);
			text.setFocus();
		}
	}
	private class HelpRate implements Listener<Text> {
		public void fire(Text text) {
			CalloutToolTip.open(text, CalloutToolTip.Orientation.TOP_RIGHT, Local.HELP_Search_Rate.toString(), 5000, -1);
			text.setFocus();
		}
	}
	
	private class HelpAdded implements Listener<DateRangePanel> {
		public void fire(DateRangePanel event) {
			CalloutToolTip.open(event, CalloutToolTip.Orientation.TOP_RIGHT, Local.HELP_Search_Added.toString(), 5000, -1);
			event.setFocus();
		}
	}
	
	private class HelpOpened implements Listener<DateRangePanel> {
		public void fire(DateRangePanel event) {
			CalloutToolTip.open(event, CalloutToolTip.Orientation.TOP_RIGHT, Local.HELP_Search_Opened.toString(), 5000, -1);
			event.setFocus();
		}
	}

	private class DataAdded implements Listener<Data> {
		public void fire(Data event) {
			long date = event.getDateAdded();
			if (date != 0 && date < panelAdded.getMinimum())
				panelAdded.setMinimum(date);
			else if (date != 0 && date > panelAdded.getMaximum())
				panelAdded.setMaximum(date);
		}
	}
	private class DataRemoved implements Listener<Data> {
		public void fire(Data event) {
			refreshAdded();
			refreshOpened();
		}
	}
	private class DataChanged implements Listener<Data> {
		public void fire(Data event) {
			refreshOpened();
		}
	}
}
