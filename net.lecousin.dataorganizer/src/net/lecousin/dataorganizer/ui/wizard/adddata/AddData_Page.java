package net.lecousin.dataorganizer.ui.wizard.adddata;

import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.core.database.VirtualData;
import net.lecousin.dataorganizer.core.database.VirtualDataBase;
import net.lecousin.framework.Pair;
import net.lecousin.framework.files.TypedFile;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.wizard.IWizardPage;

public interface AddData_Page extends IWizardPage {

	public boolean canFinish();
	public Result performFinish();
	
	public static class Result {
		public VirtualDataBase db;
		public List<VirtualData> toAdd = new LinkedList<VirtualData>();
		public List<IFileStore> usedFiles = new LinkedList<IFileStore>();
		public List<IFileStore> notDetectedNotTypesFiles = new LinkedList<IFileStore>();
		public List<Pair<IFileStore,TypedFile>> notDetectedTypesFiles = new LinkedList<Pair<IFileStore,TypedFile>>();
	}
}
