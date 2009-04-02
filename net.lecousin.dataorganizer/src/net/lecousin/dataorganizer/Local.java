package net.lecousin.dataorganizer;

import net.lecousin.framework.application.Application;

public enum Local {

	Add("Add", "Ajouter"),
	Added("Added", "Ajouté"),
	Always("Always", "Toujours"),
	analyzed__s("analyzed", "analyzés"),
	Comment("Comment", "Commentaire"),
	Contact("Contact", "Contact"),
	creation("creation", "création"),
	data("data", "donnée"),
	data__s("data", "donnée(s)"),
	datas("data", "données"),
	days("days", "jours"),
	Delete("Delete", "Supprimer"),
	Description("Description", "Description"),
	Details("Details", "Détails"),
	extensions("extensions", "extensions"),
	Extensions("Extensions", "Extensions"),
	_for("for", "pour"),
	From("From", "De"),
	including("including", "incluant"),
	into("into", "vers"),
	Labels("Labels", "Etiquettes"),
	last("last", "dernier"),
	on("on", "sur"),
	on__date__("on", "le"),
	opened("opened", "ouvert"),
	Page("Page", "Page"),
	Mosaic("Mosaic", "Mosaïque"),
	Name("Name", "Nom"),
	Never("Never", "Jamais"),
	No("No", "Non"),
	Opened("Opened", "Ouvert"),
	Options("Options", "Options"),
	Rate("Rate", "Note"),
	Rating("Rating", "Notation"),
	Refreshing("Refreshing", "Rafraîchissement"),
	Remove("Remove", "Supprimer"),
	Rename("Rename", "Renommer"),
	Resume("Resume", "Résumé"),
	Retrieve("Retrieve", "Récupération"),
	Reviews("Reviews", "Critiques"),
	reviews("reviews", "critiques"),
	Search("Search", "Recherche"),
	Source("Source", "Source"),
	Sources("Sources", "Sources"),
	Table("Table", "Table"),
	times("times", "fois"),
	Update("Update", "Mise à jour"),
	Views("Views", "Vues"),
	
