package net.minecraft.src.mystlinkingbook;

import java.awt.Color;
import java.util.List;

import net.minecraft.src.EntitySheep;
import net.minecraft.src.Item;
import net.minecraft.src.ItemDye;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.mystlinkingbook.ResourcesManager.SpriteResource;

/**
 * Represents a Linking Book's page as an {@code Item} (ie. either dropped in the world or in the inventory).<br>
 * <br>
 * There is only 1 instance of this class.
 * 
 * @author ziliss
 * @see EntitySheep
 * @see ItemDye
 * @since 0.6b
 */
public class ItemPage extends Item {
	
	// A pair of: internal/displayed names for colors. (The internal names are the same as the ones in ItemDye):
	//@formatter:off
	public static String[][] colorNames = {
		{ "white", "White" },
		{ "orange", "Orange" },
		{ "magenta", "Magenta" },
		{ "lightBlue", "Light Blue" },
		{ "yellow", "Yellow" },
		{ "lime", "Lime" },
		{ "pink", "Pink" },
		{ "gray", "Gray" },
		{ "silver", "Light Grey" },
		{ "cyan", "Cyan" },
		{ "purple", "Purple" },
		{ "blue", "Blue" },
		{ "brown", "Brown" },
		{ "green", "Green" },
		{ "red", "Red" },
		{ "black", "Black" } };
	
	public static final Color[] colorTable = {
		// The base colors used to color the pages (Between parenthesis are the original colors from EntitySheep):
		new Color( 1.0F, 1.0F, 1.0F ),	// White ( 1.0F, 1.0F, 1.0F )
		new Color( 0xec8841 ),	// Orange ( 0.95F, 0.7F, 0.2F )
		new Color( 220, 104, 205 ),	// Magenta ( 0.9F, 0.5F, 0.85F )
		new Color( 0x88a6ec ),	// LightBlue ( 0.6F, 0.7F, 0.95F )
		new Color( 0xffff4f ),	// Yellow ( 0.9F, 0.9F, 0.2F )
		new Color( 0x3db92f ),	// Lime ( 0.5F, 0.8F, 0.1F )
		new Color( 0xf37877 ),	// Pink ( 0.95F, 0.7F, 0.8F )
		new Color( 0x4c4c4c ),	// Gray ( 0.3F, 0.3F, 0.3F )
		new Color( 0x808080 ),	// LightGray ( 0.6F, 0.6F, 0.6F )
		new Color( 0x4590b0 ),	// Cyan ( 0.3F, 0.6F, 0.7F )
		new Color( 0x723abc ),	// Purple ( 0.7F, 0.4F, 0.9F )
		new Color( 0x414baf ),	// Blue ( 0.2F, 0.4F, 0.8F )
		new Color( 0x755c44 ),	// Brown ( 0.5F, 0.4F, 0.3F )
		new Color( 0x425b1e ),	// Green ( 0.4F, 0.5F, 0.2F )
		new Color( 0xcc4d4d ),	// Red ( 0.8F, 0.3F, 0.3F )
		new Color( 0x262626 ) };// Black ( 0.1F, 0.1F, 0.1F )
	//@formatter:on
	
	// The brighter colors are used when blending the color with a texture that is not fully white.
	// This way it keeps approximatively the same visual color.
	public static final Color[] brighterColorTable = new Color[16];
	public static final int[] colorInts = new int[16];
	public static final int[] brighterColorInts = new int[16];
	
	static {
		// Initialize all the arrays:
		for (int i = 0; i < colorTable.length; i++) {
			brighterColorTable[i] = getBrighterColor(colorTable[i], 1.5f);
			colorInts[i] = colorTable[i].getRGB();
			brighterColorInts[i] = brighterColorTable[i].getRGB();
		}
	}
	
	public SpriteResource pageSprite;
	
	public ItemPage(int itemID, SpriteResource pageTexture) {
		super(itemID);
		
		this.pageSprite = pageTexture;
		setIconIndex(pageTexture.getSpriteId());
		
		setItemName("linkingBookPage");
		setHasSubtypes(true); // Prevents stacking items of different color in the same inventory slot.
		setMaxDamage(0);
	}
	
	@Override
	public int getColorFromDamage(int i, int j) {
		return brighterColorInts[i];
	}
	
	@Override
	public String getItemNameIS(ItemStack itemstack) {
		return super.getItemName() + "." + colorNames[itemstack.getItemDamage()][0];
	}
	
	// Here I redefine getLocalItemName and getItemDisplayName since the localization system doesn't seem to work well.
	
	@Override
	public String getLocalItemName(ItemStack itemstack) {
		return colorNames[itemstack.getItemDamage()][1] + " page";
	}
	
	@Override
	public String getItemDisplayName(ItemStack itemstack) {
		return getLocalItemName(itemstack);
	}
	
	/**
	 * Adds the name of the linking book to the tooltip displayed on mouse hover in the inventory.
	 */
	@Override
	public void addInformation(ItemStack itemstack, List list) {
		String name = getLinkingBookName(itemstack);
		if (!name.isEmpty()) {
			list.add(name);
		}
	}
	
	public String getLinkingBookName(ItemStack itemstack) {
		if (!itemstack.hasTagCompound()) return "";
		return itemstack.getTagCompound().getString("name");
	}
	
	public int getLinkingBookRandomId(ItemStack itemstack) {
		if (!itemstack.hasTagCompound()) return 0;
		return itemstack.getTagCompound().getInteger("randId");
	}
	
	public ItemStack createPages(int nbPages, int colorCode, String name, int randId) {
		ItemStack itemstack = new ItemStack(this, nbPages, colorCode);
		NBTTagCompound nbttagcompound_page = new NBTTagCompound();
		nbttagcompound_page.setString("name", name);
		nbttagcompound_page.setInteger("randId", randId);
		itemstack.setTagCompound(nbttagcompound_page);
		return itemstack;
	}
	
	public static final Color getBrighterColor(Color color, float factor) {
		// I know 2 methods to brighten a color.
		// After some testing, the first method seems to give better results for the purpose of this mod.
		
		// Method 1: blend with white color:
		float[] rgb = color.getRGBColorComponents(null);
		for (int i = 0; i < 3; i++) {
			rgb[i] *= factor;
			if (rgb[i] > 1f) {
				// System.out.println("Too much: " + i);
				rgb[i] = 1f;
			}
		}
		return new Color(rgb[0], rgb[1], rgb[2]);// */
		
		// Method 2: change the brightness component of the color in the HSB color space:
		/*float[] hsbVals = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
		hsbVals[2] *= factor;
		if (hsbVals[2] > 1f) {
			// System.out.println("Too much.");
			hsbVals[2] = 1f;
		}
		return Color.getHSBColor(hsbVals[0], hsbVals[1], hsbVals[2]);// */
	}
}
