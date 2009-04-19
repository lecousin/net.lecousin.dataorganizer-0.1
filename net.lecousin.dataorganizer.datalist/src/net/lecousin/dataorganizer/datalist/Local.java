package net.lecousin.dataorganizer.datalist;

import net.lecousin.framework.application.Application;

public enum Local {

	A_list_already_exists_with_the_same_name("A list already exists with the same name", "Une list existe déjà avec le même nom"),
	Add_to_list("Add to list", "Ajouter à une liste"),
	Create_new_list("Create a new list", "Créer une nouvelle liste"),
	Data_lists("Data Lists", "Listes de données"),
	Enter_the_new_list_name("Enter the new name for the new list", "Entrez le nom de la nouvelle liste"),
	Name("Name", "Nom"),
	Open_data_lists("Open data lists", "Ouvrir les listes de données"),
	The_name_cannot_be_empty("The name cannot be empty", "Le nom ne peut pas être vide"),
	
	invalid_list_name("Invalid list name: only letters, digits and space are allowed", "Nom de liste invalide: uniquement les lettres, les chiffres et les espaces sont autorisés"),
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
