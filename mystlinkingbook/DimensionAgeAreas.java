package net.minecraft.src.mystlinkingbook;

import java.util.HashMap;

/**
 * Stores all the Age areas defined in a dimension.<br>
 * This class is used internally by {@code LinkingBookAgesManager}, and should not be used or accessed otherwise.
 * 
 * @author ziliss
 * @see AgesManager
 * @since 0.5a
 */
public class DimensionAgeAreas {
	
	public int dimension;
	
	public Boolean allowOutOfAgeAreasDimLinking = null; // Three possible values: true, false or inherited if equals to null.
	
	public int lastUsedAgeAreaID = 0;
	
	public HashMap<Integer, AgeArea> allAgeAreas = new HashMap<Integer, AgeArea>();
	
	public HashMap<Integer, AgeArea> readyAgeAreas = new HashMap<Integer, AgeArea>();
	public HashMap<Integer, AgeArea> disabledAgeAreas = new HashMap<Integer, AgeArea>();
	public HashMap<Integer, AgeArea> invalidAgeAreas = new HashMap<Integer, AgeArea>();
	
	public DimensionAgeAreas(int dim) {
		this.dimension = dim;
	}
	
	public AgeArea getOrCreateAgeArea(int ageAreaID) {
		AgeArea age = allAgeAreas.get(ageAreaID);
		if (age == null) {
			age = new AgeArea(dimension, ageAreaID);
			allAgeAreas.put(ageAreaID, age);
		}
		return age;
	}
	
	public AgeArea createAgeArea() {
		do {
			lastUsedAgeAreaID++;
		} while (allAgeAreas.containsKey(lastUsedAgeAreaID));
		AgeArea age = new AgeArea(dimension, lastUsedAgeAreaID);
		allAgeAreas.put(lastUsedAgeAreaID, age);
		updatedAgeArea(age);
		return age;
	}
	
	public void removeAgeArea(AgeArea ageArea) {
		Integer id = ageArea.id;
		allAgeAreas.remove(id);
		invalidAgeAreas.remove(id);
		disabledAgeAreas.remove(id);
		readyAgeAreas.remove(id);
	}
	
	public AgeArea getFirstReadyAgeAreaContaining(int x, int y, int z) {
		for (AgeArea ageArea : readyAgeAreas.values()) {
			if (ageArea.isInAge(x, y, z)) return ageArea;
		}
		return null;
	}
	
	public void updatedAgeArea(AgeArea ageArea) {
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
		for (AgeArea ageArea : allAgeAreas.values()) {
			updatedAgeArea(ageArea);
		}
	}
}
