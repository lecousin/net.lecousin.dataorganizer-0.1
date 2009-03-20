package net.lecousin.dataorganizer.core.database.info;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.DataOrganizer;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.content.ContentType;
import net.lecousin.dataorganizer.core.database.info.Info.DataLink;
import net.lecousin.dataorganizer.core.database.info.InfoRetrieverPlugin.SearchResult;
import net.lecousin.dataorganizer.core.database.source.DataSource;
import net.lecousin.dataorganizer.internal.EclipsePlugin;
import net.lecousin.dataorganizer.ui.datalist.DataListMenu;
import net.lecousin.framework.Pair;
import net.lecousin.framework.collections.CollectionUtil;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.thread.RunnableWithData;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.dialog.ErrorDlg;
import net.lecousin.framework.ui.eclipse.dialog.QuestionDlg;
import net.lecousin.framework.ui.eclipse.progress.WorkProgressDialog;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

public class InfoRetriever {

	private InfoRetriever() {}
	
	public static void retrieve(Shell shell, Data data) {
		List<InfoRetrieverPlugin> plugins = InfoRetrieverPluginRegistry.getRetrievers(data.getContentType().getID());
		if (plugins.isEmpty()) {
			MessageDialog.openInformation(shell, Local.Retrieve_information.toString(), Local.NoPluginToRetrieveInforForType + " " + data.getContentType().getName());
			return;
		}
		retrieve(shell, data, plugins);
	}
	public static void retrieve(Shell shell, Data data, InfoRetrieverPlugin plugin) {
		retrieve(shell, data, CollectionUtil.single_element_list(plugin));
	}
	public static void retrieve(Shell shell, Data data, List<InfoRetrieverPlugin> plugins) {
		WorkProgress progress = new WorkProgress(Local.Retrieve_information.toString(), 1+plugins.size()*100, true);
		WorkProgressDialog dlg = new WorkProgressDialog(shell, progress);
		retrieve(shell, data, plugins, progress, 1+plugins.size()*100);
		dlg.close();
	}

	public static void retrieve(Shell shell, Data data, WorkProgress progress, int amount) {
		List<InfoRetrieverPlugin> plugins = InfoRetrieverPluginRegistry.getRetrievers(data.getContentType().getID());
		if (plugins.isEmpty()) {
			progress.progress(amount);
			return;
		}
		retrieve(shell, data, plugins, progress, amount);
	}
	