	A_new_version_has_been_found__Retrieving_information_about_this_version("A new version has been found. Retrieving information about this version", "Une mise à jour a été trouvée. Récupération des informations sur cette version"),
	About_DataOrganizer("About DataOrganizer", "A propos de DataOrganizer"),
	Add_a_data_of_type("Add a data of type", "Ajouter une donnée de type"),
	Add_data("Add data", "Ajouter des données"),
	Add_data_from_a_folder("Add data from a folder", "Ajouter des données depuis un dossier"),
	Add_data_from_a_folder__description("Specify from which folder to add data and how the folder should be analyzed.", "Spécifiez depuis quel dossier vous souhaitez ajouter des données et comment ce dossier doit être analysé."),
	Add_the_new_data_anyway("Add the new data anyway", "Quand même ajouter la nouvelle donnée"),
	Amovible_media("Amovible media", "Média amovible"),
	Analyze_sub_folders_recursively("Analyze sub-folders recursively", "Parcourir les sous-dossiers recursivement"),
	Analyzing_database("Analyzing DataBase", "Analyse de la base de données"),
	Analyzing_file_system("Analyzing file system", "Analyse du système de fichiers"),
	analyzed_folders("folders analyzed", "dossiers analysés"),
	analyzed_files("files analyzed", "fichiers analysés"),
	Application_update("DataOrganizer update", "Mise à jour de DataOrganizer"),
	Are_you_sure_you_want_to_remove_the_label("Are you sure you want to remove the label", "Etes-vous sûr de vouloir supprimer l'étiquette"),
	Browse_local("Browse local", "Parcourir l'ordinateur"),
	Cannot_found("Cannot found", "Impossible de trouver"),
	Content_type("Content type", "Type de contenu"),
	Content_types_to_detect("Content types to detect", "Types de contenu à détecter"),
	Copying_files("Copying files", "Copie des fichiers"),
	Create_and_retrieve_information("Create data and retrieve information", "Créer la donnée et récupérer les informations"),
	Create_label("Create a label", "Créer une étiquette"),
	Creating_labels_project("Creating lables project", "Création du project pour les étiquettes"),
	Data_added("Data added", "Données ajoutées"),
	data_have_been_added_before_cancel("data have been added before you cancelled the operation. Do you want to remove these data from the database ?", "donnée(s) ont été ajoutée(s) avant l'annulation de l'opération. Voulez-vous supprimer ces données de la base de données ?"),
	data_impacted("data impacted", "donnée(s) impactée(s)"),
	Data_opened("Data opened", "Donnée(s) ouverte(s)"),
	data__s_to_database("data to database", "donnée(s) à la base de données"),
	data_successfully_added("data have been successfully added to the database", "donnée(s) ont été ajoutée(s) à la base de données avec succès"),
	Deselect_all("Deselect all", "Désélectionner tout"),
	Detecting_data_from_analyzed_files("Detecting data from analyzed files and folders", "Détection de données à partir des fichiers et dossiers analysés"),
	Detecting_data_for_type("Detecting data of type", "Détection des données de type"),
	Do_action_for_same_situation("Do this action for all data in the same situation", "Effectuer cette action pour toutes les données dans la même situation"),
	Do_not_rate_this_data("Do not rate this data", "Ne pas noter cette donnée"),
	Do_you_confirm_this_operation("Do you confirm this operation ?", "Confirmez-vous cette opération ?"),
	Do_not_do_anything__skip_new_data("Do not do anything (skip the new data and do not store it in the database)", "Ne rien faire (la nouvelle donnée sera ignorée et ne figurera pas dans la base de données)"),
	Downloading_latest_version("Downloading latest version", "Télechargement de la dernière version"),
	Drag_data_to_a_label("Drag data to a label to attach this label to the data", "Glissez des données vers une étiquette pour attacher cette étiquette aux données"),
	Duplicate_data("Duplicate data", "Donnée en double"),
	Enter_the_name_of_the_label("Enter the name of the label", "Entrez le nom pour l'étiquette"),
	Enter_the_name_of_the_new_label("Enter the name of the new label", "Entrez le nom pour la nouvelle étiquette"),
	Extracting_files("Extracting files", "Extraction des fichiers"),
	Files_and_folders_found("Files and folders found", "Fichiers et dossiers trouvés"),
	Files_found("Files found", "Fichiers trouvés"),
	Files_not_detected("Files not detected", "Fichiers non détectés"),
	Filter_files_already_linked("Filter files already linked", "Filtre les fichiers déjà liés"),
	Folders_found("Folders found", "Dossiers trouvés"),
	Folder_URI("Folder URI", "Dossier URI"),
	Go_to__page_of__("Go to %#1% page of %#2%", "Voir la page %#1% de %#2%"),
	Ignore_all("Ignore all", "Ignorer tout"),
	Informtion_already_retrieved("Information have been already retrieved from all known sources", "Les informations ont déjà été récupérées depuis toutes les sources connues"),
	Initializing_application("Initializing application", "Initialisation de l'application"),
	into_the_label("into the label", "vers l'étiquette"),
	is_exactly_the_same_as_data("is exactly the same as data", "est exactement la même que la donnée"),
	is_not_in_your_database("is not in your database", "n'est pas dans votre base de données"),
	Keep_data_with_links_on_removed_files("Keep the data with links on removed files", "Conserver la donnée avec les liens sur les fichiers supprimés"),
	Last_open("Last open", "Dernière ouverture"),
	Load_information_from_the_data_sources("Load information from the data sources (i.e. previews on videos)", "Récupérer des information depuis les sources (par ex. prévisualisation sur les vidéos)"),
	Loading_application("Loading DataOrganizer application", "Chargement de l'application DataOrganizer"),
	Loading_data("Loading data", "Chargement des données"),
	Loading_database("Loading database", "Chargement de la base de données"),
	Loading_labels("Loading labels", "Chargement des étiquettes"),
	Loading_workspace("Loading workspace", "Chargement de l'espace de travail"),
	Main_criteria("Main criteria", "Critères principaux"),
	Move_labels("Move labels", "Déplacer des étiquettes"),
	New_label("New label", "Nouvelle étiquette"),
	No_database_exist("No database exist", "Pas de base de données existante"),
	No_image("No image", "Pas d'image"),
	no_rated("no rated", "non noté"),
	No_result("No result", "Pas de résultats"),
	No_update_installed("No update have been installed", "Aucune mise à jour n'a été installée"),
	None_of_the_known_sources_are_already_retrieved("None of the known sources are already retrieved", "Aucune des sources connues n'a déjà été récupérée"),
	Not_labeled("Not labeled", "Non étiquetté"),
	Not_rated("Not rated", "Non noté"),
	Old_database("Old database", "Ancienne base de données"),
	Only_if_not_yet_done("Only if not yet done", "Seulement si pas encore fait"),
	opened_on("opened on", "ouvert le"),
	Opening_database("Opening database", "Ouverture de la base de données"),
	Opening_labels_project("Opening labels project", "Chargement du projet contenant les étiquettes"),
	Operation_cancelled("Operation cancelled", "Opération annulée"),
	or_any_URI("or any URI", "ou d'une URI"),
	Please_insert_the_amovible_media_containing("Please insert the amovible media containing", "Veuillez insérer le média amovible contenant"),
	Please_select_at_least_one_content_type("Please select at least one content type", "Veuillez sélectionner au moins un type de contenu"),
	Please_select_the_dates_to_take_into_account("Please select the dates to take into account","Veuillez sélectionner les dates à prendre en compte"),
	Please_specify_a_folder_to_analyze("Please specify a folder to analyze", "Veuillez spécifier un dossier à analyzer"),
	Reading_database_content("Reading database content", "Lecture du contenu de la base de données"),
	Refresh_database("Refresh database", "Rafraîchir la base de données"),
	Refreshing_database_content("Refreshing database content", "Rafraîchissement du contenu de la base de données"),
	Refreshing_labels("Refreshing labels", "Rafraîchissement des étiquettes"),
	Refresh_information_from__("Refresh information from %#1%", "Rafraîchir les informations de %#1%"),
	Remove_data("Remove data", "Supprimer la donnée"),
	Remove_data__s("Remove data", "Supprimer des données"),
	Remove_from_database_and_filesystem("Remove from database and filesystem", "Supprimer de la base de données et de l'ordinateur"),
	Remove_label("Remove label", "Supprimer une étiquette"),
	Remove_links_to_removed_files("Remove links to removed files", "Supprimer les liens vers les fichiers supprimés"),
	Remove_only_from_database("Remove only from database", "Supprimer uniquement de la base de données"),
	Remove_the_new_data_from_the_filesystem("Remove the new data from the file system and keep the current", "Supprimer la nouvelle donnée de l'ordinateur, et conserver la donnée actuelle"),
	Remove_the_old_data_from_the_filesystem_and_replace_by_new("Remove the old data from the file system and replace it with the new one", "Supprimer la donnée actuelle de l'ordinateur, et remplacer par la nouvelle dans la base de données"),
	Removing_data("Removing data", "Suppression de données"),
	Rename_label("Rename label", "Renommer une étiquette"),
	Replace_completly_the_current_data("Replace completly the current data with the new one (no information about the current data will be kept)", "Remplacer la donnée actuelle avec la nouvelle (aucune information à propos de la donnée actuelle ne sera gardée)"),
	results_have_been_found("results have been found", "résultats ont été trouvés"),
	Refresh_all_already_retrieved_sources("Refresh all already retrieved sources", "Rafraîchir toutes les sources déjà récupérées"),
	Retrieve_from_all_missing_sources("Retrieve from all missing sources", "Récupérer depuis toutes les sources manquantes"),
	Retrieve_from_all_sources("Retrieve from all sources", "Récupérer depuis toutes les sources"),
	Retrieve_information("Retrieve information", "Recupérer des informations"),
	Retrieve_information_from_Internet("Retrieve information from Internet", "Récupérer les informations depuis Internet"),
	Retrieving_update_information("Retrieving update information", "Récupération des informations de mise à jour"),
	Reverse_search("Reverse search criterion", "Inverser le critère de recherche"),
	Search_result("Search result", "Résultat de la recherche"),
	Searching_data("Searching data", "Recherche de données"),
	See_reviews("See reviews", "Voir les critiques"),
	Select_all("Select all", "Sélectionner tout"),
	Select_how_you_want_to_add_data("Select how you want to add data", "Sélectionnez la manière dont vous souhaitez ajouter des données"),
	Select_the_labels("Select the labels", "Sélectionnez les étiquettes"),
	Select_your_rating("Select your rating", "Sélectionnez votre note"),
	Several_results_found("Several results found", "Plusieurs résultats trouvés"),
	Some_data_have_been_opened_and_need_action("Some data have been opened and need action", "Des données ont été ouvertes et demande votre action"),
	Support_the_project("Support the project", "Aidez le projet"),
	Take_all_into_account("Take all into account", "Tous les comptabiliser"),
	that_is_currently_present_in_the_database("currently present in the database", "qui est actuellement en base de données"),
	The_name_cannot_be_empty("The name cannot be empty", "Le nom ne peut pas être vide"),
	The_new_data("The new data", "La nouvelle donnée"),
	The_specified_folder_doesnt_exist("The specified folder doesn't exist", "Le dossier spécifié n'existe pas"),
	The_specified_folder_is_not_a_folder("The specified folder is not a folder", "Le dossier spécifié n'est pas un dossier"),
	The_specified_folder_URI_is_not_valid("The specified folder URI is not valid", "Le dossier URI spécifié n'est pas valide"),
	The_specified_folder_URI_is_malformed("The specified folder URI is malformed", "Le dossier URI spécifié est malformé"),
	This_label_already_exists("This label already exists", "Cette étiquette existe déjà"),
	Try_to_detect_data_even_on_files_already_linked_to_an_existing_data("Try to detect data even on files already linked to an existing data", "Essayer de détecter des données même sur les fichiers déjà liés à une donnée existante"),
	Try_to_relocate_sources_if_necessary("Try to re-locate sources if necessary (files moved)", "Essayer de retrouver les sources si nécessaire (fichiers déplacés)"),
	Unable_to_create_the_new_label("Unable to create the new label", "Impossible de créer la nouvelle étiquette"),
	Unable_to_locate_file("Unable to locate file", "Impossible de localiser le fichier"),
	Unable_to_move_label("Unable to move the label", "Impossible de déplacer l'étiquette"),
	Unable_to_remove_the_label("Unable to remove the label", "Impossible de supprimer l'étiquette"),
	Unable_to_rename_the_label("Unable to rename the label", "Impossible de renommer l'étiquette"),
	Update_application("Update application", "Mettre à jour l'application"),
	Update_check_frequency("Check for available update every", "Vérifier les mises à jour disponibles tous les"),
	Update_current_data_with_new_location("Update the current data with the new location (keep all the information, only update the data location)", "Mettre à jour la donnée actuelle avec son nouvel emplacement (toutes les informations sont conservées, uniquement l'emplacement est mis à jour)"),
	What_do_you_want_to_do("What do you want to do ?", "Que souhaitez vous faire ?"),
	You_are_requesting_to_remove("You are requesting to remove", "Vous demandez la suppression de"),
	You_must_select_a_data_type("You must select a data type", "Vous devez sélectionner un type de donnée"),
	You_must_select_the_way_you_want_to_add_data("You must select the way you want to add data", "Vous devez sélectionner une manière d'ajouter les données"),
	Your_review("Your review", "Votre critique"),
	
	
	A_label_name_cannot_contain_a_slash("A label name cannot contain a slash (/)", "Le nom d'une étiquette ne peut pas contenir de slash (/)"),
	A_label_name_cannot_contain_the_character__("A label name cannot contain the character ¤", "Le nom d'une étiquette ne peut pas contenir le caractère ¤"),
	are_stored_in_database_with_version("are stored in database with version", "sont stockées en base de données avec la version"),
	but_current_version_is("but current version is", "et la version actuelle est"),
	current_data_no_more_in_database("However the data currently in the database doesn't exist anymore, so it seems the data has been moved", "Cependant la donnée actuellement en base de données n'exist plus, il semble donc qu'elle est été déplacée"),
	Data_for_content_type("Data for content type", "Les données pour le type de contenu"),
	retry_with_a_different_name("retry with a different name", "réessayer avec un nom différent"),
	You_are_about_to_move_labels("You are about to move the following labels", "Vous vous apprêtez à déplacer les étiquettes suivantes"),
	You_can_retry_with_a_different_name("You can retry with a different name", "Vous pouvez réessayer avec un nom différent"),
	You_must_provide_a_name("You must provide a name", "Vous devez indiquer un nom"),
	
	
	MENU_Database("&Data Base", "&Base de données"),
	MENU_Options("&Options", "&Options"),
	MENU_Help("&Help", "&Aide"),
	MENU_ITEM_Options_Configuration("&Preferences", "&Préférences"),
	MENU_ITEM_About_DataOrganizer("&About DataOrganizer...", "&A propos de DataOrganizer..."),
	
