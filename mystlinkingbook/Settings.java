package net.minecraft.src.mystlinkingbook;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


/**
 * Load and provide quick access to the settings.
 * 
 * @author ziliss
 * @since 0.8b
 */
public class Settings {
	
	protected ResourcePath playerPropsPath;
	protected ResourcePath worldPropsPath;
	
	protected Properties defaultProps = new Properties();
	protected Properties playerProps = new Properties(defaultProps);
	protected Properties worldProps = new Properties(playerProps);
	
	public boolean loaded = false;
	
	public boolean allowWorldAssets = true;
	public boolean noDestinationPreloading = false;
	public boolean showLoadingScreens = false;
	public boolean logImportantWorldChanges = false;
	
	// TODO: add logImportantWorldChanges ?
	protected String[] keysSkippedInWorldFolders = new String[] { "allowWorldAssets" };
	
	public Settings(ResourcePath playerPropsPath, ResourcePath worldPropsPath) {
		this.playerPropsPath = playerPropsPath;
		this.worldPropsPath = worldPropsPath;
		
		defaultProps.setProperty("allowWorldAssets", Boolean.toString(allowWorldAssets));
		defaultProps.setProperty("noDestinationPreloading", Boolean.toString(noDestinationPreloading));
		defaultProps.setProperty("showLoadingScreens", Boolean.toString(showLoadingScreens));
		defaultProps.setProperty("logImportantWorldChanges", Boolean.toString(logImportantWorldChanges));
	}
	
	public void load() {
		if (loaded) {
			unload();
		}
		loadProperties(playerProps, playerPropsPath);
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
	
	protected void loadProperties(Properties props, ResourcePath path) {
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
		playerProps.clear();
		worldProps.clear();
		loaded = false;
	}
}
