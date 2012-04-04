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
	
	public LinkingPanel linkingPanel = null;
	
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
		prevShowLinkCursor = false;
	}
	
	public void onGuiClosed() {
		ModLoader.getMinecraftInstance().mcCanvas.setCursor(originalCursor);
	}
	
	public void notifyLinkingPanelImageChanged(LinkingPanel linkingPanel) {
		this.linkingPanel = linkingPanel;
	}
	
	/**
	 * True if the linking panel is ready to link.
	 */
	public boolean canLink() {
		return guiLinkingBook.tileEntityLinkingBook.canLink() && !linkingStarted;
	}
	
	public void startLinking() {
		linkingStarted = true;
	}
	
	@Override
	public void drawButton(Minecraft minecraft, int x, int y) {
		if (!drawButton) return;
		
		boolean isCurrentlyHover = x >= xPosition && y >= yPosition && x < xPosition + width && y < yPosition + height;
		boolean showLinkCursor = canLink() && getHoverState(isCurrentlyHover) == 2;
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
