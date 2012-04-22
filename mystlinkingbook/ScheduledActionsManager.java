package net.minecraft.src.mystlinkingbook;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

import net.minecraft.client.Minecraft;
import net.minecraft.src.ModLoader;

/**
 * 
 * @author ziliss
 * @since 0.7b
 */
public class ScheduledActionsManager {
	
	/**
	 * Reference to the mod instance.
	 */
	public Mod_MystLinkingBook mod_MLB;
	
	public int time = -1;
	public float partialTick;
	public boolean isTick;
	public boolean onClockOnlyTick = true;
	public long lastClock = -1;
	
	public final TreeMap<Integer, ArrayList<ScheduledActionRef>> scheduledTickActionsList = new TreeMap<Integer, ArrayList<ScheduledActionRef>>();
	public final ArrayList<ScheduledActionRef> currentTickActionsList = new ArrayList<ScheduledActionRef>();
	public final ArrayList<ScheduledActionRef> currentFrameActionsList = new ArrayList<ScheduledActionRef>();
	
	public final ArrayList<ArrayList<ScheduledActionRef>> unusedArrayLists = new ArrayList<ArrayList<ScheduledActionRef>>();
	
	public ArrayList<ScheduledActionRef> workingActionsList = null;
	
	public ScheduledActionsManager(Mod_MystLinkingBook mod_MLB) {
		this.mod_MLB = mod_MLB;
	}
	
	protected void checkHook() {
		if (onClockOnlyTick == currentFrameActionsList.isEmpty() && time >= 0) return;
		
		if (time < 0) {
			time = 0;
			lastClock = mod_MLB.mc.theWorld.getWorldTime();
		}
		onClockOnlyTick = currentFrameActionsList.isEmpty();
		ModLoader.setInGameHook(mod_MLB, true, onClockOnlyTick);
	}
	
	protected boolean checkContinue() {
		boolean cont = !scheduledTickActionsList.isEmpty() || !currentTickActionsList.isEmpty() || !currentFrameActionsList.isEmpty();
		if (!cont) {
			time = -1;
			lastClock = -1;
			onClockOnlyTick = true;
		}
		return cont;
	}
	
	public void unschedule(ScheduledActionRef actionRef) {
		if (actionRef.executionTime == -1) return;
		if (actionRef.executionTime > time || actionRef.executionTime == time && workingActionsList == null) {
			ArrayList<ScheduledActionRef> actionsGroup = scheduledTickActionsList.get(actionRef.executionTime);
			actionsGroup.remove(actionRef);
			if (actionsGroup.isEmpty()) {
				releaseEmptyArrayList(scheduledTickActionsList.remove(actionRef.executionTime));
			}
		}
		else if (actionRef.executionTime == time && workingActionsList != null) {
			if (!workingActionsList.remove(actionRef)) {
				if (actionRef.eachFrame()) {
					currentFrameActionsList.set(currentFrameActionsList.indexOf(actionRef), null);
				}
				else {
					currentTickActionsList.set(currentTickActionsList.indexOf(actionRef), null);
				}
			}
		}
		else {
			if (actionRef.eachFrame()) {
				currentFrameActionsList.set(currentFrameActionsList.indexOf(actionRef), null);
			}
			else {
				currentTickActionsList.set(currentTickActionsList.indexOf(actionRef), null);
			}
		}
		actionRef.executionTime = -1;
	}
	
	public void reschedule(int delay, ScheduledActionRef actionRef) {
		unschedule(actionRef);
		
		if (delay < 0) {
			delay = 0;
		}
		checkHook();
		actionRef.executionTime = time + delay;
		ArrayList<ScheduledActionRef> actionsGroup = scheduledTickActionsList.get(actionRef.executionTime);
		if (actionsGroup == null) {
			actionsGroup = getEmptyArrayList();
			scheduledTickActionsList.put(actionRef.executionTime, actionsGroup);
		}
		actionsGroup.add(actionRef);
	}
	