	NoPluginToRetrieveInforForType("There is no plug-in to retrieve information for content type", "Il n'y a pas de plug-in pour récupérer des information du type"),
	ERROR_Invalid_DB_Version(
		"Invalid database version: the database you are trying to open has been created by a newer version of the software, it is not possible to open it. Please update your software.",
		"Version de base de données invalide: la base de données que vous essayez d'ouvrir a été crée par une version plus récente de l'application, et il n'est pas possible de l'ouvrir. Veuillez mettre à jour votre application."
	),
	ERROR_Invalid_DB_Version_ContentType(
		"Invalid database version: the database you are trying to open contains data of type",
		"Version de base de données invalide: la base de données que vous essayez d'ouvrir contient des données de type"
	),
	ERROR_Invalid_DB_Version_ContentType2(
		"that has been created by a newer version of the plug-in, it is not possible to open it. Please update your software or your plug-in",
		"qui ont été crées par une version plus récente de logiciel, il n'est donc pas possible de l'ouvrir. Veuillez mettre à jour votre application"
	),
	ERROR_Invalid_DB_Version_Older1(
		"Your database contains data from an older version",
		"Votre base de données contient des données d'une version précedente de l'application"
	),
	ERROR_Invalid_DB_Version_Older2(
		"\r\nYou need to convert this database into the current version.\r\n" +
		"Warning: after the conversion the database won't be openable with a previous version anymore." +
		"\r\n\r\nAre you sure you want to convert your database and open it ?",
		"\r\nVous devez convertir cette base de données vers la version actuelle.\r\n" +
		"Attention: après la conversion il ne sera plus possible d'ouvrir la base de données avec une version précédente." +
		"\r\n\r\nEtes-vous sûr de vouloir convertir votre base de données et l'ouvrir avec cette version de l'application ?"
	),
	ERROR_UPDATE_WEB_PERSISTS(
		"If the problem persists, you may need to update your application manually. To do so, follow these steps:\r\n" +
		" 1. Download the latest version from dataorganizer.webhop.net/download\r\n" +
		" 2. Uninstall your current version of the application (databse won't be lost)\r\n" +
		" 3. Install the version you just download\r\n" +
		"We are sorry for any inconveniance. Do not hesitate to signal the problem so we can fix it for the next releases.",
		"Si le problème persiste, vous aurez besoin de mettre à jour votre application manuellement. Pour le faire, suivez ces étapes:\r\n" +
		" 1. Télechargez la dernière version depuis dataorganizer.webhop.net/download\r\n" +
		" 2. Désinstallez votre version actuelle de l'application (aucune base de données ne sera perdue)\r\n" +
		" 3. Installez la version que vous venez de télecharger\r\n" +
		"Veuillez excuser cette erreur. N'hésitez pas à nous en faire part afin que nous puissions corriger le problème à l'avenir."
	),
	ERROR_UPDATE_WEB_CONNECTION(
		"Unable to contact DataOrganizer website.\r\n" +
		"Please check your internet connection.\r\n" +
		ERROR_UPDATE_WEB_PERSISTS.english,
		"Impossible de contacter le site web de DataOrganizer.\r\n" +
		"Veuillez vérifier votre connexion internet.\r\n" +
		ERROR_UPDATE_WEB_PERSISTS.french
	),
	ERROR_UPDATE_WEB_SITE(
		"Unable to retrieve update information from DataOrganizer website.\r\n" +
		ERROR_UPDATE_WEB_PERSISTS.english,
		"Impossible d'obtenir les informations de mise à jour depuis le site web de DataOrganizer.\r\n" +
		ERROR_UPDATE_WEB_PERSISTS.french
	),
	ERROR_UPDATE_INTERNAL(
		"Unable to get correct update information from DataOrganizer website.\r\n" +
		"Try again later.\r\n" +
		ERROR_UPDATE_WEB_PERSISTS.english,
		"Impossible d'obtenir les informations de mise à jour depuis le site web de DataOrganizer.\r\n" +
		"Veuillez réessayer l'opération plus tard.\r\n" +
		ERROR_UPDATE_WEB_PERSISTS.french
	),
	ERROR_UPDATE_WEB(
		"Unable to get the latest update from DataOrganizer website.\r\n" +
		"Try again later.\r\n" +
		ERROR_UPDATE_WEB_PERSISTS.english,
		"Impossible d'obtenir la dernière mise à jour depuis le site web de DataOrganizer.\r\n" +
		"Try again later.\r\n" +
		ERROR_UPDATE_WEB_PERSISTS.french
	),
	ERROR_UPDATE_FILESYSTEM(
		"An error occured while preparing the update.\r\n" +
		ERROR_UPDATE_WEB_PERSISTS.english,
		"Une erreur est survenue lors de la préparation de la mise à jour.\r\n" +
		ERROR_UPDATE_WEB_PERSISTS.french
	),
	
	
	HELP_Search_Name(
		"Enter words separated by spaces",
		"Entrez des mots séparés par des espaces"
	),
	HELP_Search_Rate(
		"Enter a value, or two values separated by character - to specify a range.<br>i.e. <b>10-15</b> or <b>8</b>.<br>Values must be between 0 and 20, empty means all data including the data that are not rated.",
		"Entrez une valeur, ou deux valeurs séparées par le caractère - pour spécifier une gamme.<br>ex: <b>10-15</b> ou <b>8</b>.<br>Les valeurs doivent être comprises entre 0 et 20, vide signifiant toutes les données y compris celles qui ne sont pas notées."
	),
	HELP_Search_Added(
		"Select a minimum and maximum date.<br>If the minimum date is empty, it means all dates until the maximum date.<br>If the maximum date is empty, it means all dates from the minimum date.<br>If both are empty, it means all dates.",
		"Sélectionnez une date minimum et maximum.<br>Si la date minimum est vide, cela signifie toutes les dates jusqu'à la date maximum.<br>Si la date maximum est vide, cela signifie toutes les dates depuis la date minimum.<br>Si les deux sont vides, cela signifie toutes les dates."
	),
	HELP_Search_Opened(
		"Select a minimum and maximum date.<br>If the minimum date is empty, it means all dates until the maximum date.<br>If the maximum date is empty, it means all dates from the minimum date.<br>If both are empty, it means all dates.",
		"Sélectionnez une date minimum et maximum.<br>Si la date minimum est vide, cela signifie toutes les dates jusqu'à la date maximum.<br>Si la date maximum est vide, cela signifie toutes les dates depuis la date minimum.<br>Si les deux sont vides, cela signifie toutes les dates."
	),
	HELP_Search_Labels(
		"Select all labels to include in the search.<br>If nothing is selected, it means all data.",
		"Sélectionnez toutes les étiquettes à inclure dans la recherche.<br>Si rien n'est sélectionné, toutes les données seront inclues."
	),


