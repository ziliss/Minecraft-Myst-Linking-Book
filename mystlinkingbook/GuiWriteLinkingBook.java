package net.minecraft.src;

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
		
		controlList.clear();
		nameTextfield = new GuiTextField(this, fontRenderer, guiLeft + 40, guiTop + 6, 120, 14, "");
		nameTextfield.setMaxStringLength(16);
		nameTextfield.setFocused(true);
		writeButton = new GuiButton(1, guiLeft + 86, guiTop + 46, 40, 20, "Write");
		// Because of a bug in GuiContainer.drawScreen(), the buttons are drawn over the item tooltips.
		// As a workaround, we will not add the button to the controlList but manage it ourself:
		// controlList.add(writeButton);
		
		updateCanWrite();
	}
	
	public void updateCanWrite() {
		canWrite = container.canWrite();
		writeButton.enabled = canWrite;
	}
	
	@Override
	protected void mouseClicked(int i, int j, int k) {
		nameTextfield.mouseClicked(i, j, k);
		
		// Because writeButton is not in the controlList, we need to check it:
		if (k == 0 && writeButton.mousePressed(mc, i, j)) {
			
			// Modify the following private field:
			// super.selectedButton = writeButton;
			try {
				mod_mystlinkingbook.setPrivateValue(GuiScreen.class, this, "a", "selectedButton", writeButton); // MCPBot: gcf GuiScreen.selectedButton
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
			actionPerformed(writeButton);
		}
		
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
		mc.renderEngine.bindTexture(mc.renderEngine.getTexture("/mystlinkingbook/tempWriteGUI.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int l = (width - xSize) / 2;
		int i1 = (height - ySize) / 2;
		drawTexturedModalRect(l, i1, 0, 0, xSize, ySize);
		// Because writeButton is not in the controlList, we need to draw it:
		writeButton.drawButton(mc, i, j);
		nameTextfield.drawTextBox();
	}
	
}
