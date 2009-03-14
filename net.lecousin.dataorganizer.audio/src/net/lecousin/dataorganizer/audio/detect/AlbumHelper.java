package net.lecousin.dataorganizer.audio.detect;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.lecousin.dataorganizer.audio.AudioContentType;
import net.lecousin.dataorganizer.audio.AudioDataType;
import net.lecousin.dataorganizer.audio.AudioInfo;
import net.lecousin.dataorganizer.audio.Local;
import net.lecousin.dataorganizer.audio.internal.EclipsePlugin;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.VirtualData;
import net.lecousin.dataorganizer.core.database.VirtualDataBase;
import net.lecousin.dataorganizer.core.database.content.ContentType;
import net.lecousin.dataorganizer.core.database.source.DataSource;
import net.lecousin.framework.Triple;
import net.lecousin.framework.collections.CollectionUtil;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.strings.StringUtil;
import net.lecousin.framework.ui.eclipse.dialog.ErrorDlg;
import net.lecousin.media.sound.files.audio.AudioFile;
import net.lecousin.media.sound.files.audio.AudioFileInfo;
import net.lecousin.media.sound.files.audio.AudioFileInfo.Picture;
import net.lecousin.media.sound.files.playlist.PlayList;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Shell;

public class AlbumHelper {

	static class Track {
		Track(IFileStore file, AudioFile audio)
		{ this.file = file; this.audio = audio; trackNumber = audio != null && audio.getInfo() != null ? audio.getInfo().getTrackNumber() : -1; }
		IFileStore file;
		AudioFile audio;
		int trackNumber;
	}
	static class PlayListFile {
		PlayListFile(IFileStore file, PlayList list)
		{ this.file = file; this.list = list; }
		IFileStore file;
		PlayList list;
	}
	static class TrackComparator implements Comparator<Track> {
		public int compare(Track o1, Track o2) {
			return o1.trackNumber - o2.trackNumber;
		}
	}
	
	static List<VirtualData> createAlbumFromOrderedList(VirtualDataBase db, Iterable<Track> orderedTracks, PlayListFile playlist, String finalAlbumName, List<IFileStore> remainingFiles, IFileStore rootDir, Shell shell) {
		LinkedList<Track> tracks = new LinkedList<Track>();
		for (Track t : orderedTracks)
			tracks.add(t);
		return createAlbumFromOrderedList(db, tracks, playlist, finalAlbumName, remainingFiles, rootDir, shell);
	}
	static List<VirtualData> createAlbumFromOrderedList(VirtualDataBase db, List<Track> orderedTracks, PlayListFile playlist, String finalAlbumName, List<IFileStore> remainingFiles, IFileStore rootDir, Shell shell) {
		List<AudioFile> list = new ArrayList<AudioFile>(orderedTracks.size());
		for (Track p : orderedTracks) {
			remainingFiles.remove(p.file);
			list.add(p.audio);
		}
		VirtualData data = createAlbum(db, list, finalAlbumName, rootDir, playlist != null ? playlist.list : null, shell);
		if (data != null) return CollectionUtil.single_element_list(data);
		return null;
	}
	
