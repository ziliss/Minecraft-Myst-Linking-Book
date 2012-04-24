package net.minecraft.src.mystlinkingbook;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

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
import net.minecraft.src.mystlinkingbook.ResourcesManager.ImageRefResource;
import net.minecraft.src.mystlinkingbook.ResourcesManager.Resource;
import net.minecraft.src.mystlinkingbook.ResourcesManager.SoundRessource;
import net.minecraft.src.mystlinkingbook.ResourcesManager.SpriteResource;
import net.minecraft.src.mystlinkingbook.ResourcesManager.TextureResource;

import org.lwjgl.opengl.GL11;

import paulscode.sound.SoundSystem;

public class Mod_MystLinkingBook extends BaseMod {
	
	public Minecraft mc;
	
	public Settings settings;
	
	public ScheduledActionsManager scheduledActionsManager;
	
	public ImagesOnTextureManager itm;
	
	public ResourcesManager ressourcesManager = new ResourcesManager(this);
	public ArrayList<Resource> ressources = new ArrayList<Resource>();
	
	// Contains methods to interact with the datas of the Linking Books:
	public LinkingBookUtils linkingBookUtils;
	
	public BlockLinkingBook blockLinkingBook;
	public ItemBlockLinkingBook itemBlockLinkingBook;
	public ItemPage itemPage;
	
	public TextureResource texture_guiLinkingBook;
	public TextureResource texture_guiWriteLinkingBook;
	public TextureResource texture_guiLookOfLinkingBook;
	public TextureResource texture_modelLinkingBookCover;
	public TextureResource texture_modelLinkingBookPages;
	
	public ImageRefResource linkingPanelImageMissing;
	
	public SoundRessource linkingsound;
	
	public static Pattern coverNameFilterPattern = Pattern.compile("[^a-zA-Z0-9\\-_\\.]");
	public HashMap<String, TextureResource> covers = new HashMap<String, TextureResource>();
	
	public ResourcePath logAgesAreasModsPath;
	
	public AgeAreaOutline outlineAgeArea;
	
	public Mod_MystLinkingBook() {
	}
	
	@Override
	public String getVersion() {
		return "0.9.1b";
	}
	
