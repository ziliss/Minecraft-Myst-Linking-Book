package net.minecraft.src;

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
	
	public LinkingBook() {
	}
	
	public boolean isWritten(NBTTagCompound nbttagcompound) {
		return nbttagcompound.getBoolean("dest");
	}
	
	public boolean write(NBTTagCompound nbttagcompound, EntityPlayer entityplayer, int nbPages, boolean unstable) {
		if (nbttagcompound.getBoolean("dest")) return false;
		nbttagcompound.setBoolean("dest", true);
		nbttagcompound.setDouble("destX", entityplayer.posX);
		nbttagcompound.setDouble("destY", entityplayer.posY);
		nbttagcompound.setDouble("destZ", entityplayer.posZ);
		nbttagcompound.setFloat("destRotYaw", entityplayer.rotationYaw);
		nbttagcompound.setFloat("destRotPitch", entityplayer.rotationPitch);
		nbttagcompound.setInteger("destDim", entityplayer.dimension);
		nbttagcompound.setInteger("nbPages", nbPages);
		nbttagcompound.setInteger("maxPages", nbPages);
		nbttagcompound.setBoolean("unstable", unstable);
		return true;
	}
	
	public String getName(NBTTagCompound nbttagcompound) {
		if (nbttagcompound == null) return "";
		else return nbttagcompound.getString("name");
	}
	
	public void setName(NBTTagCompound nbttagcompound, String name) {
		nbttagcompound.setString("name", name);
	}
	
	public int getNbPages(NBTTagCompound nbttagcompound) {
		if (nbttagcompound == null) return 0;
		else return nbttagcompound.getInteger("nbPages");
	}
	
	public int getMaxPages(NBTTagCompound nbttagcompound) {
		if (nbttagcompound == null) return 0;
		else return nbttagcompound.getInteger("maxPages");
	}
	
	public int addPages(NBTTagCompound nbttagcompound, int nb) {
		if (nb <= 0) return 0;
		else if (nbttagcompound == null) return nb;
		else {
			int nbPages = nbttagcompound.getInteger("nbPages");
			int available = nbttagcompound.getInteger("maxPages") - nbPages;
			int added = nb;
			if (added > available) {
				added = available;
			}
			nbttagcompound.setInteger("nbPages", nbPages + added);
			return nb - added;
		}
	}
	
	public int removePages(NBTTagCompound nbttagcompound, int nb) {
		if (nb <= 0 || nbttagcompound == null) return 0;
		else {
			int available = nbttagcompound.getInteger("nbPages");
			int removed = nb;
			if (removed > available) {
				removed = available;
			}
			nbttagcompound.setInteger("nbPages", available - removed);
			return removed;
		}
	}
	
	public boolean link(NBTTagCompound nbttagcompound, EntityPlayer entityplayer) {
		if (nbttagcompound.getBoolean("dest")) {
			double destX = nbttagcompound.getDouble("destX");
			double destY = nbttagcompound.getDouble("destY") - entityplayer.yOffset; // yOffset: prevent the tiny jump when teleporting
			double destZ = nbttagcompound.getDouble("destZ");
			float destRotYaw = nbttagcompound.getFloat("destRotYaw");
			float destRotPitch = nbttagcompound.getFloat("destRotPitch");
			int destDim = nbttagcompound.getInteger("destDim");
			if (destDim == entityplayer.dimension) {
				entityplayer.setLocationAndAngles(destX, destY, destZ, destRotYaw, destRotPitch);
				return true;
			}
		}
		return false;
	}
	
	public boolean isUnstable(NBTTagCompound nbttagcompound) {
		return nbttagcompound.getBoolean("unstable");
	}
}
