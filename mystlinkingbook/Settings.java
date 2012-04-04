package net.minecraft.src.mystlinkingbook;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import net.minecraft.src.mystlinkingbook.RessourcesManager.PathEnd;

/**
 * 
 * @author ziliss
 * @since 0.8b
 */
public class Settings {
	
	PathEnd basePropsPath;
	PathEnd worldPropsPath;
	
	Properties defaultsProps = new Properties();
	Properties baseProps = new Properties(defaultsProps);
	Properties worldProps = new Properties(baseProps);
	
	public boolean loaded = false;
	
	public boolean allowWorldAssets = true;
	public boolean logAgeAreasModifications = false;
	
	String[] worldForbiddenKeys = new String[] { "allowWorldAssets" };
	
	public Settings(PathEnd basePropsPath, PathEnd worldPropsPath) {
		this.basePropsPath = basePropsPath;
		this.worldPropsPath = worldPropsPath;
		
		defaultsProps.setProperty("allowWorldAssets", Boolean.toString(allowWorldAssets));
		defaultsProps.setProperty("logAgeAreasModifications", Boolean.toString(logAgeAreasModifications));
	}
	
	public void load() {
		if (loaded) {
			unload();
		}
		loadProperties(baseProps, basePropsPath);
		loadProperties(worldProps, worldPropsPath);
		for (String prop : worldForbiddenKeys) {
			worldProps.remove(prop);
		}
		loaded();
		loaded = true;
	}
	
	public void loaded() {
		allowWorldAssets = Boolean.parseBoolean(worldProps.getProperty("allowWorldAssets"));
		logAgeAreasModifications = Boolean.parseBoolean(worldProps.getProperty("logAgeAreasModifications"));
	}
	
	void loadProperties(Properties props, PathEnd path) {
		if (path.exists()) {
			BufferedInputStream in = null;
			try {
				in = new BufferedInputStream(new FileInputStream(path.toString()));
				props.load(in);
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			catch (IOException e) {
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
	}
	
	public void unload() {
		baseProps.clear();
		worldProps.clear();
		loaded = false;
	}
}
