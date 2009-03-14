package net.lecousin.dataorganizer.ui.application;

import java.io.File;
import java.net.URISyntaxException;

import net.lecousin.dataorganizer.core.DataOrganizer;
import net.lecousin.dataorganizer.core.DataOrganizerConfig;
import net.lecousin.dataorganizer.ui.application.update.Updater;
import net.lecousin.framework.Pair;
import net.lecousin.framework.io.FileSystemUtil;
import net.lecousin.framework.log.DualLog;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.log.LogConsole;
import net.lecousin.framework.log.LogFile;
import net.lecousin.framework.version.Version;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication {

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	public Object start(IApplicationContext context) {
		String[] args = Platform.getApplicationArgs();
		String path = null;
		for (int i = 0; i < args.length-1; ++i)
			if (args[i].equals("-deployPath")) {
				path = args[i+1];
				break;
			}
		try {
			net.lecousin.framework.application.Application.deployPath = path != null ? new File(path) : new File(Platform.getInstallLocation().getURL().toURI());
		} catch (URISyntaxException e) {
			// should never happen
		}
		
		try {
			File workspace = new File(Platform.getInstanceLocation().getURL().toURI());
			Log.setDefaultLog(new DualLog(new LogConsole(Log.Severity.DEBUG), new LogFile(Log.Severity.DEBUG, new File(workspace, "dataorganizer.log").getAbsolutePath())));
			File file = new File(workspace, ".metadata");
			file = new File(file, ".plugins");
			file = new File(file, "org.eclipse.core.resources");
			if (file.exists()) {
				// remove resources plugin data, to avoid bad remaining stuff that avoid starting the application
				// moreover nothing is kept in history... so it is not usefull for our application
				FileSystemUtil.deleteDirectory(file);
			}
		} catch (Throwable t) {
			Log.setDefaultLog(new LogConsole(Log.Severity.DEBUG));
			Log.error(this, "Unable to initialize file log", t);
		}

		net.lecousin.framework.application.Application.language = net.lecousin.framework.application.Application.Language.FRENCH;
		
		Log.debug(this, "Install: " + Platform.getInstallLocation().getURL().toString());
		Log.debug(this, "Instance: " + Platform.getInstanceLocation().getURL().toString());
		
		Display display = PlatformUI.createDisplay();

		if (checkUpdate(display))
			return IApplication.EXIT_OK;

		try {
			int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
			if (returnCode == PlatformUI.RETURN_RESTART) {
				return IApplication.EXIT_RESTART;
			}
			return IApplication.EXIT_OK;
		} finally {
			display.dispose();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null)
			return;
		final Display display = workbench.getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				if (!display.isDisposed())
					workbench.close();
			}
		});
	}
	
	private boolean checkUpdate(Display display) {
		DataOrganizerConfig config = DataOrganizer.config();
		if ((System.currentTimeMillis()-config.last_update_check)/((long)24*60*60*1000) < config.update_check_frequency) return false;

		config.last_update_check = System.currentTimeMillis();
		config.save();
		Shell shell = null;
		try {
			Pair<Version,String> p = Updater.getLatestVersionInfo();
			Version current = Updater.getCurrentVersion();
			if (p.getValue1().compareTo(current) <= 0) return false;
			shell = new Shell(display, SWT.PRIMARY_MODAL/*SWT.APPLICATION_MODAL*/);
			if (!Updater.askToUpdate(shell, current, p.getValue1())) return false;
			Updater.launchUpdate(shell, p.getValue2());
			return true;
		} catch (Throwable t) {
			if (Log.warning(this))
				Log.warning(this, "Error while checking for a new version", t);
			return false;
		} finally {
			if (shell != null)
				shell.close();
		}
	}
}
