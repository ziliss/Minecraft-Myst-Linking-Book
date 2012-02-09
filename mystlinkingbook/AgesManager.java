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

/**
 * Manages the Ages of the currently loaded world.
 * 
 * @author ziliss
 * @see DimensionAgeAreas
 * @see AgeArea
 * @since 0.5a
 */
public class AgesManager {
	
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
	
	public File worldFolder;
	
	public Properties props = new Properties();
	public File agesDataFile = null;
	public boolean unsavedModifications = false;
	
	public HashMap<Integer, DimensionAgeAreas> dimList = new HashMap<Integer, DimensionAgeAreas>();
	
	public DimensionAgeAreas theNether = null;
	public DimensionAgeAreas overworld = null;
	public DimensionAgeAreas theEnd = null;
	
	public boolean allowOutOfAgeAreasDimLinking = true;
	
	public AgesManager() {
	}
	
	public void changeWorld(File worldFolder) {
		this.worldFolder = worldFolder;
		// TODO: change the name of the file ?
		this.agesDataFile = new File(worldFolder, "mystlinkingbook/agesDatas.props");
		loadAges();
	}
	
	DimensionAgeAreas getDimensionAgeAreas(int dim) {
		DimensionAgeAreas dimAgeAreas;
		// For faster access:
		switch (dim) {
			case -1:
				dimAgeAreas = theNether;
			case 0:
				dimAgeAreas = overworld;
			case 1:
				dimAgeAreas = theEnd;
			default:
				dimAgeAreas = dimList.get(dim);
		}
		if (dimAgeAreas == null) {
			dimAgeAreas = new DimensionAgeAreas(dim, worldFolder);
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
	
	public boolean linksToDifferentAge(int x1, int y1, int z1, int dim1, int x2, int y2, int z2, int dim2) {
		// TODO: remove this to activate Age area:
		if ("demo".equals("demo")) return true;
		if (dim1 != dim2) return true;
		DimensionAgeAreas dimAgeAreas = getDimensionAgeAreas(dim1);
		if (dimAgeAreas == null || dimAgeAreas.readyAgeAreas.isEmpty()) return false;
		for (AgeArea ageArea : dimAgeAreas.readyAgeAreas.values()) {
			if (ageArea.isInAge(x1, y1, z1) && ageArea.isInAge(x2, y2, z2)) return false;
		}
		return dimAgeAreas.allowOutOfAgeAreasDimLinking == null ? allowOutOfAgeAreasDimLinking : dimAgeAreas.allowOutOfAgeAreasDimLinking;
	}
	
	public AgeArea getFirstReadyAgeContaining(int x, int y, int z, int dim) {
		DimensionAgeAreas dimAgeAreas = getDimensionAgeAreas(dim);
		if (dimAgeAreas == null) return null;
		else return dimAgeAreas.getFirstReadyAgeAreaContaining(x, y, z);
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
		getDimensionAgeAreas(dim).updatedDimension();
	}
	
	public void updatedAgeArea(AgeArea ageArea) {
		getDimensionAgeAreas(ageArea.dimension).updatedAgeArea(ageArea);
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
				
				key = sc.next();
				if (key.equalsIgnoreCase("allowOutOfAgeAreasDimLinking")) {
					allowOutOfAgeAreasDimLinking = Boolean.parseBoolean(value);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
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
		
		if (theNether != null) {
			theNether.saveAgeAreas();
		}
		if (overworld != null) {
			overworld.saveAgeAreas();
		}
		if (theEnd != null) {
			theEnd.saveAgeAreas();
		}
		for (DimensionAgeAreas dimensionAges : dimList.values()) {
			dimensionAges.saveAgeAreas();
		}
	}
}