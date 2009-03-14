package net.lecousin.dataorganizer.core.database.version;

import java.util.HashMap;
import java.util.Map;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.InitializationException;
import net.lecousin.dataorganizer.core.database.RealDataBase;
import net.lecousin.dataorganizer.core.database.content.ContentType;
import net.lecousin.dataorganizer.internal.EclipsePlugin;
import net.lecousin.framework.version.Version;

import org.eclipse.jface.dialogs.MessageDialog;

public class VersionLoader {

	public VersionLoader() {
		
	}
	
	private DBLoader db = null;
	private Map<String,ContentTypeLoader> contentLoaders = new HashMap<String,ContentTypeLoader>();
	
	public void addLoader(String type, Version version) throws InitializationException {
		if (type.equals("db")) {
			try {
				Class<?> clazz = EclipsePlugin.getDefault().getBundle().loadClass("net.lecousin.dataorganizer.core.database.version.DBLoader_"+version.major+"_"+version.minor+"_"+version.sub);
				Object o = clazz.newInstance();
				db = (DBLoader)o;
				return;
			} catch (ClassNotFoundException e) {
				throw new InitializationException("No loader for database version " + version.toString(), e);
			} catch (Throwable t) {
				throw new InitializationException("Cannot instantiate loader for database version " + version.toString(), t);
			}
		}
		ContentTypeLoader loader = ContentType.getLoader(type, version);
		if (loader == null)
			throw new InitializationException("Unable to have a loader for content type " + type + " version " + version.toString(), new Exception("No loader configured or unable to instantiate it. Check your logs."));
		contentLoaders.put(type, loader);
	}
	
	public DBLoader getDB() {
		return db;
	}
	
	public ContentTypeLoader get(String type) {
		return contentLoaders.get(type);
	}
	
	public boolean checkVersions() throws Exception {
		int i;
		StringBuilder msg = new StringBuilder();
		i = db.getVersion().compareTo(RealDataBase.CURRENT_VERSION);
		if (i < 0)
			msg.append("Database version is ").append(db.getVersion().toString())
			.append(" but current version is ").append(RealDataBase.CURRENT_VERSION.toString())
			.append("\r\n");
		if (i > 0)
			throw new Exception(Local.ERROR_Invalid_DB_Version.toString());
		for (String type : contentLoaders.keySet()) {
			ContentTypeLoader loader = contentLoaders.get(type);
			ContentType ctype = ContentType.getContentType(type);
			i = loader.getVersion().compareTo(ctype.getCurrentVersion());
			if (i < 0)
				msg.append(Local.Data_for_content_type).append(" ").append(type)
				.append(" ").append(Local.are_stored_in_database_with_version).append(" ").append(loader.getVersion().toString())
				.append(" ").append(Local.but_current_version_is).append(" ").append(ctype.getCurrentVersion().toString())
				.append("\r\n");
			if (i > 0)
				throw new Exception(Local.ERROR_Invalid_DB_Version_ContentType + " " + type + " " + Local.ERROR_Invalid_DB_Version_ContentType2 + ".");
		}
		if (msg.length() == 0) return true;
		return MessageDialog.openConfirm(null, Local.Old_database.toString(), Local.ERROR_Invalid_DB_Version_Older1 + ":\r\n\r\n" + msg.toString() + 
				Local.ERROR_Invalid_DB_Version_Older2);
	}
	
	public boolean hasPreviousVersion() {
		if (db.getVersion().compareTo(RealDataBase.CURRENT_VERSION) < 0) return true;
		for (String type : contentLoaders.keySet()) {
			ContentTypeLoader loader = contentLoaders.get(type);
			ContentType ctype = ContentType.getContentType(type);
			if (loader.getVersion().compareTo(ctype.getCurrentVersion()) < 0) return true;
		}
		return false;
	}
	
	public boolean isDBPreviousVersion() {
		return db.getVersion().compareTo(RealDataBase.CURRENT_VERSION) < 0;
	}
	public boolean isPreviousVersion(String type) throws IllegalArgumentException {
		ContentTypeLoader loader = contentLoaders.get(type);
		if (loader == null) throw new IllegalArgumentException("Invalid content type " + type + ": no loader available.");
		ContentType ctype = ContentType.getContentType(type);
		if (ctype == null) throw new IllegalArgumentException("Invalid content type " + type + ".");
		return loader.getVersion().compareTo(ctype.getCurrentVersion()) < 0;
	}

}