	static VirtualData createAlbum(VirtualDataBase db, List<AudioFile> tracks, String finalAlbumName, IFileStore rootDir, PlayList list, Shell shell) {
		List<DataSource> sources = new ArrayList<DataSource>(tracks.size());
		for (AudioFile file : tracks)
			try { sources.add(DataSource.get(file.getURI())); }
			catch (Throwable t) {
				ErrorDlg.exception(Local.Create_Music_Album.toString(), "Unable to add DataSource", EclipsePlugin.ID, t);
				return null;
			}
		Triple<String,String,Integer> t = getAlbumArtistYear(tracks, list, finalAlbumName, rootDir, shell);
		if (t == null) return null;
		VirtualData data;
		String name = t.getValue1();
		if (t.getValue2() != null)
			name = t.getValue2() + " - " + name;
		try { data = (VirtualData)db.addData(name, ContentType.getContentType(AudioContentType.AUDIO_TYPE), sources); }
		catch (CoreException e) {
			return null;
		}
		AudioDataType content = (AudioDataType)data.getContent();
		AudioInfo info = (AudioInfo)content.getInfo();
		info.setAlbum(t.getValue1());
		if (t.getValue2() != null)
			info.setArtist(t.getValue2());
		if (t.getValue3() != null && t.getValue3() > 0)
			info.setYear(t.getValue3());
		for (String genre : getGenres(tracks, list, rootDir, shell))
			info.addGenre(genre);
		List<Picture> cover_front = new LinkedList<Picture>();
		List<Picture> cover_back = new LinkedList<Picture>();
		for (AudioFile file : tracks) {
			AudioFileInfo ai = file.getInfo();
			if (ai == null)
				info.newTrack();
			else {
				String title = ai.getSongTitle();
				long length = ai.getDuration();
				AudioInfo.Track track = info.newTrack(title, length);
				if (ai.getPictures() != null)
					for (Picture pic : ai.getPictures()) {
						switch (pic.type) {
						case COVER_FRONT: cover_front.add(pic); break;
						case COVER_BACK: cover_back.add(pic); break;
						default:
							String filename = saveImage(data, pic.data);
							if (filename != null)
								track.addImage(AudioInfo.FILE_SOURCE, pic.description, filename);
							break;
						}
					}
			}
		}
//		byte[] mcdi = getMCDI(tracks, list, rootDir, shell);
//		if (mcdi != null)
//			info.setMCDI(mcdi);
		return data;
	}
	