	OPENED_DATA_DIALOG_MESSAGE(
		"You opened the data <a href=\"data\">%#1%</a> %#2%.<br> <br>" +
		"This dialog gives you the opportunity to indicate if an opening was relevant or not, and so if DataOrganizer should take it into account (for counting the opening, dates of opening...). Indeed, if you open a video and close it quickly because it was not interesting, you may not want to count it because you didn't really watch it.<br>" +
		"This dialog is also a good opportunity to rate the data, and to put a comment on it.",
		"Vous avez ouvert la donnée <a href=\"data\">%#1%</a> %#2%.<br> <br>" +
		"Cette fenêtre vous donne la possibilité d'indiquer si l'ouverture de cette donnée était pertinente ou pas, et donc si DataOrganizer doit la prendre en compte (pour le décompte des ouvertures, leurs dates...). En effet, lorsque vous ouvrez une vidéo et la fermez rapidement car finalement elle ne vous intéressait pas, vous pouvez souhaiter ne pas la comptabiliser car vous ne l'avez pas réellement regardée.<br>" +
		"Cette fenêtre est également l'occasion pour vous de donner une note à cette donnée, et d'y déposer un commentaire."
	),
	
	MESSAGE_Update_Available(
		"<b>A new version of DataOrganizer is available !</b><br/>" +
		"Your current version is %#1% and version %#2% is available.<br/>" +
		"<a href=\"news\">Check what's new</a>",
		"<b>Une nouvelle version de DataOrganizer est disponible !</b><br/>" +
		"Votre version actuelle est %#1% et la version %#2% est disponible.<br/>" +
		"<a href=\"news\">Voir ce qu'il y a de nouveau</a>"
	),
	MESSAGE_Update_Now(
		"Update now to the new version (need to close and restart the application).",
		"Mettre à jour maintenant (l'application va devoir être fermée et redémarrée)."
	),
	MESSAGE_Update_Later(
		"Later.",
		"Plus tard"
	),
	
