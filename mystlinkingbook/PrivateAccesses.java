package net.minecraft.src.mystlinkingbook;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.src.BlockFire;
import net.minecraft.src.EntityRenderer;
import net.minecraft.src.GameSettings;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiTextField;
import net.minecraft.src.ISaveFormat;
import net.minecraft.src.InventoryBasic;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.RenderEngine;
import net.minecraft.src.SoundManager;
import net.minecraft.src.SoundPool;
import net.minecraft.src.Timer;
import net.minecraft.src.World;
import paulscode.sound.SoundSystem;

/**
 * Utility class used to access the private members (fields and methods) of the Minecraft classes.<br>
 * <br>
 * Centralizes the access to private members, allowing more control and checks at runtime. For example, it will tell you which members names are not correct (useful when you update your mod to a new Minecraft version). It also gives you the MCPBot commands you need to run to find the correct names.<br>
 * <br>
 * Keeps all members in cache when initialized for very fast accesses.<br>
 * <br>
 * Uses the MCP name or the obfuscated name depending on whether it is run from Eclipse or not.<br>
 * <br>
 * Easy syntax to access the private fields from your mod:<br>
 * Get a value: {@code PrivateAccesses.ClassName_fieldName.getFrom(instance)}<br>
 * Set a value: {@code PrivateAccesses.ClassName_fieldName.setTo(instance, value)}<br>
 * <br>
 * Easy syntax to invoke the private methods from your mod:<br>
 * Invoke: {@code PrivateAccesses.ClassName_methodName.invokeFrom(instance, arg1, arg2, ..., argN)}<br>
 * <br>
 * All casts are managed and checked internally. You don't need to cast anymore.<br>
 * <br>
 * Call the static method {@link PrivateAccesses#ensureInitialized()} to ensure the members checks are run when your mod is loaded.<br>
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
	
	private static boolean hasMembersNotFound = false;
	
	private static final StringBuilder errors = new StringBuilder();
	
	// Start of the private members definitions (set your own private members here):
	
	public static PrivateField<BlockFire, int[]> BlockFire_chanceToEncourageFire = new PrivateField<BlockFire, int[]>(BlockFire.class, "BlockFire", "a", "chanceToEncourageFire", int[].class);
	public static PrivateField<BlockFire, int[]> BlockFire_abilityToCatchFire = new PrivateField<BlockFire, int[]>(BlockFire.class, "BlockFire", "b", "abilityToCatchFire", int[].class);
	public static PrivateField<Minecraft, ISaveFormat> Minecraft_saveLoader = new PrivateField<Minecraft, ISaveFormat>(Minecraft.class, "Minecraft", "ad", "saveLoader", ISaveFormat.class);
	
	public static PrivateField<SoundPool, Map> SoundPool_nameToSoundPoolEntriesMapping = new PrivateField<SoundPool, Map>(SoundPool.class, "SoundPool", "d", "nameToSoundPoolEntriesMapping", Map.class);
	public static PrivateField<SoundPool, List> SoundPool_allSoundPoolEntries = new PrivateField<SoundPool, List>(SoundPool.class, "SoundPool", "e", "allSoundPoolEntries", List.class);
	
	public static PrivateField<SoundManager, Boolean> SoundManager_loaded = new PrivateField<SoundManager, Boolean>(SoundManager.class, "SoundManager", "g", "loaded", boolean.class);
	public static PrivateField<SoundManager, GameSettings> SoundManager_options = new PrivateField<SoundManager, GameSettings>(SoundManager.class, "SoundManager", "f", "options", GameSettings.class);
	public static PrivateField<SoundManager, SoundPool> SoundManager_soundPoolSounds = new PrivateField<SoundManager, SoundPool>(SoundManager.class, "SoundManager", "b", "soundPoolSounds", SoundPool.class);
	public static PrivateField<SoundManager, Integer> SoundManager_latestSoundID = new PrivateField<SoundManager, Integer>(SoundManager.class, "SoundManager", "e", "latestSoundID", int.class);
	public static PrivateField<SoundManager, SoundSystem> SoundManager_sndSystem = new PrivateField<SoundManager, SoundSystem>(SoundManager.class, "SoundManager", "a", "sndSystem", SoundSystem.class);
	
	public static PrivateField<GuiScreen, GuiButton> GuiScreen_selectedButton = new PrivateField<GuiScreen, GuiButton>(GuiScreen.class, "GuiScreen", "a", "selectedButton", GuiButton.class);
	
	public static PrivateField<InventoryBasic, ItemStack[]> InventoryBasic_inventoryContents = new PrivateField<InventoryBasic, ItemStack[]>(InventoryBasic.class, "InventoryBasic", "c", "inventoryContents", ItemStack[].class);
	
	public static PrivateField<Item, Integer> Item_maxStackSize = new PrivateField<Item, Integer>(Item.class, "Item", "bR", "maxStackSize", int.class);
	
	public static PrivateField<Minecraft, Timer> Minecraft_timer = new PrivateField<Minecraft, Timer>(Minecraft.class, "Minecraft", "X", "timer", Timer.class);
	
	public static PrivateField<GuiTextField, Boolean> GuiTextField_isEnabled = new PrivateField<GuiTextField, Boolean>(GuiTextField.class, "GuiTextField", "m", "isEnabled", boolean.class);
	
	public static PrivateField<RenderEngine, List> RenderEngine_textureList = new PrivateField<RenderEngine, List>(RenderEngine.class, "RenderEngine", "i", "textureList", List.class);
	
	public static PrivateMethod<EntityRenderer, Void> EntityRenderer_setupCameraTransform = new PrivateMethod<EntityRenderer, Void>(EntityRenderer.class, "EntityRenderer", "a", "setupCameraTransform", new Class[] { float.class, int.class }, Void.TYPE);
	
	// End of the private members definitions.
	
	static {
		if (hasMembersNotFound) {
			//@formatter:off
			String msg = "\n############# NoSuchMember: wrong private field names ! #############\n"
					   + "MCPBot commands (http://mcp.ocean-labs.de/index.php/MCPBot):\n"
					   + errors.toString()
					   + "#####################################################################\n\n";
			//@formatter:on
			System.err.print(msg);
		}
	}
	
	public static abstract class PrivateMember<C> {
		protected final Class<C> instanceClass;
		protected final String instanceClassName;
		protected final String obfName;
		protected final String mcpName;
		
		public PrivateMember(Class<C> instanceClass, String instanceClassName, String obfName, String mcpName) {
			this.instanceClass = instanceClass;
			this.instanceClassName = instanceClassName;
			this.obfName = obfName;
			this.mcpName = mcpName;
			
			// Tests if you made a mistake in the class name (It is not executed when run in normal Minecraft, only when run with MCP/Eclipse):
			if (!isObfuscated && !instanceClass.getSimpleName().equals(instanceClassName)) throw new IllegalArgumentException("Wrong class name: " + instanceClassName);
			// Intercept unknown obfuscated names:
			if (obfName.isEmpty()) {
				hasMembersNotFound = true;
				errors.append("gcf ").append(instanceClassName).append('.').append(mcpName).append('\n');
			}
		}
	}
	
	public static class PrivateField<C, T> extends PrivateMember<C> {
		protected final Class type;
		public final Field field;
		
		public PrivateField(Class<C> instanceClass, String instanceClassName, String obfName, String mcpName, Class<T> type) {
			super(instanceClass, instanceClassName, obfName, mcpName);
			this.type = type;
			
			// Check if the parameters are correct. Otherwise it gives you the MCPBot command that you can use to find the correct names.
			Field field = null;
			try {
				// Test if the field exists:
				field = instanceClass.getDeclaredField(isObfuscated ? obfName : mcpName);
				// Test if the field type is correct:
				if (!field.getType().equals(type)) throw new ClassCastException(field.getType() + " cannot be cast to " + type);
				field.setAccessible(true);
			}
			catch (SecurityException e) {
				e.printStackTrace();
			}
			catch (NoSuchFieldException e) {
				hasMembersNotFound = true;
				errors.append("gcf ").append(instanceClassName).append('.').append(mcpName).append('\n');
				// e.printStackTrace();
			}
			catch (ClassCastException e) {
				hasMembersNotFound = true;
				errors.append("gcf ").append(instanceClassName).append('.').append(mcpName).append('\n');
				// e.printStackTrace();
			}
			this.field = field;
		}
		
		public <I extends C> T getFrom(I instance) {
			try {
				return (T)field.get(instance);
			}
			catch (Exception e) {
				// Because of the checks in the constructor, this should never happen !
				// But just in case:
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		
		public <I extends C> void setTo(I instance, T value) {
			try {
				field.set(instance, value);
				// ModLoader.setPrivateValue(instanceClass, instance, isObfuscated ? obfName : mcpName, value);
			}
			catch (Exception e) {
				// Because of the checks in the constructor, this should never happen !
				// But just in case:
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}
	
	public static class PrivateMethod<C, R> extends PrivateMember<C> {
		protected final Class[] parameterTypes;
		protected final Class<R> returnType;
		public final Method method;
		
		public PrivateMethod(Class<C> instanceClass, String instanceClassName, String obfName, String mcpName, Class[] parameterTypes, Class returnType) {
			super(instanceClass, instanceClassName, obfName, mcpName);
			this.parameterTypes = parameterTypes;
			this.returnType = returnType;
			
			// Check if the parameters are correct. Otherwise it gives you the MCPBot command that you can use to find the correct names.
			Method method = null;
			try {
				// Test if the field exists:
				method = instanceClass.getDeclaredMethod(isObfuscated ? obfName : mcpName, parameterTypes);
				// Test if the return type is correct:
				if (!method.getReturnType().equals(returnType)) throw new ClassCastException(method.getReturnType() + " cannot be cast to " + returnType);
				method.setAccessible(true);
			}
			catch (SecurityException e) {
				e.printStackTrace();
			}
			catch (NoSuchMethodException e) {
				hasMembersNotFound = true;
				errors.append("gcm ").append(instanceClassName).append('.').append(mcpName).append('\n');
				// e.printStackTrace();
			}
			catch (ClassCastException e) {
				hasMembersNotFound = true;
				errors.append("gcm ").append(instanceClassName).append('.').append(mcpName).append('\n');
				// e.printStackTrace();
			}
			this.method = method;
		}
		
		public <I extends C> R invokeFrom(I instance, Object... args) {
			try {
				return (R)method.invoke(instance, args);
			}
			catch (Exception e) {
				// Because of the checks in the constructor, this should never happen !
				// But just in case:
				e.printStackTrace();
				throw new RuntimeException(e);
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
	
	public static final boolean hasMembersNotFound() {
		return hasMembersNotFound;
	}
}