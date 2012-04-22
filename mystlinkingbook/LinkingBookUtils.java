package net.minecraft.src.mystlinkingbook;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import net.minecraft.client.Minecraft;
import net.minecraft.src.BlockCloth;
import net.minecraft.src.Chunk;
import net.minecraft.src.ChunkProviderLoadOrGenerate;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IChunkProvider;
import net.minecraft.src.ItemStack;
import net.minecraft.src.LoadingScreenRenderer;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;
import net.minecraft.src.WorldProvider;
import net.minecraft.src.mystlinkingbook.ScheduledActionsManager.IScheduledAction;
import net.minecraft.src.mystlinkingbook.ScheduledActionsManager.ScheduledActionRef;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 * Contains the methods to interact with the datas of the Linking Books.<br>
 * <br>
 * The datas of the Linking Books are stored in a format called NBT (Named Binary Tag), like any other map datas in Minecraft.<br>
 * <br>
 * There is only 1 instance of this class.
 * 
 * @author ziliss
 * @since 0.1a
 */
public class LinkingBookUtils {
	
	/**
	 * Reference to the mod instance.
	 */
	public Mod_MystLinkingBook mod_MLB;
	
	public Random rand = new Random();
	
	public World lastUsedWorld = null;
	
	public WorldPreloader worldPreloader;
	
	public AgesManager agesManager;
	
	public LinkingBookUtils(Mod_MystLinkingBook mod_MLB) {
		this.mod_MLB = mod_MLB;
		worldPreloader = new WorldPreloader(mod_MLB.mc);
		agesManager = new AgesManager(mod_MLB);
	}
	
	public boolean isWritten(NBTTagCompound nbttagcompound) {
		return nbttagcompound.getBoolean("dest");
	}
	
	public NBTTagCompound createNew() {
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		// nbttagcompound.setByte("ver", (byte)0);
		return nbttagcompound;
	}
	
	public int createNewRandomId() {
		int randId;
		do {
			randId = rand.nextInt();
		} while (randId >= -1 && randId <= 255);
		return randId;
	}
	
	// Only for current versions!
	protected NBTTagCompound cleanup(NBTTagCompound oldNbttagcompound) {
		NBTTagCompound nbttagcompound = createNew();
		byte ver = oldNbttagcompound.getByte("ver");
		if (ver != 0) return oldNbttagcompound;
		else {
			nbttagcompound.setByte("ver", ver);
		}
		if (nbttagcompound.getBoolean("dest")) {
			nbttagcompound.setBoolean("dest", true);
			nbttagcompound.setDouble("destX", oldNbttagcompound.getDouble("destX"));
			nbttagcompound.setDouble("destY", oldNbttagcompound.getDouble("destY"));
			nbttagcompound.setDouble("destZ", oldNbttagcompound.getDouble("destZ"));
			nbttagcompound.setFloat("destRotYaw", oldNbttagcompound.getFloat("destRotYaw"));
			nbttagcompound.setFloat("destRotPitch", oldNbttagcompound.getFloat("destRotPitch"));
			nbttagcompound.setInteger("destDim", oldNbttagcompound.getInteger("destDim"));
			nbttagcompound.setInteger("nbPages", oldNbttagcompound.getInteger("nbPages"));
			nbttagcompound.setInteger("maxPages", oldNbttagcompound.getInteger("maxPages"));
			nbttagcompound.setBoolean("unstable", oldNbttagcompound.getBoolean("unstable"));
			int color = oldNbttagcompound.getInteger("color");
			if (color != 0) {
				nbttagcompound.setInteger("color", color);
			}
			boolean stayOpen = oldNbttagcompound.getBoolean("stayOpen");
			if (stayOpen) {
				nbttagcompound.setBoolean("stayOpen", true);
			}
			String name = oldNbttagcompound.getString("name");
			if (name.length() > 0) {
				nbttagcompound.setString("name", name);
			}
			String coverName = oldNbttagcompound.getString("coverName");
			if (coverName.length() > 0) {
				nbttagcompound.setString("coverName", coverName);
			}
			byte imgVer = oldNbttagcompound.getByte("imgVer");
			byte[] imageDatas;
			switch (imgVer) {
				case 1:
					imageDatas = oldNbttagcompound.getByteArray("img");
					if (imageDatas != null && imageDatas.length > 0) {
						nbttagcompound.setByte("imgVer", imgVer);
						nbttagcompound.setByteArray("img", imageDatas);
					}
					break;
				case 2:
					imageDatas = oldNbttagcompound.getByteArray("img");
					if (imageDatas != null && imageDatas.length > 0) {
						nbttagcompound.setByte("imgVer", imgVer);
						nbttagcompound.setByteArray("img", imageDatas);
					}
					break;
			}
		}
		return nbttagcompound;
	}
	