	/**
	 * Load ressources and register all necessary elements.
	 * 
	 * Called when the mod is loaded.
	 */
	@Override
	public void load() {
		if (PrivateAccesses.hasMembersNotFound()) throw new RuntimeException("Some private members could not be found.");
		
		mc = ModLoader.getMinecraftInstance();
		
		scheduledActionsManager = new ScheduledActionsManager(this);
		ressourcesManager.init();
		logAgesAreasModsPath = new ResourcePath(ressourcesManager.worldMLB, "/importantChanges.log");
		FBO.load();
		itm = new ImagesOnTextureManager(256, 256, 80, 60, this);
		
		ResourcePath basePropsPath = new ResourcePath(ressourcesManager.configMLB, "options.properties");
		ResourcePath worldPropsPath = new ResourcePath(ressourcesManager.worldMLB, "options.properties");
		settings = new Settings(basePropsPath, worldPropsPath);
		
		linkingBookUtils = new LinkingBookUtils(this);
		
		outlineAgeArea = new AgeAreaOutline(this);
		
		SpriteResource top = ressourcesManager.new SpriteResource("blockLinkingBookTop.png", ResourcesManager.TERRAIN_SPRITE);
		SpriteResource side = ressourcesManager.new SpriteResource("blockLinkingBookSide.png", ResourcesManager.TERRAIN_SPRITE);
		SpriteResource bottom = ressourcesManager.new SpriteResource("blockLinkingBookBottom.png", ResourcesManager.TERRAIN_SPRITE);
		int renderType = ModLoader.getUniqueBlockModelID(this, false); // full3DItem = false: the item is not rendered as a 3D block, but as a flat image.
		blockLinkingBook = new BlockLinkingBook(233, 233, top, side, bottom, renderType, this);
		
		// Execute the following private method:
		// Block.fire.setBurnRate(Block.bookShelf.blockID, 70, 100);
		PrivateAccesses.BlockFire_chanceToEncourageFire.getFrom(Block.fire)[blockLinkingBook.blockID] = 70;
		PrivateAccesses.BlockFire_abilityToCatchFire.getFrom(Block.fire)[blockLinkingBook.blockID] = 100;
		
		SpriteResource icon = ressourcesManager.new SpriteResource("iconLinkingBook.png", ResourcesManager.ITEMS_SPRITE);
		SpriteResource unwritten = ressourcesManager.new SpriteResource("iconLinkingBookUnwritten.png", ResourcesManager.ITEMS_SPRITE);
		SpriteResource pages = ressourcesManager.new SpriteResource("iconLinkingBookPages.png", ResourcesManager.ITEMS_SPRITE);
		SpriteResource unwrittenPages = ressourcesManager.new SpriteResource("iconLinkingBookUnwrittenPages.png", ResourcesManager.ITEMS_SPRITE);
		itemBlockLinkingBook = new ItemBlockLinkingBook(233 - 256, icon, unwritten, pages, unwrittenPages, this);
		
		SpriteResource page = ressourcesManager.new SpriteResource("iconPage.png", ResourcesManager.ITEMS_SPRITE);
		itemPage = new ItemPage(3233, page);
		
		texture_guiLinkingBook = ressourcesManager.new TextureResource("guiLinkingBook.png");
		texture_guiWriteLinkingBook = ressourcesManager.new TextureResource("guiWriteLinkingBook.png");
		texture_guiLookOfLinkingBook = ressourcesManager.new TextureResource("guiLookOfLinkingBook.png");
		texture_modelLinkingBookCover = ressourcesManager.new TextureResource("modelLinkingBookCover.png");
		texture_modelLinkingBookPages = ressourcesManager.new TextureResource("modelLinkingBookPages.png");
		
		linkingPanelImageMissing = ressourcesManager.new ImageRefResource("linkingPanelImageMissing.png", itm.registerImage(null));
		
		ModLoader.addName(blockLinkingBook, "Linking Book");
		ModLoader.addName(itemBlockLinkingBook, "Linking Book");
		
		ModLoader.registerTileEntity(TileEntityLinkingBook.class, "LinkingBook", new RenderBlockLinkingBook());
		
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
		ressources.add(texture_guiLinkingBook);
		ressources.add(texture_guiWriteLinkingBook);
		ressources.add(texture_guiLookOfLinkingBook);
		ressources.add(texture_modelLinkingBookCover);
		ressources.add(texture_modelLinkingBookPages);
		ressources.add(linkingPanelImageMissing);
		ressources.add(linkingsound);
		for (Resource ressource : ressources) {
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
		for (TextureResource cover : covers.values()) {
			cover.clear();
		}
		covers.clear();
		
		ressourcesManager.startNewWorld(worldFolderName);
		
		settings.load();
		ressourcesManager.assets_world.setDisabled(!settings.allowWorldAssets);
		if (!settings.allowWorldAssets) {
			System.out.println("World assets disabled.");
		}
		
		for (Resource ressource : ressources) {
			ressource.load();
		}
		
		linkingBookUtils.startNewWorld();
		
		try {
			linkingBookUtils.agesManager.startNewWorld(ressourcesManager.world);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public boolean onTickInGame(float partialTick, Minecraft mc) {
		return scheduledActionsManager.OnTickInGame(partialTick, mc);
	}
	
	@Override
	public boolean renderWorldBlock(RenderBlocks renderblocks, IBlockAccess iblockaccess, int i, int j, int k, Block block, int l) {
		// Taken from: RenderBlocks.renderBlockByRenderType:
		// block.setBlockBoundsBasedOnState(iblockaccess, i, j, k);
		renderblocks.renderStandardBlock(block, i, j, k);
		return true;
	}
	
	public int allocateTextureId() {
		// Also see Minecraft's GLAllocation class
		return GL11.glGenTextures();
	}
	
	public void releasedTextureId(int texId) {
		if (texId > 0) {
			GL11.glDeleteTextures(texId);
		}
	}
	
	public TextureResource getCover(String name) {
		if (!name.isEmpty()) {
			name = coverNameFilterPattern.matcher(name).replaceAll("");
		}
		if (name.isEmpty()) return texture_modelLinkingBookCover;
		
		TextureResource cover = covers.get(name);
		if (cover == null) {
			cover = ressourcesManager.new TextureResource("modelLinkingBookCover-" + name + ".png");
			cover.setDefault(texture_modelLinkingBookCover);
			cover.load();
			covers.put(name, cover);
		}
		return cover;
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
	
	public void logImportantWorldChanges(String msg) {
		if (!settings.logImportantWorldChanges) return;
		
		// TODO: to be implemented
	}
}
