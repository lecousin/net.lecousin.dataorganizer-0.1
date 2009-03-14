package net.lecousin.dataorganizer.audio.detect;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.lecousin.dataorganizer.audio.Local;
import net.lecousin.dataorganizer.audio.detect.AlbumHelper.PlayListFile;
import net.lecousin.dataorganizer.audio.detect.AlbumHelper.Track;
import net.lecousin.dataorganizer.audio.detect.AlbumHelper.TrackComparator;
import net.lecousin.dataorganizer.core.database.VirtualData;
import net.lecousin.dataorganizer.core.database.VirtualDataBase;
import net.lecousin.framework.collections.CollectionUtil;
import net.lecousin.framework.collections.SortedListTree;
import net.lecousin.framework.io.FileSystemUtil;
import net.lecousin.framework.strings.StringUtil;
import net.lecousin.media.sound.files.audio.AudioFile;
import net.lecousin.media.sound.files.audio.AudioFileInfo;
import net.lecousin.media.sound.files.audio.InvalidAudioFile;
import net.lecousin.media.sound.files.audio.mp3.MP3File;
import net.lecousin.media.sound.files.playlist.PlayList;
import net.lecousin.media.sound.files.playlist.m3u.M3UPlayList;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Shell;

public class AlbumDetector {

	public static List<VirtualData> detect(VirtualDataBase db, IFileStore dir, IFileInfo info, List<IFileStore> remainingFolders, List<IFileStore> remainingFiles, Shell shell) {
		IFileStore[] files;
		try { files = dir.childStores(EFS.NONE, null); }
		catch (CoreException e) { return null; }
		
		List<Track> tracks = new LinkedList<Track>();
		List<PlayListFile> playlists = new LinkedList<PlayListFile>();
		for (IFileStore file : files) {
			if (file.fetchInfo().isDirectory()) continue;
			String ext = FileSystemUtil.getExtension(file.getName());
			AudioFile audio = detectAudioFile(file, ext);
			if (audio != null)
				tracks.add(new Track(file, audio));
			else {
				PlayList list = detectPlayList(file, ext);
				if (list != null)
					playlists.add(new PlayListFile(file, list));
			}
		}
		
		if (tracks.isEmpty()) return null;
		
		List<VirtualData> result;
		
		// ignore the playlists in future detection
		for (PlayListFile pl : playlists)
			remainingFiles.remove(pl.file);

		result = handlePlayLists(db, tracks, playlists, remainingFiles, remainingFolders, dir, shell);
		if (result != null) return result;
		result = handleTracks(db, tracks, remainingFiles, remainingFolders, dir, shell);
		return result;
	}
	
	public static AudioFile detectAudioFile(IFileStore file, String ext) {
		try {
			if (ext.equals("mp3") || ext.equals("mpeg3") || ext.equals("mpg3"))
				return new MP3File(file);
		} catch (InvalidAudioFile e) {}
		return null;
	}
	
	public static PlayList detectPlayList(IFileStore file, String ext) {
		try {
			if (ext.equals("m3u"))
				return M3UPlayList.load(FileSystemUtil.getFileNameWithoutExtension(file.getName()), file.openInputStream(EFS.NONE, null));
		} catch (CoreException e) {}
		return null;
	}
	
	private static List<VirtualData> handlePlayLists(VirtualDataBase db, List<Track> tracks, List<PlayListFile> playlists, List<IFileStore> remainingFiles, List<IFileStore> remainingFolders, IFileStore rootDir, Shell shell) {
		if (playlists.isEmpty()) return null;
		if (playlists.size() == 1)
			return handleSinglePlayList(db, tracks, playlists.get(0), remainingFiles, remainingFolders, rootDir, shell);
		return handleSeveralPlayLists(db, tracks, playlists, remainingFiles, remainingFolders, rootDir, shell);
	}

