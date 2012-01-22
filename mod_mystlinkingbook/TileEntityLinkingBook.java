package net.minecraft.src;

/**
 * Manages the datas associated with {@code BlockLinkingBook}s.<br>
 * Manages the powered state, the inventory, and the opening of the linking book.<br>
 * <br>
 * For each Linking Book Block there is an associated {@code TileEntityLinkingBook} object.
 * 
 * @author ziliss
 * @see BlockLinkingBook
 * @see net.minecraft.src.Block
 * @see net.minecraft.src.BlockEntity
 * @since 0.1a
 */
public class TileEntityLinkingBook extends TileEntity {
	
	/**
	 * The datas for this Linking Book Block.
	 */
	public NBTTagCompound nbttagcompound_linkingBook;
	
	/**
	 * The inventory contained by the Linking Book Block.
	 */
	public InventoryBasic inventoryLinkingBook = new InventoryBasic("InventoryLinkingBook", 1) {
		@Override
		public void onInventoryChanged() {
			super.onInventoryChanged();
			worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);
		}
	};
	
	public boolean isPowered;
	
	public GuiLinkingBook guiLinkingBook = null;
	
	public void setPoweredState(boolean isPowered) {
		if (isPowered == this.isPowered) return;
		this.isPowered = isPowered;
		if (guiLinkingBook != null) {
			guiLinkingBook.notifyPowerStateChanged(isPowered);
		}
	}
	
	/**
	 * Loads the datas from the disk. (Called by Minecraft)
	 */
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		isPowered = nbttagcompound.getBoolean("powered");
		this.nbttagcompound_linkingBook = nbttagcompound.getCompoundTag("tag");
		
		int slotNb;
		NBTTagCompound nbttagcompound1;
		NBTTagList nbttaglist = nbttagcompound.getTagList("Inventory");
		for (int i = 0; i < nbttaglist.tagCount(); i++) {
			nbttagcompound1 = (NBTTagCompound)nbttaglist.tagAt(i);
			slotNb = nbttagcompound1.getByte("Slot") & 0xff;
			if (slotNb >= 0 && slotNb < inventoryLinkingBook.getSizeInventory()) {
				
				// Modify the following private field:
				// inventoryLinkingBook.inventoryContents[slotNb] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
				ItemStack[] inventoryContents;
				try {
					inventoryContents = (ItemStack[])ModLoader.getPrivateValue(InventoryBasic.class, inventoryLinkingBook, "c");
					inventoryContents[slotNb] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
				}
				catch (NoSuchFieldException e) {
					try {
						inventoryContents = (ItemStack[])ModLoader.getPrivateValue(InventoryBasic.class, inventoryLinkingBook, "inventoryContents");
						inventoryContents[slotNb] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
					}
					catch (Exception ex) {
						e.printStackTrace();
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Saves the datas to the disk. (Called by Minecraft)
	 */
	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setBoolean("powered", isPowered);
		nbttagcompound.setTag("tag", this.nbttagcompound_linkingBook);
		
		ItemStack itemstack;
		NBTTagCompound nbttagcompound1;
		NBTTagList nbttaglist = new NBTTagList();
		for (int i = 0; i < inventoryLinkingBook.getSizeInventory(); i++) {
			itemstack = inventoryLinkingBook.getStackInSlot(i);
			if (itemstack != null) {
				nbttagcompound1 = new NBTTagCompound();
				nbttagcompound1.setByte("Slot", (byte)i);
				itemstack.writeToNBT(nbttagcompound1);
				nbttaglist.setTag(nbttagcompound1);
			}
		}
		nbttagcompound.setTag("Inventory", nbttaglist);
	}
	
	// Everything below comes from TileEntityEnchantmentTable:
	public float field_40059_f = 0;
	public float field_40060_g = 0;
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		field_40060_g = field_40059_f;
		EntityPlayer entityplayer = worldObj.getClosestPlayer(xCoord + 0.5F, yCoord + 1.5F, zCoord + 0.5F, 1.8D);
		if (entityplayer != null) {
			if (field_40059_f < 1.0F) {
				field_40059_f += 0.1F;
				if (field_40059_f > 1.0F) {
					field_40059_f = 1.0F;
				}
			}
		}
		else {
			if (field_40059_f > 0) {
				field_40059_f -= 0.1F;
				if (field_40059_f < 0.0F) {
					field_40059_f = 0.0F;
				}
			}
		}
	}
}