	public NBTTagCompound checkAndUpdateOldFormat(NBTTagCompound nbttagcompound) {
		if (nbttagcompound == null) return null;
		
		boolean updated = false;
		
		double destY = nbttagcompound.getDouble("destY");
		if (destY - (int)destY == 0.62f) {
			System.out.println("Removing yOffset from destY");
			nbttagcompound.setDouble("destY", destY - 1.62f);
		}
		else if (destY - (int)destY == 0f) {
			// System.out.println("Adding yOffset to destY");
			// nbttagcompound.setDouble("destY", destY + 1.62f);
		}
		
		int maxPages = getMaxPages(nbttagcompound);
		if (maxPages > 0 && maxPages == getNbPages(nbttagcompound)) {
			int randId = nbttagcompound.getInteger("randId");
			if (randId >= -1 && randId <= 255) {
				randId = createNewRandomId();
				nbttagcompound.setInteger("randId", randId);
				// System.out.println("New randId (from checkUpdate): " + randId);
			}
		}
		
		if (maxPages > 0 && nbttagcompound.getInteger("randId") == 0) {
			mod_MLB.mc.ingameGUI.addChatMessage("A linking book named \"" + getName(nbttagcompound) + "\" needs all its pages returned to be updated to the new pages system.");
		}
		
		return updated ? cleanup(nbttagcompound) : nbttagcompound;
	}
	
	public boolean write(NBTTagCompound nbttagcompound, EntityPlayer entityplayer, int nbPages, boolean unstable) {
		if (nbttagcompound.getBoolean("dest")) return false;
		nbttagcompound.setBoolean("dest", true);
		nbttagcompound.setDouble("destX", entityplayer.posX);
		nbttagcompound.setDouble("destY", entityplayer.posY - entityplayer.yOffset);
		nbttagcompound.setDouble("destZ", entityplayer.posZ);
		nbttagcompound.setFloat("destRotYaw", entityplayer.rotationYaw);
		nbttagcompound.setFloat("destRotPitch", entityplayer.rotationPitch);
		nbttagcompound.setInteger("destDim", entityplayer.dimension);
		nbttagcompound.setBoolean("unstable", unstable);
		nbttagcompound.setInteger("nbPages", nbPages);
		nbttagcompound.setInteger("maxPages", nbPages);
		if (getMaxPages(nbttagcompound) > 0) {
			int randId = createNewRandomId();
			nbttagcompound.setInteger("randId", randId);
			System.out.println("New randId (from write): " + randId);
		}
		return true;
	}
	
	public String getName(NBTTagCompound nbttagcompound) {
		if (nbttagcompound == null) return "";
		else return nbttagcompound.getString("name");
	}
	
	public boolean setName(NBTTagCompound nbttagcompound, String name) {
		int missingPages = nbttagcompound.getInteger("maxPages") - nbttagcompound.getInteger("nbPages");
		if (missingPages == 0) {
			nbttagcompound.setString("name", name);
			return true;
		}
		else return false;
	}
	
	public int getRandomId(NBTTagCompound nbttagcompound) {
		if (nbttagcompound == null) return 0;
		else return nbttagcompound.getInteger("randId");
	}
	
	public int getNbPages(NBTTagCompound nbttagcompound) {
		if (nbttagcompound == null) return 0;
		else return nbttagcompound.getInteger("nbPages");
	}
	
