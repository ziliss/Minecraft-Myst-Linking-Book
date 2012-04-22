package net.minecraft.src.mystlinkingbook;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.GuiTextField;

import org.lwjgl.opengl.GL11;

/**
 * 
 * 
 * @author ziliss
 * @see
 * @since 0.3a
 */
public class GuiLookOfLinkingBook extends GuiContainer {
	
	public Mod_MystLinkingBook mod_MLB;
	public EntityPlayer entityplayer;
	public LinkingBook linkingBook;
	public TileEntityLinkingBook tileEntityLinkingBook;
	
	public ContainerLookOfLinkingBook container;
	
	protected GuiButtonStates stayOpenButton;
	
	protected GuiButton updateImageButton;
	protected GuiButtonStates autoUpdateImageButton;
	
	protected GuiTextField coverNameTextfield;
	
	public GuiLookOfLinkingBook(EntityPlayer entityplayer, LinkingBook linkingBook, TileEntityLinkingBook tileEntityLinkingBook, Mod_MystLinkingBook mod_MLB) {
		super(new ContainerLookOfLinkingBook(entityplayer.inventory, tileEntityLinkingBook, mod_MLB));
		this.entityplayer = entityplayer;
		this.linkingBook = linkingBook;
		this.tileEntityLinkingBook = tileEntityLinkingBook;
		this.mod_MLB = mod_MLB;
		container = (ContainerLookOfLinkingBook)inventorySlots;
	}
	
	@Override
	public void initGui() {
		super.initGui();
		
		controlList.clear();
		stayOpenButton = new GuiButtonStates(1, guiLeft + 40, guiTop + 18, 120, 20, "Book Stays Open", new String[] { "NO", "YES" }) {
			@Override
			public void updateState() {
				setBooleanState(linkingBook.getStayOpen());
			}
		};
		controlList.add(stayOpenButton);
		
		updateImageButton = new GuiButton(1, guiLeft + 10, guiTop + 42, 70, 20, "Update image");
		updateImageButton.enabled = linkingBook.canLink();
		controlList.add(updateImageButton);
		
		autoUpdateImageButton = new GuiButtonStates(1, guiLeft + 85, guiTop + 42, 80, 20, "Update", new String[] { "MANUAL", "AUTO" }) {
			@Override
			public void updateState() {
				// setBooleanState(tileEntityLinkingBook.);
				setNextBooleanState(); // For testing
			}
		};
		// controlList.add(autoUpdateImageButton);
		
		coverNameTextfield = new GuiTextField(fontRenderer, guiLeft + 47, guiTop + 66, 70, 14);
		coverNameTextfield.setText(linkingBook.getCoverName());
		coverNameTextfield.setMaxStringLength(10);
		coverNameTextfield.setFocused(true);
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if (guibutton == stayOpenButton) {
			boolean stayOpen = stayOpenButton.getBooleanState();
			if (linkingBook.getStayOpen() == stayOpen) {
				linkingBook.setStayOpen(!stayOpen);
				stayOpenButton.updateState();
			}
		}
		else if (guibutton == updateImageButton) {
			if (linkingBook.canLink()) {
				mc.displayGuiScreen(null);
				GuiTakeLinkingPanelImage.startTakeLinkingPanelImage(entityplayer, tileEntityLinkingBook, mod_MLB);
			}
		}
		else if (guibutton == autoUpdateImageButton) {
			
			autoUpdateImageButton.updateState();
		}
	}
	
	@Override
	protected void mouseClicked(int i, int j, int k) {
		coverNameTextfield.mouseClicked(i, j, k);
		super.mouseClicked(i, j, k);
	}
	
	@Override
	protected void keyTyped(char c, int i) {
		if (i == 1) { // Esc
			mc.displayGuiScreen(null);
			mc.setIngameFocus();
		}
		else if (PrivateAccesses.GuiTextField_isEnabled.getFrom(coverNameTextfield) && coverNameTextfield.getIsFocused()) {
			if (i == 28 || i == 114) { // Return || Enter
				saveCoverName();
				coverNameTextfield.setFocused(false);
			}
			else {
				coverNameTextfield.textboxKeyTyped(c, i);
			}
		}
		else if (i == mc.gameSettings.keyBindInventory.keyCode) {
			saveCoverName();
			mc.displayGuiScreen(null);
			mc.setIngameFocus();
		}
	}
	
	public void saveCoverName() {
		coverNameTextfield.setText(linkingBook.setCoverName(coverNameTextfield.getText()));
	}
	
	@Override
	public void updateScreen() {
		if (linkingBook.canLink() != updateImageButton.enabled) {
			updateImageButton.enabled = !updateImageButton.enabled;
		}
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer() {
		fontRenderer.drawString("Look of the linking table", 8, 7, 0x404040);
		// fontRenderer.drawString("Inventory", 8, ySize - 96 + 2 , 0x404040);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		mc.renderEngine.bindTexture(mod_MLB.texture_guiLookOfLinkingBook.getTextureId());
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int l = (width - xSize) / 2;
		int i1 = (height - ySize) / 2;
		drawTexturedModalRect(l, i1, 0, 0, xSize, ySize);
		
		fontRenderer.drawString("Cover:", guiLeft + 11, guiTop + 69, 0x000000);
		coverNameTextfield.drawTextBox();
	}
	
}
