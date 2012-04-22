package net.minecraft.src.mystlinkingbook;

import java.awt.image.BufferedImage;

import net.minecraft.src.Chunk;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.World;

/**
 * 
 * @author ziliss
 * @since 0.7b
 */
public class GuiAfterLinking extends GuiScreen {
	
	/**
	 * Reference to the mod instance.
	 */
	public Mod_MystLinkingBook mod_MLB;
	
	/**
	 * The player opening the GUI.
	 */
	public EntityPlayer entityplayer;
	
	/**
	 * The {@code TileEntity} of the linking book {@code Block}.
	 */
	public TileEntityLinkingBook tileEntityLinkingBook;
	
	public boolean updateLinkingImage;
	
	public PositionKeeper positionKeeper;
	
	public int ticksSinceLinking = 0;
	public int maxTicksSinceLinking = 2 * 20;
	
	public GuiAfterLinking(EntityPlayer entityplayer, TileEntityLinkingBook tileEntityLinkingBook, Mod_MystLinkingBook mod_MLB) {
		this(entityplayer, tileEntityLinkingBook, false, mod_MLB);
	}
	
	public GuiAfterLinking(EntityPlayer entityplayer, TileEntityLinkingBook tileEntityLinkingBook, boolean updateLinkingImage, Mod_MystLinkingBook mod_MLB) {
		this.entityplayer = entityplayer;
		this.mod_MLB = mod_MLB;
		this.tileEntityLinkingBook = tileEntityLinkingBook;
		this.updateLinkingImage = updateLinkingImage;
		
		positionKeeper = new PositionKeeper(entityplayer, false, mod_MLB);
	}
	
	@Override
	public void initGui() {
		positionKeeper.start();
		mod_MLB.mc.mouseHelper.grabMouseCursor();
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
	
	@Override
	protected void mouseClicked(int i, int j, int k) {
		return;
	}
	
	@Override
	protected void keyTyped(char c, int i) {
		return;
	}
	
	@Override
	public void onGuiClosed() {
		positionKeeper.stop();
		mod_MLB.mc.mouseHelper.ungrabMouseCursor();
	}
	
	@Override
	public void updateScreen() {
		ticksSinceLinking++;
		if (ticksSinceLinking == maxTicksSinceLinking) {
			if (updateLinkingImage) {
				updateLinkingImage();
			}
			mc.displayGuiScreen(null);
		}
	}
	
	public void updateLinkingImage() {
		BufferedImage linkingPanelImage = mod_MLB.itm.takeImageFromScreen();
		
		World world = tileEntityLinkingBook.worldObj;
		Chunk chunk = world.getChunkFromBlockCoords(tileEntityLinkingBook.xCoord, tileEntityLinkingBook.zCoord); // Load or generate the needed chunk.
		
		tileEntityLinkingBook.linkingBook.setLinkingPanelImage(linkingPanelImage);
		
		chunk.isModified = true; // To force saving
		
		if (world != entityplayer.worldObj) {
			world.dropOldChunks();
			while (!world.quickSaveWorld(-1)) { // Until all chunks have been saved
			}
			mc.getSaveLoader().flushCache();
			mod_MLB.linkingBookUtils.lastUsedWorld = world;
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);
		// System.out.println(entityplayer.hurtTime);
		if (ticksSinceLinking < maxTicksSinceLinking) {
			int alpha = (int)((maxTicksSinceLinking - (ticksSinceLinking + f)) * 0xff) / maxTicksSinceLinking;
			drawRect(0, 0, width, height, alpha * 0x01000000);
		}
	}
}