	static Triple<String,String,Integer> getAlbumArtistYear(List<AudioFile> tracks, PlayList list, String finalAlbumName, IFileStore rootDir, Shell shell) {
		Triple<String,String,Integer> result = new Triple<String,String,Integer>(null, null, null);
		Set<String> albumNames = new HashSet<String>();
		if (finalAlbumName != null)
			albumNames.add(finalAlbumName);
		else
			for (AudioFile file : tracks) {
				AudioFileInfo ai = file.getInfo();
				if (ai != null) {
					String name = normalize_name(ai.getAlbum());
					if (name != null && name.length() > 0)
						albumNames.add(name);
				}
			}
		Set<String> artistNames = new HashSet<String>();
		for (AudioFile file : tracks) {
			AudioFileInfo ai = file.getInfo();
			if (ai != null) {
				String name = normalize_name(ai.getArtist());
				if (name != null && name.length() > 0)
					artistNames.add(name);
			}
		}
		Set<Integer> years = new HashSet<Integer>();
		for (AudioFile file : tracks) {
			AudioFileInfo ai = file.getInfo();
			if (ai != null) {
				int year = ai.getYear();
				if (year > 0)
					years.add(year);
			}
		}
		boolean needDecision = false;
		boolean needAlbumName = false;
		if (albumNames.size() == 1) result.setValue1(albumNames.iterator().next());
		else {
			albumNames.add(normalize_name(rootDir.getName()));
			if (list != null)
				albumNames.add(normalize_name(list.getName()));
			needDecision = true;
			needAlbumName = true;
		}
		if (artistNames.size() == 1) result.setValue2(artistNames.iterator().next());
		else if (artistNames.size() > 1) needDecision = true;
		if (years.size() == 1) result.setValue3(years.iterator().next());
		else if (years.size() > 1) needDecision = true;
		
		if (!needDecision) return result;
		
		DecideAlbumInfoDialog dlg = new DecideAlbumInfoDialog(shell, tracks, needAlbumName ? albumNames : null, artistNames.size() > 1 ? artistNames : null, years.size() > 1 ? years : null, rootDir);
		if (dlg.open()) {
			if (needAlbumName)
				result.setValue1(dlg.getAlbumName());
			if (artistNames.size() > 1)
				result.setValue2(dlg.getArtistName());
			if (years.size() > 1)
				result.setValue3(dlg.getYear());
			return result;
		}
		return null;
	}
	
//	static String getAlbumName(List<AudioFile> tracks, PlayList list, IFileStore rootDir, Shell shell) {
//		Set<String> albumNames = new HashSet<String>();
//		for (AudioFile file : tracks) {
//			AudioFileInfo ai = file.getInfo();
//			if (ai != null) {
//				String name = ai.getAlbum();
//				if (name != null && name.length() > 0)
//					albumNames.add(name);
//			}
//		}
//		String album;
//		if (albumNames.size() == 1)
//			album = albumNames.iterator().next();
//		else {
//			albumNames.add(rootDir.getName());
//			if (list != null)
//				albumNames.add(list.getName());
//			album = askAlbumName(albumNames, rootDir, tracks, list, shell);
//		}
//		return album;
//	}
//	
//	static String askAlbumName(Set<String> names, IFileStore rootDir, List<AudioFile> tracks, PlayList list, Shell shell) {
//		QuestionDlg dlg = new QuestionDlg(shell, Local.Create_Music_Album.toString(), null);
//		StringBuilder tracksNames = new StringBuilder();
//		for (AudioFile file : tracks) {
//			if (tracksNames.length() > 0) tracksNames.append("<br>");
//			String s = file.getURI();
//			int i = s.lastIndexOf('/');
//			if (i >= 0) s = s.substring(i+1);
//			tracksNames.append(Local.File).append(": ").append(s);
//			AudioFileInfo info = file.getInfo();
//			if (info != null) {
//				s = info.getAlbum();
//				if (s != null && s.length() > 0)
//					tracksNames.append("; ").append(Local.Album).append(": ").append(s);
//				s = info.getSongTitle();
//				if (s != null && s.length() > 0)
//					tracksNames.append("; ").append(Local.Track).append(": ").append(s);
//			}
//		}
//		dlg.setMessage(Local.process(Local.MESSAGE_Album_Name, rootDir.toString(), tracksNames.toString()));
//		Answer[] answers = new Answer[names.size()+1];
//		int i = 0;
//		for (String name : names)
//			answers[i++] = new QuestionDlg.AnswerSimple(name, name); 
//		answers[i] = new QuestionDlg.AnswerText("/new_name", Local.Another_name.toString(), "", false, new IInputValidator() {
//			public String isValid(String newText) {
//				if (newText.length() == 0) return Local.The_name_cannot_be_empty.toString();
//				return null;
//			}
//		});
//		dlg.setAnswers(answers);
//		dlg.show();
//		String answer = dlg.getAnswerID();
//		if (answer == null) return null;
//		if (answer.equals("/new_name")) return ((QuestionDlg.AnswerText)dlg.getAnswer()).text;
//		return answer;
//	}
//
//	static String getArtist(List<AudioFile> tracks, PlayList list, IFileStore rootDir, Shell shell) {
//		Set<String> names = new HashSet<String>();
//		for (AudioFile file : tracks) {
//			AudioFileInfo ai = file.getInfo();
//			if (ai != null) {
//				String name = ai.getArtist();
//				if (name != null && name.length() > 0)
//					names.add(name);
//			}
//		}
//		if (names.isEmpty()) return null;
//		if (names.size() == 1) return names.iterator().next();
//
//		QuestionDlg dlg = new QuestionDlg(shell, Local.Create_Music_Album.toString(), null);
//		StringBuilder tracksNames = new StringBuilder();
//		for (AudioFile file : tracks) {
//			if (tracksNames.length() > 0) tracksNames.append("<br>");
//			String s = file.getURI();
//			int i = s.lastIndexOf('/');
//			if (i >= 0) s = s.substring(i+1);
//			tracksNames.append(Local.File).append(": ").append(s);
//			AudioFileInfo info = file.getInfo();
//			if (info != null) {
//				s = info.getArtist();
//				if (s != null && s.length() > 0)
//					tracksNames.append("; ").append(Local.Artist).append(": ").append(s);
//				s = info.getSongTitle();
//				if (s != null && s.length() > 0)
//					tracksNames.append("; ").append(Local.Track).append(": ").append(s);
//			}
//		}
//		dlg.setMessage(Local.process(Local.MESSAGE_Artist_Name, rootDir.toString(), tracksNames.toString()));
//		Answer[] answers = new Answer[names.size()+1];
//		int i = 0;
//		for (String name : names)
//			answers[i++] = new QuestionDlg.AnswerSimple(name, name); 
//		answers[i] = new QuestionDlg.AnswerText("/new_name", Local.Another_name.toString(), "", false, new IInputValidator() {
//			public String isValid(String newText) {
//				if (newText.length() == 0) return Local.The_name_cannot_be_empty.toString();
//				return null;
//			}
//		});
//		dlg.setAnswers(answers);
//		dlg.show();
//		String answer = dlg.getAnswerID();
//		if (answer == null) return null;
//		if (answer.equals("/new_name")) return ((QuestionDlg.AnswerText)dlg.getAnswer()).text;
//		return answer;
//	}
//
//	static int getYear(List<AudioFile> tracks, PlayList list, IFileStore rootDir, Shell shell) {
//		Set<Integer> names = new HashSet<Integer>();
//		for (AudioFile file : tracks) {
//			AudioFileInfo ai = file.getInfo();
//			if (ai != null) {
//				int year = ai.getYear();
//				if (year > 0)
//					names.add(year);
//			}
//		}
//		if (names.isEmpty()) return -1;
//		if (names.size() == 1) return names.iterator().next();
//
//		QuestionDlg dlg = new QuestionDlg(shell, Local.Create_Music_Album.toString(), null);
//		StringBuilder tracksNames = new StringBuilder();
//		for (AudioFile file : tracks) {
//			if (tracksNames.length() > 0) tracksNames.append("<br>");
//			String s = file.getURI();
//			int i = s.lastIndexOf('/');
//			if (i >= 0) s = s.substring(i+1);
//			tracksNames.append(Local.File).append(": ").append(s);
//			AudioFileInfo info = file.getInfo();
//			if (info != null) {
//				i = info.getYear();
//				if (i > 0)
//					tracksNames.append("; ").append(Local.Year).append(": ").append(s);
//				s = info.getSongTitle();
//				if (s != null && s.length() > 0)
//					tracksNames.append("; ").append(Local.Track).append(": ").append(s);
//			}
//		}
//		dlg.setMessage(Local.process(Local.MESSAGE_Year, rootDir.toString(), tracksNames.toString()));
//		Answer[] answers = new Answer[names.size()+1];
//		int i = 0;
//		for (Integer name : names)
//			answers[i++] = new QuestionDlg.AnswerSimple(name.toString(), name.toString()); 
//		answers[i] = new QuestionDlg.AnswerText("/new_name", Local.Another_year.toString(), "", false, new IInputValidator() {
//			public String isValid(String newText) {
//				if (newText.length() == 0) return Local.The_year_cannot_be_empty.toString();
//				try { Integer.parseInt(newText); } catch (NumberFormatException e) { return Local.The_year_must_be_a_number.toString(); }
//				return null;
//			}
//		});
//		dlg.setAnswers(answers);
//		dlg.show();
//		String answer = dlg.getAnswerID();
//		if (answer == null) return -1;
//		if (answer.equals("/new_name")) try { return Integer.parseInt(((QuestionDlg.AnswerText)dlg.getAnswer()).text); } catch (NumberFormatException e) { return -1; }
//		return Integer.parseInt(answer);
//	}