	MESSAGE_About_Licenses(
		"DataOrganizer is under <a href=\"gpl\">GPL License</a><br/>"+
		"It contains a copy of VLC also under GPL License<br/>"+
		"It is based on the Eclipse product under <a href=\"epl\">EPL License</a><br/>"+
		"It contains Apache libraries under <a href=\"apache\">Apache license</a>",
		"DataOrganizer est sous <a href=\"gpl\">licence GPL</a><br/>"+
		"Il contient une copie de VLC également sous licence GPL<br/>"+
		"Ce logiciel est basé sur un produit Eclipse sous <a href=\"epl\">licence EPL</a><br/>"+
		"Il contient des librairies Apache sous <a href=\"apache\">licence Apache</a>"
	),
	
	MESSAGE_Add_Data(
		"%#1% data are ready to be added to your database.<br/>" +
		"<p marginTop=3>" +
		"They are listed in the table below, and you are free to remove some of them." +
		"</p>",
		"%#1% donnée(s) sont prêtes à être ajoutées à votre base de données.<br/>" +
		"<p marginTop=3>" +
		"Elles sont listées dans la table ci-dessous, et vous êtes libre d'en supprimer certaines." +
		"</p>"
	),
	
	MESSAGE_Not_Detected_Header(
		"Some files have not been included into a data:",
		"Certains fichiers n'ont pas été inclus dans une données:"
	),
	MESSAGE_Not_Detected_Typed_Files(
		"<a href=\"typed\">%#1% files</a> which have been a reconized type, but not added:<br/>" +
		" - you choose to do not add those files<br/>" +
		" - or even they have been reconized, it was not possible to include them into a data",
		"<a href=\"typed\">%#1% fichiers</a> ont un type reconnu, mais n'ont pas été ajoutés:<br/>" +
		" - vous avez choisi de ne pas ajouter ces fichiers<br/>" +
		" - ou malgré que leur type ait pu être reconnu, il n'a pas été possible de les inclure dans une donnée"
	),
	MESSAGE_Not_Detected_Not_Typed_Files(
		"<a href=\"not_typed\">%#1% files</a> which have not been reconized:<br/>" +
		" - the types of content of those files are effectivly not supported types,<br>" +
		" - you didn't select those types so they are not added,<br>" +
		" - this version of DataOrganizer was not able to detect those files as data; in this case, check you have the latest version of DataOrganizer, if yes you can signal it to the author.",
		"<a href=\"not_typed\">%#1% fichiers</a> n'ont pas été reconnus:<br/>" +
		" - le type de contenu de ces fichiers ne sont effectivement pas des types supportés,<br>" +
		" - vous n'avez pas sélectionné ces types, ils n'ont donc pas été ajoutés,<br>" +
		" - cette version de DataOrganizer n'a pas été capable de détecter ces fichiers comme des données; dans ce cas, vérifez que vous avez la dernière version de DataOrganizer, si tel est le cas vous pouvez le signaler à l'auteur."
	),
	MESSAGE_Not_Detected_Footer(
		"<p marginTop=5>"+
		"A data has not been added ? some files are not reconized ? Do not hesitate to let us know !!"+
		"</p>",
		"<p marginTop=5>"+
		"Une donnée n'a pas été ajoutée ? des fichiers non reconnus ? n'hésitez pas à nous en faire part !!"+
		"</p>"
	),
	
