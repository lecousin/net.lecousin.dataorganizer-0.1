package net.lecousin.dataorganizer.allocine;

import net.lecousin.framework.application.Application;

public enum Local {

	Activities("Activities", "Activités"),
	Casting("Casting", "Casting"),
	Movie_information("Movie information", "Information sur le film"),
	Page("Page", "Page"),
	People_information("People information", "Information sur la personne"),
	Poster("Poster", "Affiche"),
	Public_reviews("Public reviews", "Critiques du public"),
	Press_reviews("Press reviews", "Critiques de presse"),
	
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
