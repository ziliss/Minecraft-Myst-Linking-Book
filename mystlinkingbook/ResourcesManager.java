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
import net.minecraft.src.mystlinkingbook.ResourcePath.Kind;

/**
 * 
 * @author ziliss
 * @since 0.8b
 */
public class ResourcesManager {
	
	public Mod_MystLinkingBook mod_MLB;
	
	public Kind worldAsset = new Kind("WorldAsset", true);
	
	public ResourcePath assets_package = new ResourcePath("/mystlinkingbook/assets/").setInPackage();
	
	public ResourcePath MCDir = new ResourcePath(""); // Set in init()
	public ResourcePath configMLB = new ResourcePath(MCDir, "config/mystlinkingbook/");
	
	public ResourcePath resources = new ResourcePath(MCDir, "resources/");
	public ResourcePath assets_resources = new ResourcePath(resources, "mods/mystlinkingbook/assets/");
	
	public ResourcePath world = new ResourcePath(new ResourcePath(MCDir, "saves/"), "?WORLDNAME?/").setDynamic();
	public ResourcePath worldMLB = new ResourcePath(world, "mystlinkingbook/");
	public ResourcePath assets_world = new ResourcePath(worldMLB, "assets/").addkind(worldAsset);
	
	/*public ResourcePath texPacksDir = new ResourcePath(MCDir, "texturepacks/");
	public ResourcePath texPack = new ResourcePath(texPacksDir, "?TEXTUREPACKNAME?/");
	public ResourcePath texPackMLB = new ResourcePath(texPack, "mystlinkingbook/");
	public ResourcePath assets_texPacks = new ResourcePath(texPackMLB, "assets/");*/
	
	protected ResourcePath[] assetsLocations = new ResourcePath[] { assets_world, assets_resources, assets_package };
	
	protected String[] imagesExts = new String[] { ".png", ".gif", ".jpg" };
	protected String[] soundExts = new String[] { ".wav", ".ogg", ".mus" };
	
	public static final int TERRAIN_SPRITE = 0;
	public static final int ITEMS_SPRITE = 1;
	public static final String[] spriteTypesPaths = new String[] { "/terrain.png", "/gui/items.png" };
	
	public ResourcesManager(Mod_MystLinkingBook mod_MLB) {
		this.mod_MLB = mod_MLB;
	}
	
	public void init() {
		try {
			MCDir.path = Minecraft.getMinecraftDir().getCanonicalPath() + File.separator;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void startNewWorld(String worldFolderName) {
		world.setPath(worldFolderName + "/");
	}
	
	public abstract class Resource {
		protected ArrayList<ResourcePath> paths = new ArrayList<ResourcePath>(4);
		protected ResourcePath currentPath = null;
		protected Resource def = null;
		protected boolean usesDefault = false;
		protected boolean isLoaded = false;
		
		public Resource() {
		}
		
		public Resource(Resource def) {
			setDefault(def);
		}
		
		public Resource setDefault(Resource def) {
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
			for (ResourcePath path : paths) {
				if (path.exists()) return true;
			}
			return false;
		}
		
		public boolean load() {
			if (usesDefault) {
				stopUsingDefault();
			}
			for (ResourcePath path : paths) {
				path = path.copyFlatten();
				if (path.exists()) {
					if (!path.equals(currentPath) || path.isDynamic()) {
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
		
		public abstract boolean load(ResourcePath path) throws Exception;
		
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
			for (ResourcePath path : paths) {
				if (!path.isDisabled()) {
					System.out.print(" \"" + path + "\"");
				}
			}
			if (def != null) {
				System.out.print(" And could not use default !");
			}
			System.out.println();
		}
	}
	
	public abstract class ImageResource extends Resource {
		public String nameWithoutExt;
		
		public ImageResource(String nameWithoutExt) {
			this.nameWithoutExt = nameWithoutExt;
			ResourcePath namedRes;
			for (ResourcePath location : assetsLocations) {
				namedRes = new ResourcePath(location, nameWithoutExt);
				for (String ext : imagesExts) {
					paths.add(new ResourcePath(namedRes, ext));
				}
			}
		}
		
		@Override
		public boolean load(ResourcePath path) throws FileNotFoundException, IOException, Exception {
			return loadImage(loadBufferedImage(path)); // bufferedimage cannot be null here
		}
		
		@Override
		public boolean loadFromDatas_do(byte[] datas) throws Exception {
			return loadImage(loadBufferedImage(datas));
		}
		
		public abstract boolean loadImage(BufferedImage bufferedimage);
	}
	
	// Careful, it does never release the UniqueSpriteIndex !
	public class SpriteResource extends ImageResource {
		protected int spriteType;
		protected int spriteId;
		protected TextureFX currentTextureFX = null;
		
		public SpriteResource(String nameWithoutExt, int spriteType) {
			super(nameWithoutExt);
			this.spriteType = spriteType;
			spriteId = ModLoader.getUniqueSpriteIndex(spriteTypesPaths[spriteType]);
			System.out.println("Overriding " + spriteTypesPaths[spriteType] + " with a SpriteRessource named \"" + nameWithoutExt + "\" @ " + spriteId + ".");
		}
		
		public int getSpriteType() {
			return usesDefault ? ((SpriteResource)def).getSpriteType() : spriteType;
		}
		
		public int getSpriteId() {
			return usesDefault ? ((SpriteResource)def).getSpriteId() : spriteId;
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
	
	public class TextureResource extends ImageResource {
		protected int textureId = -1;
		
		public TextureResource(String nameWithoutExt) {
			super(nameWithoutExt);
		}
		
		public int getTextureId() {
			return usesDefault ? ((TextureResource)def).getTextureId() : textureId;
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
	
	public class ImageRefResource extends ImageResource {
		public ImageRef imageRef;
		
		public ImageRefResource(String nameWithoutExt, ImageRef imageRef) {
			super(nameWithoutExt);
			this.imageRef = imageRef;
		}
		
		public ImageRef getImageRef() {
			return usesDefault ? ((ImageRefResource)def).getImageRef() : imageRef;
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
	
	public class SoundRessource extends Resource {
		protected String idRoot;
		protected String srcId;
		protected String soundId;
		protected URL loadedURL = null;
		
		public SoundRessource(String idRoot, String nameWithoutExt) {
			this.idRoot = idRoot;
			
			ResourcePath namedRes;
			for (ResourcePath location : assetsLocations) {
				namedRes = new ResourcePath(location, nameWithoutExt);
				for (String ext : soundExts) {
					paths.add(new ResourcePath(namedRes, ext));
				}
			}
		}
		
		public String getSoundId() {
			return usesDefault ? ((SoundRessource)def).getSoundId() : soundId;
		}
		
		@Override
		public boolean load(ResourcePath path) throws FileNotFoundException, IOException, Exception {
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
	
	public BufferedImage loadBufferedImage(ResourcePath path) throws FileNotFoundException, IOException, Exception {
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
	
	public static final void downloadResource(InputStream in, File dest) throws Exception {
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
