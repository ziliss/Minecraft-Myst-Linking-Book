package net.minecraft.src.mystlinkingbook;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.src.BlockCloth;
import net.minecraft.src.Chunk;
import net.minecraft.src.ChunkProviderLoadOrGenerate;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IChunkProvider;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;
import net.minecraft.src.WorldProvider;

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
public class LinkingBook {
	
	/**
	 * Reference to the mod instance.
	 */
	public Mod_MystLinkingBook mod_MLB;
	
	public Random rand = new Random();
	
	public LinkPreloader linkPreloader = new LinkPreloader(ModLoader.getMinecraftInstance());;
	
	public AgesManager agesManager;
	
	public LinkingBook(Settings settings, Mod_MystLinkingBook mod_MLB) {
		this.mod_MLB = mod_MLB;
		agesManager = new AgesManager(settings, mod_MLB.ressourcesManager.world);
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
	NBTTagCompound cleanup(NBTTagCompound oldNbttagcompound) {
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
		else if (itemstack.getItemDamage() != getPagesColor(nbttagcompound)) return 0;
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
		
		ItemStack itemstack = new ItemStack(mod_MLB.itemPage, removed, getPagesColor(nbttagcompound));
		NBTTagCompound nbttagcompound_page = new NBTTagCompound();
		nbttagcompound_page.setString("name", getName(nbttagcompound));
		nbttagcompound_page.setInteger("randId", getRandomId(nbttagcompound));
		itemstack.setTagCompound(nbttagcompound_page);
		
		return itemstack;
	}
	
	public int getPagesColor(NBTTagCompound nbttagcompound) {
		return nbttagcompound.getInteger("color");
	}
	
	public void setPagesColorFromDye(NBTTagCompound nbttagcompound, int dyeColor) {
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
	
	public boolean doLinkToDifferentAge(TileEntityLinkingBook tileEntityLinkingBook) {
		NBTTagCompound nbttagcompound = tileEntityLinkingBook.nbttagcompound_linkingBook;
		if (!nbttagcompound.getBoolean("dest")) return false;
		int destX = (int)nbttagcompound.getDouble("destX");
		int destY = (int)nbttagcompound.getDouble("destY");
		int destZ = (int)nbttagcompound.getDouble("destZ");
		int destDim = nbttagcompound.getInteger("destDim");
		int bookX = tileEntityLinkingBook.xCoord;
		int bookY = tileEntityLinkingBook.yCoord;
		int bookZ = tileEntityLinkingBook.zCoord;
		int bookDim = tileEntityLinkingBook.worldObj.worldProvider.worldType;
		return agesManager.linksToDifferentAge(bookX, bookY, bookZ, bookDim, destX, destY, destZ, destDim);
	}
	
	public boolean doLinkChangesDimension(NBTTagCompound nbttagcompound, EntityPlayer entityplayer) {
		return nbttagcompound.getBoolean("dest") && nbttagcompound.getInteger("destDim") != entityplayer.dimension;
	}
	
	public void prepareLinking(NBTTagCompound nbttagcompound, EntityPlayer entityplayer) {
		if ("".length() == 0) return;
		if (!nbttagcompound.getBoolean("dest")) return;
		int destX = (int)nbttagcompound.getDouble("destX");
		int destY = (int)nbttagcompound.getDouble("destY");
		int destZ = (int)nbttagcompound.getDouble("destZ");
		int destDim = nbttagcompound.getInteger("destDim");
		linkPreloader.preloadDestination(entityplayer.worldObj, destX, destY, destZ, destDim);
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
		World theWorld = entity.worldObj;
		
		World newWorld = linkPreloader.getWorld();
		if (newWorld != null && newWorld.worldProvider.worldType != destDim) {
			newWorld = null;
		}
		if (newWorld == null) {
			newWorld = new World(theWorld, WorldProvider.getProviderForDimension(destDim));
		}
		Chunk chunk = newWorld.getChunkFromBlockCoords((int)destX, (int)destZ); // Load or generate the needed chunk.
		
		if (entity instanceof EntityPlayer) {
			EntityPlayer entityplayer = (EntityPlayer)entity;
			
			// This is a workaround for a weird bug I called the "speed bug".
			// I don't know why, but sometimes after teleporting quickly multiple times to the same dimension, the player is in this list.
			// Maybe a chunkloader cache bug ?
			if (newWorld.loadedEntityList.contains(entityplayer)) {
				newWorld.unloadEntities(Collections.singletonList(entity));
				newWorld.updateEntityList();
				
				// newWorld.loadedEntityList.remove(entityplayer);
				// This should be called, but it is a protected method:
				// newWorld.releaseEntitySkin(entityplayer);
			}
			
			// Inspired by Minecraft.usePortal(int i):
			int curDim = entityplayer.dimension;
			theWorld.setEntityDead(entityplayer);
			entityplayer.isDead = false;
			entityplayer.dimension = destDim;
			
			entityplayer.setLocationAndAngles(destX, destY, destZ, destRotYaw, destRotPitch);
			// Is the following not useful ?
			newWorld.updateEntityWithOptionalForce(entityplayer, true);
			
			ModLoader.getMinecraftInstance().changeWorld(newWorld, "Linking to " + bookName, entityplayer);
			
			entityplayer.worldObj = newWorld;
			System.out.println("Teleported to " + newWorld.worldProvider.worldType);
			entityplayer.setLocationAndAngles(destX, destY, destZ, destRotYaw, destRotPitch);
			newWorld.updateEntityWithOptionalForce(entityplayer, true);
			
			while (Keyboard.next()) {
				// KeyBinding.setKeyBindState(Keyboard.getEventKey(), Keyboard.getEventKeyState());
			}
			while (Mouse.next()) {
			}
		}
		else {
			theWorld.unloadEntities(Collections.singletonList(entity));
			theWorld.updateEntityList();
			
			entity.setLocationAndAngles(destX, destY, destZ, destRotYaw, destRotPitch);
			newWorld.spawnEntityInWorld(entity);
			entity.setWorld(newWorld);
			newWorld.updateEntityWithOptionalForce(entity, true);
			chunk.isModified = true; // Because it is not set in Chunk.addEntity()
			
			newWorld.quickSaveWorld(0);
		}
	}
	
	static class LinkPreloader {
		
		public PreloadThread preloadThread = null;
		
		public Minecraft mc;
		
		public class PreloadThread extends Thread {
			public World curWorld;
			public int destX;
			public int destY;
			public int destZ;
			public int destDim;
			public World destWorld;
			
			public boolean aborted = false;
			
			public PreloadThread(World curWorld, double destX, double destY, double destZ, int destDim, World destWorld) {
				this.curWorld = curWorld;
				this.destX = (int)Math.floor(destX);
				this.destY = (int)Math.floor(destY);
				this.destZ = (int)Math.floor(destZ);
				this.destDim = destDim;
				this.destWorld = destWorld;
			}
			
			@Override
			// Insppired by Minecraft.preloadWorld():
			public void run() {
				int c = '\200';
				
				if (destWorld == null) {
					destWorld = new World(curWorld, WorldProvider.getProviderForDimension(destDim));
				}
				
				if (aborted) return;
				if (destWorld != curWorld) {
					IChunkProvider ichunkprovider = destWorld.getChunkProvider();
					if (ichunkprovider instanceof ChunkProviderLoadOrGenerate) {
						ChunkProviderLoadOrGenerate chunkproviderloadorgenerate = (ChunkProviderLoadOrGenerate)ichunkprovider;
						chunkproviderloadorgenerate.setCurrentChunkOver(destX >> 4, destZ >> 4);
					}
				}
				for (int k = -c; k <= c; k += 16) {
					if (aborted) return;
					for (int l = -c; l <= c; l += 16) {
						destWorld.getBlockId(destX + k, 64, destZ + l);
						while (destWorld.updatingLighting()) {
						}
					}
				}
				
				if (aborted) return;
				// mc.statFileWriter.func_27175_b();
				mc.statFileWriter.syncStats();
				
				if (aborted) return;
				curWorld.quickSaveWorld(0);
				
				if (aborted) return;
				mc.getSaveLoader().flushCache();
			}
			
			public void abort() {
				aborted = true;
			}
		};
		
		public LinkPreloader(Minecraft mc) {
			this.mc = mc;
		}
		
		public void preloadDestination(World curWorld, double destX, double destY, double destZ, int destDim) {
			World destWorld = curWorld != null && curWorld.worldProvider.worldType == destDim ? curWorld : null;
			if (preloadThread != null) {
				preloadThread.abort();
				if (destWorld == null && preloadThread.destDim == destDim) {
					destWorld = getWorld();
				}
			}
			preloadThread = new PreloadThread(curWorld, destX, destY, destZ, destDim, destWorld);
			preloadThread.setPriority(Thread.MIN_PRIORITY);
			preloadThread.run();
		}
		
		public World getWorld() {
			if (preloadThread != null) {
				preloadThread.abort();
				preloadThread.setPriority(Thread.NORM_PRIORITY + 1);
				try {
					preloadThread.join();
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
				return preloadThread.destWorld;
			}
			else return null;
		}
	}
}
