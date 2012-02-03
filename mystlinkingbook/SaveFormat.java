package net.minecraft.src.mystlinkingbook;

import java.util.List;

import net.minecraft.src.IProgressUpdate;
import net.minecraft.src.ISaveFormat;
import net.minecraft.src.ISaveHandler;
import net.minecraft.src.WorldInfo;

/**
 * Replaces the original {@code saveLoader} in Minecraft, adding useful hooks.<br>
 * <br>
 * The currently implemented hook is in {@code getSaveLoader()}. All other methods just pass the call to the original {@code saveLoader}.
 * 
 * @author ziliss
 * @see net.minecraft.client.Minecraft#saveLoader
 * @since 0.5a
 */
public class SaveFormat implements ISaveFormat {
	
	/**
	 * Reference to the mod instance.
	 */
	public Mod_MystLinkingBook mod_MLB;
	
	/**
	 * Reference to the original saveLoader in Minecraft.
	 * 
	 * @see net.minecraft.client.Minecraft#saveLoader
	 */
	public ISaveFormat originalSaveLoader;
	
	public SaveFormat(ISaveFormat originalSaveLoader, Mod_MystLinkingBook mod_MLB) {
		this.originalSaveLoader = originalSaveLoader;
		this.mod_MLB = mod_MLB;
	}
	
	@Override
	public String getFormatName() {
		return originalSaveLoader.getFormatName();
	}
	
	/**
	 * Called when a new world is going to be loaded. It calls {@code mod_mystlinkingbook.onWorldStarting}.
	 * 
	 * @param s
	 *            The name of the world save folder.
	 * @see Mod_MystLinkingBook#onWorldStarting
	 */
	@Override
	public ISaveHandler getSaveLoader(String s, boolean flag) {
		mod_MLB.onWorldStarting(s);
		return originalSaveLoader.getSaveLoader(s, flag);
	}
	
	@Override
	public List getSaveList() {
		return originalSaveLoader.getSaveList();
	}
	
	@Override
	public void flushCache() {
		originalSaveLoader.flushCache();
	}
	
	@Override
	public WorldInfo getWorldInfo(String s) {
		return originalSaveLoader.getWorldInfo(s);
	}
	
	@Override
	public void deleteWorldDirectory(String s) {
		originalSaveLoader.deleteWorldDirectory(s);
	}
	
	@Override
	public void renameWorld(String s, String s1) {
		originalSaveLoader.renameWorld(s, s1);
	}
	
	@Override
	public boolean isOldMapFormat(String s) {
		return originalSaveLoader.isOldMapFormat(s);
	}
	
	@Override
	public boolean convertMapFormat(String s, IProgressUpdate iprogressupdate) {
		return originalSaveLoader.convertMapFormat(s, iprogressupdate);
	}
	
}