	static byte[] getMCDI(List<AudioFile> tracks, PlayList list, IFileStore rootDir, Shell shell) {
		for (AudioFile file : tracks) {
			AudioFileInfo ai = file.getInfo();
			if (ai != null) {
				if (ai.getCDIdentifier() != null)
					return ai.getCDIdentifier();
			}
		}
		return null;
	}
	
	static Set<String> getGenres(List<AudioFile> tracks, PlayList list, IFileStore rootDir, Shell shell) {
		Set<String> genres = new HashSet<String>();
		for (AudioFile file : tracks) {
			AudioFileInfo ai = file.getInfo();
			if (ai != null) {
				String s = ai.getGenre();
				if (s != null && s.length() > 0)
					genres.add(s);
			}
		}
		return genres;
	}
	
	static String saveImage(Data data, byte[] buf) {
		ByteArrayInputStream stream = new ByteArrayInputStream(buf);
		ImageLoader img = new ImageLoader();
		img.load(stream);
		try { 
			IFolder folder = data.getFolder().getFolder("audio");
			if (!folder.exists())
				folder.create(true, true, null);
			int i = 1;
			while (folder.getFile("image_"+i+".gif").exists()) i++;
			String filename = "image_"+i+".gif";
			img.save(folder.getFile(filename).getLocation().toFile().getAbsolutePath(), SWT.IMAGE_GIF);
			return "audio/"+filename;
		} catch (CoreException e) {
			if (Log.error(AlbumDetector.class))
				Log.error(AlbumDetector.class, "Unable to save image file", e);
			return null;
		}
	}

//	static boolean askToIncludeFilesIntoAlbum(List<Pair<IFileStore,AudioFile>> files, List<Pair<IFileStore,AudioFile>> tracks, PlayList list, IFileStore rootDir, Shell shell) {
//		DoubleListDialog<Pair<IFileStore,AudioFile>> dlg = new DoubleListDialog<Pair<IFileStore,AudioFile>>(shell, files, Local.Remaining_files.toString(), false, tracks, Local.Tracks.toString(), true, new TrackProvider());
//		dlg.setMessage(Local.process(Local.MESSAGE_Include_files_into_album, rootDir.toURI().toString()));
//		if (dlg.open(Local.Create_Music_Album.toString())) {
//			for (Pair<IFileStore,AudioFile> p : dlg.getList2())
//				if (!tracks.contains(p))
//					tracks.add(p);
//			for (Pair<IFileStore,AudioFile> p : dlg.getList1())
//				if (tracks.contains(p))
//					tracks.remove(p);
//			return true;
//		}
//		return false;
//	}
//	
//	static class TrackProvider implements DoubleListControl.Provider<Pair<IFileStore,AudioFile>> {
//		public String getText(Pair<IFileStore, AudioFile> element) {
//			return element.getValue1().getName();
//		}
//		public Control createElementDetailsControl(Composite parent, Pair<IFileStore, AudioFile> element) {
//			// TODO
//			return null;
//		}
//	}
	
	public static String normalize_name(String name) {
		if (name == null) return null;
		StringBuilder str = new StringBuilder();
		boolean lastSpace = true;
		boolean lastSep = true;
		for (int i = 0; i < name.length(); ++i) {
			char c = name.charAt(i);
			if (StringUtil.isLetter(c) || StringUtil.isDigit(c)) {
				if (lastSep)
					str.append(StringUtil.upper(c));
				else
					str.append(StringUtil.lower(c));
				lastSpace = false;
				lastSep = false;
			} else {
				lastSep = true;
				if (c == ' ') {
					if (!lastSpace) {
						lastSpace = true;
						str.append(c);
					}
				} else {
					str.append(c);
				}
			}
		}
		if (str.length() == 0) return null;
		String s = str.toString().toLowerCase();
		if (s.equals("unknown")) return null;
		if (s.equals("unknown artist")) return null;
		if (s.equals("various artists")) return null;
		return str.toString();
	}
}
