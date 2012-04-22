package net.minecraft.src.mystlinkingbook;

import net.minecraft.src.GuiButton;

/**
 * 
 * @author ziliss
 * @since 0.7b
 */
public class GuiButtonStates extends GuiButton {
	
	public String label;
	public String[] states;
	public int state;
	
	public GuiButtonStates(int par1, int par2, int par3, String label, String[] states) {
		this(par1, par2, par3, 200, 20, label, states);
	}
	
	public GuiButtonStates(int par1, int par2, int par3, int par4, int par5, String label, String[] states) {
		super(par1, par2, par3, par4, par5, label);
		
		if (label == null) {
			label = "";
		}
		else if (label.length() > 0) {
			label += ": ";
		}
		
		this.label = label;
		this.states = states;
		
		setState(0);
		updateState();
	}
	
	public int getState() {
		return state;
	}
	
	public boolean getBooleanState() {
		return state != 0;
	}
	
	public int setState(int i) {
		state = i;
		displayString = label + states[i];
		return state;
	}
	
	public boolean setBooleanState(boolean b) {
		return setState(b ? 1 : 0) != 0;
	}
	
	public int setNextState() {
		return setState((state + 1) % states.length);
	}
	
	public boolean setNextBooleanState() {
		return setBooleanState(!(state != 0));
	}
	
	public void updateState() {
	}
}
