package net.minecraft.src.mystlinkingbook;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.minecraft.client.Minecraft;
import net.minecraft.src.BaseMod;
import net.minecraft.src.Block;
import net.minecraft.src.GameSettings;
import net.minecraft.src.ISaveFormat;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.SoundManager;
import net.minecraft.src.SoundPool;
import net.minecraft.src.SoundPoolEntry;
import paulscode.sound.SoundSystem;

public class Mod_MystLinkingBook extends BaseMod {
	
	// Contains methods to interact with the datas of the Linking Books:
	public LinkingBook linkingBook = new LinkingBook();
	
	public BlockLinkingBook blockLinkingBook = new BlockLinkingBook(233, 233, this);
	public ItemBlockLinkingBook itemBlockLinkingBook = new ItemBlockLinkingBook(233 - 256, this);
	
	public static String resourcesPath = "/mystlinkingbook/resources/";
	
	public Mod_MystLinkingBook() {
	}
	
	@Override
	public String getVersion() {
		return "0.6b";
	}
	
	/**
	 * Load ressources and register all necessary elements.
	 * 
	 * Called when the mod is loaded.
	 */
	@Override
	public void load() {
		if (PrivateAccesses.hasFieldsNotFound) throw new RuntimeException("Some private fields could not be found.");
		
		Minecraft mc = ModLoader.getMinecraftInstance();
		
		blockLinkingBook.topTextureIndex = ModLoader.addOverride("/terrain.png", resourcesPath + "blockLinkingBookSide.png");
		blockLinkingBook.sideTextureIndex = blockLinkingBook.topTextureIndex;
		blockLinkingBook.bottomTextureIndex = blockLinkingBook.topTextureIndex;
		// itemBlockLinkingBook.iconIndex = ModLoader.addOverride("/gui/items.png", resourcePath+"tempBook.png");
		
		// Execute the following private method:
		// Block.fire.setBurnRate(Block.bookShelf.blockID, 70, 100);
		PrivateAccesses.BlockFire_chanceToEncourageFire.getFrom(Block.fire)[blockLinkingBook.blockID] = 70;
		PrivateAccesses.BlockFire_abilityToCatchFire.getFrom(Block.fire)[blockLinkingBook.blockID] = 100;
		
		BufferedImage img;
		try {
			img = ModLoader.loadImage(mc.renderEngine, resourcesPath + "tempLinkGUI.png");
			mc.renderEngine.setupTexture(img, 3233);
			img = ModLoader.loadImage(mc.renderEngine, resourcesPath + "tempPanel.png");
			mc.renderEngine.setupTexture(img, 3234);
			img = ModLoader.loadImage(mc.renderEngine, resourcesPath + "tempWriteGUI.png");
			mc.renderEngine.setupTexture(img, 3235);
			img = ModLoader.loadImage(mc.renderEngine, resourcesPath + "tempLookGUI.png");
			mc.renderEngine.setupTexture(img, 3236);
			img = ModLoader.loadImage(mc.renderEngine, resourcesPath + "tempLinkingBook3D.png");
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
			InputStream integratedLinkingSound = Mod_MystLinkingBook.class.getResourceAsStream(resourcesPath + "linkingsound.wav");
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
		ISaveFormat saveLoader = PrivateAccesses.Minecraft_saveLoader.getFrom(mc);
		SaveFormat saveFormat = new SaveFormat(saveLoader, this);
		PrivateAccesses.Minecraft_saveLoader.setTo(mc, saveFormat);
	}
	
	/**
	 * Called when a new world starts loading.
	 * 
	 * @param worldFolderName
	 *            The name of the world save folder.
	 * @see SaveFormat#getSaveLoader
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
	
	/**
	 * Rewrites the method {@code SoundManager.playSoundFX()} so that it doesn't lower the volume of the sound. <br>
	 * <br>
	 * The only difference is that the instruction {@code f *= 0.25F} is not executed.
	 * 
	 * @see SoundManager#playSoundFX(String s, float f, float f1)
	 */
	public static final void playSoundFX(String s, float f, float f1) {
		SoundManager sndManager = ModLoader.getMinecraftInstance().sndManager;
		
		boolean loaded = PrivateAccesses.SoundManager_loaded.getFrom(sndManager);
		GameSettings options = PrivateAccesses.SoundManager_options.getFrom(sndManager);
		SoundPool soundPoolSounds = PrivateAccesses.SoundManager_soundPoolSounds.getFrom(sndManager);
		int latestSoundID = PrivateAccesses.SoundManager_latestSoundID.getFrom(sndManager);
		SoundSystem sndSystem = PrivateAccesses.SoundManager_sndSystem.getFrom(sndManager);
		
		// The following part is taken from SoundManager.playSoundFX(...):
		if (!loaded || options.soundVolume == 0.0F) return;
		
		SoundPoolEntry soundpoolentry = soundPoolSounds.getRandomSoundFromSoundPool(s);
		if (soundpoolentry != null) {
			latestSoundID = (latestSoundID + 1) % 256;
			PrivateAccesses.SoundManager_latestSoundID.setTo(sndManager, latestSoundID);
			String s1 = new StringBuilder("sound_").append(latestSoundID).toString();
			sndSystem.newSource(false, s1, soundpoolentry.soundUrl, soundpoolentry.soundName, false, 0.0F, 0.0F, 0.0F, 0, 0.0F);
			if (f > 1.0F) {
				f = 1.0F;
			}
			// Removing this instruction: f *= 0.25F;
			sndSystem.setPitch(s1, f1);
			sndSystem.setVolume(s1, f * options.soundVolume);
			sndSystem.play(s1);
		}
		// End of the part from SoundManager.playSoundFX(...).
	}
}
