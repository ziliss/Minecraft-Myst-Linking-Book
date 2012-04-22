package net.minecraft.src.mystlinkingbook;

import java.awt.image.BufferedImage;
import java.util.Random;

import net.minecraft.src.Gui;
import net.minecraft.src.Tessellator;
import net.minecraft.src.mystlinkingbook.ImagesOnTextureManager.ImageRef;
import net.minecraft.src.mystlinkingbook.ScheduledActionsManager.ScheduledAction;
import net.minecraft.src.mystlinkingbook.ScheduledActionsManager.ScheduledActionRef;

import org.lwjgl.opengl.GL11;

/**
 * 
 * @author ziliss
 * @since 0.7b
 */
public class LinkingPanel {
	
	/**
	 * Reference to the mod instance.
	 */
	public LinkingBook linkingBook;
	
	public ImagesOnTextureManager itm;
	
	public Gui gui = new Gui();
	
	public State state;
	
	public float staticLevel;
	
	public ImageRef imageRef = null;
	public int linkingPanelImageRefUses = 0;
	protected ScheduledActionRef releaseLinkingPanelImageActionRef;
	
	public final static Random rand = new Random();
	public static final Tessellator tessellator = Tessellator.instance;
	
	public LinkingPanel(LinkingBook linkingBook) {
		this.linkingBook = linkingBook;
		itm = linkingBook.mod_MLB.itm;
		
		releaseLinkingPanelImageActionRef = linkingBook.mod_MLB.scheduledActionsManager.getNewReadyScheduledActionRef(new ScheduledAction() {
			@Override
			public void executeOnce() {
				if (linkingPanelImageRefUses == 0) {
					unloadLinkingPanelImage();
				}
			}
		});
		notifyNbMissingPagesChanged();
	}
	
	public void notifyNbMissingPagesChanged() {
		int nbPages = linkingBook.nbPages;
		int maxPages = linkingBook.maxPages;
		staticLevel = maxPages == 0 ? maxPages : (float)(maxPages - nbPages) / maxPages;
		updateState();
	}
	
	public void notifyLinkingPanelImageChanged() {
		if (imageRef != null) {
			loadLinkingPanelImage();
		}
	}
	
	public void updateState() {
		State prev = state;
		if (!linkingBook.isWritten()) {
			state = State.unwritten;
		}
		else if (!linkingBook.doLinkToDifferentAge()) {
			state = State.disabled;
		}
		else if (linkingBook.isUnstable && !linkingBook.isPowered) {
			state = State.unpowered;
		}
		else if (linkingBook.getNbMissingPages() > 0) {
			state = State.pagesMissing;
		}
		else {
			state = State.ready;
		}
		
		if (state != prev) {
			linkingBook.fireLinkingPanelStateChanged(state);
		}
	}
	
	public LinkingPanel acquireImage() {
		if (imageRef == null) {
			loadLinkingPanelImage();
		}
		else {
			releaseLinkingPanelImageActionRef.unschedule();
		}
		linkingPanelImageRefUses++;
		return this;
	}
	
	public void releaseImage() {
		linkingPanelImageRefUses--;
		if (linkingPanelImageRefUses == 0) {
			releaseLinkingPanelImageActionRef.reschedule(20 * 20);
		}
	}
	
	public void unloadLinkingPanelImage() {
		if (imageRef != null) {
			if (imageRef != linkingBook.mod_MLB.linkingPanelImageMissing.getImageRef()) {
				imageRef.dispose();
			}
			imageRef = null;
		}
	}
	
	public void loadLinkingPanelImage() {
		BufferedImage linkingPanelImage = linkingBook.getLinkingPanelImage();
		if (imageRef == linkingBook.mod_MLB.linkingPanelImageMissing.getImageRef()) {
			unloadLinkingPanelImage();
		}
		if (imageRef == null) {
			imageRef = linkingPanelImage == null ? linkingBook.mod_MLB.linkingPanelImageMissing.getImageRef() : itm.registerImage(linkingPanelImage);
		}
		else {
			imageRef.updateImage(linkingPanelImage);
		}
	}
	
	public void invalidate() {
		releaseLinkingPanelImageActionRef.unschedule();
		unloadLinkingPanelImage();
	}
	
