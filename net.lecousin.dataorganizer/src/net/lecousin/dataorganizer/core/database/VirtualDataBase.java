package net.lecousin.dataorganizer.core.database;

import java.util.List;

import net.lecousin.dataorganizer.core.InitializationException;
import net.lecousin.dataorganizer.core.database.content.ContentType;
import net.lecousin.dataorganizer.core.database.source.DataSource;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.progress.WorkProgress;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

public class VirtualDataBase extends DataBase {

	public VirtualDataBase(WorkProgress progress, int work) throws InitializationException {
		super(createProject(), progress, work);
	}
	
	private static IProject createProject() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		int i = 0;
		while (root.getProject("tmp_db_" + i).exists()) i++;
		return root.getProject("tmp_db_" + i);
	}
	
	@Override
	public void close() {
		try { project.delete(true, true, null); }
		catch (CoreException e) {
			if (Log.error(this))
				Log.error(this, "Unable to remove virtual database", e);
		}
	}
	
	@Override
	protected VirtualData createData(long id, String name, ContentType type, List<DataSource> sources) throws CoreException {
		return new VirtualData(this, id, name, type, sources);
	}
}
