package net.lecousin.dataorganizer.ui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.dataorganizer.core.DataOrganizer;
import net.lecousin.dataorganizer.core.DataLabels.Label;
import net.lecousin.dataorganizer.core.database.Data;


public class DataOrganizerDND {

	private DataOrganizerDND() {}

	public static String getLabelsDNDString(List<Label> labels) {
		StringBuilder str = new StringBuilder("DataOrganizer:labels:");
		for (Label l : labels)
			str.append(l.getPath()).append('¤');
		return str.toString();
	}
	public static List<Label> getLabelsDNDFromString(String str) {
		if (!str.startsWith("DataOrganizer:labels:")) return null;
		String[] strs = str.substring(21).split("¤");
		List<Label> result = new ArrayList<Label>(strs.length);
		for (String s : strs)
			if (s.length() > 0) {
				Label l = DataOrganizer.labels().getLabelFromPath(s);
				if (l != null)
					result.add(l);
			}
		return result;
	}
	public static boolean isLabels(String str) {
		return str.startsWith("DataOrganizer:labels:");
	}
	
	public static String getDataDNDString(List<Data> data) {
		StringBuilder str = new StringBuilder();
		str.append("DataOrganizer:data:");
		boolean first = true;
		for (Data d : data) {
			if (first) first = false;
			else str.append(';');
			str.append(d.getID());
		}
		return str.toString();
	}
	public static List<Data> getDataDNDFromString(String str) {
		if (!str.startsWith("DataOrganizer:data:")) return null;
		String[] ids = str.substring(19).split(";");
		List<Data> result = new LinkedList<Data>();
		for (String sid : ids) {
			long id;
			try { id = Long.parseLong(sid); }
			catch (NumberFormatException e) { continue; }
			Data data = DataOrganizer.database().get(id);
			if (data == null) continue;
			result.add(data);
		}
		return result;
	}
	public static boolean isData(String str) {
		return str.startsWith("DataOrganizer:data:");
	}
	
}
