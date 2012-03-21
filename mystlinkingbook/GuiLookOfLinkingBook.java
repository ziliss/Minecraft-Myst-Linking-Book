package net.minecraft.src.mystlinkingbook;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiContainer;

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
	public TileEntityLinkingBook tileEntityLinkingBook;
	
	public ContainerLookOfLinkingBook container;
	
	GuiButtonStates stayOpenButton;
	
	GuiButton updateImageButton;
	GuiButtonStates autoUpdateImageButton;
	
	public GuiLookOfLinkingBook(EntityPlayer entityplayer, TileEntityLinkingBook tileEntityLinkingBook, Mod_MystLinkingBook mod_MLB) {
		super(new ContainerLookOfLinkingBook(entityplayer.inventory, tileEntityLinkingBook, mod_MLB));
		this.entityplayer = entityplayer;
		this.tileEntityLinkingBook = tileEntityLinkingBook;
		this.mod_MLB = mod_MLB;
		container = (ContainerLookOfLinkingBook)inventorySlots;
	}
	
	@Override
	public void initGui() {
		super.initGui();
		
		controlList.clear();
		stayOpenButton = new GuiButtonStates(1, guiLeft + 40, guiTop + 22, 120, 20, "Book Stays Open", new String[] { "NO", "YES" }) {
			@Override
			public void updateState() {
				setBooleanState(tileEntityLinkingBook.stayOpen);
			}
		};
		controlList.add(stayOpenButton);
		
		updateImageButton = new GuiButton(1, guiLeft + 10, guiTop + 46, 70, 20, "Update image");
		controlList.add(updateImageButton);
		
		autoUpdateImageButton = new GuiButtonStates(1, guiLeft + 85, guiTop + 46, 80, 20, "Update", new String[] { "MANUAL", "AUTO" }) {
			@Override
			public void updateState() {
				// setBooleanState(tileEntityLinkingBook.);
				setNextBooleanState(); // For testing
			}
		};
		// controlList.add(autoUpdateImageButton);
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if (guibutton == stayOpenButton) {
			boolean stayOpen = stayOpenButton.getBooleanState();
			if (tileEntityLinkingBook.stayOpen == stayOpen) {
				mod_MLB.linkingBook.setStayOpen(tileEntityLinkingBook.nbttagcompound_linkingBook, !stayOpen);
				tileEntityLinkingBook.notifyStayOpenChanged();
				stayOpenButton.updateState();
			}
		}
		else if (guibutton == updateImageButton) {
			mc.displayGuiScreen(null);
			GuiTakeLinkingPanelImage.startTakeLinkingPanelImage(entityplayer, tileEntityLinkingBook, mod_MLB);
		}
		else if (guibutton == autoUpdateImageButton) {
			
			autoUpdateImageButton.updateState();
		}
	}
	
	@Override
	protected void mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer() {
		fontRenderer.drawString("Look of the linking table", 8, 10, 0x404040);
		fontRenderer.drawString("Inventory", 8, ySize - 96 + 2, 0x404040);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		mc.renderEngine.bindTexture(mc.renderEngine.getTexture(Mod_MystLinkingBook.resourcesPath + "tempLookGUI.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int l = (width - xSize) / 2;
		int i1 = (height - ySize) / 2;
		drawTexturedModalRect(l, i1, 0, 0, xSize, ySize);
	}
	
}
