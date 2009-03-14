package net.lecousin.dataorganizer.core.database.refresh;

import java.util.List;
import java.util.Map;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.DataBase;
import net.lecousin.dataorganizer.core.database.content.ContentType;
import net.lecousin.dataorganizer.core.database.util.DataUtil;
import net.lecousin.dataorganizer.internal.EclipsePlugin;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.dialog.ErrorDlg;
import net.lecousin.framework.ui.eclipse.progress.WorkProgressDialog;

public class Refresher {

	public static void refresh(DataBase db, List<Data> data, RefreshOptions options) {
		WorkProgress progress = new WorkProgress(Local.Refreshing.toString(), data.size(), true);
		WorkProgressDialog dlg = new WorkProgressDialog(null, progress);
		Map<ContentType,List<Data>> byContent = DataUtil.splitByContent(data);
		try {
			for (ContentType type : byContent.keySet()) {
				UIUtil.runPendingEvents(dlg.getShell().getDisplay());
				if (progress.isCancelled()) break;
				List<Data> list = byContent.get(type);
				Context ctx = new Context(type, dlg);
				for (Data d : list) {
					UIUtil.runPendingEvents(dlg.getShell().getDisplay());
					if (progress.isCancelled()) break;
					progress.setSubDescription(d.getName());
					dlg.forceRefresh();
					refresh(d, ctx, options);
					progress.progress(1);
				}
				ctx.close();
			}
		} catch (Throwable t) {
			ErrorDlg.exception("Refreshing", "A fatal error occured while refreshing data", EclipsePlugin.ID, t);
		}
		dlg.close();
	}
	
	private static class Context {
		Context(ContentType type, WorkProgressDialog dlg) { this.type = type; this.dlg = dlg; }
		ContentType type;
		WorkProgressDialog dlg;
		
		Object getDataContentContext = null;
		
		void initGetDataContent() {
			if (getDataContentContext == null)
				getDataContentContext = type.openLoadDataContentContext(dlg.getCustomizePanel());
		}
		
		void close() {
			if (getDataContentContext != null)
				type.closeLoadDataContentContext(getDataContentContext);
		}
	}
	
	private static void refresh(Data data, Context ctx, RefreshOptions options) {
		if (options.cleanName)
			cleanName(data);
		boolean getDataContent = options.refreshAllDataContent;
		if (!getDataContent && options.getDataContentIfNotYetDone)
			getDataContent = !data.getContent().isContentAvailable();
		if (getDataContent) {
			ctx.initGetDataContent();
			ctx.dlg.ensureCustomPanelVisibleIfNeeded();
			ctx.type.loadDataContent(data, ctx.getDataContentContext);
		}
	}
	
	public static void cleanName(Data data) {
		// TODO
	}
}
