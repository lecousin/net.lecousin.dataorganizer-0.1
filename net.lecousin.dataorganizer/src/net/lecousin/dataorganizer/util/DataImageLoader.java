package net.lecousin.dataorganizer.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.framework.Pair;
import net.lecousin.framework.Triple;
import net.lecousin.framework.event.ProcessListener;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.thread.BackgroundTemporaryWorkThreading;
import net.lecousin.framework.thread.RunnableWithData;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;


public class DataImageLoader {

	private static BackgroundTemporaryWorkThreading threading = new BackgroundTemporaryWorkThreading("Data images loader", 30000, 2);
	
	private static List<Requester> requesters = new LinkedList<Requester>();
	
	private static class Requester implements Runnable {
		Object instance;
		String key;
		FileProvider provider;
		List<ProcessListener<Pair<Image,String>>> listeners = new LinkedList<ProcessListener<Pair<Image,String>>>();
		List<Pair<Image,String>> images = null;
		
		public void run() {
			synchronized (listeners) {
				for (ProcessListener<Pair<Image,String>> l : listeners)
					l.started();
				images = new LinkedList<Pair<Image,String>>();
			}
			while (provider.hasNext()) {
				Pair<File,String> file = provider.next();
				if (file == null) continue;
				FileInputStream in;
				try { in = new FileInputStream(file.getValue1()); }
				catch (IOException e) { continue; }
				Triple<InputStream,Pair<Image,String>,Pair<File,String>> p = new Triple<InputStream,Pair<Image,String>,Pair<File,String>>(in,null,file);
				Display.getDefault().syncExec(new RunnableWithData<Triple<InputStream,Pair<Image,String>,Pair<File,String>>>(p) {
					public void run() {
						try {
							Image img = new Image(Display.getCurrent(), data().getValue1());
							data().setValue2(new Pair<Image,String>(img,data().getValue3().getValue2()));
						} catch (Throwable t) {
							if (Log.warning(this))
								Log.warning(this, "Unable to load data image "+data().getValue3().getValue1().getAbsolutePath(), t);
						}
					}
				});
				try { in.close(); }
				catch (IOException e) {}
				Pair<Image,String> img = p.getValue2();
				if (img != null) {
					synchronized (listeners) {
						images.add(img);
						for (ProcessListener<Pair<Image,String>> l : listeners)
							l.fire(img);
					}
				}
			}
			synchronized (listeners) {
				for (ProcessListener<Pair<Image,String>> l : listeners)
					l.done();
			}
		}
	}
	
	public static void load(Object instance, String key, FileProvider provider, ProcessListener<Pair<Image,String>> listener) {
		Requester requester;
		synchronized (requesters) {
			boolean found = false;
			for (Requester r : requesters) {
				if (r.instance == instance && r.key.equals(key)) {
					synchronized (r.listeners) {
						r.listeners.add(listener);
						if (r.images != null) {
							listener.started();
							for (Pair<Image,String> i : r.images)
								listener.fire(i);
						}
					}
					found = true;
					break;
				}
			}
			if (found) return;
			requester = new Requester();
			requester.instance = instance;
			requester.key = key;
			requester.provider = provider;
			requester.listeners.add(listener);
		}
		threading.newWork(requester, 0);
	}
	
	public static void release(Object instance, String key) {
		synchronized (requesters) {
			for (Iterator<Requester> it = requesters.iterator(); it.hasNext(); ) {
				Requester r = it.next();
				if (r.instance == instance && r.key.equals(key)) {
					it.remove();
					break;
				}
			}
		}
	}
	
	public static interface FileProvider {
		public boolean hasNext();
		public Pair<File,String> next();
	}
	
	public static class FileProvider_FromDataPath implements FileProvider {
		public FileProvider_FromDataPath(Data data, Iterable<String> list) {
			this.data = data;
			this.it = list.iterator();
		}
		private Data data;
		Iterator<String> it;
		public boolean hasNext() { return it.hasNext(); }
		public Pair<File,String> next() {
			String path = it.next();
			try { 
				IFolder folder = data.getFolder();
				IFile file = folder.getFile(new Path(path));
				return new Pair<File,String>(file.getLocation().toFile(), path);
			} catch (CoreException e) { return null; }
		}
	}
	
	public static class FileProvider_ListStartWith implements FileProvider {
		public FileProvider_ListStartWith(IFolder folder, String start, String folderPath) {
			this.folder = folder;
			this.folderPath = folderPath;
			this.start = start;
			initNext();
		}
		private IFolder folder;
		private String folderPath;
		private String start;
		private int index = 0;
		private IFile next;
		public boolean hasNext() { 
			return next != null; 
		}
		public Pair<File,String> next() {
			File file = next.getLocation().toFile();
			index++;
			initNext();
			return new Pair<File,String>(file,folderPath+'/'+file.getName());
		}
		
		private void initNext() {
			try {
				if (!folder.exists()) { next = null; return; }
				for (IResource res : folder.members()) {
					if (!(res instanceof IFile)) continue;
					if (!res.getName().startsWith(start + index + '.')) continue;
					next = (IFile)res;
					return;
				}
			} catch (CoreException e) {
				if (Log.warning(this))
					Log.warning(this, "Unable to correctly load images", e);
			}
			next = null;
		}
	}
}
