package net.lecousin.dataorganizer.core.database.info;

import java.util.List;

import net.lecousin.dataorganizer.core.database.info.Info.DataLink;
import net.lecousin.dataorganizer.core.database.info.SourceInfo.Review;
import net.lecousin.framework.Pair;
import net.lecousin.framework.collections.CollectionUtil;
import net.lecousin.framework.collections.SelfMap;

public class SourceInfoMergeUtil {

	private SourceInfoMergeUtil(){}
	
	public static long mergeDate(long currentDate, long newDate) {
		if (currentDate > 0) return currentDate;
		return newDate;
	}
	public static String mergeString(String currentValue, String newValue) {
		if (currentValue == null || currentValue.length() == 0) return newValue;
		if (newValue == null || newValue.length() == 0) return currentValue;
		if (newValue.length() > currentValue.length()) return newValue;
		return currentValue;
	}
	
	public static void mergeReviews(SelfMap<String,Review> currentReviews, SelfMap<String,Review> newReviews) {
		if (newReviews == null) return;
		for (Review newReview : newReviews)
			if (!currentReviews.containsKey(newReview.getAuthor()))
				currentReviews.put(newReview);
	}
	
	public static void mergePeopleLists(List<Pair<List<String>,List<DataLink>>> currentList, List<Pair<String,DataLink>> newList) {
		for (Pair<String,DataLink> pNew : newList) {
			String newName = pNew.getValue1();
			DataLink newLink = pNew.getValue2();
			// looking for the same link
			Pair<List<String>,List<DataLink>> linkFound = null;
			for (Pair<List<String>,List<DataLink>> pOld : currentList) {
				for (DataLink oldLink : pOld.getValue2()) {
					if (!oldLink.isSame(newLink)) continue;
					linkFound = pOld;
					break;
				}
				if (linkFound != null) break;
			}
			if (linkFound == null) {
				// looking for the same name in a link
				for (Pair<List<String>,List<DataLink>> pOld : currentList) {
					for (DataLink oldLink : pOld.getValue2()) {
						if (oldLink.name == null) continue;
						if (newLink.name == null) continue;
						if (!oldLink.name.equals(newLink.name)) continue;
						linkFound = pOld;
						break;
					}
					if (linkFound != null) break;
				}
			}
			if (linkFound != null) {
				// we found a link => merge
				List<String> oldNames = linkFound.getValue1();
				List<DataLink> oldLinks = linkFound.getValue2();
				if (!oldNames.contains(newName))
					oldNames.add(newName);
				boolean found = false;
				for (DataLink oldLink : oldLinks) {
					if (oldLink.isSame(newLink)) {
						found = true;
						oldLink.merge(newLink);
						break;
					}
				}
				if (!found)
					oldLinks.add(new DataLink(newLink));
				continue;
			}
			// neither link or same people name found, we cannot assume this is the same people => add it
			currentList.add(new Pair<List<String>,List<DataLink>>(CollectionUtil.single_element_list(newName), CollectionUtil.single_element_list(new DataLink(newLink))));
		}
	}
	
	
}
