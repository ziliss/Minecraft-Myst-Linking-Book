package net.minecraft.src.mystlinkingbook;

import java.lang.reflect.Field;

import net.minecraft.client.Minecraft;
import net.minecraft.src.BlockFire;
import net.minecraft.src.GameSettings;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.ISaveFormat;
import net.minecraft.src.InventoryBasic;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.SoundManager;
import net.minecraft.src.SoundPool;
import net.minecraft.src.World;
import net.minecraft.src.WorldInfo;
import paulscode.sound.SoundSystem;

/**
 * Utility class used to access the private fields of the Minecraft classes.<br>
 * <br>
 * Centralizes the access to private fields, allowing more control and checks at runtime. For example, it will tell you which field names are not correct (useful when you update your mod to a new Minecraft version). It also gives you the MCPBot commands you need to run to find the correct names.<br>
 * <br>
 * Automatically try the obfuscated name, then the MCP name in case you run from Eclipse.<br>
 * <br>
 * Easy syntax to access the private fields from your mod:<br>
 * Get a value: {@code PrivateAccesses.ClassName_fieldName.getFrom(instance)}<br>
 * Set a value: {@code PrivateAccesses.ClassName_fieldName.setTo(instance, value)}<br>
 * All casts are managed and checked internally. You don't need to cast anymore.<br>
 * <br>
 * Call the static method {@link PrivateAccesses#ensureInitialized()} to ensure the fields checks are run when your mod is loaded.<br>
 * <br>
 * (Needs Modloader to work.)
 * 
 * @author ziliss
 * @see ModLoader#getPrivateValue(Class, Object, String)
 * @see ModLoader#setPrivateValue(Class, Object, String, Object)
 * @since 0.5a (as a nested class of mod_mystlinkingbook)
 */
public class PrivateAccesses {
	
	/**
	 * Are we run with the obfuscated Minecraft classes ?
	 */
	public static final boolean isObfuscated = !World.class.getSimpleName().equals("World");
	static {
		if (!isObfuscated) {
			System.out.println("Class names are not obfuscated !");
		}
	}
	
	public static boolean hasFieldsNotFound = false;
	
	private static final StringBuilder errors = new StringBuilder();
	
	// Start of the private fields definitions (set your own private fields here):
	
	public static PrivateField<BlockFire, int[]> BlockFire_chanceToEncourageFire = new PrivateField<BlockFire, int[]>(BlockFire.class, "BlockFire", "a", "chanceToEncourageFire", int[].class);
	public static PrivateField<BlockFire, int[]> BlockFire_abilityToCatchFire = new PrivateField<BlockFire, int[]>(BlockFire.class, "BlockFire", "b", "abilityToCatchFire", int[].class);
	public static PrivateField<Minecraft, ISaveFormat> Minecraft_saveLoader = new PrivateField<Minecraft, ISaveFormat>(Minecraft.class, "Minecraft", "ad", "saveLoader", ISaveFormat.class);
	
	public static PrivateField<SoundManager, Boolean> SoundManager_loaded = new PrivateField<SoundManager, Boolean>(SoundManager.class, "SoundManager", "g", "loaded", boolean.class);
	public static PrivateField<SoundManager, GameSettings> SoundManager_options = new PrivateField<SoundManager, GameSettings>(SoundManager.class, "SoundManager", "f", "options", GameSettings.class);
	public static PrivateField<SoundManager, SoundPool> SoundManager_soundPoolSounds = new PrivateField<SoundManager, SoundPool>(SoundManager.class, "SoundManager", "b", "soundPoolSounds", SoundPool.class);
	public static PrivateField<SoundManager, Integer> SoundManager_latestSoundID = new PrivateField<SoundManager, Integer>(SoundManager.class, "SoundManager", "e", "latestSoundID", int.class);
	public static PrivateField<SoundManager, SoundSystem> SoundManager_sndSystem = new PrivateField<SoundManager, SoundSystem>(SoundManager.class, "SoundManager", "a", "sndSystem", SoundSystem.class);
	
	public static PrivateField<GuiScreen, GuiButton> GuiScreen_selectedButton = new PrivateField<GuiScreen, GuiButton>(GuiScreen.class, "GuiScreen", "a", "selectedButton", GuiButton.class);
	