	public static void retrieve(Shell shell, Data data, List<InfoRetrieverPlugin> plugins, WorkProgress progress, int amount) {
		List<Pair<InfoRetrieverPlugin,WorkProgress>> todo = new ArrayList<Pair<InfoRetrieverPlugin,WorkProgress>>(plugins.size());
		amount--;
		int nb = plugins.size();
		for (InfoRetrieverPlugin plugin : plugins) {
			int step = amount/nb--;
			amount -= step;
			todo.add(new Pair<InfoRetrieverPlugin,WorkProgress>(plugin, progress.addSubWork(plugin.getName(), step, 1000)));
		}
		Info info = data.getContent().getInfo();
		progress.progress(1);
		for (Pair<InfoRetrieverPlugin,WorkProgress> p : todo) {
			if (progress.isCancelled()) break;
			p.getValue2().progress(1);
			
			String id = info.getSourceID(p.getValue1().getSourceID());
			if (id == null) {
				WorkProgress search = p.getValue2().addSubWork(Local.Search.toString(), 249, 10001);
				WorkProgress retrieve = p.getValue2().addSubWork(Local.Retrieve.toString(), 750, 10001);
				SearchResult result = null;
				String name = data.getName();
				do {
					UIUtil.runPendingEvents(shell.getDisplay());
					if (search.isCancelled()) break;
					search.reset();
					search.progress(1);
					List<SearchResult> results;
					try {
						results = p.getValue1().search(name, search, 10000);
					} catch (Throwable t) {
						ErrorDlg.exception("Search on " + p.getValue1().getName(), "Internal error in plug-in", EclipsePlugin.ID, t);
						search.done();
						retrieve.done();
						p.getValue2().done();
						break;
					}
					if (results.size() == 0) {
						InputDialog dlgInput = new InputDialog(null, Local.No_result.toString(), Local.Cannot_found + " " + name + " " + Local.on + " " + p.getValue1().getName() + "\r\n" + Local.You_can_retry_with_a_different_name + ":", name, new IInputValidator() {
							public String isValid(String newText) {
								if (newText.length() == 0) return "You must provide a name";
								return null;
							}
						});
						if (dlgInput.open() == Window.CANCEL) break;
						name = dlgInput.getValue();
					} else if (results.size() > 1 || !name.equalsIgnoreCase(results.get(0).getName())) {
						QuestionDlg dlgQ = new QuestionDlg(shell, Local.Several_results_found.toString(), null);
						QuestionDlg.Answer[] answers = new QuestionDlg.Answer[results.size() + 1];
						for (int i = 0; i < results.size(); ++i)
							answers[i] = new QuestionDlg.AnswerControl(Integer.toString(i), results.get(i).getDescriptionControlProvider());	
						answers[results.size()] = new QuestionDlg.AnswerText("*change_name", Local.retry_with_a_different_name + ":", name, false, new IInputValidator() {
							public String isValid(String newText) {
								if (newText.length() == 0) return Local.You_must_provide_a_name + ".";
								return null;
							}
						});
						dlgQ.setAnswers(answers);
						dlgQ.setMessage(results.size() + " " + Local.results_have_been_found + " " + Local._for + " '<a href=\"open\">" + name + "</a>' " + Local.on + " " + p.getValue1().getName() + ".");
						dlgQ.handleHyperlinkMessage("open", new RunnableWithData<Data>(data) {
							public void run() {
								DataListMenu.menu(data(), false);
							}
						});
						dlgQ.show();
						QuestionDlg.Answer answer = dlgQ.getAnswer();
						if (answer == null) break;
						if (answer.id.equals("*change_name"))
							name = ((QuestionDlg.AnswerText)answer).text;
						else
							result = results.get(Integer.parseInt(answer.id));
					} else
						result = results.get(0);
				} while(result == null && !progress.isCancelled());
				if (result == null) continue;
				if (progress.isCancelled()) break;
				search.done();
				retrieve.progress(1);
				try {
					p.getValue1().retrieve(result, info, retrieve, 10000);
				} catch (Throwable t) {
					ErrorDlg.exception("Retrieve on " + p.getValue1().getName(), "Internal error in plug-in", EclipsePlugin.ID, t);
				}
				retrieve.done();
			} else {
				String name = info.getName(p.getValue1().getSourceID());
				if (name == null || name.length() == 0) name = data.getName();
				try {
					p.getValue1().retrieve(id, name, info, p.getValue2(), 999);
				} catch (Throwable t) {
					ErrorDlg.exception("Retrieve on " + p.getValue1().getName(), "Internal error in plug-in", EclipsePlugin.ID, t);
				}
			}
			p.getValue2().done();
		}
		progress.done();
	}
	
	public static Data retrieve(ContentType type, List<DataLink> links) {
		List<InfoRetrieverPlugin> plugins = InfoRetrieverPluginRegistry.getRetrievers(type.getID());
		WorkProgress progress = new WorkProgress(Local.Retrieve_information.toString(), 1+plugins.size()*100, true);
		WorkProgressDialog dlg = new WorkProgressDialog(null, progress);
		progress.progress(1);
		Data data;
		try { data = DataOrganizer.database().addData(links.get(0).name, type, new LinkedList<DataSource>()); }
		catch (CoreException e) {
			ErrorDlg.exception("Add data", "Unable to add data to database", EclipsePlugin.ID, e);
			return null;
		}
		boolean success = false;
		for (InfoRetrieverPlugin plugin : plugins) {
			UIUtil.runPendingEvents(dlg.getShell().getDisplay());
			if (progress.isCancelled()) break;
			String sourceID = plugin.getSourceID();
			DataLink link = null;
			for (DataLink l : links)
				if (l.source.equals(sourceID)) {
					link = l;
					break;
				}
			if (link != null) {
				success |= plugin.retrieve(link.id, link.name, data.getContent().getInfo(), progress, 100);
			} else {
				// TODO ??
				progress.progress(100);
			}
		}
		progress.done();
		dlg.close();
		if (progress.isCancelled()) success = false;
		if (!success) {
			data.remove();
			return null;
		}
		return data;
	}
	
	public static void refresh(Data data, String source) {
		InfoRetrieverPlugin plugin = InfoRetrieverPluginRegistry.getPlugin(source, data.getContentType().getID());
		WorkProgress progress = new WorkProgress(Local.Retrieve_information.toString(), 10001, true);
		WorkProgressDialog dlg = new WorkProgressDialog(null, progress);
		progress.progress(1);
		Info info = data.getContent().getInfo();
		String id = info.getSourceID(source);
		String name = info.getName(source);
		plugin.retrieve(id, name, info, progress, 10000);
		progress.done();
		dlg.close();
	}
}
