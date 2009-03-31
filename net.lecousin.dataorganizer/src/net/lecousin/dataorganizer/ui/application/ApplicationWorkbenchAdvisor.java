package net.lecousin.dataorganizer.ui.application;

import net.lecousin.dataorganizer.core.DataOrganizer;
import net.lecousin.dataorganizer.core.InitializationException;
import net.lecousin.dataorganizer.internal.EclipsePlugin;
import net.lecousin.dataorganizer.ui.Perspective;
import net.lecousin.dataorganizer.ui.application.preferences.DOPreferenceNode;
import net.lecousin.dataorganizer.ui.application.splash.InteractiveSplashHandler;
import net.lecousin.dataorganizer.ui.plugin.PreferencePageProvider;
import net.lecousin.framework.eclipse.extension.EclipsePluginExtensionUtil;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.ui.eclipse.dialog.ErrorDlg;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
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
		initPreferences();
	}
	
	private void initPreferences() {
		PreferenceManager pref = PlatformUI.getWorkbench().getPreferenceManager(); 
		pref.removeAll();
		for (IConfigurationElement ext : EclipsePluginExtensionUtil.getExtensionsSubNode(EclipsePlugin.ID, "preference", "page")) {
			try {
				PreferencePageProvider provider = EclipsePluginExtensionUtil.createInstance(PreferencePageProvider.class, ext, "provider", new Object[][] { new Object[] {} });
				PreferenceNode node = new DOPreferenceNode(provider);
				String path = provider.getPath();
				if (path == null || path.length() == 0)
					pref.addToRoot(node);
				else
					pref.addTo(path, node);
			} catch (Throwable t) {
				if (Log.error(this))
					Log.error(this, "Unable to initialize preference page", t);
			}
		}
	}
	
	@Override
	public boolean preShutdown() {
		DataOrganizer.close();
		return super.preShutdown();
	}
}
