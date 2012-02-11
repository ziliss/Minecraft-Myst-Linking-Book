package net.minecraft.src.mystlinkingbook;

import java.awt.Color;

import net.minecraft.src.FontRenderer;
import net.minecraft.src.MathHelper;
import net.minecraft.src.ModelBase;
import net.minecraft.src.ModelBook;
import net.minecraft.src.ModelRenderer;

import org.lwjgl.opengl.GL11;

/**
 * 3D model of the linking book.<br>
 * <br>
 * Inspired by {@code ModelBook}.
 * 
 * @author ziliss
 * @see ModelBook
 * @since 0.3a
 */
public class ModelLinkingBook extends ModelBase {
	public ModelRenderer coverLeft;
	public ModelRenderer coverRight;
	public ModelRenderer pagesLeft;
	public ModelRenderer pagesRight;
	public ModelRenderer bookSpine;
	
	public ModelRenderer pagesLeftTransparent;
	public ModelRenderer pagesRightTransparent;
	
	public float lastOpening = -1;
	
	public static final float PI = (float)Math.PI;
	public static final float halfPI = PI / 2f;
	
	public ModelLinkingBook() {
		coverLeft = new ModelRenderer(this).setTextureOffset(0, 0).addBox(-6F, -3F, 0.0F, 6, 10, 0);
		coverRight = new ModelRenderer(this).setTextureOffset(16, 0).addBox(0.0F, -3F, 0.0F, 6, 10, 0);
		bookSpine = new ModelRenderer(this).setTextureOffset(12, 0).addBox(-2F, -3F, 0.0F, 2, 10, 0);
		pagesLeft = new ModelRenderer(this).setTextureOffset(0, 10).addBox(0.0F, -3F, -1F, 6, 10, 1);
		pagesRight = new ModelRenderer(this).setTextureOffset(14, 10).addBox(0.0F, -3F, 0F, 6, 10, 1);
		
		pagesLeftTransparent = new ModelRenderer(this).setTextureOffset(0, 21).addBox(0.0F, -3F, -1F, 6, 10, 1);
		pagesRightTransparent = new ModelRenderer(this).setTextureOffset(14, 21).addBox(0.0F, -3F, 0F, 6, 10, 1);
		
		coverLeft.rotationPointX = -1;
		coverRight.rotationPointX = 1;
		
		pagesLeft.rotateAngleY = halfPI;
		pagesRight.rotateAngleY = halfPI;
		pagesLeftTransparent.rotateAngleY = halfPI;
		pagesRightTransparent.rotateAngleY = halfPI;
		
		bookSpine.rotationPointX = 1;
		
		// Set the origin to the bottom of the spine (also need to translate back by 10/16f when rendering):
		coverLeft.rotationPointY -= 10;
		coverRight.rotationPointY -= 10;
		bookSpine.rotationPointY -= 10;
		pagesLeft.rotationPointY -= 10;
		pagesRight.rotationPointY -= 10;
		pagesLeftTransparent.rotationPointY -= 10;
		pagesRightTransparent.rotationPointY -= 10;
		coverLeft.rotationPointX -= 1;
		coverRight.rotationPointX -= 1;
		bookSpine.rotationPointX -= 1;
		pagesLeft.rotationPointX -= 1;
		pagesRight.rotationPointX -= 1;
		pagesLeftTransparent.rotationPointX -= 1;
		pagesRightTransparent.rotationPointX -= 1;
		
	}
	
	private float count = 0;
	