	public void drawOnGui(int x, int y, int width, int height) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		draw(x, y, width, height, 2);
	}
	
	public void drawInGame(int x, int y, int width, int height) {
		if (state.emitsLight()) {
			linkingBook.mod_MLB.mc.entityRenderer.disableLightmap(0);
		}
		draw(x, y, width, height, 5);
		if (state.emitsLight()) {
			linkingBook.mod_MLB.mc.entityRenderer.enableLightmap(0);
		}
	}
	
	protected void draw(int x, int y, int width, int height, int staticPointSize) {
		int color = 0xff000000; // black
		ImageRef drawImageRef = null;
		
		switch (state) {
			case unwritten:
				color = 0xffffffff; // white
				break;
			case disabled:
				// Ok, nothing to do.
				break;
			case unpowered:
				// Ok, nothing to do.
				break;
			case pagesMissing:
				if (itm.fbo != null) {
					itm.fbo.startDrawingTexture();
					itm.fbo.clear();
					
					imageRef.drawImage2(0, 0);
					drawStatic(0, 0, imageRef.getWidth(), imageRef.getHeight(), staticLevel, staticPointSize, 0xffffffff);
					
					itm.fbo.endDrawingTexture();
					GL11.glDisable(GL11.GL_LIGHTING); // Because it is enabled in endDrawingTexture().
					
					drawImageRef = itm.fboImageRef;
				}
				else {
					drawImageRef = imageRef;
				}
				break;
			case ready:
				drawImageRef = imageRef;
				break;
		}
		
		if (drawImageRef == null) {
			drawRect(x, y, width, height, color);
		}
		else {
			drawImageRef.drawImage(x, y, width, height);
		}
	}
	
	public void drawStatic(int x, int y, int width, int height, float staticLevel, int pointSize, int color) {
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		float originalPointSize = GL11.glGetFloat(GL11.GL_POINT_SIZE);
		GL11.glPointSize(pointSize);
		byte a = (byte)(color >> 24 & 0xff);
		byte r = (byte)(color >> 16 & 0xff);
		byte g = (byte)(color >> 8 & 0xff);
		byte b = (byte)(color & 0xff);
		GL11.glColor4ub(r, g, b, a);
		
		int halfPointSize = pointSize / 2;
		
		/*
		width /= pointSize;
		height /= pointSize;
		int nbDots = (int)(staticLevel * (width * height));
		int dotX, dotY;
		for (int i = 0; i < nbDots; i++) {
			dotX = rand.nextInt(width) * pointSize + halfPointSize;
			dotY = rand.nextInt(height) * pointSize + halfPointSize;
			GL11.glBegin(GL11.GL_POINTS);
			GL11.glVertex3f(x + dotX, y + dotY, 0);
			GL11.glEnd();
		}/**/
		
		staticLevel *= 0.9f;
		int i, j;
		for (i = halfPointSize; i < width; i += pointSize) {
			for (j = halfPointSize; j < height; j += pointSize) {
				if (rand.nextFloat() <= staticLevel) {
					GL11.glBegin(GL11.GL_POINTS);
					GL11.glVertex3f(x + i, y + j, 0);
					GL11.glEnd();
				}
			}
		}/**/
		
		GL11.glPointSize(originalPointSize);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
	}
	
	/**
	 * Draws a solid color rectangle with the specified coordinates and color.
	 */
	// Taken from GUI.drawRect():
	public void drawRect(int x, int y, int width, int height, int color) {
		// GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		// GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		byte a = (byte)(color >> 24 & 0xff);
		byte r = (byte)(color >> 16 & 0xff);
		byte g = (byte)(color >> 8 & 0xff);
		byte b = (byte)(color & 0xff);
		GL11.glColor4ub(r, g, b, a);
		tessellator.startDrawingQuads();
		tessellator.addVertex(x, y + height, 0.0D);
		tessellator.addVertex(x + width, y + height, 0.0D);
		tessellator.addVertex(x + width, y, 0.0D);
		tessellator.addVertex(x, y, 0.0D);
		tessellator.draw();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		// GL11.glDisable(GL11.GL_BLEND);
	}
	
	/**
	 * Draws a solid color rectangle with the specified coordinates and color.
	 */
	// Taken from GUI.drawRect():
	public void drawRect2(int x, int y, int width, int height, int color) {
		// GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		// GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		byte a = (byte)(color >> 24 & 0xff);
		byte r = (byte)(color >> 16 & 0xff);
		byte g = (byte)(color >> 8 & 0xff);
		byte b = (byte)(color & 0xff);
		GL11.glColor4ub(r, g, b, a);
		tessellator.startDrawingQuads();
		tessellator.addVertex(x + width, y + height, 0.0D);
		tessellator.addVertex(x, y + height, 0.0D);
		tessellator.addVertex(x, y, 0.0D);
		tessellator.addVertex(x + width, y, 0.0D);
		tessellator.draw();
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		// GL11.glDisable(GL11.GL_BLEND);
	}
	
	public static enum State {
		
		/** When linking has not yet been written. Panel is white. */
		unwritten,
		
		/** When linking to the same Age. Panel is black. */
		disabled,
		
		/** When the book is unstable and is not powered by redstone power. Panel is black. */
		unpowered,
		
		/** When pages are missing from the book. Static is shown on the panel. */
		pagesMissing,
		
		/** Ready to link. Shows the images of the destination. */
		ready;
		
		public boolean emitsLight() {
			return this == pagesMissing || this == ready;
		}
	}
}
