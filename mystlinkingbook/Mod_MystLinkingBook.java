package net.minecraft.src.mystlinkingbook;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import net.minecraft.src.mystlinkingbook.RessourcesManager.ImageRefRessource;
import net.minecraft.src.mystlinkingbook.RessourcesManager.PathEnd;
import net.minecraft.src.mystlinkingbook.RessourcesManager.Ressource;
import net.minecraft.src.mystlinkingbook.RessourcesManager.SoundRessource;
import net.minecraft.src.mystlinkingbook.RessourcesManager.SpriteRessource;
import net.minecraft.src.mystlinkingbook.RessourcesManager.TextureRessource;

import org.lwjgl.opengl.GL11;

import paulscode.sound.SoundSystem;

public class Mod_MystLinkingBook extends BaseMod {
	
	public Minecraft mc;
	
	public Settings settings;
	
	public ScheduledActionsManager scheduledActionsManager;
	
	public ImagesOnTextureManager itm;
	
	public RessourcesManager ressourcesManager = new RessourcesManager(this);
	public ArrayList<Ressource> ressources = new ArrayList<Ressource>();
	
	// Contains methods to interact with the datas of the Linking Books:
	public LinkingBook linkingBook;
	
	public BlockLinkingBook blockLinkingBook;
	public ItemBlockLinkingBook itemBlockLinkingBook;
	public ItemPage itemPage;
	
	TreeSet<Integer> texturesPool = new TreeSet<Integer>();
	int lastUsedTextureId = 3233 - 1;
	
	public TextureRessource texture_tempLinkGUI;
	public TextureRessource texture_tempWriteGUI;
	public TextureRessource texture_tempLookGUI;
	public TextureRessource texture_tempLinkingBook3D;
	
	public ImageRefRessource missingLinkingPanelImage;
	
	public SoundRessource linkingsound;
	
	public Mod_MystLinkingBook() {
	}
	
	@Override
	public String getVersion() {
		return "0.8b";
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
		ressourcesManager.init();
		itm = new ImagesOnTextureManager(256, 256, 80, 60, this);
		
		PathEnd basePropsPath = new PathEnd(ressourcesManager.configMLB, "options.properties");
		PathEnd worldPropsPath = new PathEnd(ressourcesManager.worldMLB, "options.properties");
		settings = new Settings(basePropsPath, worldPropsPath);
		
		linkingBook = new LinkingBook(settings, this);
		
		SpriteRessource top = ressourcesManager.new SpriteRessource("blockLinkingBookSide.png", RessourcesManager.TERRAIN_SPRITE);
		SpriteRessource side = ressourcesManager.new SpriteRessource("blockLinkingBookSide.png", RessourcesManager.TERRAIN_SPRITE);
		SpriteRessource bottom = ressourcesManager.new SpriteRessource("blockLinkingBookSide.png", RessourcesManager.TERRAIN_SPRITE);
		blockLinkingBook = new BlockLinkingBook(233, 233, top, side, bottom, this);
		blockLinkingBook.renderType = ModLoader.getUniqueBlockModelID(this, false);
		
		// Execute the following private method:
		// Block.fire.setBurnRate(Block.bookShelf.blockID, 70, 100);
		PrivateAccesses.BlockFire_chanceToEncourageFire.getFrom(Block.fire)[blockLinkingBook.blockID] = 70;
		PrivateAccesses.BlockFire_abilityToCatchFire.getFrom(Block.fire)[blockLinkingBook.blockID] = 100;
		
		SpriteRessource icon = ressourcesManager.new SpriteRessource("iconLinkingBook.png", RessourcesManager.ITEMS_SPRITE);
		SpriteRessource unwritten = ressourcesManager.new SpriteRessource("iconLinkingBookUnwritten.png", RessourcesManager.ITEMS_SPRITE);
		SpriteRessource pages = ressourcesManager.new SpriteRessource("iconLinkingBookPages.png", RessourcesManager.ITEMS_SPRITE);
		SpriteRessource unwrittenPages = ressourcesManager.new SpriteRessource("iconLinkingBookUnwrittenPages.png", RessourcesManager.ITEMS_SPRITE);
		itemBlockLinkingBook = new ItemBlockLinkingBook(233 - 256, icon, unwritten, pages, unwrittenPages, this);
		
		SpriteRessource page = ressourcesManager.new SpriteRessource("iconPage.png", RessourcesManager.ITEMS_SPRITE);
		itemPage = new ItemPage(3233, page);
		
		texture_tempLinkGUI = ressourcesManager.new TextureRessource("tempLinkGUI-BW.png");
		texture_tempWriteGUI = ressourcesManager.new TextureRessource("tempWriteGUI.png");
		texture_tempLookGUI = ressourcesManager.new TextureRessource("tempLookGUI.png");
		texture_tempLinkingBook3D = ressourcesManager.new TextureRessource("tempLinkingBook3D.png");
		
		missingLinkingPanelImage = ressourcesManager.new ImageRefRessource("missingLinkingPanelImage.png", itm.registerImage(null));
		
		ModLoader.addName(blockLinkingBook, "Linking Book");
		ModLoader.addName(itemBlockLinkingBook, "Linking Book");
		
		ModLoader.registerTileEntity(TileEntityLinkingBook.class, "LinkingBook", new RenderLinkingBook(this));
		
		ModLoader.addRecipe(new ItemStack(itemBlockLinkingBook, 1, 0), new Object[] { "#", "#", Character.valueOf('#'), Item.paper });
		
		linkingsound = ressourcesManager.new SoundRessource("mystlinkingbook/", "linkingsound");
		
		ressources.add(blockLinkingBook.topSprite);
		ressources.add(blockLinkingBook.sideSprite);
		ressources.add(blockLinkingBook.bottomSprite);
		ressources.add(itemBlockLinkingBook.icon);
		ressources.add(itemBlockLinkingBook.unwritten);
		ressources.add(itemBlockLinkingBook.pages);
		ressources.add(itemBlockLinkingBook.unwrittenPages);
		ressources.add(itemPage.pageSprite);
		ressources.add(blockLinkingBook.topSprite);
		ressources.add(blockLinkingBook.sideSprite);
		ressources.add(blockLinkingBook.bottomSprite);
		ressources.add(texture_tempLinkGUI);
		ressources.add(texture_tempWriteGUI);
		ressources.add(texture_tempLookGUI);
		ressources.add(texture_tempLinkingBook3D);
		ressources.add(missingLinkingPanelImage);
		ressources.add(linkingsound);
		for (Ressource ressource : ressources) {
			ressource.load();
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
		
		ressourcesManager.world.piece = worldFolderName + "/";
		
		settings.load();
		ressourcesManager.assets_world.setDisabled(!settings.allowWorldAssets);
		if (!settings.allowWorldAssets) {
			System.out.println("World assets disabled.");
		}
		
		for (Ressource ressource : ressources) {
			ressource.load();
		}
		
		try {
			linkingBook.agesManager.load();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
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
		if (id > 0) {
			texturesPool.add(id);
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
		// Preparing the variables:
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
