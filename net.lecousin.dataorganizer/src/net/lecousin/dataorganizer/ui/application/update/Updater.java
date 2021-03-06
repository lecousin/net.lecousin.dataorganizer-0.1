package net.lecousin.dataorganizer.ui.application.update;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.net.SocketFactory;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.internal.EclipsePlugin;
import net.lecousin.framework.Pair;
import net.lecousin.framework.application.Application;
import net.lecousin.framework.collections.SortedListTree;
import net.lecousin.framework.io.FileSystemUtil;
import net.lecousin.framework.io.IOUtil;
import net.lecousin.framework.io.ZipUtil;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.net.http.HttpUtil;
import net.lecousin.framework.net.http.client.HttpClient;
import net.lecousin.framework.net.http.client.HttpRequest;
import net.lecousin.framework.net.http.client.HttpResponse;
import net.lecousin.framework.net.mime.Mime;
import net.lecousin.framework.net.mime.content.MimeContent;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.thread.RunnableWithData;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.text.lcml.LCMLText;
import net.lecousin.framework.ui.eclipse.dialog.FlatDialog;
import net.lecousin.framework.ui.eclipse.dialog.QuestionDlg;
import net.lecousin.framework.ui.eclipse.dialog.QuestionDlg.Answer;
import net.lecousin.framework.ui.eclipse.progress.WorkProgressDialog;
import net.lecousin.framework.version.Version;
import net.lecousin.framework.xml.XmlUtil;

import org.apache.http.HttpStatus;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Element;

public class Updater {

	private static final String update_host = "dataorganizer.webhop.net";
	private static final String update_path = "/update/update.xml";
	
	private static final String update_tracker_host = "am1.activemeter.com";
	private static final String update_tracker_path = "/webtracker/track.html?method=track&pid=56538&java=0";
	private static final String install_tracker_host = "am1.activemeter.com";
	private static final String install_tracker_path = "/webtracker/track.html?method=track&pid=56539&java=0";
	            
	
	public static class UpdateException extends Exception {
		private static final long serialVersionUID = -3371309807726490506L;
		UpdateException(String message) {
			super(message);
		}
	}
	
	public static boolean isMySelf() {
		if (!"C:\\Users\\Guillaume".equals(System.getProperty("user.home"))) return false;
		if (!"GUILLAUME-PORT".equals(System.getenv("COMPUTERNAME"))) return false;
		if (Log.debug(Updater.class))
			Log.debug(Updater.class, "This is me, so do not track update..");
		return true;
	}
	
	public static boolean signalInstallation() {
		HttpClient client = new HttpClient(SocketFactory.getDefault());
		HttpRequest req;
		try {
			Version v = getCurrentVersion();
			req = new HttpRequest("dataorganizer.webhop.net", "/track/track.php?type=install&version=" + (isMySelf() ? "ME" : "") + v.toString());
			try { client.send(req, true, null, 0); }
			catch (IOException e) {
				if (Log.warning(Updater.class))
					Log.warning(Updater.class, "Unable to track installation", e);
				return false;
			}
		} catch (UpdateException e) {
			if (Log.warning(Updater.class))
				Log.warning(Updater.class, "Unable to track installation", e);
		}
		if (isMySelf()) return true;
		req = new HttpRequest(install_tracker_host, install_tracker_path);
		try { client.send(req, true, null, 0); }
		catch (IOException e) {
			if (Log.warning(Updater.class))
				Log.warning(Updater.class, "Unable to track installation", e);
			return false;
		}
		return true;
	}
	public static void signalLaunch(long count) {
		if (isMySelf()) return;
		Thread t = new Thread(new SignalLaunch(count));
		t.start();
	}
	private static class SignalLaunch implements Runnable {
		SignalLaunch(long count) { this.count = count; }
		private long count;
		public void run() {
			HttpClient client = new HttpClient(SocketFactory.getDefault());
			HttpRequest req;
			try {
				Version v = getCurrentVersion();
				req = new HttpRequest("dataorganizer.webhop.net", "/track/track.php?type=launch&version=" + (isMySelf() ? "ME" : "") + v.toString()+"&lang="+Application.language.toString()+"&count="+count);
				try { client.send(req, true, null, 0); }
				catch (IOException e) {
					if (Log.warning(Updater.class))
						Log.warning(Updater.class, "Unable to track launch", e);
				}
			} catch (UpdateException e) {
				if (Log.warning(Updater.class))
					Log.warning(Updater.class, "Unable to track installation", e);
			}
		}
	}
	
