package net.lecousin.dataorganizer.core.database;

import java.util.List;

import net.lecousin.dataorganizer.core.database.content.ContentType;
import net.lecousin.dataorganizer.core.database.content.DataContentType;
import net.lecousin.dataorganizer.core.database.source.DataSource;

import org.eclipse.core.runtime.CoreException;

public class VirtualData extends Data {

	VirtualData(VirtualDataBase db, long id, String name, ContentType type, List<DataSource> sources) throws CoreException {
		super(db, id, name, type, sources);
	}

	private DataContentType content;
	
	@Override
	protected void storeContent(DataContentType content) {
		this.content = content;
	}
	
	@Override
	public DataContentType getContent() {
		return content;
	}
}
