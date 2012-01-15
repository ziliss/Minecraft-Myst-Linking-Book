package net.minecraft.src;

import java.awt.Cursor;

import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;

/**
 * Represents and manages the state of a linking panel.
 * 
 * @author ziliss
 * @see GuiLinkingBook
 * @since 0.2a
 */
public class GuiButtonLinkingPanel extends GuiButton {
	
	/**
	 * The linking book of this linking panel.
	 */
	GuiLinkingBook guiLinkingBook;
	
	/**
	 * Does the linking book need power to ba able to link ?
	 */
	public boolean isUnstable;
	
	/**
	 * Whether the Block of the linking book is powered.
	 */
	public boolean isPowered;
	
	/**
	 * True if the linking book is ready to link.
	 */
	public boolean canLink;
	
	/**
	 * True when the linking panel has been clicked and the linking has started.
	 */
	public boolean linkingStarted = false;
	
	// The following fields are used to manage the mouse cursor:
	boolean prevShowLinkCursor;
	Cursor originalCursor;
	static Cursor linkCursor = new Cursor(Cursor.HAND_CURSOR);
	
	public GuiButtonLinkingPanel(int id, int xPosition, int yPosition, int width, int height, GuiLinkingBook guiLinkingBook) {
		super(id, xPosition, yPosition, width, height, "");
		this.guiLinkingBook = guiLinkingBook;
		isUnstable = guiLinkingBook.mod_MLB.linkingBook.isUnstable(guiLinkingBook.nbttagcompound_linkingBook);
	}
	
	public void initGui() {
		isPowered = guiLinkingBook.tileEntityLinkingBook.isPowered;
		canLink = guiLinkingBook.missingPages == 0 && (isUnstable ? isPowered : true);
		prevShowLinkCursor = false;
	}
	
	public void onGuiClosed() {
		canLink = false;
		ModLoader.getMinecraftInstance().mcCanvas.setCursor(originalCursor);
	}
	
	public void notifyPowerStateChanged(boolean isPowered) {
		this.isPowered = isPowered;
		canLink = guiLinkingBook.missingPages == 0 && (isUnstable ? isPowered : true) && !linkingStarted;
	}
	
	public boolean canLink() {
		return canLink;
	}
	
	public void startLinking() {
		linkingStarted = true;
		canLink = false;
	}
	
	@Override
	public void drawButton(Minecraft minecraft, int x, int y) {
		if (!drawButton) return;
		
		boolean isCurrentlyHover = x >= xPosition && y >= yPosition && x < xPosition + width && y < yPosition + height;
		boolean showLinkCursor = canLink && getHoverState(isCurrentlyHover) == 2;
		if (prevShowLinkCursor) {
			if (!showLinkCursor) {
				minecraft.mcCanvas.setCursor(originalCursor);
				prevShowLinkCursor = false;
			}
		}
		else {
			if (showLinkCursor) {
				originalCursor = minecraft.mcCanvas.getCursor();
				minecraft.mcCanvas.setCursor(linkCursor);
				prevShowLinkCursor = true;
			}
		}
		
		minecraft.renderEngine.bindTexture(minecraft.renderEngine.getTexture("/mystlinkingbook/tempPanel.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		drawTexturedModalRect(xPosition, yPosition, 108, 36, width, height);
		
		// drawRect(xPosition, yPosition, width, height, 0x88ff0000);
	}
}
