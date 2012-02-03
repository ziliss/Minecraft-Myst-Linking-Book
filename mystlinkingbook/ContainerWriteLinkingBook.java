package net.minecraft.src.mystlinkingbook;

import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.InventoryBasic;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;

/**
 * Manages the slots used by a {@code GuiLinkingBook}.
 * 
 * @author ziliss
 * @see GuiLinkingBook
 * @see SlotWriteLinkingBook
 * @since 0.2a
 */
public class ContainerWriteLinkingBook extends Container {
	
	/**
	 * Reference to the mod instance.
	 */
	public Mod_MystLinkingBook mod_MLB;
	
	/**
	 * The inventory of the player.
	 */
	public InventoryPlayer inventoryPlayer;
	
	/**
	 * The inventory used to temporarily keep the items while writing the linking book.
	 */
	public InventoryBasic inventoryWriteLinkingBook;
	
	// The slots used to keep the items while writing the linking book:
	public SlotWriteLinkingBook featherSlot;
	public SlotWriteLinkingBook paperSlot;
	public SlotWriteLinkingBook inkSlot;
	public SlotWriteLinkingBook redstoneSlot;
	
	public ContainerWriteLinkingBook(InventoryPlayer inventoryPlayer, Mod_MystLinkingBook mod_MLB) {
		this.inventoryPlayer = inventoryPlayer;
		inventoryWriteLinkingBook = new InventoryBasic("WriteLinkingBook", 4);
		this.mod_MLB = mod_MLB;
		
		// Add the slots for items used to write the linking book:
		addSlot(featherSlot = new SlotWriteLinkingBook(inventoryWriteLinkingBook, 0, 53, 28, Item.feather, 1));
		addSlot(paperSlot = new SlotWriteLinkingBook(inventoryWriteLinkingBook, 1, 143, 28, Item.paper, PrivateAccesses.Item_maxStackSize.getFrom(Item.paper)));
		addSlot(inkSlot = new SlotWriteLinkingBook(inventoryWriteLinkingBook, 2, 53, 53, Item.dyePowder, 0, 1)); // 0 because: Item.dyeColorNames[0] == "black" which is the ink sac.
		addSlot(redstoneSlot = new SlotWriteLinkingBook(inventoryWriteLinkingBook, 3, 17, 53, Item.redstone, 1));
		
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
	
	/**
	 * Tells if all the necessary items have been added. That means: 1 feather and 1 ink sac
	 * 
	 * @return true if the linking book can be written.
	 */
	public boolean canWrite() {
		boolean canWrite = featherSlot.getHasStack() && featherSlot.getStack().stackSize == 1;
		canWrite &= inkSlot.getHasStack() && inkSlot.getStack().stackSize == 1;
		return canWrite;
	}
	
	/**
	 * Drops all remaining items placed in the GUI it is closed.
	 */
	@Override
	public void onCraftGuiClosed(EntityPlayer entityplayer) {
		ItemStack itemstack;
		for (int i = 0; i < inventoryWriteLinkingBook.getSizeInventory(); i++) {
			itemstack = inventoryWriteLinkingBook.getStackInSlot(i);
			if (itemstack != null) {
				inventoryWriteLinkingBook.setInventorySlotContents(i, null);
				entityplayer.dropPlayerItem(itemstack);
			}
		}
		super.onCraftGuiClosed(entityplayer);
	}
}