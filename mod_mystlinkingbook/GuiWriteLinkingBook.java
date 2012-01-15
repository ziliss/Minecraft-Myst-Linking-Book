package net.minecraft.src;

import java.awt.image.BufferedImage;

import org.lwjgl.opengl.GL11;

/**
 * 
 * 
 * @author ziliss
 * @see
 * @since 0.2a
 */
public class GuiWriteLinkingBook extends GuiContainer {
	
	public mod_mystlinkingbook mod_MLB;
	public EntityPlayer entityplayer;
	public NBTTagCompound nbttagcompound_linkingBook;
	
	public ContainerWriteLinkingBook container;
	
	GuiTextField nameTextfield;
	GuiButton writeButton;
	
	boolean canWrite;
	
	int imgID_GUI = 3235;
	
	public GuiWriteLinkingBook(EntityPlayer entityplayer, NBTTagCompound nbttagcompound_linkingBook, mod_mystlinkingbook mod_MLB) {
		super(new ContainerWriteLinkingBook(entityplayer.inventory, mod_MLB));
		this.entityplayer = entityplayer;
		this.nbttagcompound_linkingBook = nbttagcompound_linkingBook;
		this.mod_MLB = mod_MLB;
		container = (ContainerWriteLinkingBook)inventorySlots;
	}
	
	@Override
	public void initGui() {
		super.initGui();
		try {
			BufferedImage img = ModLoader.loadImage(mc.renderEngine, "/mystlinkingbook/tempWriteGUI.png");
			mc.renderEngine.setupTexture(img, imgID_GUI);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		controlList.clear();
		nameTextfield = new GuiTextField(this, fontRenderer, guiLeft + 40, guiTop + 6, 120, 14, "");
		nameTextfield.setMaxStringLength(16);
		nameTextfield.setFocused(true);
		writeButton = new GuiButton(1, guiLeft + 86, guiTop + 46, 40, 20, "Write");
		controlList.add(writeButton);
		
		updateCanWrite();
	}
	
	public void updateCanWrite() {
		canWrite = container.canWrite();
		writeButton.enabled = canWrite;
	}
	
	@Override
	protected void mouseClicked(int i, int j, int k) {
		nameTextfield.mouseClicked(i, j, k);
		super.mouseClicked(i, j, k);
	}
	
	@Override
	protected void handleMouseClick(Slot slot, int i, int j, boolean flag) {
		if (slot != null && slot.slotNumber != container.inventorySlots.size() - (9 - entityplayer.inventory.currentItem)) {
			super.handleMouseClick(slot, i, j, flag);
			updateCanWrite();
		}
	}
	
	@Override
	protected void keyTyped(char c, int i) {
		if (i == 1) {
			mc.displayGuiScreen(null);
			mc.setIngameFocus();
		}
		else if (nameTextfield.isEnabled && nameTextfield.isFocused) {
			nameTextfield.textboxKeyTyped(c, i);
		}
		else if (i == mc.gameSettings.keyBindInventory.keyCode) {
			mc.displayGuiScreen(null);
			mc.setIngameFocus();
		}
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if (guibutton == writeButton && canWrite) {
			int nbPaper = container.paperSlot.getHasStack() ? container.paperSlot.getStack().stackSize : 0;
			if (nbPaper > 0) {
				container.paperSlot.putStack(null);
			}
			boolean unstable = container.redstoneSlot.getHasStack() && container.redstoneSlot.getStack().stackSize == 1;
			if (unstable) {
				container.redstoneSlot.putStack(null);
			}
			container.inkSlot.putStack(null);
			updateCanWrite();
			
			mod_MLB.linkingBook.write(nbttagcompound_linkingBook, entityplayer, nbPaper, unstable);
			
			String name = nameTextfield.getText();
			if (!name.isEmpty()) {
				mod_MLB.linkingBook.setName(nbttagcompound_linkingBook, name);
			}
			if (entityplayer.inventory.addItemStackToInventory(container.featherSlot.getStack())) {
				container.featherSlot.putStack(null);
			}
			mc.displayGuiScreen(null);
		}
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer() {
		fontRenderer.drawString("Name: ", 8, 10, 0x404040);
		fontRenderer.drawString("Inventory", 8, ySize - 96 + 2, 0x404040);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		System.out.println(mc.renderEngine.getTexture("/mystlinkingbook/tempWriteGUI.png"));
		mc.renderEngine.bindTexture(mc.renderEngine.getTexture("/mystlinkingbook/tempWriteGUI.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int l = (width - xSize) / 2;
		int i1 = (height - ySize) / 2;
		drawTexturedModalRect(l, i1, 0, 0, xSize, ySize);
		nameTextfield.drawTextBox();
	}
	
}