	public static class Update {
		private Update(Version currentVersion) {
			this.currentVersion = currentVersion;
		}
		private void newNode(Element node) {
			if (!node.hasAttribute("version")) {
				if (Log.error(Updater.class))
					Log.error(Updater.class, "update.xml contains an invalid update node: missing attribute 'version'.");
				return;
			}
			String version = node.getAttribute("version");
			if (version.equals("DEBUG")) {
				if (!Application.isDebugEnabled || !isMySelf())
					return;
				version = node.getAttribute("simulated");
			}
			Version v = new Version(version);
			if (!node.hasAttribute("jre")) {
				if (Log.error(Updater.class))
					Log.error(Updater.class, "update.xml contains an invalid update node: missing attribute 'jre'.");
				return;
			}
			Version jre = new Version(node.getAttribute("jre"));
			if (!node.hasAttribute("extras")) {
				if (Log.error(Updater.class))
					Log.error(Updater.class, "update.xml contains an invalid update node: missing attribute 'extras'.");
				return;
			}
			Version extras = new Version(node.getAttribute("extras"));
			if (v.compareTo(currentVersion)<0) return; // older version: skip
			if (v.compareTo(currentVersion) == 0) {
				currentJRE = jre;
				currentExtras = extras;
				return;
			}
			// new version
			newVersions.add(new Pair<Version,Element>(v, node));
		}
		
		private boolean end(Element root, WorkProgress progress) {
			if (newVersions.isEmpty()) return false;
			Element latest = null;
			for (Pair<Version,Element> p : newVersions) {
				newVersion = p.getValue1();
				latest = p.getValue2();
				Element app = XmlUtil.get_child_element(latest, "application"); 
				if (app != null && isValid(app)) {
					applicationFiles = getFiles(app);
					plugins.clear();
				} else {
					for (Element e : XmlUtil.get_childs_element(latest, "plugin")) {
						String name = e.getAttribute("name");
						List<UpdateFile> list = plugins.get(name);
						if (list == null) {
							list = new LinkedList<UpdateFile>();
							plugins.put(name, list);
						}
						UpdateFile file = new UpdateFile();
						file.host = e.getAttribute("host");
						try { file.port = Integer.parseInt(e.getAttribute("port")); }
						catch (NumberFormatException ex) { file.port = 80; }
						file.path = XmlUtil.get_inner_text(e);
						list.add(file);
					}
				}
			}
			Version newJRE = new Version(latest.getAttribute("jre"));
			Version newExtras = new Version(latest.getAttribute("extras"));
			if (currentJRE == null || newJRE.compareTo(currentJRE)>0)
				jreFiles = getFiles(getNode(root, "jre", newJRE));
			if (currentExtras == null || newExtras.compareTo(currentExtras)>0)
				extrasFiles = getFiles(getNode(root, "extras", newExtras));
			updaterFiles = getFiles(XmlUtil.get_child_element(latest, "updater"));
			progress.reset(Local.A_new_version_has_been_found__Retrieving_information_about_this_version.toString(), 10000);
			news = getNews(root, currentVersion, newVersion, progress, 10000);
			return true;
		}
		private boolean isValid(Element appNode) {
			if (appNode.hasAttribute("less_than")) {
				Version v = new Version(appNode.getAttribute("less_than"));
				if (currentVersion.compareTo(v) < 0)
					return true;
				return false;
			}
			return true;
		}

		private Version currentVersion;
		private Version currentJRE;
		private Version currentExtras;
		private SortedListTree<Pair<Version,Element>> newVersions = new SortedListTree<Pair<Version,Element>>(new Comparator<Pair<Version,Element>>() {
			public int compare(Pair<Version, Element> o1, Pair<Version, Element> o2) {
				return o1.getValue1().compareTo(o2.getValue1());
			}
		});
		private Version newVersion;
		private String news;
		private List<UpdateFile> updaterFiles;
		private List<UpdateFile> applicationFiles;
		private List<UpdateFile> jreFiles;
		private List<UpdateFile> extrasFiles;
		private Map<String,List<UpdateFile>> plugins = new HashMap<String,List<UpdateFile>>();
		
