package net.minecraft.src.mystlinkingbook;

import net.minecraft.client.Minecraft;
import net.minecraft.src.EntityRenderer;
import net.minecraft.src.mystlinkingbook.ScheduledActionsManager.ScheduledActionRef;
import net.minecraft.src.mystlinkingbook.ScheduledActionsManager.ScheduledFrameAction;

import org.lwjgl.opengl.GL11;

/**
 * 
 * @author ziliss
 * @since 0.9b
 */
public class AgeAreaOutline {
	
	/**
	 * Reference to the mod instance.
	 */
	public Mod_MystLinkingBook mod_MLB;
	
	public AgeArea ageArea = null;
	
	protected ScheduledActionRef outlineAgeAreaActionRef;
	
	protected boolean origGL_TEXTURE_2D;
	protected boolean origGL_DEPTH_TEST;
	protected boolean origGL_BLEND;
	protected float originalLineWidth;
	protected int originalRenderDistance;
	
	public AgeAreaOutline(final Mod_MystLinkingBook mod_MLB) {
		this.mod_MLB = mod_MLB;
		
		outlineAgeAreaActionRef = mod_MLB.scheduledActionsManager.getNewReadyScheduledActionRef(new ScheduledFrameAction() {
			@Override
			public boolean execute(int nbTicks, float partialTick) {
				if (ageArea == null) return false;
				if (ageArea.dimension != mod_MLB.mc.thePlayer.dimension) {
					unsetAgeArea();
					return false;
				}
				if (mod_MLB.mc.currentScreen == null && ageArea.isValid()) {
					startOutline(partialTick);
					
					float x1 = Math.min(ageArea.pos1X, ageArea.pos2X);
					float x2 = Math.max(ageArea.pos1X, ageArea.pos2X) + 1;
					float y1 = Math.min(ageArea.pos1Y, ageArea.pos2Y);
					float y2 = Math.max(ageArea.pos1Y, ageArea.pos2Y) + 1;
					float z1 = Math.min(ageArea.pos1Z, ageArea.pos2Z);
					float z2 = Math.max(ageArea.pos1Z, ageArea.pos2Z) + 1;
					
					drawOutline(x1, y1, z1, x2, y2, z2, 0xff0000); // ageArea
					GL11.glLineWidth(2f);
					drawOutline(ageArea.pos1X, ageArea.pos1Y, ageArea.pos1Z, ageArea.pos1X + 1, ageArea.pos1Y + 1, ageArea.pos1Z + 1, 0x00ff00); // Pos1
					drawOutline(ageArea.pos2X, ageArea.pos2Y, ageArea.pos2Z, ageArea.pos2X + 1, ageArea.pos2Y + 1, ageArea.pos2Z + 1, 0x0000ff); // Pos2
					
					/*GL11.glEnable(GL11.GL_DEPTH_TEST);
					// GL11.glEnable(GL11.GL_BLEND);
					GL11.glDepthMask(false);
					GL11.glLineWidth(2f);
					drawOutline(x1, y1, z1, x2, y2, z2, 0xff0000); // ageArea
					drawOutline(x1, y1, z1, x1 + 1, y1 + 1, z1 + 1, 0x00ff00); // Pos1
					drawOutline(x2 - 1, y2 - 1, z2 - 1, x2, y2, z2, 0x0000ff); // Pos2*/
					
					endOutline();
				}
				return true;
			}
		});
	}
	
	protected AgesManagerListener agesManagerListener = new AgesManagerListener() {
		@Override
		public void updatePerformed(int dimension) {
			unsetAgeArea();
		}
	};
	
	public boolean isAgeAreaSet() {
		return ageArea != null;
	}
	
	public AgeArea getAgeArea() {
		return ageArea;
	}
	
	public void setAgeArea(AgeArea ageArea) {
		if (ageArea == null) {
			unsetAgeArea();
		}
		else {
			this.ageArea = ageArea;
			outlineAgeAreaActionRef.reschedule(0);
			mod_MLB.linkingBookUtils.agesManager.addListener(agesManagerListener, ageArea.dimension);
		}
	}
	
