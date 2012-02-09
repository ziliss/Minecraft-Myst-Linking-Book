package net.minecraft.src.mystlinkingbook;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

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
	public File dimAgesDatasFile;
	public boolean unsavedModifications;
	
	public Boolean allowOutOfAgeAreasDimLinking = null; // Three possible values: true, false or inherited if equals to null.
	
	public int lastUsedAgeAreaID;
	
	public HashMap<Integer, AgeArea> allAgeAreas = new HashMap<Integer, AgeArea>();
	
	public HashMap<Integer, AgeArea> readyAgeAreas = new HashMap<Integer, AgeArea>();
	public HashMap<Integer, AgeArea> disabledAgeAreas = new HashMap<Integer, AgeArea>();
	public HashMap<Integer, AgeArea> invalidAgeAreas = new HashMap<Integer, AgeArea>();
	
	public static Pattern agePattern = Pattern.compile("^AGE(-?\\d+)\\.", Pattern.CASE_INSENSITIVE);
	
	public DimensionAgeAreas(int dim, File worldFolder) {
		this.dimension = dim;
		this.dimAgesDatasFile = new File(worldFolder, "DIM" + dim + "/mystlinkingbook/agesDatas.props");
		
		loadAgeAreas();
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
	
	public void updatedDimension() {
		String key = "dim" + dimension + ".allowOutOfAgeAreasDimLinking";
		String value = allowOutOfAgeAreasDimLinking == null ? null : Boolean.toString(allowOutOfAgeAreasDimLinking);
		if (!props.getProperty(key).equals(value)) {
			if (value == null) {
				props.remove(key);
			}
			else {
				props.setProperty(key, value);
			}
			unsavedModifications = true;
		}
	}
	
	public void updatedAgeArea(AgeArea ageArea) {
		reSortAgeArea(ageArea);
		String baseKey = "dim" + ageArea.dimension + ".age" + ageArea.id + '.';
		
		//@formatter:off
		String[][] entries = new String[][] {
			new String[] { baseKey + "name", ageArea.name },
			new String[] { baseKey + "pos1", ageArea.pos1Set ? ageArea.pos1X + " " + ageArea.pos1Y + " " + ageArea.pos1Z : null },
			new String[] { baseKey + "pos2", ageArea.pos2Set ? ageArea.pos2X + " " + ageArea.pos2Y + " " + ageArea.pos2Z : null },
			new String[] { baseKey + "disabled", ageArea.disabled ? Boolean.TRUE.toString() : null }
		};
		//@formatter:on
		
		String key, value;
		for (String[] entry : entries) {
			key = entry[0];
			value = entry[1];
			if (!props.getProperty(key).equals(value)) {
				if (value == null) {
					props.remove(key);
				}
				else {
					props.setProperty(key, value);
				}
				unsavedModifications = true;
			}
		}
	}
	
	public void loadAgeAreas() {
		unsavedModifications = false;
		
		props.clear();
		
		allAgeAreas.clear();
		readyAgeAreas.clear();
		disabledAgeAreas.clear();
		invalidAgeAreas.clear();
		
		allowOutOfAgeAreasDimLinking = false;
		lastUsedAgeAreaID = 0;
		
		if (dimAgesDatasFile.exists()) {
			FileInputStream in = null;
			try {
				in = new FileInputStream(dimAgesDatasFile);
				props.load(new BufferedInputStream(in));
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
				return;
			}
			catch (IOException e) {
				e.printStackTrace();
				return;
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
			try {
				key = ((String)entry.getKey()).toUpperCase();
				value = (String)entry.getValue();
				
				sc = new Scanner(key);
				MatchResult result;
				
				if (sc.findInLine(agePattern) != null) {
					result = sc.match();
					int ageAreaID = Integer.parseInt(result.group(1));
					key = sc.next();
					
					AgeArea ageArea = getOrCreateAgeArea(ageAreaID);
					
					if (key.equalsIgnoreCase("name")) {
						ageArea.name = value;
					}
					else if (key.equalsIgnoreCase("pos1")) {
						ageArea.setPos1(value);
					}
					else if (key.equalsIgnoreCase("pos2")) {
						ageArea.setPos2(value);
					}
					else if (key.equalsIgnoreCase("disabled")) {
						ageArea.disabled = Boolean.parseBoolean(value);
					}
				}
				else {
					key = sc.next();
					if (key.equalsIgnoreCase("allowOutOfAgeAreasDimLinking")) {
						if (value.equalsIgnoreCase("true")) {
							allowOutOfAgeAreasDimLinking = true;
						}
						else if (value.equalsIgnoreCase("false")) {
							allowOutOfAgeAreasDimLinking = false;
						}
						else {
							allowOutOfAgeAreasDimLinking = null;
						}
						
					}
				}
				
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		for (AgeArea ageArea : allAgeAreas.values()) {
			reSortAgeArea(ageArea);
		}
	}
	
	public void saveAgeAreas() {
		if (unsavedModifications) {
			FileOutputStream out = null;
			try {
				if (!dimAgesDatasFile.exists()) {
					dimAgesDatasFile.getParentFile().mkdirs();
					dimAgesDatasFile.createNewFile();
				}
				out = new FileOutputStream(dimAgesDatasFile);
				props.store(new BufferedOutputStream(out), "Myst Linking Book mod: Age areas datas for a dimension");
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
				return;
			}
			catch (IOException e) {
				e.printStackTrace();
				return;
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
}
