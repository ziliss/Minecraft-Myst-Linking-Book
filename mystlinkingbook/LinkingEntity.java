package net.minecraft.src.mystlinkingbook;

import net.minecraft.src.Entity;
import net.minecraft.src.mystlinkingbook.ScheduledActionsManager.ScheduledAction;
import net.minecraft.src.mystlinkingbook.ScheduledActionsManager.ScheduledActionRef;

/**
 * 
 * @author ziliss
 * @since 0.7b
 */
public class LinkingEntity {
	/**
	 * Reference to the mod instance.
	 */
	public Mod_MystLinkingBook mod_MLB;
	
	/**
	 * The player opening the GUI.
	 */
	public Entity entity;
	
	/**
	 * The {@code TileEntity} of the linking book {@code Block}.
	 */
	public TileEntityLinkingBook tileEntityLinkingBook;
	
	public int ticksBeforeLinking = 0;
	public int maxTicksBeforeLinking = 2 * 20;
	ScheduledActionRef linkingActionRef;
	public PositionKeeper positionKeeper;
	
	public LinkingEntity(Entity entity, TileEntityLinkingBook tileEntityLinkingBook, Mod_MystLinkingBook mod_MLB) {
		this.entity = entity;
		this.mod_MLB = mod_MLB;
		this.tileEntityLinkingBook = tileEntityLinkingBook;
		
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
	
	void link() {
		positionKeeper.stop();
		if (entity.worldObj == mod_MLB.mc.theWorld) { // TODO: is it a good test ? Is it enough ?
			mod_MLB.linkingBook.link(tileEntityLinkingBook.nbttagcompound_linkingBook, entity);
		}
	}
}