	private static List<VirtualData> handleSinglePlayList(VirtualDataBase db, List<Track> tracks, PlayListFile playlist, List<IFileStore> remainingFiles, List<IFileStore> remainingFolders, IFileStore rootDir, Shell shell) {
		List<String> list = playlist.list.getPlayListFiles();
		List<Track> files = new ArrayList<Track>(tracks);
		List<Track> orderedFiles = new ArrayList<Track>(tracks.size());
		boolean ok = true;
		for (String s : list) {
			boolean found = false;
			for (Track file : files)
				if (file.file.getName().equalsIgnoreCase(s)) {
					files.remove(file);
					orderedFiles.add(file);
					found = true;
					break;
				}
			if (!found) { ok = false; break; }
		}
		if (ok) {
			if (!files.isEmpty())
				return null; // TODO
			// the playlist contains exactly all the tracks
			return AlbumHelper.createAlbumFromOrderedList(db, orderedFiles, playlist, null, remainingFiles, rootDir, shell);
		}
		return null;
	}

	private static List<VirtualData> handleSeveralPlayLists(VirtualDataBase db, List<Track> tracks, List<PlayListFile> playlists, List<IFileStore> remainingFiles, List<IFileStore> remainingFolders, IFileStore rootDir, Shell shell) {
		return null; // TODO
	}
	
	static class Album {
		String name;
		List<Track> noNumber = new LinkedList<Track>();
		List<SortedListTree<Track>> sorted = new LinkedList<SortedListTree<Track>>();
//		boolean userAlreadySawNoInfo = false;
		boolean nameIsFromUser = false;
	}
	// TODO improvment? use the number of tracks to better detect... ???
	private static List<VirtualData> handleTracks(VirtualDataBase db, List<Track> tracks, List<IFileStore> remainingFiles, List<IFileStore> remainingFolders, IFileStore rootDir, Shell shell) {
		// noInfo: pas de AudioFileInfo, Integer=numero de track detecte sur le nom du fichier ou null
		List<Track> noInfo = new LinkedList<Track>();
		Map<String,Album> albums = new HashMap<String,Album>();
		for (Track t : tracks) {
			AudioFileInfo ai = t.audio.getInfo();
			if (ai == null) {
				t.trackNumber = detectTrackNumberFromFile(t, tracks);
				noInfo.add(t);
				continue;
			}
			String albumName = AlbumHelper.normalize_name(ai.getAlbum());
			if (t.trackNumber <= 0) t.trackNumber = detectTrackNumberFromFile(t, tracks);
			Album album = albums.get(albumName);
			if (album == null) {
				album = new Album();
				albums.put(albumName, album);
				album.name = albumName;
			}
			if (t.trackNumber <= 0)
				album.noNumber.add(t);
			else {
				boolean put = false;
				for (SortedListTree<Track> list : album.sorted)
					if (!list.containsEquivalent(t)) {
						list.add(t);
						put = true;
						break;
					}
				if (!put) {
					SortedListTree<Track> list = new SortedListTree<Track>(new TrackComparator());
					list.add(t);
					album.sorted.add(list);
				}
			}
		}
		
		Album noName = albums.remove(null);

		boolean needUser = false;
		List<Album> albumsReadyToBeCreated = new LinkedList<Album>();
		// consolidate albums
		for (Map.Entry<String, Album> entry : new ArrayList<Map.Entry<String, Album>>(albums.entrySet())) {
			Album album = entry.getValue();
			List<Album> list = consolidateAlbum(album, noName, noInfo, rootDir, shell);
			if (list == null)
				needUser = true;
			else {
				albumsReadyToBeCreated.addAll(list);
				albums.remove(entry.getKey());
			}
		}
		
		if (albums.size() > 1) {
			// plusieurs albums restants => besoin de l'utilisateur
			needUser = true;
		}
		if (!needUser) {
			if (albums.size() == 1) {
				Album album = albums.values().iterator().next();
				if (!noInfo.isEmpty()/* && !album.userAlreadySawNoInfo*/) {
					// besoin de l'utilisateur pour inclure eventuellement des pistes de noInfo
					needUser = true;
				} else {
					// 1 seul album, ok => create
					return AlbumHelper.createAlbumFromOrderedList(db, album.sorted.get(0), null, album.nameIsFromUser ? album.name : null, remainingFiles, rootDir, shell);
				}
			} else {
				// no album
				if (noName != null) {
					// pas de nom d'album, mais un album possible ?
					List<Album> list =  consolidateAlbum(noName, null, noInfo, rootDir, shell);
					if (list == null)
						needUser = true;
					else {
						albumsReadyToBeCreated.addAll(list);
						noName = null;
					}
				} else if (!noInfo.isEmpty()) {
					// il reste des tracks dans noInfo => aucun track number n'a encore pu etre trouve
					SortedListTree<Track> list = new SortedListTree<Track>(new TrackComparator());
					int first = -1;
					int last = -1;
					List<Integer> found = new LinkedList<Integer>();
					for (Track t : noInfo) {
						t.trackNumber = detectTrackNumberOnlyFromFile(t, tracks);
						if (t.trackNumber <= 0) {
							needUser = true;
							break;
						}
						if (list.containsEquivalent(t)) {
							needUser = true;
							break;
						}
						list.add(t);
						if (first == -1 || t.trackNumber < first) first = t.trackNumber;
						if (last == -1 || t.trackNumber > last) last = t.trackNumber;
						found.add(t.trackNumber);
					}
					if (!needUser) {
						// no info, but from the filenames we've got the numbers
						if (first != 1)
							needUser = true;
						else {
							for (int i = 2; i < last; ++i)
								if (!found.contains(i)) {
									// il y a un trou dans la numerotation
									needUser = true;
									break;
								}
							if (!needUser) {
								// victoire, on a une série continue démarrant à 1
								return AlbumHelper.createAlbumFromOrderedList(db, list, null, null, remainingFiles, rootDir, shell);
							}
						}
					}
				}
			}
		}

		if (needUser) {
			List<Album> list = new LinkedList<Album>();
			list.addAll(albumsReadyToBeCreated);
			list.addAll(albums.values());
			return userBuildsAlbums(list, noName, noInfo, remainingFiles, db, rootDir, shell);
		}
		
		List<VirtualData> result = new LinkedList<VirtualData>();
		for (Album album : albumsReadyToBeCreated) {
			List<VirtualData> list = AlbumHelper.createAlbumFromOrderedList(db, album.sorted.get(0), null, album.name, remainingFiles, rootDir, shell);
			if (list != null)
				result.addAll(list);
		}
		return result.isEmpty() ? null : result; 
	}
	
