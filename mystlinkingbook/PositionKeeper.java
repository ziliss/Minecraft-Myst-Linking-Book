package net.minecraft.src.mystlinkingbook;

import net.minecraft.src.Entity;
import net.minecraft.src.mystlinkingbook.ScheduledActionsManager.ScheduledActionRef;
import net.minecraft.src.mystlinkingbook.ScheduledActionsManager.ScheduledFrameAction;

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
	protected boolean prevNoclip;
	protected double x;
	protected double y;
	protected double z;
	protected float rotYaw;
	protected float rotPitch;
	
	protected ScheduledActionRef actionRef;
	
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
		
		actionRef = mod_MLB.scheduledActionsManager.getNewReadyScheduledActionRef(new ScheduledFrameAction() {
			@Override
			public boolean execute(int nbTicksElapsed, float partialTick) {
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