package net.minecraft.src.mystlinkingbook;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.minecraft.src.mystlinkingbook.RessourcesManager.PathEnd;

/**
 * 
 * @author ziliss
 * @since 0.8b
 */
public class Settings {
	
	protected PathEnd basePropsPath;
	protected PathEnd worldPropsPath;
	
	protected Properties defaultsProps = new Properties();
	protected Properties baseProps = new Properties(defaultsProps);
	protected Properties worldProps = new Properties(baseProps);
	
	public boolean loaded = false;
	
	public boolean allowWorldAssets = true;
	public boolean noDestinationPreloading = false;
	public boolean showLoadingScreens = false;
	public boolean logImportantWorldChanges = false;
	
	// TODO: add logImportantWorldChanges ?
	protected String[] keysSkippedInWorldFolders = new String[] { "allowWorldAssets" };
	
	public Settings(PathEnd basePropsPath, PathEnd worldPropsPath) {
		this.basePropsPath = basePropsPath;
		this.worldPropsPath = worldPropsPath;
		
		defaultsProps.setProperty("allowWorldAssets", Boolean.toString(allowWorldAssets));
		defaultsProps.setProperty("noDestinationPreloading", Boolean.toString(noDestinationPreloading));
		defaultsProps.setProperty("showLoadingScreens", Boolean.toString(showLoadingScreens));
		defaultsProps.setProperty("logImportantWorldChanges", Boolean.toString(logImportantWorldChanges));
	}
	
	public void load() {
		if (loaded) {
			unload();
		}
		loadProperties(baseProps, basePropsPath);
		loadProperties(worldProps, worldPropsPath);
		for (String prop : keysSkippedInWorldFolders) {
			worldProps.remove(prop);
		}
		loaded();
		loaded = true;
	}
	
	public void loaded() {
		allowWorldAssets = Boolean.parseBoolean(worldProps.getProperty("allowWorldAssets"));
		noDestinationPreloading = Boolean.parseBoolean(worldProps.getProperty("noDestinationPreloading"));
		showLoadingScreens = Boolean.parseBoolean(worldProps.getProperty("showLoadingScreens"));
		logImportantWorldChanges = Boolean.parseBoolean(worldProps.getProperty("logImportantWorldChanges"));
	}
	
	protected void loadProperties(Properties props, PathEnd path) {
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
