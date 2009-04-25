package net.lecousin.dataorganizer.core.database.refresh;

import java.util.List;
import java.util.Map;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.content.ContentType;
import net.lecousin.dataorganizer.core.database.util.DataUtil;
import net.lecousin.framework.Pair;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.thread.RunnableWithData;
import net.lecousin.framework.ui.eclipse.progress.WorkProgressDialog;

class Refresher_DataContent implements Runnable {

	static Refresher_DataContent refresh(List<Data> data, WorkProgress progress, WorkProgressDialog dlg) {
		Refresher_DataContent refresher = new Refresher_DataContent(data, progress, dlg); 
		Thread t = new Thread(refresher);
		t.start();
		return refresher;
	}
	
	private Refresher_DataContent(List<Data> data, WorkProgress progress, WorkProgressDialog dlg) {
		this.data = data;
		this.progress = progress;
		this.dlg = dlg;
	}
	public boolean isDone() { return done; }
	private List<Data> data;
	private WorkProgress progress;
	private WorkProgressDialog dlg;
	private boolean done = false;
	public void run() {
		progress.reset(Local.Refreshing_data_content.toString(), data.size()*1000);
		Map<ContentType,List<Data>> byContent = DataUtil.splitByContent(data);
		for (ContentType type : byContent.keySet()) {
			if (progress.isCancelled()) break;
			List<Data> list = byContent.get(type);
			Context ctx = new Context(type, dlg);
			for (Data d : list) {
				if (progress.isCancelled()) break;
				progress.setSubDescription(d.getName());
				dlg.forceRefresh();
				getDataContent(d, ctx, progress, 1000);
				if (progress.isCancelled()) break;
			}
			ctx.close();
			if (progress.isCancelled()) break;
		}
		done = true;
	}
	private void getDataContent(Data data, Context ctx, WorkProgress progress, int work) {
		ctx.initGetDataContent();
		dlg.getShell().getDisplay().syncExec(new RunnableWithData<Pair<Pair<Data,Context>,Pair<WorkProgress,Integer>>>(new Pair<Pair<Data,Context>,Pair<WorkProgress,Integer>>(new Pair<Data,Context>(data, ctx), new Pair<WorkProgress,Integer>(progress, work))) {
			public void run() {
				Context ctx = data().getValue1().getValue2();
				ctx.dlg.ensureCustomPanelVisibleIfNeeded();
				ctx.type.loadDataContent(data().getValue1().getValue1(), ctx.getDataContentContext, data().getValue2().getValue1(), data().getValue2().getValue2());
			}
		});
	}
	
	private static class Context {
		Context(ContentType type, WorkProgressDialog dlg) { this.type = type; this.dlg = dlg; }
		ContentType type;
		WorkProgressDialog dlg;
		
		Object getDataContentContext = null;
		
		void initGetDataContent() {
			if (getDataContentContext == null) {
				dlg.getShell().getDisplay().syncExec(new Runnable() {
					public void run() { getDataContentContext = type.openLoadDataContentContext(dlg.getCustomizePanel()); }
				});
			}
		}
		
		void close() {
			if (getDataContentContext != null)
				type.closeLoadDataContentContext(getDataContentContext);
		}
	}
	
}