	private static List<Album> consolidateAlbum(Album album, Album noName, List<Track> noInfo, IFileStore rootDir, Shell shell) {
		if (album.noNumber.size() == 1 && album.sorted.size() == 1) {
			if (tryToIncludeNotNumberedIntoAlbum(album.noNumber.get(0), album.sorted.get(0)))
				album.noNumber.remove(0);
		}
		if (!album.noNumber.isEmpty() && album.sorted.size() == 0) {
			SortedListTree<Track> list = new SortedListTree<Track>(new TrackComparator());
			int first = -1;
			int last = -1;
			List<Integer> found = new LinkedList<Integer>();
			boolean ok = true;
			for (Track t : album.noNumber) {
				t.trackNumber = detectTrackNumberOnlyFromFile(t, album.noNumber);
				if (t.trackNumber <= 0) {
					ok = false;
					break;
				}
				if (list.containsEquivalent(t)) {
					ok = false;
					break;
				}
				list.add(t);
				if (first == -1 || t.trackNumber < first) first = t.trackNumber;
				if (last == -1 || t.trackNumber > last) last = t.trackNumber;
				found.add(t.trackNumber);
			}
			if (ok) {
				// from the filenames we've got the numbers
				if (first != 1)
					ok = false;
				else {
					for (int i = 2; i < last; ++i)
						if (!found.contains(i)) {
							// il y a un trou dans la numerotation
							ok = false;
							break;
						}
					if (ok) {
						// victoire, on a une série continue démarrant à 1
						album.sorted.add(list);
						album.noNumber.clear();
					}
				}
			}
		}
		if (!album.noNumber.isEmpty()) {
			return null; // need user
//			if (album.sorted.size() > 1) {
//				// plusieurs listes + non numerotes => besoin de l'utilisateur
//				return null;
//			} else if (album.sorted.size() == 1) {
//				// non numerotes + 1 album => demande a l'utilisateur d'inclure ou non ces non numerotes dans l'album / creer un nouvel album
//				List<Album> albums = includeNotNumberedTracksIntoAlbum(album, noName, noInfo, rootDir, shell);
//				album.userAlreadySawNoInfo = true;
//				album.nameIsFromUser = true;
//				return albums;
//			} else {
//				// uniquement des non numérotés => demande a l'utilisateur de trier
//				List<Album> albums = CollectionUtil.single_element_list(album);
//				StringBuilder message = new StringBuilder();
//				message.append(Local.process(Local.MESSAGE_Create_Album__Only_Not_Numbered, URLDecoder.decode(rootDir.toURI().toString())));
//				if (noName != null) {
//					if (noInfo != null && !noInfo.isEmpty())
//						message.append("<br>").append(Local.MESSAGE_Create_Album__NoName_NoInfo);
//					else
//						message.append("<br>").append(Local.MESSAGE_Create_Album__NoName);
//				} else if (noInfo != null && !noInfo.isEmpty())
//					message.append("<br>").append(Local.MESSAGE_Create_Album__NoInfo);
//				CreateAlbumsDialog dlg = new CreateAlbumsDialog(shell, message.toString(), albums, noName, noInfo, rootDir);
//				if (!dlg.open()) {
//					noInfo.addAll(album.noNumber);
//					for (SortedListTree<Track> list : album.sorted)
//						for (Track t : list)
//							noInfo.add(t);
//					return new LinkedList<Album>();
//				}
//				album.userAlreadySawNoInfo = true;
//				album.nameIsFromUser = true;
//				return albums;
//			}
		} else {
			if (album.sorted.size() > 1) {
				// tous numerotes, mais plusieurs listes => besoin de l'utilisateur
				return null;
			}
			// sinon l'album est ok
			return CollectionUtil.single_element_list(album);
		}
	}
	
