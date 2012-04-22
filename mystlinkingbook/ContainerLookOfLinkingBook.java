package net.minecraft.src.mystlinkingbook;

import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.ItemStack;
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
	
	public int nbSlots = 1;
	
	// The slots used to keep the items while writing the linking book:
	public SlotAppearanceOfLinkingBook blockSlot;
	
	public ContainerLookOfLinkingBook(InventoryPlayer inventoryPlayer, TileEntityLinkingBook tileEntityLinkingBook, Mod_MystLinkingBook mod_MLB) {
		this.inventoryPlayer = inventoryPlayer;
		this.tileEntityLinkingBook = tileEntityLinkingBook;
		
		// Add the slot for blocks used to set the look of the linking book table:
		addSlot(blockSlot = new SlotAppearanceOfLinkingBook(tileEntityLinkingBook.inventoryLinkingBook, 0, 15, 20, mod_MLB));
		
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
	
	@Override
	public ItemStack transferStackInSlot(int i) {
		// Taken from ContainerChest.transferStackInSlot:
		ItemStack itemstack = null;
		Slot slot = (Slot)inventorySlots.get(i);
		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			if (i < nbSlots) {
				if (!mergeItemStack(itemstack1, nbSlots, inventorySlots.size(), true)) return null;
			}
			else if (!mergeItemStack(itemstack1, 0, nbSlots, false)) return null;
			if (itemstack1.stackSize == 0) {
				slot.putStack(null);
			}
			else {
				slot.onSlotChanged();
			}
		}
		return itemstack;
	}
	
	@Override
	protected boolean mergeItemStack(ItemStack itemstack, int i, int j, boolean flag) {
		// The default implementation in Slot doesn't take into account the Slot.isItemValid() and Slot.getSlotStackLimit() values.
		// So here is a modified implementation. I have only modified the parts with a comment.
		
		boolean flag1 = false;
		int k = i;
		if (flag) {
			k = j - 1;
		}
		if (itemstack.isStackable()) {
			while (itemstack.stackSize > 0 && (!flag && k < j || flag && k >= i)) {
				Slot slot = (Slot)inventorySlots.get(k);
				ItemStack itemstack1 = slot.getStack();
				
				if (flag) {
					k--;
				}
				else {
					k++;
				}
				
				// Check if item is valid:
				if (!slot.isItemValid(itemstack)) {
					continue;
				}
				
				if (itemstack1 != null && itemstack1.itemID == itemstack.itemID && (!itemstack.getHasSubtypes() || itemstack.getItemDamage() == itemstack1.getItemDamage()) && ItemStack.func_46154_a(itemstack, itemstack1)) {
					int i1 = itemstack1.stackSize + itemstack.stackSize;
					
					// Don't put more items than the slot can take:
					int maxItemsInDest = Math.min(itemstack1.getMaxStackSize(), slot.getSlotStackLimit());
					
					if (i1 <= maxItemsInDest) {
						itemstack.stackSize = 0;
						itemstack1.stackSize = i1;
						slot.onSlotChanged();
						flag1 = true;
					}
					else if (itemstack1.stackSize < maxItemsInDest) {
						itemstack.stackSize -= maxItemsInDest - itemstack1.stackSize;
						itemstack1.stackSize = maxItemsInDest;
						slot.onSlotChanged();
						flag1 = true;
					}
				}
				
			}
		}
		if (itemstack.stackSize > 0) {
			int l;
			if (flag) {
				l = j - 1;
			}
			else {
				l = i;
			}
			do {
				if ((flag || l >= j) && (!flag || l < i)) {
					break;
				}
				Slot slot1 = (Slot)inventorySlots.get(l);
				ItemStack itemstack2 = slot1.getStack();
				
				if (flag) {
					l--;
				}
				else {
					l++;
				}
				
				// Check if item is valid:
				if (!slot1.isItemValid(itemstack)) {
					continue;
				}
				
				if (itemstack2 == null) {
					
					// Don't put more items than the slot can take:
					int nbItemsInDest = Math.min(itemstack.stackSize, slot1.getSlotStackLimit());
					ItemStack itemStack1 = itemstack.copy();
					itemstack.stackSize -= nbItemsInDest;
					itemStack1.stackSize = nbItemsInDest;
					
					slot1.putStack(itemStack1);
					slot1.onSlotChanged();
					// itemstack.stackSize = 0;
					flag1 = true;
					break;
				}
			} while (true);
		}
		return flag1;
	}
}