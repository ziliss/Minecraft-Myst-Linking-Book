package net.minecraft.src.mystlinkingbook;

import net.minecraft.src.Entity;
import net.minecraft.src.MathHelper;
import net.minecraft.src.ModelBase;
import net.minecraft.src.ModelBook;
import net.minecraft.src.ModelRenderer;

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
	public ModelRenderer field_40330_a;
	public ModelRenderer field_40328_b;
	public ModelRenderer field_40329_c;
	public ModelRenderer field_40326_d;
	public ModelRenderer field_40325_g;
	
	public float lastF3 = -1;
	
	public static final float PI = (float)Math.PI;
	public static final float halfPI = PI / 2.0f;
	
	public ModelLinkingBook() {
		field_40330_a = new ModelRenderer(this).setTextureOffset(0, 0).addBox(-6F, -7F, 0.0F, 6, 10, 0);
		field_40328_b = new ModelRenderer(this).setTextureOffset(16, 0).addBox(0.0F, -7F, 0.0F, 6, 10, 0);
		field_40325_g = new ModelRenderer(this).setTextureOffset(12, 0).addBox(-2F, -7F, 0.0F, 2, 10, 0);
		field_40329_c = new ModelRenderer(this).setTextureOffset(0, 10).addBox(0.0F, -6F, -0.99F, 5, 8, 1);
		field_40326_d = new ModelRenderer(this).setTextureOffset(12, 10).addBox(0.0F, -6F, -0.01F, 5, 8, 1);
		
		field_40330_a.rotateAngleY = PI + halfPI;
		field_40329_c.rotateAngleY = halfPI;
		
		field_40329_c.rotationPointX = 1;
		field_40326_d.rotationPointX = 1;
	}
	
	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		if (f3 != lastF3) {
			lastF3 = f3;
			setRotationAngles(f, f1, f2, f3, f4, f5);
		}
		field_40330_a.render(f5);
		field_40328_b.render(f5);
		field_40325_g.render(f5);
		field_40329_c.render(f5);
		field_40326_d.render(f5);
	}
	
	@Override
	public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5) {
		float angleOpen = PI * f3;
		float halfPI_F3 = halfPI * f3;
		
		float openingTranslation = -MathHelper.sin(halfPI_F3) * 3 + 3;
		
		field_40330_a.rotationPointZ = -1 + openingTranslation; // gauche/droite
		
		field_40328_b.rotateAngleY = -angleOpen + halfPI; // rotation
		field_40328_b.rotationPointX = MathHelper.cos(halfPI_F3) * 2; // hauteur
		field_40328_b.rotationPointZ = MathHelper.sin(halfPI_F3) * 2 - 1 + openingTranslation; // gauche/droite
		
		field_40329_c.rotationPointZ = -MathHelper.cos(halfPI_F3) - 0.001f + openingTranslation; // gauche/droite
		field_40326_d.rotateAngleY = -angleOpen + halfPI; // rotation
		field_40326_d.rotationPointZ = field_40329_c.rotationPointZ - 0.001f; // gauche/droite
		
		field_40325_g.rotateAngleY = PI - (angleOpen - halfPI) / 2 - halfPI / 2; // rotation
		field_40325_g.rotationPointZ = -1 + openingTranslation; // gauche/droite
	}
}
