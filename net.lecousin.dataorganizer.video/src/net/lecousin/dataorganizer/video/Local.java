package net.lecousin.dataorganizer.video;

import net.lecousin.framework.application.Application;

public enum Local {

	Actors("Actors", "Acteurs"),
	Add_at_least_one_file_to_the_list("Add at least one file to the list", "Ajoutez au moins un fichier à la liste"),
	Add_files("Add files", "Ajouter des fichiers"),
	Add_to_Media_Player_List("Add to Media Player List", "Ajouter à la liste du Media Player"),
	Add_videos("Add videos", "Ajouter des vidéos"),
	Casting("Casting", "Casting"),
	Dimension("Dimension", "Dimension"),
	Directed_by("Directed by", "Réalisé par"),
	Direction("Direction", "Réalisation"),
	Duration("Duration", "Durée"),
	doesnt_exist("doesn't exist", "n'existe pas"),
	Genre("Genre", "Genre"),
	No_source("You do not have a physical source for this data", "Vous n'avez pas de source physique pour cette donnée"),
	Open("Open", "Ouvrir"),
	Open_in_Media_Player("Open in Media Player", "Ouvrir avec le Media Player"),
	Open_with_system_application("Open with default system application", "Ouvrir avec l'application système par défaut"),
	People("People", "Personne"),
	Poster("Poster", "Affiche"),
	Press("Press", "Presse"),
	Preview("Preview", "Preview"),
	Producted_by("Producted by", "Produit par"),
	Production("Production", "Production"),
	Public("Public", "Public"),
	Release("Release", "Sortie"),
	Role("Role", "Rôle"),
	Select_the_video_files_to_add("Select the video files to add", "Sélectionnez les fichiers vidéos à ajouter"),
	Select_the_videos_you_want_to_add("Select the videos you want to add", "Sélectionnez les vidéos que vous souhaitez ajouter"),
	The_file("The file", "Le fichier"),
	Unable_to_locate_file("Unable to locate file", "Impossible de localiser le fichier"),
	Unable_to_open_video("Unable to open the video file", "Impossible d'ouvrir le fichier vidéo"),
	Video("Video", "Vidéo"),
	View_all("View all", "Tout voir"),
	Write("Writen by", "Scénario"),
	Writen_by("Writen by", "Scénaristes"),
	
	
	HELP_Search_Duration(
		"Select a minimum and maximum duration.<br>If the minimum duration is empty, it means 0.<br>If the maximum duration is empty, it means all duration from the minimum.<br>The format of a duration is <b><i>hh</i>:<i>mm</i>:<i>ss</i></b><br><b>Only videos having a known duration can be selected in the search result.</b>",
		"Sélectionnez une durée minimum et maximum.<br>Si la durée minimum est vide, cela signifie 0.<br>Si la durée maximum est vide, cela signifie toutes les durées à partir du minimum.<br>Le format d'une durée est <b><i>hh</i>:<i>mm</i>:<i>ss</i></b><br><b>Uniquement les vidéos qui ont une durée connue seront sélectionnées dans le résultat.</b>"
	),
	HELP_Search_Casting(
		"Enter a list of words.<br>For a video to be selected, one people that has participated to the movie must contain all those word in his name.<br>People are actors, producers, directors and writers.<br><b>Only videos that have those kind of information (from internet) can be selected in the search result.</b>",
		"Entrez une liste de mots.<br>Pour qu'une vidéo soit sélectionnée, au moins une personne ayant participé à cette vidéo doit contenir dans son nom tous les mots spécifiés.<br>Ces personnes sont les acteurs, les producteurs, les réalisateurs et les scénaristes.<br><b>Uniquement les vidéos ayant ces informations (récupérées depuis Internet) pourront être sélectionnées dans le résultat.</b>"
	),
	
	MESSAGE_Take_Previews(
		"The rectangle next is used to take video previews. Please do not do anything during this step.<br/>" +
		"If you go to another application this window will be hiden and previews won't be valid !<br/>" +
		"This step may take time, please wait...",
		"Le rectangle à côté est utilisé pour prendre des images depuis les vidéos. Veuillez ne rien faire pendant cette étape.<br/>" +
		"Si vous allez sur une autre application cette fenêtre sera cachée et les images ne seront pas valides !<br/>" +
		"Cette étape peut durer un peu de temps, veuillez patienter..."
	),
	
	;
	
	private Local(String english, String french) {
		this.english = english;
		this.french = french;
	}
	private String english;
	private String french;
	@Override
	public java.lang.String toString() {
		switch (Application.language) {
		case FRENCH: return french;
		default: return english;
		}
	}
}