		private Element getNode(Element root, String name, Version version) {
			return XmlUtil.get_child_with_attr(root, name, "version", version.toString());
		}
		private List<UpdateFile> getFiles(Element node) {
			if (node == null) return null;
			List<UpdateFile> list = new LinkedList<UpdateFile>();
			for (Element e : XmlUtil.get_childs_element(node, "file")) {
				UpdateFile file = new UpdateFile();
				file.host = e.getAttribute("host");
				try { file.port = Integer.parseInt(e.getAttribute("port")); }
				catch (NumberFormatException ex) { file.port = 80; }
				file.path = XmlUtil.get_inner_text(e);
				list.add(file);
			}
			return list;
		}
	}
	private static class UpdateFile {
		String host;
		int port;
		String path;
	}
	
	public static Update getLatestVersionInfo(Shell shell) throws UpdateException {
		WorkProgress progress = new WorkProgress(Local.Retrieving_update_information.toString(), 10000, true);
		WorkProgressDialog dlg = new WorkProgressDialog(shell, progress);

		try {
			HttpClient client = new HttpClient(SocketFactory.getDefault());
			HttpRequest req;
			HttpResponse resp;
			Version current = getCurrentVersion();
			req = new HttpRequest("dataorganizer.webhop.net", "/track/track.php?type=update&version=" + (isMySelf() ? "ME" : "") + current.toString());
			try { client.send(req, true, null, 0); }
			catch (IOException e) {
				if (Log.warning(Updater.class))
					Log.warning(Updater.class, "Unable to track update", e);
			}
			if (!isMySelf()) {
				req = new HttpRequest(update_tracker_host, update_tracker_path);
				try { resp = client.send(req, true, progress, 9500); }
				catch (IOException e) {
					if (Log.warning(Updater.class))
						Log.warning(Updater.class, "Unable to track update", e);
				}
			}
			req = new HttpRequest(update_host, update_path);
			try { resp = client.send(req, true, null, 0); }
			catch (IOException e) {
				if (Log.error(Updater.class))
					Log.error(Updater.class, "Unable to get update.xml file", e);
				throw new UpdateException(Local.ERROR_UPDATE_WEB_CONNECTION.toString());
			}
			if (resp.getStatusCode() != HttpStatus.SC_OK) {
				if (Log.error(Updater.class))
					Log.error(Updater.class, "Unable to get update.xml file: status = " + resp.getStatusCode() + " (" + resp.getStatusDescription() + ")");
				throw new UpdateException(Local.ERROR_UPDATE_WEB_SITE.toString());
			}			
			Mime mime = resp.getContent();
			if (mime == null) {
				if (Log.error(Updater.class))
					Log.error(Updater.class, "Unable to get update.xml file content: no MIME in response.");
				throw new UpdateException(Local.ERROR_UPDATE_WEB_SITE.toString());
			}			
			MimeContent content = mime.getContent();
			if (content == null) {
				if (Log.error(Updater.class))
					Log.error(Updater.class, "Unable to get update.xml file content: no MIME content.");
				throw new UpdateException(Local.ERROR_UPDATE_WEB_SITE.toString());
			}			
			String xml;
			try { xml = content.getAsString(); }
			catch (IOException e) {
				if (Log.error(Updater.class))
					Log.error(Updater.class, "Unable to get update.xml file content", e);
				throw new UpdateException(Local.ERROR_UPDATE_WEB_SITE.toString());
			}
			
			Element root;
			try { root = XmlUtil.loadFile(new ByteArrayInputStream(xml.getBytes())); }
			catch (Throwable t) {
				if (Log.error(Updater.class))
					Log.error(Updater.class, "Unable to parse update.xml", t);
				throw new UpdateException(Local.ERROR_UPDATE_WEB_SITE.toString());
			}
			progress.progress(450);
			
			if (!root.getNodeName().equals("dataorganizer")) {
				if (Log.error(Updater.class))
					Log.error(Updater.class, "Invalid update.xml file: expected root node is <dataorganizer>");
				throw new UpdateException(Local.ERROR_UPDATE_WEB_SITE.toString());
			}
			
			Update update = new Update(current);
			for (Element e : XmlUtil.get_childs_element(root, "update"))
				update.newNode(e);
			progress.progress(50);

			if (!update.end(root, progress)) return null;
			return update;
		} finally {
			dlg.close();
		}
	}
	