	public int getMaxPages(NBTTagCompound nbttagcompound) {
		if (nbttagcompound == null) return 0;
		else return nbttagcompound.getInteger("maxPages");
	}
	
	public int addPages(NBTTagCompound nbttagcompound, ItemStack itemstack) {
		return addPages(nbttagcompound, itemstack, itemstack.stackSize);
	}
	
	public int addPages(NBTTagCompound nbttagcompound, ItemStack itemstack, int max) {
		if (max <= 0) return 0;
		else if (nbttagcompound == null || itemstack == null) return 0;
		else if (itemstack.getItemDamage() != getColorCode(nbttagcompound)) return 0;
		else {
			int randId = getRandomId(nbttagcompound);
			if (randId != -1) { // randId of -1 can be added to any book (of the same color)
				if (randId != mod_MLB.itemPage.getLinkingBookRandomId(itemstack)) return 0;
				if (randId != 0 && mod_MLB.itemPage.getLinkingBookName(itemstack) != getName(nbttagcompound)) return 0;
			}
		}
		
		if (max > itemstack.stackSize) {
			max = itemstack.stackSize;
		}
		int nbPages = nbttagcompound.getInteger("nbPages");
		int missingPages = nbttagcompound.getInteger("maxPages") - nbPages;
		int added = max;
		if (added > missingPages) {
			added = missingPages;
		}
		if (added == 0) return 0;
		nbttagcompound.setInteger("nbPages", nbPages + added);
		
		itemstack.stackSize -= added;
		
		if (getMaxPages(nbttagcompound) > 0 && added == missingPages) {
			int randId = nbttagcompound.getInteger("randId");
			if (randId >= -1 && randId <= 255) {
				randId = createNewRandomId();
				nbttagcompound.setInteger("randId", randId);
				// System.out.println("New randId (from addPages): " + randId);
			}
		}
		
		return added;
	}
	
	public int addPages(NBTTagCompound nbttagcompound, int nb) {
		int nbPages = nbttagcompound.getInteger("nbPages");
		int missingPages = nbttagcompound.getInteger("maxPages") - nbPages;
		if (nb > missingPages) {
			nb = missingPages;
		}
		nbttagcompound.setInteger("nbPages", nbPages + nb);
		return nb;
	}
	
	public ItemStack removePages(NBTTagCompound nbttagcompound) {
		return removePages(nbttagcompound, mod_MLB.itemPage.getItemStackLimit());
	}
	
	public ItemStack removePages(NBTTagCompound nbttagcompound, int max) {
		if (max < 0 || nbttagcompound == null) return null;
		
		if (max > mod_MLB.itemPage.getItemStackLimit()) {
			max = mod_MLB.itemPage.getItemStackLimit();
		}
		int nbPages = nbttagcompound.getInteger("nbPages");
		int removed = max;
		if (removed > nbPages) {
			removed = nbPages;
		}
		if (removed == 0) return null;
		nbttagcompound.setInteger("nbPages", nbPages - removed);
		
		ItemStack itemstack = new ItemStack(mod_MLB.itemPage, removed, getColorCode(nbttagcompound));
		NBTTagCompound nbttagcompound_page = new NBTTagCompound();
		nbttagcompound_page.setString("name", getName(nbttagcompound));
		nbttagcompound_page.setInteger("randId", getRandomId(nbttagcompound));
		itemstack.setTagCompound(nbttagcompound_page);
		
		return itemstack;
	}
	
	public int getColorCode(NBTTagCompound nbttagcompound) {
		return nbttagcompound.getInteger("color");
	}
	
	public void setPagesColorCodeFromDye(NBTTagCompound nbttagcompound, int dyeColor) {
		int color = BlockCloth.getBlockFromDye(dyeColor);
		nbttagcompound.setInteger("color", color);
	}
	
	public boolean isUnstable(NBTTagCompound nbttagcompound) {
		return nbttagcompound.getBoolean("unstable");
	}
	
	public boolean getStayOpen(NBTTagCompound nbttagcompound) {
		return nbttagcompound.getBoolean("stayOpen");
	}
	
