
package MCP.mod_mystlinkingbook;

import java.io.File;
import java.util.Properties;

import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import MCP.ApiController;
import MCP.ApiCraftMgr;
import MCP.Mod;

public class mod_mystlinkingbook extends Mod {
	
	public ApiController	    ctrl;
	
	public File	                worldFolder;
	
	Properties	                datasProps	       = new Properties();
	final String	            datasVersion	   = "0.1";
	final int	                datasVersion_major	= Integer.parseInt(this.datasVersion.substring(0, this.datasVersion.indexOf('.')));
	
	// Contains methods to interact with the datas of the Linking Books:
	public LinkingBook	        linkingBook	       = new LinkingBook();
	
	public BlockLinkingBook	    blockLinkingBook;
	public ItemBlockLinkingBook	itemBlockLinkingBook;
	
	public mod_mystlinkingbook(ApiController ctrl) {
		super(ctrl);
		this.ctrl = ctrl;
	}
	
	@Override
	public String getMinecraftVersion() {
		return "1.0.0";
	}
	
	@Override
	public String getModAuthor() {
		return "ziliss";
	}
	
	@Override
	public String getModDescription() {
		String desc = "Adds Linking Books, Myst style !";
		return desc;
	}
	
	@Override
	public String getModName() {
		return "Myst Linking Book mod";
	}
	
	@Override
	public String getModSystemVersion() {
		return "1.0.0.1";
	}
	
	@Override
	public String getModVersion() {
		return "0.1a";
	}
	
	@Override
	public void onStartWorld(String folder, String name, long seed) {
		// this.isWorking = false;
		this.worldFolder = new File(folder);
		// this.datasProps.clear();
		// this.linkingBooks.clear();
		
		// this.nextAvailableIndex = 1; // Because metadata == 0 when there is no metadata, so metadata == 1 -> index == 0
		
		/*
		 * this.isWorking = this.loadProperties();
		 * if(this.isWorking) {
		 * while(this.linkingBooks.containsKey(this.nextAvailableIndex)) {
		 * this.nextAvailableIndex++;
		 * }
		 * }
		 * else {
		 * this.ctrl.printc("MystLinkingBook: Not activated for this world (Datas loading errors).");
		 * }
		 */
	}
	
	@Override
	public void onRegisterBlocksAndItems() {
		int linkingBookTexture = api().registerBlockTexture(this.imageName(this.getClass(), "gfx/mcp.png"), 0);
		// int linkingBookIcon = api().registerItemIcon(this.imageName(this.getClass(), "gfx/mcp.png"), 0);// This gives us a custom texture for the item. The "1" tells the Mod System to grab the second 16x16 pixel square(going from left to right) in the .png.
		
		this.itemBlockLinkingBook = new ItemBlockLinkingBook(api(), linkingBookTexture, this);
		this.blockLinkingBook = (BlockLinkingBook) this.itemBlockLinkingBook.block;
		
		this.ctrl.registerTileEntity(TileEntityLinkingBook.class, "LinkingBook");
	}
	
	@Override
	public void onRegisterRecipes(ApiCraftMgr craftMgr) {
		craftMgr.addRecipe(new ItemStack(this.itemBlockLinkingBook, 1), new Object[] { "#", "#", Character.valueOf('#'), Item.paper });
	}
	
