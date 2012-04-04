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
	public TileEntityLinkingBook entityLB;
	
	public ImagesOnTextureManager itm;
	
	public final static Random rand = new Random();
	
	public Gui gui = new Gui();
	
	public float noiseLevel;
	
	public ImageRef imageRef = null;
	public int linkingPanelImageRefUses = 0;
	ScheduledActionRef releaseLinkingPanelImageActionRef;
	
	public static final Tessellator tessellator = Tessellator.instance;
	
	public LinkingPanel(TileEntityLinkingBook entityLB) {
		this.entityLB = entityLB;
		itm = entityLB.mod_MLB.itm;
		
		releaseLinkingPanelImageActionRef = entityLB.mod_MLB.scheduledActionsManager.getNewReadyScheduledActionRef(new ScheduledAction() {
			@Override
			public void executeOnce() {
				if (linkingPanelImageRefUses == 0) {
					unloadLinkingPanelImage();
				}
			}
		});
	}
	
	public void notifyNbMissingPagesChanged() {
		int nbPages = entityLB.nbPages;
		int maxPages = entityLB.maxPages;
		noiseLevel = maxPages == 0 ? maxPages : (float)(maxPages - nbPages) / maxPages;
	}
	
	public void notifyLinkingPanelImageChanged() {
		if (imageRef != null) {
			loadLinkingPanelImage();
		}
	}
	
	public LinkingPanel acquireLinkingPanel() {
		if (imageRef == null) {
			loadLinkingPanelImage();
		}
		else {
			releaseLinkingPanelImageActionRef.unschedule();
		}
		linkingPanelImageRefUses++;
		return this;
	}
	
	public void releaseLinkingPanel() {
		linkingPanelImageRefUses--;
		if (linkingPanelImageRefUses == 0) {
			releaseLinkingPanelImageActionRef.reschedule(20 * 20);
		}
	}
	
	public void unloadLinkingPanelImage() {
		if (imageRef != null) {
			if (imageRef != entityLB.mod_MLB.missingLinkingPanelImage.imageRef) {
				imageRef.dispose();
			}
			imageRef = null;
		}
	}
	
	public void loadLinkingPanelImage() {
		BufferedImage linkingPanelImage = entityLB.mod_MLB.linkingBook.getLinkingPanelImage(entityLB.nbttagcompound_linkingBook);
		if (imageRef == entityLB.mod_MLB.missingLinkingPanelImage.imageRef && linkingPanelImage != null) {
			unloadLinkingPanelImage();
		}
		if (imageRef == null) {
			imageRef = linkingPanelImage == null ? entityLB.mod_MLB.missingLinkingPanelImage.imageRef : itm.registerImage(linkingPanelImage);
		}
		else {
			itm.updateImage(imageRef, linkingPanelImage);
		}
	}
	
	public void invalidate() {
		releaseLinkingPanelImageActionRef.unschedule();
		unloadLinkingPanelImage();
	}
	
	public void drawOnGui(int x, int y, int width, int height) {
		int color = 0xff000000; // black
		ImageRef drawImageRef = null;
		
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		
		if (imageRef != null && (!entityLB.isUnstable || entityLB.isPowered)) {
			if (!entityLB.getLinksToDifferentAge()) {
				// Ok, nothing to do.
			}
			else if (noiseLevel == 0f) {
				drawImageRef = imageRef;
			}
			else {
				if (itm.fbo.FBOEnabled) {
					itm.fbo.startDrawingTexture();
					
					itm.fbo.clear();
					itm.drawImage2(imageRef, 0, 0);
					drawNoise(0, 0, imageRef.getWidth(), imageRef.getHeight(), noiseLevel, 2, 0xffffffff);
					
					itm.fbo.endDrawingTexture();
					GL11.glDisable(GL11.GL_LIGHTING);
					
					drawImageRef = itm.fbo.fboImageRef;
				}
				else {
					drawImageRef = imageRef;
				}
			}
		}
		
		if (drawImageRef == null) {
			drawRect(x, y, width, height, color);
		}
		else {
			itm.drawImage(drawImageRef, x, y, width, height);
		}
	}
	
	public void draw(int x, int y, int width, int height) {
		int color = 0xff000000; // black
		ImageRef drawImageRef = null;
		
		entityLB.mod_MLB.mc.entityRenderer.disableLightmap(0);
		
		if (imageRef != null && (!entityLB.isUnstable || entityLB.isPowered)) {
			if (!entityLB.getLinksToDifferentAge()) {
				// Ok, nothing to do.
			}
			else if (noiseLevel == 0f) {
				drawImageRef = imageRef;
			}
			else {
				if (itm.fbo.FBOEnabled) {
					itm.fbo.startDrawingTexture();
					
					itm.fbo.clear();
					itm.drawImage2(imageRef, 0, 0);
					drawNoise(0, 0, imageRef.getWidth(), imageRef.getHeight(), noiseLevel, 5, 0xffffffff);
					
					itm.fbo.endDrawingTexture();
					
					drawImageRef = itm.fbo.fboImageRef;
				}
				else {
					drawImageRef = imageRef;
				}
			}
		}
		
		GL11.glDisable(GL11.GL_LIGHTING);
		
		if (drawImageRef == null) {
			drawRect(x, y, width, height, color);
		}
		else {
			itm.drawImage(drawImageRef, x, y, width, height);
		}
		
		GL11.glEnable(GL11.GL_LIGHTING);
		entityLB.mod_MLB.mc.entityRenderer.enableLightmap(0);
	}
	
	public void drawNoise(int x, int y, int width, int height, float noiseLevel, int pointSize, int color) {
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
		int nbDots = (int)(noiseLevel * (width * height));
		int dotX, dotY;
		for (int i = 0; i < nbDots; i++) {
			dotX = rand.nextInt(width) * pointSize + halfPointSize;
			dotY = rand.nextInt(height) * pointSize + halfPointSize;
			GL11.glBegin(GL11.GL_POINTS);
			GL11.glVertex3f(x + dotX, y + dotY, 0);
			GL11.glEnd();
		}/**/
		
		noiseLevel *= 0.9f;
		int i, j;
		for (i = halfPointSize; i < width; i += pointSize) {
			for (j = halfPointSize; j < height; j += pointSize) {
				if (rand.nextFloat() <= noiseLevel) {
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
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
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
		GL11.glDisable(GL11.GL_BLEND);
	}
	
	/**
	 * Draws a solid color rectangle with the specified coordinates and color.
	 */
	// Taken from GUI.drawRect():
	public void drawRect2(int x, int y, int width, int height, int color) {
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
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
		GL11.glDisable(GL11.GL_BLEND);
	}
	
}
