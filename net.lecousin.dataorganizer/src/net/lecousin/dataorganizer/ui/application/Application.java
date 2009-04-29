package net.lecousin.dataorganizer.ui.application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import net.lecousin.dataorganizer.core.DataOrganizer;
import net.lecousin.dataorganizer.core.DataOrganizerConfig;
import net.lecousin.dataorganizer.ui.application.update.Updater;
import net.lecousin.framework.io.FileSystemUtil;
import net.lecousin.framework.io.TextLineInputStream;
import net.lecousin.framework.log.DualLog;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.log.LogConsole;
import net.lecousin.framework.log.LogFile;
import net.lecousin.framework.ui.eclipse.dialog.ErrorDlg;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.osgi.service.datalocation.Location;
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
		Display display = null;
		try {
		String[] args = Platform.getApplicationArgs();
		String path = null;
		boolean firstlaunch = false;
		boolean debug = false;
		for (int i = 0; i < args.length; ++i) {
			if (args[i].equals("-deployPath") && i < args.length-1)
				path = args[++i];
			else if (args[i].equals("-firstLaunch"))
				firstlaunch = true;
			else if (args[i].equals("-enable_debug"))
				debug = true;
		}
		try {
			net.lecousin.framework.application.Application.deployPath = path != null ? new File(path) : new File(Platform.getInstallLocation().getURL().toURI());
		} catch (URISyntaxException e) {
			// should never happen
		}
		net.lecousin.framework.application.Application.isDebugEnabled = debug;
		
		display = PlatformUI.createDisplay();
		
		Location instance = Platform.getInstanceLocation();
		if (!instance.isSet()) {
			if (!chooseWorkspace(instance, display))
				return IApplication.EXIT_OK;
		} else {
			//net.lecousin.framework.application.Application.language = net.lecousin.framework.application.Application.Language.FRENCH;
			net.lecousin.framework.application.Application.language = net.lecousin.framework.application.Application.Language.ENGLISH;
		}
		
		try {
			File workspace = new File(instance.getURL().toURI());
			Log.setDefaultLog(new DualLog(new LogConsole(debug ? Log.Severity.DEBUG : Log.Severity.WARNING), new LogFile(debug ? Log.Severity.DEBUG : Log.Severity.WARNING, new File(workspace, "dataorganizer.log").getAbsolutePath())));
			File file = new File(workspace, ".metadata");
			file = new File(file, ".plugins");
			file = new File(file, "org.eclipse.core.resources");
			if (file.exists()) {
				// remove resources plugin data, to avoid bad remaining stuff that avoid starting the application
				// moreover nothing is kept in history... so it is not usefull for our application
				FileSystemUtil.deleteDirectory(file);
			}
		} catch (Throwable t) {
			Log.setDefaultLog(new LogConsole(debug ? Log.Severity.DEBUG : Log.Severity.WARNING));
			Log.error(this, "Unable to initialize file log", t);
		}

		if (Log.debug(this)) {
			Log.debug(this, "Install: " + Platform.getInstallLocation().getURL().toString());
			Log.debug(this, "Instance: " + Platform.getInstanceLocation().getURL().toString());
			Log.debug(this, "Environment:");
			for (Map.Entry<String,String> e : System.getenv().entrySet())
				Log.debug(this, "  "+e.getKey()+"="+e.getValue());
			Log.debug(this, "Properties:");
			for (Map.Entry<Object, Object> e : System.getProperties().entrySet())
				Log.debug(this, "  "+e.getKey().toString()+"="+e.getValue().toString());
		}
		
		
		if (firstlaunch)
			handleFirstLaunch();
		Updater.signalLaunch();
		
		if (checkUpdate(display, firstlaunch))
			return IApplication.EXIT_OK;

		int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
		if (returnCode == PlatformUI.RETURN_RESTART) {
			return IApplication.EXIT_RESTART;
		}
		return IApplication.EXIT_OK;

		} finally {
			if (display != null && !display.isDisposed())
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
	
	private boolean checkUpdate(Display display, boolean firstLaunch) {
		DataOrganizerConfig config = DataOrganizer.config();
		if ((System.currentTimeMillis()-config.last_update_check)/((long)24*60*60*1000) < config.update_check_frequency) return false;

		config.last_update_check = System.currentTimeMillis();
		config.save();
		if (firstLaunch) return false;
		Shell shell = null;
		try {
			shell = new Shell(display, SWT.PRIMARY_MODAL/*SWT.APPLICATION_MODAL*/);
			Updater.Update update = Updater.getLatestVersionInfo(shell);
			if (update == null) return false;
			if (!Updater.askToUpdate(shell, update)) return false;
			Updater.launchUpdate(shell, update);
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
	
	private void handleFirstLaunch() {
		if (Updater.signalInstallation()) {
			try {
				File file = new File(net.lecousin.framework.application.Application.deployPath, "DataOrganizer.ini");
				TextLineInputStream in = new TextLineInputStream(new FileInputStream(file));
				StringBuilder str = new StringBuilder();
				String line;
				while ((line = in.readLine()) != null) {
					if (!line.equals("-firstLaunch"))
						str.append(line).append("\r\n");
				}
				in.close();
				FileOutputStream out = new FileOutputStream(file);
				out.write(str.toString().getBytes());
				out.flush();
				out.close();
			} catch (IOException e) {
				if (Log.error(this))
					Log.error(this, "Unable to remove -firstLaunch argument from DataOrganizer.ini");
			}
		}
	}
	
	private boolean chooseWorkspace(Location loc, Display display) {
		ChooseWorkspaceDialog dlg = new ChooseWorkspaceDialog(display);
		if (!dlg.open()) return false;
		try { loc.set(new File(dlg.getSelectedDir()).toURL(), true); }
		catch (Throwable t) {
			ErrorDlg.error("DataOrganizer", "Unable to use the specified database.", t);
			return false;
		}
		switch (dlg.getSelectedLang()) {
		case 0: net.lecousin.framework.application.Application.language = net.lecousin.framework.application.Application.Language.ENGLISH; break;
		case 1: net.lecousin.framework.application.Application.language = net.lecousin.framework.application.Application.Language.FRENCH; break;
		}
		return true;
	}
}