	public void setStayOpen(NBTTagCompound nbttagcompound, boolean stayOpen) {
		nbttagcompound.setBoolean("stayOpen", stayOpen);
	}
	
	public String getCoverName(NBTTagCompound nbttagcompound) {
		if (nbttagcompound == null) return "";
		else return nbttagcompound.getString("coverName");
	}
	
	public String setCoverName(NBTTagCompound nbttagcompound, String coverName) {
		coverName = Mod_MystLinkingBook.coverNameFilterPattern.matcher(coverName).replaceAll("");
		nbttagcompound.setString("coverName", coverName);
		return coverName;
	}
	
	public BufferedImage getLinkingPanelImage(NBTTagCompound nbttagcompound) {
		byte imgVer = nbttagcompound.getByte("imgVer");
		switch (imgVer) {
			case 1:
				return mod_MLB.itm.getImageFromRawBytes(nbttagcompound.getByteArray("img"), 80, 60);
			case 2:
				return mod_MLB.itm.getImageFromPNGBytes(nbttagcompound.getByteArray("img"));
			default:
				return null;
		}
	}
	
	public void setLinkingPanelImage(NBTTagCompound nbttagcompound, BufferedImage image) {
		byte imgVer = 2;
		nbttagcompound.setByte("imgVer", imgVer);
		switch (imgVer) {
			case 1:
				nbttagcompound.setByteArray("img", mod_MLB.itm.getRawBytesFromImage(image));
				break;
			case 2:
				nbttagcompound.setByteArray("img", mod_MLB.itm.getPNGBytesFromImage(image));
				break;
		}
	}
	
	public boolean doLinkToDifferentAge(NBTTagCompound nbttagcompound, int bookX, int bookY, int bookZ, int bookDim) {
		if (!nbttagcompound.getBoolean("dest")) return false;
		int destX = (int)nbttagcompound.getDouble("destX");
		int destY = (int)nbttagcompound.getDouble("destY");
		int destZ = (int)nbttagcompound.getDouble("destZ");
		int destDim = nbttagcompound.getInteger("destDim");
		return agesManager.linksToDifferentAge(bookX, bookY, bookZ, bookDim, destX, destY, destZ, destDim);
	}
	
	public boolean doLinkChangesDimension(NBTTagCompound nbttagcompound, EntityPlayer entityplayer) {
		return nbttagcompound.getBoolean("dest") && nbttagcompound.getInteger("destDim") != entityplayer.dimension;
	}
	
	public void prepareLinking(NBTTagCompound nbttagcompound, EntityPlayer entityplayer) {
		if (mod_MLB.settings.noDestinationPreloading) return;
		if (!nbttagcompound.getBoolean("dest")) return;
		int destX = (int)nbttagcompound.getDouble("destX");
		int destY = (int)nbttagcompound.getDouble("destY");
		int destZ = (int)nbttagcompound.getDouble("destZ");
		int destDim = nbttagcompound.getInteger("destDim");
		worldPreloader.preloadDestination(entityplayer.worldObj, destX, destY, destZ, destDim);
	}
	
	public boolean link(NBTTagCompound nbttagcompound, Entity entity) {
		if (nbttagcompound.getBoolean("dest")) {
			entity.setVelocity(0, 0, 0);
			entity.fallDistance = 0f;
			
			double destX = nbttagcompound.getDouble("destX");
			double destY = nbttagcompound.getDouble("destY");
			double destZ = nbttagcompound.getDouble("destZ");
			float destRotYaw = nbttagcompound.getFloat("destRotYaw");
			float destRotPitch = nbttagcompound.getFloat("destRotPitch");
			int curDim = entity.worldObj.worldProvider.worldType;
			int destDim = nbttagcompound.getInteger("destDim");
			String bookName = getName(nbttagcompound);
			
			if (destDim == curDim) {
				teleport(destX, destY, destZ, destRotYaw, destRotPitch, bookName, entity);
				return true;
			}
			else {
				teleport(destX, destY, destZ, destRotYaw, destRotPitch, destDim, bookName, entity);
				return true;
			}
		}
		return false;
	}
	