	public static Version getCurrentVersion() throws UpdateException {
		String current = (String)EclipsePlugin.getDefault().getBundle().getHeaders().get("Bundle-Version");
		if (current == null || current.length() == 0) {
			if (Log.error(Updater.class))
				Log.error(Updater.class, "Unable to get Bundle-Version.");
			throw new UpdateException(Local.ERROR_UPDATE_INTERNAL.toString());
		}
		return new Version(current);
	}
	
	private static String getNews(Element root, Version currentVersion, Version newVersion, WorkProgress progress, int work) {
		StringBuilder str = new StringBuilder();
		List<String> urls = new LinkedList<String>();
		for (Element e : XmlUtil.get_childs_element(root, "news")) {
			Version v = new Version(e.getAttribute("version"));
			if (currentVersion.compareTo(v) >= 0) continue;
			String url = null;
			for (Element e2 : XmlUtil.get_childs_element(e, "content")) {
				if (e2.getAttribute("language").equals(Application.language.name())) {
					url = e2.getAttribute("url");
					break;
				}
			}
			if (url == null) continue;
			urls.add(url);
		}
		HttpClient client = new HttpClient(SocketFactory.getDefault());
		int nb = urls.size();
		for (String url : urls) {
			HttpRequest req = new HttpRequest(update_host, url);
			HttpResponse resp;
			int step = work/nb--;
			work -= step;
			try { resp = client.send(req, true, progress, step); }
			catch (IOException ex) { continue; }
			Mime mime = resp.getContent();
			if (mime == null) continue;
			MimeContent content = mime.getContent();
			if (content == null) continue;
			try { str.append(content.getAsString()); }
			catch (IOException ex) { continue; }
		}
		return str.toString();
	}
	
	public static boolean askToUpdate(Shell shell, Update update) {
		QuestionDlg dlg = new QuestionDlg(shell, "DataOrganizer update", null);
		dlg.setMessage(Local.process(Local.MESSAGE_Update_Available, update.currentVersion.toString(), update.newVersion.toString()));
		dlg.handleHyperlinkMessage("news", new RunnableWithData<Pair<Shell,String>>(new Pair<Shell,String>(dlg.getShell(), update.news)) {
			public void run() {
				FlatDialog dlg = new FlatDialog(data().getValue1(), Local.Update_application.toString(), true, true) {
					@Override
					protected void createContent(Composite container) {
						LCMLText text = new LCMLText(container, false, false);
						text.setText(data().getValue2());
						text.setLayoutData(UIUtil.gridData(1, true, 1, false));
					}
				};
				dlg.open(true);
			}
		});
		dlg.setAnswers(new Answer[] {
			new QuestionDlg.AnswerSimple("update", Local.MESSAGE_Update_Now.toString()),
			new QuestionDlg.AnswerSimple("later", Local.MESSAGE_Update_Later.toString())
		});
		dlg.show();
		String answer = dlg.getAnswerID();
		if (answer == null || !answer.equals("update"))
			return false;
		return true;
	}
	
