package net.minecraft.src.mystlinkingbook;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.naming.OperationNotSupportedException;

import net.minecraft.client.Minecraft;
import net.minecraft.src.ModLoader;
import net.minecraft.src.ModTextureStatic;
import net.minecraft.src.SoundPool;
import net.minecraft.src.SoundPoolEntry;
import net.minecraft.src.TextureFX;
import net.minecraft.src.mystlinkingbook.ImagesOnTextureManager.ImageRef;

/**
 * 
 * @author ziliss
 * @since 0.8b
 */
public class RessourcesManager {
	
	public Mod_MystLinkingBook mod_MLB;
	
	public PathEnd assets_package = new PathEnd("/mystlinkingbook/assets/").setInPackage();
	public PathEnd MCDir = new PathEnd("");
	public PathEnd configMLB = new PathEnd(MCDir, "config/mystlinkingbook/");
	public PathEnd ressources = new PathEnd(MCDir, "resources/");
	public PathEnd assets_ressources = new PathEnd(ressources, "mod/mystlinkingbook/assets/");
	public PathEnd world = new PathEnd(new PathEnd(MCDir, "saves/"), "?WORLDNAME?/").setForceReload();
	public PathEnd worldMLB = new PathEnd(world, "mystlinkingbook/");
	public PathEnd assets_world = new PathEnd(worldMLB, "assets/");
	
	public static final int TERRAIN_SPRITE = 0;
	public static final int ITEMS_SPRITE = 1;
	public static final String[] spriteTypesPaths = new String[] { "/terrain.png", "/gui/items.png" };
	
	public RessourcesManager(Mod_MystLinkingBook mod_MLB) {
		this.mod_MLB = mod_MLB;
	}
	
