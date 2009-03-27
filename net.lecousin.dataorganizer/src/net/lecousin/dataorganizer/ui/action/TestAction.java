package net.lecousin.dataorganizer.ui.action;

import net.lecousin.dataorganizer.Local;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.text.lcml.LCMLText;
import net.lecousin.framework.ui.eclipse.dialog.FlatDialog;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Composite;

public class TestAction extends Action {

	public static final String ID = "net.lecousin.dataorganizer.action.TestAction";
	
	public TestAction() {
		super("Test", SharedImages.getImageDescriptor(SharedImages.icons.x16.basic.ERROR));
		setId(ID);
	}

	@Override
	public void run() {
		FlatDialog dlg = new FlatDialog(null, Local.Update_application.toString(), true, true) {
			@Override
			protected void createContent(Composite container) {
				LCMLText text = new LCMLText(container, false, false);
				text.setText(
						"Bonjour voici un test pour des paragraphes:" +
						"<p>" +
						"<b>Titre</b><br/><br/>" +
						"A noter que juste devant cette ligne il doit y avoir une ligne vide, de séparation, si cela n'est pas le cas il va falloir corriger le cas ou on mets 2 br d'affile.." +
						"</p>" +
						"Un peu de texte entre 2 paragraphe..." +
						"<p>" +
						"Et voici un nouveau paragraphe, cela marche-t-il ??" +
						"</p>" +
						"Et enfin un peu de texte en dehors de tout paragraphe!" +
						"<p marginTop=30>Un paragraphe avec un grand espace devant</p>" +
						"sep" +
						"<p marginBottom=30>Grand espace apres</p>" +
						"sep" +
						"<p marginLeft=30>Indente</p>" +
						"sep" +
						"<p marginTop=10 marginLeft=5>Un peu devant, legere indentation</p>" +
						"FIN"
						);
				text.setLayoutData(UIUtil.gridData(1, true, 1, false));
			}
		};
		dlg.open(true);
		
		
//		D d = new D();
//		d.open();
	}
	
//	private class D extends MyDialog {
//		D() {
//			super(null);
//		}
//		@Override
//		protected Composite createControl(Composite container) {
//			Composite panel = new Composite(container, SWT.NONE);
//			UIUtil.gridLayout(panel, 1);
//			LCProgressBar pb = new LCProgressBar(panel, LCProgressBar.Style.ROUND, ColorUtil.get(60, 60, 240));
//			pb.setMinimum(0);
//			pb.setMaximum(100);
//			pb.setPosition(75);
//			UIUtil.gridDataHorizFill(pb).heightHint = 20;
//			return panel;
//		}
//		public void open() {
//			super.open("Test", FLAG_CLOSABLE | FLAG_BORDER | FLAG_RESIZABLE | FLAG_MODAL);
//		}
//	}
}
