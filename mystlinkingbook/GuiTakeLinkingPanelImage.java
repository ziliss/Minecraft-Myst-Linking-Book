package net.minecraft.src.mystlinkingbook;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.NBTTagCompound;

import org.lwjgl.opengl.GL11;

/**
 * 
 * @author ziliss
 * @since 0.7b
 */
public class GuiTakeLinkingPanelImage extends GuiAfterLinking {
	
	public NBTTagCompound nbttagcompound_linkBack;
	
	protected GuiTakeLinkingPanelImage(EntityPlayer entityplayer, TileEntityLinkingBook tileEntityLinkingBook, NBTTagCompound nbttagcompound_linkBack, Mod_MystLinkingBook mod_MLB) {
		super(entityplayer, tileEntityLinkingBook, mod_MLB);
		
		this.nbttagcompound_linkBack = nbttagcompound_linkBack;
		maxTicksSinceLinking = -1;
		
	}
	
	@Override
	protected void mouseClicked(int i, int j, int k) {
		updateLinkingImage();
		mc.displayGuiScreen(null);
	}
	
	@Override
	protected void keyTyped(char c, int i) {
		if (i == 1) { // Esc
			mc.displayGuiScreen(null);
		}
	}
	
	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		mod_MLB.linkingBookUtils.link(nbttagcompound_linkBack, entityplayer);
		tileEntityLinkingBook.setBookSpread(1f);
	}
	
	public static void startTakeLinkingPanelImage(EntityPlayer entityplayer, TileEntityLinkingBook tileEntityLinkingBook, Mod_MystLinkingBook mod_MLB) {
		NBTTagCompound nbttagcompound_linkBack = mod_MLB.linkingBookUtils.createNew();
		mod_MLB.linkingBookUtils.write(nbttagcompound_linkBack, entityplayer, 0, false);
		
		// Apparently it is not always disabled at this point (Causes the text on the screen to be grey):
		boolean GL_LIGHTING_enabled = GL11.glIsEnabled(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_LIGHTING);
		
		boolean linked = tileEntityLinkingBook.linkingBook.link(entityplayer);
		
		if (GL_LIGHTING_enabled) {
			GL11.glEnable(GL11.GL_LIGHTING);
		}
		
		if (linked) {
			mod_MLB.mc.displayGuiScreen(new GuiTakeLinkingPanelImage(entityplayer, tileEntityLinkingBook, nbttagcompound_linkBack, mod_MLB));
		}
	}
}