	public void render(float opening, Color color, String bookName, FontRenderer fontrenderer, float f5) {
		GL11.glPushMatrix();
		
		float angleOpen = halfPI * opening;
		
		if (opening != lastOpening) {
			setRotationAngles(angleOpen);
			lastOpening = opening;
		}
		// Lateral translation of the book when opening + replace the origin at 10/16f:
		GL11.glTranslatef(((MathHelper.cos(angleOpen) + 1f) * -4f + 5) / 16f, 10 / 16f, 0);
		// Make the right cover stay in place:
		GL11.glRotatef(90 * (opening - 1), 0, 1, 0);
		
		coverLeft.render(f5);
		coverRight.render(f5);
		bookSpine.render(f5);
		
		GL11.glColor3ub((byte)color.getRed(), (byte)color.getGreen(), (byte)color.getBlue());
		if (opening <= 0.01f || opening >= 0.99f) {
			pagesLeftTransparent.rotateAngleY = pagesLeft.rotateAngleY;
			pagesLeftTransparent.rotationPointZ = pagesLeft.rotationPointZ;
			pagesRightTransparent.rotateAngleY = pagesRight.rotateAngleY;
			pagesRightTransparent.rotationPointZ = pagesRight.rotationPointZ;
			pagesLeftTransparent.render(f5);
			pagesRightTransparent.render(f5);
		}
		else {
			pagesLeft.render(f5);
			pagesRight.render(f5);
		}
		GL11.glColor4f(1, 1, 1, 1);
		
		if (opening > 0f) {
			// Follow the slight up and down movement of the pages:
			float d = MathHelper.sin(angleOpen);
			
			// Move to over the book:
			GL11.glTranslatef(0, -1f, 0);
			// Scale to 1/16 of normal block size:
			GL11.glScalef(1 / 16f, 1 / 16f, 1 / 16f);
			
			// Move to the center of the book, following the pages up and down movements:
			GL11.glTranslatef(-1f, 4f, -d - 0.01f);
			// Follow left pages rotation:
			GL11.glRotatef(90 * (opening - 1), 0, 1f, 0);
			// Set at the right place on the left page:
			GL11.glTranslatef(-3f, 0.5f, 0);
			// Set the size of the font:
			GL11.glScalef(1 / 16f, 1 / 16f, 1 / 16f);
			fontrenderer.drawString(bookName, -fontrenderer.getStringWidth(bookName) / 2, 0, 0x000000);
		}
		
		GL11.glPopMatrix();
	}
	
	public void setRotationAngles(float angleOpen) {
		float halfPI_Opening = angleOpen / 2f;
		
		coverLeft.rotateAngleY = -halfPI + angleOpen;
		coverRight.rotateAngleY = -coverLeft.rotateAngleY;
		
		pagesLeft.rotateAngleY = halfPI + angleOpen;
		pagesRight.rotateAngleY = halfPI - angleOpen;
		
		pagesLeft.rotationPointZ = -MathHelper.sin(angleOpen) - 0.001f;
		pagesRight.rotationPointZ = pagesLeft.rotationPointZ;
	}
	
	// This method is for debugging only:
	public static final void drawOrigin() {
		boolean GL_TEXTURE_2D_enabled = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
		boolean GL_DEPTH_TEST_enabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
		boolean GL_BLEND_enabled = GL11.glIsEnabled(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
		
		// GL11.glPointSize(1);
		GL11.glColor3f(0, 1, 1);
		
		// Draw a point at the origine:
		GL11.glBegin(GL11.GL_POINTS);
		GL11.glVertex3f(0, 0, 0);
		GL11.glEnd();
		
		// Draw 1 line in each directions;
		GL11.glBegin(GL11.GL_LINES);
		GL11.glColor3f(1, 0, 0);
		GL11.glVertex3f(0, 0, 0);
		GL11.glVertex3f(1, 0, 0);
		GL11.glColor3f(0, 1, 0);
		GL11.glVertex3f(0, 0, 0);
		GL11.glVertex3f(0, 1, 0);
		GL11.glColor3f(0, 0, 1);
		GL11.glVertex3f(0, 0, 0);
		GL11.glVertex3f(0, 0, 1);
		GL11.glEnd();
		
		if (GL_TEXTURE_2D_enabled) {
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}
		if (GL_DEPTH_TEST_enabled) {
			GL11.glEnable(GL11.GL_DEPTH_TEST);
		}
		if (GL_BLEND_enabled) {
			GL11.glEnable(GL11.GL_BLEND);
		}
	}
}
