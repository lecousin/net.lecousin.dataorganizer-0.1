package net.lecousin.dataorganizer.audio;

import java.util.List;

import net.lecousin.dataorganizer.audio.detect.AlbumDetector;
import net.lecousin.dataorganizer.audio.internal.EclipsePlugin;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.VirtualData;
import net.lecousin.dataorganizer.core.database.VirtualDataBase;
import net.lecousin.dataorganizer.core.database.content.ContentType;
import net.lecousin.dataorganizer.core.database.content.DataContentType;
import net.lecousin.dataorganizer.core.database.version.ContentTypeLoader;
import net.lecousin.dataorganizer.ui.wizard.adddata.AddData_Page;
import net.lecousin.framework.Pair;
import net.lecousin.framework.files.FileType;
import net.lecousin.framework.files.TypedFile;
import net.lecousin.framework.files.TypedFolder;
import net.lecousin.framework.files.audio.AudioFile;
import net.lecousin.framework.files.playlist.PlayList;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.ui.eclipse.EclipseImages;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.version.Version;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Element;

public class AudioContentType extends ContentType {

	public static final String AUDIO_TYPE = "audio";
	
	public static final Version VERSION = new Version(0, 1, 0);
	
	public AudioContentType() {
	}

	@Override
	public Version getCurrentVersion() { return VERSION; }
	@Override
	public String getID() { return AUDIO_TYPE; }
	@Override
	public String getName() { return Local.Audio.toString(); }
	@Override
	public Image getIcon() { return SharedImages.getImage(SharedImages.icons.x16.filetypes.AUDIO); }
	@Override
	public Image getDefaultTypeImage() { return EclipseImages.getImage(EclipsePlugin.ID, "images/audio_128.gif"); }

	@Override
	public DataContentType create(Data data) { return new AudioDataType(data); }
	@Override
	public DataContentType loadContent(Data data, Element elt) { return new AudioDataType(data, elt, new Loader_0_1_0()); }
	@Override
	public DataContentType loadContent(Data data, Element elt, ContentTypeLoader loader) { return new AudioDataType(data, elt, loader); }


	@Override
	public AddData_Page createAddDataWizardPage() {
		return null;
	}


	@Override
	public Object openLoadDataContentContext(Composite panel) {
		return null;
	}
	@Override
	public void loadDataContent(Data data, Object context, WorkProgress progress, int work) {
		progress.progress(work);
	}
	@Override
	public void closeLoadDataContentContext(Object context) {
	}

	private static final FileType[] filetypes = new FileType[] {
		AudioFile.FILE_TYPE, PlayList.FILE_TYPE,
	};
	@Override
	public FileType[] getEligibleFileTypesForDetection() {
		return filetypes;
	}
	@Override
	public List<Pair<List<IFileStore>, VirtualData>> detectOnFolder(VirtualDataBase db, TypedFolder folder, Shell shell) {
		return AlbumDetector.detect(db, folder, shell);
	}
	@Override
	public List<Pair<List<IFileStore>, VirtualData>> detectOnFile(VirtualDataBase db, TypedFolder folder, IFileStore file, Shell shell) {
		return null;
	}
	@Override
	public List<Pair<List<IFileStore>, VirtualData>> detectOnFile(VirtualDataBase db, TypedFolder folder, IFileStore file, TypedFile typedFile, Shell shell) {
		return null; // TODO
	}

}
