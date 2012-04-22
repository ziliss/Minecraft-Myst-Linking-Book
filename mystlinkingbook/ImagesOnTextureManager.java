package net.minecraft.src.mystlinkingbook;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Tessellator;
import net.minecraft.src.mystlinkingbook.ScheduledActionsManager.ScheduledAction;
import net.minecraft.src.mystlinkingbook.ScheduledActionsManager.ScheduledActionRef;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

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
	
	protected ScheduledActionRef optimizationActionRef;
	protected int timeSinceSchedulingOptimization = 0;
	protected int maxOptimizationDelay = 20 * 20;
	
	public FBO fbo = null;
	public ImageRef fboImageRef = null;
	public FBO screenShotFBO = null;
	
	protected ByteBuffer screenBuffer;
	protected ByteBuffer captureBuffer;
	
	public ImagesOnTextureManager(int textureWidth, int textureHeight, int imagesWidth, int imagesHeight, Mod_MystLinkingBook mod_MLB) {
		this.mod_MLB = mod_MLB;
		mc = mod_MLB.mc;
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
		this.imagesWidth = imagesWidth;
		this.imagesHeight = imagesHeight;
		nbImagesPerLine = textureWidth / imagesWidth;
		nbImagesPerColumn = textureHeight / imagesHeight;
		nbImagesPerTexture = nbImagesPerLine * nbImagesPerColumn;
		
		optimizationActionRef = mod_MLB.scheduledActionsManager.getNewReadyScheduledActionRef(new ScheduledAction() {
			@Override
			public void executeOnce() {
				optimizeTextures();
			}
		});
		
		fbo = FBO.createTextureFBO(textureWidth, textureHeight, mod_MLB);
		if (fbo != null) {
			fboImageRef = new CustomImageRef(fbo.texId, 0, 0);
			screenShotFBO = FBO.createTextureFBO(imagesWidth, imagesHeight, mod_MLB);
		}
		else {
			System.out.println("FBO (OpenGL Frame Buffer Objects) are unsupported !");
		}
	}
	
	public BufferedImage takeImageFromScreen() {
		if (screenShotFBO == null) return takeImageFromScreen(imagesWidth, imagesHeight);
		
		Minecraft mc = mod_MLB.mc;
		int orig_displayWidth = mc.displayWidth;
		int orig_displayHeight = mc.displayHeight;
		int captureWidth = imagesWidth;
		int captureHeight = imagesHeight;
		
		screenShotFBO.startDrawing();
		screenShotFBO.clear();
		
		// Re-render the last render without gui so that we can take a screenshot of the screen:
		mc.displayWidth = captureWidth;
		mc.displayHeight = captureHeight;
		boolean orig_hideGui = mc.gameSettings.hideGUI;
		mc.gameSettings.hideGUI = true;
		
		mc.entityRenderer.renderWorld(PrivateAccesses.Minecraft_timer.getFrom(mc).renderPartialTicks, 0); // 0 prevents the renderers from being updated.
		GL11.glFlush();
		
		// Inspired by ScreenShotHelper:
		captureBuffer = readRGBTexToByteBuffer(screenShotFBO.texId, screenShotFBO.width, screenShotFBO.height, captureBuffer);
		
		mc.gameSettings.hideGUI = orig_hideGui;
		mc.displayWidth = orig_displayWidth;
		mc.displayHeight = orig_displayHeight;
		GL11.glViewport(0, 0, mc.displayWidth, mc.displayHeight);
		
		screenShotFBO.endDrawing();
		
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
		return bufferedimage;
	}
	
	public BufferedImage takeImageFromScreen(int width, int height) {
		Minecraft mc = mod_MLB.mc;
		
		boolean scaleDownAfterRender = false;
		
		int orig_displayWidth = mc.displayWidth;
		int orig_displayHeight = mc.displayHeight;
		
		int captureWidth, captureHeight, captureLeft, captureTop;
		
		if (scaleDownAfterRender) {
			float screenAspectRatio = (float)orig_displayWidth / (float)orig_displayHeight;
			float captureAspectRatio = (float)width / (float)height;
			
			if (screenAspectRatio < captureAspectRatio) {
				captureWidth = orig_displayWidth;
				captureHeight = (int)(captureWidth / captureAspectRatio);
			}
			else {
				captureHeight = orig_displayHeight;
				captureWidth = (int)(captureHeight * captureAspectRatio);
			}
			captureLeft = (orig_displayWidth - captureWidth) / 2;
			captureTop = (orig_displayHeight - captureHeight) / 2;
		}
		else {
			captureLeft = 0;
			captureTop = 0;
			captureWidth = width;
			captureHeight = height;
		}
		
		screenBuffer = readRGBPixelsToByteBuffer(0, 0, orig_displayWidth, orig_displayHeight, screenBuffer);
		
		// Re-render the last render without gui so that we can take a screenshot of the screen:
		boolean orig_hideGUI = mc.gameSettings.hideGUI;
		mc.gameSettings.hideGUI = true;
		if (!scaleDownAfterRender) {
			mc.displayWidth = width;
			mc.displayHeight = height;
		}
		mc.entityRenderer.renderWorld(PrivateAccesses.Minecraft_timer.getFrom(mc).renderPartialTicks, 0); // 0 prevents the renderers from being updated.
		GL11.glFlush();
		
		// Inspired by ScreenShotHelper:
		captureBuffer = readRGBPixelsToByteBuffer(captureLeft, captureTop, captureWidth, captureHeight, captureBuffer);
		
		if (!scaleDownAfterRender) {
			mc.displayWidth = orig_displayWidth;
			mc.displayHeight = orig_displayHeight;
			GL11.glViewport(0, 0, mc.displayWidth, mc.displayHeight);
		}
		mc.gameSettings.hideGUI = orig_hideGUI;
		
		GL11.glDrawPixels(orig_displayWidth, orig_displayHeight, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, screenBuffer);
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
	
	public ByteBuffer readRGBPixelsToByteBuffer(int left, int top, int width, int height, ByteBuffer buffer) {
		if (buffer == null || buffer.capacity() < width * height * 3) {
			buffer = BufferUtils.createByteBuffer(width * height * 3);
		}
		buffer.clear();
		GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
		GL11.glReadPixels(left, top, width, height, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, buffer);
		buffer.clear();
		return buffer;
	}
	
	public ByteBuffer readRGBTexToByteBuffer(int texId, int texWidth, int texHeight, ByteBuffer buffer) {
		if (buffer == null || buffer.capacity() < texWidth * texHeight * 3) {
			buffer = BufferUtils.createByteBuffer(texWidth * texHeight * 3);
		}
		buffer.clear();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);
		GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
		GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, buffer);
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
	
	protected void moveImageTo(MovableImageRef srcImageRef, TextureContainer destTexture) {
		TextureContainer srcTexture = srcImageRef.texture;
		int srcPos = srcImageRef.pos;
		BufferedImage image = srcTexture.textureImage.getSubimage(srcTexture.getLeft(srcPos), srcTexture.getTop(srcPos), imagesWidth, imagesHeight);
		
		MovableImageRef destImageRef = destTexture.addImage(image);
		srcImageRef.texture.removeImage(srcImageRef);
		destImageRef.copyRefTo(srcImageRef);
		destTexture.images[destImageRef.pos] = srcImageRef;
	}
	
	public class TextureContainer implements Comparable<TextureContainer> {
		
		/**
		 * Reference to the mod instance.
		 */
		public Mod_MystLinkingBook mod_MLB;
		
		public final int texId;
		
		public BufferedImage textureImage = new BufferedImage(textureWidth, textureHeight, BufferedImage.TYPE_INT_ARGB);
		protected Graphics2D graphics2D = textureImage.createGraphics();
		
		public final MovableImageRef[] images = new MovableImageRef[nbImagesPerTexture];
		public int free = nbImagesPerTexture;
		
		protected TextureContainer(Mod_MystLinkingBook mod_MLB) {
			this.mod_MLB = mod_MLB;
			texId = mod_MLB.allocateTextureId();
			
			graphics2D.setColor(new Color(255, 255, 255, 0)); // Set transparent color as default color for any operation (like fillRect())
			
			mc.renderEngine.setupTexture(textureImage, texId);
		}
		
		public MovableImageRef addImage(BufferedImage image) {
			int pos = 0;
			for (; pos < images.length && images[pos] != null; pos++) {
			}
			if (pos == nbImagesPerTexture) throw new RuntimeException("No free position in texture.");
			
			images[pos] = new MovableImageRef(this, pos);
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
			}
			mod_MLB.mc.renderEngine.setupTexture(textureImage, texId);
		}
		
		public void removeImage(MovableImageRef imageRef) {
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
			graphics2D = null;
			mod_MLB.releasedTextureId(texId);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (obj instanceof ImagesOnTextureManager) {
				TextureContainer other = (TextureContainer)obj;
				return texId == other.texId;
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return texId;
		}
		
		@Override
		public int compareTo(TextureContainer o) {
			return this.texId - o.texId;
		}
	}
	
	public abstract class ImageRef {
		
		public abstract int getTextureId();
		
		public abstract int getLeft();
		
		public abstract int getTop();
		
		public int getWidth() {
			return imagesWidth;
		}
		
		public int getHeight() {
			return imagesHeight;
		}
		
		public abstract void updateImage(BufferedImage image);
		
		public void dispose() {
		}
		
		public void drawImage(int x, int y) {
			drawImage(x, y, getWidth(), getHeight());
		}
		
		public void drawImage(int x, int y, int destWidth, int destHeight) {
			mc.renderEngine.bindTexture(getTextureId());
			float srcLeft = getLeft();
			float srcTop = getTop();
			float srcRight = (srcLeft + getWidth()) / 256f;
			float srcBottom = (srcTop + getHeight()) / 256f;
			srcLeft /= 256f;
			srcTop /= 256f;
			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(x, y + destHeight, 0, srcLeft, srcBottom);
			tessellator.addVertexWithUV(x + destWidth, y + destHeight, 0, srcRight, srcBottom);
			tessellator.addVertexWithUV(x + destWidth, y, 0, srcRight, srcTop);
			tessellator.addVertexWithUV(x, y, 0, srcLeft, srcTop);
			tessellator.draw();
		}
		
		public void drawImage2(int x, int y) {
			drawImage2(x, y, getWidth(), getHeight());
		}
		
		public void drawImage2(int x, int y, int destWidth, int destHeight) {
			mc.renderEngine.bindTexture(getTextureId());
			float srcLeft = getLeft();
			float srcTop = getTop();
			float srcRight = (srcLeft + getWidth()) / 256f;
			float srcBottom = (srcTop + getHeight()) / 256f;
			srcLeft /= 256f;
			srcTop /= 256f;
			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(x + destWidth, y + destHeight, 0, srcRight, srcBottom);
			tessellator.addVertexWithUV(x, y + destHeight, 0, srcLeft, srcBottom);
			tessellator.addVertexWithUV(x, y, 0, srcLeft, srcTop);
			tessellator.addVertexWithUV(x + destWidth, y, 0, srcRight, srcTop);
			tessellator.draw();
		}
	}
	
	protected class MovableImageRef extends ImageRef {
		
		public TextureContainer texture;
		
		public int pos;
		
		protected MovableImageRef(TextureContainer texture, int pos) {
			this.texture = texture;
			this.pos = pos;
		}
		
		@Override
		public int getTextureId() {
			return texture.texId;
		}
		
		@Override
		public int getLeft() {
			return texture.getLeft(pos);
		}
		
		@Override
		public int getTop() {
			return texture.getTop(pos);
		}
		
		@Override
		public void updateImage(BufferedImage image) {
			texture.updateImage(pos, image);
		}
		
		protected void copyRefTo(MovableImageRef dest) {
			dest.texture = texture;
			dest.pos = pos;
		}
		
		@Override
		public void dispose() {
			texture.removeImage(this);
			scheduleTexturesOptimization();
		}
	}
	
	public class CustomImageRef extends ImageRef {
		
		protected int left;
		protected int top;
		protected int texId;
		
		protected CustomImageRef(int texId, int left, int top) {
			this.left = left;
			this.top = top;
			this.texId = texId;
		}
		
		@Override
		public int getTextureId() {
			return texId;
		}
		
		@Override
		public int getLeft() {
			return left;
		}
		
		@Override
		public int getTop() {
			return top;
		}
		
		@Override
		public void updateImage(BufferedImage image) {
			throw new UnsupportedOperationException();
		}
	}
}
