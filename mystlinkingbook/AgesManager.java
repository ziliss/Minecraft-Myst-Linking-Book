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
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Scanner;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.mystlinkingbook.RessourcesManager.PathEnd;

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
	 */
	
	public Settings settings;
	
	public PathEnd worldPath;
	
	public Properties props = new Properties();
	public PathEnd agesDataPath = null;
	public boolean unsavedModifications = false;
	
	public HashMap<Integer, DimensionAgeAreas> dimList = new HashMap<Integer, DimensionAgeAreas>();
	
	public DimensionAgeAreas theNether = null;
	public DimensionAgeAreas overworld = null;
	public DimensionAgeAreas theEnd = null;
	
	public int updateCounter = 0;
	
	public boolean allowOutOfAgeAreasDimLinking = false;
	
	public AgesManager(Settings settings, PathEnd worldPath) {
		this.settings = settings;
		this.worldPath = worldPath;
		
		agesDataPath = new PathEnd(worldPath, "mystlinkingbook/agesDatas.properties");
	}
	
	DimensionAgeAreas getDimensionAgeAreas(int dim) {
		DimensionAgeAreas dimAgeAreas;
		// For faster access:
		switch (dim) {
			case -1:
				dimAgeAreas = theNether;
				break;
			case 0:
				dimAgeAreas = overworld;
				break;
			case 1:
				dimAgeAreas = theEnd;
				break;
			default:
				dimAgeAreas = dimList.get(dim);
		}
		if (dimAgeAreas == null) {
			try {
				dimAgeAreas = new DimensionAgeAreas(dim, worldPath);
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
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
			}
			
		}
		return dimAgeAreas;
	}
	
	public boolean linksToDifferentAge(int x1, int y1, int z1, int dim1, int x2, int y2, int z2, int dim2) {
		// TODO: remove this to activate Age area:
		// if ("demo".equals("demo")) return true;
		DimensionAgeAreas destDimAgeAreas = getDimensionAgeAreas(dim2);
		if (destDimAgeAreas.readyAgeAreas.isEmpty()) return dim1 != dim2; // Destination dimension is an Age
		if (dim1 == dim2) {
			for (AgeArea ageArea : destDimAgeAreas.readyAgeAreas.values()) {
				if (ageArea.isInAge(x1, y1, z1) && ageArea.isInAge(x2, y2, z2)) return false; // source and destination are in the same Age area.
			}
		}
		if (destDimAgeAreas.getFirstReadyAgeAreaContaining(x2, y2, z2) != null) return true; // You are outside of an Age are and want to link inside one.
		// Here destination is outside an Age area:
		return destDimAgeAreas.allowOutOfAgeAreasDimLinking == null ? allowOutOfAgeAreasDimLinking : destDimAgeAreas.allowOutOfAgeAreasDimLinking;
	}
	
	public AgeArea getFirstReadyAgeAreaContaining(int x, int y, int z, int dim) {
		return getDimensionAgeAreas(dim).getFirstReadyAgeAreaContaining(x, y, z);
	}
	
	public List<AgeArea> getAllReadyAgeAreaContaining(int x, int y, int z, int dim) {
		return addToListAllReadyAgeAreaContaining(x, y, z, dim, new ArrayList<AgeArea>());
	}
	
	public List<AgeArea> addToListAllReadyAgeAreaContaining(int x, int y, int z, int dim, List<AgeArea> list) {
		return getDimensionAgeAreas(dim).addToListAllReadyAgeAreaContaining(x, y, z, list);
	}
	
	public void updated() {
		String key = "allowOutOfAgeAreasDimLinking";
		String value = allowOutOfAgeAreasDimLinking ? Boolean.TRUE.toString() : null;
		String oldValue = props.getProperty(key);
		if (oldValue == null || !oldValue.equals(value)) {
			if (value == null) {
				props.remove(key);
			}
			else {
				props.setProperty(key, value);
			}
			unsavedModifications = true;
			updateCounter++;
		}
	}
	
	public void updatedDimension(int dim) {
		if (getDimensionAgeAreas(dim).updatedDimension()) {
			updateCounter++;
		}
	}
	
	public AgeAreasModifications startEdition(EntityPlayer player, int dim) {
		return getDimensionAgeAreas(dim).startEdition(player);
	}
	
	public boolean endEdition(EntityPlayer player, AgeAreasModifications mods) {
		boolean modified = getDimensionAgeAreas(mods.dimension).endEdition(player, mods);
		if (modified) {
			updateCounter++;
			try {
				save();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		return modified;
	}
	
	public void load() throws IOException {
		props.clear();
		
		dimList.clear();
		theNether = null;
		overworld = null;
		theEnd = null;
		unsavedModifications = false;
		
		if (agesDataPath.exists()) {
			FileInputStream in = null;
			try {
				in = new FileInputStream(agesDataPath.toString());
				props.load(new BufferedInputStream(in));
				System.out.println("Loaded file: " + agesDataPath.toString());
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
			key = ((String)entry.getKey());
			value = (String)entry.getValue();
			
			sc = new Scanner(key);
			
			key = sc.next();
			if (key.equals("allowOutOfAgeAreasDimLinking")) {
				allowOutOfAgeAreasDimLinking = Boolean.parseBoolean(value);
			}
		}
		updateCounter++;
	}
	
	public void save() throws IOException {
		if (!worldPath.exists()) throw new RuntimeException("World folder does not exists: " + worldPath.toString());
		if (unsavedModifications) {
			FileOutputStream out = null;
			try {
				File dimAgesDatasFile = new File(agesDataPath.toString());
				if (!dimAgesDatasFile.exists()) {
					dimAgesDatasFile.getParentFile().mkdirs();
					dimAgesDatasFile.createNewFile();
				}
				out = new FileOutputStream(dimAgesDatasFile);
				props.store(new BufferedOutputStream(out), "Myst Linking Book mod: Ages datas");
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
			unsavedModifications = false;
		}
		
		if (theNether != null) {
			theNether.save();
		}
		if (overworld != null) {
			overworld.save();
		}
		if (theEnd != null) {
			theEnd.save();
		}
		for (DimensionAgeAreas dimensionAges : dimList.values()) {
			dimensionAges.save();
		}
	}
}