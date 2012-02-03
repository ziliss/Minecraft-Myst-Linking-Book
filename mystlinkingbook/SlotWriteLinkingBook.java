package net.minecraft.src.mystlinkingbook;

import net.minecraft.src.IInventory;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;

/**
 * Each instance represents a customizable {@code Slot}.<br>
 * You can customize which kind of {@code Item} is accepted and how much.
 * 
 * @author ziliss
 * @see net.minecraft.src.Slot
 * @since 0.2a
 */
public class SlotWriteLinkingBook extends Slot {
	
	/**
	 * The kind of {@code Item} accepted in this slot.
	 */
	public Item item;
	
	/**
	 * Used to check the sub types of an {@code Item}. In case the item has multiple sub-types represented by different damage values.<br>
	 * -1 means don't check damage.
	 */
	public int damage = -1;
	
	/**
	 * The max number of {@code Item}s allowed in this slot.
	 */
	public int stackLimit;
	
	public SlotWriteLinkingBook(IInventory iinventory, int slotIndex, int xDisplayPosition, int yDisplayPosition, Item item, int stackLimit) {
		super(iinventory, slotIndex, xDisplayPosition, yDisplayPosition);
		
		this.item = item;
		this.stackLimit = stackLimit;
	}
	
	public SlotWriteLinkingBook(IInventory iinventory, int slotIndex, int xDisplayPosition, int yDisplayPosition, Item item, int damage, int stackLimit) {
		this(iinventory, slotIndex, xDisplayPosition, yDisplayPosition, item, stackLimit);
		
		this.damage = damage;
	}
	
	@Override
	public int getSlotStackLimit() {
		return stackLimit;
	}
	
	@Override
	public boolean isItemValid(ItemStack itemstack) {
		if (itemstack.getItem().shiftedIndex == item.shiftedIndex) {
			if (damage == -1) return true;
			else return itemstack.getItemDamage() == damage;
		}
		else return false;
	}
}