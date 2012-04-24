package net.minecraft.src.mystlinkingbook;

import java.awt.Color;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiTextField;
import net.minecraft.src.ModLoader;

import org.lwjgl.opengl.GL11;

/**
 * 
 * 
 * @author ziliss
 * @see
 * @since 0.2a
 */
public class GuiLinkingBook extends GuiScreen {
	
	/**
	 * Reference to the mod instance.
	 */
	public Mod_MystLinkingBook mod_MLB;
	
	/**
	 * The player opening the GUI.
	 */
	public EntityPlayer entityplayer;
	
	public LinkingBook linkingBook;
	
	public LinkingBookListener linkingBookListener;
	
	public TileEntityLinkingBook tileEntityLinkingBook;
	
	protected boolean editName;
	protected GuiTextField nameTextfield;
	protected String savedName;
	protected int savedNameWidth;
	
	protected String missingPagesStr = null;
	protected int missingPagesStrWidth;
	
	protected LinkingPanel linkingPanel;
	public GuiLinkingPanel guiLinkingPanel;
	
	// Using coordinates relative to those to draw everything:
	protected int bookLeft;
	protected int bookTop;
	protected int bookWidth = 252;
	protected int bookHeight = 180;
	
	protected int pagesLeft = 5;
	protected int pagesTop = 5;
	protected int pagesWidth = 242;
	protected int pagesHeight = 170;
	
	/**
	 * -1 means not started
	 */
	public int ticksBeforeLinking = -1;
	public int defaultTicksBeforeLinking = 2 * 20;
	public int maxTicksBeforeLinking;
	
	public PositionKeeper positionKeeper;
	
	public GuiLinkingBook(EntityPlayer entityplayer, LinkingBook linkingBook, TileEntityLinkingBook tileEntityLinkingBook, Mod_MystLinkingBook mod_MLB) {
		this.entityplayer = entityplayer;
		this.mod_MLB = mod_MLB;
		this.linkingBook = linkingBook;
		this.tileEntityLinkingBook = tileEntityLinkingBook;
		
		// Uncomment this to debug panel image creation:
		// linkingBook.setLinkingPanelImage(mod_MLB.itm.takeImageFromScreen());
	}
	
	@Override
	public void initGui() {
		bookLeft = (width - bookWidth) / 2;
		bookTop = (height - bookHeight) / 2;
		
		if (linkingBookListener == null) {
			linkingBookListener = new LinkingBookListener() {
				@Override
				public void notifyNbMissingPagesChanged(int nbPages, int nbMissingPages, int maxPages) {
					if (maxPages > 0) {
						missingPagesStr = (nbMissingPages == 0 ? "" : nbPages + "/") + maxPages + " page" + (maxPages > 1 ? "s" : "");
					}
					else {
						missingPagesStr = "";
					}
					missingPagesStrWidth = fontRenderer.getStringWidth(missingPagesStr);
					
					if (editName && nbMissingPages > 0) {
						nameTextfield.setFocused(false);
						editName = false;
					}
				}
			};
			linkingBook.addListener(linkingBookListener);
		}
		
		if (linkingPanel == null) {
			linkingPanel = linkingBook.linkingPanel.acquireImage();
			if (!linkingBook.doLinkToDifferentAge()) { // TODO: remove these messages for 1.0 release ?
				int destX = (int)Math.floor(linkingBook.getNBTTagCompound().getDouble("destX"));
				int destY = (int)Math.floor(linkingBook.getNBTTagCompound().getDouble("destY"));
				int destZ = (int)Math.floor(linkingBook.getNBTTagCompound().getDouble("destZ"));
				int destDim = linkingBook.getNBTTagCompound().getInteger("destDim");
				AgeArea ageDest = mod_MLB.linkingBookUtils.agesManager.getFirstReadyAgeAreaContaining(destX, destY, destZ, destDim);
				int destAgeId = ageDest == null ? -1 : ageDest.id;
				String src = linkingBook.bookX + " " + linkingBook.bookY + " " + linkingBook.bookZ;
				String dest = destX + " " + destY + " " + destZ;
				switch (mod_MLB.linkingBookUtils.agesManager.getTypeOfLinking(linkingBook.bookX, linkingBook.bookY, linkingBook.bookZ, linkingBook.bookDim, destX, destY, destZ, destDim)) {
					case sameAgeArea:
						mc.ingameGUI.addChatMessage("Cannot link to the same Age you are in (from: " + src + ", dest: " + dest + (destAgeId != -1 ? ", both in Age area id: " + destAgeId : "") + ")");
						break;
					case sameAgeDim:
						mc.ingameGUI.addChatMessage("Cannot link to the same Age you are in (the whole current dimension is an Age)");
						break;
					case outOfAgeAreaInSameDim:
					case outOfAgeAreaInDifferentDim:
						mc.ingameGUI.addChatMessage("Cannot link outside of an Age area (dest: " + dest + ")");
				}
			}
		}
		
		controlList.clear();
		guiLinkingPanel = new GuiLinkingPanel(1, bookLeft + 149, bookTop + 21, 80, 60, linkingPanel, this);
		controlList.add(guiLinkingPanel);
		
		savedName = linkingBook.getName();
		nameTextfield = new GuiTextField(fontRenderer, bookLeft + 12, bookTop + 26, 105, 14);
		nameTextfield.setMaxStringLength(16);
		nameTextfield.setText(savedName);
		if (savedName.isEmpty() && linkingBook.getNbMissingPages() == 0) {
			editName = true;
			nameTextfield.setFocused(true);
		}
		savedNameWidth = fontRenderer.getStringWidth(savedName);
		
		linkingBookListener.notifyNbMissingPagesChanged(linkingBook.getNbPages(), linkingBook.getNbMissingPages(), linkingBook.getMaxPages());
		
		guiLinkingPanel.initGui();
	}
	