	public void unsetAgeArea() {
		if (ageArea == null) return;
		mod_MLB.linkingBookUtils.agesManager.removeListener(agesManagerListener, ageArea.dimension);
		outlineAgeAreaActionRef.unschedule();
		ageArea = null;
	}
	
	public void startOutline(float partialTick) {
		Minecraft mc = mod_MLB.mc;
		originalRenderDistance = mc.gameSettings.renderDistance;
		mc.gameSettings.renderDistance = -22; // (256>>-22)*2f == 2^31, because 256 == 2^8.
		PrivateAccesses.EntityRenderer_setupCameraTransform.invokeFrom(mc.entityRenderer, partialTick, EntityRenderer.anaglyphField);
		
		origGL_TEXTURE_2D = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
		origGL_DEPTH_TEST = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
		origGL_BLEND = GL11.glIsEnabled(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
		
		originalLineWidth = GL11.glGetFloat(GL11.GL_LINE_WIDTH);
		GL11.glLineWidth(1f);
		
		GL11.glPushMatrix();
		
		double interpPosX = mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * partialTick;
		double interpPosY = mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * partialTick;
		double interpPosZ = mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * partialTick;
		GL11.glTranslated(-(float)interpPosX, -(float)interpPosY, -(float)interpPosZ);
	}
	
	public static void drawOutline(float x1, float y1, float z1, float x2, float y2, float z2, int color) {
		byte r = (byte)(color >> 16 & 0xff);
		byte g = (byte)(color >> 8 & 0xff);
		byte b = (byte)(color & 0xff);
		GL11.glColor3ub(r, g, b);
		
		GL11.glBegin(GL11.GL_LINES);
		
		float incr = x1 > x2 ? -1f : 1f;
		float end = x2 + incr;
		for (float x = x1; x != end; x += incr) {
			GL11.glVertex3f(x, y1, z1);
			GL11.glVertex3f(x, y1, z2);
			GL11.glVertex3f(x, y1, z1);
			GL11.glVertex3f(x, y2, z1);
			GL11.glVertex3f(x, y2, z2);
			GL11.glVertex3f(x, y1, z2);
			GL11.glVertex3f(x, y2, z2);
			GL11.glVertex3f(x, y2, z1);
		}
		incr = y1 > y2 ? -1f : 1f;
		end = y2 + incr;
		for (float y = y1; y != end; y += incr) {
			GL11.glVertex3f(x1, y, z1);
			GL11.glVertex3f(x1, y, z2);
			GL11.glVertex3f(x1, y, z1);
			GL11.glVertex3f(x2, y, z1);
			GL11.glVertex3f(x2, y, z2);
			GL11.glVertex3f(x1, y, z2);
			GL11.glVertex3f(x2, y, z2);
			GL11.glVertex3f(x2, y, z1);
		}
		incr = z1 > z2 ? -1f : 1f;
		end = z2 + incr;
		for (float z = z1; z != end; z += incr) {
			GL11.glVertex3f(x1, y1, z);
			GL11.glVertex3f(x1, y2, z);
			GL11.glVertex3f(x1, y1, z);
			GL11.glVertex3f(x2, y1, z);
			GL11.glVertex3f(x2, y2, z);
			GL11.glVertex3f(x1, y2, z);
			GL11.glVertex3f(x2, y2, z);
			GL11.glVertex3f(x2, y1, z);
		}
		
		GL11.glEnd();
	}
	
	public void endOutline() {
		GL11.glPopMatrix();
		
		GL11.glLineWidth(originalLineWidth);
		
		if (origGL_TEXTURE_2D) {
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}
		if (origGL_DEPTH_TEST) {
			GL11.glEnable(GL11.GL_DEPTH_TEST);
		}
		if (origGL_BLEND) {
			GL11.glEnable(GL11.GL_BLEND);
		}
		
		mod_MLB.mc.gameSettings.renderDistance = originalRenderDistance;
		mod_MLB.mc.entityRenderer.setupOverlayRendering();
	}
}
