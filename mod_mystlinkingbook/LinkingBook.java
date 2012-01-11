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
	
	public boolean isDestinationSet(NBTTagCompound nbttagcompound) {
		return nbttagcompound.getBoolean("dest");
	}
	
	public boolean setDestination(NBTTagCompound nbttagcompound, EntityPlayer entityplayer) {
		if (nbttagcompound.getBoolean("dest")) return false;
		nbttagcompound.setBoolean("dest", true);
		nbttagcompound.setDouble("destX", entityplayer.posX);
		nbttagcompound.setDouble("destY", entityplayer.posY);
		nbttagcompound.setDouble("destZ", entityplayer.posZ);
		nbttagcompound.setFloat("destRotYaw", entityplayer.rotationYaw);
		nbttagcompound.setFloat("destRotPitch", entityplayer.rotationPitch);
		nbttagcompound.setInteger("destDim", entityplayer.dimension);
		return true;
	}
	
	public boolean teleportToDestination(NBTTagCompound nbttagcompound, EntityPlayer entityplayer) {
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
	
	public String getName(NBTTagCompound nbttagcompound) {
		if (nbttagcompound == null) return "";
		else {
			String name = nbttagcompound.getString("name");
			return name.isEmpty() ? "Unwritten" : name;
		}
	}
	
	public void setName(NBTTagCompound nbttagcompound, String name) {
		nbttagcompound.setString("name", name);
	}
}
