package net.lecousin.dataorganizer.audio.detect;

import java.util.List;

import net.lecousin.dataorganizer.core.database.VirtualData;
import net.lecousin.dataorganizer.core.database.VirtualDataBase;
import net.lecousin.framework.log.Log;
import net.lecousin.media.sound.files.audio.mp3.ID3Format;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;

public class MP3Detector {

	public static List<VirtualData> detect(VirtualDataBase db, IFileStore file, IFileInfo info, List<IFileStore> remainingFolders, List<IFileStore> remainingFiles) {
		try {
			ID3Format id3 = ID3Format.load(file.openInputStream(0, null));
		} catch (Throwable e) {
			if (Log.error(MP3Detector.class))
				Log.error(MP3Detector.class, "Unable to load ID3Format correctly", e);
		}
		return null;
	}
	
}
