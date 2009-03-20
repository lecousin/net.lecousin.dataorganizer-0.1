package net.lecousin.dataorganizer.audio;

import net.lecousin.framework.application.Application;

public enum Local {

	Add_selected_tracks_to_the_album_list_below("Add selected tracks to the album list below", "Ajouter les pistes sélectionnées à la liste de l'album ci-dessous"),
	Add_to_Media_Player_List("Add to Media Player List", "Ajouter à la liste du Media Player"),
	Album("Album", "Album"),
	Album_name("Album name", "Nom de l'album"),
	Albums_to_create("Albums to create", "Albums à créer"),
	Another_name("Another name", "Un autre nom"),
	Another_year("Another year", "Une autre année"),
	Artist("Artist", "Artiste"),
	Audio("Audio", "Audio"),
	Create_Music_Album("Create Music Album", "Création d'un album musical"),
	Create_new_album("Create a new album", "Créer un nouvel album"),
	doesnt_exist("doesn't exist", "n'existe pas"),
	Duration("Duration", "Durée"),
	File("File", "Fichier"),
	Genres("Genres", "Genres"),
	I_dont_know("I don't know", "Je ne sais pas"),
	lets_it_without_an_artist_name_for_now("let's it without an artist name for now", "laissons le sans nom d'artiste pour le moment"),
	lets_it_without_a_year_for_now("let's it without a year for now", "laissons le sans année pour le moment"),
	List("List", "Liste"),
	Lists_of_numbered_tracks("Lists of numbered tracks found", "Listes de pistes numérotées trouvées"),
	Name("Name", "Nom"),
	No_source("You do not have a physical source for this data", "Vous n'avez pas de source physique pour cette donnée"),
	Not_numbered_tracks("Tracks found without a track number", "Pistes trouvées sans numéro de piste"),
	Open("Open", "Ouvrir"),
	Open_in_Media_Player("Open in Media Player", "Ouvrir avec le Media Player"),
	Open_with_system_application("Open with default system application", "Ouvrir avec l'application système par défaut"),
	Please_select_an_album_name("Please select an album name", "Veuillez sélectionner un nom pour l'album"),
	Please_select_an_artist_name("Please select an artist name", "Veuillez sélectionner un nom pour l'artiste"),
	Please_select_a_year("Please select a year", "Veuillez sélectionner une année pour l'album"),
	Remaining_files("Remaining files", "Fichiers restants"),
	Remove_selected_tracks_from_the_album("Remove selected tracks from the album", "Supprimer les pistes sélectionnées de l'album"),
	The_album_cannot_be_empty("The album cannot be empty. Please add at least one track.", "L'album ne peut pas être vide. Veuillez ajouter au moins une piste."),
	the_album_name("the album name", "le nom de l'album"),
	the_artist_name("the artist name", "le nom de l'artiste"),
	The_file("The file", "Le fichier"),
	The_name_cannot_be_empty("The name cannot be empty", "Le nom ne peut pas être vide"),
	the_year_of_the_album("the year of the album", "l'année de l'album"),
	The_year_cannot_be_empty("The year cannot be empty", "L'année ne peut pas être vide"),
	The_year_must_be_a_number("The year must be a number", "L'année doit être un nombre"),
	Title("Title", "Titre"),
	Track("Track", "Piste"),
	Tracks("Tracks", "Pistes"),
	Tracks_not_included_into_an_album("Tracks not included into an album", "Pistes non inclues dans un album"),
	Unable_to_locate_file("Unable to locate file", "Impossible de localiser le fichier"),
	Unable_to_open_audio_media("Unable to open the audio media", "Impossible d'ouvrir le média audio"),
	Year("Year", "Année"),

