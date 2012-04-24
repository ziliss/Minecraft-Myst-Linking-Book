package net.minecraft.src.mystlinkingbook;

import java.util.List;

import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemBlock;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;
import net.minecraft.src.mystlinkingbook.ResourcesManager.SpriteResource;

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
	
	public SpriteResource icon;
	public SpriteResource pages;
	public SpriteResource unwritten;
	public SpriteResource unwrittenPages;
	
	public int itemsTexture; // For texture /gui/items.png
	
	public ItemBlockLinkingBook(int itemID, SpriteResource icon, SpriteResource unwritten, SpriteResource pages, SpriteResource unwrittenPages, Mod_MystLinkingBook mod_MLB) {
		super(itemID);
		
		this.mod_MLB = mod_MLB;
		
		this.icon = icon;
		this.pages = pages;
		this.unwritten = unwritten;
		this.unwrittenPages = unwrittenPages;
		
		itemsTexture = mod_MLB.mc.renderEngine.getTexture("/gui/items.png");
		
		setItemName("linkingBookItem");
		setIconIndex(icon.getSpriteId()); // Instead of the icon from the block set in the constructor of ItemBlock
		// setMaxStackSize(1);
		setMaxDamage(0);
	}
	
	// Should the icon be rendered in 2 passes ?
	@Override
	public boolean func_46058_c() {
		return true;
	}
	
	/**
	 * Called if {@code func_46058_c()} is true. Returns the iconIndex to be used to render the item.<br>
	 * j is the pass number. First pass we render the normal sprite. Second pass, only the pages, which will be colored.
	 */
	@Override
	public int func_46057_a(int i, int j) {
		// Workaround a limitation when rendering an item in the hand.
		// ItemRenderer.renderItem() only checks for itemID < 256, not for func_46058_c(), and thus it uses the terrain texture.
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, itemsTexture);
		
		//@formatter:off
		return (i & 16) == 16 ? // Is it written ? Yes if the fourth bit is set.
				j == 0 ? iconIndex : pages.getSpriteId()
			   : j == 0 ? unwritten.getSpriteId() : unwrittenPages.getSpriteId();
		//@formatter:on
	}
	
	/**
	 * Called if {@code func_46058_c()} is true. Returns the color to be used to color the item.<br>
	 * j is the pass number. First pass is white. Second pass is the color of the pages.
	 */
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
		itemstack.setTagCompound(mod_MLB.linkingBookUtils.createNew());
	}
	
	/**
	 * Allows to place the Linking Book as a block if the Linking Book's destination has already been set.<br>
	 * It also associate the Linking Book's datas with the new block.<br>
	 * <br>
	 * When the player right-clicks with a Linking Book item in hand, the method {@code onItemUse} is called before the method {@code onItemRightClick}. The later is called only if the former returns true.
	 * 
	 * @see #onItemRightClick
	 */
	@Override
	public boolean onItemUse(ItemStack itemstack, EntityPlayer entityplayer, World world, int i, int j, int k, int l) {
		NBTTagCompound nbttagcompound = mod_MLB.linkingBookUtils.checkAndUpdateOldFormat(itemstack.getTagCompound());
		// In case onCreated() has never been called on this item:
		if (nbttagcompound == null) {
			onCreated(itemstack, world, entityplayer);
			nbttagcompound = itemstack.getTagCompound();
		}
		if (mod_MLB.linkingBookUtils.isWritten(nbttagcompound)) {
			
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
				tileEntity.onPlacedInWorld(nbttagcompound);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Opens the writing GUI.
	 * 
	 * @see #onItemUse
	 */
	@Override
	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer) {
		NBTTagCompound nbttagcompound = itemstack.getTagCompound();
		// In case onCreated() was not called on this item:
		if (nbttagcompound == null) {
			onCreated(itemstack, world, entityplayer);
			nbttagcompound = itemstack.getTagCompound();
		}
		if (!mod_MLB.linkingBookUtils.isWritten(nbttagcompound)) {
			ModLoader.openGUI(entityplayer, new GuiWriteLinkingBook(entityplayer, nbttagcompound, mod_MLB));
		}
		return itemstack;
	}
	
	/**
	 * Adds the name of the linking book to the tooltip displayed on mouse hover in the inventory.
	 */
	@Override
	public void addInformation(ItemStack itemstack, List list) {
		if (!itemstack.hasTagCompound()) return; // In case the item has not yet been created (for example in the crafting result slot).
		String name = mod_MLB.linkingBookUtils.getName(itemstack.getTagCompound());
		if (!name.isEmpty()) {
			list.add(name);
		}
	}
}
