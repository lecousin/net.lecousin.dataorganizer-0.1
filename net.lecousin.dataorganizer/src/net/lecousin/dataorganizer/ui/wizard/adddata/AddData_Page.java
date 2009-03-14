package net.lecousin.dataorganizer.ui.wizard.adddata;

import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.core.database.VirtualData;
import net.lecousin.dataorganizer.core.database.VirtualDataBase;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.wizard.IWizardPage;

public interface AddData_Page extends IWizardPage {

	public boolean canFinish();
	public Result performFinish();
	
	public static class Result {
		public VirtualDataBase db;
		public List<VirtualData> toAdd = new LinkedList<VirtualData>();
		public List<IFileStore> noDetectedFiles = new LinkedList<IFileStore>();
	}
}
