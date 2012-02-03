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
	
	public boolean isTopBlocked;
	
	public boolean isPowered;
	
	public GuiLinkingBook guiLinkingBook = null;
	
	public void onNeighborBlockChange(int id) {
		switch (worldObj.getBlockId(xCoord, yCoord + 1, zCoord)) {
			case 0: // air
			case 50: // torch
			case 51: // fire
			case 69: // lever
			case 76: // redstone torch
			case 77: // button
				isTopBlocked = false;
				break;
			default:
				isTopBlocked = true;
				if (guiLinkingBook != null) {
					ModLoader.getMinecraftInstance().displayGuiScreen(null);
				}
		}
		
		if (id == 0 || id > 0 && Block.blocksList[id].canProvidePower()) {
			boolean powered = worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
			if (powered != this.isPowered) {
				this.isPowered = powered;
				if (guiLinkingBook != null) {
					guiLinkingBook.notifyPowerStateChanged(isPowered);
				}
			}
		}
	}
	
	public void notifyNbMissingPagesChanged() {
		if (guiLinkingBook != null) {
			guiLinkingBook.updateNbMissingPages();
		}
	}
	
	public void onBlockRemoval() {
		if (guiLinkingBook != null) {
			ModLoader.getMinecraftInstance().displayGuiScreen(null);
		}
	}
	
	/**
	 * Loads the datas from the disk. (Called by Minecraft)
	 */
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		isTopBlocked = nbttagcompound.getBoolean("topBlocked");
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
				try {
					ItemStack[] inventoryContents = (ItemStack[])mod_mystlinkingbook.getPrivateValue(InventoryBasic.class, inventoryLinkingBook, "c", "inventoryContents"); // MCPBot: gcf InventoryBasic.inventoryContents
					inventoryContents[slotNb] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
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
		nbttagcompound.setBoolean("topBlocked", isTopBlocked);
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
		
		EntityPlayer closestPlayer = null;
		if (!isTopBlocked) {
			float dX = 0.5f;
			float dZ = 0.5f;
			closestPlayer = worldObj.getClosestPlayer(xCoord + dX, yCoord + 1.8F, zCoord + dZ, 2);
			if (closestPlayer != null) {
				// Get the coordinates of the front of the book, depending on orientation:
				switch (getBlockMetadata() & 3) {
					case 0:
						dZ += 0.7f;
						break;
					case 1:
						dX -= 0.7f;
						break;
					case 2:
						dZ -= 0.7f;
						break;
					case 3:
						dX += 0.7f;
						break;
				}
				if (closestPlayer.getDistance(xCoord + dX, yCoord + 1.8F, zCoord + dZ) > 1.1D) {
					closestPlayer = null;
				}
			}
		}
		if (closestPlayer != null) {
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
