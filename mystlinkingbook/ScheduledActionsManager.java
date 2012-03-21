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
	
	public final TreeMap<Integer, ArrayList<ScheduledActionRef>> scheduledActionsList = new TreeMap<Integer, ArrayList<ScheduledActionRef>>();
	public final ArrayList<ScheduledActionRef> currentActionsList = new ArrayList<ScheduledActionRef>();
	
	public final ArrayList<ArrayList<ScheduledActionRef>> unusedArrayLists = new ArrayList<ArrayList<ScheduledActionRef>>();
	
	public ArrayList<ScheduledActionRef> workingActionsList = null;
	
	public ScheduledActionsManager(Mod_MystLinkingBook mod_MLB) {
		this.mod_MLB = mod_MLB;
	}
	
	void ensureStarted() {
		if (time >= 0) return;
		else {
			time = 0;
			ModLoader.setInGameHook(mod_MLB, true, false);
		}
	}
	
	boolean checkContinue() {
		boolean cont = !scheduledActionsList.isEmpty() || !currentActionsList.isEmpty();
		if (!cont) {
			time = -1;
		}
		return cont;
	}
	
	public void unschedule(ScheduledActionRef actionRef) {
		if (actionRef.executionTime == -1) return;
		if (actionRef.executionTime > time || actionRef.executionTime == time && workingActionsList == null) {
			ArrayList<ScheduledActionRef> actionsGroup = scheduledActionsList.get(actionRef.executionTime);
			actionsGroup.remove(actionRef);
			if (actionsGroup.isEmpty()) {
				releaseEmptyArrayList(scheduledActionsList.remove(actionRef.executionTime));
			}
		}
		else if (actionRef.executionTime == time && workingActionsList != null) {
			if (!workingActionsList.remove(actionRef)) {
				currentActionsList.set(currentActionsList.indexOf(actionRef), null);
			}
		}
		else {
			currentActionsList.set(currentActionsList.indexOf(actionRef), null);
		}
		actionRef.executionTime = -1;
	}
	
	public void reschedule(int delay, ScheduledActionRef actionRef) {
		unschedule(actionRef);
		
		if (delay < 0) {
			delay = 0;
		}
		ensureStarted();
		actionRef.executionTime = time + delay;
		ArrayList<ScheduledActionRef> actionsGroup = scheduledActionsList.get(actionRef.executionTime);
		if (actionsGroup == null) {
			actionsGroup = getEmptyArrayList();
			scheduledActionsList.put(actionRef.executionTime, actionsGroup);
		}
		actionsGroup.add(actionRef);
	}
	
	public boolean OnTickInGame(float tick, Minecraft mc) {
		if (time == -1) return false;
		
		Iterator<ScheduledActionRef> iter = currentActionsList.iterator();
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
		workingActionsList = scheduledActionsList.get(time);
		if (workingActionsList != null) {
			ScheduledActionRef actionRef;
			while (workingActionsList.size() > 0) { // This allows for modifications of the list while we are working with it.
				actionRef = workingActionsList.remove(0);
				currentActionsList.add(actionRef);
				if (actionRef.executionTime == -1) throw new RuntimeException("Should not happen: actionRef.executionTime == -1");
				actionRef.execute();
			}
			scheduledActionsList.remove(time);
			releaseEmptyArrayList(workingActionsList);
			workingActionsList = null;
		}
		if (!scheduledActionsList.headMap(time).isEmpty()) throw new RuntimeException("Should not have actions after time is passed!");
		time++;
		return checkContinue();
	}
	
	ArrayList<ScheduledActionRef> getEmptyArrayList() {
		int size = unusedArrayLists.size();
		return size == 0 ? new ArrayList<ScheduledActionRef>() : unusedArrayLists.remove(size - 1);
	}
	
	void releaseEmptyArrayList(ArrayList<ScheduledActionRef> arrayList) {
		if (unusedArrayLists.size() < 100) {
			unusedArrayLists.add(arrayList);
		}
	}
	
	public ScheduledActionRef getNewReadyScheduledActionRef(IScheduledAction action) {
		return new ScheduledActionRef(action, -1);
	}
	
	public class ScheduledActionRef {
		
		IScheduledAction action;
		
		int executionTime;
		
		public ScheduledActionRef(IScheduledAction action, int executionTime) {
			this.action = action;
			this.executionTime = executionTime;
		}
		
		void execute() {
			if (!action.execute(time - executionTime)) {
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
	}
	
	public static abstract interface IScheduledAction {
		
		public abstract boolean execute(int nbTicks);
	}
	
	public static abstract class ScheduledAction implements IScheduledAction {
		
		@Override
		public boolean execute(int nbTicks) {
			executeOnce();
			return false;
		}
		
		public abstract void executeOnce();
	}
}
