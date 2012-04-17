package net.minecraft.src.mystlinkingbook;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.mystlinkingbook.RessourcesManager.PathEnd;

/**
 * Stores all the Age areas defined in a dimension.<br>
 * This class is used internally by {@code LinkingBookAgesManager}, and should not be used or accessed otherwise.
 * 
 * @author ziliss
 * @see AgesManager
 * @since 0.5a
 */
public class DimensionAgeAreas {
	
	/* Example of format for the agesDataFile:
	 * 
	 * allowOutOfAgeAreasDimLinking=true
	 * allowOutOfAgeAreasDimLinking=inherited
	 * 
	 * age1.name=My own Age
	 * age1.pos1= -12 34 65
	 * age1.pos2=12 90 7
	 * age1.disabled=true
	 */
	
	public int dimension;
	
	public Properties props = new Properties();
	public PathEnd dimAgesDatasPath;
	
	public EntityPlayer playerEditing = null;
	public boolean unsavedModifications;
	
	public Boolean allowOutOfAgeAreasDimLinking = null; // Three possible values: true, false or inherited if equals to null.
	
	public ArrayList<AgeArea> allAgeAreas = new ArrayList<AgeArea>();
	
	public HashMap<Integer, AgeArea> readyAgeAreas = new HashMap<Integer, AgeArea>();
	public HashMap<Integer, AgeArea> disabledAgeAreas = new HashMap<Integer, AgeArea>();
	public HashMap<Integer, AgeArea> invalidAgeAreas = new HashMap<Integer, AgeArea>();
	
	public static Pattern agePattern = Pattern.compile("^age(-?\\d+)\\.");
	
	public DimensionAgeAreas(int dim, PathEnd worldPath) throws IOException {
		this.dimension = dim;
		String dimFolderName = dim == 0 ? "region" : "DIM" + dim;
		this.dimAgesDatasPath = new PathEnd(worldPath, dimFolderName + "/mystlinkingbook/dimAgesDatas.properties").copyFlatten();
		
		load();
	}
	
	public void removeAgeArea(int id) {
		allAgeAreas.set(id, null);
		invalidAgeAreas.remove(id);
		disabledAgeAreas.remove(id);
		readyAgeAreas.remove(id);
		
		removeAgeAreaFromProps(id);
	}
	
	public void removeAgeAreaFromProps(int id) {
		String baseKey = "age" + id + '.';
		
		//@formatter:off
		String[][] entries = new String[][] {
			new String[] { baseKey + "name", null },
			new String[] { baseKey + "pos1", null },
			new String[] { baseKey + "pos2", null },
			new String[] { baseKey + "disabled", null }
		};
		//@formatter:on
		boolean modified = false;
		for (String[] entry : entries) {
			modified &= updatePropsWith(entry[0], entry[1]);
		}
		if (modified) {
			unsavedModifications = true;
		}
	}
	
	public AgeArea getFirstReadyAgeAreaContaining(int x, int y, int z) {
		for (AgeArea ageArea : readyAgeAreas.values()) {
			if (ageArea.isInAge(x, y, z)) return ageArea;
		}
		return null;
	}
	
	public List<AgeArea> addToListAllReadyAgeAreaContaining(int x, int y, int z, List<AgeArea> list) {
		for (AgeArea ageArea : readyAgeAreas.values()) {
			if (ageArea.isInAge(x, y, z)) {
				list.add(ageArea);
			}
		}
		return list;
	}
	
