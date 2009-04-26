package net.lecousin.dataorganizer.ui.wizard.adddata;

import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.core.database.VirtualData;
import net.lecousin.dataorganizer.core.database.VirtualDataBase;

import org.eclipse.jface.wizard.IWizardPage;

public interface AddData_Page extends IWizardPage {

	public boolean canFinish();
	public Result performFinish();
	public boolean finished();
	
	public static class Result {
		public VirtualDataBase db;
		public List<VirtualData> toAdd = new LinkedList<VirtualData>();
		//public List<IFileStore> usedFiles = new LinkedList<IFileStore>();
		public List<String> notDetectedNotTypesFiles = new LinkedList<String>();
		public List<String> notDetectedTypesFiles = new LinkedList<String>();
		
		public boolean showRefreshAfterAdd = true;
	}
}
