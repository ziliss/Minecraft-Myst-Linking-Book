package net.minecraft.src.mystlinkingbook;

import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.Slot;

/**
 * Manages the slots used by a {@code GuiLookOfLinkingBook}.
 * 
 * @author ziliss
 * @see GuiLookOfLinkingBook
 * @see SlotAppearanceOfLinkingBook
 * @since 0.3a
 */
public class ContainerLookOfLinkingBook extends Container {
	
	/**
	 * The inventory of the player.
	 */
	public InventoryPlayer inventoryPlayer;
	
	/**
	 * The {@code TileEntity} of the linking book {@code Block}.
	 */
	public TileEntityLinkingBook tileEntityLinkingBook;
	
	// The slots used to keep the items while writing the linking book:
	public SlotAppearanceOfLinkingBook blockSlot;
	
	public ContainerLookOfLinkingBook(InventoryPlayer inventoryPlayer, TileEntityLinkingBook tileEntityLinkingBook, Mod_MystLinkingBook mod_MLB) {
		this.inventoryPlayer = inventoryPlayer;
		this.tileEntityLinkingBook = tileEntityLinkingBook;
		
		// Add the slot for blocks used to set the look of the linking book table:
		addSlot(blockSlot = new SlotAppearanceOfLinkingBook(tileEntityLinkingBook.inventoryLinkingBook, 0, 53, 35, mod_MLB));
		
		// Add the slots of the player inventory:
		for (int j = 0; j < 3; j++) {
			for (int i1 = 0; i1 < 9; i1++) {
				addSlot(new Slot(inventoryPlayer, i1 + j * 9 + 9, 8 + i1 * 18, 84 + j * 18));
			}
			
		}
		for (int k = 0; k < 9; k++) {
			addSlot(new Slot(inventoryPlayer, k, 8 + k * 18, 142));
		}
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}
}