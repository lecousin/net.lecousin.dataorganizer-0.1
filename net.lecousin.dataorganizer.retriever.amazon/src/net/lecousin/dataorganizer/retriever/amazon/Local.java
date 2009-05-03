package net.lecousin.dataorganizer.retriever.amazon;

import net.lecousin.framework.application.Application;

public enum Local {

	AlbumImage("Album images", "Images de l'album"),
	AlbumInformation("Album information", "Informations sur l'album"),
	Image("Image", "Image"),
	MovieInformation("Movie information", "Informations sur le film"),
	Page("Page", "Page"),
	Poster("Poster", "Affiche"),
	PublicReviews("Public reviews", "Critiques du public"),
	UnknownAuthor("Unknown", "Inconnu"),
	
	
	MESSAGE_Choose_Pictures(
		"Images have been found on Amazon for data %#1%. Please select how you want to add these images to the data.",
		"Des images ont été trouvées sur Amazon pour la donnée %#1%. Veuillez sélectionner comment vous souhaitez ajouter ces images à la donnée."
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
	public java.lang.String toString() {
		switch (Application.language) {
		case FRENCH: return french;
		default: return english;
		}
	}
}
