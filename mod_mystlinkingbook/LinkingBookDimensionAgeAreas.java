package net.minecraft.src;

import java.util.HashMap;

/**
 * Stores all the Age areas defined in a dimension.<br>
 * This class is used internally by {@code LinkingBookAgesManager}, and should not be used or accessed otherwise.
 * 
 * @author ziliss
 * @see LinkingBookAgesManager
 * @since 0.5a
 */
public class LinkingBookDimensionAgeAreas {
	
	public int dimension;
	
	public Boolean allowOutOfAgeAreasDimLinking = null; // Three possible values: true, false or inherited if equals to null.
	
	public int lastUsedAgeAreaID = 0;
	
	public HashMap<Integer, LinkingBookAgeArea> allAgeAreas = new HashMap<Integer, LinkingBookAgeArea>();
	
	public HashMap<Integer, LinkingBookAgeArea> readyAgeAreas = new HashMap<Integer, LinkingBookAgeArea>();
	public HashMap<Integer, LinkingBookAgeArea> disabledAgeAreas = new HashMap<Integer, LinkingBookAgeArea>();
	public HashMap<Integer, LinkingBookAgeArea> invalidAgeAreas = new HashMap<Integer, LinkingBookAgeArea>();
	
	public LinkingBookDimensionAgeAreas(int dim) {
		this.dimension = dim;
	}
	
	public LinkingBookAgeArea getOrCreateAgeArea(int ageAreaID) {
		LinkingBookAgeArea age = allAgeAreas.get(ageAreaID);
		if (age == null) {
			age = new LinkingBookAgeArea(dimension, ageAreaID);
			allAgeAreas.put(ageAreaID, age);
		}
		return age;
	}
	
	public LinkingBookAgeArea createAgeArea() {
		do {
			lastUsedAgeAreaID++;
		} while (allAgeAreas.containsKey(lastUsedAgeAreaID));
		LinkingBookAgeArea age = new LinkingBookAgeArea(dimension, lastUsedAgeAreaID);
		allAgeAreas.put(lastUsedAgeAreaID, age);
		updatedAgeArea(age);
		return age;
	}
	
	public void removeAgeArea(LinkingBookAgeArea ageArea) {
		Integer id = ageArea.id;
		allAgeAreas.remove(id);
		invalidAgeAreas.remove(id);
		disabledAgeAreas.remove(id);
		readyAgeAreas.remove(id);
	}
	
	public LinkingBookAgeArea getFirstReadyAgeAreaContaining(int x, int y, int z) {
		for (LinkingBookAgeArea ageArea : readyAgeAreas.values()) {
			if (ageArea.isInAge(x, y, z)) return ageArea;
		}
		return null;
	}
	
	public void updatedAgeArea(LinkingBookAgeArea ageArea) {
		Integer id = ageArea.id;
		invalidAgeAreas.remove(id);
		disabledAgeAreas.remove(id);
		readyAgeAreas.remove(id);
		
		if (!ageArea.isValid()) {
			invalidAgeAreas.put(id, ageArea);
		}
		else if (ageArea.disabled) {
			disabledAgeAreas.put(id, ageArea);
		}
		else {
			readyAgeAreas.put(id, ageArea);
		}
	}
	
	public void updatedAllAgeArea() {
		for (LinkingBookAgeArea ageArea : allAgeAreas.values()) {
			updatedAgeArea(ageArea);
		}
	}
}