	public static PrivateField<InventoryBasic, ItemStack[]> InventoryBasic_inventoryContents = new PrivateField<InventoryBasic, ItemStack[]>(InventoryBasic.class, "InventoryBasic", "c", "inventoryContents", ItemStack[].class);
	
	public static PrivateField<Item, Integer> Item_maxStackSize = new PrivateField<Item, Integer>(Item.class, "Item", "bR", "maxStackSize", int.class);
	
	public static PrivateField<World, WorldInfo> World_worldInfo = new PrivateField<World, WorldInfo>(World.class, "World", "x", "worldInfo", WorldInfo.class);
	
	// End of the private fields definitions.
	
	static {
		if (hasFieldsNotFound) {
			//@formatter:off
			String msg = "\n########## NoSuchFieldException: wrong private fields names ! ##########\n"
					   + "MCPBot commands (http://mcp.ocean-labs.de/index.php/MCPBot):\n"
					   + errors.toString()
					   + "#####################################################################\n\n";
			//@formatter:on
			System.err.print(msg);
		}
	}
	
	public static class PrivateField<C, V> {
		final Class<C> instanceClass;
		final String instanceClassName;
		final String obfName;
		final String mcpName;
		final Class type;
		
		public PrivateField(Class<C> instanceClass, String instanceClassName, String obfName, String mcpName, Class<V> type) {
			this.instanceClass = instanceClass;
			this.instanceClassName = instanceClassName;
			this.obfName = obfName;
			this.mcpName = mcpName;
			this.type = type;
			
			// Check if the parameters are correct. Otherwise it gives you the MCPBot command.
			// You can use the commands to find the correct names.
			try {
				// Tests if you made a mistake in the class name (It is not executed when run in normal Minecraft, only when run with MCP/Eclipse):
				if (!isObfuscated && !instanceClass.getSimpleName().equals(instanceClassName)) throw new IllegalArgumentException("Wrong class name: " + instanceClassName);
				// Test if the field exists:
				Field field = instanceClass.getDeclaredField(isObfuscated ? obfName : mcpName);
				// Test if the field type is correct:
				if (!field.getType().equals(type)) throw new ClassCastException(field.getType() + " cannot be cast to " + type);
			}
			catch (SecurityException e) {
				e.printStackTrace();
			}
			catch (NoSuchFieldException e) {
				hasFieldsNotFound = true;
				errors.append("gcf ").append(instanceClassName).append('.').append(mcpName).append('\n');
				// e.printStackTrace();
			}
			catch (ClassCastException e) {
				hasFieldsNotFound = true;
				errors.append("gcf ").append(instanceClassName).append('.').append(mcpName).append('\n');
				// e.printStackTrace();
			}
		}
		
		public <I extends C> V getFrom(I instance) {
			try {
				// First try with the obfuscated name:
				return (V)ModLoader.getPrivateValue(instanceClass, instance, obfName);
			}
			catch (NoSuchFieldException e) {
				try {
					// Otherwise use the MCP name (in case it is run from Eclipse):
					return (V)ModLoader.getPrivateValue(instanceClass, instance, mcpName);
				}
				catch (Exception ex) {
					// Because of the checks in the constructor, this should never happen !
					// But just in case:
					e.printStackTrace();
					throw new RuntimeException(ex);
				}
			}
		}
		
		public <I extends C> void setTo(I instance, V value) {
			try {
				// First try with the obfuscated name:
				ModLoader.setPrivateValue(instanceClass, instance, obfName, value);
			}
			catch (NoSuchFieldException e) {
				try {
					// Otherwise use the MCP name (in case it is run from Eclipse):
					ModLoader.setPrivateValue(instanceClass, instance, mcpName, value);
				}
				catch (Exception ex) {
					// Because of the checks in the constructor, this should never happen !
					// But just in case:
					e.printStackTrace();
					throw new RuntimeException(ex);
				}
			}
		}
	}
	
	/**
	 * Calling this method ensures that the class has been loaded and initialized by Java.<br>
	 * <br>
	 * The only benefit of doing that is that the fields tests are run when your mod is loaded, not when you first use a field. Thus finding the errors is easier, because you know where to look in the program's output.
	 */
	public static final void ensureInitialized() {
	}
}