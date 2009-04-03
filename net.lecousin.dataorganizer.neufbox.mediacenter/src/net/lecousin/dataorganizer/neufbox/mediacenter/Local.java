package net.lecousin.dataorganizer.neufbox.mediacenter;

import net.lecousin.framework.application.Application;

public enum Local {

	Add_to_media_center("Add to NeufBox Media Center", "Ajouter au NeufBox Media Center"),
	File_not_found("File not found", "Fichier introuvable"),
	
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