	public static void launchUpdate(Shell shell, Update update) throws UpdateException {
		WorkProgress progress = new WorkProgress(Local.Downloading_latest_version.toString(), 10000, true);
		WorkProgressDialog dlg = new WorkProgressDialog(shell, progress);
		
		int amount = 10000;
		int stepInit = 20;
		int stepFinalize = 480;
		int stepDownload = amount - stepInit - stepFinalize;
		
		try {
			File updateTmp = new File(Application.deployPath, "update-tmp");
			FileSystemUtil.deleteDirectory(updateTmp);
			updateTmp.mkdirs();
			progress.progress(20);
			
			if (update.updaterFiles == null || update.updaterFiles.isEmpty())
				throw new UpdateException(Local.ERROR_UPDATE_INTERNAL.toString());
			if (update.applicationFiles != null && update.applicationFiles.isEmpty())
				throw new UpdateException(Local.ERROR_UPDATE_INTERNAL.toString());
			if (update.jreFiles != null && update.jreFiles.isEmpty())
				throw new UpdateException(Local.ERROR_UPDATE_INTERNAL.toString());
			if (update.extrasFiles != null && update.extrasFiles.isEmpty())
				throw new UpdateException(Local.ERROR_UPDATE_INTERNAL.toString());
			
			int nb = update.updaterFiles.size() + update.applicationFiles.size();
			if (update.jreFiles != null)
				nb += update.jreFiles.size();
			if (update.extrasFiles != null)
				nb += update.extrasFiles.size();
			for (List<UpdateFile> list : update.plugins.values())
				nb += list.size();
			
			int step;
			step = stepDownload*update.updaterFiles.size()/nb;
			stepDownload -= step; nb -= update.updaterFiles.size();
			downloadFile(update.updaterFiles, updateTmp, "updater.jar", progress, step);
			if (update.jreFiles != null) {
				step = stepDownload*update.jreFiles.size()/nb;
				stepDownload -= step; nb -= update.jreFiles.size();
				downloadFile(update.jreFiles, updateTmp, "jre.zip", progress, step);
			}
			if (update.extrasFiles != null) {
				step = stepDownload*update.extrasFiles.size()/nb;
				stepDownload -= step; nb -= update.extrasFiles.size();
				downloadFile(update.extrasFiles, updateTmp, "extras.zip", progress, step);
			}
			for (Map.Entry<String, List<UpdateFile>> e : update.plugins.entrySet()) {
				step = stepDownload*e.getValue().size()/nb;
				stepDownload -= step; nb -= e.getValue().size();
				downloadFile(e.getValue(), updateTmp, "plugin_"+e.getKey()+".zip", progress, step);
			}
			step = stepDownload;
			stepDownload -= step; nb -= update.applicationFiles.size();
			downloadFile(update.applicationFiles, updateTmp, "application.zip", progress, step);
			
			int stepJRE = stepFinalize*80/100;
			int stepRun = stepFinalize - stepJRE;
			try {
				if (update.jreFiles != null) {
					progress.setSubDescription(Local.Extracting_files+"...");
					ZipUtil.unzip(new File(updateTmp, "jre.zip"), new File(updateTmp, "jre"), progress, stepJRE);
				} else {
					progress.setSubDescription(Local.Copying_files+"...");
					FileSystemUtil.copyDirectory(new File(Application.deployPath, "jre"), new File(updateTmp, "jre"), progress, stepJRE);
				}
	
				progress.setSubDescription("");
				Runtime.getRuntime().exec(updateTmp.getAbsolutePath()+"\\jre\\bin\\java.exe -cp \"" + updateTmp.getAbsolutePath() + "\\updater.jar\" -jar \"" + updateTmp.getAbsolutePath() + "\\updater.jar\" -deployPath \"" + Application.deployPath.getAbsolutePath() + "\"");
				progress.progress(stepRun);
				progress.done();
			} catch (IOException e) {
				if (Log.error(Updater.class))
					Log.error(Updater.class, "Unable to prepare/launch updater", e);
				throw new UpdateException(Local.ERROR_UPDATE_FILESYSTEM.toString());
			}
		} finally {
			dlg.close();
		}
	}
	
	private static void downloadFile(List<UpdateFile> files, File targetDir, String targetFilename, WorkProgress progress, int amount) throws UpdateException {
		int nb = files.size();
		int i = 0;
		int total = amount*95/100;
		amount -= total;
		for (UpdateFile file : files) {
			int step = total/(nb--);
			total -= step;
			if (!HttpUtil.retrieveFile(file.host, file.port, file.path, new File(targetDir, "file"+(i++)), true, progress, step)) {
				if (Log.error(Updater.class))
					Log.error(Updater.class, "Unable to retrieve file: " + file.path + " on " + file.host + ":" + file.port);
				throw new UpdateException(Local.ERROR_UPDATE_WEB.toString());
			}
		}
		
		try {
			File file = new File(targetDir, targetFilename);
			file.createNewFile();
			FileOutputStream out = new FileOutputStream(file);
			i = 0;
			do {
				file = new File(targetDir, "file"+(i++));
				if (!file.exists()) break;
				FileInputStream in = new FileInputStream(file);
				IOUtil.copy(in, out);
				in.close();
				file.delete();
			} while (true);
			out.flush();
			out.close();
		} catch (IOException e) {
			if (Log.error(Updater.class))
				Log.error(Updater.class, "Unable to build file: " + targetFilename, e);
			throw new UpdateException(Local.ERROR_UPDATE_FILESYSTEM.toString());
		}
		progress.progress(amount);
	}
	
}
