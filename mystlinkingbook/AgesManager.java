package net.minecraft.src;

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
 * Manages the Ages of the currently loaded world.
 * 
 * @author ziliss
 * @see LinkingBookDimensionAgeAreas
 * @see LinkingBookAgeArea
 * @since 0.5a
 */
public class LinkingBookAgesManager {
	
	/* Example of format for the agesDataFile:
	 * 
	 * allowOutOfAgeAreasDimLinking=true
	 * dim1.allowOutOfAgeAreasDimLinking=true
	 * dim389.allowOutOfAgeAreasDimLinking=inherited
	 * 
	 * dim1.age1.name=My own Age
	 * dim1.age1.pos1= -12 34 65
	 * dim1.age1.pos2=12 90 7
	 * dim1.age1.disabled=true
	 */
	
	public Properties props = new Properties();
	
	public boolean allowOutOfAgeAreasDimLinking = true;
	
	public File agesDataFile = null;
	
	public HashMap<Integer, LinkingBookDimensionAgeAreas> dimList = new HashMap<Integer, LinkingBookDimensionAgeAreas>();
	
	public LinkingBookDimensionAgeAreas theNether = null;
	public LinkingBookDimensionAgeAreas overworld = null;
	public LinkingBookDimensionAgeAreas theEnd = null;
	
	public boolean unsavedModifications = false;
	
	public Pattern dimPattern = Pattern.compile("^DIM(-?\\d+)\\.", Pattern.CASE_INSENSITIVE);
	
	public Pattern agePattern = Pattern.compile("AGE(-?\\d+)\\.", Pattern.CASE_INSENSITIVE);
	
	public LinkingBookAgesManager() {
		
	}
	
	public void setAgesDataFile(File agesDataFile) {
		this.agesDataFile = agesDataFile;
	}
	
	public LinkingBookDimensionAgeAreas getDimensionAgeAreas(int dim) {
		// For faster access:
		switch (dim) {
			case -1:
				return theNether;
			case 0:
				return overworld;
			case 1:
				return theEnd;
			default:
				return dimList.get(dim);
		}
	}
	
	public boolean linksToDifferentAge(int x1, int y1, int z1, int dim1, int x2, int y2, int z2, int dim2) {
		if ("demo".equals("demo")) return true;
		if (dim1 != dim2) return true;
		LinkingBookDimensionAgeAreas dimAgeAreas = getDimensionAgeAreas(dim1);
		if (dimAgeAreas == null || dimAgeAreas.readyAgeAreas.isEmpty()) return false;
		for (LinkingBookAgeArea ageArea : dimAgeAreas.readyAgeAreas.values()) {
			if (ageArea.isInAge(x1, y1, z1) && ageArea.isInAge(x2, y2, z2)) return false;
		}
		return dimAgeAreas.allowOutOfAgeAreasDimLinking == null ? allowOutOfAgeAreasDimLinking : dimAgeAreas.allowOutOfAgeAreasDimLinking;
	}
	
	public LinkingBookAgeArea getFirstReadyAgeContaining(int x, int y, int z, int dim) {
		LinkingBookDimensionAgeAreas dimAgeAreas = getDimensionAgeAreas(dim);
		if (dimAgeAreas == null) return null;
		else return dimAgeAreas.getFirstReadyAgeAreaContaining(x, y, z);
	}
	
	public LinkingBookDimensionAgeAreas getOrCreateDimensionAgeAreas(int dim) {
		LinkingBookDimensionAgeAreas dimAgeAreas = getDimensionAgeAreas(dim);
		if (dimAgeAreas == null) {
			dimAgeAreas = new LinkingBookDimensionAgeAreas(dim);
			switch (dim) {
				case -1:
					theNether = dimAgeAreas;
					break;
				case 0:
					overworld = dimAgeAreas;
					break;
				case 1:
					theEnd = dimAgeAreas;
					break;
				default:
					dimList.put(dim, dimAgeAreas);
					break;
			}
			
		}
		return dimAgeAreas;
	}
	
	public void updated() {
		String key = "allowOutOfAgeAreasDimLinking";
		String value = allowOutOfAgeAreasDimLinking ? Boolean.TRUE.toString() : null;
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
	
	public void updatedDimension(int dim) {
		LinkingBookDimensionAgeAreas dimAgeAreas = getDimensionAgeAreas(dim);
		String key = "dim" + dim + ".allowOutOfAgeAreasDimLinking";
		String value = dimAgeAreas.allowOutOfAgeAreasDimLinking == null ? null : Boolean.toString(dimAgeAreas.allowOutOfAgeAreasDimLinking);
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
	
	public void updatedAgeArea(LinkingBookAgeArea ageArea) {
		getDimensionAgeAreas(ageArea.dimension).updatedAgeArea(ageArea);
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
	
	public void loadAges() {
		if (agesDataFile == null) return;
		dimList.clear();
		theNether = null;
		overworld = null;
		theEnd = null;
		unsavedModifications = false;
		
		if (agesDataFile != null && agesDataFile.exists()) {
			props.clear();
			FileInputStream in = null;
			try {
				in = new FileInputStream(agesDataFile);
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
				if (sc.findInLine(dimPattern) != null) {
					result = sc.match();
					int dim = Integer.parseInt(result.group(1));
					
					LinkingBookDimensionAgeAreas dimAgeAreas = getOrCreateDimensionAgeAreas(dim);
					
					if (sc.findInLine(agePattern) != null) {
						result = sc.match();
						int ageAreaID = Integer.parseInt(result.group(1));
						key = sc.next();
						
						LinkingBookAgeArea ageArea = dimAgeAreas.getOrCreateAgeArea(ageAreaID);
						
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
								dimAgeAreas.allowOutOfAgeAreasDimLinking = true;
							}
							else if (value.equalsIgnoreCase("false")) {
								dimAgeAreas.allowOutOfAgeAreasDimLinking = false;
							}
							else {
								dimAgeAreas.allowOutOfAgeAreasDimLinking = null;
							}
							
						}
					}
				}
				else {
					key = sc.next();
					if (key.equalsIgnoreCase("allowOutOfAgeAreasDimLinking")) {
						allowOutOfAgeAreasDimLinking = Boolean.parseBoolean(value);
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (theNether != null) {
			theNether.updatedAllAgeArea();
		}
		if (overworld != null) {
			overworld.updatedAllAgeArea();
		}
		if (theEnd != null) {
			theEnd.updatedAllAgeArea();
		}
		for (LinkingBookDimensionAgeAreas dimensionAges : dimList.values()) {
			dimensionAges.updatedAllAgeArea();
		}
	}
	
	public void saveAges() {
		if (agesDataFile != null && unsavedModifications) {
			FileOutputStream out = null;
			try {
				if (!agesDataFile.exists()) {
					agesDataFile.getParentFile().mkdirs();
					agesDataFile.createNewFile();
				}
				out = new FileOutputStream(agesDataFile);
				props.store(new BufferedOutputStream(out), "Myst Linking Book mod: Ages datas");
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