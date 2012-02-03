package net.minecraft.src.mystlinkingbook;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiTextField;
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
	
	public int missingPages;
	String missingPagesStr = null;
	int missingPagesStrWidth;
	
	public GuiLinkingPanel linkingPanel;
	
	// TODO: use coordinates relative to those to draw everything if possible:
	int bookLeft;
	int bookTop;
	int bookWidth;
	int bookHeight;
	
	/**
	 * -1 means not started, a value between 0 and 1 represents the current progress of the linking.<br>
	 * 
	 */
	public float linkingProgress = -1;
	public long linkingStartedTime;
	public float defaultLinkingDuration = 2000f; // 2 seconds. (float is important for division !)
	public float linkingDuration;
	
	public boolean runGC;
	
	public GuiLinkingBook(EntityPlayer entityplayer, TileEntityLinkingBook tileEntityLinkingBook, Mod_MystLinkingBook mod_MLB) {
		this.entityplayer = entityplayer;
		this.mod_MLB = mod_MLB;
		this.tileEntityLinkingBook = tileEntityLinkingBook;
		this.nbttagcompound_linkingBook = tileEntityLinkingBook.nbttagcompound_linkingBook;
	}
	
	@Override
	public void initGui() {
		linkingProgress = -1;
		runGC = false;
		
		controlList.clear();
		linkingPanel = new GuiLinkingPanel(1, width / 2 + 12, height / 2 - 70, 60, 48, this);
		controlList.add(linkingPanel);
		
		savedName = mod_MLB.linkingBook.getName(nbttagcompound_linkingBook);
		nameTextfield = new GuiTextField(this, fontRenderer, width / 2 - 82, height / 2 - 62, 68, 14, savedName);
		nameTextfield.setMaxStringLength(16);
		if (savedName.isEmpty()) {
			editName = true;
			nameTextfield.setFocused(true);
		}
		savedNameWidth = fontRenderer.getStringWidth(savedName);
		
		updateNbMissingPages();
		
		linkingPanel.initGui();
		
		tileEntityLinkingBook.guiLinkingBook = this;
	}
	
	public boolean checkLinksToDifferentAge() {
		return mod_MLB.linkingBook.doLinkToDifferentAge(tileEntityLinkingBook, entityplayer);
	}
	
	@Override
	protected void mouseClicked(int i, int j, int k) {
		if (linkingProgress != -1) return;
		// The following part is taken from GuiScreen.mouseClicked(...):
		if (k == 0) {
			for (int l = 0; l < controlList.size(); l++) {
				GuiButton guibutton = (GuiButton)controlList.get(l);
				if (guibutton.mousePressed(mc, i, j)) {
					
					PrivateAccesses.GuiScreen_selectedButton.setTo(this, guibutton);
					
					if (guibutton != linkingPanel) {
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
		if (linkingProgress != -1) return;
		super.keyTyped(c, i);
		if (editName && nameTextfield.isEnabled && nameTextfield.isFocused) {
			if (i == 28 || i == 156) {
				saveName();
			}
			else {
				nameTextfield.textboxKeyTyped(c, i);
			}
		}
		else if (i == mc.gameSettings.keyBindInventory.keyCode) {
			mc.displayGuiScreen(null);
			mc.setIngameFocus();
		}
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if (guibutton == linkingPanel && linkingPanel.updateCanLink()) {
			linkingProgress = 0;
			runGC = mod_MLB.linkingBook.doLinkChangesDimension(nbttagcompound_linkingBook, entityplayer);
			linkingDuration = defaultLinkingDuration;
			if (runGC) {
				linkingDuration -= 200;
			}
			
			linkingPanel.startLinking();
			
			if (editName) {
				nameTextfield.setFocused(false);
				saveName();
			}
			
			mod_MLB.linkingBook.prepareLinking(nbttagcompound_linkingBook, entityplayer);
			
			linkingStartedTime = System.currentTimeMillis();
			
			Mod_MystLinkingBook.playSoundFX("mystlinkingbook.linkingsound", 1.0F, 1.0F);
			// ModLoader.getMinecraftInstance().sndManager.playSoundFX("mystlinkingbook.linkingsound", 1.0F, 1.0F);
			// ModLoader.getMinecraftInstance().sndManager.playSound("mystlinkingbook.linkingsound", (float)entityplayer.posX, (float)entityplayer.posY, (float)entityplayer.posZ, 1.0F, 1.0F);
			// entityplayer.worldObj.playSoundAtEntity(entityplayer, "mystlinkingbook.linkingsound", 1.0F, 1.0F);
		}
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
	
	@Override
	public void onGuiClosed() {
		saveName();
		linkingPanel.onGuiClosed();
		if (tileEntityLinkingBook.guiLinkingBook == this) {
			tileEntityLinkingBook.guiLinkingBook = null;
		}
	}
	
	public void saveName() {
		if (editName) {
			String name = nameTextfield.getText();
			if (!name.isEmpty()) {
				if (!name.equals(savedName)) {
					mod_MLB.linkingBook.setName(nbttagcompound_linkingBook, name);
					savedName = name;
				}
				savedNameWidth = fontRenderer.getStringWidth(savedName);
				editName = false;
			}
		}
	}
	
	public void notifyPowerStateChanged(boolean isPowered) {
		linkingPanel.notifyPowerStateChanged(isPowered);
	}
	
	public void updateNbMissingPages() {
		int nbPages = mod_MLB.linkingBook.getNbPages(nbttagcompound_linkingBook);
		int maxPages = mod_MLB.linkingBook.getMaxPages(nbttagcompound_linkingBook);
		missingPages = maxPages - nbPages;
		if (maxPages > 0) {
			missingPagesStr = (missingPages == 0 ? "" : nbPages + "/") + maxPages + " page" + (maxPages > 1 ? "s" : "");
		}
		else {
			missingPagesStr = "";
		}
		missingPagesStrWidth = fontRenderer.getStringWidth(missingPagesStr);
		linkingPanel.updateCanLink();
	}
	
	public void updateLinkingProgress() {
		linkingProgress = (System.currentTimeMillis() - linkingStartedTime) / linkingDuration;
		if (linkingProgress < 0) {
			linkingProgress = 0;
		}
		else if (linkingProgress > 1) {
			linkingProgress = 1;
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		drawDefaultBackground();
		int bookWidth = 192;
		int bookHeight = 192;
		
		mc.renderEngine.bindTexture(mc.renderEngine.getTexture(Mod_MystLinkingBook.resourcesPath + "tempLinkGUI.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		drawTexturedModalRect((width - bookWidth) / 2, (height - bookHeight) / 2, 0, 0, 192, 192);
		
		if (editName) {
			fontRenderer.drawString("Name:", width / 2 - 82, height / 2 - 75, 0x000000);
			nameTextfield.drawTextBox();
		}
		else {
			fontRenderer.drawString(savedName, width / 2 - 47 - savedNameWidth / 2, height / 2 - 62, 0x000000);
			// drawString(fontRenderer, "Text", width / 2 - 82, height / 2 - 75, 0x000000);
		}
		
		fontRenderer.drawString(missingPagesStr, width / 2 - 47 - missingPagesStrWidth / 2, height / 2 + 62, 0x000000);
		
		super.drawScreen(mouseX, mouseY, f);
		
		if (linkingProgress != -1) {
			updateLinkingProgress();
			drawRect(0, 0, width, height, (int)(linkingProgress * 0xff) * 0x01000000);
			
			if (linkingProgress > 0.95 && runGC) {
				runGC = false;
				// Shortens the System.gc() run after teleporting to another dimension. See end of: Minecraft.changeWorld()
				System.gc();
				updateLinkingProgress();
			}
			if (linkingProgress == 1) {
				mc.displayGuiScreen(null);
				mod_MLB.linkingBook.link(nbttagcompound_linkingBook, entityplayer);
			}
		}
	}
}