	public void init() {
		try {
			MCDir.piece = Minecraft.getMinecraftDir().getCanonicalPath() + File.separator;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public abstract class Ressource {
		protected ArrayList<PathEnd> paths = new ArrayList<PathEnd>(4);
		protected PathEnd currentPath = null;
		protected Ressource def = null;
		protected boolean usesDefault = false;
		protected boolean isLoaded = false;
		
		public Ressource() {
		}
		
		public Ressource(Ressource def) {
			setDefault(def);
		}
		
		public Ressource setDefault(Ressource def) {
			if (!this.getClass().isAssignableFrom(def.getClass())) throw new RuntimeException("The default Ressource must be of the same kind than the Ressource !");
			this.def = def;
			return this;
		}
		
		public boolean isLoaded() {
			return isLoaded;
		}
		
		public boolean isReady() {
			return isLoaded || usesDefault;
		}
		
		public boolean exists() {
			for (PathEnd path : paths) {
				if (path.exists()) return true;
			}
			return false;
		}
		
		public boolean load() {
			if (usesDefault) {
				stopUsingDefault();
			}
			for (PathEnd path : paths) {
				path = path.copyFlatten();
				if (path.exists()) {
					if (!path.equals(currentPath) || path.isForceReload()) {
						boolean loaded = false;
						try {
							loaded = load(path);
						}
						catch (Exception e) {
							e.printStackTrace();
						}
						if (!loaded) {
							continue;
						}
						currentPath = path;
						isLoaded = true;
						logLoaded();
					}
					return true;
				}
			}
			
			if (isLoaded) {
				clear();
			}
			
			startUsingDefault();
			
			if (isReady()) return true;
			else {
				logNotFound();
				return false;
			}
		}
		
		public abstract boolean load(PathEnd path) throws Exception;
		
		public boolean startUsingDefault() {
			usesDefault = def != null;
			return usesDefault;
		}
		
		public void clear() {
			if (usesDefault) {
				stopUsingDefault();
			}
			if (isLoaded) {
				unload();
			}
		}
		
		public void unload() {
			logUnloaded();
			currentPath = null;
			isLoaded = false;
		}
		
		public void stopUsingDefault() {
			usesDefault = false;
		}
		
		public boolean loadFromDatas(byte[] datas) throws Exception {
			if (loadFromDatas_do(datas)) {
				if (usesDefault) {
					stopUsingDefault();
				}
				return true;
			}
			return false;
		}
		
		public boolean loadFromDatas_do(byte[] datas) throws Exception {
			throw new OperationNotSupportedException("Class " + this.getClass().getSimpleName() + " cannot load datas directly.");
		}
		
		public void logLoaded() {
			System.out.println("Loaded " + this.getClass().getSimpleName() + ": " + currentPath.toString());
		}
		
		public void logUnloaded() {
			System.out.println("Unloaded " + this.getClass().getSimpleName() + ": " + currentPath.toString());
		}
		
		public void logNotFound() {
			System.out.print("Not found " + this.getClass().getSimpleName() + ":");
			for (PathEnd path : paths) {
				System.out.print(" \"" + path + "\"");
			}
			if (def != null) {
				System.out.print(" And could not use default !");
			}
			System.out.println();
		}
	}
	
	public abstract class ImageRessource extends Ressource {
		public String name;
		
		public ImageRessource(String name) {
			this.name = name;
			paths.add(new PathEnd(assets_world, name));
			paths.add(new PathEnd(assets_ressources, name));
			paths.add(new PathEnd(assets_package, name));
		}
		
		@Override
		public boolean load(PathEnd path) throws FileNotFoundException, IOException, Exception {
			return loadImage(loadBufferedImage(path)); // bufferedimage cannot be null here
		}
		
		@Override
		public boolean loadFromDatas_do(byte[] datas) throws Exception {
			return loadImage(loadBufferedImage(datas));
		}
		
		public abstract boolean loadImage(BufferedImage bufferedimage);
	}
	
	// Careful, it does never release the UniqueSpriteIndex !
	public class SpriteRessource extends ImageRessource {
		protected int spriteType;
		protected int spriteId;
		protected TextureFX currentTextureFX = null;
		
		public SpriteRessource(String name, int spriteType) {
			super(name);
			this.spriteType = spriteType;
			spriteId = ModLoader.getUniqueSpriteIndex(spriteTypesPaths[spriteType]);
			System.out.println("Overriding " + spriteTypesPaths[spriteType] + " with a SpriteRessource named \"" + name + "\" @ " + spriteId + ".");
		}
		
		public int getSpriteType() {
			return usesDefault ? ((SpriteRessource)def).getSpriteType() : spriteType;
		}
		
		public int getSpriteId() {
			return usesDefault ? ((SpriteRessource)def).getSpriteId() : spriteId;
		}
		
		@Override
		public boolean loadImage(BufferedImage bufferedimage) {
			ModTextureStatic modtexturestatic = new ModTextureStatic(spriteId, spriteType, bufferedimage);
			PrivateAccesses.RenderEngine_textureList.getFrom(mod_MLB.mc.renderEngine).remove(currentTextureFX);
			mod_MLB.mc.renderEngine.registerTextureFX(modtexturestatic);
			currentTextureFX = modtexturestatic;
			return true;
		}
		
		@Override
		public void unload() {
			if (currentTextureFX != null) {
				Arrays.fill(currentTextureFX.imageData, (byte)0);
				mod_MLB.mc.renderEngine.updateDynamicTextures();
				PrivateAccesses.RenderEngine_textureList.getFrom(mod_MLB.mc.renderEngine).remove(currentTextureFX);
				currentTextureFX = null;
			}
			super.unload();
		}
	}
	
	public class TextureRessource extends ImageRessource {
		protected int textureId = -1;
		
		public TextureRessource(String name) {
			super(name);
		}
		
		public int getTextureId() {
			return usesDefault ? ((TextureRessource)def).getTextureId() : textureId;
		}
		
		@Override
		public boolean loadImage(BufferedImage bufferedimage) {
			if (textureId == -1) {
				textureId = mod_MLB.allocateTextureId();
			}
			mod_MLB.mc.renderEngine.setupTexture(bufferedimage, textureId);
			return true;
		}
		
		@Override
		public void unload() {
			if (textureId != -1) {
				mod_MLB.mc.renderEngine.deleteTexture(textureId);
				mod_MLB.releasedTextureId(textureId);
				textureId = -1;
			}
			super.unload();
		}
	}
	
	public class ImageRefRessource extends ImageRessource {
		public ImageRef imageRef;
		
		public ImageRefRessource(String name, ImageRef imageRef) {
			super(name);
			this.imageRef = imageRef;
		}
		
		public ImageRef getImageRef() {
			return usesDefault ? ((ImageRefRessource)def).getImageRef() : imageRef;
		}
		
		@Override
		public boolean loadImage(BufferedImage bufferedimage) {
			imageRef.updateImage(bufferedimage);
			return true;
		}
		
		@Override
		public void unload() {
			imageRef.updateImage(null);
			super.unload();
		}
	}
	
	public class SoundRessource extends Ressource {
		protected String idRoot;
		protected String srcId;
		protected String soundId;
		protected URL loadedURL = null;
		
		public SoundRessource(String idRoot, String nameWithoutExt) {
			this.idRoot = idRoot;
			
			String[] names = new String[] { ".wav", ".ogg", ".mus" };
			for (int i = 0; i < names.length; i++) {
				names[i] = nameWithoutExt + names[i];
			}
			for (String name : names) {
				paths.add(new PathEnd(assets_world, name));
			}
			for (String name : names) {
				paths.add(new PathEnd(assets_ressources, name));
			}
			for (String name : names) {
				paths.add(new PathEnd(assets_package, name));
			}
		}
		
		public String getSoundId() {
			return usesDefault ? ((SoundRessource)def).getSoundId() : soundId;
		}
		
		@Override
		public boolean load(PathEnd path) throws FileNotFoundException, IOException, Exception {
			if (loadedURL != null) {
				removeSound(srcId, loadedURL);
				loadedURL = null;
			}
			
			String pathStr = path.toString();
			File pathFile = new File(pathStr);
			srcId = idRoot + pathFile.getName();
			
			soundId = srcId.substring(0, srcId.indexOf("."));
			if (PrivateAccesses.SoundManager_soundPoolSounds.getFrom(mod_MLB.mc.sndManager).isGetRandomSound) {
				for (; Character.isDigit(soundId.charAt(soundId.length() - 1)); soundId = soundId.substring(0, soundId.length() - 1)) {
				}
			}
			soundId = soundId.replaceAll("/", ".");
			
			if (path.isInPackage()) {
				URL url = Mod_MystLinkingBook.class.getResource(pathStr);
				addSound(srcId, url);
				loadedURL = url;
			}
			else {
				mod_MLB.mc.sndManager.addSound(srcId, pathFile);
				loadedURL = pathFile.toURI().toURL();
			}
			return true;
		}
		
		@Override
		public void unload() {
			if (loadedURL != null) {
				removeSound(srcId, loadedURL);
				loadedURL = null;
				srcId = null;
				soundId = null;
			}
			super.unload();
		}
	}
	
	public static class PathEnd {
		
		public PathEnd parent = null; // Considered final
		
		public String piece = ""; // Never null !
		
		protected boolean inPackage = false; // Considered final
		
		protected boolean forceReload = false; // Can be changed at anytime
		
		protected boolean disabled = false; // Can be changed at anytime
		
		public PathEnd(PathEnd parent, String piece) {
			this.parent = parent;
			this.piece = piece;
		}
		
		public PathEnd(String piece) {
			this.piece = piece;
		}
		
		public PathEnd setInPackage() {
			inPackage = true;
			return this;
		}
		
		public PathEnd setForceReload() {
			forceReload = true;
			return this;
		}
		
		public boolean isInPackage() {
			return parent == null ? inPackage : parent.inPackage;
		}
		
		public boolean isForceReload() {
			return forceReload ? true : parent == null ? false : parent.isForceReload();
		}
		
		public boolean isDisabled() {
			return disabled ? true : parent == null ? false : parent.isDisabled();
		}
		
		public void setDisabled(boolean disabled) {
			this.disabled = disabled;
		}
		
		public boolean exists() {
			if (disabled) return false;
			if (isInPackage()) return Mod_MystLinkingBook.class.getResource(toString()) != null;
			else return new File(toString()).exists();
		}
		
		public boolean isParentOf(PathEnd path) {
			PathEnd current = this;
			do {
				if (current.equals(path)) return true;
				current = current.parent;
			} while (current.parent != null);
			return false;
		}
		
		public StringBuilder toString(StringBuilder builder) {
			if (parent != null) {
				parent.toString(builder);
			}
			builder.append(piece);
			return builder;
		}
		
		@Override
		public String toString() {
			return toString(new StringBuilder()).toString();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj != null && obj instanceof PathEnd) {
				PathEnd other = (PathEnd)obj;
				return isInPackage() == other.isInPackage() && toString().equals(other.toString());
			}
			else return false;
		}
		
		public PathEnd copyFlatten() {
			PathEnd path = new PathEnd(toString());
			path.inPackage = isInPackage();
			// Does not copy disabled state.
			return path;
		}
	}
	
	public BufferedImage loadBufferedImage(PathEnd path) throws FileNotFoundException, IOException, Exception {
		if (path.isInPackage()) return ModLoader.loadImage(mod_MLB.mc.renderEngine, path.toString());
		else {
			InputStream in = new FileInputStream(path.toString());
			return ImageIO.read(in);
		}
	}
	
	public BufferedImage loadBufferedImage(byte[] datas) throws IOException {
		InputStream in = new ByteArrayInputStream(datas);
		return ImageIO.read(in);
	}
	
	public SoundPoolEntry addSound(String par1Str, URL url) {
		// Preparing the variables:
		SoundPool sndPool = PrivateAccesses.SoundManager_soundPoolSounds.getFrom(mod_MLB.mc.sndManager);
		Map nameToSoundPoolEntriesMapping = PrivateAccesses.SoundPool_nameToSoundPoolEntriesMapping.getFrom(sndPool);
		List allSoundPoolEntries = PrivateAccesses.SoundPool_allSoundPoolEntries.getFrom(sndPool);
		
		// The following part is taken from SoundPool.addSound(...):
		String s = par1Str;
		par1Str = par1Str.substring(0, par1Str.indexOf("."));
		
		if (sndPool.isGetRandomSound) {
			for (; Character.isDigit(par1Str.charAt(par1Str.length() - 1)); par1Str = par1Str.substring(0, par1Str.length() - 1)) {
			}
		}
		
		par1Str = par1Str.replaceAll("/", ".");
		
		if (!nameToSoundPoolEntriesMapping.containsKey(par1Str)) {
			nameToSoundPoolEntriesMapping.put(par1Str, new ArrayList());
		}
		
		SoundPoolEntry soundpoolentry = new SoundPoolEntry(s, url);
		((List)nameToSoundPoolEntriesMapping.get(par1Str)).add(soundpoolentry);
		allSoundPoolEntries.add(soundpoolentry);
		sndPool.numberOfSoundPoolEntries++;
		return soundpoolentry;
	}
	
	public void removeSound(String par1Str, URL url) {
		// Preparing the variables:
		SoundPool sndPool = PrivateAccesses.SoundManager_soundPoolSounds.getFrom(mod_MLB.mc.sndManager);
		Map nameToSoundPoolEntriesMapping = PrivateAccesses.SoundPool_nameToSoundPoolEntriesMapping.getFrom(sndPool);
		List allSoundPoolEntries = PrivateAccesses.SoundPool_allSoundPoolEntries.getFrom(sndPool);
		
		// String s = par1Str;
		par1Str = par1Str.substring(0, par1Str.indexOf("."));
		
		if (sndPool.isGetRandomSound) {
			for (; Character.isDigit(par1Str.charAt(par1Str.length() - 1)); par1Str = par1Str.substring(0, par1Str.length() - 1)) {
			}
		}
		
		par1Str = par1Str.replaceAll("/", ".");
		
		ArrayList<SoundPoolEntry> list = (ArrayList<SoundPoolEntry>)nameToSoundPoolEntriesMapping.get(par1Str);
		for (SoundPoolEntry soundpoolentry : list) {
			if (soundpoolentry.soundUrl.equals(url)) {
				list.remove(soundpoolentry);
				allSoundPoolEntries.remove(soundpoolentry);
				sndPool.numberOfSoundPoolEntries--;
				break;
			}
		}
	}
	
	public static final void downloadRessource(InputStream in, File dest) throws Exception {
		in = new BufferedInputStream(in);
		dest.getParentFile().mkdirs();
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(dest);
			byte cache[] = new byte[1024 * 32];
			for (int i = 0; (i = in.read(cache)) >= 0;) {
				out.write(cache, 0, i);
			}
			out.flush();
			System.out.println("Resource added: " + dest);
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			try {
				in.close();
				if (out != null) {
					out.close();
				}
			}
			catch (IOException ignored) {
			}
		}
	}
}
