package net.minecraft.src.mystlinkingbook;

import net.minecraft.client.Minecraft;
import net.minecraft.src.GuiButton;
import net.minecraft.src.Tessellator;

import org.lwjgl.opengl.GL11;

/**
 * 
 * @author ziliss
 * @since 0.8b
 */
public abstract class GuiSelect extends GuiButton {
	
	/** The width of the scrollbar area */
	public int scrollbarWidth = 5;
	
	/** Amount scrolled (0 = top, 1 = bottom) */
	protected float currentScroll = 0f;
	
	/** True if the scrollbar is being dragged */
	protected boolean isScrolling = false;
	
	/** True if the left mouse button was held down last time drawScreen was called. */
	protected boolean wasClicking = false;
	
	/** Height of 1 line */
	public int lineHeight;
	
	/** Number of lines to be displayed */
	public int nbVisibleLines;
	
	/** Index of the currently selected line. -1 for no selection */
	protected int selectedLine = -1;
	
	/** True if this control is enabled, false to disable. */
	public boolean enabled = true;
	
	/** Hides the select completely if false. */
	public boolean draw = true;
	
	public GuiSelect(int id, int xPosition, int yPosition, int width, int nbVisibleLines, int lineHeight) {
		super(id, xPosition, yPosition, width, nbVisibleLines * lineHeight, null);
		this.lineHeight = lineHeight;
		this.nbVisibleLines = nbVisibleLines;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getLineAt(int mouseX, int mouseY, int nbLines) {
		if (mouseX < xPosition || mouseX > xPosition + width - scrollbarWidth || mouseY < yPosition || mouseY > yPosition + height) return -2;
		else {
			int line = (mouseY - yPosition) / lineHeight + getFirstVisibleLine(currentScroll, nbLines, nbVisibleLines);
			return line >= nbLines ? -1 : line;
		}
	}
	
	public int getSelectedLine() {
		return selectedLine;
	}
	
	public void setSelectedLine(int selectedLine) {
		if (selectedLine < 0 || selectedLine >= getNbLines()) {
			selectedLine = -1;
		}
		this.selectedLine = lineSelected(selectedLine, this.selectedLine);
	}
	
	public void unselectLine() {
		selectedLine = -1;
	}
	
	public float getCurrentScroll() {
		return currentScroll;
	}
	
	public void setCurrentScroll(float scroll) {
		if (scroll < 0f) {
			scroll = 0f;
		}
		else if (scroll > 1f) {
			scroll = 1f;
		}
		currentScroll = scroll;
	}
	
	public static int getFirstVisibleLine(float scroll, int nbLines, int nbVisibleLines) {
		int firstVisibleLine = Math.round(scroll * (nbLines - nbVisibleLines + 1));
		if (firstVisibleLine > nbLines - nbVisibleLines) {
			firstVisibleLine = nbLines - nbVisibleLines;
		}
		if (firstVisibleLine < 0) {
			firstVisibleLine = 0;
		}
		return firstVisibleLine;
	}
	
	public static float getScroll(int firstVisibleLine, int nbLines, int nbVisibleLines) {
		float scroll = firstVisibleLine / (float)(nbLines - nbVisibleLines + 1);
		if (scroll < 0f) {
			scroll = 0f;
		}
		else if (scroll > 1f) {
			scroll = 1f;
		}
		return scroll;
	}
	
	/**
	 * Return the number of lines in this select. Must be overridden.
	 */
	protected abstract int getNbLines();
	
	/**
	 * Called when a new line is selected by the user. Return the line that must be selected (-1 for none, prevLine to prevent the change).
	 */
	protected int lineSelected(int line, int prevLine) {
		return line;
	}
	
	/**
	 * Returns true if the mouse has been pressed on this control. Equivalent of MouseListener.mousePressed(MouseEvente).
	 */
	@Override
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		boolean pressed = enabled && draw && mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height;
		if (pressed) {
			int line = getLineAt(mouseX, mouseY, getNbLines());
			if (line != -2) {
				selectedLine = lineSelected(line, selectedLine);
			}
			else if (mouseX >= xPosition + width - scrollbarWidth) {
				setCurrentScroll((mouseY - yPosition) / (float)height);
			}
		}
		return pressed;
	}
	