	public boolean OnTickInGame(float partialTick, Minecraft mc) {
		if (time == -1) return false;
		
		this.partialTick = partialTick;
		isTick = lastClock != mod_MLB.mc.theWorld.getWorldTime();
		lastClock = mod_MLB.mc.theWorld.getWorldTime();
		
		if (isTick) {
			Iterator<ScheduledActionRef> iter = currentTickActionsList.iterator();
			while (iter.hasNext()) {
				ScheduledActionRef actionRef = iter.next();
				if (actionRef == null) {
					iter.remove();
				}
				else if (actionRef.executionTime == -1) throw new RuntimeException("Should not happen: actionRef.executionTime == -1");
				else {
					actionRef.execute();
				}
			}
			workingActionsList = scheduledTickActionsList.get(time);
			if (workingActionsList != null) {
				ScheduledActionRef actionRef;
				while (workingActionsList.size() > 0) { // This allows for modifications of the list while we are working with it.
					actionRef = workingActionsList.remove(0);
					if (actionRef.eachFrame()) {
						currentFrameActionsList.add(actionRef);
					}
					else {
						currentTickActionsList.add(actionRef);
					}
					if (actionRef.executionTime == -1) throw new RuntimeException("Should not happen: actionRef.executionTime == -1");
					if (!actionRef.eachFrame()) {
						actionRef.execute();
					}
				}
				scheduledTickActionsList.remove(time);
				releaseEmptyArrayList(workingActionsList);
				workingActionsList = null;
			}
			if (!scheduledTickActionsList.headMap(time).isEmpty()) throw new RuntimeException("Should not have actions after time is passed!");
		}
		
		Iterator<ScheduledActionRef> frameIter = currentFrameActionsList.iterator();
		while (frameIter.hasNext()) {
			ScheduledActionRef actionRef = frameIter.next();
			if (actionRef == null) {
				frameIter.remove();
			}
			else if (actionRef.executionTime == -1) throw new RuntimeException("Should not happen: actionRef.executionTime == -1");
			else {
				actionRef.execute();
			}
		}
		
		if (isTick) {
			time++;
		}
		checkHook();
		return checkContinue();
	}
	
	protected ArrayList<ScheduledActionRef> getEmptyArrayList() {
		int size = unusedArrayLists.size();
		return size == 0 ? new ArrayList<ScheduledActionRef>() : unusedArrayLists.remove(size - 1);
	}
	
	protected void releaseEmptyArrayList(ArrayList<ScheduledActionRef> arrayList) {
		if (unusedArrayLists.size() < 100) {
			unusedArrayLists.add(arrayList);
		}
	}
	
	public ScheduledActionRef getNewReadyScheduledActionRef(IScheduledAction action) {
		return new ScheduledActionRef(action, -1);
	}
	
	public class ScheduledActionRef {
		
		protected IScheduledAction action;
		
		protected int executionTime;
		
		public ScheduledActionRef(IScheduledAction action, int executionTime) {
			this.action = action;
			this.executionTime = executionTime;
		}
		
		protected void execute() {
			if (eachFrame()) {
				if (!((ScheduledFrameAction)action).execute(time - executionTime, partialTick, isTick)) {
					unschedule();
				}
			}
			else {
				if (!action.execute(time - executionTime, partialTick)) {
					unschedule();
				}
			}
		}
		
		protected void execute(boolean isTick) {
			if (!((ScheduledFrameAction)action).execute(time - executionTime, partialTick, isTick)) {
				unschedule();
			}
		}
		
		public boolean isScheduled() {
			return executionTime != -1;
		}
		
		public int getTimeBeforeNextExecution() {
			if (isScheduled()) return Math.max(0, executionTime - time);
			return -1;
		}
		
		public void unschedule() {
			ScheduledActionsManager.this.unschedule(this);
		}
		
		public void reschedule(int delay) {
			ScheduledActionsManager.this.reschedule(delay, this);
		}
		
		public int getNbExecutions() {
			return time - executionTime;
		}
		
		public boolean eachFrame() {
			return action instanceof ScheduledFrameAction;
		}
	}
	
	public static abstract interface IScheduledAction {
		
		public abstract boolean execute(int nbTicks, float partialTick);
	}
	
	public static abstract class ScheduledFrameAction implements IScheduledAction {
		
		public boolean execute(int nbTicks, float partialTick, boolean isTick) {
			return execute(nbTicks, partialTick);
		}
		
		@Override
		public boolean execute(int nbTicks, float partialTick) {
			return false;
		}
	}
	
	public static abstract class ScheduledAction implements IScheduledAction {
		
		@Override
		public boolean execute(int nbTicks, float partialTick) {
			executeOnce();
			return false;
		}
		
		public abstract void executeOnce();
	}
}