	MESSAGE_Contact(
		"You can contact us simply by mail to <a href=\"mail\">support.dataorganizer@gmail.com</a><br/>"+
		"Try to be as precise as possible and to indicate the version number you are using, so that we can be most effective to process your request.<br/>"+
		"<br/>"+
		"Or visit our <a href=\"forum\">forum</a>.",
		"Vous pouvez nous contacter simplement par mail à l'adresse <a href=\"mail\">support.dataorganizer@gmail.com</a><br/>"+
		"Pensez à être le plus précis possible et à nous indiquer le numéro de version que vous utilisez, ceci afin que nous puissions être le plus efficace possible pour traiter votre demande.<br/>" +
		"<br/>"+
		"Ou visitez notre <a href=\"forum\">forum</a>."
	),
	
	MESSAGE_Preference_Update(
		"Configure how DataOrganizer can be updated.",
		"Configure la manière de mettre à jour DataOrganizer."
	),
	
	MESSAGE_Files_removed(
		"The file(s)<br/>%#1%has(ve) been removed from the file system but the data %#2% contains linked to this(ese) file(s).<br/>What do you want to do ?",
		"Le(s) fichier(s)<br/>%#1%a(ont) été supprimé(s) de l'ordinateur mais la donnée %#2% contient des liens vers ce(s) fichier(s).<br/>Que souhaitez-vous faire ?"
	),
		
	
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
	public String toString() {
		switch (Application.language) {
		case FRENCH: return french;
		default: return english;
		}
	}
}
