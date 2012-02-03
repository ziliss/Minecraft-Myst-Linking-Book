package net.minecraft.src;

import org.lwjgl.opengl.GL11;

/**
 * 
 * 
 * @author ziliss
 * @see
 * @since 0.3a
 */
public class GuiLookOfLinkingBook extends GuiContainer {
	
	public mod_mystlinkingbook mod_MLB;
	public EntityPlayer entityplayer;
	
	public ContainerLookOfLinkingBook container;
	
	// Not used for now:
	GuiButton writeButton;
	
	public GuiLookOfLinkingBook(EntityPlayer entityplayer, TileEntityLinkingBook tileEntityLinkingBook, mod_mystlinkingbook mod_MLB) {
		super(new ContainerLookOfLinkingBook(entityplayer.inventory, tileEntityLinkingBook, mod_MLB));
		this.entityplayer = entityplayer;
		this.mod_MLB = mod_MLB;
		container = (ContainerLookOfLinkingBook)inventorySlots;
	}
	
	@Override
	public void initGui() {
		super.initGui();
		
		controlList.clear();
		writeButton = new GuiButton(1, guiLeft + 86, guiTop + 46, 40, 20, "Nothing");
		controlList.add(writeButton);
		writeButton.enabled = false;
		writeButton.drawButton = false;
	}
	
	@Override
	protected void mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer() {
		fontRenderer.drawString("Look of the Linking Table ", 8, 10, 0x404040);
		fontRenderer.drawString("Inventory", 8, ySize - 96 + 2, 0x404040);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		mc.renderEngine.bindTexture(mc.renderEngine.getTexture("/mystlinkingbook/tempLookGUI.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int l = (width - xSize) / 2;
		int i1 = (height - ySize) / 2;
		drawTexturedModalRect(l, i1, 0, 0, xSize, ySize);
	}
	
}