	There_are_remaining_tracks_in_the_not_numbered_list(
			"There are remaining tracks in the list of not numbered tracks. You have to treat all those tracks either by removing them from the album or to include and sort them into the album list.",
			"Il reste des pistes dans la liste des pistes non numérotées. Vous devez traiter toutes ces pistes, soit en les supprimant de l'album, soit en les ajoutant et en les triant parmis les pistes de l'album."
	),
	You_must_merge_or_remove_lists(
			"You must merge the different lists into a single one, or remove the ones that should not be part of the album. A valid album must contain a single list.",
			"Vous devez fusionner les différentes listes, ou supprimer celles qui ne devraient pqs fqire pqrtie de l'album. Pour que l'album soit valide, il ne doit rester qu'une seule liste."
	),
	There_are_XX_albums_containing_errors(
			"There are %#1% albums containing errors. Please fix these errors before to continue.",
			"Il reste %#1% albums avec des erreurs. Veuillez corriger ces erreurs avant de poursuivre."
	),
	
	MESSAGE_Album_Decision(
		"An album has been detected in the following directory:<br>" +
		"%#1%<br>" +
		"but it is difficult to decide on %#2%.<br>" +
		"It contains the following tracks:",
		"Un album a été détecté dans le dossier suivant:<br>" +
		"%#1%<br>" +
		"Cependant il est difficile de décider sur %#2%.<br>" +
		"Cet album contient les pistes suivantes:"
	),
	
