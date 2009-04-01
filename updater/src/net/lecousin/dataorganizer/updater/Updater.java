package net.lecousin.dataorganizer.updater;

import java.io.File;

import net.lecousin.framework.io.FileSystemUtil;
import net.lecousin.framework.io.ZipUtil;
import net.lecousin.framework.progress.WorkProgress;

public class Updater {

	public static void main(String[] args) {
		String path = null;
		for (int i = 0; i < args.length; ++i)
			if (args[i].equals("-deployPath") && i < args.length-1)
				path = args[i+1];
			else if (args[i].equals("--test_wait")) {
				WaitDataOrganizerToBeClose.waitClose();
				System.exit(1);
			}
			else if (args[i].equals("--test_error")) {
				new ErrorDlg("Test message for error dialog", null);
				new ErrorDlg("Second test", new Exception("This is the description of an exception that has been thrown"));
				System.exit(1);
			}
			else if (args[i].equals("--test_progress")) {
				WorkProgress progress = new WorkProgress("First", 100, false);
				ProgressDlg dlg = new ProgressDlg(progress);
				try { Thread.sleep(2000); } catch (InterruptedException e) {}
				progress.progress(10);
				try { Thread.sleep(2000); } catch (InterruptedException e) {}
				progress.setDescription("Second");
				try { Thread.sleep(2000); } catch (InterruptedException e) {}
				progress.progress(5);
				try { Thread.sleep(2000); } catch (InterruptedException e) {}
				progress.progress(50);
				try { Thread.sleep(2000); } catch (InterruptedException e) {}
				progress.setDescription("Third");
				try { Thread.sleep(2000); } catch (InterruptedException e) {}
				progress.progress(34);
				try { Thread.sleep(2000); } catch (InterruptedException e) {}
				progress.progress(1);
				progress.setDescription("Finished");
				try { Thread.sleep(3000); } catch (InterruptedException e) {}
				dlg.close();
				System.exit(1);
			}
		if (path == null) System.exit(1);
		if (!WaitDataOrganizerToBeClose.waitClose()) System.exit(1);

		WorkProgress progress = new WorkProgress("Building backup...", 100000, false);
		Backup backup = new Backup(path, progress, 20000);
		
		File appDir = new File(path);
		File extrasDir = new File(appDir, "extras");
		File jreDir = new File(appDir, "jre");
		File tmpDir = new File(appDir, "update-tmp");
		File tmpAppDir = new File(tmpDir, "application");
		File tmpJREDir = new File(tmpDir, "jre");
		File tmpExtrasDir = new File(tmpDir, "extras");
		File zipApp = new File(tmpDir, "application.zip");
		File zipExtras = new File(tmpDir, "extras.zip");
		File zipJRE = new File(tmpDir, "jre.zip");

		progress.setDescription("Installing DataOrganizer update...");
		int stepUnzip = 40000;
		int stepInstall = 40000;
		int stepUnzipApp, stepUnzipExtras;
		int nbUnzip = (zipApp.exists() ? 1 : 0) + (zipExtras.exists() ? 1 : 0);
		if (zipApp.exists()) {
			stepUnzipApp = stepUnzip/nbUnzip--;
			stepUnzip -= stepUnzipApp;
		} else stepUnzipApp = 0;
		if (zipExtras.exists()) {
			stepUnzipExtras = stepUnzip/nbUnzip--;
			stepUnzip -= stepUnzipExtras;
		} else stepUnzipExtras = 0;
		int stepInstallJRE, stepInstallExtras, stepInstallApp;
		int nbInstall = (zipApp.exists() ? 1 : 0) + (zipExtras.exists() ? 1 : 0) + (zipJRE.exists() ? 1 : 0);
		if (zipApp.exists()) {
			stepInstallApp = stepInstall/nbInstall--;
			stepInstall -= stepInstallApp;
		} else stepInstallApp = 0;
		if (zipExtras.exists()) {
			stepInstallExtras = stepInstall/nbInstall--;
			stepInstall -= stepInstallExtras;
		} else stepInstallExtras = 0;
		if (zipJRE.exists()) {
			stepInstallJRE = stepInstall/nbInstall--;
			stepInstall -= stepInstallJRE;
		} else stepInstallJRE = 0;
		ProgressDlg progressDlg = new ProgressDlg(progress);

		if (zipApp.exists()) {
			try { ZipUtil.unzip(zipApp, tmpAppDir, progress, stepUnzipApp); }
			catch (Throwable t) {
				new ErrorDlg("Unable to extract update", t);
				progress.setDescription("Restring backuped application...");
				progress.reset("Restring backuped application...", 10000);
				backup.restore(progress, 10000);
				System.exit(1);
			}
		}
		if (zipExtras.exists()) {
			try { ZipUtil.unzip(zipExtras, tmpExtrasDir, progress, stepUnzipExtras); }
			catch (Throwable t) {
				new ErrorDlg("Unable to extract update", t);
				progress.setDescription("Restring backuped application...");
				progress.reset("Restring backuped application...", 10000);
				backup.restore(progress, 10000);
				System.exit(1);
			}
		}
		if (zipApp.exists())
			if (!Installer.installApplication(appDir, tmpAppDir, progress, stepInstallApp)) {
				progress.setDescription("Restring backuped application...");
				progress.reset("Restring backuped application...", 10000);
				backup.restore(progress, 10000);
				System.exit(1);
			}
		if (zipExtras.exists())
			if (!Installer.installExtras(extrasDir, tmpExtrasDir, progress, stepInstallExtras)) {
				progress.setDescription("Restring backuped application...");
				progress.reset("Restring backuped application...", 10000);
				backup.restore(progress, 10000);
				System.exit(1);
			}
		if (zipJRE.exists())
			if (!Installer.installJRE(jreDir, tmpJREDir, progress, stepInstallJRE)) {
				progress.setDescription("Restring backuped application...");
				progress.reset("Restring backuped application...", 10000);
				backup.restore(progress, 10000);
				System.exit(1);
			}
		progress.setDescription("Finalizing installation...");
		InstallFinalizer.finalize(path);
		backup.remove();
		FileSystemUtil.deleteDirectory(tmpDir);
		launchDataOrganizer(path);
		progressDlg.close();
		System.exit(0);
	}

	static void launchDataOrganizer(String path) {
		// TODO
	}
}
