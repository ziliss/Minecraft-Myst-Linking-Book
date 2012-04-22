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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import javax.swing.event.EventListenerList;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.mystlinkingbook.RessourcesManager.PathEnd;

/**
 * Manages the Ages of the currently loaded world.
 * 
 * @author ziliss
 * @see DimensionAgesAreas
 * @see AgeArea
 * @since 0.5a
 */
public class AgesManager {
	
	/* Example of format for the agesDataFile:
	 * 
	 * allowOutOfAgeAreaDimLinking=true
	 */
	
	/**
	 * Reference to the mod instance.
	 */
	public Mod_MystLinkingBook mod_MLB;
	
	public PathEnd worldPath;
	
	public Properties props = new Properties();
	public PathEnd agesDataPath = null;
	public boolean unsavedModifications = false;
	
	public HashMap<Integer, DimensionAgesAreas> dimList = new HashMap<Integer, DimensionAgesAreas>();
	
	public DimensionAgesAreas theNether = null;
	public DimensionAgesAreas overworld = null;
	public DimensionAgesAreas theEnd = null;
	
	// public int updateCounter = 0;
	public Map<Integer, EventListenerList> listenersMap = new HashMap<Integer, EventListenerList>();
	
	public boolean allowOutOfAgeAreaDimLinking = false;
	
	public AgesManager(Mod_MystLinkingBook mod_MLB) {
		this.mod_MLB = mod_MLB;
	}
	
	protected DimensionAgesAreas getDimensionAgeAreas(int dim) {
		DimensionAgesAreas dimAgeAreas;
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
				dimAgeAreas = new DimensionAgesAreas(dim, worldPath, mod_MLB);
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
	
	public void addListener(AgesManagerListener listener, int dimension) {
		EventListenerList list = listenersMap.get(dimension);
		if (list == null) {
			list = new EventListenerList();
			listenersMap.put(dimension, list);
		}
		list.add(AgesManagerListener.class, listener);
	}
	
	public void removeListener(AgesManagerListener listener, int dimension) {
		EventListenerList list = listenersMap.get(dimension);
		if (list != null) {
			list.remove(AgesManagerListener.class, listener);
		}
	}
	
	protected void fireUpdatePerformed(int dimension) {
		EventListenerList list = listenersMap.get(dimension);
		if (list != null) {
			Object[] listeners = list.getListenerList();
			for (int i = listeners.length - 2; i >= 0; i -= 2) {
				((AgesManagerListener)listeners[i + 1]).updatePerformed(dimension);
			}
		}
	}
	
	public boolean linksToDifferentAge(int x1, int y1, int z1, int dim1, int x2, int y2, int z2, int dim2) {
		// if ("debug".equals("debug")) return true;
		DimensionAgesAreas destDimAgeAreas = getDimensionAgeAreas(dim2);
		if (destDimAgeAreas.readyAgeAreas.isEmpty()) return dim1 != dim2; // Destination dimension is an Age
		if (dim1 == dim2) {
			for (AgeArea ageArea : destDimAgeAreas.readyAgeAreas.values()) {
				if (ageArea.isInAge(x1, y1, z1) && ageArea.isInAge(x2, y2, z2)) return false; // source and destination are in the same Age area.
			}
		}
		if (destDimAgeAreas.getFirstReadyAgeAreaContaining(x2, y2, z2) != null) return true; // You are outside of an Age are and want to link inside one.
		// Here destination is outside an Age area:
		return destDimAgeAreas.allowOutOfAgeAreaDimLinking == null ? allowOutOfAgeAreaDimLinking : destDimAgeAreas.allowOutOfAgeAreaDimLinking;
	}
	
	public AgeArea getAgeAreaWithId(int dim, int id) {
		return getDimensionAgeAreas(dim).getAgeAreaWithId(id);
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
	
	public void setAllowOutOfAgeAreaDimLinking(boolean allow) {
		unsavedModifications |= updatePropsWith(props, "allowOutOfAgeAreaDimLinking", allow ? Boolean.TRUE.toString() : null);
	}
	
	public AgeAreasList getAgesAreasList(EntityPlayer player, int dim) {
		return getDimensionAgeAreas(dim).getAgesAreasList(player);
	}
	
	public AgeAreasList getAgesAreasListWithEdition(EntityPlayer player, int dim) {
		return getDimensionAgeAreas(dim).getAgesAreasListWithEdition(player);
	}
	
	public boolean endEdition(EntityPlayer player, AgeAreasList mods) {
		boolean modified = getDimensionAgeAreas(mods.dimension).endEdition(player, mods);
		if (modified) {
			try {
				save();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			fireUpdatePerformed(mods.dimension);
		}
		return modified;
	}
	
	public void startNewWorld(PathEnd worldFolder) throws IOException {
		reset();
		worldPath = worldFolder.copyFlatten();
		agesDataPath = new PathEnd(worldPath, "mystlinkingbook/agesDatas.properties");
		load();
	}
	
	public void reset() {
		listenersMap.clear();
		props.clear();
		
		dimList.clear();
		theNether = null;
		overworld = null;
		theEnd = null;
		
		allowOutOfAgeAreaDimLinking = false;
		
		unsavedModifications = false;
	}
	
	public void load() throws IOException {
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
			key = (String)entry.getKey();
			value = (String)entry.getValue();
			
			sc = new Scanner(key);
			
			key = sc.next();
			if (key.equals("allowOutOfAgeAreaDimLinking")) {
				allowOutOfAgeAreaDimLinking = Boolean.parseBoolean(value);
			}
		}
		for (Integer dimension : listenersMap.keySet()) {
			fireUpdatePerformed(dimension);
		}
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
				props.setSorted(true);
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
				props.setSorted(false);
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
		for (DimensionAgesAreas dimensionAges : dimList.values()) {
			dimensionAges.save();
		}
	}
	
	/** Returns true if the props has been modified. (And thus needs saving) */
	public static boolean updatePropsWith(Properties props, String key, String value) {
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
}