package net.lecousin.dataorganizer.video;

import net.lecousin.framework.application.Application;

public enum Local {

	Actors("Actors", "Acteurs"),
	Add_at_least_one_file_to_the_list("Add at least one file to the list", "Ajoutez au moins un fichier � la liste"),
	Add_files("Add files", "Ajouter des fichiers"),
	Add_to_Media_Player_List("Add to Media Player List", "Ajouter � la liste du Media Player"),
	Add_videos("Add videos", "Ajouter des vid�os"),
	Casting("Casting", "Casting"),
	Dimension("Dimension", "Dimension"),
	Directed_by("Directed by", "R�alis� par"),
	Direction("Direction", "R�alisation"),
	Duration("Duration", "Dur�e"),
	doesnt_exist("doesn't exist", "n'existe pas"),
	Genre("Genre", "Genre"),
	No_source("You do not have a physical source for this data", "Vous n'avez pas de source physique pour cette donn�e"),
	Open("Open", "Ouvrir"),
	Open_in_Media_Player("Open in Media Player", "Ouvrir avec le Media Player"),
	Open_with_system_application("Open with default system application", "Ouvrir avec l'application syst�me par d�faut"),
	People("People", "Personne"),
	Poster("Poster", "Affiche"),
	Press("Press", "Presse"),
	Preview("Preview", "Preview"),
	Producted_by("Producted by", "Produit par"),
	Production("Production", "Production"),
	Public("Public", "Public"),
	Release("Release", "Sortie"),
	Role("Role", "R�le"),
	Select_the_video_files_to_add("Select the video files to add", "S�lectionnez les fichiers vid�os � ajouter"),
	Select_the_videos_you_want_to_add("Select the videos you want to add", "S�lectionnez les vid�os que vous souhaitez ajouter"),
	The_file("The file", "Le fichier"),
	Unable_to_locate_file("Unable to locate file", "Impossible de localiser le fichier"),
	Unable_to_open_video("Unable to open the video file", "Impossible d'ouvrir le fichier vid�o"),
	Video("Video", "Vid�o"),
	View_all("View all", "Tout voir"),
	Write("Writen by", "Sc�nario"),
	Writen_by("Writen by", "Sc�naristes"),
	
	
	HELP_Search_Duration(
		"Select a minimum and maximum duration.<br>If the minimum duration is empty, it means 0.<br>If the maximum duration is empty, it means all duration from the minimum.<br>The format of a duration is <b><i>hh</i>:<i>mm</i>:<i>ss</i></b><br><b>Only videos having a known duration can be selected in the search result.</b>",
		"S�lectionnez une dur�e minimum et maximum.<br>Si la dur�e minimum est vide, cela signifie 0.<br>Si la dur�e maximum est vide, cela signifie toutes les dur�es � partir du minimum.<br>Le format d'une dur�e est <b><i>hh</i>:<i>mm</i>:<i>ss</i></b><br><b>Uniquement les vid�os qui ont une dur�e connue seront s�lectionn�es dans le r�sultat.</b>"
	),
	HELP_Search_Casting(
		"Enter a list of words.<br>For a video to be selected, one people that has participated to the movie must contain all those word in his name.<br>People are actors, producers, directors and writers.<br><b>Only videos that have those kind of information (from internet) can be selected in the search result.</b>",
		"Entrez une liste de mots.<br>Pour qu'une vid�o soit s�lectionn�e, au moins une personne ayant particip� � cette vid�o doit contenir dans son nom tous les mots sp�cifi�s.<br>Ces personnes sont les acteurs, les producteurs, les r�alisateurs et les sc�naristes.<br><b>Uniquement les vid�os ayant ces informations (r�cup�r�es depuis Internet) pourront �tre s�lectionn�es dans le r�sultat.</b>"
	),
	
	MESSAGE_Take_Previews(
		"The rectangle next is used to take video previews. Please do not do anything during this step.<br/>" +
		"If you go to another application this window will be hiden and previews won't be valid !<br/>" +
		"This step may take time, please wait...",
		"Le rectangle � c�t� est utilis� pour prendre des images depuis les vid�os. Veuillez ne rien faire pendant cette �tape.<br/>" +
		"Si vous allez sur une autre application cette fen�tre sera cach�e et les images ne seront pas valides !<br/>" +
		"Cette �tape peut durer un peu de temps, veuillez patienter..."
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
