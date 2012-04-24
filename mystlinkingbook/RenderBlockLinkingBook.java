package net.minecraft.src.mystlinkingbook;

import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntitySpecialRenderer;

import org.lwjgl.opengl.GL11;

/**
 * Renders the book over the linking book table.<br>
 * <br>
 * Inspired by {@code RenderEnchantmentTable}.
 * 
 * @author ziliss
 * @since 0.3a
 */
public class RenderBlockLinkingBook extends TileEntitySpecialRenderer {
	
	/** The 3D model of the book used by the renderer. */
	protected ModelLinkingBook modelLinkingBook = new ModelLinkingBook();
	
	@Override
	public void renderTileEntityAt(TileEntity tileentity, double d, double d1, double d2, float partialTick) {
		TileEntityLinkingBook tileEntityLinkingBook = (TileEntityLinkingBook)tileentity;
		LinkingBook linkingBook = tileEntityLinkingBook.linkingBook;
		
		GL11.glPushMatrix();
		
		// Update the bookSpread for the current partialTick:
		float bookSpread = tileEntityLinkingBook.bookSpreadPrev + (tileEntityLinkingBook.bookSpread - tileEntityLinkingBook.bookSpreadPrev) * partialTick;
		if (bookSpread < 0f) {
			bookSpread = 0f;
		}
		else if (bookSpread > 1f) {
			bookSpread = 1f;
		}
		linkingBook.notifyBookSpreadChanged(linkingBook.getBookSpreadState(), bookSpread);
		
		float inclination = bookSpread * 20; // Angle in degrees.
		float recoil = 0f;
		
		// Set the book position over the block:
		GL11.glTranslatef((float)d + 0.5F, (float)d1 + 1F, (float)d2 + 0.5F);
		
		// Facing the right direction (upward when closed):
		GL11.glRotatef(90, 1F, 0, 0);
		// Rotate in the direction of the block:
		int direction = tileEntityLinkingBook.getBlockMetadata() & 3;
		GL11.glRotatef(90 * direction, 0, 0, 1F);
		// Set the inclination and recoil of the book:
		GL11.glTranslatef(0, (+7 + recoil) / 16f, 0 / 16f);
		GL11.glRotatef(inclination, 1F, 0, 0);
		GL11.glTranslatef(0, -7 / 16f, 0 / 16f);
		
		modelLinkingBook.render(linkingBook, getFontRenderer());
		
		GL11.glPopMatrix();
	}
}
