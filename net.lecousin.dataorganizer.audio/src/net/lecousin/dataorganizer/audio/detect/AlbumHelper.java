package net.lecousin.dataorganizer.audio.detect;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.lecousin.dataorganizer.audio.AudioContentType;
import net.lecousin.dataorganizer.audio.AudioDataType;
import net.lecousin.dataorganizer.audio.AudioInfo;
import net.lecousin.dataorganizer.audio.AudioSourceInfo;
import net.lecousin.dataorganizer.audio.Local;
import net.lecousin.dataorganizer.audio.detect.AlbumDetector.Album;
import net.lecousin.dataorganizer.audio.internal.EclipsePlugin;
import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.VirtualData;
import net.lecousin.dataorganizer.core.database.VirtualDataBase;
import net.lecousin.dataorganizer.core.database.content.ContentType;
import net.lecousin.dataorganizer.core.database.source.DataSource;
import net.lecousin.framework.Pair;
import net.lecousin.framework.Triple;
import net.lecousin.framework.collections.CollectionUtil;
import net.lecousin.framework.collections.SortedListTree;
import net.lecousin.framework.eclipse.resource.ResourceUtil;
import net.lecousin.framework.files.audio.AudioFile;
import net.lecousin.framework.files.audio.AudioFileInfo;
import net.lecousin.framework.files.audio.AudioFileInfo.Picture;
import net.lecousin.framework.files.image.ImageFile;
import net.lecousin.framework.files.playlist.PlayList;
import net.lecousin.framework.io.FileSystemUtil;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.strings.StringUtil;
import net.lecousin.framework.ui.eclipse.dialog.ErrorDlg;

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
	static class PictureFile {
		PictureFile(IFileStore file, ImageFile image)
		{ this.file = file; this.image = image; }
		IFileStore file;
		ImageFile image;
	}
	static class TrackComparator implements Comparator<Track> {
		public int compare(Track o1, Track o2) {
			return o1.trackNumber - o2.trackNumber;
		}
	}
	
	static List<Pair<List<IFileStore>,VirtualData>> createAlbumFromOrderedList(VirtualDataBase db, Iterable<Track> orderedTracks, PlayListFile playlist, String finalAlbumName, String finalArtist, List<PictureFile> images, IFileStore rootDir, Shell shell) {
		LinkedList<Track> tracks = new LinkedList<Track>();
		for (Track t : orderedTracks)
			tracks.add(t);
		return createAlbumFromOrderedList(db, tracks, playlist, finalAlbumName, finalArtist, images, rootDir, shell);
	}
	static List<Pair<List<IFileStore>,VirtualData>> createAlbumFromOrderedList(VirtualDataBase db, List<Track> orderedTracks, PlayListFile playlist, String finalAlbumName, String finalArtist, List<PictureFile> images, IFileStore rootDir, Shell shell) {
		List<IFileStore> files = new LinkedList<IFileStore>();
		List<AudioFile> list = new ArrayList<AudioFile>(orderedTracks.size());
		for (Track p : orderedTracks) {
			files.add(p.file);
			list.add(p.audio);
		}
		if (playlist != null) files.add(playlist.file);
		VirtualData data = createAlbum(db, list, finalAlbumName, finalArtist, rootDir, playlist != null ? playlist.list : null, images, shell);
		if (data == null) return null;
		if (!images.isEmpty()) {
			Triple<List<PictureFile>,List<PictureFile>,List<PictureFile>> pictures = getPictures(data, images, shell);
			if (pictures != null) {
				AudioDataType audio = (AudioDataType)data.getContent();
				for (PictureFile p : pictures.getValue1()) {
					files.add(p.file);
					audio.saveCoverFront(p.image.getInfo().getData());
				}
				for (PictureFile p : pictures.getValue2()) {
					files.add(p.file);
					audio.saveCoverBack(p.image.getInfo().getData());
				}
				for (PictureFile p : pictures.getValue3()) {
					files.add(p.file);
					audio.saveImage(p.image.getInfo().getData());
				}
			}
		}

		return CollectionUtil.single_element_list(new Pair<List<IFileStore>,VirtualData>(files, data));
	}
	
	static VirtualData createAlbum(VirtualDataBase db, List<AudioFile> tracks, String finalAlbumName, String finalArtist, IFileStore rootDir, PlayList list, List<PictureFile> images, Shell shell) {
		List<DataSource> sources = new ArrayList<DataSource>(tracks.size());
		for (AudioFile file : tracks)
			try { sources.add(DataSource.get(file.getURI())); }
			catch (Throwable t) {
				ErrorDlg.exception(Local.Create_Music_Album.toString(), "Unable to add DataSource for file: " + file.getURI(), EclipsePlugin.ID, t);
				sources.add(null);
			}
		Triple<String,String,Integer> t = getAlbumArtistYear(tracks, list, finalAlbumName, finalArtist, rootDir, shell);
		if (t == null) return null;
		VirtualData data;
		String name = t.getValue1();
		if (name == null) name = rootDir.getName();
		if (t.getValue2() != null)
			name = t.getValue2() + " - " + name;
		try { data = (VirtualData)db.addData(name, ContentType.getContentType(AudioContentType.AUDIO_TYPE), sources); }
		catch (CoreException e) {
			return null;
		}
		AudioDataType content = (AudioDataType)data.getContent();
		AudioInfo i = (AudioInfo)content.getInfo();
		AudioSourceInfo info = (AudioSourceInfo)i.setSource(AudioInfo.FILE_SOURCE, "file", "");
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
			AudioSourceInfo.Track track;
			if (ai == null)
				track = info.newTrack();
			else {
				String title = ai.getSongTitle();
				long length = ai.getDuration();
				track = info.newTrack(title, length);
				if (ai.getPictures() != null)
					for (Picture pic : ai.getPictures()) {
						switch (pic.type) {
						case COVER_FRONT: cover_front.add(pic); break;
						case COVER_BACK: cover_back.add(pic); break;
						default:
							String filename = saveImage(data, pic.data);
							if (filename != null)
								track.addImage(pic.description, filename);
							break;
						}
					}
			}
			if (track.getTitle() == null)
				track.setTitle(getTrackNameFromFile(file, tracks));
		}
		ImageLoader img = new ImageLoader();
		for (Picture p : cover_front)
			try { content.saveCoverFront(img.load(new ByteArrayInputStream(p.data))); }
			catch (Throwable e) {}
		for (Picture p : cover_back)
			try { content.saveCoverBack(img.load(new ByteArrayInputStream(p.data))); }
			catch (Throwable e) {}
		byte[] mcdi = getMCDI(tracks, list, rootDir, shell);
		if (mcdi != null)
			info.setMCDI(mcdi);
		
		return data;
	}
	
	public static VirtualData createSingleData(VirtualDataBase db, IFileStore file, AudioFile afile) {
		List<DataSource> sources = new ArrayList<DataSource>(1);
		try { sources.add(DataSource.get(afile.getURI())); }
		catch (Throwable t) {
			ErrorDlg.exception(Local.Create_Music_Album.toString(), "Unable to add DataSource for file: " + afile.getURI(), EclipsePlugin.ID, t);
			sources.add(null);
		}
		AudioFileInfo ainfo = (AudioFileInfo)afile.getInfo();
		String album = null, artist = null, title = null;
		if (ainfo != null) {
			album = ainfo.getAlbum();
			artist = ainfo.getArtist();
			title = ainfo.getSongTitle();
		}
		String name = title;
		if (name == null) 
			name = FileSystemUtil.getFileNameWithoutExtension(file.getName());
		else if (artist != null)
			name = artist + " - " + name;
		VirtualData data;
		try { data = (VirtualData)db.addData(name, ContentType.getContentType(AudioContentType.AUDIO_TYPE), sources); }
		catch (CoreException e) {
			return null;
		}
		AudioDataType content = (AudioDataType)data.getContent();
		AudioInfo i = (AudioInfo)content.getInfo();
		AudioSourceInfo info = (AudioSourceInfo)i.setSource(AudioInfo.FILE_SOURCE, "file", "");
		info.setAlbum(album);
		if (artist != null)
			info.setArtist(artist);
		AudioSourceInfo.Track track = info.newTrack(title, ainfo != null ? ainfo.getDuration() : -1);
		if (ainfo != null) {
			if (ainfo.getYear() > 0)
				info.setYear(ainfo.getYear());
			String s = ainfo.getGenre();
			if (s != null && s.length() > 0)
				info.addGenre(s);
			if (ainfo.getPictures() != null) {
				ImageLoader img = new ImageLoader();
				for (Picture pic : ainfo.getPictures()) {
					switch (pic.type) {
					case COVER_FRONT: 
						try { content.saveCoverFront(img.load(new ByteArrayInputStream(pic.data))); }
						catch (Throwable e) {}
						break;
					case COVER_BACK: 
						try { content.saveCoverBack(img.load(new ByteArrayInputStream(pic.data))); }
						catch (Throwable e) {}
						break;
					default:
						String filename = saveImage(data, pic.data);
						if (filename != null)
							track.addImage(pic.description, filename);
						break;
					}
				}
			}
			if (ainfo.getCDIdentifier() != null)
				info.setMCDI(ainfo.getCDIdentifier());
		}		
		return data;
	}
	
	static Triple<String,String,Integer> getAlbumArtistYear(List<AudioFile> tracks, PlayList list, String finalAlbumName, String finalArtist, IFileStore rootDir, Shell shell) {
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
		if (finalArtist != null)
			artistNames.add(finalArtist);
		else
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
		if (albumNames.size() == 0) {
			String name = normalize_name(rootDir.getName());
			if (name != null)
				albumNames.add(name);
			if (list != null) {
				name = normalize_name(list.getInfo().getName());
				if (name != null)
					albumNames.add(name);
			}
			needAlbumName = true;
		}
		if (albumNames.size() == 1) result.setValue1(albumNames.iterator().next());
		else {
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
	
	static Set<String> getPossibleAlbumNames(Album album, IFileStore rootDir) {
		Set<String> albumNames = new HashSet<String>();
		for (SortedListTree<Track> list : album.sorted)
			for (Track t : list) {
				AudioFileInfo i = t.audio.getInfo();
				if (i != null) {
					String s = i.getAlbum();
					if (s != null && s.length() > 0)
						albumNames.add(s);
				}
			}
		for (Track t : album.noNumber) {
			AudioFileInfo i = t.audio.getInfo();
			if (i != null) {
				String s = i.getAlbum();
				if (s != null && s.length() > 0)
					albumNames.add(s);
			}
		}
		String name = normalize_name(rootDir.getName());
		if (name != null)
			albumNames.add(name);
		return albumNames;
	}
	static Set<String> getPossibleArtistNames(Album album) {
		Set<String> albumNames = new HashSet<String>();
		for (SortedListTree<Track> list : album.sorted)
			for (Track t : list) {
				AudioFileInfo i = t.audio.getInfo();
				if (i != null) {
					String s = i.getArtist();
					if (s != null && s.length() > 0)
						albumNames.add(s);
				}
			}
		for (Track t : album.noNumber) {
			AudioFileInfo i = t.audio.getInfo();
			if (i != null) {
				String s = i.getArtist();
				if (s != null && s.length() > 0)
					albumNames.add(s);
			}
		}
		return albumNames;
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
		try {
			try { img.load(stream); }
			catch (Throwable t) {
				if (Log.error(AlbumHelper.class))
					Log.error(AlbumHelper.class, "Unable to load image", t);
				return null;
			}
			try { 
				IFolder folder = data.getFolder().getFolder("audio");
				if (!folder.exists())
					ResourceUtil.createFolderAndParents(folder);
				int i = 1;
				while (folder.getFile("image_"+i+".gif").exists()) i++;
				String filename = "image_"+i+".gif";
				try {
					img.save(folder.getFile(filename).getLocation().toFile().getAbsolutePath(), SWT.IMAGE_GIF);
				} catch (Throwable t) {
					filename = "image_"+i+".jpg";
					try { img.save(folder.getFile(filename).getLocation().toFile().getAbsolutePath(), SWT.IMAGE_JPEG); }
					catch (Throwable t2) {
						filename = "image_"+i+".png";
						try { img.save(folder.getFile(filename).getLocation().toFile().getAbsolutePath(), SWT.IMAGE_PNG); }
						catch (Throwable t3) {
							filename = "image_"+i+".bmp";
							try { img.save(folder.getFile(filename).getLocation().toFile().getAbsolutePath(), SWT.IMAGE_BMP_RLE); }
							catch (Throwable t4) {
								if (Log.error(AlbumDetector.class)) {
									Log.error(AlbumDetector.class, "Unable to save image file (gif)", t);
									Log.error(AlbumDetector.class, "Unable to save image file (jpg)", t2);
									Log.error(AlbumDetector.class, "Unable to save image file (png)", t3);
									Log.error(AlbumDetector.class, "Unable to save image file (bmp)", t4);
								}
								return null;
							}
						}
					}
				}
				return "audio/"+filename;
			} catch (CoreException e) {
				if (Log.error(AlbumDetector.class))
					Log.error(AlbumDetector.class, "Unable to save image file", e);
				return null;
			}
		} finally {
			img.data = null;
			try { stream.close(); } catch (IOException e){}
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
		String s = str.toString().trim().toLowerCase();
		if (s.equals("unknown")) return null;
		if (s.equals("unknown artist")) return null;
		if (s.equals("no artist")) return null;
		if (s.equals("various")) return null;
		if (s.equals("various artists")) return null;
		return str.toString();
	}
	
	public static String getTrackNameFromFile(AudioFile track, List<AudioFile> allTracks) {
		int index = allTracks.indexOf(track)+1;
		String name = getAsSimpleName(track, index);
		if (name.length() == 0) return null;
		if (allTracks.size() > 1) {
			int same = name.length();
			int ti = 1;
			for (AudioFile t : allTracks) {
				if (t == track) continue;
				String tname = getAsSimpleName(t, ti);
				for (int i = 0; i < tname.length() && i < same; ++i) {
					if (tname.charAt(i) != name.charAt(i)) {
						same = i;
						break;
					}
				}
				if (same == 0) break;
				ti++;
			}
			if (same != 0)
				name = name.substring(same).trim();
			same = name.length();
			ti = 1;
			for (AudioFile t : allTracks) {
				if (t == track) continue;
				String tname = getAsSimpleName(t, ti);
				for (int i = 0; i < tname.length() && i < same; ++i) {
					if (tname.charAt(tname.length()-i-1) != name.charAt(name.length()-i-1)) {
						same = i;
						break;
					}
				}
				if (same == 0) break;
				ti++;
			}
			if (same != 0)
				name = name.substring(0, name.length()-same).trim();
			if (name.startsWith(Integer.toString(index)))
				name = name.substring(Integer.toString(index).length()).trim();
		}
		if (name.length() == 0) return null;
		return name;
	}
	
	private static String getAsSimpleName(AudioFile file, int index) {
		String name = file.getURI().toString();
		int i = name.lastIndexOf('/');
		if (i >= 0)
			name = name.substring(i+1);
		name = URLDecoder.decode(name).trim();
		AudioFileInfo info = file.getInfo();
		if (info != null && info.getTrackNumber() > 0) {
			if (name.startsWith(Integer.toString(info.getTrackNumber())))
				name = name.substring(Integer.toString(info.getTrackNumber()).length());
		}
		name = name.trim();
		if (name.startsWith(Integer.toString(index)))
			name = name.substring(Integer.toString(index).length());
		name = name.trim();
		i = 0;
		while (i < name.length() && !StringUtil.isLetter(name.charAt(i)))
			i++;
		if (i > 0) name = name.substring(i).trim();
		i = name.lastIndexOf('.');
		if (i > 0)
			name = name.substring(0, i).trim();
		if (name.length() == 0) return null;
		return name;
	}
	
	private static Triple<List<PictureFile>,List<PictureFile>,List<PictureFile>> getPictures(VirtualData data, List<PictureFile> images, Shell shell) {
		List<PictureFile> coverFront = new LinkedList<PictureFile>();
		List<PictureFile> coverBack = new LinkedList<PictureFile>();
		List<PictureFile> others = new LinkedList<PictureFile>();
		for (Iterator<PictureFile> it = images.iterator(); it.hasNext(); ) {
			PictureFile file = it.next();
			String name = file.file.getName();
			if (name.equalsIgnoreCase("Folder.jpg")) {
				coverFront.add(file);
				it.remove();
			} else if (name.equalsIgnoreCase("AlbumArtSmall.jpg"))
				it.remove();
			else if (StringUtil.containsWord(name, "front", false) || StringUtil.containsWord(name, "recto", false)) {
				coverFront.add(file);
				it.remove();
			} else if (StringUtil.containsWord(name, "back", false) || StringUtil.containsWord(name, "verso", false)) {
				coverFront.add(file);
				it.remove();
			}
		}
		if (!images.isEmpty()) {
			DecidePicturesDialog dlg = new DecidePicturesDialog(shell, data, images);
			if (dlg.open()) {
				coverFront.addAll(dlg.getCoverFront());
				coverBack.addAll(dlg.getCoverBack());
				others.addAll(dlg.getOthers());
			}
		}
		return new Triple<List<PictureFile>,List<PictureFile>,List<PictureFile>>(coverFront, coverBack, others);
	}
}
