package net.minecraft.src.mystlinkingbook;

import java.util.List;

import net.minecraft.src.BaseMod;
import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.InventoryBasic;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.TileEntity;
import net.minecraft.src.mystlinkingbook.LinkingBook.SpreadState;

/**
 * Manages the datas associated with {@code BlockLinkingBook}s.<br>
 * Manages the powered state, the inventory, and the opening of the linking book.<br>
 * <br>
 * For each Linking Book Block there is an associated {@code TileEntityLinkingBook} object.
 * 
 * @author ziliss
 * @see BlockLinkingBook
 * @see net.minecraft.src.mod_mystlinkingbookpack.Block
 * @see net.minecraft.src.BlockEntity
 * @since 0.1a
 */
public class TileEntityLinkingBook extends TileEntity {
	
	/**
	 * Reference to the mod instance.
	 */
	public Mod_MystLinkingBook mod_MLB;
	
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
	
	public double playerRange = 1.18D;
	public double rangeCenterHoriz = 0.60D;
	public double rangeCenterVert = 1.8D;
	
	public boolean isTopBlocked;
	
	/** Whether the Block of the linking book is powered. */
	public boolean isPowered;
	
	public float bookSpread = 0;
	public float bookSpreadPrev = 0;
	
	protected NBTTagCompound nbttagcompound_linkingBookTemp = null;
	public LinkingBook linkingBook;
	
	public GuiLinkingBook guiLinkingBook = null;
	
	public TileEntityLinkingBook() {
		this(getMod_MLB());
	}
	
	public TileEntityLinkingBook(Mod_MystLinkingBook mod_MLB) {
		this.mod_MLB = mod_MLB;
	}
	
	private static final Mod_MystLinkingBook getMod_MLB() {
		Mod_MystLinkingBook mod_MLB = null;
		List<BaseMod> mods = ModLoader.getLoadedMods();
		for (BaseMod mod : mods) {
			if (mod instanceof Mod_MystLinkingBook) {
				mod_MLB = (Mod_MystLinkingBook)mod;
				break;
			}
		}
		if (mod_MLB == null) throw new ExceptionInInitializerError("Could not find the Mod_MystLinkingBook instance.");
		else return mod_MLB;
	}
	
	public void onNeighborBlockChange(int id) {
		switch (worldObj.getBlockId(xCoord, yCoord + 1, zCoord)) {
			case 0: // air
			case 50: // torch
			case 51: // fire
			case 68: // wall sign
			case 69: // lever
			case 76: // redstone torch
			case 77: // button
				isTopBlocked = false;
				break;
			default:
				isTopBlocked = true;
				if (guiLinkingBook != null) {
					mod_MLB.mc.displayGuiScreen(null);
				}
		}
		
		if (id == 0 || id > 0 && Block.blocksList[id].canProvidePower()) {
			isPowered = worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
			linkingBook.notifyPoweredChanged(isPowered);
		}
	}
	
	@Override
	public void invalidate() {
		if (guiLinkingBook != null) {
			mod_MLB.mc.displayGuiScreen(null);
		}
		linkingBook.invalidate();
		super.invalidate();
	}
	
	public void initBookState(NBTTagCompound nbttagcompound_linkingBook) {
		nbttagcompound_linkingBook = mod_MLB.linkingBookUtils.checkAndUpdateOldFormat(nbttagcompound_linkingBook);
		linkingBook = new LinkingBook(nbttagcompound_linkingBook, this, isPowered, bookSpread, mod_MLB);
		
		if (linkingBook.getStayOpen()) {
			setBookSpread(1F);
		}
	}
	
	public void onPlacedInWorld(NBTTagCompound nbttagcompound_linkingBook) {
		initBookState(nbttagcompound_linkingBook);
		onNeighborBlockChange(Block.redstoneWire.blockID);
	}
	
	/**
	 * Loads the datas from the disk. (Called by Minecraft)
	 */
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		isTopBlocked = nbttagcompound.getBoolean("topBlocked");
		isPowered = nbttagcompound.getBoolean("powered");
		if (nbttagcompound.hasKey("mlb")) {
			nbttagcompound_linkingBookTemp = nbttagcompound.getCompoundTag("mlb");
		}
		else {
			nbttagcompound_linkingBookTemp = nbttagcompound.getCompoundTag("tag");
		}
		
