package net.minecraft.src.mystlinkingbook;

import net.minecraft.src.Entity;
import net.minecraft.src.mystlinkingbook.ScheduledActionsManager.IScheduledAction;
import net.minecraft.src.mystlinkingbook.ScheduledActionsManager.ScheduledActionRef;

/**
 * 
 * @author ziliss
 * @since 0.7b
 */
public class PositionKeeper {
	
	/**
	 * Reference to the mod instance.
	 */
	public Mod_MystLinkingBook mod_MLB;
	
	public Entity entity;
	public boolean noclip;
	boolean prevNoclip;
	double x;
	double y;
	double z;
	float rotYaw;
	float rotPitch;
	
	ScheduledActionRef actionRef;
	
	public PositionKeeper(Entity entity, Mod_MystLinkingBook mod_MLB) {
		this(entity, false, mod_MLB);
	}
	
	public PositionKeeper(Entity entity, boolean noclip, Mod_MystLinkingBook mod_MLB) {
		this.mod_MLB = mod_MLB;
		this.entity = entity;
		this.noclip = noclip;
		x = entity.posX;
		y = entity.posY - entity.yOffset;
		z = entity.posZ;
		rotYaw = entity.rotationYaw;
		rotPitch = entity.rotationPitch;
		
		actionRef = mod_MLB.scheduledActionsManager.getNewReadyScheduledActionRef(new IScheduledAction() {
			@Override
			public boolean execute(int nbTicksElapsed) {
				keepPosition();
				return true;
			}
		});
	}
	
	public void start() {
		if (noclip && !actionRef.isScheduled()) {
			prevNoclip = entity.noClip;
			entity.noClip = true;
		}
		keepPosition();
		actionRef.reschedule(0);
	}
	
	public void stop() {
		if (noclip && actionRef.isScheduled()) {
			entity.noClip = prevNoclip;
		}
		actionRef.unschedule();
	}
	
	public void keepPosition() {
		entity.setVelocity(0, 0, 0);
		entity.setLocationAndAngles(x, y, z, rotYaw, rotPitch);
		entity.fallDistance = 0f;
	}
}