package net.minecraft.src.mystlinkingbook;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.TreeSet;

import net.minecraft.client.Minecraft;
import net.minecraft.src.BaseMod;
import net.minecraft.src.Block;
import net.minecraft.src.GameSettings;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.ISaveFormat;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.RenderBlocks;
import net.minecraft.src.SoundManager;
import net.minecraft.src.SoundPool;
import net.minecraft.src.SoundPoolEntry;
import net.minecraft.src.mystlinkingbook.ImagesOnTextureManager.ImageRef;

import org.lwjgl.opengl.GL11;

import paulscode.sound.SoundSystem;

public class Mod_MystLinkingBook extends BaseMod {
	
	public Minecraft mc;
	
	public ScheduledActionsManager scheduledActionsManager;
	
	public ImagesOnTextureManager itm;
	
	// Contains methods to interact with the datas of the Linking Books:
	public LinkingBook linkingBook;
	
	public BlockLinkingBook blockLinkingBook;
	public ItemBlockLinkingBook itemBlockLinkingBook;
	public ItemPage itemPage;
	
	public static String resourcesPath = "/mystlinkingbook/resources/";
	
	TreeSet<Integer> texturesPool = new TreeSet<Integer>();
	int lastUsedTextureId = 3233 - 1;
	
	public ImageRef missingLinkingPanelImageRef = null;
	
	public Mod_MystLinkingBook() {
	}
	
	@Override
	public String getVersion() {
		return "0.7b";
	}
	
