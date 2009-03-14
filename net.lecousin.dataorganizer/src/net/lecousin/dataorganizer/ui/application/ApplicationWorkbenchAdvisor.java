package net.lecousin.dataorganizer.ui.application;

import net.lecousin.dataorganizer.core.DataOrganizer;
import net.lecousin.dataorganizer.core.InitializationException;
import net.lecousin.dataorganizer.internal.EclipsePlugin;
import net.lecousin.dataorganizer.ui.Perspective;
import net.lecousin.dataorganizer.ui.application.splash.InteractiveSplashHandler;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.ui.eclipse.dialog.ErrorDlg;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

/**
 * This workbench advisor creates the window advisor, and specifies
 * the perspective id for the initial window.
 */
public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {
	
    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        return new ApplicationWorkbenchWindowAdvisor(configurer);
    }

	public String getInitialWindowPerspectiveId() {
		return Perspective.ID;
	} 
	
	@Override
	public void preStartup() {
		super.preStartup();
		try { 
			InteractiveSplashHandler.preStartup();
			WorkProgress progress = InteractiveSplashHandler.getWorkspaceProgress();
			DataOrganizer.init(progress, progress.getAmount());
		}
		catch (InitializationException e) {
			ErrorDlg.exception("DataOrganizer initialization", "An error occured during the initialization of the application.", EclipsePlugin.ID, e);
			PlatformUI.getWorkbench().close();
		}
	}
	@Override
	public void postStartup() {
		super.postStartup();
		InteractiveSplashHandler.postStartup();
		DataOrganizer.dataSelectionChanged().fire(null);
	}
	
	@Override
	public boolean preShutdown() {
		DataOrganizer.close();
		return super.preShutdown();
	}
}
