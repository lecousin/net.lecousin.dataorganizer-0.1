
package net.lecousin.dataorganizer.ui.application.splash;

import net.lecousin.dataorganizer.Local;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.dialog.MyDialog;
import net.lecousin.framework.ui.eclipse.progress.EmbeddedWorkProgressControl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.splash.AbstractSplashHandler;

/**
 * @since 3.3
 * 
 */
public class InteractiveSplashHandler extends AbstractSplashHandler {
	
	public InteractiveSplashHandler() {
		instance = this;
	}
	
	private static InteractiveSplashHandler instance;
	
	public static void preStartup() {
		instance.progressInit.done();
	}
	public static void postStartup() {
		instance = null; // free resources
	}
	public static WorkProgress getWorkspaceProgress() {
		return instance.progressWorkspace;
	}
	
	
	private WorkProgress progress, progressInit, progressWorkspace;
	private EmbeddedWorkProgressControl control;
	private Image image;
	private Composite panel;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.splash.AbstractSplashHandler#init(org.eclipse.swt.widgets.Shell)
	 */
	public void init(final Shell splash) {
		// Store the shell
		super.init(splash);
		// Configure the shell layout
		configureUISplash();
		// Create UI
		createUI();		
		// Force the splash screen to layout
		splash.layout(true);
	}
	
	private void configureUISplash() {
		// Configure layout
		FillLayout layout = new FillLayout(); 
		getSplash().setLayout(layout);
		// Force shell to inherit the splash background
		//getSplash().setBackgroundMode(SWT.INHERIT_DEFAULT);
	}

	private void createUI() {
		progress = new WorkProgress(Local.Loading_application.toString(), 10000, false);
		progressInit = progress.addSubWork(Local.Initializing_application.toString(), 1000, 100);
		progressWorkspace = progress.addSubWork(Local.Loading_workspace.toString(), 9000, 10000);
		image = getSplash().getBackgroundImage();
		panel = new Composite(getSplash(), SWT.NONE);
		UIUtil.gridLayout(panel, 1, 0, 0).verticalSpacing = 0;
		UIUtil.newImage(panel, image).setLayoutData(UIUtil.gridData(1, true, 1, true));
		Composite container = new Composite(panel, SWT.BORDER);
		UIUtil.gridLayout(container, 1, 0, 0);
		container.setLayoutData(UIUtil.gridData(1, true, 1, false));
		control = new EmbeddedWorkProgressControl(progress);
		control.create(container, new SplashResizer());
		UIUtil.gridDataHorizFill(control.getControl());
		progressInit.progress(1);
	}		

	private class SplashResizer implements EmbeddedWorkProgressControl.Resizer {
		public void resize() {
			MyDialog.resizeShell(getSplash(), panel.computeSize(SWT.DEFAULT, SWT.DEFAULT, true));
		}
	}
}
