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
	
	// Not used for now:
	GuiButton stayOpenButton;
	String openButton_text = "Book Stays Open: ";
	boolean stayOpen;
	
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
		stayOpenButton = new GuiButton(1, guiLeft + 25, guiTop + 20, 120, 20, openButton_text);
		controlList.add(stayOpenButton);
		updateStayOpenButton();
	}
	
	public void updateStayOpenButton() {
		stayOpen = tileEntityLinkingBook.getStayOpen();
		stayOpenButton.displayString = openButton_text + (stayOpen ? "YES" : "NO");
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if (guibutton == stayOpenButton) {
			if (tileEntityLinkingBook.getStayOpen() == stayOpen) {
				tileEntityLinkingBook.setStayOpen(!stayOpen);
				updateStayOpenButton();
			}
		}
	}
	
	@Override
	protected void mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer() {
		fontRenderer.drawString("Look of the linking table ", 8, 10, 0x404040);
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
