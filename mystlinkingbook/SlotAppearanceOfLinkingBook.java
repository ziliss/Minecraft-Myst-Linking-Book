package net.minecraft.src;

/**
 * Each instance represents a customizable {@code Slot}.<br>
 * This Slot can only accept Block items.
 * 
 * @author ziliss
 * @see net.minecraft.src.Slot
 * @since 0.3a
 */
public class SlotAppearanceOfLinkingBook extends Slot {
	
	/**
	 * Reference to the mod instance.
	 */
	public mod_mystlinkingbook mod_MLB;
	
	public SlotAppearanceOfLinkingBook(IInventory iinventory, int slotIndex, int xDisplayPosition, int yDisplayPosition, mod_mystlinkingbook mod_MLB) {
		super(iinventory, slotIndex, xDisplayPosition, yDisplayPosition);
		this.mod_MLB = mod_MLB;
	}
	
	@Override
	public int getSlotStackLimit() {
		return 1;
	}
	
	@Override
	public boolean isItemValid(ItemStack itemstack) {
		int itemID = itemstack.getItem().shiftedIndex;
		if (itemID < 0 || itemID >= Block.blocksList.length) return false;
		if (itemID == mod_MLB.blockLinkingBook.blockID) return false;
		switch (itemID) {
			case 0:
				return false;
			default:
				return true;
		}
	}
}