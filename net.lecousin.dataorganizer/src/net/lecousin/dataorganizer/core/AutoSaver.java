package net.lecousin.dataorganizer.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.RealData;
import net.lecousin.dataorganizer.core.database.content.DataContentType;
import net.lecousin.framework.application.Application;
import net.lecousin.framework.collections.CollectionUtil;
import net.lecousin.framework.collections.SelfMapUniqueLong;
import net.lecousin.framework.log.Log;

public class AutoSaver implements Runnable {

	AutoSaver() {
		instance = this;
		Application.getMonitor().newWork(this, 5*60*1000);
	}
	
	private static AutoSaver instance;
	
	private SelfMapUniqueLong<RealData> dataToSave = new SelfMapUniqueLong<RealData>();
	private SelfMapUniqueLong<DataContentType> contentToSave = new SelfMapUniqueLong<DataContentType>();
	private List<RealData> dataRemoved = new LinkedList<RealData>();
	
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
			instance.dataRemoved.add(data);
		}
		synchronized (instance.dataToSave) {
			instance.dataToSave.remove(data);
		}
		synchronized (instance.contentToSave) {
			instance.contentToSave.remove(data.getID());
		}
	}
	public static void replaced(DataContentType content) {
		synchronized (instance.contentToSave) {
			instance.contentToSave.remove(content);
		}
	}
	
	public static DataContentType getContent(RealData data) {
		synchronized (instance.contentToSave) {
			for (DataContentType content : instance.contentToSave)
				if (content.getData() == data)
					return content;
		}
		return null;
	}
	
	public void run() {
		List<Data> removed;
		synchronized (dataRemoved) {
			removed = new ArrayList<Data>(dataRemoved);
		}
		saveData();
		saveContent();
		synchronized (dataRemoved) {
			for (Data d : removed)
				for (Iterator<RealData> it = dataRemoved.iterator(); it.hasNext(); ) {
					RealData rd = it.next();
					if (rd == d) {
						it.remove();
						break;
					}
				}
		}
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
				if (CollectionUtil.containsIdentity(dataRemoved, data))
					continue;
			}
			if (Log.debug(this))
				Log.debug(this, "AutoSaver: save data: " + data.getName());
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
				if (CollectionUtil.containsIdentity(dataRemoved, content.getData()))
					continue;
			}
			if (Log.debug(this))
				Log.debug(this, "AutoSaver: save data content: " + content.getData().getName());
			content.save();
		} while (content != null);
	}
}
