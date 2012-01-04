
package MCP.mod_mystlinkingbook;

import java.util.Random;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

/**
 * Represents Linking Books that are placed in the world as {@code Block}.<br>
 * <br>
 * The {@code Block}s are actually {@code BlockContainers}, meaning that they contain associated datas.
 * The Linking Books datas are thus stored by the associated {@code TileEntityLinkingBook} objects.<br>
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
	
	/**
	 * Reference to the mod instance.
	 */
	public mod_mystlinkingbook	mod_mystlinkingbook;
	
	public BlockLinkingBook(int blockID, int textureID, Material material) {
		super(blockID, textureID, material);
		
		this.setHardness(1F);
		this.setResistance(2.0F);
		this.setStepSound(soundWoodFootstep);
		this.setBlockName("linkingbookblock");
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
		this.mod_mystlinkingbook.linkingBook.teleportToDestination(tileEntityLinkingBook.nbttagcompound_linkingBook, entityplayer);
		return true;
	}
	
	/**
	 * Drops the Linking Book as an item when the block is removed (harvested).<br>
	 * In order to keep the datas from the Linking Book with the dropped item, the dropping code must be rewritten. The datas are stored in the associated {@code ItemStack}.
	 * Also we prevent Minecraft from dropping the item by overriding the method {@link quantityDropped}.
	 * 
	 */
	@Override
	public void onBlockRemoval(World world, int i, int j, int k) {
		// Prepare our ItemStack, giving it a new NBTTagCompound to store the datas:
		ItemStack itemstack = new ItemStack(this.blockID, 1, this.damageDropped(0));
		TileEntityLinkingBook tileEntityLinkingBook = (TileEntityLinkingBook) world.getBlockTileEntity(i, j, k);
		itemstack.func_40706_d(tileEntityLinkingBook.nbttagcompound_linkingBook);
		
		// The following part is taken from Block.dropBlockAsItem_do(...):
		float f = 0.7F;
		double d = (world.rand.nextFloat() * f) + ((1.0F - f) * 0.5D);
		double d1 = (world.rand.nextFloat() * f) + ((1.0F - f) * 0.5D);
		double d2 = (world.rand.nextFloat() * f) + ((1.0F - f) * 0.5D);
		EntityItem entityitem = new EntityItem(world, i + d, j + d1, k + d2, itemstack);
		entityitem.delayBeforeCanPickup = 10;
		
		// This instruction is from BlockChest.onBlockRemoval(...):
		entityitem.item.func_40706_d((NBTTagCompound) itemstack.func_40709_o().func_40195_b());
		
		world.entityJoinedWorld(entityitem);
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
