
package MCP.mod_mystlinkingbook;

import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;

/**
 * Keeps the datas associated with {@code BlockLinkingBook}s.<br>
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
	public NBTTagCompound	nbttagcompound_linkingBook;
	
	/**
	 * Loads the datas from the disk. (Called by Minecraft)
	 */
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		this.nbttagcompound_linkingBook = nbttagcompound.getCompoundTag("tag");
	}
	
	/**
	 * Saves the datas to the disk. (Called by Minecraft)
	 */
	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setTag("tag", this.nbttagcompound_linkingBook);
	}
	
}
