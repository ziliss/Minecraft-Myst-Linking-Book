package net.minecraft.src.mystlinkingbook;

import java.util.List;

import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemBlock;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;

import org.lwjgl.opengl.GL11;

/**
 * Represents Linking Books as {@code Item} (ie. either dropped in the world or in the inventory).<br>
 * <br>
 * The {@code Item}s are actually {@code ItemBlock}. This defines a kind of item, and its associated block (a {@code BlockLinkingBook}). The Linking Books datas are stored in the associated {@code ItemStack}.<br>
 * <br>
 * There is only 1 instance of this class.
 * 
 * @author ziliss
 * @see BlockLinkingBook
 * @see net.minecraft.src.ItemBlock
 * @see net.minecraft.src.ItemStack
 * @since 0.1a
 */
public class ItemBlockLinkingBook extends ItemBlock {
	
	/**
	 * Reference to the mod instance.
	 */
	public Mod_MystLinkingBook mod_MLB;
	
	public int pagesIconIndex;
	public int unwrittenIconIndex;
	public int unwrittenPagesIconIndex;
	
	public int itemsTexture = ModLoader.getMinecraftInstance().renderEngine.getTexture("/gui/items.png");
	
	public ItemBlockLinkingBook(int itemID, Mod_MystLinkingBook mod_MLB) {
		super(itemID);
		
		this.mod_MLB = mod_MLB;
		
		setItemName("linkingBookItem");
		setMaxStackSize(1);
		setMaxDamage(0);
	}
	
	// Should the icon be rendered in 2 passes ?
	@Override
	public boolean func_46058_c() {
		return true;
	}
	
	@Override
	public int func_46057_a(int i, int j) {
		// Workaround a limitation when rendering an item in the hand.
		// ItemRenderer.renderItem() only checks for itemID < 256, not for func_46058_c(), and thus it uses the terrain texture.
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, itemsTexture);
		
		//@formatter:off
		return (i & 16) == 16 ? // Is it written ? Yes if the fourth bit is set.
				j == 0 ? iconIndex : pagesIconIndex
			   : j == 0 ? unwrittenIconIndex : unwrittenPagesIconIndex;
		//@formatter:on
	}
	
	@Override
	public int getColorFromDamage(int i, int j) {
		return j == 0 ? 0xffffff : ItemPage.brighterColorInts[i & 15];
	}
	
	/**
	 * Associate the new Linking Book datas when the Item is crafted.<br>
	 * <br>
	 * Unfortunately this is not perfect. Mods can create items without calling this method. The inventory in creative mode does it too.
	 */
	@Override
	public void onCreated(ItemStack itemstack, World world, EntityPlayer entityplayer) {
		itemstack.setTagCompound(new NBTTagCompound());
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
		NBTTagCompound nbttagcompound = itemstack.getTagCompound();
		// In case onCreated() was not called on this item:
		if (nbttagcompound == null) {
			onCreated(itemstack, world, entityplayer);
			nbttagcompound = itemstack.getTagCompound();
		}
		if (mod_MLB.linkingBook.isWritten(nbttagcompound)) {
			
			int x = i, y = j, z = k, side = l;
			
			// The following part is taken from ItemBlock.onItemUse(...).
			// It tells us the position of the new block depending on the side (int l) of the aimed block:
			int i1 = world.getBlockId(i, j, k);
			if (i1 == Block.snow.blockID) {
				l = 0;
			}
			else if (i1 != Block.vine.blockID) {
				if (l == 0) {
					j--;
				}
				else if (l == 1) {
					j++;
				}
				else if (l == 2) {
					k--;
				}
				else if (l == 3) {
					k++;
				}
				else if (l == 4) {
					i--;
				}
				else if (l == 5) {
					i++;
				}
			}
			// End of the part from ItemBlock.onItemUse(...).
			
			// if (world.getBlockTileEntity(i, j, k) != null) return false;
			
			// Now we try to place the block, then if the block has been placed, set it's tileEntity:
			if (super.onItemUse(itemstack, entityplayer, world, x, y, z, side)) {
				
				TileEntityLinkingBook tileEntity = (TileEntityLinkingBook)world.getBlockTileEntity(i, j, k);
				if (tileEntity == null) {
					tileEntity = (TileEntityLinkingBook)mod_MLB.blockLinkingBook.getBlockEntity();
					world.setBlockTileEntity(i, j, k, tileEntity);
				}
				tileEntity.nbttagcompound_linkingBook = nbttagcompound;
				tileEntity.notifyColorChanged();
				tileEntity.onNeighborBlockChange(Block.redstoneWire.blockID);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Opens the linking GUI.
	 * 
	 * @see onItemUse
	 */
	@Override
	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer) {
		NBTTagCompound nbttagcompound = itemstack.getTagCompound();
		// In case onCreated() was not called on this item:
		if (nbttagcompound == null) {
			onCreated(itemstack, world, entityplayer);
			nbttagcompound = itemstack.getTagCompound();
		}
		if (!mod_MLB.linkingBook.isWritten(nbttagcompound)) {
			ModLoader.openGUI(entityplayer, new GuiWriteLinkingBook(entityplayer, nbttagcompound, mod_MLB));
		}
		return itemstack;
	}
	
	/**
	 * Returns the name of the linking book that is displayed on mouse hover in the inventory.
	 */
	@Override
	public void addInformation(ItemStack itemstack, List list) {
		String name = mod_MLB.linkingBook.getName(itemstack.getTagCompound());
		if (!name.isEmpty()) {
			list.add(name);
		}
	}
}
