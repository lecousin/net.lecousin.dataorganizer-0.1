package net.lecousin.dataorganizer.updater;

import java.io.File;

import net.lecousin.framework.io.FileSystemUtil;
import net.lecousin.framework.progress.WorkProgress;

public class Installer {

	public static boolean installApplication(File appDir, File tmpAppDir, WorkProgress progress, int work) {
		FileSystemUtil.deleteDirectory(new File(appDir, "configuration"));
		FileSystemUtil.deleteDirectory(new File(appDir, "plugins"));
		for (File file : appDir.listFiles()) {
			if (file.isDirectory()) continue;
			if (file.getName().equalsIgnoreCase("DataOrganizer.xml")) continue;
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
	
}