	private static List<VirtualData> userBuildsAlbums(Collection<Album> albums, Album noName, List<Track> noInfo, List<IFileStore> remainingFiles, VirtualDataBase db, IFileStore rootDir, Shell shell) {
		List<Album> list = new LinkedList<Album>();
		list.addAll(albums);
		StringBuilder message = new StringBuilder();
		//message.append(Local.process(Local.MESSAGE_Create_Album__Too_Difficult, URLDecoder.decode(rootDir.toURI().toString())));
		message.append(Local.process(Local.MESSAGE_Create_Album, URLDecoder.decode(rootDir.toURI().toString())));
		CreateAlbumsDialog dlg = new CreateAlbumsDialog(shell, message.toString(), list, noName, noInfo, rootDir);
		if (!dlg.open()) return null;
		List<VirtualData> result = new LinkedList<VirtualData>();
		for (Album album : list) {
			List<VirtualData> data = AlbumHelper.createAlbumFromOrderedList(db, album.sorted.get(0), null, album.name, remainingFiles, rootDir, shell);
			if (data != null)
				result.addAll(data);
		}
		return result;
	}
	
//	private static List<Album> includeNotNumberedTracksIntoAlbum(Album album, Album noName, List<Track> noInfo, IFileStore rootDir, Shell shell) {
//		List<Album> albums = CollectionUtil.single_element_list(album);
//		StringBuilder message = new StringBuilder();
//		message.append(Local.process(Local.MESSAGE_Create_Album__Include_Not_Numbered, URLDecoder.decode(rootDir.toURI().toString())));
//		if (noName != null) {
//			if (noInfo != null && !noInfo.isEmpty())
//				message.append("<br>").append(Local.MESSAGE_Create_Album__NoName_NoInfo);
//			else
//				message.append("<br>").append(Local.MESSAGE_Create_Album__NoName);
//		} else if (noInfo != null && !noInfo.isEmpty())
//			message.append("<br>").append(Local.MESSAGE_Create_Album__NoInfo);
//		CreateAlbumsDialog dlg = new CreateAlbumsDialog(shell, message.toString(), albums, noName, noInfo, rootDir);
//		if (!dlg.open()) {
//			noInfo.addAll(album.noNumber);
//			for (SortedListTree<Track> list : album.sorted)
//				for (Track t : list)
//					noInfo.add(t);
//			return new LinkedList<Album>();
//		}
//		return albums;
//	}
	
