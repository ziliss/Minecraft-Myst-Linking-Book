
package MCP.mod_mystlinkingbook;

import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;
import MCP.ApiController;
import MCP.api.BlockItemBase;

/**
 * Represents Linking Books as {@code Item} (ie. either dropped in the world or in the inventory).<br>
 * <br>
 * The {@code Item}s are actually {@code ItemBlock}. This defines a kind of item, and its associated block (a {@code BlockLinkingBook}).
 * The Linking Books datas are stored in the associated {@code ItemStack}.<br>
 * <br>
 * There is only 1 instance of this class.
 * 
 * @author ziliss
 * @see BlockLinkingBook
 * @see net.minecraft.src.ItemBlock
 * @see net.minecraft.src.ItemStack
 * @since 0.1a
 */
public class ItemBlockLinkingBook extends BlockItemBase {
	
	/**
	 * Reference to the mod instance.
	 */
	public mod_mystlinkingbook	mod_mystlinkingbook;
	
	public ItemBlockLinkingBook(ApiController ctrl, int textureID, mod_mystlinkingbook mod_mystlinkingbook) {
		super(ctrl.getBlockItemID(ItemBlockLinkingBook.class), textureID, Material.wood, BlockLinkingBook.class);
		
		this.mod_mystlinkingbook = mod_mystlinkingbook;
		((BlockLinkingBook) this.block).mod_mystlinkingbook = mod_mystlinkingbook;
		
		this.setItemName("itemblocklinkingbook");
		this.setMaxStackSize(1);
		this.setMaxDamage(0);
	}
	
	/**
	 * Associate the new Linking Book datas when the Item is crafted.<br>
	 * <br>
	 * Unfortunately this is not perfect. Mods can create items without calling this method. The inventory in creative mode does it too.
	 */
	@Override
	public void onCreated(ItemStack itemstack, World world, EntityPlayer entityplayer) {
		itemstack.func_40706_d(new NBTTagCompound());
	}
	
	/**
	 * Allows to place the Linking Book as a block if the Linking Book's destination has already been set.<br>
	 * It also associate the Linking Book's datas with the new block.<br>
	 * <br>
	 * When the player right-clicks with a Linking Book item in hand, the method {@code onItemRightClick} is called after the method {@code onItemUse} only if the later returns false.
	 * 
	 * @see onItemRightClick
	 */
	@Override
	public boolean onItemUse(ItemStack itemstack, EntityPlayer entityplayer, World world, int i, int j, int k, int l) {
		NBTTagCompound nbttagcompound = itemstack.func_40709_o();
		// In case onCreated() was not called on this item:
		if(nbttagcompound == null) {
			this.onCreated(itemstack, world, entityplayer);
		}
		if(this.mod_mystlinkingbook.linkingBook.isDestinationSet(nbttagcompound)) {
			boolean itemUsed = super.onItemUse(itemstack, entityplayer, world, i, j, k, l);
			if(itemUsed) {
				
				// The following part is taken from ItemBlock.onItemUse(...).
				// It tells us the position of the new block depending on the side (int l) of the aimed block:
				int i1 = world.getBlockId(i, j, k);
				if(i1 == Block.snow.blockID) {
					l = 0;
				}
				else if(i1 != Block.vine.blockID) {
					if(l == 0) {
						j--;
					}
					if(l == 1) {
						j++;
					}
					if(l == 2) {
						k--;
					}
					if(l == 3) {
						k++;
					}
					if(l == 4) {
						i--;
					}
					if(l == 5) {
						i++;
					}
				}
				// End of the part from ItemBlock.onItemUse(...).
				
				TileEntityLinkingBook tileEntity = (TileEntityLinkingBook) world.getBlockTileEntity(i, j, k);
				tileEntity.nbttagcompound_linkingBook = nbttagcompound;
			}
			return itemUsed;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Records the position of the player as the destination for this Linking Book.
	 * 
	 * @see onItemUse
	 */
	@Override
	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer) {
		NBTTagCompound nbttagcompound = itemstack.func_40709_o();
		if(!this.mod_mystlinkingbook.linkingBook.isDestinationSet(nbttagcompound)) {
			if(this.mod_mystlinkingbook.linkingBook.setDestination(nbttagcompound, entityplayer)) {
				entityplayer.swingItem();
			}
		}
		return itemstack;
	}
}
