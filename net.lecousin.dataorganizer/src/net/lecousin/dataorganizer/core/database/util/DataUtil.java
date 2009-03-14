package net.lecousin.dataorganizer.core.database.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.dataorganizer.core.database.content.ContentType;

public class DataUtil {

	public static Map<ContentType,List<Data>> splitByContent(Iterable<Data> data) {
		HashMap<ContentType,List<Data>> result = new HashMap<ContentType,List<Data>>();
		for (Data d : data) {
			List<Data> list = result.get(d.getContentType());
			if (list == null) {
				list = new LinkedList<Data>();
				result.put(d.getContentType(), list);
			}
			list.add(d);
		}
		return result;
	}
	
}
