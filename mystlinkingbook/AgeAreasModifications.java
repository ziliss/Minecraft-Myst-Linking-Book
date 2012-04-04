package net.minecraft.src.mystlinkingbook;

import java.util.ArrayList;

/**
 * 
 * @author ziliss
 * @since 0.8b
 */
public class AgeAreasModifications {
	
	public final int dimension;
	final ArrayList<AgeArea> ageAreas;
	
	final ArrayList<Integer> ageAreasOldIds = new ArrayList<Integer>();
	final ArrayList<Integer> removedAgesAreaIds = new ArrayList<Integer>();
	
	public final ArrayList<AgeArea> displayAgeAreas = new ArrayList<AgeArea>();
	
	public final boolean canEdit;
	public final String playerEditingName;
	
	public boolean cancel = false;
	
	public AgeAreasModifications(ArrayList<AgeArea> ageAreas, int dimension) {
		this(ageAreas, dimension, true, null);
	}
	
	public AgeAreasModifications(ArrayList<AgeArea> ageAreas, int dimension, String playerEditingName) {
		this(ageAreas, dimension, false, playerEditingName);
	}
	
	AgeAreasModifications(ArrayList<AgeArea> ageAreas, int dimension, boolean canEdit, String playerEditingName) {
		this.dimension = dimension;
		this.ageAreas = ageAreas;
		this.canEdit = canEdit;
		this.playerEditingName = playerEditingName;
		
		for (AgeArea ageArea : ageAreas) {
			if (ageArea == null) {
				ageAreasOldIds.add(null);
			}
			else {
				ageAreasOldIds.add(ageArea.id);
				displayAgeAreas.add(ageArea);
			}
		}
	}
	
	public int addNewDisplayedAgeArea() {
		AgeArea ageArea = new AgeArea(dimension, ageAreas.size());
		ageAreas.add(ageArea);
		ageAreasOldIds.add(-1);
		displayAgeAreas.add(ageArea);
		return displayAgeAreas.size() - 1;
	}
	
	public void removeDisplayedAgeArea(int index) {
		AgeArea ageArea = displayAgeAreas.remove(index);
		ageAreas.set(ageArea.id, null);
		int oldId = ageAreasOldIds.set(ageArea.id, null);
		if (oldId != -1) {
			removedAgesAreaIds.add(oldId);
		}
	}
	
	public void moveDisplayedAgeArea(int index, int delta) {
		if (index + delta < 0) {
			delta = -index;
		}
		else if (index + delta > displayAgeAreas.size() - 1) {
			delta = displayAgeAreas.size() - 1 - index;
		}
		if (delta == 0) return;
		
		// Ensure there is an empty slot at the end. (Makes moving objects easier):
		if (ageAreas.get(ageAreas.size() - 1) != null) {
			ageAreas.add(null);
			ageAreasOldIds.add(null);
		}
		
		// Remove current:
		AgeArea ageArea = ageAreas.set(displayAgeAreas.remove(index).id, null);
		int oldId = ageAreasOldIds.set(ageArea.id, null);
		
		// Move until we pass a number delta of AgesArea:
		int destIsBefore; // New destination for the AgeArea. During research, destination is between (destIsBefore) and (destIsBefore-1).
		if (index + delta == 0) {
			destIsBefore = 0;
		}
		else if (index + delta == displayAgeAreas.size() - 1) {
			destIsBefore = ageAreas.size() - 1;
		}
		else {
			destIsBefore = ageArea.id;
			int passed = 0;
			int incr = delta > 0 ? 1 : -1;
			while (passed != delta) {
				destIsBefore += incr;
				if (ageAreas.get(destIsBefore) != null) {
					passed += incr;
				}
			}
			if (incr < 0) {
				destIsBefore++;
			}
		}
		
		// Decrease destIsBefore to the lowest available slot:
		while (destIsBefore > 0 && ageAreas.get(destIsBefore - 1) == null) {
			destIsBefore--;
		}
		
		// If slot is occupied, move all following AgeArea up:
		if (ageAreas.get(destIsBefore) != null) {
			int i = destIsBefore;
			AgeArea tempAgeArea = ageAreas.set(i, null);
			int tempOldId = ageAreasOldIds.set(i, null);
			while (tempAgeArea != null) {
				tempAgeArea.id++;
				tempAgeArea = ageAreas.set(i, tempAgeArea);
				tempOldId = ageAreasOldIds.set(i, tempOldId);
			}
		}
		
		// Put the AgeArea in place:
		ageAreas.set(destIsBefore, ageArea);
		ageAreasOldIds.set(ageArea.id, oldId);
		displayAgeAreas.add(index + delta, ageArea);
	}
}
