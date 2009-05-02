package net.lecousin.dataorganizer.core.database.refresh;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.info.InfoRetriever;
import net.lecousin.dataorganizer.core.database.info.InfoRetrieverPlugin;
import net.lecousin.dataorganizer.core.database.info.InfoRetriever.FeedBackImpl;
import net.lecousin.dataorganizer.core.database.info.InfoRetriever.MultiRetrieveFeedBack;
import net.lecousin.framework.collections.CollectionUtil;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.progress.WorkProgress;

import org.eclipse.swt.widgets.Shell;

class Refresher_Internet {

	Refresher_Internet(Shell shell, Map<InfoRetrieverPlugin,List<Data>> plugins, WorkProgress progress) {
		int total = 0;
		for (Map.Entry<InfoRetrieverPlugin,List<Data>> entry : plugins.entrySet()) {
			total += entry.getValue().size();
			refreshers.add(new PluginRefresher(entry.getKey(), entry.getValue()));
			for (Data d : entry.getValue())
				if (!feedbacks.containsKey(d))
					feedbacks.put(d, new FeedBackImpl(d));
		}
		this.progress = progress;
		this.shell = shell;
		progress.reset(Local.Retrieve_information_from_Internet.toString(), total*100);
		for (PluginRefresher r : refreshers)
			r.start();
	}
	private List<PluginRefresher> refreshers = new LinkedList<PluginRefresher>();
	private WorkProgress progress;
	private Shell shell;
	private List<Data> inProgress = new LinkedList<Data>();
	private Map<Data,MultiRetrieveFeedBack> feedbacks = new HashMap<Data,MultiRetrieveFeedBack>();
	
	public boolean isDone() {
		for (PluginRefresher r : refreshers)
			for (PluginRefresher.RefreshThread t : r.threads)
				if (!t.done)
					return false;
		return true;
	}
	
	private class PluginRefresher {
		PluginRefresher(InfoRetrieverPlugin plugin, List<Data> data) {
			this.plugin = plugin;
			this.data = data;
		}
		private InfoRetrieverPlugin plugin;
		private List<Data> data;
		private RefreshThread[] threads;
		private void start() {
			int nb = plugin.getMaxThreads();
			threads = new RefreshThread[nb];
			for (int i = 0; i < nb; ++i) {
				threads[i] = new RefreshThread();
				threads[i].start();
			}
		}
		private class RefreshThread extends Thread {
			private boolean done = false;
			@Override
			public void run() {
				try {
					do {
						Data d;
						synchronized (data) {
							if (data.isEmpty()) break;;
							d = data.remove(0);
							synchronized (inProgress) {
								if (inProgress.contains(d)) {
									data.add(d);
									d = null;
								} else
									inProgress.add(d);
							}
						}
						if (d == null) {
							if (data.size() == 1)
								try { Thread.sleep(100); }
								catch (InterruptedException e) { break; }
							continue;
						}
						try {
							WorkProgress subProgress = progress.addSubWork(plugin.getName()+": "+d.getName(), 100, 10000);
							InfoRetriever.retrieve(shell, d, CollectionUtil.single_element_list(plugin), feedbacks.get(d), subProgress, 10000, false);
							progress.mergeSubWork(subProgress);
						} finally {
							inProgress.remove(d);
						}
						if (progress.isCancelled()) break;
					} while (true);
				} catch (Throwable t) {
					if (Log.error(this))
						Log.error(this, "Error in a RefresherInternet thread", t);
				}
				done = true;
			}
		}
	}
	
}
