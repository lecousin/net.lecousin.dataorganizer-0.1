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
import net.lecousin.framework.ui.eclipse.dialog.QuestionDlg;
import net.lecousin.framework.ui.eclipse.dialog.QuestionDlg.Answer;
import net.lecousin.framework.ui.eclipse.progress.WorkProgressDialog;
import net.lecousin.framework.version.Version;
import net.lecousin.framework.xml.XmlParsingUtil;
import net.lecousin.framework.xml.XmlUtil;
import net.lecousin.framework.xml.XmlParsingUtil.Node;

import org.apache.http.HttpStatus;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Element;

public class Updater {

	private static final String update_host = "dataorganizer.webhop.net";
	private static final String update_path = "/download/update-software/update.xml";
	private static final String update_tracker_path = "/download/update-software/update-tracker";
	
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
	
	public static Pair<Version,String> getLatestVersionInfo() throws UpdateException {
		HttpClient client = new HttpClient(SocketFactory.getDefault());
		HttpRequest req;
		HttpResponse resp;
		if (!isMySelf()) {
			req = new HttpRequest(update_host, update_tracker_path);
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
		Triple<Node,Boolean,Integer> t = XmlParsingUtil.parseOpenNode(xml, 0);
		Node node = t.getValue1();
		if (!node.name.equals("dataorganizer")) {
			if (Log.error(Updater.class))
				Log.error(Updater.class, "Invalid update.xml file: expected node is <dataorganizer...");
			throw new UpdateException(Local.ERROR_UPDATE_WEB_SITE.toString());
		}			
			
		String version = node.attributes.get("version");
		
		if (version == null || version.length() == 0) {
			if (Log.error(Updater.class))
				Log.error(Updater.class, "Invalid update.xml file: missing version attribute.");
			throw new UpdateException(Local.ERROR_UPDATE_WEB_SITE.toString());
		}
		
		if (Log.debug(Updater.class))
			Log.debug(Updater.class, "update.xml found: version=" + version);
		
		return new Pair<Version,String>(new Version(version), xml);
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
	
	public static boolean askToUpdate(Shell shell, Version currentVersion, Version newVersion) {
		QuestionDlg dlg = new QuestionDlg(shell, "DataOrganizer update", null);
		dlg.setMessage(Local.process(Local.MESSAGE_Update_Available, currentVersion.toString(), newVersion.toString()));
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
	
	public static void launchUpdate(Shell shell, String xml) throws UpdateException {
		WorkProgress progress = new WorkProgress(Local.Downloading_latest_version.toString(), 10000, true);
		WorkProgressDialog dlg = new WorkProgressDialog(shell, progress);
		Element root;
		try { root = XmlUtil.loadFile(new ByteArrayInputStream(xml.getBytes())); }
		catch (Throwable t) {
			if (Log.error(Updater.class))
				Log.error(Updater.class, "Unable to parse update.xml", t);
			throw new UpdateException(Local.ERROR_UPDATE_WEB_SITE.toString());
		}
		progress.progress(50);
		File updateTmp = new File(Application.deployPath, "update-tmp");
		FileSystemUtil.deleteDirectory(updateTmp);
		updateTmp.mkdirs();
		progress.progress(50);

		List<String> urls = new LinkedList<String>();
		for (Element e : XmlUtil.get_childs_element(root, "updater_file"))
			urls.add(XmlUtil.get_inner_text(e));
		if (urls.isEmpty()) {
			if (Log.error(Updater.class))
				Log.error(Updater.class, "No update_file node in the update.xml file.");
			throw new UpdateException(Local.ERROR_UPDATE_WEB_SITE.toString());
		}
		downloadFile(urls, updateTmp, "updater.jar", progress, 500);

		urls.clear();
		for (Element e : XmlUtil.get_childs_element(root, "application_file"))
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
			progress.progress(100);
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
