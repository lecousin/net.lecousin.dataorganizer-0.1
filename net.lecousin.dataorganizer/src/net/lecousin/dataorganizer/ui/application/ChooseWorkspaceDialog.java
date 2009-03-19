package net.lecousin.dataorganizer.ui.application;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.internal.EclipsePlugin;
import net.lecousin.framework.Pair;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.ui.eclipse.EclipseImages;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.text.lcml.LCMLText;
import net.lecousin.framework.ui.eclipse.dialog.MyDialog;
import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;
import net.lecousin.framework.xml.XmlUtil;
import net.lecousin.framework.xml.XmlWriter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Element;

public class ChooseWorkspaceDialog extends MyDialog {

	public ChooseWorkspaceDialog(Display display) {
		super(findShell(display));
		File config = new File(net.lecousin.framework.application.Application.deployPath, "recentWorkspaces.xml");
		boolean ok = false;
		if (config.exists()) {
			try { 
				Element root = XmlUtil.loadFile(config);
				for (Element e : XmlUtil.get_childs_element(root, "workspace"))
					recent.add(new Pair<String,Integer>(e.getAttribute("location"), Integer.parseInt(e.getAttribute("language"))));
				ok = true;
			} catch (Throwable t) {
			}
		}
		if (!ok) {
			recent.add(new Pair<String,Integer>(System.getProperty("user.home") + "/database", 0));
		}
	}
	private static Shell findShell(Display display) {
//		try {
//			return WorkbenchPlugin.getSplashShell(display);
//		} catch (Throwable t) {
			return new Shell(display, SWT.PRIMARY_MODAL);
//		}
	}
	
	private static String[] _text = new String[] {
		"DataOrganizer uses a database to store your data information.<br/>" +
		"You can have several databases, i.e. one for your movies and music albums, another for your family videos and photos...<br/>" +
		"Choose a database and a language for this session.",
		"DataOrganizer utilise une base de données pour stocker les informations de vos données.<br/>" +
		"Vous pouvez avoir plusieurs base de données, par exemple une pour vos films et albums de musique, une autre pour vos vidéos de famille et vos photos...<br/>" +
		"Choisissez une base de données et une langue pour cette session."
	};
	private static final String[] _database = new String[] {
		"DataBase",
		"Base de données"
	};
	private static final String[] _browse = new String[] {
		"Browse...",
		"Parcourir..."
	};
	private static final String[] _language = new String[] {
		"Language",
		"Langue"
	};
	private static final String[] _ok = new String[] {
		"Ok",
		"Ok"
	};
	private static final String[] _cancel = new String[] {
		"Cancel",
		"Annuler"
	};
	
	private List<Pair<String,Integer>> recent = new LinkedList<Pair<String,Integer>>();
	
	@Override
	protected Composite createControl(Composite container) {
		container.setBackground(ColorUtil.getWhite());
		Composite panel = UIUtil.newGridComposite(container, 0, 0, 1);

		int lang = recent.get(0).getValue2();
		Composite header = UIUtil.newGridComposite(panel, 2, 2, 2);
		UIUtil.gridDataHorizFill(header);
		UIUtil.newImage(header, EclipseImages.getImage(EclipsePlugin.ID, "cd-dvd.jpg"));
		text = new LCMLText(header, false, false);
		text.setText(_text[lang]);
		GridData gd = new GridData();
		gd.widthHint = 350;
		text.setLayoutData(gd);
		
		Composite body = UIUtil.newGridComposite(panel, 2, 2, 3);
		UIUtil.gridDataHorizFill(body);
		labelDataBase = UIUtil.newLabel(body, _database[lang]);
		comboDataBase = new Combo(body, SWT.DROP_DOWN);
		for (Pair<String,Integer> p : recent)
			comboDataBase.add(p.getValue1());
		comboDataBase.select(0);
		comboDataBase.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				refresh(true);
			}
		});
		comboDataBase.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				refresh(true);
			}
		});
		comboDataBase.setLayoutData(UIUtil.gridDataHoriz(1, true));
		buttonBrowse = UIUtil.newButton(body, _browse[lang], new Listener<Object>() {
			public void fire(Object event) {
				DirectoryDialog dlg = new DirectoryDialog(getShell());
				String result = dlg.open();
				if (result != null) {
					comboDataBase.setText(result);
					refresh(true);
				}
			}
		}, null);
		labelLanguage = UIUtil.newLabel(body, _language[lang]);
		comboLanguage = new Combo(body, SWT.DROP_DOWN | SWT.READ_ONLY);
		comboLanguage.add("English");
		comboLanguage.add("Français");
		comboLanguage.select(lang);
		comboLanguage.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				refresh(false);
			}
		});
		
		UIUtil.newSeparator(panel, true, true);

		Composite footer = UIUtil.newGridComposite(panel, 2, 2, 2, 40, 0);
		gd = new GridData();
		gd.horizontalAlignment = SWT.CENTER;
		footer.setLayoutData(gd);
		buttonOk = new Button(footer, SWT.PUSH);
		buttonOk.setText(_ok[lang]);
		buttonCancel = new Button(footer, SWT.PUSH);
		buttonCancel.setText(_cancel[lang]);
		buttonOk.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				selectedDir = comboDataBase.getText();
				if (selectedDir.length() == 0) return;
				selectedLang = comboLanguage.getSelectionIndex();
				if (selectedLang < 0) return;
				ok = true;
				close();
			}
		});
		buttonCancel.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				ok = false;
				close();
			}
		});
		
		return panel;
	}
	
	private LCMLText text;
	private Label labelDataBase, labelLanguage;
	private Combo comboDataBase, comboLanguage;
	private Button buttonBrowse, buttonOk, buttonCancel;
	private boolean ok;
	private int selectedLang;
	private String selectedDir;
	
	public boolean open() {
		super.open("DataOrganizer", MyDialog.FLAGS_MODAL_DIALOG);
		
		if (ok) {
			File config = new File(net.lecousin.framework.application.Application.deployPath, "recentWorkspaces.xml");
			XmlWriter xml = new XmlWriter();
			xml.openTag("dataorganizer_workpsaces");
			xml.openTag("workspace").addAttribute("location", selectedDir).addAttribute("language", selectedLang).closeTag();
			for (Pair<String,Integer> p : recent) {
				if (p.getValue1().equals(selectedDir)) continue;
				xml.openTag("workspace").addAttribute("location", p.getValue1()).addAttribute("language", p.getValue2()).closeTag();
			}
			xml.closeTag();
			try {
				xml.writeToFile(config.getAbsolutePath());
			} catch (IOException e) {
				// skip
			}
		}
		
		return ok;
	}
	public String getSelectedDir() { return selectedDir; }
	public int getSelectedLang() { return selectedLang; }
	
	private void refresh(boolean fromText) {
		String loc = comboDataBase.getText();
		int index = -1;
		for (int i = 0; i < recent.size(); ++i)
			if (recent.get(i).getValue1().equals(loc)) {
				index = i;
				break;
			}
		int lang;
		if (index >= 0 && fromText) {
			lang = recent.get(index).getValue2();
			comboLanguage.select(lang);
		} else {
			lang = comboLanguage.getSelectionIndex();
			if (lang < 0) return;
		}
		
		text.setText(_text[lang]);
		labelDataBase.setText(_database[lang]);
		buttonBrowse.setText(_browse[lang]);
		labelLanguage.setText(_language[lang]);
		buttonOk.setText(_ok[lang]);
		buttonCancel.setText(_cancel[lang]);
		getDialogPanel().layout(true, true);
		resize();
	}
}
