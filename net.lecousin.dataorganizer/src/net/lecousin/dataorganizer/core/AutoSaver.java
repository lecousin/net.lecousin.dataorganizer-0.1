package net.lecousin.dataorganizer.core;

import net.lecousin.dataorganizer.core.database.RealData;
import net.lecousin.dataorganizer.core.database.content.DataContentType;
import net.lecousin.framework.application.Application;
import net.lecousin.framework.collections.SelfMapUniqueLong;

public class AutoSaver implements Runnable {

	AutoSaver() {
		instance = this;
		Application.getMonitor().newWork(this, 5*60*1000);
	}
	
	private static AutoSaver instance;
	
	private SelfMapUniqueLong<RealData> dataToSave = new SelfMapUniqueLong<RealData>();
	private SelfMapUniqueLong<DataContentType> contentToSave = new SelfMapUniqueLong<DataContentType>();
	private SelfMapUniqueLong<RealData> dataRemoved = new SelfMapUniqueLong<RealData>();
	
	public static void modified(RealData data) {
		synchronized (instance.dataToSave) {
			if (instance.dataToSave.containsKey(data.getHashObject()))
				return;
			instance.dataToSave.add(data);
		}
	}
	public static void modified(DataContentType content) {
		synchronized (instance.contentToSave) {
			if (instance.contentToSave.containsKey(content.getHashObject()))
				return;
			instance.contentToSave.add(content);
//			try {
//				throw new Exception("DataContentType to save: data="+content.getData().getName() + " (type " + content.getData().getClass().getName() + "), located on " + content.getFolder().getFullPath().toString());
//			} catch (Exception e) {
//				e.printStackTrace(System.out);
//			}
		}
	}
	public static void removed(RealData data) {
		synchronized (instance.dataRemoved) {
			if (!instance.dataRemoved.containsKey(data.getHashObject()))
				instance.dataRemoved.add(data);
		}
		synchronized (instance.dataToSave) {
			instance.dataToSave.remove(data);
		}
		synchronized (instance.contentToSave) {
			instance.contentToSave.remove(data.getID());
		}
	}
	
	public void run() {
		saveData();
		saveContent();
	}
	
	public static void close() {
		instance.run();
	}
	
	private void saveData() {
		RealData data;
		do {
			synchronized (dataToSave) {
				data = dataToSave.removeFirst();
			}
			if (data == null) break;
			synchronized (dataRemoved) {
				if (dataRemoved.containsKey(data.getID()))
					continue;
			}
			data.save();
		} while (data != null);
	}
	
	private void saveContent() {
		DataContentType content;
		do {
			synchronized (contentToSave) {
				content = contentToSave.removeFirst();
			}
			if (content == null) break;
			synchronized (dataRemoved) {
				if (dataRemoved.containsKey(content.getHashObject()))
					continue;
			}
			content.save();
		} while (content != null);
	}
}