	private static boolean tryToIncludeNotNumberedIntoAlbum(Track notNumbered, SortedListTree<Track> tracks) {
		int first = -1;
		int last = -1;
		List<Integer> found = new LinkedList<Integer>();
		for (Track t : tracks) {
			if (first == -1 || t.trackNumber < first) first = t.trackNumber;
			if (last == -1 || t.trackNumber > last) last = t.trackNumber;
			found.add(t.trackNumber);
		}
		List<Integer> notFound = new LinkedList<Integer>();
		for (int i = first + 1; i < last - 1; ++i)
			if (!found.contains(i))
				notFound.add(i);
		if (first > 1 && notFound.isEmpty()) {
			notNumbered.trackNumber = first-1;
			tracks.add(notNumbered);
			return true;
		} else if (first == 1 && notFound.size() == 1) {
			notNumbered.trackNumber = notFound.get(0);
			tracks.add(notNumbered);
			return true;
		} else if (first == 1 && notFound.isEmpty()) {
			notNumbered.trackNumber = last+1;
			tracks.add(notNumbered);
			return true;
		}
		return false;
	}
	
	private static int detectTrackNumberFromFile(Track track, List<Track> allTracks) {
		String name = track.file.getName();
		List<Track> noNumber = new LinkedList<Track>();
		for (Track t : allTracks) {
			if (t == track) continue;
			if (t.trackNumber == -1) {
				noNumber.add(t);
				continue;
			}
			String str = t.file.getName();
			// looking for the following patterns:
			// xx YY zzz
			// zzz YY xx
			// where zzz is anything, YY is the number possibly with heading 0, xx is a fixed string
			StringBuilder head = new StringBuilder();
			StringBuilder number = new StringBuilder();
			int foundHead = -1;
			for (int i = 0; i < str.length(); ++i) {
				char c = str.charAt(i);
				if (StringUtil.isDigit(c))
					number.append(c);
				else {
					if (number.length() == 0) {
						head.append(c);
						if (!name.startsWith(head.toString()))
							break;
					} else {
						int num = Integer.parseInt(number.toString());
						if (num == t.trackNumber) {
							int j = head.length();
							while (j < name.length() && StringUtil.isDigit(name.charAt(j))) j++;
							if (j > head.length()) {
								foundHead = Integer.parseInt(name.substring(head.length(), j));
								break;
							}
						}
						head.append(number);
						if (!name.startsWith(head.toString()))
							break;
						number = new StringBuilder();
						continue;
					}
				}
			}
			StringBuilder tail = new StringBuilder();
			number = new StringBuilder();
			int foundTail = -1;
			for (int i = str.length()-1; i >= 0; --i) {
				char c = str.charAt(i);
				if (StringUtil.isDigit(c))
					number.insert(0, c);
				else {
					if (number.length() == 0) {
						tail.insert(0, c);
						if (!name.endsWith(tail.toString()))
							break;
					} else {
						int num = Integer.parseInt(number.toString());
						if (num == t.trackNumber) {
							int j = name.length() - tail.length() - 1;
							while (j >= 0 && StringUtil.isDigit(name.charAt(j))) j--;
							if (j < tail.length()-1) {
								foundTail = Integer.parseInt(name.substring(j, name.length()-tail.length()));
								break;
							}
						}
						tail.append(number);
						if (!name.endsWith(tail.toString()))
							break;
						number = new StringBuilder();
						continue;
					}
				}
			}
			
			if (foundHead > 0) {
				if (foundTail > 0) {
					if (foundTail == foundHead) return foundHead;
					continue; // ambiguity
				}
				return foundHead;
			} else if (foundTail > 0)
				return foundTail;
		}
		return -1;
	}

	private static int detectTrackNumberOnlyFromFile(Track track, List<Track> allTracks) {
		if (allTracks.size() == 1)
			return 1;
		
		String head = null;
		for (Track t : allTracks) {
			String tname = t.file.getName();
			if (head == null)
				head = tname;
			else {
				int i;
				for (i = 0; i < head.length() && i < t.file.getName().length(); ++i)
					if (tname.charAt(i) != head.charAt(i))
						break;
				if (i == 0) {
					head = "";
					break;
				}
				head = head.substring(0, i);
			}
		}
		
		String name = track.file.getName();
		int pos = head.length();
		if (!StringUtil.isDigit(name.charAt(pos))) return -1;
		while (StringUtil.isDigit(name.charAt(++pos)));
		return Integer.parseInt(name.substring(head.length(), pos));
	}
}
