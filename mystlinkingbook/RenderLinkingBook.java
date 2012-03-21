package net.minecraft.src.mystlinkingbook;

import net.minecraft.client.Minecraft;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntitySpecialRenderer;

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
	
	/**
	 * Reference to the mod instance.
	 */
	public Mod_MystLinkingBook mod_MLB;
	
	private ModelLinkingBook field_40450_a;
	
	public RenderLinkingBook(Mod_MystLinkingBook mod_MLB) {
		this.mod_MLB = mod_MLB;
		field_40450_a = new ModelLinkingBook(mod_MLB.itm);
	}
	
	@Override
	public void renderTileEntityAt(TileEntity tileentity, double d, double d1, double d2, float f) {
		TileEntityLinkingBook tileEntityLinkingBook = (TileEntityLinkingBook)tileentity;
		String bookName = mod_MLB.linkingBook.getName(tileEntityLinkingBook.nbttagcompound_linkingBook);
		
		GL11.glPushMatrix();
		
		float bookSpread = tileEntityLinkingBook.bookSpreadPrev + (tileEntityLinkingBook.bookSpread - tileEntityLinkingBook.bookSpreadPrev) * f;
		float inclination = 10f; // Angle in degrees.
		float recul = 0f;
		inclination = bookSpread * 20;
		
		// Set the book position over the block:
		GL11.glTranslatef((float)d + 0.5F, (float)d1 + 1F, (float)d2 + 0.5F);
		
		// Facing the right direction (upward when closed):
		GL11.glRotatef(90, 1F, 0, 0);
		// Rotate in the direction of the block:
		int direction = tileEntityLinkingBook.getBlockMetadata() & 3;
		GL11.glRotatef(90 * direction, 0, 0, 1F);
		// Incline the book a little:
		GL11.glTranslatef(0, (+7 + recul) / 16f, 0 / 16f);
		GL11.glRotatef(inclination, 1F, 0, 0);
		GL11.glTranslatef(0, -7 / 16f, 0 / 16f);
		
		// bindTextureByName("/item/book.png");
		Minecraft mc = ModLoader.getMinecraftInstance();
		mc.renderEngine.bindTexture(mc.renderEngine.getTexture(Mod_MystLinkingBook.resourcesPath + "tempLinkingBook3D.png"));
		field_40450_a.render(bookSpread, tileEntityLinkingBook.color, bookName, tileEntityLinkingBook.linkingPanel, getFontRenderer());
		
		GL11.glPopMatrix();
	}
}
