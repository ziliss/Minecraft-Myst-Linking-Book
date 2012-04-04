package net.minecraft.src.mystlinkingbook;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Gui;
import net.minecraft.src.ModLoader;
import net.minecraft.src.Tessellator;
import net.minecraft.src.mystlinkingbook.ScheduledActionsManager.ScheduledAction;
import net.minecraft.src.mystlinkingbook.ScheduledActionsManager.ScheduledActionRef;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBVertexBlend;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

/**
 * 
 * @author ziliss
 * @since 0.7b
 */
public class ImagesOnTextureManager {
	
	/**
	 * Reference to the mod instance.
	 */
	public Mod_MystLinkingBook mod_MLB;
	
	public Minecraft mc;
	
	public Tessellator tessellator = Tessellator.instance;
	
	public final int textureWidth;
	public final int textureHeight;
	public final int imagesWidth;
	public final int imagesHeight;
	public final int nbImagesPerLine;
	public final int nbImagesPerColumn;
	public final int nbImagesPerTexture;
	
	public ArrayList<TextureContainer> textures = new ArrayList<TextureContainer>();
	public int maxIncompleteTextures = 5;
	public int minIncompleteTextures = 1;
	public ArrayList<TextureContainer> incompleteTextures = new ArrayList<TextureContainer>();
	
	public Comparator<TextureContainer> descendingNbFreeComparator = new Comparator<TextureContainer>() {
		@Override
		public int compare(TextureContainer o1, TextureContainer o2) {
			return o2.free - o1.free;
		}
	};
	
	ScheduledActionRef optimizationActionRef;
	int timeSinceSchedulingOptimization = 0;
	int maxOptimizationDelay = 20 * 20;
	
	public FBO fbo;
	
	ByteBuffer screenBuffer;
	ByteBuffer captureBuffer;
	
	public ImagesOnTextureManager(int textureWidth, int textureHeight, int imagesWidth, int imagesHeight, Mod_MystLinkingBook mod_MLB) {
		this.mod_MLB = mod_MLB;
		mc = mod_MLB.mc;
		this.imagesWidth = imagesWidth;
		this.imagesHeight = imagesHeight;
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
		nbImagesPerLine = textureWidth / imagesWidth;
		nbImagesPerColumn = textureHeight / imagesHeight;
		nbImagesPerTexture = nbImagesPerLine * nbImagesPerColumn;
		
		optimizationActionRef = mod_MLB.scheduledActionsManager.getNewReadyScheduledActionRef(new ScheduledAction() {
			@Override
			public void executeOnce() {
				optimizeTextures();
			}
		});
		
		fbo = new FBO(mod_MLB.getTextureId());
	}
	
	public BufferedImage takeImageFromScreen() {
		return takeImageFromScreen(imagesWidth, imagesHeight);
	}
	
	public BufferedImage takeImageFromScreen(int width, int height) {
		Minecraft mc = ModLoader.getMinecraftInstance();
		
		boolean scaleDownAfterRender = false;
		
		int screenWidth = mc.displayWidth;
		int screenHeight = mc.displayHeight;
		
		int captureWidth, captureHeight, captureLeft, captureTop;
		
		if (scaleDownAfterRender) {
			float screenAspectRatio = (float)screenWidth / (float)screenHeight;
			float captureAspectRatio = (float)width / (float)height;
			
			if (screenAspectRatio < captureAspectRatio) {
				captureWidth = screenWidth;
				captureHeight = (int)(captureWidth / captureAspectRatio);
			}
			else {
				captureHeight = screenHeight;
				captureWidth = (int)(captureHeight * captureAspectRatio);
			}
			captureLeft = (screenWidth - captureWidth) / 2;
			captureTop = (screenHeight - captureHeight) / 2;
		}
		else {
			captureLeft = 0;
			captureTop = 0;
			captureWidth = width;
			captureHeight = height;
		}
		
		screenBuffer = readRGBPixelsToByteBuffer(screenBuffer, 0, 0, screenWidth, screenHeight);
		
		// Re-render the last render without gui so that we can take a screenshot of the screen:
		boolean guiHidden = mc.gameSettings.hideGUI;
		mc.gameSettings.hideGUI = true;
		if (!scaleDownAfterRender) {
			mc.displayWidth = width;
			mc.displayHeight = height;
		}
		mc.entityRenderer.renderWorld(PrivateAccesses.Minecraft_timer.getFrom(mc).renderPartialTicks, 0);
		GL11.glFlush();
		
		// Inspired by ScreenShotHelper:
		captureBuffer = readRGBPixelsToByteBuffer(captureBuffer, captureLeft, captureTop, captureWidth, captureHeight);
		
		if (!scaleDownAfterRender) {
			mc.displayWidth = screenWidth;
			mc.displayHeight = screenHeight;
			GL11.glViewport(0, 0, mc.displayWidth, mc.displayHeight);
		}
		mc.gameSettings.hideGUI = guiHidden;
		
		GL11.glDrawPixels(screenWidth, screenHeight, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, screenBuffer);
		GL11.glFlush();
		
		BufferedImage bufferedimage = new BufferedImage(captureWidth, captureHeight, BufferedImage.TYPE_INT_RGB);
		
		int x, y, r, g, b;
		for (y = 0; y < captureHeight; y++) {
			for (x = 0; x < captureWidth; x++) {
				r = captureBuffer.get() & 0xff;
				g = captureBuffer.get() & 0xff;
				b = captureBuffer.get() & 0xff;
				bufferedimage.setRGB(x, captureHeight - y - 1, 0xff000000 | r << 16 | g << 8 | b);
			}
		}
		
		if (scaleDownAfterRender) {
			bufferedimage = getScaledImage(bufferedimage, width, height);
		}
		return bufferedimage;
	}
	
