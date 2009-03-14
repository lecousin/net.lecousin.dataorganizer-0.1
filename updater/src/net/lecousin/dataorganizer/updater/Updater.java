package net.lecousin.dataorganizer.updater;

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
		if (path == null) System.exit(1);
		if (!WaitDataOrganizerToBeClose.waitClose()) System.exit(1);
		Backup backup = new Backup(path);
		if (!Unzipper.unzip(path+"\\update-tmp\\update.zip", path)) {
			backup.restore();
			System.exit(1);
		}
		InstallFinalizer.finalize(path);
		backup.remove();
		launchDataOrganizer(path);
		System.exit(0);
	}

	static void launchDataOrganizer(String path) {
		// TODO
	}
}