	/**
	 * Draws 1 line of this select to the screen. Must be overridden. Outline the selected line.
	 */
	public void drawLine(int lineNb, int left, int top, int right, int bottom, boolean selected, boolean enabled, Tessellator tessellator) {
		// Inspired by GuiSlot.drawScreen():
		if (selected) {
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			tessellator.startDrawingQuads();
			tessellator.setColorOpaque_I(enabled ? 0xe0e0e0 : 0x404040);
			tessellator.addVertexWithUV(left, bottom, 0.0D, 0.0D, 1.0D);
			tessellator.addVertexWithUV(right, bottom, 0.0D, 1.0D, 1.0D);
			tessellator.addVertexWithUV(right, top, 0.0D, 1.0D, 0.0D);
			tessellator.addVertexWithUV(left, top, 0.0D, 0.0D, 0.0D);
			tessellator.setColorOpaque_I(0);
			tessellator.addVertexWithUV(left + 1, bottom - 1, 0.0D, 0.0D, 1.0D);
			tessellator.addVertexWithUV(right - 1, bottom - 1, 0.0D, 1.0D, 1.0D);
			tessellator.addVertexWithUV(right - 1, top + 1, 0.0D, 1.0D, 0.0D);
			tessellator.addVertexWithUV(left + 1, top + 1, 0.0D, 0.0D, 0.0D);
			tessellator.draw();
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}
	}
	
	public void drawScrollbar(int left, int top, int right, int bottom, float scroll, float viewed, boolean enabled, Tessellator tessellator) {
		if (viewed >= 1f) return;
		int height = bottom - top;
		int scrollHeight = (int)(height * viewed);
		if (scrollHeight < 5) {
			scrollHeight = 5;
		}
		int scrollTop = top + (int)(height * scroll) - scrollHeight / 2;
		if (scrollTop + scrollHeight > top + height) {
			scrollTop = top + height - scrollHeight;
		}
		if (scrollTop < top) {
			scrollTop = top;
		}
		drawRect(left, scrollTop, right, scrollTop + scrollHeight, enabled ? 0xff808080 : 0xff404040);
	}
	
	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		draw(mc, mouseX, mouseY);
		mouseDragged(mc, mouseX, mouseY);
	}
	
	/**
	 * Draws this select to the screen.
	 */
	public void draw(Minecraft mc, int mouseX, int mouseY) {
		if (!draw) return;
		
		Tessellator tessellator = Tessellator.instance;
		int nbLines = getNbLines();
		
		int firstVisibleLine = getFirstVisibleLine(currentScroll, nbLines, nbVisibleLines);
		int lastVisibleLine = firstVisibleLine + nbVisibleLines - 1;
		if (lastVisibleLine >= nbLines) {
			lastVisibleLine = nbLines - 1;
		}
		int lineTop = yPosition - lineHeight;
		int lineBottom = lineTop + lineHeight;
		int lineRight = xPosition + width - scrollbarWidth;
		for (int i = firstVisibleLine; i <= lastVisibleLine; i++) {
			lineTop += lineHeight;
			lineBottom = lineTop + lineHeight;
			
			drawLine(i, xPosition, lineTop, lineRight, lineBottom, i == selectedLine, enabled, tessellator);
		}
		
		float scroll = getScroll(firstVisibleLine, nbLines, nbVisibleLines);
		float viewed = (float)nbVisibleLines / nbLines;
		drawScrollbar(lineRight, yPosition, lineRight + scrollbarWidth, yPosition + height, scroll, viewed, enabled, tessellator);
	}
}
