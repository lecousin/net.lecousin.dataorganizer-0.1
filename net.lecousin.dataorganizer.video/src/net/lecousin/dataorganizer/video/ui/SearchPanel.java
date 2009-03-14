package net.lecousin.dataorganizer.video.ui;

import net.lecousin.dataorganizer.core.DataOrganizer;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.video.Local;
import net.lecousin.dataorganizer.video.VideoContentType;
import net.lecousin.dataorganizer.video.VideoDataType;
import net.lecousin.dataorganizer.video.VideoParameters;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.math.RangeLong;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.date.TimeRangePanel;
import net.lecousin.framework.ui.eclipse.dialog.CalloutToolTip;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class SearchPanel extends Composite {

	public SearchPanel(Composite parent) {
		super(parent, SWT.NONE);
		setBackground(parent.getBackground());
		UIUtil.gridLayout(this, 3);

		Image help = SharedImages.getImage(SharedImages.icons.x16.basic.HELP);
		params = (VideoParameters)DataOrganizer.search().getParameters().getParameters(VideoContentType.VIDEO_TYPE);

		UIUtil.newLabel(this, Local.Duration.toString());
		panelDuration = new TimeRangePanel(this);
		panelDuration.setLayoutData(UIUtil.gridDataHoriz(1, true));
		UIUtil.newImageButton(this, help, new HelpDuration(), panelDuration);
		
		UIUtil.newLabel(this, Local.Casting.toString());
		Text text = UIUtil.newText(this, params.getCasting(), new CastingModified());
		text.setLayoutData(UIUtil.gridDataHoriz(1, true));
		UIUtil.newImageButton(this, help, new HelpCasting(), text);
		
		// configure panelDuration
		refreshDuration();
		panelDuration.setRange(params.getRangeDuration());
		panelDuration.rangeChanged().addListener(new DurationModified());
		
		
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
	
	private TimeRangePanel panelDuration;
	private VideoParameters params;
	private DataAdded dataAddedListener = new DataAdded();
	private DataRemoved dataRemovedListener = new DataRemoved();
	private DataChanged dataChangedListener = new DataChanged();
	
	private void refreshDuration() {
		long min = 0, max = 0;
		for (Data data : DataOrganizer.database().getAllData()) {
			if (!data.getContentType().getID().equals(VideoContentType.VIDEO_TYPE)) continue;
			long duration = ((VideoDataType)data.getContent()).getDuration();
			if (duration <= 0) continue;
			if (min == 0 || duration < min) min = duration;
			if (max == 0 || duration > max) max = duration;
		}
		panelDuration.setMinimum(min);
		panelDuration.setMaximum(max == 0 ? Long.MAX_VALUE : max);
	}
	
	private class DurationModified implements Listener<RangeLong> {
		public void fire(RangeLong event) {
			params.setRangeDuration(event);
		}
	}
	private class CastingModified implements ModifyListener {
		public void modifyText(ModifyEvent e) {
			params.setCasting(((Text)e.widget).getText());
		}
	}

	private class HelpDuration implements Listener<TimeRangePanel> {
		public void fire(TimeRangePanel event) {
			CalloutToolTip.open(event, CalloutToolTip.Orientation.TOP_RIGHT, Local.HELP_Search_Duration.toString(), 5000, -1);
			event.setFocus();
		}
	}
	private class HelpCasting implements Listener<Text> {
		public void fire(Text event) {
			CalloutToolTip.open(event, CalloutToolTip.Orientation.TOP_RIGHT, Local.HELP_Search_Casting.toString(), 5000, -1);
			event.setFocus();
		}
	}
	
	
	private class DataAdded implements Listener<Data> {
		public void fire(Data event) {
			if (!event.getContentType().getID().equals(VideoContentType.VIDEO_TYPE)) return;
			long duration = ((VideoDataType)event.getContent()).getDuration();
			if (duration <= 0) return;
			if (duration < panelDuration.getMinimum())
				panelDuration.setMinimum(duration);
			else if (duration > panelDuration.getMaximum())
				panelDuration.setMaximum(duration);
		}
	}
	private class DataRemoved implements Listener<Data> {
		public void fire(Data event) {
			if (!event.getContentType().getID().equals(VideoContentType.VIDEO_TYPE)) return;
			refreshDuration();
		}
	}
	private class DataChanged implements Listener<Data> {
		public void fire(Data event) {
			if (!event.getContentType().getID().equals(VideoContentType.VIDEO_TYPE)) return;
			refreshDuration();
		}
	}
}
