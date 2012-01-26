package net.minecraft.src;

import java.util.Random;

/**
 * Represents Linking Books that are placed in the world as {@code Block}.<br>
 * <br>
 * The {@code Block}s are actually {@code BlockContainers}, meaning that they contain associated datas. The Linking Books datas are thus stored by the associated {@code TileEntityLinkingBook} objects.<br>
 * <br>
 * There is only 1 instance of this class.
 * 
 * @author ziliss
 * @see TileEntityLinkingBook
 * @see net.minecraft.src.Block
 * @see net.minecraft.src.BlockEntity
 * @since 0.1a
 */
public class BlockLinkingBook extends BlockContainer {
	
	public static Random random = new Random();
	
	public int topTextureIndex;
	public int sideTextureIndex;
	public int bottomTextureIndex;
	
	/**
	 * Reference to the mod instance.
	 */
	public mod_mystlinkingbook mod_MLB;
	
	public BlockLinkingBook(int blockID, int textureID, mod_mystlinkingbook mod_MLB) {
		super(blockID, textureID, Material.wood);
		
		this.mod_MLB = mod_MLB;
		
		setHardness(1F);
		setResistance(2.0F);
		setStepSound(soundWoodFootstep);
		setBlockName("linkingBookBlock");
		
		setRequiresSelfNotify();
	}
	
	/**
	 * Stores the orientation of the block to the metadatas. Uses the first 2 bits of the metadatas.<br>
	 * Called when a Linking Book Block is placed in the world. The code that places the block in the world is in {@link ItemBlockLinkingBook.onItemUse}.
	 * 
	 * @see ItemBlockLinkingBook.onItemUse
	 */
	@Override
	public void onBlockPlacedBy(World world, int i, int j, int k, EntityLiving entityliving) {
		// Inspired by BlockRedstoneRepeater.onBlockPlacedBy:
		int l = ((MathHelper.floor_double(entityliving.rotationYaw * 4F / 360F + 0.5D) & 3) + 2) % 4;
		world.setBlockMetadataWithNotify(i, j, k, l);
	}
	
	/**
	 * Method factory for new {@code BlockEntity} that keep datas.
	 * 
	 * @see net.minecraft.src.BlockContainer#getBlockEntity()
	 */
	@Override
	public TileEntity getBlockEntity() {
		return new TileEntityLinkingBook();
	}
	
	@Override
	public boolean renderAsNormalBlock() {
		return true; // Should be true or false ?
	}
	
	@Override
	public int getBlockTexture(IBlockAccess iblockaccess, int i, int j, int k, int l) {
		TileEntityLinkingBook tileEntityLinkingBook = (TileEntityLinkingBook)iblockaccess.getBlockTileEntity(i, j, k);
		ItemStack itemStack = tileEntityLinkingBook.inventoryLinkingBook.getStackInSlot(0);
		if (itemStack != null && itemStack.stackSize > 0) {
			try {
				return Block.blocksList[itemStack.itemID].getBlockTextureFromSideAndMetadata(l, itemStack.getItemDamage());
			}
			catch (Exception e) {
				System.err.println("id: " + itemStack.itemID);
				e.printStackTrace();
			}
		}
		
		return this.getBlockTextureFromSide(l);
	}
	
	@Override
	public int getBlockTextureFromSide(int i) {
		switch (i) {
			case 0:
				return topTextureIndex;
			case 1:
				return bottomTextureIndex;
			default:
				return sideTextureIndex;
		}
	}
	
	/**
	 * Forces the blocks behind to be rendered.<br>
	 * In case there is some transparency in the texture.
	 */
	@Override
	public boolean isOpaqueCube() {
		return false;
	}
	
