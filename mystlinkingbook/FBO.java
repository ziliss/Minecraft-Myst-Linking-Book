package net.minecraft.src.mystlinkingbook;

import java.awt.image.BufferedImage;

import org.lwjgl.opengl.ARBVertexBlend;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GLContext;

/**
 * http://lwjgl.org/wiki/index.php?title=Using_Frame_Buffer_Objects_%28FBO%29
 * 
 * @author ziliss
 * @since 0.7b (as a nested class of ImagesOnTextureManager)
 */
public class FBO {
	
	/** Tells whether FBOs can be used in the current OpenGL context. Initialized in load(). */
	public static boolean FBOEnabled = false;
	
	public int width;
	public int height;
	
	protected int fboId = -1;
	protected int texId = -1;
	protected int depthBufId = -1;
	
	protected int prevBoundFBO = -1;
	
	public static void load() {
		// Check if GL_EXT_framebuffer_object can be used in the current OpenGL context:
		FBOEnabled = GLContext.getCapabilities().GL_EXT_framebuffer_object;
	}
	
	public static FBO createTextureFBO(int width, int height, Mod_MystLinkingBook mod_mlb) {
		if (FBOEnabled) {
			// create a texture:
			int texId = mod_mlb.allocateTextureId();
			BufferedImage textureImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			mod_mlb.mc.renderEngine.setupTexture(textureImage, texId);
			
			return new FBO(texId, width, height);
		}
		else return null;
	}
	
	protected FBO(int texId, int width, int height) {
		this.texId = texId;
		this.width = width;
		this.height = height;
		
		// Create a new framebuffer then switch to it:
		fboId = EXTFramebufferObject.glGenFramebuffersEXT();
		EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, fboId);
		
		// Attach the texture to the framebuffer:
		EXTFramebufferObject.glFramebufferTexture2DEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT, GL11.GL_TEXTURE_2D, texId, 0);
		// Set the filter to not use mipmaps, because they are not generated anyway:
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		
		// Create the depth renderbuffer:
		depthBufId = EXTFramebufferObject.glGenRenderbuffersEXT();
		EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, depthBufId); // bind the depth renderbuffer
		EXTFramebufferObject.glRenderbufferStorageEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, GL14.GL_DEPTH_COMPONENT24, width, height); // get the data space for it
		EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, depthBufId); // bind it to the renderbuffer
		
		int framebuffer = EXTFramebufferObject.glCheckFramebufferStatusEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT);
		if (framebuffer != EXTFramebufferObject.GL_FRAMEBUFFER_COMPLETE_EXT) {
			String msg = "FrameBuffer: " + fboId;
			fboId = -1;
			switch (framebuffer) {
				case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT:
					throw new RuntimeException(msg + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT exception");
				case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT:
					throw new RuntimeException(msg + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT exception");
				case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT:
					throw new RuntimeException(msg + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT exception");
				case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT:
					throw new RuntimeException(msg + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT exception");
				case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT:
					throw new RuntimeException(msg + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT exception");
				case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT:
					throw new RuntimeException(msg + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT exception");
				default:
					throw new RuntimeException(msg + ", unexpected reply from glCheckFramebufferStatusEXT: " + framebuffer);
			}
		}
		
		EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);
	}
	
	public void startDrawingTexture() {
		// Store the current state:
		GL11.glMatrixMode(ARBVertexBlend.GL_MODELVIEW0_ARB);
		GL11.glPushMatrix();
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_LIGHTING_BIT | GL11.GL_FOG_BIT);
		GL11.glMatrixMode(ARBVertexBlend.GL_MODELVIEW0_ARB);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_FOG);
		
		startDrawing();
		
		// Inspired by EntityRenderer.setupOverlayRendering()
		// Setup GUI-like rendering for the FBO:
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0.0D, width, 0.0D, height, -1D, 1D);
		GL11.glMatrixMode(ARBVertexBlend.GL_MODELVIEW0_ARB);
		GL11.glLoadIdentity();
	}
	
	public void endDrawingTexture() {
		endDrawing();
		
		// Reset to the previous state:
		GL11.glPopAttrib();
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPopMatrix();
		GL11.glMatrixMode(ARBVertexBlend.GL_MODELVIEW0_ARB);
		GL11.glPopMatrix();
	}
	
	public void startDrawing() {
		prevBoundFBO = GL11.glGetInteger(EXTFramebufferObject.GL_FRAMEBUFFER_BINDING_EXT);
		if (prevBoundFBO == fboId) return;
		
		// Inspired by http://lwjgl.org/wiki/index.php?title=Using_Frame_Buffer_Objects_%28FBO%29
		// Bind the FBO instead of the screen:
		EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, fboId);
		GL11.glPushAttrib(GL11.GL_VIEWPORT_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glViewport(0, 0, width, height);
	}
	
	public void endDrawing() {
		if (GL11.glGetInteger(EXTFramebufferObject.GL_FRAMEBUFFER_BINDING_EXT) != fboId) return;
		
		// Bind the screen, unbinding the FBO:
		GL11.glPopAttrib();
		EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, prevBoundFBO);
		prevBoundFBO = -1;
	}
	
	public void clear() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
	}
}
