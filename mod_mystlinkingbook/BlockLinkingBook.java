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
	
	/**
	 * Reference to the mod instance.
	 */
	public mod_mystlinkingbook mod_MLB;
	
	public BlockLinkingBook(int blockID, int textureID, Material material, mod_mystlinkingbook mod_MLB) {
		super(blockID, textureID, material);
		
		this.mod_MLB = mod_MLB;
		
		setHardness(1F);
		setResistance(2.0F);
		setStepSound(soundWoodFootstep);
		setBlockName("linkingBookBlock");
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
	
	/**
	 * Teleports the player when a {@code Block} is activated (right-clicked).
	 */
	@Override
	public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer) {
		TileEntityLinkingBook tileEntityLinkingBook = (TileEntityLinkingBook) world.getBlockTileEntity(i, j, k);
		NBTTagCompound nbttagcompound_linkingBook = tileEntityLinkingBook.nbttagcompound_linkingBook;
		
		ItemStack currentItem = entityplayer.inventory.getCurrentItem();
		boolean openGui = false;
		if (currentItem == null) {
			openGui = true;
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
				entityitem.motionX = (float) random.nextGaussian() * f3;
				entityitem.motionY = (float) random.nextGaussian() * f3 + 0.2F;
				entityitem.motionZ = (float) random.nextGaussian() * f3;
				entityitem.setVelocity(0, 0, 0);
				world.spawnEntityInWorld(entityitem);
				// End of the part from BlockChest.onBlockRemoval(...).
				
				currentItem.damageItem(1, entityplayer);
			}
		}
		else if (currentItem.itemID == Item.feather.shiftedIndex && currentItem.stackSize > 0) {
			mod_MLB.linkingBook.setName(nbttagcompound_linkingBook, "");
			openGui = true;
		}
		else {
			openGui = true;
		}
		
		if (openGui) {
			ModLoader.OpenGUI(entityplayer, new GuiLinkingBook(entityplayer, tileEntityLinkingBook, mod_MLB));
		}
		return true;
	}
	
	@Override
	public void onNeighborBlockChange(World world, int i, int j, int k, int id) {
		if (id > 0 && Block.blocksList[id].canProvidePower()) {
			boolean powered = world.isBlockIndirectlyGettingPowered(i, j, k);
			TileEntityLinkingBook tileEntityLinkingBook = (TileEntityLinkingBook) world.getBlockTileEntity(i, j, k);
			tileEntityLinkingBook.setPoweredState(powered);
		}
	}
	
	/**
	 * Drops the Linking Book as an item when the block is removed (harvested).<br>
	 * In order to keep the datas from the Linking Book with the dropped item, the dropping code must be rewritten. The datas are stored in the associated {@code ItemStack}. Also we prevent Minecraft from dropping the item by overriding the method {@link quantityDropped}.
	 * 
	 */
	@Override
	public void onBlockRemoval(World world, int i, int j, int k) {
		// Prepare our ItemStack, giving it a new NBTTagCompound to store the datas:
		ItemStack itemstack = new ItemStack(blockID, 1, damageDropped(0));
		NBTTagCompound nbttagcompound_linkingBook = ((TileEntityLinkingBook) world.getBlockTileEntity(i, j, k)).nbttagcompound_linkingBook;
		itemstack.setTagCompound(nbttagcompound_linkingBook);
		
		// The following part is taken from Block.dropBlockAsItem_do(...):
		float f = 0.7F;
		double d = world.rand.nextFloat() * f + (1.0F - f) * 0.5D;
		double d1 = world.rand.nextFloat() * f + (1.0F - f) * 0.5D;
		double d2 = world.rand.nextFloat() * f + (1.0F - f) * 0.5D;
		EntityItem entityitem = new EntityItem(world, i + d, j + d1, k + d2, itemstack);
		entityitem.delayBeforeCanPickup = 10;
		
		// This instruction is from BlockChest.onBlockRemoval(...):
		entityitem.item.setTagCompound((NBTTagCompound) itemstack.getTagCompound().cloneTag());
		
		world.spawnEntityInWorld(entityitem);
		// End of the part from Block.dropBlockAsItem_do(...).
		
		super.onBlockRemoval(world, i, j, k);
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