	/**
	 * Load ressources and register all necessary elements.
	 * 
	 * Called when the mod is loaded.
	 */
	@Override
	public void load() {
		if (PrivateAccesses.hasFieldsNotFound) throw new RuntimeException("Some private fields could not be found.");
		
		mc = ModLoader.getMinecraftInstance();
		
		scheduledActionsManager = new ScheduledActionsManager(this);
		itm = new ImagesOnTextureManager(256, 256, 80, 60, this);
		linkingBook = new LinkingBook(itm);
		blockLinkingBook = new BlockLinkingBook(233, 233, this);
		itemBlockLinkingBook = new ItemBlockLinkingBook(233 - 256, this);
		itemPage = new ItemPage(3233);
		
		blockLinkingBook.topTextureIndex = ModLoader.addOverride("/terrain.png", resourcesPath + "blockLinkingBookSide.png");
		blockLinkingBook.sideTextureIndex = blockLinkingBook.topTextureIndex;
		blockLinkingBook.bottomTextureIndex = blockLinkingBook.topTextureIndex;
		blockLinkingBook.renderType = ModLoader.getUniqueBlockModelID(this, false);
		
		itemBlockLinkingBook.setIconIndex(ModLoader.addOverride("/gui/items.png", resourcesPath + "iconLinkingBook.png"));
		itemBlockLinkingBook.unwrittenIconIndex = ModLoader.addOverride("/gui/items.png", resourcesPath + "iconLinkingBookUnwritten.png");
		itemBlockLinkingBook.pagesIconIndex = ModLoader.addOverride("/gui/items.png", resourcesPath + "iconLinkingBookPages.png");
		itemBlockLinkingBook.unwrittenPagesIconIndex = ModLoader.addOverride("/gui/items.png", resourcesPath + "iconLinkingBookUnwrittenPages.png");
		
		itemPage.setIconIndex(ModLoader.addOverride("/gui/items.png", resourcesPath + "iconPage.png"));
		
		// Execute the following private method:
		// Block.fire.setBurnRate(Block.bookShelf.blockID, 70, 100);
		PrivateAccesses.BlockFire_chanceToEncourageFire.getFrom(Block.fire)[blockLinkingBook.blockID] = 70;
		PrivateAccesses.BlockFire_abilityToCatchFire.getFrom(Block.fire)[blockLinkingBook.blockID] = 100;
		
		BufferedImage img;
		try {
			img = ModLoader.loadImage(mc.renderEngine, resourcesPath + "tempLinkGUI.png");
			mc.renderEngine.setupTexture(img, getTextureId());
			// img = ModLoader.loadImage(mc.renderEngine, resourcesPath + "tempPanel.png");
			// mc.renderEngine.setupTexture(img, nextTextureId++);
			img = ModLoader.loadImage(mc.renderEngine, resourcesPath + "tempWriteGUI.png");
			mc.renderEngine.setupTexture(img, getTextureId());
			img = ModLoader.loadImage(mc.renderEngine, resourcesPath + "tempLookGUI.png");
			mc.renderEngine.setupTexture(img, getTextureId());
			img = ModLoader.loadImage(mc.renderEngine, resourcesPath + "tempLinkingBook3D.png");
			mc.renderEngine.setupTexture(img, getTextureId());
			img = ModLoader.loadImage(mc.renderEngine, resourcesPath + "missingLinkingPanelImage.png");
			missingLinkingPanelImageRef = itm.registerImage(img);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		ModLoader.addName(blockLinkingBook, "Linking Book");
		ModLoader.addName(itemBlockLinkingBook, "Linking Book");
		
		ModLoader.registerTileEntity(TileEntityLinkingBook.class, "LinkingBook", new RenderLinkingBook(this));
		
		ModLoader.addRecipe(new ItemStack(itemBlockLinkingBook, 1, 0), new Object[] { "#", "#", Character.valueOf('#'), Item.paper });
		
		File resourcesFolder = new File(Minecraft.getMinecraftDir(), "resources/");
		File linkingsound = new File(resourcesFolder, "mod/mystlinkingbook/defaultlinkingsound.wav");
		if (!linkingsound.exists()) {
			InputStream includedLinkingSound = Mod_MystLinkingBook.class.getResourceAsStream(resourcesPath + "defaultlinkingsound.wav");
			if (includedLinkingSound != null) {
				try {
					downloadRessource(includedLinkingSound, linkingsound);
					System.out.println("Added default linking sound: " + linkingsound);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			else {
				new FileNotFoundException("Could not add default linking sound: " + linkingsound).printStackTrace();
			}
		}
		linkingsound = null;
		String[] names = new String[] { "linkingsound", "defaultlinkingsound" };
		String[] exts = new String[] { ".wav", ".ogg", ".mus" };
		searchSound: for (String name : names) {
			for (String ext : exts) {
				linkingsound = new File(resourcesFolder, "mod/mystlinkingbook/" + name + ext);
				// For debugging the sound file:
				/*File temp = linkingsound;
				while (temp.getParent() != null) {
					System.out.println(temp.getAbsolutePath() + ": " + (temp.exists() ? "exists" : "!!! DOES NOT EXIST !!!"));
				}*/
				if (linkingsound.exists()) {
					mc.sndManager.addSound("mystlinkingbook/linkingsound" + ext, linkingsound);
					System.out.println("Using linking sound: " + linkingsound);
					break searchSound;
				}
			}
		}
		
		// Modify the following private field:
		// mc.saveLoader = new LinkingBookSaveFormat(mc.saveLoader, this);
		ISaveFormat saveLoader = PrivateAccesses.Minecraft_saveLoader.getFrom(mc);
		SaveFormat saveFormat = new SaveFormat(saveLoader, this);
		PrivateAccesses.Minecraft_saveLoader.setTo(mc, saveFormat);
		
		// ModLoader.setInGameHook(this, true, true);
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
		linkingBook.agesManager.changeWorld(worldFolder);
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
	
	@Override
	public boolean onTickInGame(float tick, Minecraft mc) {
		return scheduledActionsManager.OnTickInGame(tick, mc);
	}
	
	@Override
	public boolean renderWorldBlock(RenderBlocks renderblocks, IBlockAccess iblockaccess, int i, int j, int k, Block block, int l) {
		// Taken from: RenderBlocks.renderBlockByRenderType:
		// block.setBlockBoundsBasedOnState(iblockaccess, i, j, k);
		renderblocks.renderStandardBlock(block, i, j, k);
		return true;
	}
	
	public int getTextureId() {
		if (texturesPool.isEmpty()) {
			while (GL11.glIsTexture(++lastUsedTextureId)) {
			}
			return lastUsedTextureId;
		}
		else {
			Integer id = texturesPool.first();
			texturesPool.remove(id);
			return id;
		}
	}
	
	public void addReleasedTextureId(int id) {
		texturesPool.add(id);
	}
	
}
