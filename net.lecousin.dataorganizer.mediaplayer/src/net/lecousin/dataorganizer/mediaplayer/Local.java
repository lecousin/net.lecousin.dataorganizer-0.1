package net.lecousin.dataorganizer.mediaplayer;

import net.lecousin.framework.application.Application;

public enum Local {

	Show_details("Show details", "Voir détails"),
	Open_Media_Player("Open Media Player", "Ouvrir le Media Player"),
	Opened_data("Opened data", "Donnée(s) ouverte(s)"),
	
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
