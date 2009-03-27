package net.lecousin.dataorganizer.ui.application.update;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.net.SocketFactory;

import net.lecousin.dataorganizer.Local;
import net.lecousin.dataorganizer.internal.EclipsePlugin;
import net.lecousin.framework.Pair;
import net.lecousin.framework.Triple;
import net.lecousin.framework.application.Application;
import net.lecousin.framework.io.FileSystemUtil;
import net.lecousin.framework.io.IOUtil;
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
	
	private static boolean isMySelf() {
		if (!"C:\\Users\\Guillaume".equals(System.getProperty("user.home"))) return false;
		if (!"GUILLAUME-PORT".equals(System.getenv("COMPUTERNAME"))) return false;
		if (Log.debug(Updater.class))
			Log.debug(Updater.class, "This is me, so do not track update..");
		return true;
	}
	
	public static boolean signalInstallation() {
		if (isMySelf()) return true;
		HttpClient client = new HttpClient(SocketFactory.getDefault());
		HttpRequest req = new HttpRequest(install_tracker_host, install_tracker_path);
		try { client.send(req, true, null, 0); return true; }
		catch (IOException e) {
			if (Log.warning(Updater.class))
				Log.warning(Updater.class, "Unable to track installation", e);
			return false;
		}
	}
	
	public static Triple<Version,Element,Element> getLatestVersionInfo() throws UpdateException {
		HttpClient client = new HttpClient(SocketFactory.getDefault());
		HttpRequest req;
		HttpResponse resp;
		if (!isMySelf()) {
			req = new HttpRequest(update_tracker_host, update_tracker_path);
			try { resp = client.send(req, true, null, 0); }
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
		
		if (!root.getNodeName().equals("dataorganizer")) {
			if (Log.error(Updater.class))
				Log.error(Updater.class, "Invalid update.xml file: expected root node is <dataorganizer>");
			throw new UpdateException(Local.ERROR_UPDATE_WEB_SITE.toString());
		}
		
		Version latest = null;
		Element updateNode = null;
		for (Element e : XmlUtil.get_childs_element(root, "update")) {
			String version = e.getAttribute("version");
			if (version.equals("DEBUG")) {
				if (!Application.isDebugEnabled)
					continue;
				version = e.getAttribute("simulated");
			}
			Version v = new Version(version);
			if (latest == null || v.compareTo(latest) > 0) {
				latest = v;
				updateNode = e;
			}
		}
			
		if (latest == null) {
			if (Log.error(Updater.class))
				Log.error(Updater.class, "Invalid update.xml file: no valid update node.");
			throw new UpdateException(Local.ERROR_UPDATE_WEB_SITE.toString());
		}
		
		if (Log.debug(Updater.class))
			Log.debug(Updater.class, "update.xml found: version=" + latest.toString());
		
		return new Triple<Version,Element,Element>(latest, root, updateNode);
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
	
	public static String getNews(Element root, Version currentVersion, Version newVersion) {
		StringBuilder str = new StringBuilder();
		HttpClient client = new HttpClient(SocketFactory.getDefault());
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
			HttpRequest req = new HttpRequest(update_host, url);
			HttpResponse resp;
			try { resp = client.send(req, true, null, 0); }
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
	
	public static boolean askToUpdate(Shell shell, Version currentVersion, Version newVersion, String whatsnew) {
		QuestionDlg dlg = new QuestionDlg(shell, "DataOrganizer update", null);
		dlg.setMessage(Local.process(Local.MESSAGE_Update_Available, currentVersion.toString(), newVersion.toString()));
		dlg.handleHyperlinkMessage("news", new RunnableWithData<Pair<Shell,String>>(new Pair<Shell,String>(dlg.getShell(), whatsnew)) {
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
	
	public static void launchUpdate(Shell shell, Element updateNode) throws UpdateException {
		WorkProgress progress = new WorkProgress(Local.Downloading_latest_version.toString(), 10000, true);
		WorkProgressDialog dlg = new WorkProgressDialog(shell, progress);
		File updateTmp = new File(Application.deployPath, "update-tmp");
		FileSystemUtil.deleteDirectory(updateTmp);
		updateTmp.mkdirs();
		progress.progress(20);

		List<String> urls = new LinkedList<String>();
		for (Element e : XmlUtil.get_childs_element(updateNode, "updater_file"))
			urls.add(XmlUtil.get_inner_text(e));
		if (urls.isEmpty()) {
			if (Log.error(Updater.class))
				Log.error(Updater.class, "No update_file node in the update.xml file.");
			throw new UpdateException(Local.ERROR_UPDATE_WEB_SITE.toString());
		}
		downloadFile(urls, updateTmp, "updater.jar", progress, 500);

		urls.clear();
		for (Element e : XmlUtil.get_childs_element(updateNode, "application_file"))
			urls.add(XmlUtil.get_inner_text(e));
		if (urls.isEmpty()) {
			if (Log.error(Updater.class))
				Log.error(Updater.class, "No application_file node in the update.xml file.");
			throw new UpdateException(Local.ERROR_UPDATE_WEB_SITE.toString());
		}
		downloadFile(urls, updateTmp, "update.zip", progress, 9000);
		
		try {
			FileSystemUtil.copyDirectory(new File(Application.deployPath, "jre"), new File(updateTmp, "jre"), progress, 300);
			Runtime.getRuntime().exec(updateTmp.getAbsolutePath()+"\\jre\\bin\\java.exe -cp \"" + updateTmp.getAbsolutePath() + "\" -jar \"" + updateTmp.getAbsolutePath() + "\\updater.jar\" -deployPath \"" + Application.deployPath.getAbsolutePath() + "\"");
			progress.progress(180);
			progress.done();
			dlg.close();
		} catch (IOException e) {
			if (Log.error(Updater.class))
				Log.error(Updater.class, "Unable to prepare/launch updater", e);
			throw new UpdateException(Local.ERROR_UPDATE_FILESYSTEM.toString());
		}
	}
	
	private static void downloadFile(List<String> urls, File targetDir, String targetFilename, WorkProgress progress, int amount) throws UpdateException {
		int nb = urls.size();
		int i = 0;
		int total = amount*95/100;
		amount -= total;
		for (String url : urls) {
			int step = total/(nb--);
			total -= step;
			if (!HttpUtil.retrieveFile(update_host, 80, url, new File(targetDir, "file"+(i++)), true, progress, step)) {
				if (Log.error(Updater.class))
					Log.error(Updater.class, "Unable to retrieve file: " + url);
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
