package net.lecousin.dataorganizer.people;

import net.lecousin.framework.application.Application;

public enum Local {

	Activity("Activity", "Activit�"),
	at("at", "�"),
	Born_at("Born at", "N�(e) �"),
	People("People", "Personne"),
	Photo("Photo", "Photo"),
	Photos("Photos", "Photos"),
	Public("Public", "Public"),
	
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