	public void teleport(double destX, double destY, double destZ, float destRotYaw, float destRotPitch, String bookName, Entity entity) {
		Chunk chunk = entity.worldObj.getChunkFromBlockCoords((int)destX, (int)destZ); // Load or generate the needed chunk.
		entity.setLocationAndAngles(destX, destY, destZ, destRotYaw, destRotPitch);
		entity.worldObj.updateEntityWithOptionalForce(entity, true);
		chunk.isModified = true; // Because it is not set in Chunk.addEntity()
	}
	
	public void teleport(double destX, double destY, double destZ, float destRotYaw, float destRotPitch, int destDim, String bookName, Entity entity) {
		World curWorld = entity.worldObj;
		
		World destWorld = lastUsedWorld;
		if (destWorld == null || destWorld.worldProvider.worldType != destDim) {
			destWorld = worldPreloader.getWorld();
		}
		if (destWorld != null && destWorld.worldProvider.worldType != destDim) {
			destWorld = null;
		}
		boolean preloaded = destWorld != null;
		if (destWorld == null) {
			destWorld = new World(curWorld, WorldProvider.getProviderForDimension(destDim));
		}
		Chunk chunk = destWorld.getChunkFromBlockCoords((int)destX, (int)destZ); // Load or generate the needed chunk.
		
		if (entity instanceof EntityPlayer) {
			EntityPlayer entityplayer = (EntityPlayer)entity;
			
			// This is a workaround for a weird bug I called the "speed bug".
			// I don't know why, but sometimes after teleporting quickly multiple times to the same dimension, the player is in this list.
			// Maybe a chunkloader cache bug ?
			if (destWorld.loadedEntityList.contains(entityplayer)) {
				destWorld.unloadEntities(Collections.singletonList(entity));
				destWorld.updateEntityList();
				
				// newWorld.loadedEntityList.remove(entityplayer);
				// This should be called, but it is a protected method:
				// newWorld.releaseEntitySkin(entityplayer);
			}
			
			// Inspired by Minecraft.usePortal(int i):
			int curDim = entityplayer.dimension;
			curWorld.setEntityDead(entityplayer);
			entityplayer.isDead = false;
			entityplayer.dimension = destDim;
			
			entityplayer.setLocationAndAngles(destX, destY, destZ, destRotYaw, destRotPitch);
			// Is the following not useful ?
			destWorld.updateEntityWithOptionalForce(entityplayer, true);
			
			LoadingScreenRenderer loadingScreen = null;
			if (preloaded && !mod_MLB.settings.showLoadingScreens) {
				loadingScreen = mod_MLB.mc.loadingScreen;
				mod_MLB.mc.loadingScreen = null;
			}
			mod_MLB.mc.changeWorld(destWorld, "Linking to " + bookName, entityplayer);
			if (loadingScreen != null) {
				mod_MLB.mc.loadingScreen = loadingScreen;
			}
			
			entityplayer.worldObj = destWorld;
			System.out.println("Teleported to " + destWorld.worldProvider.worldType);
			entityplayer.setLocationAndAngles(destX, destY, destZ, destRotYaw, destRotPitch);
			destWorld.updateEntityWithOptionalForce(entityplayer, true);
			
			lastUsedWorld = curWorld;
			
			while (Keyboard.next()) {
				// KeyBinding.setKeyBindState(Keyboard.getEventKey(), Keyboard.getEventKeyState());
			}
			while (Mouse.next()) {
			}
		}
		else {
			curWorld.unloadEntities(Collections.singletonList(entity));
			curWorld.updateEntityList();
			
			entity.setLocationAndAngles(destX, destY, destZ, destRotYaw, destRotPitch);
			destWorld.spawnEntityInWorld(entity);
			entity.setWorld(destWorld);
			destWorld.updateEntityWithOptionalForce(entity, true);
			chunk.isModified = true; // Because it is not set in Chunk.addEntity()
			
			while (!destWorld.quickSaveWorld(-1)) { // Until all chunks have been saved
			}
		}
	}
	
