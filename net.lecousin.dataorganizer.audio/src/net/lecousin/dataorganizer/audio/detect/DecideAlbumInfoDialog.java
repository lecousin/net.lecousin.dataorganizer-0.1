package net.lecousin.dataorganizer.audio.detect;

import java.net.URLDecoder;
import java.util.List;
import java.util.Set;

import net.lecousin.dataorganizer.audio.Local;
import net.lecousin.dataorganizer.audio.internal.EclipsePlugin;
import net.lecousin.framework.Pair;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.files.audio.AudioFile;
import net.lecousin.framework.files.audio.AudioFileInfo;
import net.lecousin.framework.ui.eclipse.EclipseImages;
import net.lecousin.framework.ui.eclipse.EclipseWorkbenchUtil;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.Radio;
import net.lecousin.framework.ui.eclipse.control.ValidationControl;
import net.lecousin.framework.ui.eclipse.control.buttonbar.OkCancelButtonsPanel;
import net.lecousin.framework.ui.eclipse.control.text.lcml.LCMLText;
import net.lecousin.framework.ui.eclipse.dialog.ErrorDlg;
import net.lecousin.framework.ui.eclipse.dialog.FlatDialog;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.FileStoreEditorInput;

public class DecideAlbumInfoDialog extends FlatDialog {

	public DecideAlbumInfoDialog(Shell shell, List<AudioFile> tracks, Set<String> albumNames, Set<String> artistNames, Set<Integer> years, IFileStore rootDir) {
		super(shell, Local.Create_Music_Album.toString(), true, true);
		this.albumNames = albumNames;
		this.artistNames = artistNames;
		this.years = years;
		this.rootDir = rootDir;
		this.tracks = tracks;
	}
	
	private List<AudioFile> tracks;
	private Set<String> albumNames;
	private Set<String> artistNames;
	private Set<Integer> years;
	private IFileStore rootDir;
	
	private Radio radioAlbum, radioArtist, radioYear;
	private Text textAlbum, textArtist, textYear;
	private ValidationControl validation;
	private OkCancelButtonsPanel buttons;
	
	private boolean resultOK = false;
	private String resultAlbum = null;
	private String resultArtist = null;
	private Integer resultYear = null;
	
