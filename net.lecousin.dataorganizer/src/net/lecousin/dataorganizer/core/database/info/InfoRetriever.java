package net.lecousin.dataorganizer.core.database.info;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.core.DataOrganizer;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.content.ContentType;
import net.lecousin.dataorganizer.core.database.content.DataContentType;
import net.lecousin.dataorganizer.core.database.info.Info.DataLink;
import net.lecousin.dataorganizer.core.database.info.InfoRetrieverPlugin.SearchResult;
import net.lecousin.dataorganizer.core.database.source.DataSource;
import net.lecousin.dataorganizer.internal.EclipsePlugin;
import net.lecousin.dataorganizer.ui.datalist.DataListMenu;
import net.lecousin.framework.Pair;
import net.lecousin.framework.collections.CollectionUtil;
import net.lecousin.framework.collections.SelfMap;
import net.lecousin.framework.collections.SelfMapUniqueLong;
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
		retrieve(shell, data, plugins, progress, 1+plugins.size()*100, true);
		dlg.close();
	}

	public static void retrieve(Shell shell, Data data, WorkProgress progress, int amount) {
		List<InfoRetrieverPlugin> plugins = InfoRetrieverPluginRegistry.getRetrievers(data.getContentType().getID());
		if (plugins.isEmpty()) {
			progress.progress(amount);
			return;
		}
		retrieve(shell, data, plugins, progress, amount, true);
	}
	
	public static void retrieve(Shell shell, Data data, List<InfoRetrieverPlugin> plugins, WorkProgress progress, int amount, boolean showSubWorksPerPlugin) {
		List<Pair<InfoRetrieverPlugin,WorkProgress>> todo = new ArrayList<Pair<InfoRetrieverPlugin,WorkProgress>>(plugins.size());
		DataContentType content = data.getContent();
		if (content == null) {
			progress.progress(amount);
			return;
		}
		Info info = content.getInfo();
		if (info == null) {
			progress.progress(amount);
			return;
		}
		amount--;
		int nb = plugins.size();
		for (InfoRetrieverPlugin plugin : plugins) {
			int step = amount/nb--;
			amount -= step;
			todo.add(new Pair<InfoRetrieverPlugin,WorkProgress>(plugin, progress.addSubWork(showSubWorksPerPlugin ? plugin.getName() : null, step, 1000)));
		}
		progress.progress(1);
		List<Pair<String,String>> newIDs = new LinkedList<Pair<String,String>>();
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
					UIUtil.runPendingEventsIfDisplayThread(shell.getDisplay());
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
						name = askForNewName(shell, name, p.getValue1());
						if (name == null) break;
					} else if (results.size() > 1 || !name.equalsIgnoreCase(results.get(0).getName())) {
						Pair<String,SearchResult> pp = askAmongResults(shell, name, p.getValue1(), results, data);
						if (pp.getValue1() == null) break;
						name = pp.getValue1();
						result = pp.getValue2();
					} else
						result = results.get(0);
				} while(result == null && !progress.isCancelled());
				if (result == null) continue;
				if (progress.isCancelled()) break;
				search.done();
				retrieve.progress(1);
				try {
					p.getValue1().retrieve(result, info, retrieve, 10000);
					id = info.getSourceID(p.getValue1().getSourceID());
					if (id != null)
						newIDs.add(new Pair<String,String>(p.getValue1().getSourceID(), id));
				} catch (Throwable t) {
					ErrorDlg.exception("Retrieve on " + p.getValue1().getName(), "Internal error in plug-in", EclipsePlugin.ID, t);
				}
				retrieve.done();
			} else {
				String name = info.getSourceName(p.getValue1().getSourceID());
				if (name == null || name.length() == 0) name = data.getName();
				try {
					p.getValue1().retrieve(id, name, info, p.getValue2(), 999);
				} catch (Throwable t) {
					ErrorDlg.exception("Retrieve on " + p.getValue1().getName(), "Internal error in plug-in", EclipsePlugin.ID, t);
				}
			}
			p.getValue2().done();
		}
		if (!newIDs.isEmpty()) {
			// check if an existing data already has the same source ID, so the data should be merged
			SelfMap<Long,Data> noMergeData = new SelfMapUniqueLong<Data>();
			SelfMap<Long,Data> mergeData = new SelfMapUniqueLong<Data>();
			for (Pair<String,String> p : newIDs) {
				Data d = DataOrganizer.database().getDataFromInfoSourceID(data.getContentType(), p.getValue1(), p.getValue2(), data);
				if (d != null && !noMergeData.contains(d) && !mergeData.contains(d)) {
					MergeResponse resp = askToMerge(shell, data, d, p.getValue1());
					switch (resp) {
					case NO_MERGE :
						noMergeData.add(d);
						break;
					case MERGE:
						mergeData.add(d);
						break;
					case BAD_SOURCE_INFO:
						data.getContent().getInfo().removeSourceInfo(p.getValue1());
						break;
					}
				}
			}
			if (!mergeData.isEmpty()) {
				synchronized (oneQuestionAtATime) {
					shell.getDisplay().syncExec(new RunnableWithData<Pair<Shell,Pair<Data,SelfMap<Long,Data>>>>(new Pair<Shell,Pair<Data,SelfMap<Long,Data>>>(shell,new Pair<Data,SelfMap<Long,Data>>(data, mergeData))) {
						public void run() {
							for (Data d : data().getValue2().getValue2()) {
								data().getValue2().getValue1().merge(d, data().getValue1());
								d.remove();
							}
						}
					});
				}
			}
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
			UIUtil.runPendingEventsIfDisplayThread(dlg.getShell().getDisplay());
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
		if (plugin == null) return;
		WorkProgress progress = new WorkProgress(Local.Retrieve_information.toString(), 10001, true);
		WorkProgressDialog dlg = new WorkProgressDialog(null, progress);
		progress.progress(1);
		Info info = data.getContent().getInfo();
		String id = info.getSourceID(source);
		String name = info.getSourceName(source);
		plugin.retrieve(id, name, info, progress, 10000);
		progress.done();
		dlg.close();
	}
	
	private static Boolean oneQuestionAtATime = new Boolean(true);
	
	private static String askForNewName(Shell shell, String name, InfoRetrieverPlugin plugin) {
		synchronized (oneQuestionAtATime) {
			AskForNewName ask = new AskForNewName(shell, name, plugin);
			shell.getDisplay().syncExec(ask);
			return ask.name;
		}
	}
	private static class AskForNewName implements Runnable {
		public AskForNewName(Shell shell, String name, InfoRetrieverPlugin plugin) {
			this.shell = shell;
			this.name = name;
			this.plugin = plugin;
		}
		private Shell shell;
		private String name;
		private InfoRetrieverPlugin plugin;
		public void run() {
			InputDialog dlgInput = new InputDialog(shell, Local.No_result.toString(), Local.Cannot_found + " " + name + " " + Local.on + " " + plugin.getName() + "\r\n" + Local.You_can_retry_with_a_different_name + ":", name, new IInputValidator() {
				public String isValid(String newText) {
					if (newText.length() == 0) return "You must provide a name";
					return null;
				}
			});
			if (dlgInput.open() == Window.CANCEL) 
				name = null;
			else
				name = dlgInput.getValue();
		}
	}

	private static Pair<String,SearchResult> askAmongResults(Shell shell, String name, InfoRetrieverPlugin plugin, List<SearchResult> results, Data data) {
		synchronized (oneQuestionAtATime) {
			AskAmongResults ask = new AskAmongResults(shell, name, plugin, results, data);
			shell.getDisplay().syncExec(ask);
			return new Pair<String,SearchResult>(ask.name,ask.result);
		}
	}
	private static class AskAmongResults implements Runnable {
		public AskAmongResults(Shell shell, String name, InfoRetrieverPlugin plugin, List<SearchResult> results, Data data) {
			this.shell = shell;
			this.name = name;
			this.plugin = plugin;
			this.results = results;
			this.data = data;
		}
		private Shell shell;
		private String name;
		private InfoRetrieverPlugin plugin;
		private List<SearchResult> results;
		private Data data;
		private SearchResult result = null;
		public void run() {
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
			dlgQ.setMessage(results.size() + " " + Local.results_have_been_found + " " + Local._for + " '<a href=\"open\">" + name + "</a>' " + Local.on + " " + plugin.getName() + ".");
			dlgQ.handleHyperlinkMessage("open", new RunnableWithData<Data>(data) {
				public void run() {
					DataListMenu.menu(data(), false);
				}
			});
			dlgQ.show();
			QuestionDlg.Answer answer = dlgQ.getAnswer();
			if (answer == null) 
				name = null;
			else if (answer.id.equals("*change_name"))
				name = ((QuestionDlg.AnswerText)answer).text;
			else
				result = results.get(Integer.parseInt(answer.id));
		}
	}
	
	private static enum MergeResponse {
		NO_MERGE, MERGE, BAD_SOURCE_INFO
	}
	private static MergeResponse askToMerge(Shell shell, Data data, Data sameData, String sourceID) {
		synchronized (oneQuestionAtATime) {
			AskToMerge ask = new AskToMerge(shell, data, sameData, sourceID);
			shell.getDisplay().syncExec(ask);
			return ask.response;
		}
	}
	private static class AskToMerge implements Runnable {
		public AskToMerge(Shell shell, Data data, Data sameData, String sourceID) {
			this.shell = shell;
			this.data = data;
			this.sameData = sameData;
			this.sourceID = sourceID;
		}
		private Shell shell;
		private Data data;
		private Data sameData;
		private String sourceID;
		private MergeResponse response = MergeResponse.NO_MERGE;
		public void run() {
			QuestionDlg dlgQ = new QuestionDlg(shell, Local.Duplicate_data.toString(), null);
			QuestionDlg.Answer[] answers = new QuestionDlg.Answer[3];
			answers[0] = new QuestionDlg.AnswerSimple("no", Local.Do_not_merge_the_two_data.toString());
			answers[1] = new QuestionDlg.AnswerSimple("yes", Local.Merge_the_two_data.toString());
			answers[2] = new QuestionDlg.AnswerSimple("bad", Local.The_retrieved_information_is_not_the_correct_one_and_must_be_removed.toString());
			dlgQ.setAnswers(answers);
			dlgQ.setMessage(Local.process(Local.MESSAGE_AskToMerge, data.getName(), sameData.getName(), InfoRetrieverPluginRegistry.getNameForSource(sourceID, data.getContentType().getID())));
			dlgQ.handleHyperlinkMessage("data", new RunnableWithData<Data>(data) {
				public void run() {
					DataListMenu.menu(data(), false);
				}
			});
			dlgQ.handleHyperlinkMessage("sameData", new RunnableWithData<Data>(sameData) {
				public void run() {
					DataListMenu.menu(data(), false);
				}
			});
			dlgQ.show();
			QuestionDlg.Answer answer = dlgQ.getAnswer();
			if (answer == null)
				response = MergeResponse.NO_MERGE;
			else if (answer.id.equals("no"))
				response = MergeResponse.NO_MERGE;
			else if (answer.id.equals("yes"))
				response = MergeResponse.MERGE;
			else if (answer.id.equals("bad"))
				response = MergeResponse.BAD_SOURCE_INFO;
		}
	}
}