	/**
	 * Teleports the player when a {@code Block} is activated (right-clicked).
	 */
	@Override
	public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer) {
		TileEntityLinkingBook tileEntityLinkingBook = (TileEntityLinkingBook)world.getBlockTileEntity(i, j, k);
		NBTTagCompound nbttagcompound_linkingBook = tileEntityLinkingBook.nbttagcompound_linkingBook;
		
		ItemStack currentItem = entityplayer.inventory.getCurrentItem();
		Class openGui = null;
		if (currentItem == null) {
			openGui = GuiLinkingBook.class;
		}
		else if (currentItem.itemID == Item.paper.shiftedIndex && currentItem.stackSize > 0) {
			if (mod_MLB.linkingBook.addPages(nbttagcompound_linkingBook, 1) == 0) {
				currentItem.stackSize--;
				if (currentItem.stackSize == 0) {
					entityplayer.inventory.mainInventory[entityplayer.inventory.currentItem] = null;
				}
			}
		}
		else if (currentItem.itemID == Item.shears.shiftedIndex) {
			int removed = mod_MLB.linkingBook.removePages(nbttagcompound_linkingBook, Item.paper.maxStackSize);
			if (removed > 0) {
				ItemStack itemstack = new ItemStack(Item.paper, removed, Item.paper.getMaxDamage());
				
				// The following part is taken from BlockChest.onBlockRemoval(...):
				EntityItem entityitem = new EntityItem(world, i + 0.5, j + 1, k + 0.5, itemstack);
				float f3 = 0.05F;
				entityitem.motionX = (float)random.nextGaussian() * f3;
				entityitem.motionY = (float)random.nextGaussian() * f3 + 0.2F;
				entityitem.motionZ = (float)random.nextGaussian() * f3;
				entityitem.setVelocity(0, 0, 0);
				world.spawnEntityInWorld(entityitem);
				// End of the part from BlockChest.onBlockRemoval(...).
				
				currentItem.damageItem(1, entityplayer);
			}
		}
		else if (currentItem.itemID == Item.feather.shiftedIndex && currentItem.stackSize > 0) {
			mod_MLB.linkingBook.setName(nbttagcompound_linkingBook, "");
			openGui = GuiLinkingBook.class;
		}
		else if (currentItem.itemID == Item.flintAndSteel.shiftedIndex && currentItem.stackSize > 0) return false;
		else if (currentItem.itemID == Item.painting.shiftedIndex && currentItem.stackSize > 0) {
			openGui = GuiLookOfLinkingBook.class;
		}
		else if (currentItem.itemID == Item.stick.shiftedIndex) { // For debugging only
			// entityplayer.inventory.addItemStackToInventory(new ItemStack(Item.feather, 64, 0));
			// entityplayer.inventory.addItemStackToInventory(new ItemStack(Item.paper, 64, 0));
			// entityplayer.inventory.addItemStackToInventory(new ItemStack(Item.dyePowder, 64, 0)); // Damage must be 0 here to get the ink sack.
			// entityplayer.inventory.addItemStackToInventory(new ItemStack(Item.redstone, 64, 0));
			// entityplayer.inventory.addItemStackToInventory(new ItemStack(Block.cloth, 64, 0)); // Damage 0 is the white wool.
			// entityplayer.inventory.addItemStackToInventory(new ItemStack(Item.flintAndSteel, 1, 0));
			// entityplayer.inventory.addItemStackToInventory(new ItemStack(Block.obsidian, 64, 0));
		}
		else {
			openGui = GuiLinkingBook.class;
		}
		
		if (openGui == GuiLinkingBook.class) {
			ModLoader.OpenGUI(entityplayer, new GuiLinkingBook(entityplayer, tileEntityLinkingBook, mod_MLB));
		}
		else if (openGui == GuiLookOfLinkingBook.class) {
			ModLoader.OpenGUI(entityplayer, new GuiLookOfLinkingBook(entityplayer, tileEntityLinkingBook, mod_MLB));
		}
		return true;
	}
	
	@Override
	public void onNeighborBlockChange(World world, int i, int j, int k, int id) {
		if (id > 0 && Block.blocksList[id].canProvidePower()) {
			boolean powered = world.isBlockIndirectlyGettingPowered(i, j, k);
			TileEntityLinkingBook tileEntityLinkingBook = (TileEntityLinkingBook)world.getBlockTileEntity(i, j, k);
			tileEntityLinkingBook.setPoweredState(powered);
		}
	}
	
	/**
	 * Drops the Linking Book and its inventory as items when the block is removed (harvested).<br>
	 * In order to keep the datas from the Linking Book with the dropped item, the dropping code must be rewritten. The datas are stored in the associated {@code ItemStack}. Also we prevent Minecraft from dropping the item by overriding the method {@link quantityDropped}.
	 */
	@Override
	public void onBlockRemoval(World world, int i, int j, int k) {
		// Prepare our ItemStack, giving it a new NBTTagCompound to store the datas:
		TileEntityLinkingBook tileEntityLinkingBook = (TileEntityLinkingBook)world.getBlockTileEntity(i, j, k);
		ItemStack itemstack = new ItemStack(blockID, 1, damageDropped(0));
		itemstack.setTagCompound(tileEntityLinkingBook.nbttagcompound_linkingBook);
		dropItemStack(world, i, j, k, itemstack);
		
		for (int z = 0; z < tileEntityLinkingBook.inventoryLinkingBook.getSizeInventory(); z++) {
			itemstack = tileEntityLinkingBook.inventoryLinkingBook.getStackInSlot(z);
			if (itemstack != null) {
				dropItemStack(world, i, j, k, itemstack);
			}
		}
		
		super.onBlockRemoval(world, i, j, k);
	}
	
	/**
	 * Helper method that drops an ItemStack in the world.
	 * 
	 * @see onBlockRemoval
	 */
	public void dropItemStack(World world, int i, int j, int k, ItemStack itemStack) {
		// The following part is taken from Block.dropBlockAsItem_do(...):
		float f = 0.7F;
		double d = world.rand.nextFloat() * f + (1.0F - f) * 0.5D;
		double d1 = world.rand.nextFloat() * f + (1.0F - f) * 0.5D;
		double d2 = world.rand.nextFloat() * f + (1.0F - f) * 0.5D;
		EntityItem entityitem = new EntityItem(world, i + d, j + d1, k + d2, itemStack);
		entityitem.delayBeforeCanPickup = 10;
		
		// This instruction is from BlockChest.onBlockRemoval(...):
		if (itemStack.getTagCompound() != null) {
			entityitem.item.setTagCompound((NBTTagCompound)itemStack.getTagCompound().cloneTag());
		}
		
		world.spawnEntityInWorld(entityitem);
		// End of the part from Block.dropBlockAsItem_do(...).
	}
	
	/**
	 * Prevents Minecraft to drop the Linking Book as an item when the block is removed (harvested).<br>
	 * Instead, the Linking Book is dropped by the mod in the overridden {@link onBlockRemoval}.
	 */
	@Override
	public int quantityDropped(Random random) {
		return 0;
	}
}