	public void startNewWorld() {
		worldPreloader.getWorld();
		lastUsedWorld = null;
		worldPreloader.preloadThread = null;
	}
	
	class WorldPreloader {
		
		public Minecraft mc;
		
		public PreloadThread preloadThread = null;
		
		protected ScheduledActionRef preloadActionRef = mod_MLB.scheduledActionsManager.getNewReadyScheduledActionRef(new IScheduledAction() {
			@Override
			public boolean execute(int nbTicks, float partialTick) {
				try {
					return preloadThread != null && preloadThread.continueExecDuring(5); // 5ms every tick is 5ms every 50 ms is ~10% of the time
				}
				catch (ExecutionException e) {
					throw new RuntimeException(e);
				}
			}
		});
		
		public class PreloadThread extends SynchronousThread {
			public World curWorld;
			public int destX;
			public int destY;
			public int destZ;
			public int destDim;
			public World destWorld;
			
			public PreloadThread(World curWorld, double destX, double destY, double destZ, int destDim, World destWorld) {
				super("PreloadThread DIM" + destDim);
				this.curWorld = curWorld;
				this.destX = (int)Math.floor(destX);
				this.destY = (int)Math.floor(destY);
				this.destZ = (int)Math.floor(destZ);
				this.destDim = destDim;
				this.destWorld = destWorld;
			}
			
			@Override
			// Insppired by Minecraft.preloadWorld():
			public void syncRun() throws AbortedException {
				int c = '\200';
				
				if (destWorld == null) {
					checkContinue();
					destWorld = new World(curWorld, WorldProvider.getProviderForDimension(destDim));
				}
				
				if (destWorld != curWorld) {
					checkContinue();
					IChunkProvider ichunkprovider = destWorld.getChunkProvider();
					if (ichunkprovider instanceof ChunkProviderLoadOrGenerate) {
						ChunkProviderLoadOrGenerate chunkproviderloadorgenerate = (ChunkProviderLoadOrGenerate)ichunkprovider;
						chunkproviderloadorgenerate.setCurrentChunkOver(destX >> 4, destZ >> 4);
					}
				}
				for (int k = -c; k <= c; k += 16) {
					checkContinue(true);
					for (int l = -c; l <= c; l += 16) {
						destWorld.getBlockId(destX + k, 64, destZ + l);
						while (destWorld.updatingLighting()) {
						}
					}
				}
				
				checkContinue();
				// mc.statFileWriter.func_27175_b();
				mc.statFileWriter.syncStats();
				
				do {
					checkContinue();
				} while (!curWorld.quickSaveWorld(-1)); // Until all chunks have been saved
				
				checkContinue();
				mc.getSaveLoader().flushCache();
			}
		};
		
		public WorldPreloader(Minecraft mc) {
			this.mc = mc;
		}
		
		public void preloadDestination(World curWorld, double destX, double destY, double destZ, int destDim) {
			World destWorld = curWorld != null && curWorld.worldProvider.worldType == destDim ? curWorld : null;
			if (preloadThread != null) {
				preloadThread.abort();
				try {
					preloadThread.finishExec();
				}
				catch (ExecutionException e) {
					throw new RuntimeException(e);
				}
				if (destWorld == null && lastUsedWorld != null && lastUsedWorld.worldProvider.worldType == destDim) {
					destWorld = lastUsedWorld;
				}
				if (destWorld == null && preloadThread.destDim == destDim) {
					destWorld = getWorld();
				}
			}
			preloadThread = new PreloadThread(curWorld, destX, destY, destZ, destDim, destWorld);
			preloadThread.start();
			preloadActionRef.reschedule(0);
		}
		
		public World getWorld() {
			if (preloadThread != null) {
				preloadThread.abort();
				try {
					preloadThread.finishExec();
				}
				catch (ExecutionException e) {
					throw new RuntimeException(e);
				}
				preloadActionRef.unschedule();
				return preloadThread.destWorld;
			}
			else return null;
		}
	}
}
