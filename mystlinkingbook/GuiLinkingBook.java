package net.minecraft.src.mystlinkingbook;

import java.awt.Color;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiTextField;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NBTTagCompound;

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
	
	/**
	 * The {@code TileEntity} of the linking book {@code Block}.
	 */
	public TileEntityLinkingBook tileEntityLinkingBook;
	
	/**
	 * The datas for this Linking Book Block.
	 */
	public NBTTagCompound nbttagcompound_linkingBook;
	
	boolean editName;
	GuiTextField nameTextfield;
	String savedName;
	int savedNameWidth;
	
	String missingPagesStr = null;
	int missingPagesStrWidth;
	
	LinkingPanel linkingPanel;
	public GuiLinkingPanel guiLinkingPanel;
	
	// Using coordinates relative to those to draw everything:
	int bookLeft;
	int bookTop;
	int bookWidth = 252;
	int bookHeight = 180;
	
	public Color pagesColor;
	int pagesLeft = 5;
	int pagesTop = 5;
	int pagesWidth = 242;
	int pagesHeight = 170;
	
	/**
	 * -1 means not started
	 */
	public int ticksBeforeLinking = -1;
	public int defaultTicksBeforeLinking = 2 * 20;
	public int maxTicksBeforeLinking;
	
	public PositionKeeper positionKeeper;
	
	public boolean runGC = false;
	
	public GuiLinkingBook(EntityPlayer entityplayer, TileEntityLinkingBook tileEntityLinkingBook, Mod_MystLinkingBook mod_MLB) {
		this.entityplayer = entityplayer;
		this.mod_MLB = mod_MLB;
		this.tileEntityLinkingBook = tileEntityLinkingBook;
		this.nbttagcompound_linkingBook = tileEntityLinkingBook.nbttagcompound_linkingBook;
	}
	
	@Override
	public void initGui() {
		bookLeft = (width - bookWidth) / 2;
		bookTop = (height - bookHeight) / 2;
		
		if (linkingPanel == null) {
			linkingPanel = tileEntityLinkingBook.linkingPanel.acquireLinkingPanel();
			if (!tileEntityLinkingBook.getLinksToDifferentAge()) { // TODO: remove this for 1.0 release
				mc.ingameGUI.addChatMessage("Cannot link to the same Age (Check your Ages areas ?)");
			}
		}
		
		controlList.clear();
		guiLinkingPanel = new GuiLinkingPanel(1, bookLeft + 149, bookTop + 21, 80, 60, linkingPanel, this);
		controlList.add(guiLinkingPanel);
		
		savedName = mod_MLB.linkingBook.getName(nbttagcompound_linkingBook);
		nameTextfield = new GuiTextField(fontRenderer, bookLeft + 12, bookTop + 26, 105, 14);
		nameTextfield.setMaxStringLength(16);
		nameTextfield.setText(savedName);
		if (savedName.isEmpty() && tileEntityLinkingBook.missingPages == 0) {
			editName = true;
			nameTextfield.func_50033_b(true); // Was setFocused(boolean b) before MC 1.2.4
		}
		savedNameWidth = fontRenderer.getStringWidth(savedName);
		
		notifyNbMissingPagesChanged();
		
		notifyColorChanged();
		
		guiLinkingPanel.initGui();
		
		tileEntityLinkingBook.guiLinkingBook = this;
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
		if (editName && PrivateAccesses.GuiTextField_isEnabled.getFrom(nameTextfield) && nameTextfield.func_50025_j()) { // For: isFocused()
			if (i == 28 || i == 156) {
				saveName();
			}
			else {
				nameTextfield.func_50037_a(c, i); // Was textboxKeyTyped(char c, int i) before MC 1.2.4
			}
		}
		else if (i == mc.gameSettings.keyBindInventory.keyCode) {
			mc.displayGuiScreen(null);
			mc.setIngameFocus();
		}
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if (guibutton == guiLinkingPanel && guiLinkingPanel.canLink()) {
			maxTicksBeforeLinking = defaultTicksBeforeLinking;
			runGC = mod_MLB.linkingBook.doLinkChangesDimension(nbttagcompound_linkingBook, entityplayer);
			
			if (runGC) {
				maxTicksBeforeLinking -= 4; // 0.2 seconds
			}
			
			ticksBeforeLinking = maxTicksBeforeLinking;
			
			guiLinkingPanel.startLinking();
			
			if (editName) {
				nameTextfield.func_50033_b(false); // Was setFocused(boolean b) before MC 1.2.4
				saveName();
			}
			
			mod_MLB.linkingBook.prepareLinking(nbttagcompound_linkingBook, entityplayer);
			
			Mod_MystLinkingBook.playSoundFX(mod_MLB.linkingsound.soundId, 1.0F, 1.0F);
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
		saveName();
		if (positionKeeper != null) {
			positionKeeper.stop();
		}
		guiLinkingPanel.onGuiClosed();
		linkingPanel.releaseLinkingPanel();
		if (tileEntityLinkingBook.guiLinkingBook == this) {
			tileEntityLinkingBook.guiLinkingBook = null;
		}
	}
	
	public void saveName() {
		if (editName) {
			String name = nameTextfield.getText();
			if (!name.isEmpty()) {
				if (!name.equals(savedName) && mod_MLB.linkingBook.setName(nbttagcompound_linkingBook, name)) {
					savedName = name;
				}
				savedNameWidth = fontRenderer.getStringWidth(savedName);
				nameTextfield.func_50033_b(false); // Was setFocused(boolean b) before MC 1.2.4
				editName = false;
			}
		}
	}
	
	public void notifyNbMissingPagesChanged() {
		int nbPages = tileEntityLinkingBook.nbPages;
		int maxPages = tileEntityLinkingBook.maxPages;
		int missingPages = tileEntityLinkingBook.missingPages;
		if (maxPages > 0) {
			missingPagesStr = (missingPages == 0 ? "" : nbPages + "/") + maxPages + " page" + (maxPages > 1 ? "s" : "");
		}
		else {
			missingPagesStr = "";
		}
		missingPagesStrWidth = fontRenderer.getStringWidth(missingPagesStr);
		
		if (editName && missingPages > 0) {
			nameTextfield.func_50033_b(false); // Was setFocused(boolean b) before MC 1.2.4
			editName = false;
		}
	}
	
	@Override
	public void updateScreen() {
		if (ticksBeforeLinking != -1) {
			ticksBeforeLinking--;
			
			if (ticksBeforeLinking == 1 && runGC) {
				runGC = false;
				// Shortens the System.gc() run after teleporting to another dimension. See end of: Minecraft.changeWorld()
				System.gc();
			}
			else if (ticksBeforeLinking == 0) {
				mc.displayGuiScreen(null);
				boolean linked = mod_MLB.linkingBook.link(nbttagcompound_linkingBook, entityplayer);
				if (linked) {
					ModLoader.openGUI(entityplayer, new GuiAfterLinking(entityplayer, tileEntityLinkingBook, mod_MLB));
				}
			}
		}
	}
	
	public void notifyColorChanged() {
		pagesColor = ItemPage.brighterColorTable[nbttagcompound_linkingBook.getInteger("color")];
	}
	
	public void notifyLinkingPanelImageChanged(LinkingPanel linkingPanel) {
		guiLinkingPanel.notifyLinkingPanelImageChanged(linkingPanel);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		drawDefaultBackground();
		
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(mod_MLB.texture_tempLinkGUI.textureId);
		drawTexturedModalRect(bookLeft, bookTop, 0, 0, bookWidth, bookHeight);
		
		GL11.glColor3ub((byte)pagesColor.getRed(), (byte)pagesColor.getGreen(), (byte)pagesColor.getBlue());
		drawTexturedModalRect(bookLeft + pagesLeft, bookTop + pagesTop, pagesLeft, pagesTop, pagesWidth, pagesHeight);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		
		if (editName) {
			// fontRenderer.drawString("Name:", bookLeft + 15, bookTop + 25, 0x000000);
			nameTextfield.drawTextBox();
		}
		else {
			fontRenderer.drawString(savedName, bookLeft + 64 - savedNameWidth / 2, bookTop + 29, 0x000000);
			// drawString(fontRenderer, "Text", width / 2 - 82, height / 2 - 75, 0x000000);
		}
		
		fontRenderer.drawString(missingPagesStr, bookLeft + 64 - missingPagesStrWidth / 2, bookTop + 150, 0x000000);
		
		super.drawScreen(mouseX, mouseY, f);
		
		if (ticksBeforeLinking != -1) {
			int alpha = (int)((maxTicksBeforeLinking - (ticksBeforeLinking - f)) * 0xff) / maxTicksBeforeLinking;
			drawRect(0, 0, width, height, alpha * 0x01000000);
		}
	}
}