	@Override
	protected void createContent(Composite container) {
		UIUtil.gridLayout(container, 1);
		
		String msg = "";
		if (albumNames != null)
			msg += Local.the_album_name.toString();
		if (artistNames != null) {
			if (msg.length() > 0) msg += ", ";
			msg += Local.the_artist_name.toString();
		}
		if (years != null) {
			if (msg.length() > 0) msg += ", ";
			msg += Local.the_year_of_the_album.toString();
		}
		
		Composite messagePanel = UIUtil.newGridComposite(container, 0, 0, 2);
		UIUtil.newImage(messagePanel, EclipseImages.getImage(EclipsePlugin.ID, "images/audio_128.gif"));
		LCMLText message = new LCMLText(messagePanel, false, false);
		message.setText(Local.process(Local.MESSAGE_Album_Decision, "<a href=\"dir\">"+rootDir.toString()+"</a>", msg));
		message.getControl().setLayoutData(UIUtil.gridDataHoriz(1, true));
		message.addLinkListener("dir", new Runnable() {
			public void run() {
				try {
					EclipseWorkbenchUtil.getPage().openEditor(
							new FileStoreEditorInput(rootDir), 
							IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID, 
							true);
				} catch (PartInitException e) {
					ErrorDlg.exception("Error", "Unable to open directory", EclipsePlugin.ID, e);
				}
			}
		});
		Composite contentPanel = UIUtil.newGridComposite(container, 0, 0, 4);
		UIUtil.gridDataHorizFill(contentPanel);
		UIUtil.newLabel(contentPanel, Local.File.toString());
		UIUtil.newLabel(contentPanel, Local.Album.toString());
		UIUtil.newLabel(contentPanel, Local.Artist.toString());
		UIUtil.newLabel(contentPanel, Local.Year.toString());
		for (AudioFile file : tracks) {
			String s = file.getURI().toString();
			int i = s.lastIndexOf('/');
			if (i >= 0) s = s.substring(i+1);
			s = URLDecoder.decode(s).trim();
			UIUtil.newLabel(contentPanel, s);
			AudioFileInfo ai = file.getInfo();
			UIUtil.newLabel(contentPanel, ai != null && ai.getAlbum() != null ? ai.getAlbum() : "");
			UIUtil.newLabel(contentPanel, ai != null && ai.getArtist() != null ? ai.getArtist() : "");
			UIUtil.newLabel(contentPanel, ai != null && ai.getYear() > 0 ? Integer.toString(ai.getYear()) : "");
		}
		Composite choicePanel = UIUtil.newGridComposite(container, 0, 0, (albumNames != null ? 1 : 0) + (artistNames != null ? 1 : 0) + (years != null ? 1 : 0));
		UIUtil.gridDataHorizFill(choicePanel);
		if (albumNames != null) UIUtil.newLabel(choicePanel, Local.Album+":");
		if (artistNames != null) UIUtil.newLabel(choicePanel, Local.Artist+":");
		if (years != null) UIUtil.newLabel(choicePanel, Local.Year+":");

		if (albumNames != null) {
			radioAlbum = new Radio(choicePanel, false);
			radioAlbum.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
			for (String name : albumNames)
				radioAlbum.addOption(name, name);
			Pair<Composite,Text> p = createAnother(radioAlbum, Local.Another_name.toString());
			textAlbum = p.getValue2();
			radioAlbum.addOption("/other", new Control[] { p.getValue1() });
			radioAlbum.addSelectionChangedListener(new Listener<String>() {
				public void fire(String event) {
					validate();
				}
			});
		}
		if (artistNames != null) {
			radioArtist = new Radio(choicePanel, false);
			radioArtist.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
			for (String name : artistNames)
				radioArtist.addOption(name, name);
			Pair<Composite,Text> p = createAnother(radioArtist, Local.Another_name.toString());
			textArtist = p.getValue2();
			radioArtist.addOption("/other", new Control[] { p.getValue1() });
			radioArtist.addOption("/no", Local.I_dont_know+".. "+Local.lets_it_without_an_artist_name_for_now);
			radioArtist.addSelectionChangedListener(new Listener<String>() {
				public void fire(String event) {
					validate();
				}
			});
		}
		if (years != null) {
			radioYear = new Radio(choicePanel, false);
			radioYear.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
			for (Integer year : years)
				radioYear.addOption(Integer.toString(year), Integer.toString(year));
			Pair<Composite,Text> p = createAnother(radioYear, Local.Another_year.toString());
			textYear = p.getValue2();
			radioYear.addOption("/other", new Control[] { p.getValue1() });
			radioYear.addOption("/no", Local.I_dont_know+".. "+Local.lets_it_without_a_year_for_now);
			radioYear.addSelectionChangedListener(new Listener<String>() {
				public void fire(String event) {
					validate();
				}
			});
		}
		validation = new ValidationControl(container);
		UIUtil.gridDataHorizFill(validation);
		UIUtil.newSeparator(container, true, true);
		buttons = new OkCancelButtonsPanel(container, true) {
			@Override
			protected boolean handleOk() {
				if (validation.isVisible()) return false;
				resultOK = true;
				if (radioAlbum != null) {
					resultAlbum = radioAlbum.getSelection();
					if (resultAlbum.equals("/other"))
						resultAlbum = textAlbum.getText();
					else if (resultAlbum.equals("/no"))
						resultAlbum = null;
				}
				if (radioArtist != null) {
					resultArtist = radioArtist.getSelection();
					if (resultArtist.equals("/other"))
						resultArtist = textArtist.getText();
					else if (resultArtist.equals("/no"))
						resultArtist = null;
				}
				if (radioYear != null) {
					String s = radioYear.getSelection();
					if (s.equals("/other"))
						resultYear = Integer.parseInt(textYear.getText());
					else if (s.equals("/no"))
						resultYear = null;
					else
						resultYear = Integer.parseInt(s);
				}
				return true;
			}
			@Override
			protected boolean handleCancel() {
				resultOK = false;
				return true;
			}
		};
		buttons.centerAndFillInGrid();
		validate();
	}
	
	private Pair<Composite,Text> createAnother(Radio radio, String label) {
		Composite panel = UIUtil.newGridComposite(radio, 0, 0, 2);
		UIUtil.newLabel(panel, label);
		Text text = UIUtil.newText(panel, "", new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});
		GridData gd = UIUtil.gridDataHoriz(1, true);
		gd.widthHint = 100;
		text.setLayoutData(gd);
		return new Pair<Composite,Text>(panel, text);
	}
	
	private void validate() {
		if (radioAlbum != null) {
			String s = radioAlbum.getSelection();
			if (s == null) { updateValidation(Local.Please_select_an_album_name.toString()); return; }
			if (s.equals("/other")) {
				s = textAlbum.getText();
				if (s.length() == 0) { updateValidation(Local.The_name_cannot_be_empty.toString()); return; }
			}
		}
		if (radioArtist != null) {
			String s = radioArtist.getSelection();
			if (s == null) { updateValidation(Local.Please_select_an_artist_name.toString()); return; }
			if (s.equals("/other")) {
				s = textArtist.getText();
				if (s.length() == 0) { updateValidation(Local.The_name_cannot_be_empty.toString()); return; }
			}
		}
		if (radioYear != null) {
			String s = radioYear.getSelection();
			if (s == null) { updateValidation(Local.Please_select_a_year.toString()); return; }
			if (s.equals("/other")) {
				s = textYear.getText();
				if (s.length() == 0) { updateValidation(Local.The_year_cannot_be_empty.toString()); return; }
				try { Integer.parseInt(s); }
				catch (NumberFormatException e) { updateValidation(Local.The_year_must_be_a_number.toString()); return; }
			}
		}
		updateValidation(null);
	}
	
	private void updateValidation(String msg) {
		validation.updateValidation(msg);
		buttons.enableOk(msg == null);
	}
	
	public boolean open() {
		super.create(Local.Create_Music_Album.toString(), 0);
		super.openProgressive(null, OrientationY.BOTTOM);
		super.modal();
		return resultOK;
	}
	
	public String getAlbumName() { return resultAlbum; }
	public String getArtistName() { return resultArtist; }
	public Integer getYear() { return resultYear; }
}
