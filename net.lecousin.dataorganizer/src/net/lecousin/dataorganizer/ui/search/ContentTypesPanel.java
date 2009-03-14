package net.lecousin.dataorganizer.ui.search;

import net.lecousin.dataorganizer.core.DataOrganizer;
import net.lecousin.dataorganizer.core.database.content.ContentType;
import net.lecousin.framework.event.Event;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class ContentTypesPanel extends Composite {

	public ContentTypesPanel(Composite parent) {
		super(parent, SWT.NONE);
		RowLayout layout = new RowLayout(SWT.HORIZONTAL);
		layout.wrap = true;
		setLayout(layout);
		for (ContentType type : ContentType.getAvailableTypes()) {
			Button button = new Button(this, SWT.CHECK);
			button.setText(type.getName());
			button.setData(type.getID());
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					Button b = (Button)e.widget;
					if (b.getSelection())
						typeAdded.fire((String)b.getData());
					else
						typeRemoved.fire((String)b.getData());
					DataOrganizer.search().getParameters().setContentType((String)b.getData(), b.getSelection());
				}
			});
		}
	}
	
	private Event<String> typeAdded = new Event<String>();
	private Event<String> typeRemoved = new Event<String>();
	
	public Event<String> typeAdded() { return typeAdded; }
	public Event<String> typeRemoved() { return typeRemoved; }
}
