package net.minecraft.src.mystlinkingbook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 
 * @author ziliss
 * @since 0.8b
 */
public class AgeAreasList {
	
	public final int dimension;
	protected final ArrayList<AgeArea> ageAreas;
	
	protected final ArrayList<Integer> agesAreasOldIds = new ArrayList<Integer>();
	protected final ArrayList<Integer> removedAgesAreasIds = new ArrayList<Integer>();
	
	public final ArrayList<AgeArea> displayAgesAreas = new ArrayList<AgeArea>();
	
	public final boolean canEdit;
	public final String playerEditingName; // Can be null
	
	public boolean cancel = false;
	
	protected AgeAreasList(ArrayList<AgeArea> ageAreas, int dimension, boolean canEdit, String playerEditingName) {
		this.dimension = dimension;
		this.ageAreas = ageAreas;
		this.canEdit = canEdit;
		this.playerEditingName = playerEditingName;
		
		if (!canEdit) {
			cancel = true;
		}
		
		for (AgeArea ageArea : ageAreas) {
			if (ageArea == null) {
				agesAreasOldIds.add(null);
			}
			else {
				agesAreasOldIds.add(ageArea.id);
				displayAgesAreas.add(ageArea);
			}
		}
	}
	
	public int addNewDisplayedAgeArea() {
		AgeArea ageArea = new AgeArea(dimension, ageAreas.size());
		ageAreas.add(ageArea);
		agesAreasOldIds.add(-1);
		displayAgesAreas.add(ageArea);
		return displayAgesAreas.size() - 1;
	}
	
	public void removeDisplayedAgeArea(int index) {
		AgeArea ageArea = displayAgesAreas.remove(index);
		ageAreas.set(ageArea.id, null);
		int oldId = agesAreasOldIds.set(ageArea.id, null);
		if (oldId != -1) {
			removedAgesAreasIds.add(oldId);
		}
	}
	
	public void moveDisplayedAgeArea(int index, int delta) {
		if (index + delta < 0) {
			delta = -index;
		}
		else if (index + delta > displayAgesAreas.size() - 1) {
			delta = displayAgesAreas.size() - 1 - index;
		}
		if (delta == 0) return;
		
		// Ensure there is an empty slot at the end. (Makes moving objects easier):
		if (ageAreas.get(ageAreas.size() - 1) != null) {
			ageAreas.add(null);
			agesAreasOldIds.add(null);
		}
		
		// Remove current:
		AgeArea ageArea = ageAreas.set(displayAgesAreas.remove(index).id, null);
		int oldId = agesAreasOldIds.set(ageArea.id, null);
		
		// Move until we pass a number delta of AgesArea:
		int destIsBefore; // New destination for the AgeArea. During research, destination is between (destIsBefore) and (destIsBefore-1).
		if (index + delta == 0) {
			destIsBefore = 0;
		}
		else if (index + delta == displayAgesAreas.size() - 1) {
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
			int tempOldId = agesAreasOldIds.set(i, null);
			while (tempAgeArea != null) {
				tempAgeArea.id++;
				tempAgeArea = ageAreas.set(i, tempAgeArea);
				tempOldId = agesAreasOldIds.set(i, tempOldId);
			}
		}
		
		// Put the AgeArea in place:
		ageAreas.set(destIsBefore, ageArea);
		agesAreasOldIds.set(ageArea.id, oldId);
		displayAgesAreas.add(index + delta, ageArea);
	}
	
	public List<AgeArea> getIntersectingAgesAreasForDisplayedAgeArea(AgeArea ageArea) {
		if (ageArea.isValid()) {
			List<AgeArea> intersectings = new ArrayList<AgeArea>();
			for (AgeArea other : displayAgesAreas) {
				if (ageArea.dimension == other.dimension && ageArea.id != other.id && ageArea.intersects(other)) {
					intersectings.add(other);
				}
			}
			return intersectings;
		}
		else return Collections.<AgeArea> emptyList();
	}
}
