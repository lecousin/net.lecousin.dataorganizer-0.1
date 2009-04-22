package net.lecousin.dataorganizer.updater;

import java.io.File;

import net.lecousin.framework.io.FileSystemUtil;
import net.lecousin.framework.io.ZipUtil;
import net.lecousin.framework.progress.WorkProgress;

public class Installer {

	public static boolean installApplication(File appDir, File tmpAppDir, WorkProgress progress, int work) {
		if (tmpAppDir.listFiles().length == 0) return true; // handle less_than case (empty app to avoid 0.1 bug)
		FileSystemUtil.deleteDirectory(new File(appDir, "configuration"));
		FileSystemUtil.deleteDirectory(new File(appDir, "plugins"));
		for (File file : appDir.listFiles()) {
			if (file.isDirectory()) continue;
			if (file.getName().equalsIgnoreCase("DataOrganizer.xml")) continue;
			if (file.getName().equalsIgnoreCase("DataOrganizer.ini")) continue;
			if (file.getName().equalsIgnoreCase("recentWorkspaces.xml")) continue;
			file.delete();
		}
		int step = work/4;
		work -= step;
		progress.progress(step);
		try { 
			FileSystemUtil.copyDirectory(tmpAppDir, appDir, progress, work);
			return true;
		} catch (Throwable t) {
			new ErrorDlg("Unable to install application", t);
			return false;
		}
	}
	
	public static boolean installExtras(File extrasDir, File tmpExtrasDir, WorkProgress progress, int work) {
		FileSystemUtil.deleteDirectory(extrasDir);
		int step = work/4;
		work -= step;
		progress.progress(step);
		try { 
			FileSystemUtil.copyDirectory(tmpExtrasDir, extrasDir, progress, work);
			return true;
		} catch (Throwable t) {
			new ErrorDlg("Unable to install extras", t);
			return false;
		}
	}
	
	public static boolean installJRE(File jreDir, File tmpJREDir, WorkProgress progress, int work) {
		FileSystemUtil.deleteDirectory(jreDir);
		int step = work/4;
		work -= step;
		progress.progress(step);
		try { 
			FileSystemUtil.copyDirectory(tmpJREDir, jreDir, progress, work);
			return true;
		} catch (Throwable t) {
			new ErrorDlg("Unable to install JRE", t);
			return false;
		}
	}
	
	public static boolean installPlugin(File zipPlugin, File tmpDir, File appDir, WorkProgress progress, int work) {
		try {
			File tmpPluginDir = new File(tmpDir, "plugin-tmp");
			FileSystemUtil.deleteDirectory(tmpPluginDir);
			tmpPluginDir.mkdirs();
			
			int stepUnzip = work/2;
			work -= stepUnzip;
			int stepDeleteOldPlugin = work/10;
			work -= stepDeleteOldPlugin;
			
			try {
				ZipUtil.unzip(zipPlugin, tmpPluginDir, progress, stepUnzip);
			} catch (Throwable t) {
				new ErrorDlg("Unable to unzip plugin", t);
				return false;
			}
			File pluginsDir = new File(appDir, "plugins");
			File oldPlugin = null;
			String name = zipPlugin.getName();
			if (name.startsWith("plugin_")) name = name.substring(7);
			if (name.endsWith(".zip")) name = name.substring(0, name.length()-4);
			for (File f : pluginsDir.listFiles()) {
				if (f.isFile() && f.getName().startsWith(name+"_") && f.getName().endsWith(".jar")) {
					oldPlugin = f;
					break;
				}
			}
			if (oldPlugin != null)
				oldPlugin.delete();
			progress.progress(stepDeleteOldPlugin);
			File[] list = tmpPluginDir.listFiles();
			int nb = list.length;
			for (File f : list) {
				int step = work/nb--;
				work -= step;
				try {
					FileSystemUtil.copyFile(f, new File(pluginsDir, f.getName()), progress, step);
				} catch (Throwable t) {
					new ErrorDlg("Unable to copy plugin", t);
					return false;
				}
			}
			progress.progress(work);
			return true;
		} catch (Throwable t) {
			new ErrorDlg("Unable to install plugin", t);
			return false;
		}
	}
	
}