	MESSAGE_Create_Album(
		"Some audio tracks have been found in the following directory:<br>" +
		"<a href=\"dir\">%#1%</a><br>" +
		"However it was not possible to create complete and ordered albums automatically, your intervention is then needed.<br>" +
		"The albums that have been found are already filled, but some of them may contain tracks it was not possible to store correctly into the album because of a lake of information.<br>" +
		"Some tracks may also not be included into an album because no information is available, these tracks are listed in the table below.<br>" +
		"This window allows you to edit the albums created, to remove albums, to create new ones, and to store tracks into albums.<br>" +
		"Every album will be created as a single data (containing several files) in your database, and every remaining track will be created as a data (with a single file) in your database.",
		"Des pistes audio ont été détectées dans le répertoire suivant:<br>" +
		"<a href=\"dir\">%#1%</a><br>" +
		"Cependant il n'a pas été possible de créer des albums complets et triés, votre intervention est donc nécessaire.<br>" +
		"Les albums qui ont été trouvés sont déjà remplis, mais certains peuvent comporter des pistes qu'il n'a pas été possible de correctement ranger dans l'album par manque d'information.<br>" +
		"Certaines pistes peuvent également n'avoir été inclues dans aucun album car aucune information n'était disponible, ces pistes sont listées dans la table ci-dessous.<br>" +
		"Cette fenêtre vous permet d'éditer les albums créés, d'en supprimer, d'en créer de nouveaux, et d'attribuer des pistes audios aux albums.<br>" +
		"Chaque album sera créé en tant que donnée unique (comportant plusieurs fichiers) dans la base de données, et chaque piste audio restante sans album sera créée en tant que donnée seule dans la base de données."
	),
	
//	MESSAGE_Create_Album__Only_Not_Numbered(
//		"An album has been detected in the following directory:<br>" +
//		"<a href=\"dir\">%#1%</a><br>" +
//		"but it was not possible to determine the tracks order.<br>" +
//		"Your intervention is then necessary to specify the tracks order.<br>" +
//		"This window allow you also to create several albums if these tracks are not part of the same album.",
//		"Un album a été détecté dans le répertoire suivant:<br>" +
//		"<a href=\"dir\">%#1%</a><br>" +
//		"mais il n'a pas été possible de déterminer l'ordre des pistes.<br>" +
//		"Votre intervention est donc nécessaire pour spécifier l'ordre des pistes.<br>" +
//		"Cette fenêtre vous permet également de créer plusieurs albums si ces pistes n'appartiennent pas toutes au même album."
//	),
//	MESSAGE_Create_Album__Too_Difficult(
//		"Some audio tracks have been detected in the following directory:<br>" +
//		"<a href=\"dir\">%#1%</a><br>" +
//		"but it is difficult to organize them into albums because of lake of information.<br>" +
//		"This window allow you to organize these tracks into albums, and to order them within the albums. Every track remaining outside of an album will be detected as single track (not included into an album) but it will be added to your database.",
//		"Des pistes audio ont été détectées dans le répertoire suivant:<br>" +
//		"<a href=\"dir\">%#1%</a><br>" +
//		"mais il est difficile des les organiser par album par manque d'information.<br>" +
//		"Cette fenêtre vous permet donc d'organiser ces pistes en albums, et de les trier. Toute piste restant sans album sera par la suite détectée en tant que piste seule (non-inclue dans un album) mais sera rajoutée à votre base de données."
//	),
//	MESSAGE_Create_Album__Include_Not_Numbered(
//		"An album has been detected in the following directory:<br>" +
//		"<a href=\"dir\">%#1%</a><br>" +
//		"It was possible to determine some tracks number, but some others remain without a number, your intervention is then necessary.<br>" +
//		"This window allow you to create several albums if these tracks are not part of the same album.",
//		"Un album a été détecté dans le répertoire suivant:<br>" +
//		"<a href=\"dir\">%#1%</a><br>" +
//		"Il a été possible de déterminer le numéro de piste pour certaines, mais il reste des pistes sans numéro, votre intervention est donc nécessaire.<br>" +
//		"Cette fenêtre vous permet également de créer plusieurs albums si ces pistes n'appartiennent pas toutes au même album."
//	),
//	MESSAGE_Create_Album____Include_Create_Leave(
//		"You can include them into the album, or create one or more new albums to include them, or you can ignore them. In this last case, the tracks won't be included into any album and they will be detected as single tracks to be included into your database.",
//		"Vous pouvez soit les inclure dans l'album, soit créer un ou plusieurs nouveaux albums pour les inclure, soit les ignorer. Dans le dernier cas ces pistes ne seront inclues dans aucun album et seront détectées comme pistes seules."
//	),
//	MESSAGE_Create_Album__NoName(
//		"Note that some tracks have been found without any album name. " + MESSAGE_Create_Album____Include_Create_Leave.english,
//		"Notez également que des pistes sans nom d'album ont été trouvées. " + MESSAGE_Create_Album____Include_Create_Leave.french
//	),
//	MESSAGE_Create_Album__NoInfo(
//		"Note that some tracks have been found without any information. " + MESSAGE_Create_Album____Include_Create_Leave.english,
//		"Notez également que des pistes sans information ont été trouvées. " + MESSAGE_Create_Album____Include_Create_Leave.french
//	),
//	MESSAGE_Create_Album__NoName_NoInfo(
//		"Note that some tracks have been found without any album name, and some others without any information at all. " + MESSAGE_Create_Album____Include_Create_Leave.english,
//		"Notez également que des pistes sans nom d'album, ainsi que des pistes sans aucune information ont été trouvées. " + MESSAGE_Create_Album____Include_Create_Leave.french 
//	),
//	MESSAGE_Include_files_into_album(
//		"An album has been detected but there are some files remaining in the directory.<br>" +
//		"The album has been found in directory %#1%.<br>" +
//		"You can include part of the remaining files into the album in this window, then press Ok.",
//		"Un album a été détecté mais il reste quelques fichiers dans le dossier.<br>" +
//		"L'album a été trouvé dans le répertoire %#1%.<br>" +
//		"Cette fenêtre vous permet d'inclure certains de ces fichiers dans l'album."
//	),
	
	;
	
	private Local(String english, String french) {
		this.english = english;
		this.french = french;
	}
	private String english;
	private String french;

	public static String process(Local text, Object...params) {
		int i = 1;
		String str = text.toString();
		for (Object param : params) {
			str = str.replace("%#"+i+"%", param.toString());
			i++;
		}
		return str;
	}
	
	@Override
	public java.lang.String toString() {
		switch (Application.language) {
		case FRENCH: return french;
		default: return english;
		}
	}
}
