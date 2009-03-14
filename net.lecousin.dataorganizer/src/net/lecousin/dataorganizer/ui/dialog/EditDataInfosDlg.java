package net.lecousin.dataorganizer.ui.dialog;

import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.internal.EclipsePlugin;
import net.lecousin.dataorganizer.ui.control.RateEditPanel;
import net.lecousin.framework.time.DateTimeUtil;
import net.lecousin.framework.ui.eclipse.EclipseImages;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.buttonbar.OkIgnoreCancelButtonsPanel;
import net.lecousin.framework.ui.eclipse.control.text.lcml.LCMLText;
import net.lecousin.framework.ui.eclipse.dialog.FlatPopupMenu;
import net.lecousin.framework.ui.eclipse.dialog.MyDialog;
import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class EditDataInfosDlg extends MyDialog {

	public EditDataInfosDlg(Shell parent, Data data, List<Long> dates) {
		super(parent);
		this.data = data;
		this.rate = data.getRate();
		this.comment = data.getComment();
		this.dates = dates;
	}
	
	private Data data;
	private List<Long> dates;
	private List<Long> selectedDates = new LinkedList<Long>();
	private byte rate;
	private String comment;
	private boolean ok = false;
	private boolean ignore = false;
	
	private RateEditPanel rateEdit;
	private Text textComment;
	
	public List<Long> getSelectedDates() { return selectedDates; }
	public boolean isIgnoring() { return ignore; }
	
	@Override
	protected Composite createControl(Composite container) {
		Composite panel = new Composite(container, SWT.NONE);
		panel.setBackground(ColorUtil.getWhite());
		UIUtil.gridLayout(panel, 1);
		setMaxWidth(700);
		
		Composite header = UIUtil.newGridComposite(panel, 0, 0, 2);
		UIUtil.gridDataHorizFill(header);
		UIUtil.newImage(header, EclipseImages.getImage(EclipsePlugin.ID, "icons/writeNote.jpg"));
		LCMLText text = new LCMLText(header, false, false);
		String opened;
		if (dates.size() == 1)
			opened = Local.on__date__+" "+DateTimeUtil.getDateString(dates.get(0));
		else
			opened = "" + dates.size() + " " + Local.times;
		text.setText(Local.process(Local.OPENED_DATA_DIALOG_MESSAGE, data.getName(), opened));
		text.setLayoutData(UIUtil.gridDataHoriz(1, true));
		text.addLinkListener("data", new Runnable() {
			public void run() {
				DataLinkPopup.open(data, getShell(), FlatPopupMenu.Orientation.BOTTOM_RIGHT);
			}
		});

		if (dates.size() > 1) {
			UIUtil.newLabel(panel, Local.Please_select_the_dates_to_take_into_account.toString());
			ScrolledComposite scroll = new ScrolledComposite(panel, SWT.V_SCROLL);
			scroll.setBackground(panel.getBackground());
			Composite datePanel = UIUtil.newGridComposite(scroll, 0, 0, 1);
			for (long date : dates) {
				Button button = UIUtil.newCheck(datePanel, DateTimeUtil.getDateTimeString(date), null, null);
				button.setSelection(true);
				selectedDates.add(date);
				button.setData(date);
				button.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						Button b = (Button)e.widget;
						Long date = (Long)b.getData();
						if (b.getSelection())
							selectedDates.add(date);
						else
							selectedDates.remove(date);
					}
				});
			}
			scroll.setContent(datePanel);
			GridData gd = new GridData();
			Point size = datePanel.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
			datePanel.setSize(size);
			gd.heightHint = size.y > 200 ? 200 : size.y;
			scroll.setLayoutData(gd);
			Rectangle r = scroll.computeTrim(0, 0, size.x, size.y);
			scroll.setSize(r.width, r.height);
		} else {
			selectedDates.add(dates.get(0));
		}
		
		Composite body = UIUtil.newGridComposite(panel, 0, 0, 2);
		UIUtil.gridDataHorizFill(body);
		UIUtil.newLabel(body, Local.Rate.toString());
		rateEdit = new RateEditPanel(body, rate);
		UIUtil.newLabel(body, Local.Comment.toString());
		textComment = new Text(panel, SWT.WRAP | SWT.MULTI | SWT.BORDER);
		UIUtil.gridDataHorizFill(textComment).heightHint = 100;
		textComment.setText(comment != null ? comment : "");
		
		UIUtil.newSeparator(panel, true, true);
		new OkIgnoreCancelButtonsPanel(panel, true) {
			@Override
			protected boolean handleOk() {
				rate = rateEdit.getRate();
				comment = textComment.getText();
				if (comment.length() == 0)
					comment = null;
				ok = true;
				ignore = false;
				return true;
			}
			@Override
			protected boolean handleCancel() {
				ok = false;
				ignore = false;
				return true;
			}
			@Override
			protected boolean handleIgnore() {
				ok = false;
				ignore = true;
				return true;
			}
		}.centerAndFillInGrid();
		
		return panel;
	}
	
	public boolean open() {
		super.open(Local.Data_opened.toString(), MyDialog.FLAGS_MODAL_DIALOG);
		return ok;
	}
	
	public byte getRate() { return rate; }
	public String getComment() { return comment; }
}
