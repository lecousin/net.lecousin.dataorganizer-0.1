package net.lecousin.dataorganizer.people;

import java.util.List;

import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.VirtualData;
import net.lecousin.dataorganizer.core.database.VirtualDataBase;
import net.lecousin.dataorganizer.core.database.content.ContentType;
import net.lecousin.dataorganizer.core.database.content.DataContentType;
import net.lecousin.dataorganizer.core.database.version.ContentTypeLoader;
import net.lecousin.dataorganizer.people.internal.EclipsePlugin;
import net.lecousin.dataorganizer.ui.wizard.adddata.AddData_Page;
import net.lecousin.framework.Pair;
import net.lecousin.framework.files.FileType;
import net.lecousin.framework.files.TypedFile;
import net.lecousin.framework.files.TypedFolder;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.ui.eclipse.EclipseImages;
import net.lecousin.framework.version.Version;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Element;

public class PeopleContentType extends ContentType {

	public static final String PEOPLE_TYPE = "people";
	
	public PeopleContentType() {
	}

	private static final Version version = new Version(0,1,0);
	@Override
	public Version getCurrentVersion() { return version; }
	
	@Override
	public String getID() { return PEOPLE_TYPE; }
	@Override
	public String getName() { return Local.People.toString(); }

	@Override
	public DataContentType create(Data data) {
		return new PeopleDataType(data);
	}

	@Override
	public DataContentType loadContent(Data data, Element elt) {
		return loadContent(data, elt, new Loader_0_1_0());
	}
	@Override
	public DataContentType loadContent(Data data, Element elt, ContentTypeLoader loader) {
		return new PeopleDataType(data, elt, (Loader)loader);
	}

	@Override
	public Image getDefaultTypeImage() { return EclipseImages.getImage(EclipsePlugin.ID, "images/execute_128.gif"); }

	@Override
	public Image getIcon() { return EclipseImages.getImage(EclipsePlugin.ID, "images/execute_16.gif"); }
	
	@Override
	public Object openLoadDataContentContext(Composite panel) {
		return null;
	}
	@Override
	public void closeLoadDataContentContext(Object context) {
	}
	@Override
	public void loadDataContent(Data data, Object context, WorkProgress progress, int work) {
		progress.progress(work);
	}
	
	@Override
	public AddData_Page createAddDataWizardPage() {
		return null;
	}

	@Override
	public FileType[] getEligibleFileTypesForDetection() {
		return null;
	}
	@Override
	public List<Pair<List<IFileStore>,VirtualData>> detectOnFolder(VirtualDataBase db, TypedFolder folder, Shell shell) {
		return null;
	}
	@Override
	public List<Pair<List<IFileStore>,VirtualData>> detectOnFile(VirtualDataBase db, TypedFolder folder, IFileStore file, Shell shell) {
		return null;
	}
	@Override
	public List<Pair<List<IFileStore>,VirtualData>> detectOnFile(VirtualDataBase db, TypedFolder folder, IFileStore file, TypedFile typedFile, Shell shell) {
		return null;
	}
}