	public ByteBuffer readRGBPixelsToByteBuffer(ByteBuffer buffer, int left, int top, int width, int height) {
		if (buffer == null || buffer.capacity() < width * height * 3) {
			buffer = BufferUtils.createByteBuffer(width * height * 3);
		}
		GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
		buffer.clear();
		GL11.glReadPixels(left, top, width, height, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, buffer);
		buffer.clear();
		return buffer;
	}
	
	public byte[] getRawBytesFromImage(BufferedImage image) {
		if (image == null) return new byte[0];
		int width = image.getWidth();
		int height = image.getHeight();
		byte[] imageBytes = new byte[width * height * 3];
		
		try {
			int x, y, pixelNb, argb;
			for (y = 0; y < height; y++) {
				for (x = 0; x < width; x++) {
					pixelNb = (x * height + y) * 3;
					argb = image.getRGB(x, y);
					imageBytes[pixelNb] = (byte)(argb >> 16 & 0xff);
					imageBytes[pixelNb + 1] = (byte)(argb >> 8 & 0xff);
					imageBytes[pixelNb + 2] = (byte)(argb & 0xff);
				}
			}
		}
		catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
			return new byte[0];
		}
		return imageBytes;
	}
	
	public BufferedImage getImageFromRawBytes(byte[] imageBytes, int width, int height) {
		if (imageBytes == null || imageBytes.length == 0) return null;
		
		BufferedImage bufferedimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		try {
			int x, y, pixelNb, r, g, b;
			for (y = 0; y < height; y++) {
				for (x = 0; x < width; x++) {
					pixelNb = (x * height + y) * 3;
					r = imageBytes[pixelNb] & 0xff;
					g = imageBytes[pixelNb + 1] & 0xff;
					b = imageBytes[pixelNb + 2] & 0xff;
					bufferedimage.setRGB(x, y, 0xff000000 | r << 16 | g << 8 | b);
				}
			}
		}
		catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
			return null;
		}
		
		return bufferedimage;
	}
	
	public byte[] getPNGBytesFromImage(BufferedImage image) {
		ByteArrayOutputStream imageBytes = new ByteArrayOutputStream();
		try {
			ImageIO.write(image, "PNG", imageBytes);
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
		return imageBytes.toByteArray();
	}
	
	public BufferedImage getImageFromPNGBytes(byte[] imageBytes) {
		if (imageBytes == null || imageBytes.length == 0) return null;
		try {
			return ImageIO.read(new ByteArrayInputStream(imageBytes));
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public BufferedImage getScaledImage(BufferedImage image, int newWidth, int newHeight) {
		// Inspired by: http://helpdesk.objects.com.au/java/how-do-i-scale-a-bufferedimage
		
		// Create new (blank) image of required (scaled) size:
		BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, image.getType());
		// Paint scaled version of image to new image:
		Graphics2D graphics2D = scaledImage.createGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		graphics2D.drawImage(image, 0, 0, newWidth, newHeight, null);
		// clean up:
		graphics2D.dispose();
		
		return scaledImage;
	}
	
	public ImageRef registerImage(BufferedImage image) {
		TextureContainer texture = null;
		int lastFree = nbImagesPerTexture + 1;
		for (TextureContainer incompleteTexture : incompleteTextures) {
			if (incompleteTexture.free < lastFree) {
				texture = incompleteTexture;
				lastFree = incompleteTexture.free;
			}
		}
		if (texture == null || lastFree == 0) {
			texture = new TextureContainer(mod_MLB);
			textures.add(texture);
			incompleteTextures.add(texture);
		}
		
		ImageRef imageRef = texture.addImage(image);
		
		if (texture.free == 0) {
			incompleteTextures.remove(texture);
		}
		scheduleTexturesOptimization();
		
		return imageRef;
	}
	
	public void updateImage(ImageRef imageRef, BufferedImage image) {
		imageRef.texture.updateImage(imageRef.pos, image);
	}
	
	public void disposeImage(ImageRef imageRef) {
		imageRef.texture.removeImage(imageRef);
		scheduleTexturesOptimization();
	}
	
	public void scheduleTexturesOptimization() {
		if (incompleteTextures.size() > maxIncompleteTextures) {
			int remainingTime = optimizationActionRef.getTimeBeforeNextExecution();
			if (remainingTime != -1 && timeSinceSchedulingOptimization >= 5 * maxOptimizationDelay) return;
			int addToDelay = remainingTime >= 0 ? maxOptimizationDelay / 5 : maxOptimizationDelay;
			if (remainingTime + addToDelay > maxOptimizationDelay) {
				addToDelay = maxOptimizationDelay - remainingTime;
			}
			timeSinceSchedulingOptimization += addToDelay;
			optimizationActionRef.reschedule(remainingTime + addToDelay);
		}
	}
	
	public void optimizeTextures() {
		System.out.println("optimizeTextures");
		Collections.sort(incompleteTextures, descendingNbFreeComparator);
		
		int srcIndex = incompleteTextures.size() - 1;
		int destIndex = 0;
		TextureContainer src;
		TextureContainer dest = incompleteTextures.get(destIndex);
		for (; srcIndex > destIndex; srcIndex--) {
			src = incompleteTextures.get(srcIndex);
			for (int imgIndex = 0; imgIndex < nbImagesPerTexture; imgIndex++) {
				if (src.images[imgIndex] != null) {
					moveImageTo(src.images[imgIndex], dest);
				}
				if (incompleteTextures.get(destIndex).free == 0) {
					destIndex++;
					dest = incompleteTextures.get(destIndex);
					if (destIndex == srcIndex) {
						break;
					}
				}
			}
		}
		
		if (srcIndex < destIndex + minIncompleteTextures) {
			srcIndex = destIndex + minIncompleteTextures;
		}
		for (int i = incompleteTextures.size() - 1; i > srcIndex; i--) {
			TextureContainer texture = incompleteTextures.remove(i);
			textures.remove(texture);
			texture.release();
		}
		for (int i = 0; i <= destIndex; i++) {
			incompleteTextures.set(i, incompleteTextures.get(i));
		}
		for (int i = incompleteTextures.size() - 1; i > destIndex; i--) {
			incompleteTextures.remove(i);
		}
	}
	
	void moveImageTo(ImageRef srcImageRef, TextureContainer destTexture) {
		TextureContainer srcTexture = srcImageRef.texture;
		int srcPos = srcImageRef.pos;
		BufferedImage image = srcTexture.textureImage.getSubimage(srcTexture.getLeft(srcPos), srcTexture.getTop(srcPos), imagesWidth, imagesHeight);
		
		ImageRef destImageRef = destTexture.addImage(image);
		srcImageRef.texture.removeImage(srcImageRef);
		destImageRef.copyRefTo(srcImageRef);
		destTexture.images[destImageRef.pos] = srcImageRef;
	}
	
	public void drawImage(ImageRef imageRef, int x, int y) {
		drawImage(imageRef, x, y, imagesWidth, imagesHeight);
	}
	
	public void drawImage(ImageRef imageRef, int x, int y, int destWidth, int destHeight) {
		mc.renderEngine.bindTexture(imageRef.texture.glId);
		float srcLeft = imageRef.texture.getLeft(imageRef.pos);
		float srcTop = imageRef.texture.getTop(imageRef.pos);
		float srcRight = (srcLeft + imagesWidth) / 256f;
		float srcBottom = (srcTop + imagesHeight) / 256f;
		srcLeft /= 256f;
		srcTop /= 256f;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(x, y + destHeight, 0, srcLeft, srcBottom);
		tessellator.addVertexWithUV(x + destWidth, y + destHeight, 0, srcRight, srcBottom);
		tessellator.addVertexWithUV(x + destWidth, y, 0, srcRight, srcTop);
		tessellator.addVertexWithUV(x, y, 0, srcLeft, srcTop);
		tessellator.draw();
	}
	
	public void drawImage2(ImageRef imageRef, int x, int y) {
		drawImage2(imageRef, x, y, imagesWidth, imagesHeight);
	}
	
	public void drawImage2(ImageRef imageRef, int x, int y, int destWidth, int destHeight) {
		mc.renderEngine.bindTexture(imageRef.texture.glId);
		float srcLeft = imageRef.texture.getLeft(imageRef.pos);
		float srcTop = imageRef.texture.getTop(imageRef.pos);
		float srcRight = (srcLeft + imagesWidth) / 256f;
		float srcBottom = (srcTop + imagesHeight) / 256f;
		srcLeft /= 256f;
		srcTop /= 256f;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(x + destWidth, y + destHeight, 0, srcRight, srcBottom);
		tessellator.addVertexWithUV(x, y + destHeight, 0, srcLeft, srcBottom);
		tessellator.addVertexWithUV(x, y, 0, srcLeft, srcTop);
		tessellator.addVertexWithUV(x + destWidth, y, 0, srcRight, srcTop);
		tessellator.draw();
	}
	
	public void drawImage(ImageRef imageRef, int x, int y, Gui gui) {
		drawImage(imageRef, x, y, imagesWidth, imagesHeight, gui);
	}
	
	public void drawImage(ImageRef imageRef, int x, int y, int width, int height, Gui gui) {
		mc.renderEngine.bindTexture(imageRef.texture.glId);
		gui.drawTexturedModalRect(x, y, imageRef.texture.getLeft(imageRef.pos), imageRef.texture.getTop(imageRef.pos), width, height);
		// gui.drawTexturedModalRect(0, 0, 0, 0, textureWidth, textureHeight);
	}
	
	public class TextureContainer implements Comparable<TextureContainer> {
		
		/**
		 * Reference to the mod instance.
		 */
		public Mod_MystLinkingBook mod_MLB;
		
		public final int glId;
		
		public BufferedImage textureImage = new BufferedImage(textureWidth, textureHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics2D = textureImage.createGraphics();
		
		public final ImageRef[] images = new ImageRef[nbImagesPerTexture];
		public int free = nbImagesPerTexture;
		
		TextureContainer(Mod_MystLinkingBook mod_MLB) {
			this(mod_MLB.getTextureId(), mod_MLB);
		}
		
		TextureContainer(int textureId, Mod_MystLinkingBook mod_MLB) {
			this.mod_MLB = mod_MLB;
			glId = textureId;
			
			graphics2D.setColor(new Color(255, 255, 255, 0)); // Set transparent color as default color for any operation (like fillRect())
		}
		
		public ImageRef addImage(BufferedImage image) {
			int pos = 0;
			for (; pos < images.length && images[pos] != null; pos++) {
			}
			if (pos == nbImagesPerTexture) throw new RuntimeException("No free position in texture.");
			
			images[pos] = new ImageRef(this, pos);
			free--;
			
			updateImage(pos, image);
			
			return images[pos];
		}
		
		public void updateImage(int pos, BufferedImage image) {
			int left = getLeft(pos);
			int top = getTop(pos);
			
			graphics2D.fillRect(left, top, imagesWidth, imagesHeight);
			
			if (image != null) {
				graphics2D.drawImage(image, left, top, imagesWidth, imagesHeight, null);
				
				mod_MLB.mc.renderEngine.setupTexture(textureImage, glId);
			}
		}
		
		public void removeImage(ImageRef imageRef) {
			images[imageRef.pos] = null;
			free++;
			imageRef.texture = null;
			imageRef.pos = -1;
		}
		
		public int getLeft(int pos) {
			return pos % nbImagesPerLine * imagesWidth;
		}
		
		public int getTop(int pos) {
			return pos / nbImagesPerLine * imagesHeight;
		}
		
		public void release() {
			graphics2D.dispose();
			mod_MLB.mc.renderEngine.deleteTexture(glId);
			mod_MLB.addReleasedTextureId(glId);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (obj instanceof ImagesOnTextureManager) {
				TextureContainer other = (TextureContainer)obj;
				return glId == other.glId;
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return glId;
		}
		
		@Override
		public int compareTo(TextureContainer o) {
			return this.glId - o.glId;
		}
	}
	
	public class ImageRef {
		
		public TextureContainer texture;
		
		public int pos;
		
		ImageRef(TextureContainer texture, int pos) {
			this.texture = texture;
			this.pos = pos;
		}
		
		public int getLeft() {
			return texture.getLeft(pos);
		}
		
		public int getTop() {
			return texture.getTop(pos);
		}
		
		public int getWidth() {
			return imagesWidth;
		}
		
		public int getHeight() {
			return imagesHeight;
		}
		
		public int getTextureId() {
			return texture.glId;
		}
		
		void copyRefTo(ImageRef dest) {
			dest.texture = texture;
			dest.pos = pos;
		}
		
		public void dispose() {
			disposeImage(this);
		}
	}
	
	// http://lwjgl.org/wiki/index.php?title=Using_Frame_Buffer_Objects_%28FBO%29
	public class FBO {
		
		public boolean FBOEnabled;
		
		int fboId = -1;
		int fboTexId = -1;
		public ImageRef fboImageRef;
		
		public FBO(int myFBOTexId) {
			this.fboTexId = myFBOTexId;
			
			FBOEnabled = GLContext.getCapabilities().GL_EXT_framebuffer_object;
			
			if (FBOEnabled) {
				IntBuffer buffer = ByteBuffer.allocateDirect(256 * 256 * 4).order(ByteOrder.nativeOrder()).asIntBuffer(); // allocate a 256*256 int byte buffer
				EXTFramebufferObject.glGenFramebuffersEXT(buffer); // generate
				fboId = buffer.get();
				
				BufferedImage emptyImage = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
				mod_MLB.mc.renderEngine.setupTexture(emptyImage, 50788);
				mc.renderEngine.setupTexture(emptyImage, myFBOTexId);
				
				EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, fboId);
				EXTFramebufferObject.glFramebufferTexture2DEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT, GL11.GL_TEXTURE_2D, myFBOTexId, 0);
				
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
				
				fboImageRef = new TextureContainer(myFBOTexId, mod_MLB).addImage(null);
				
				EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);
			}
			else {
				System.out.println("FBO (OpenGL FrameBuffer Objects) are unsupported !");
			}
		}
		
		public void startDrawingTexture() {
			// if (EXTFramebufferObject.glIsFramebufferEXT(myFBOId)) return;
			
			// Store the current state:
			GL11.glMatrixMode(ARBVertexBlend.GL_MODELVIEW0_ARB);
			GL11.glPushMatrix();
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glPushMatrix();
			GL11.glMatrixMode(ARBVertexBlend.GL_MODELVIEW0_ARB);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glDisable(GL11.GL_LIGHTING);
			
			// Inspired by http://lwjgl.org/wiki/index.php?title=Using_Frame_Buffer_Objects_%28FBO%29:
			// Bind the FBO instead of the screen:
			EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, fboId);
			GL11.glPushAttrib(GL11.GL_VIEWPORT_BIT);
			GL11.glViewport(0, 0, textureWidth, textureHeight);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
			
			// Inspired by EntityRenderer.setupOverlayRendering()
			// Setup GUI-like rendering for the FBO:
			GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glLoadIdentity();
			GL11.glOrtho(0.0D, textureWidth, 0.0D, textureHeight, -1D, 1D);
			GL11.glMatrixMode(ARBVertexBlend.GL_MODELVIEW0_ARB);
			GL11.glLoadIdentity();
		}
		
		public void endDrawingTexture() {
			// if (!EXTFramebufferObject.glIsFramebufferEXT(myFBOId)) return;
			
			// Bind the screen, unbinding the FBO:
			GL11.glPopAttrib();
			EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);
			
			// Reset to the previous state:
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glPopMatrix();
			GL11.glMatrixMode(ARBVertexBlend.GL_MODELVIEW0_ARB);
			GL11.glPopMatrix();
			// GL11.glMatrixMode(ARBVertexBlend.GL_MODELVIEW0_ARB); // Is this necessary ?
		}
		
		public void clear() {
			// if (!EXTFramebufferObject.glIsFramebufferEXT(myFBOId)) return;
			
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		}
	}
}
