package net.lecousin.dataorganizer.updater;

import java.io.File;
import java.io.IOException;

import net.lecousin.framework.io.FileSystemUtil;
import net.lecousin.framework.io.FileSystemUtil.Filter;
import net.lecousin.framework.progress.WorkProgress;

public class Backup {

	private File backupDir;
	private File appDir;
	
	public Backup(String path, WorkProgress progress, int work) throws IOException {
		appDir = new File(path);
		backupDir = new File(appDir, "backup");
		if (backupDir.exists())
			FileSystemUtil.deleteDirectory(backupDir);
		backupDir.mkdirs();
		Filter filter = new Filter() {
			public boolean accept(File file) {
				if (file.getParentFile().equals(appDir) && file.getName().equals("backup")) return false;
				return true;
			}
		};
		FileSystemUtil.copyDirectory(appDir, backupDir, filter, progress, work);
	}
	
	public void restore(WorkProgress progress, int work) {
		Filter filter = new Filter() {
			public boolean accept(File file) {
				if (file.equals(appDir)) return false;
				if (file.getParentFile().equals(appDir) && file.getName().equals("backup")) return false;
				return true;
			}
		};
		FileSystemUtil.deleteDirectory(appDir, filter);
		try {
			FileSystemUtil.copyDirectory(backupDir, appDir, progress, work);
		} catch (IOException e) {
			new ErrorDlg("Unable to correctly restore the application. It is recommended to uninstall and re-install the application.", e);
			return;
		}
		FileSystemUtil.deleteDirectory(backupDir, null);
	}
	
	public void remove() {
		FileSystemUtil.deleteDirectory(backupDir);
	}
	
}
