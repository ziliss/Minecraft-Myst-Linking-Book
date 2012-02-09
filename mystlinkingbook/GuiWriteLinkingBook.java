package net.minecraft.src.mystlinkingbook;

import java.awt.Color;

import net.minecraft.src.BlockCloth;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.GuiTextField;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.Slot;

import org.lwjgl.opengl.GL11;

/**
 * 
 * 
 * @author ziliss
 * @see
 * @since 0.2a
 */
public class GuiWriteLinkingBook extends GuiContainer {
	
	public Mod_MystLinkingBook mod_MLB;
	public EntityPlayer entityplayer;
	public NBTTagCompound nbttagcompound_linkingBook;
	
	public ContainerWriteLinkingBook container;
	
	GuiTextField nameTextfield;
	GuiButton writeButton;
	
	boolean canWrite;
	
	int timePassing = 0;
	
	public Color pagesColor;
	int pagesLeft = 75;
	int pagesTop = 26;
	int pagesWidth = 63;
	int pagesHeight = 45;
	
	public GuiWriteLinkingBook(EntityPlayer entityplayer, NBTTagCompound nbttagcompound_linkingBook, Mod_MystLinkingBook mod_MLB) {
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
		
		notifyColorChanged();
		
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
			
			PrivateAccesses.GuiScreen_selectedButton.setTo(this, writeButton);
			
			mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
			actionPerformed(writeButton);
		}
		
		super.mouseClicked(i, j, k);
	}
	
	@Override
	protected void handleMouseClick(Slot slot, int i, int j, boolean flag) {
		if (slot != null && slot.slotNumber != container.inventorySlots.size() - (9 - entityplayer.inventory.currentItem)) {
			super.handleMouseClick(slot, i, j, flag);
			if (slot.slotNumber == container.colorSlot.slotNumber) {
				notifyColorChanged();
			}
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
			container.featherSlot.putStack(null);
			container.inkSlot.putStack(null);
			
			int nbPaper = container.paperSlot.getHasStack() ? container.paperSlot.getStack().stackSize : 0;
			if (nbPaper > 0) {
				container.paperSlot.putStack(null);
			}
			boolean unstable = container.redstoneSlot.getHasStack() && container.redstoneSlot.getStack().stackSize == 1;
			if (unstable) {
				container.redstoneSlot.putStack(null);
			}
			
			boolean hasDyeColor = container.colorSlot.getHasStack() && container.colorSlot.getStack().stackSize == 1;
			if (hasDyeColor) {
				mod_MLB.linkingBook.setPagesColorFromDye(nbttagcompound_linkingBook, container.colorSlot.getStack().getItemDamage());
				container.colorSlot.putStack(null);
			}
			updateCanWrite();
			
			mod_MLB.linkingBook.write(nbttagcompound_linkingBook, entityplayer, nbPaper, unstable);
			
			String name = nameTextfield.getText();
			if (!name.isEmpty()) {
				mod_MLB.linkingBook.setName(nbttagcompound_linkingBook, name);
			}
			
			mc.displayGuiScreen(null);
		}
	}
	
	public void notifyColorChanged() {
		if (container.colorSlot.getHasStack()) {
			int dyeColor = container.colorSlot.getStack().getItemDamage();
			pagesColor = ItemPage.brighterColorTable[BlockCloth.getBlockFromDye(dyeColor)];
		}
		else {
			pagesColor = ItemPage.brighterColorTable[mod_MLB.linkingBook.getPagesColor(nbttagcompound_linkingBook)];
		}
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer() {
		fontRenderer.drawString("Name: ", 8, 10, 0x404040);
		fontRenderer.drawString("Inventory", 8, ySize - 96 + 2, 0x404040);
	}
	
	@Override
	public void updateScreen() {
		timePassing++;
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		mc.renderEngine.bindTexture(mc.renderEngine.getTexture(Mod_MystLinkingBook.resourcesPath + "tempWriteGUI.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		GL11.glColor3ub((byte)pagesColor.getRed(), (byte)pagesColor.getGreen(), (byte)pagesColor.getBlue());
		drawTexturedModalRect(guiLeft + pagesLeft, guiTop + pagesTop, pagesLeft, pagesTop, pagesWidth, pagesHeight);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		
		if (!container.colorSlot.getHasStack() || container.colorSlot.getStack().stackSize < 1) {
			int itemNb = timePassing / 20 % 16;
			int itemCoordX = itemNb / 8 * 16;
			int itemCoordY = itemNb % 8 * 16;
			drawTexturedModalRect(guiLeft + 143, guiTop + 53, xSize + itemCoordX, itemCoordY, 16, 16);
		}
		
		// Because writeButton is not in the controlList, we need to draw it:
		writeButton.drawButton(mc, i, j);
		nameTextfield.drawTextBox();
	}
	
}