	public void reSortAgeArea(AgeArea ageArea) {
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
	
	public boolean updatedDimension() {
		String key = "allowOutOfAgeAreasDimLinking";
		String value = allowOutOfAgeAreasDimLinking == null ? null : Boolean.toString(allowOutOfAgeAreasDimLinking);
		if (!props.getProperty(key).equals(value)) {
			if (value == null) {
				props.remove(key);
			}
			else {
				props.setProperty(key, value);
			}
			unsavedModifications = true;
			return true;
		}
		return false;
	}
	
	public boolean updatedAgeArea(AgeArea ageArea) {
		reSortAgeArea(ageArea);
		String baseKey = "age" + ageArea.id + '.';
		
		//@formatter:off
		String[][] entries = new String[][] {
			new String[] { baseKey + "name", ageArea.name },
			new String[] { baseKey + "pos1", ageArea.pos1Set ? ageArea.pos1X + " " + ageArea.pos1Y + " " + ageArea.pos1Z : null },
			new String[] { baseKey + "pos2", ageArea.pos2Set ? ageArea.pos2X + " " + ageArea.pos2Y + " " + ageArea.pos2Z : null },
			new String[] { baseKey + "disabled", ageArea.disabled ? Boolean.TRUE.toString() : null }
		};
		//@formatter:on
		boolean modified = false;
		for (String[] entry : entries) {
			modified &= updatePropsWith(entry[0], entry[1]);
		}
		if (modified) {
			unsavedModifications = true;
		}
		return modified;
	}
	
	public boolean updatePropsWith(String key, String value) {
		String oldValue = props.getProperty(key);
		if (oldValue == null || oldValue != value || !oldValue.equals(value)) {
			if (value == null) {
				props.remove(key);
			}
			else {
				props.setProperty(key, value);
			}
			return true;
		}
		return false;
	}
	
	public AgeAreasModifications startEdition(EntityPlayer player) {
		ArrayList<AgeArea> ageAreas = new ArrayList<AgeArea>();
		for (AgeArea ageArea : allAgeAreas) {
			ageAreas.add(ageArea == null ? null : ageArea.clone());
		}
		
		if (playerEditing == null || playerEditing.equals(player)) {
			playerEditing = player;
			return new AgeAreasModifications(ageAreas, dimension);
		}
		else return new AgeAreasModifications(ageAreas, dimension, playerEditing.username);
	}
	
	public boolean endEdition(EntityPlayer player, AgeAreasModifications mods) {
		if (playerEditing == null || !playerEditing.equals(player) || !mods.canEdit) return false;
		if (mods.ageAreas.size() != mods.ageAreasOldIds.size()) throw new IllegalArgumentException();
		
		boolean isCancelled = mods.cancel;
		boolean hasRemovedAges = false;
		boolean hasIdModifiedAges = false;
		boolean hasModifiedAges = false;
		boolean hasNewAges = false;
		StringBuilder logRemovedAges = null;
		StringBuilder logIdModifiedAges = null;
		StringBuilder logModifiedAges = null;
		StringBuilder logNewAges = null;
		
		StringBuilder log = new StringBuilder("Player ").append(player.username);
		
		if (!isCancelled) {
			hasRemovedAges = !mods.removedAgesAreaIds.isEmpty();
			if (hasRemovedAges) {
				logRemovedAges = new StringBuilder(" Removing Age areas: ");
				for (int removedId : mods.removedAgesAreaIds) {
					logRemovedAges.append(allAgeAreas.get(removedId).name + " (" + removedId + "), ");
					removeAgeArea(removedId);
				}
				logRemovedAges.setLength(logRemovedAges.length() - 2);
			}
			
			logIdModifiedAges = new StringBuilder(" Changing Age area Ids: ");
			ArrayList<AgeArea> newAgeAreas = new ArrayList<AgeArea>();
			HashMap<Integer, Integer> oldToNewIds = new HashMap<Integer, Integer>();
			for (int i = 0; i < mods.ageAreas.size(); i++) {
				if (mods.ageAreas.get(i) != null) {
					int oldId = mods.ageAreasOldIds.get(i);
					if (oldId == -1) {
						newAgeAreas.add(mods.ageAreas.set(i, null));
						hasNewAges = true;
					}
					else if (oldId != i) {
						oldToNewIds.put(oldId, i);
						logIdModifiedAges.append(allAgeAreas.get(oldId).name + " (" + oldId + " => " + i + "), ");
						hasIdModifiedAges = true;
					}
				}
			}
			if (hasIdModifiedAges) {
				logIdModifiedAges.setLength(logIdModifiedAges.length() - 2);
				changeIds(oldToNewIds);
			}
			
			logModifiedAges = new StringBuilder(" Modified Age area datas:");
			AgeArea ageArea;
			for (AgeArea ageMod : mods.ageAreas) {
				if (ageMod != null) {
					ageArea = allAgeAreas.get(ageMod.id);
					if (!ageMod.sameData(ageArea)) {
						ageMod.copyDatasTo(ageArea);
						updatedAgeArea(ageArea);
						logModifiedAges.append("\n  " + ageArea.name + " (" + ageArea.id + "), pos1: " + ageArea.getPos1() + ", pos2: " + ageArea.getPos2());
						if (ageArea.disabled) {
							logModifiedAges.append(", disabled");
						}
						hasModifiedAges = true;
					}
				}
			}
			
			if (hasNewAges) {
				logNewAges = new StringBuilder(" New Age area:");
				
				for (AgeArea ageMod : newAgeAreas) {
					ageArea = ageMod.clone();
					ageArea.dimension = dimension; // Just in case
					allAgeAreas.ensureCapacity(ageArea.id + 1);
					ensureSize(allAgeAreas, ageArea.id + 1, null);
					allAgeAreas.set(ageArea.id, ageArea);
					updatedAgeArea(ageArea);
					
					logNewAges.append("\n  " + ageArea.name + " (" + ageArea.id + "), pos1: " + ageArea.getPos1() + ", pos2: " + ageArea.getPos2());
					if (ageArea.disabled) {
						logNewAges.append(", disabled");
					}
				}
			}
		}
		
		boolean modified = hasRemovedAges || hasIdModifiedAges || hasModifiedAges || hasNewAges;
		if (modified) {
			log.append(" has modified the Ages areas for this dimension (" + dimension + "):");
			if (hasRemovedAges) {
				log.append("\n").append(logRemovedAges);
			}
			if (hasIdModifiedAges) {
				log.append("\n").append(logIdModifiedAges);
			}
			if (hasModifiedAges) {
				log.append("\n").append(logModifiedAges);
			}
			if (hasNewAges) {
				log.append("\n").append(logNewAges);
			}
			unsavedModifications = true;
		}
		else {
			if (isCancelled) {
				log.append(" has finished reading the Ages areas for this dimension (" + dimension + ").");
			}
			else {
				log.append(" has finished reading the Ages areas for this dimension (" + dimension + ").");
			}
		}
		System.out.println(log);
		playerEditing = null;
		return modified;
	}
	
	public void changeIds(HashMap<Integer, Integer> oldToNewIds) {
		ArrayList<AgeArea> temp = new ArrayList<AgeArea>();
		
		int oldId, newId;
		Iterator<Entry<Integer, Integer>> iter = oldToNewIds.entrySet().iterator();
		Entry<Integer, Integer> entry;
		while (iter.hasNext()) {
			entry = iter.next();
			oldId = entry.getKey();
			newId = entry.getValue();
			
			AgeArea ageArea = allAgeAreas.set(oldId, null);
			invalidAgeAreas.remove(oldId);
			disabledAgeAreas.remove(oldId);
			readyAgeAreas.remove(oldId);
			removeAgeAreaFromProps(oldId);
			ageArea.id = newId;
			temp.add(ageArea);
		}
		
		for (AgeArea ageArea : temp) {
			ensureSize(allAgeAreas, ageArea.id + 1, null);
			allAgeAreas.set(ageArea.id, ageArea);
			updatedAgeArea(ageArea);
		}
	}
	
	public void load() throws IOException {
		unsavedModifications = false;
		
		props.clear();
		
		allAgeAreas.clear();
		readyAgeAreas.clear();
		disabledAgeAreas.clear();
		invalidAgeAreas.clear();
		
		allowOutOfAgeAreasDimLinking = false;
		
		if (dimAgesDatasPath.exists()) {
			FileInputStream in = null;
			try {
				in = new FileInputStream(dimAgesDatasPath.toString());
				props.load(new BufferedInputStream(in));
				System.out.println("Loaded file: " + dimAgesDatasPath.toString());
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			finally {
				try {
					if (in != null) {
						in.close();
					}
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		String key, value;
		Scanner sc;
		for (Entry entry : props.entrySet()) {
			key = (String)entry.getKey();
			value = (String)entry.getValue();
			
			sc = new Scanner(key);
			MatchResult result;
			
			if (sc.findInLine(agePattern) != null) {
				result = sc.match();
				int ageAreaID = Integer.parseInt(result.group(1));
				if (ageAreaID < 0) {
					System.out.println("Skiped AgeArea id " + ageAreaID + ". Id can not be negative !");
					continue;
				}
				key = sc.next();
				
				AgeArea ageArea = allAgeAreas.size() <= ageAreaID ? null : allAgeAreas.get(ageAreaID);
				if (ageArea == null) {
					ageArea = new AgeArea(dimension, ageAreaID);
					ensureSize(allAgeAreas, ageAreaID + 1, null);
					allAgeAreas.set(ageAreaID, ageArea);
				}
				
				if (key.equals("name")) {
					ageArea.name = value;
				}
				else if (key.equals("pos1")) {
					ageArea.setPos1(value);
				}
				else if (key.equals("pos2")) {
					ageArea.setPos2(value);
				}
				else if (key.equals("disabled")) {
					ageArea.disabled = Boolean.parseBoolean(value);
				}
			}
			else {
				key = sc.next();
				if (key.equals("allowOutOfAgeAreasDimLinking")) {
					if (value.equals("true")) {
						allowOutOfAgeAreasDimLinking = true;
					}
					else if (value.equals("false")) {
						allowOutOfAgeAreasDimLinking = false;
					}
					else {
						allowOutOfAgeAreasDimLinking = null;
					}
				}
			}
		}
		
		for (AgeArea ageArea : allAgeAreas) {
			if (ageArea != null) {
				reSortAgeArea(ageArea);
			}
		}
	}
	
	public void save() throws IOException {
		if (unsavedModifications) {
			FileOutputStream out = null;
			try {
				File dimAgesDatasFile = new File(dimAgesDatasPath.toString());
				if (!dimAgesDatasFile.exists()) {
					dimAgesDatasFile.getParentFile().mkdirs();
					dimAgesDatasFile.createNewFile();
				}
				out = new FileOutputStream(dimAgesDatasFile);
				props.store(new BufferedOutputStream(out), "Myst Linking Book mod: Age areas datas for a dimension");
			}
			finally {
				try {
					if (out != null) {
						out.close();
					}
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		unsavedModifications = false;
	}
	
	public static <T> void ensureSize(ArrayList<T> arrayList, int size, T filler) {
		if (arrayList.size() < size) {
			arrayList.ensureCapacity(size);
			for (int i = arrayList.size(); i < size; i++) {
				arrayList.add(filler);
			}
		}
	}
}
