package net.minecraft.src.mystlinkingbook;

import java.awt.Cursor;

import net.minecraft.client.Minecraft;
import net.minecraft.src.GuiButton;
import net.minecraft.src.ModLoader;

/**
 * Represents and manages the state of a linking panel.
 * 
 * @author ziliss
 * @see GuiLinkingBook
 * @since 0.2a
 */
public class GuiLinkingPanel extends GuiButton {
	
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
	
	public LinkingPanel linkingPanel = null;
	
	/**
	 * True if the linking book links to another Age.
	 */
	public boolean linksToDifferentAge;
	
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
	
	public GuiLinkingPanel(int id, int xPosition, int yPosition, int width, int height, LinkingPanel linkingPanel, GuiLinkingBook guiLinkingBook) {
		super(id, xPosition, yPosition, width, height, "");
		this.linkingPanel = linkingPanel;
		this.guiLinkingBook = guiLinkingBook;
	}
	
	public void initGui() {
		updateCanLink();
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
	
	public void notifyLinkingPanelImageChanged(LinkingPanel linkingPanel) {
		this.linkingPanel = linkingPanel;
	}
	
	public boolean canLink() {
		return canLink && linksToDifferentAge;
	}
	
	public boolean updateCanLink() {
		linksToDifferentAge = guiLinkingBook.mod_MLB.linkingBook.doLinkToDifferentAge(guiLinkingBook.tileEntityLinkingBook, guiLinkingBook.entityplayer);
		isUnstable = guiLinkingBook.mod_MLB.linkingBook.isUnstable(guiLinkingBook.nbttagcompound_linkingBook);
		isPowered = guiLinkingBook.tileEntityLinkingBook.isPowered;
		canLink = guiLinkingBook.missingPages == 0 && (isUnstable ? isPowered : true) && !linkingStarted;
		return canLink();
	}
	
	public void startLinking() {
		linkingStarted = true;
		canLink = false;
	}
	
	@Override
	public void drawButton(Minecraft minecraft, int x, int y) {
		if (!drawButton) return;
		
		boolean isCurrentlyHover = x >= xPosition && y >= yPosition && x < xPosition + width && y < yPosition + height;
		boolean showLinkCursor = canLink && linksToDifferentAge && getHoverState(isCurrentlyHover) == 2;
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
		
		linkingPanel.drawOnGui(xPosition, yPosition, 80, 60);
	}
}
