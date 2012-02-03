package net.minecraft.src;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.minecraft.client.Minecraft;
import paulscode.sound.SoundSystem;

public class mod_mystlinkingbook extends BaseMod {
	
	// Contains methods to interact with the datas of the Linking Books:
	public LinkingBook linkingBook = new LinkingBook();
	
	public BlockLinkingBook blockLinkingBook = new BlockLinkingBook(233, 233, this);
	public ItemBlockLinkingBook itemBlockLinkingBook = new ItemBlockLinkingBook(233 - 256, this);
	
	public mod_mystlinkingbook() {
	}
	
	@Override
	public String getVersion() {
		return "0.5a";
	}
	
	/**
	 * Load ressources and register all necessary elements.
	 * 
	 * Called when the mod is loaded.
	 */
	@Override
	public void load() {
		Minecraft mc = ModLoader.getMinecraftInstance();
		
		blockLinkingBook.topTextureIndex = ModLoader.addOverride("/terrain.png", "/mystlinkingbook/blockLinkingBookSide.png");
		blockLinkingBook.sideTextureIndex = blockLinkingBook.topTextureIndex;
		blockLinkingBook.bottomTextureIndex = blockLinkingBook.topTextureIndex;
		// itemBlockLinkingBook.iconIndex = ModLoader.addOverride("/gui/items.png", "/mystlinkingbook/tempBook.png");
		
		// Execute the following private method:
		// Block.fire.setBurnRate(Block.bookShelf.blockID, 70, 100);
		try {
			int[] chanceToEncourageFire = (int[])getPrivateValue(BlockFire.class, Block.fire, "a", "chanceToEncourageFire"); // MCPBot: gcf BlockFire.chanceToEncourageFire
			int[] abilityToCatchFire = (int[])getPrivateValue(BlockFire.class, Block.fire, "b", "abilityToCatchFire"); // MCPBot: gcf BlockFire.abilityToCatchFire
			chanceToEncourageFire[blockLinkingBook.blockID] = 70;
			abilityToCatchFire[blockLinkingBook.blockID] = 100;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		BufferedImage img;
		try {
			img = ModLoader.loadImage(mc.renderEngine, "/mystlinkingbook/tempLinkGUI.png");
			mc.renderEngine.setupTexture(img, 3233);
			img = ModLoader.loadImage(mc.renderEngine, "/mystlinkingbook/tempPanel.png");
			mc.renderEngine.setupTexture(img, 3234);
			img = ModLoader.loadImage(mc.renderEngine, "/mystlinkingbook/tempWriteGUI.png");
			mc.renderEngine.setupTexture(img, 3235);
			img = ModLoader.loadImage(mc.renderEngine, "/mystlinkingbook/tempLookGUI.png");
			mc.renderEngine.setupTexture(img, 3236);
			img = ModLoader.loadImage(mc.renderEngine, "/mystlinkingbook/tempLinkingBook3D.png");
			mc.renderEngine.setupTexture(img, 3237);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		ModLoader.AddName(blockLinkingBook, "Linking Book");
		ModLoader.AddName(itemBlockLinkingBook, "Linking Book");
		
		ModLoader.RegisterTileEntity(TileEntityLinkingBook.class, "LinkingBook", new RenderLinkingBook());
		
		ModLoader.AddRecipe(new ItemStack(itemBlockLinkingBook, 1), new Object[] { "#", "#", Character.valueOf('#'), Item.paper });
		
		File resourcesFolder = new File(Minecraft.getMinecraftDir(), "resources/");
		String[] exts = new String[] { ".wav", ".ogg", ".mus" };
		File linkingsound = null;
		for (String ext : exts) {
			linkingsound = new File(resourcesFolder, "mod/mystlinkingbook/linkingsound" + ext);
			if (linkingsound.exists()) {
				mc.sndManager.addSound("mystlinkingbook/linkingsound" + ext, linkingsound);
				break;
			}
			linkingsound = null;
		}
		if (linkingsound == null) {
			InputStream integratedLinkingSound = mod_mystlinkingbook.class.getResourceAsStream("/mystlinkingbook/linkingsound.wav");
			if (integratedLinkingSound != null) {
				linkingsound = new File(resourcesFolder, "mod/mystlinkingbook/linkingsound.wav");
				try {
					downloadRessource(integratedLinkingSound, linkingsound);
					mc.sndManager.addSound("mystlinkingbook/linkingsound.wav", linkingsound);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		// Modify the following private field:
		// mc.saveLoader = new LinkingBookSaveFormat(mc.saveLoader, this);
		try {
			ISaveFormat saveLoader = (ISaveFormat)getPrivateValue(Minecraft.class, mc, "ad", "saveLoader"); // MCPBot: gcf Minecraft.saveLoader
			LinkingBookSaveFormat saveFormat = new LinkingBookSaveFormat(saveLoader, this);
			setPrivateValue(Minecraft.class, mc, "ad", "saveLoader", saveFormat); // MCPBot: gcf Minecraft.saveLoader
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Called when a new world starts loading.
	 * 
	 * @param worldFolderName
	 *            The name of the world save folder.
	 * @see LinkingBookSaveFormat#getSaveLoader
	 */
	public void onWorldStarting(String worldFolderName) {
		File worldFolder = new File(Minecraft.getMinecraftDir(), "saves/" + worldFolderName);
		File worldMLBFolder = new File(worldFolder, "mystlinkingbook");
		linkingBook.agesManager.setAgesDataFile(new File(worldMLBFolder, "AgesDatas.props"));
		linkingBook.agesManager.loadAges();
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
			System.out.println("Ressource added: " + dest);
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
	
	/**
	 * Rewrites the method {@code SoundManager.playSoundFX()} so that it doesn't lower the volume of the sound. <br>
	 * <br>
	 * The only difference is that the instruction {@code f *= 0.25F} is not executed.
	 * 
	 * @see SoundManager#playSoundFX(String s, float f, float f1)
	 */
	public static final void playSoundFX(String s, float f, float f1) {
		SoundManager sndManager = ModLoader.getMinecraftInstance().sndManager;
		
		try {
			boolean loaded = (Boolean)getPrivateValue(SoundManager.class, sndManager, "g", "loaded"); // MCPBot: gcf SoundManager.loaded
			GameSettings options = (GameSettings)getPrivateValue(SoundManager.class, sndManager, "f", "options"); // MCPBot: gcf SoundManager.options
			SoundPool soundPoolSounds = (SoundPool)getPrivateValue(SoundManager.class, sndManager, "b", "soundPoolSounds"); // MCPBot: gcf SoundManager.soundPoolSounds
			int latestSoundID = (Integer)getPrivateValue(SoundManager.class, sndManager, "e", "latestSoundID"); // MCPBot: gcf SoundManager.latestSoundID
			SoundSystem sndSystem = (SoundSystem)getPrivateValue(SoundManager.class, sndManager, "a", "sndSystem"); // MCPBot: gcf SoundManager.sndSystem
			
			// The following part is taken from SoundManager.playSoundFX(...):
			if (!loaded || options.soundVolume == 0.0F) return;
			
			SoundPoolEntry soundpoolentry = soundPoolSounds.getRandomSoundFromSoundPool(s);
			if (soundpoolentry != null) {
				latestSoundID = (latestSoundID + 1) % 256;
				setPrivateValue(SoundManager.class, sndManager, "e", "latestSoundID", latestSoundID); // MCPBot: gcf SoundManager.latestSoundID
				String s1 = new StringBuilder("sound_").append(latestSoundID).toString();
				sndSystem.newSource(false, s1, soundpoolentry.soundUrl, soundpoolentry.soundName, false, 0.0F, 0.0F, 0.0F, 0, 0.0F);
				if (f > 1.0F) {
					f = 1.0F;
				}
				// f *= 0.25F;
				sndSystem.setPitch(s1, f1);
				sndSystem.setVolume(s1, f * options.soundVolume);
				sndSystem.play(s1);
			}
			// End of the part from SoundManager.playSoundFX(...).
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static final <T, E> T getPrivateValue(Class<? super E> instanceClass, E instance, String fieldObfName, String fieldName) throws Exception {
		try {
			return (T)ModLoader.getPrivateValue(instanceClass, instance, fieldObfName);
		}
		catch (NoSuchFieldException e) {
			try {
				return (T)ModLoader.getPrivateValue(instanceClass, instance, fieldName);
			}
			catch (Exception ex) {
				e.printStackTrace();
				throw ex;
			}
		}
	}
	
	public static final <T, E> void setPrivateValue(Class<? super E> instanceClass, E instance, String fieldObfName, String fieldName, T value) throws Exception {
		try {
			ModLoader.setPrivateValue(instanceClass, instance, fieldObfName, value);
		}
		catch (NoSuchFieldException e) {
			try {
				ModLoader.setPrivateValue(instanceClass, instance, fieldName, value);
			}
			catch (Exception ex) {
				e.printStackTrace();
				throw ex;
			}
		}
	}
}