	// The following commented code is old code that might be useful later. Keeping it for now.
	/*
	 * public int addLinkingBook(LinkingBook linkingBook) {
	 * int index = this.nextAvailableIndex;
	 * this.linkingBooks.put(index, linkingBook);
	 * do {
	 * this.nextAvailableIndex++;
	 * } while(this.linkingBooks.containsKey(this.nextAvailableIndex));
	 * this.datasNeedSaving = true;
	 * return index;
	 * }
	 * public LinkingBook getLinkingBook(int index) {
	 * return this.linkingBooks.get(index);
	 * }
	 * public void removeLinkingBook(int index) {
	 * this.linkingBooks.remove(index);
	 * this.datasNeedSaving = true;
	 * }
	 * boolean loadProperties() {
	 * File dataFile = new File(this.worldFolder, "mystlinkingbookdata.dat");
	 * try {
	 * if(dataFile.canRead()) {
	 * synchronized(this.datasProps) {
	 * this.datasProps.clear();
	 * this.datasProps.load(new FileInputStream(dataFile));
	 * float readDatasVersion = Float.parseFloat(this.datasProps.getProperty("Datas version"));
	 * int readDatasVersion_major = (int) readDatasVersion;
	 * if(readDatasVersion_major != this.datasVersion_major) {
	 * Mod.log("MystLinkingBook: datas Properties file version is incompatible with current version of MystLinkingBook");
	 * return false;
	 * }
	 * Scanner allIds = new Scanner(this.datasProps.getProperty("allIds", "").replace(',', ' '));
	 * while(allIds.hasNextInt()) {
	 * int index = allIds.nextInt();
	 * String indexString = Integer.toString(index);
	 * try {
	 * String prop = this.datasProps.getProperty(indexString + ".isDestinationSet");
	 * if(prop == null) {
	 * throw new NullPointerException("MystLinkingBook: Could not load datas for linkingBook " + indexString);
	 * }
	 * else {
	 * boolean isDestinationSet = Boolean.parseBoolean(prop);
	 * if(isDestinationSet) {
	 * double destinationPosX = Double.parseDouble(this.datasProps.getProperty(indexString + ".destinationPosX"));
	 * double destinationPosY = Double.parseDouble(this.datasProps.getProperty(indexString + ".destinationPosY"));
	 * double destinationPosZ = Double.parseDouble(this.datasProps.getProperty(indexString + ".destinationPosZ"));
	 * float destinationRotationYaw = Float.parseFloat(this.datasProps.getProperty(indexString + ".destinationRotationYaw"));
	 * float destinationRotationPitch = Float.parseFloat(this.datasProps.getProperty(indexString + ".destinationRotationPitch"));
	 * LinkingBook linkingBook = new LinkingBook(destinationPosX, destinationPosY, destinationPosZ, destinationRotationYaw, destinationRotationPitch);
	 * this.linkingBooks.put(index, linkingBook);
	 * }
	 * }
	 * }
	 * catch(NullPointerException e) {
	 * e.printStackTrace();
	 * }
	 * catch(NumberFormatException e) {
	 * Mod.log("MystLinkingBook: Could not load datas for linkingBook " + index);
	 * e.printStackTrace();
	 * }
	 * }
	 * return true;
	 * }
	 * }
	 * else if(!dataFile.exists()) {
	 * Mod.log("MystLinkingBook: No previous datas Properties file");
	 * return true;
	 * }
	 * else {
	 * throw new IOException("MystLinkingBook: Unable to handle Properties file!");
	 * }
	 * }
	 * catch(FileNotFoundException e) {
	 * e.printStackTrace();
	 * }
	 * catch(IOException e) {
	 * e.printStackTrace();
	 * }
	 * return false;
	 * }
	 * boolean saveDatas() {
	 * if(!this.isWorking) {
	 * return false;
	 * }
	 * if(!this.datasNeedSaving) {
	 * return true;
	 * }
	 * File dataFile = new File(this.worldFolder, "mystlinkingbookdata.dat");
	 * try {
	 * if(!dataFile.exists()) {
	 * dataFile.createNewFile();
	 * }
	 * if(dataFile.canWrite()) {
	 * synchronized(this.datasProps) {
	 * // this.props.load(new FileInputStream(dataFile));
	 * // Mod.log("Saving Properties.");
	 * if(!this.datasNeedSaving) {
	 * return true;
	 * }
	 * this.datasProps.clear();
	 * this.datasProps.setProperty("MystLinkingBook version", this.getModVersion());
	 * this.datasProps.setProperty("Datas version", this.datasVersion);
	 * StringBuilder allIds = new StringBuilder(this.linkingBooks.size() * 4);
	 * for(Integer index : this.linkingBooks.keySet()) {
	 * allIds.append(index).append(", ");
	 * LinkingBook linkingBook = this.linkingBooks.get(index);
	 * String indexString = index.toString();
	 * this.datasProps.setProperty(indexString + ".isDestinationSet", Boolean.toString(linkingBook.isDestinationSet));
	 * if(linkingBook.isDestinationSet) {
	 * this.datasProps.setProperty(indexString + ".destinationPosX", Double.toString(linkingBook.destinationPosX));
	 * this.datasProps.setProperty(indexString + ".destinationPosY", Double.toString(linkingBook.destinationPosY));
	 * this.datasProps.setProperty(indexString + ".destinationPosZ", Double.toString(linkingBook.destinationPosZ));
	 * this.datasProps.setProperty(indexString + ".destinationRotationYaw", Float.toString(linkingBook.destinationRotationYaw));
	 * this.datasProps.setProperty(indexString + ".destinationRotationPitch", Float.toString(linkingBook.destinationRotationPitch));
	 * }
	 * }
	 * allIds.delete(allIds.length() - 2, allIds.length());
	 * this.datasProps.setProperty("allIds", allIds.toString());
	 * this.datasProps.store(new FileOutputStream(dataFile), "MystLinkingBook Datas Properties");
	 * this.datasNeedSaving = false;
	 * return true;
	 * }
	 * }
	 * else {
	 * throw new IOException("MystLinkingBook: Unable to handle Properties file!");
	 * }
	 * }
	 * catch(FileNotFoundException e) {
	 * e.printStackTrace();
	 * }
	 * catch(IOException e) {
	 * e.printStackTrace();
	 * }
	 * return false;
	 * }
	 */
	
}
