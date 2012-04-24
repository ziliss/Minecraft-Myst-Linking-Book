package net.minecraft.src.mystlinkingbook;

import net.minecraft.src.Entity;
import net.minecraft.src.World;
import net.minecraft.src.mystlinkingbook.ScheduledActionsManager.ScheduledAction;
import net.minecraft.src.mystlinkingbook.ScheduledActionsManager.ScheduledActionRef;

/**
 * Keeps a reference to an entity that is being linked, and link it.<br>
 * The entity cannot move for {@code maxTicksBeforeLinking} ticks.
 * 
 * @author ziliss
 * @since 0.7b
 */
public class LinkingEntity {
	/**
	 * Reference to the mod instance.
	 */
	public Mod_MystLinkingBook mod_MLB;
	
	/** The entity to be linked. */
	public Entity entity;
	public World origEntityWorld;
	
	/** The linking book linking the entity. */
	public LinkingBook linkingBook;
	
	public int ticksBeforeLinking = 0;
	public int maxTicksBeforeLinking = 2 * 20;
	protected ScheduledActionRef linkingActionRef;
	public PositionKeeper positionKeeper;
	
	public LinkingEntity(Entity entity, LinkingBook linkingBook, Mod_MystLinkingBook mod_MLB) {
		this.entity = entity;
		this.mod_MLB = mod_MLB;
		this.linkingBook = linkingBook;
		
		origEntityWorld = entity.worldObj;
		
		linkingActionRef = mod_MLB.scheduledActionsManager.getNewReadyScheduledActionRef(new ScheduledAction() {
			@Override
			public void executeOnce() {
				link();
			}
		});
		linkingActionRef.reschedule(maxTicksBeforeLinking);
		
		positionKeeper = new PositionKeeper(entity, true, mod_MLB);
		positionKeeper.start();
	}
	
	protected void link() {
		positionKeeper.stop();
		if (entity.worldObj == origEntityWorld) { // Checks that the entity has not changed world meanwhile.
			linkingBook.link(entity);
		}
	}
}