		int slotNb;
		NBTTagCompound nbttagcompound1;
		NBTTagList nbttaglist = nbttagcompound.getTagList("Inventory");
		for (int i = 0; i < nbttaglist.tagCount(); i++) {
			nbttagcompound1 = (NBTTagCompound)nbttaglist.tagAt(i);
			slotNb = nbttagcompound1.getByte("Slot") & 0xff;
			if (slotNb >= 0 && slotNb < inventoryLinkingBook.getSizeInventory()) {
				// Modify the following private field:
				// inventoryLinkingBook.inventoryContents[slotNb] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
				ItemStack[] inventoryContents = PrivateAccesses.InventoryBasic_inventoryContents.getFrom(inventoryLinkingBook);
				inventoryContents[slotNb] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
			}
		}
	}
	
	@Override
	public void validate() {
		super.validate();
		
		if (nbttagcompound_linkingBookTemp != null) {
			initBookState(nbttagcompound_linkingBookTemp);
			nbttagcompound_linkingBookTemp = null;
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
		nbttagcompound.setTag("mlb", linkingBook.getNBTTagCompound());
		
		ItemStack itemstack;
		NBTTagCompound nbttagcompound1;
		NBTTagList nbttaglist = new NBTTagList();
		for (int i = 0; i < inventoryLinkingBook.getSizeInventory(); i++) {
			itemstack = inventoryLinkingBook.getStackInSlot(i);
			if (itemstack != null) {
				nbttagcompound1 = new NBTTagCompound();
				nbttagcompound1.setByte("Slot", (byte)i);
				itemstack.writeToNBT(nbttagcompound1);
				nbttaglist.appendTag(nbttagcompound1);
			}
		}
		nbttagcompound.setTag("Inventory", nbttaglist);
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		
		EntityPlayer playerInRange = null;
		if (!isTopBlocked) {
			double dX = 0.5D;
			double dZ = 0.5D;
			// Get the coordinates of the front of the book, depending on orientation:
			switch (getBlockMetadata() & 3) {
				case 0:
					dZ += rangeCenterHoriz;
					break;
				case 1:
					dX -= rangeCenterHoriz;
					break;
				case 2:
					dZ -= rangeCenterHoriz;
					break;
				case 3:
					dX += rangeCenterHoriz;
					break;
			}
			playerInRange = worldObj.getClosestPlayer(xCoord + dX, yCoord + rangeCenterVert, zCoord + dZ, playerRange);
		}
		
		if (playerInRange != null || linkingBook.getStayOpen()) {
			setBookSpread(bookSpread + 0.1f);
		}
		else {
			setBookSpread(bookSpread - 0.1f);
		}
	}
	
	public void setBookSpread(float newBookSpread) {
		if (newBookSpread < 0f) {
			newBookSpread = 0f;
		}
		else if (newBookSpread > 1f) {
			newBookSpread = 1f;
		}
		
		bookSpreadPrev = bookSpread;
		boolean wasClosed = bookSpread == 0f;
		bookSpread = newBookSpread;
		boolean isClosed = bookSpread == 0f;
		
		if (isClosed != wasClosed) {
			if (isClosed) {
				linkingBook.linkingPanel.releaseImage();
			}
			else {
				linkingBook.linkingPanel.acquireImage();
			}
		}
		
		SpreadState spreadState;
		if (isClosed) {
			spreadState = SpreadState.closed;
		}
		else if (bookSpread > bookSpreadPrev) {
			spreadState = SpreadState.opening;
		}
		else if (bookSpread < bookSpreadPrev) {
			spreadState = SpreadState.closing;
		}
		else {
			spreadState = SpreadState.open;
		}
		linkingBook.notifyBookSpreadChanged(spreadState, bookSpread);
	}
	
	public boolean isInRange(EntityPlayer entityPlayer) {
		double dX = 0.5D;
		double dZ = 0.5D;
		// Get the coordinates of the front of the book, depending on orientation:
		switch (getBlockMetadata() & 3) {
			case 0:
				dZ += rangeCenterHoriz;
				break;
			case 1:
				dX -= rangeCenterHoriz;
				break;
			case 2:
				dZ -= rangeCenterHoriz;
				break;
			case 3:
				dX += rangeCenterHoriz;
				break;
		}
		return entityPlayer.getDistance(xCoord + dX, yCoord + rangeCenterVert, zCoord + dZ) <= playerRange;
	}
}
