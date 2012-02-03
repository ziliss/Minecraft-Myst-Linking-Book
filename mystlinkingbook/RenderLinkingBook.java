package net.minecraft.src;

import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;

/**
 * Renders the book on the linking book table.<br>
 * <br>
 * Inspired by {@code RenderEnchantmentTable}.
 * 
 * @author ziliss
 * @since 0.3a
 */
public class RenderLinkingBook extends TileEntitySpecialRenderer {
	
	private ModelLinkingBook field_40450_a = new ModelLinkingBook();
	
	@Override
	public void renderTileEntityAt(TileEntity tileentity, double d, double d1, double d2, float f) {
		TileEntityLinkingBook tileEntityLinkingBook = (TileEntityLinkingBook)tileentity;
		
		GL11.glPushMatrix();
		GL11.glTranslatef((float)d + 0.5F, (float)d1 + 1.1F, (float)d2 + 0.5F);
		
		float angle = 0;
		// Inspired by BlockRedstoneRepeater.randomDisplayTick:
		switch (tileEntityLinkingBook.getBlockMetadata() & 3) {
			case 0:
				angle = 90;
				break;
			case 1:
				angle = 180;
				break;
			case 2:
				angle = 270;
				break;
		// By default: case 3: angle = 0;
		}
		GL11.glRotatef(-angle, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(80F, 0.0F, 0.0F, 1.0F);
		// bindTextureByName("/item/book.png");
		Minecraft mc = ModLoader.getMinecraftInstance();
		mc.renderEngine.bindTexture(mc.renderEngine.getTexture("/mystlinkingbook/tempLinkingBook3D.png"));
		float f6 = tileEntityLinkingBook.field_40060_g + (tileEntityLinkingBook.field_40059_f - tileEntityLinkingBook.field_40060_g) * f;
		field_40450_a.render(null, -1, -1, -1, f6, 0.0F, 0.0625F);
		GL11.glPopMatrix();
	}
}
