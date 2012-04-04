package net.minecraft.src.mystlinkingbook;

import java.awt.Color;
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
	
	public double playerRange = 1.18D;
	public double rangeCenterHoriz = 0.60D;
	public double rangeCenterVert = 1.8D;
	
	/**
	 * True if the linking book links to another Age.
	 */
	boolean linksToDifferentAge;
	int lastUpdateCounter = -1;
	
	public boolean isTopBlocked;
	
	/**
	 * Does the linking book need power to be able to link ?
	 */
	public boolean isUnstable;
	
	/**
	 * Whether the Block of the linking book is powered.
	 */
	public boolean isPowered;
	
	public int nbPages;
	public int maxPages;
	public int missingPages;
	
	public Color color;
	
	public boolean stayOpen;
	
	public LinkingPanel linkingPanel;
	
	public GuiLinkingBook guiLinkingBook = null;
	
	public TileEntityLinkingBook() {
		this(getMod_MLB());
	}
	
	public TileEntityLinkingBook(Mod_MystLinkingBook mod_MLB) {
		this.mod_MLB = mod_MLB;
		linkingPanel = new LinkingPanel(this);
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
					ModLoader.getMinecraftInstance().displayGuiScreen(null);
				}
		}
		
		if (id == 0 || id > 0 && Block.blocksList[id].canProvidePower()) {
			isPowered = worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
		}
	}
	
	public void notifyNbMissingPagesChanged() {
		nbPages = mod_MLB.linkingBook.getNbPages(nbttagcompound_linkingBook);
		maxPages = mod_MLB.linkingBook.getMaxPages(nbttagcompound_linkingBook);
		missingPages = maxPages - nbPages;
		linkingPanel.notifyNbMissingPagesChanged();
		if (guiLinkingBook != null) {
			guiLinkingBook.notifyNbMissingPagesChanged();
		}
	}
	
	public void notifyColorChanged() {
		color = ItemPage.colorTable[mod_MLB.linkingBook.getPagesColor(nbttagcompound_linkingBook)];
		if (guiLinkingBook != null) {
			guiLinkingBook.notifyColorChanged();
		}
	}
	
	public void notifyLinkingPanelImageChanged() {
		linkingPanel.notifyLinkingPanelImageChanged();
		if (guiLinkingBook != null) {
			guiLinkingBook.notifyLinkingPanelImageChanged(linkingPanel);
		}
	}
	
	public void notifyStayOpenChanged() {
		stayOpen = mod_MLB.linkingBook.getStayOpen(nbttagcompound_linkingBook);
	}
	
	public boolean getLinksToDifferentAge() {
		if (lastUpdateCounter != mod_MLB.linkingBook.agesManager.updateCounter) {
			linksToDifferentAge = mod_MLB.linkingBook.doLinkToDifferentAge(this);
			lastUpdateCounter = mod_MLB.linkingBook.agesManager.updateCounter;
		}
		return linksToDifferentAge;
	}
	
	public boolean canLink() {
		return missingPages == 0 && (isUnstable ? isPowered : true) && getLinksToDifferentAge();
	}
	
	@Override
	public void invalidate() {
		if (guiLinkingBook != null) {
			ModLoader.getMinecraftInstance().displayGuiScreen(null);
		}
		linkingPanel.invalidate();
		lastUpdateCounter = -1;
		super.invalidate();
	}
	
	public void onPlacedInWorld(NBTTagCompound nbttagcompound_linkingBook) {
		this.nbttagcompound_linkingBook = mod_MLB.linkingBook.checkAndUpdateOldFormat(nbttagcompound_linkingBook);
		onNeighborBlockChange(Block.redstoneWire.blockID);
		initBookState();
	}
	
	public void initBookState() {
		isUnstable = mod_MLB.linkingBook.isUnstable(nbttagcompound_linkingBook);
		
		notifyNbMissingPagesChanged();
		notifyColorChanged();
		notifyStayOpenChanged();
		if (stayOpen) {
			setBookSpread(1F);
		}
		notifyLinkingPanelImageChanged();
	}
	
	/**
	 * Loads the datas from the disk. (Called by Minecraft)
	 */
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		isTopBlocked = nbttagcompound.getBoolean("topBlocked");
		isPowered = nbttagcompound.getBoolean("powered");
		nbttagcompound_linkingBook = mod_MLB.linkingBook.checkAndUpdateOldFormat(nbttagcompound.getCompoundTag("tag"));
		initBookState();
		
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
				nbttaglist.appendTag(nbttagcompound1);
			}
		}
		nbttagcompound.setTag("Inventory", nbttaglist);
	}
	
	// Everything below comes from TileEntityEnchantmentTable:
	public float bookSpread = 0;
	public float bookSpreadPrev = 0;
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		bookSpreadPrev = bookSpread;
		
		EntityPlayer closestPlayer = null;
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
			closestPlayer = worldObj.getClosestPlayer(xCoord + dX, yCoord + rangeCenterVert, zCoord + dZ, playerRange);
		}
		
		boolean wasClosed = bookSpread == 0f;
		if (closestPlayer != null || stayOpen) {
			if (bookSpread < 1.0F) {
				bookSpread += 0.1F;
				if (bookSpread > 1F) {
					bookSpread = 1F;
				}
			}
		}
		else {
			if (bookSpread > 0) {
				bookSpread -= 0.1F;
				if (bookSpread < 0F) {
					bookSpread = 0F;
				}
			}
		}
		boolean isClosed = bookSpread == 0f;
		
		if (isClosed != wasClosed) {
			if (isClosed) {
				linkingPanel.releaseLinkingPanel();
			}
			else {
				linkingPanel.acquireLinkingPanel();
			}
		}
	}
	
	public void setBookSpread(float newBookSpread) {
		if (newBookSpread < 0) {
			newBookSpread = 0F;
		}
		else if (newBookSpread > 1) {
			newBookSpread = 1F;
		}
		
		boolean wasClosed = bookSpread == 0f;
		bookSpread = newBookSpread;
		boolean isClosed = bookSpread == 0f;
		
		if (isClosed != wasClosed) {
			if (isClosed) {
				linkingPanel.releaseLinkingPanel();
			}
			else {
				linkingPanel.acquireLinkingPanel();
			}
		}
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
