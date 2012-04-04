package net.minecraft.src.mystlinkingbook;

import java.util.Random;

import net.minecraft.src.Block;
import net.minecraft.src.BlockContainer;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.MathHelper;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.mystlinkingbook.RessourcesManager.SpriteRessource;

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
	
	public SpriteRessource topSprite;
	public SpriteRessource sideSprite;
	public SpriteRessource bottomSprite;
	
	/**
	 * Reference to the mod instance.
	 */
	public Mod_MystLinkingBook mod_MLB;
	
	public int renderType;
	
	public BlockLinkingBook(int blockID, int textureID, SpriteRessource top, SpriteRessource side, SpriteRessource bottom, Mod_MystLinkingBook mod_MLB) {
		super(blockID, textureID, Material.wood);
		
		topSprite = top;
		sideSprite = side;
		bottomSprite = bottom;
		
		this.mod_MLB = mod_MLB;
		
		setHardness(1F);
		setResistance(2.0F);
		setStepSound(soundWoodFootstep);
		setBlockName("linkingBookBlock");
		
		setRequiresSelfNotify();
	}
	
	@Override
	public int getRenderType() {
		return renderType;
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
		int orientation = ((MathHelper.floor_double(entityliving.rotationYaw * 4F / 360F + 0.5D) & 3) + 2) % 4;
		world.setBlockMetadataWithNotify(i, j, k, orientation);
	}
	
	/**
	 * Method factory for new {@code BlockEntity} that keep datas.
	 * 
	 * @see net.minecraft.src.BlockContainer#getBlockEntity()
	 */
	@Override
	public TileEntity getBlockEntity() {
		return new TileEntityLinkingBook(mod_MLB);
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
				return topSprite.spriteId;
			case 1:
				return bottomSprite.spriteId;
			default:
				return sideSprite.spriteId;
		}
	}
	
	@Override
	public int colorMultiplier(IBlockAccess iblockaccess, int i, int j, int k) {
		TileEntityLinkingBook tileEntityLinkingBook = (TileEntityLinkingBook)iblockaccess.getBlockTileEntity(i, j, k);
		ItemStack itemStack = tileEntityLinkingBook.inventoryLinkingBook.getStackInSlot(0);
		if (itemStack != null && itemStack.stackSize > 0) {
			try {
				return Block.blocksList[itemStack.itemID].colorMultiplier(iblockaccess, i, j, k);
			}
			catch (Exception e) {
				System.err.println("id: " + itemStack.itemID);
				e.printStackTrace();
			}
		}
		
		return 0xffffff;
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
		boolean canUseBook = tileEntityLinkingBook.bookSpread >= 1f && tileEntityLinkingBook.isInRange(entityplayer);
		if (!canUseBook) return false;
		
		Class openGui = null;
		
		if (currentItem == null) {
			openGui = GuiLinkingBook.class;
		}
		else if (currentItem.itemID == mod_MLB.itemPage.shiftedIndex) {
			if (currentItem.stackSize > 0) {
				if (mod_MLB.linkingBook.addPages(nbttagcompound_linkingBook, currentItem, 1) != 0) {
					if (currentItem.stackSize == 0) {
						entityplayer.inventory.mainInventory[entityplayer.inventory.currentItem] = null;
					}
					tileEntityLinkingBook.notifyNbMissingPagesChanged();
				}
			}
		}
		else if (currentItem.itemID == Item.shears.shiftedIndex) {
			ItemStack itemstack = mod_MLB.linkingBook.removePages(nbttagcompound_linkingBook);
			if (itemstack != null) {
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
				tileEntityLinkingBook.notifyNbMissingPagesChanged();
			}
		}
		else if (currentItem.itemID == Item.feather.shiftedIndex) {
			if (mod_MLB.linkingBook.setName(nbttagcompound_linkingBook, "")) {
				currentItem.stackSize--;
				if (currentItem.stackSize == 0) {
					entityplayer.inventory.mainInventory[entityplayer.inventory.currentItem] = null;
				}
			}
			openGui = GuiLinkingBook.class;
		}
		else if (currentItem.itemID == Item.flintAndSteel.shiftedIndex) return false;
		else if (currentItem.itemID == Item.painting.shiftedIndex) {
			openGui = GuiLookOfLinkingBook.class;
		}
		else if (currentItem.itemID == Item.map.shiftedIndex) {
			openGui = GuiAgesArea.class;
		}
		else if (currentItem.itemID == Item.dyePowder.shiftedIndex) { // TODO: remove for 1.0
			mod_MLB.linkingBook.setPagesColorFromDye(nbttagcompound_linkingBook, currentItem.getItemDamage());
			tileEntityLinkingBook.notifyColorChanged();
		}
		else if (currentItem.itemID == Item.stick.shiftedIndex) { // For debugging only
			// entityplayer.inventory.addItemStackToInventory(new ItemStack(Item.pickaxeDiamond, 1, 0));
			// entityplayer.inventory.addItemStackToInventory(new ItemStack(Block.torchWood, 64, 0));
			// entityplayer.inventory.addItemStackToInventory(new ItemStack(Block.wood, 64, 0));
			// entityplayer.inventory.addItemStackToInventory(new ItemStack(Item.feather, 64, 0));
			// entityplayer.inventory.addItemStackToInventory(new ItemStack(Item.paper, 64, 0));
			// entityplayer.inventory.addItemStackToInventory(new ItemStack(Item.dyePowder, 64, 0)); // Damage must be 0 here to get the ink sack.
			// entityplayer.inventory.addItemStackToInventory(new ItemStack(Item.redstone, 64, 0));
			// entityplayer.inventory.addItemStackToInventory(new ItemStack(Item.flintAndSteel, 1, 0));
			// entityplayer.inventory.addItemStackToInventory(new ItemStack(Block.obsidian, 64, 0));
			// entityplayer.inventory.addItemStackToInventory(new ItemStack(Item.painting, 1, 0));
			// entityplayer.inventory.addItemStackToInventory(new ItemStack(Item.map, 1, 0));
			// entityplayer.inventory.addItemStackToInventory(new ItemStack(Block.cloth, 64, 0)); // Damage 0 is the white wool.
			// entityplayer.inventory.addItemStackToInventory(new ItemStack(Block.grass, 64, 0));
			// entityplayer.inventory.addItemStackToInventory(new ItemStack(Block.snow, 64, 0));
			// entityplayer.inventory.addItemStackToInventory(new ItemStack(Item.monsterPlacer, 64, 93)); // Chickens spawners
			// entityplayer.inventory.addItemStackToInventory(new ItemStack(Item.dyePowder, 64, 14));
			for (int d = 0; d < 0; d++) {
				entityplayer.inventory.addItemStackToInventory(new ItemStack(Item.dyePowder, 64, d));
			}
			// mod_MLB.mc.theWorld.getWorldInfo().setWorldTime(5000); // A bit before noon
			// mod_MLB.mc.theWorld.getWorldInfo().setRaining(false);
			// nbttagcompound_linkingBook.setInteger("nbPages", nbttagcompound_linkingBook.getInteger("nbPages") + 1);
		}
		else {
			openGui = GuiLinkingBook.class;
		}
		
		if (openGui == GuiLinkingBook.class) {
			ModLoader.openGUI(entityplayer, new GuiLinkingBook(entityplayer, tileEntityLinkingBook, mod_MLB));
		}
		else if (openGui == GuiLookOfLinkingBook.class) {
			ModLoader.openGUI(entityplayer, new GuiLookOfLinkingBook(entityplayer, tileEntityLinkingBook, mod_MLB));
		}
		else if (openGui == GuiAgesArea.class) {
			ModLoader.openGUI(entityplayer, new GuiAgesArea(entityplayer, mod_MLB));
		}
		return true;
	}
	
	@Override
	public void onEntityWalking(World world, int i, int j, int k, Entity entity) {
		super.onEntityWalking(world, i, j, k, entity);
		linkEntity(i, j, k, entity);
	}
	
	@Override
	public void onFallenUpon(World world, int i, int j, int k, Entity entity, float f) {
		super.onFallenUpon(world, i, j, k, entity, f);
		linkEntity(i, j, k, entity);
	}
	
	public void linkEntity(int i, int j, int k, Entity entity) {
		if (entity instanceof EntityLiving && !(entity instanceof EntityPlayer)) {
			World world = entity.worldObj;
			TileEntityLinkingBook tileEntityLinkingBook = (TileEntityLinkingBook)world.getBlockTileEntity(i, j, k);
			if (tileEntityLinkingBook.bookSpread >= 1f) {
				world.playSoundAtEntity(entity, mod_MLB.linkingsound.soundId, 1.0F, 1.0F);
				// world.playSoundEffect(i + 0.5D, j + 1.1D, k + 0.5D, "mystlinkingbook.linkingsound", 1.0F, 1.0F);
				new LinkingEntity(entity, tileEntityLinkingBook, mod_MLB);
			}
		}
	}
	
	@Override
	public void onNeighborBlockChange(World world, int i, int j, int k, int id) {
		TileEntityLinkingBook tileEntityLinkingBook = (TileEntityLinkingBook)world.getBlockTileEntity(i, j, k);
		tileEntityLinkingBook.onNeighborBlockChange(id);
	}
	
	/**
	 * Drops the Linking Book and its inventory as items when the block is removed (harvested).<br>
	 * In order to keep the datas from the Linking Book with the dropped item, the dropping code must be rewritten. The datas are stored in the associated {@code ItemStack}. Also we prevent Minecraft from dropping the item by overriding the method {@link quantityDropped}.
	 */
	@Override
	public void onBlockRemoval(World world, int i, int j, int k) {
		TileEntityLinkingBook tileEntityLinkingBook = (TileEntityLinkingBook)world.getBlockTileEntity(i, j, k);
		
		int color = mod_MLB.linkingBook.getPagesColor(tileEntityLinkingBook.nbttagcompound_linkingBook);
		
		// Drop nothing if it was burnt:
		if (isNeighborFire(world, i, j, k)) {
			super.onBlockRemoval(world, i, j, k);
			return;
		}
		
		// Prepare our ItemStack, giving it a new NBTTagCompound to store the datas:
		int written = (true ? 1 : 0) << 4;
		ItemStack itemstack = new ItemStack(blockID, 1, color | written);
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
			entityitem.item.setTagCompound((NBTTagCompound)itemStack.getTagCompound().copy());
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
	
	private boolean isNeighborFire(World world, int i, int j, int k) {
		int fireID = Block.fire.blockID;
		//@formatter:off
		return world.getBlockId(i + 1, j, k) == fireID || world.getBlockId(i - 1, j, k) == fireID
			|| world.getBlockId(i, j + 1, k) == fireID || world.getBlockId(i, j - 1, k) == fireID
			|| world.getBlockId(i, j, k + 1) == fireID || world.getBlockId(i, j, k - 1) == fireID;
		//@formatter:on
	}
}
