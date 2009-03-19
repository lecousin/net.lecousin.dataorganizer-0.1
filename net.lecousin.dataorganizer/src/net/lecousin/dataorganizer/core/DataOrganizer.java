package net.lecousin.dataorganizer.core;

import java.io.File;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.RealDataBase;
import net.lecousin.dataorganizer.internal.MemoryMonitor;
import net.lecousin.framework.application.Application;
import net.lecousin.framework.event.Event;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.xml.XmlUtil;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Element;

public class DataOrganizer {

	private DataOrganizer(WorkProgress progress, int amount) throws InitializationException {
		instance = this;
		Application.setName("DataOrganizer");
		Application.getMonitor().newWork(new MemoryMonitor(), (long)5*60*1000);
		new AutoSaver();
		
		int stepLabels = amount * 10 / 100;
		int stepDataBase = amount - stepLabels;
		WorkProgress labelsProgress = progress.addSubWork(Local.Loading_labels.toString(), stepLabels, 1000);
		WorkProgress databaseProgress = progress.addSubWork(Local.Loading_database.toString(), stepDataBase, 10000);
		
		amount -= stepLabels;
		try { labels = new DataLabels(labelsProgress, 1000); }
		catch (InitializationException e) {
			progress.progress(amount);
			throw e;
		}
		amount -= stepDataBase;
		try { database = new RealDataBase(databaseProgress, 10000); }
		catch (InitializationException e) {
			progress.progress(amount);
			throw e;
		}
		search.registerDBEvents();
		
		for (IProject p : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			if (p.getName().startsWith("tmp_db_")) {
				if (Log.warning(this))
					Log.warning(this, "A virtual database has been found: removing");
				try { p.delete(true, true, null); }
				catch (CoreException e) {
					if (Log.warning(this))
						Log.warning(this, "Unable to remove virtual database", e);
				}
			}
		}
	}
	private static DataOrganizer instance = null;
	
	public static void init(WorkProgress progress, int amount) throws InitializationException {
		new DataOrganizer(progress, amount);
	}
	public static void close() {
		AutoSaver.close();
		instance.database.close();
		Application.close();
	}
	
	private RealDataBase database;
	private DataLabels labels;
	private DataSearch search = new DataSearch();
	
	private Data selectedData = null;
	
	private Event<Data> dataSelectionChanged = new Event<Data>();
	
	public static RealDataBase database() { return instance.database; }
	public static DataLabels labels() { return instance.labels; }
	public static DataSearch search() { return instance.search; }
	
	public static Event<Data> dataSelectionChanged() { return instance.dataSelectionChanged; }
	
	public static void setSelectedData(Data data) {
		instance.selectedData = data;
		instance.dataSelectionChanged.fire(data);
	}
	public static Data getSelectedData() { return instance.selectedData; }
	
	private static DataOrganizerConfig config = null;
	public static DataOrganizerConfig config() { if (config==null) loadConfig(); return config; }
	private static void loadConfig() {
		try {
			Element root = XmlUtil.loadFile(new File(Application.deployPath, "DataOrganizer.xml"));
			config = new DataOrganizerConfig(root);
		} catch (Throwable t) {
			if (Log.error(DataOrganizer.class))
				Log.error(DataOrganizer.class, "Unable to load configuration file DataOrganizer.xml", t);
			config = new DataOrganizerConfig();
		}
	}
}