	@Override
	protected void mouseClicked(int i, int j, int k) {
		if (ticksBeforeLinking != -1) return;
		// The following part is taken from GuiScreen.mouseClicked(...):
		if (k == 0) {
			for (int l = 0; l < controlList.size(); l++) {
				GuiButton guibutton = (GuiButton)controlList.get(l);
				if (guibutton.mousePressed(mc, i, j)) {
					
					PrivateAccesses.GuiScreen_selectedButton.setTo(this, guibutton);
					
					// This condition is added:
					if (guibutton != guiLinkingPanel) {
						mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
					}
					actionPerformed(guibutton);
				}
			}
		}
		// End of the part from GuiScreen.mouseClicked(...).
		
		if (editName) {
			nameTextfield.mouseClicked(i, j, k);
		}
	}
	
	@Override
	protected void keyTyped(char c, int i) {
		if (ticksBeforeLinking != -1) return;
		super.keyTyped(c, i);
		if (editName && PrivateAccesses.GuiTextField_isEnabled.getFrom(nameTextfield) && nameTextfield.getIsFocused()) {
			if (i == 28 || i == 114) { // Return || Enter
				saveName();
				nameTextfield.setFocused(false);
			}
			else {
				nameTextfield.textboxKeyTyped(c, i);
			}
		}
		else if (i == mc.gameSettings.keyBindInventory.keyCode) {
			saveName();
			mc.displayGuiScreen(null);
			mc.setIngameFocus();
		}
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if (guibutton == guiLinkingPanel && guiLinkingPanel.canLink()) {
			maxTicksBeforeLinking = defaultTicksBeforeLinking;
			
			if (linkingBook.doLinkChangesDimension(entityplayer)) {
				maxTicksBeforeLinking -= 6; // - 0.3 seconds
			}
			
			ticksBeforeLinking = maxTicksBeforeLinking;
			
			guiLinkingPanel.startLinking();
			
			if (editName) {
				saveName();
				nameTextfield.setFocused(false);
			}
			
			linkingBook.prepareLinking(entityplayer);
			
			Mod_MystLinkingBook.playSoundFX(mod_MLB.linkingsound.getSoundId(), 1.0F, 1.0F);
			// ModLoader.getMinecraftInstance().sndManager.playSoundFX("mystlinkingbook.linkingsound", 1.0F, 1.0F);
			// ModLoader.getMinecraftInstance().sndManager.playSound("mystlinkingbook.linkingsound", (float)entityplayer.posX, (float)entityplayer.posY, (float)entityplayer.posZ, 1.0F, 1.0F);
			// entityplayer.worldObj.playSoundAtEntity(entityplayer, "mystlinkingbook.linkingsound", 1.0F, 1.0F);
			
			positionKeeper = new PositionKeeper(entityplayer, false, mod_MLB);
			positionKeeper.start();
		}
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
	
	@Override
	public void onGuiClosed() {
		if (positionKeeper != null) {
			positionKeeper.stop();
		}
		guiLinkingPanel.onGuiClosed();
		linkingPanel.releaseImage();
		if (linkingBookListener != null) {
			linkingBook.removeListener(linkingBookListener);
		}
	}
	
	public void saveName() {
		if (editName) {
			String name = nameTextfield.getText();
			if (!name.isEmpty()) {
				if (!name.equals(savedName) && linkingBook.setName(name)) {
					savedName = name;
				}
				savedNameWidth = fontRenderer.getStringWidth(savedName);
				nameTextfield.setFocused(false);
				editName = false;
			}
		}
	}
	
	@Override
	public void updateScreen() {
		if (ticksBeforeLinking != -1) {
			ticksBeforeLinking--;
			
			if (ticksBeforeLinking == 0) {
				mc.displayGuiScreen(null);
				boolean linked = linkingBook.link(entityplayer);
				if (linked) {
					ModLoader.openGUI(entityplayer, new GuiAfterLinking(entityplayer, tileEntityLinkingBook, mod_MLB));
				}
			}
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		drawDefaultBackground();
		
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(mod_MLB.texture_guiLinkingBook.getTextureId());
		drawTexturedModalRect(bookLeft, bookTop, 0, 0, bookWidth, bookHeight);
		
		Color brighterColorObj = linkingBook.getBrighterColorObj();
		GL11.glColor3ub((byte)brighterColorObj.getRed(), (byte)brighterColorObj.getGreen(), (byte)brighterColorObj.getBlue());
		drawTexturedModalRect(bookLeft + pagesLeft, bookTop + pagesTop, pagesLeft, pagesTop, pagesWidth, pagesHeight);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		
		if (editName) {
			// fontRenderer.drawString("Name:", bookLeft + 15, bookTop + 25, 0x000000);
			nameTextfield.drawTextBox();
		}
		else {
			fontRenderer.drawString(savedName, bookLeft + 64 - savedNameWidth / 2, bookTop + 29, 0x000000);
		}
		
		fontRenderer.drawString(missingPagesStr, bookLeft + 64 - missingPagesStrWidth / 2, bookTop + 150, 0x000000);
		
		super.drawScreen(mouseX, mouseY, f);
		
		if (ticksBeforeLinking != -1) {
			int alpha = (int)((maxTicksBeforeLinking - (ticksBeforeLinking - f)) * 0xff) / maxTicksBeforeLinking;
			drawRect(0, 0, width, height, alpha * 0x01000000);
		}
	}
}
