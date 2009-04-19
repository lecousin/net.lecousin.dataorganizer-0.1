package net.lecousin.dataorganizer.datalist;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.lecousin.dataorganizer.core.database.Data;
import net.lecousin.framework.event.Event;
import net.lecousin.framework.strings.StringUtil;

public class DataList {

	DataList(String name) {
		this.name = name;
	}
	
	private String name;
	private Set<Long> list = new LinkedHashSet<Long>();
	public Event<Long> dataAdded = new Event<Long>();
	public Event<Long> dataRemoved = new Event<Long>();
	
	public String getName() { return name; }
	public Set<Long> getDataIDs() { return list; }
	
	public void addDataID(Long id) {
		_addDataID(id);
		DataLists.getInstance().save(this);
		dataAdded.fire(id);
	}
	void _addDataID(Long id) {
		list.add(id);
	}
	public void addData(Data data) {
		addDataID(data.getID());
	}
	public void addData(List<Data> data) {
		for (Data d : data) addData(d);
	}
	
	public void removeDataID(Long id) {
		if (list.remove(id))
			dataRemoved.fire(id);
	}

	public static String validateName(String name) {
		for (int i = 0; i < name.length(); ++i)
			if (!StringUtil.isLetter(name.charAt(i)) && !StringUtil.isDigit(name.charAt(i)) && name.charAt(i) != ' ')
				return Local.invalid_list_name.toString();
		return null;
	}
}
