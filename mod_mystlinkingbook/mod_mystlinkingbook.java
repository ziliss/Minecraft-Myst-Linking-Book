package net.minecraft.src;

import java.awt.image.BufferedImage;
import java.io.File;

import net.minecraft.client.Minecraft;

public class mod_mystlinkingbook extends BaseMod {
	
	// Contains methods to interact with the datas of the Linking Books:
	public LinkingBook linkingBook = new LinkingBook();
	
	public BlockLinkingBook blockLinkingBook = new BlockLinkingBook(233, 233, this);
	public ItemBlockLinkingBook itemBlockLinkingBook = new ItemBlockLinkingBook(233 - 256, this);
	
	public mod_mystlinkingbook() {
	}
	
	@Override
	public String getVersion() {
		return "0.4a";
	}
	
	/**
	 * Load ressources and register all necessary elements.
	 * 
	 * Called when the mod is loaded.
	 */
	@Override
	public void load() {
		blockLinkingBook.topTextureIndex = ModLoader.addOverride("/terrain.png", "/mystlinkingbook/blockLinkingBookSide.png");
		blockLinkingBook.sideTextureIndex = blockLinkingBook.topTextureIndex;
		blockLinkingBook.bottomTextureIndex = blockLinkingBook.topTextureIndex;
		// itemBlockLinkingBook.iconIndex = ModLoader.addOverride("/gui/items.png", "/mystlinkingbook/tempBook.png");
		
		// Execute the following private method:
		// Block.fire.setBurnRate(Block.bookShelf.blockID, 60, 100);
		int chanceToEncourageFire[];
		int abilityToCatchFire[];
		try {
			chanceToEncourageFire = (int[])ModLoader.getPrivateValue(BlockFire.class, Block.fire, "a");
			chanceToEncourageFire[blockLinkingBook.blockID] = 60;
			abilityToCatchFire = (int[])ModLoader.getPrivateValue(BlockFire.class, Block.fire, "b");
			abilityToCatchFire[blockLinkingBook.blockID] = 100;
		}
		catch (NoSuchFieldException e) {
			try {
				chanceToEncourageFire = (int[])ModLoader.getPrivateValue(BlockFire.class, Block.fire, "chanceToEncourageFire");
				chanceToEncourageFire[blockLinkingBook.blockID] = 60;
				abilityToCatchFire = (int[])ModLoader.getPrivateValue(BlockFire.class, Block.fire, "abilityToCatchFire");
				abilityToCatchFire[blockLinkingBook.blockID] = 100;
			}
			catch (Exception ex) {
				e.printStackTrace();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		Minecraft mc = ModLoader.getMinecraftInstance();
		BufferedImage img;
		try {
			img = ModLoader.loadImage(mc.renderEngine, "/mystlinkingbook/tempLinkGUI.png");
			mc.renderEngine.setupTexture(img, 3233);
			img = ModLoader.loadImage(mc.renderEngine, "/mystlinkingbook/tempPanel.png");
			mc.renderEngine.setupTexture(img, 3234);
			img = ModLoader.loadImage(mc.renderEngine, "/mystlinkingbook/tempWriteGUI.png");
			mc.renderEngine.setupTexture(img, 3235);
			img = ModLoader.loadImage(mc.renderEngine, "/mystlinkingbook/tempLookGUI.png");
			mc.renderEngine.setupTexture(img, 3236);
			img = ModLoader.loadImage(mc.renderEngine, "/mystlinkingbook/tempLinkingBook3D.png");
			mc.renderEngine.setupTexture(img, 3237);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		ModLoader.AddName(blockLinkingBook, "Linking Book");
		ModLoader.AddName(itemBlockLinkingBook, "Linking Book");
		
		ModLoader.RegisterTileEntity(TileEntityLinkingBook.class, "LinkingBook", new RenderLinkingBook());
		
		ModLoader.AddRecipe(new ItemStack(itemBlockLinkingBook, 1), new Object[] { "#", "#", Character.valueOf('#'), Item.paper });
		
		File resourcesFolder = new File(Minecraft.getMinecraftDir(), "resources/");
		String[] exts = new String[] { ".wav", ".ogg", ".mus" };
		File linkingsound;
		for (String ext : exts) {
			linkingsound = new File(resourcesFolder, "mod/mystlinkingbook/linkingsound" + ext);
			if (linkingsound.exists()) {
				mc.sndManager.addSound("mystlinkingbook/linkingsound" + ext, linkingsound);
				break;
			}
		}
	}
	// The following commented code is old code that might be useful later.
	// Keeping it for now.
	/*
	public int addLinkingBook(LinkingBook linkingBook) {
		int index = this.nextAvailableIndex;
		this.linkingBooks.put(index, linkingBook);
		do {
			this.nextAvailableIndex++;
		} while (this.linkingBooks.containsKey(this.nextAvailableIndex));
		this.datasNeedSaving = true;
		return index;
	}
	
	public LinkingBook getLinkingBook(int index) {
		return this.linkingBooks.get(index);
	}
	
	public void removeLinkingBook(int index) {
		this.linkingBooks.remove(index);
		this.datasNeedSaving = true;
	}
	
	boolean loadProperties() {
		File dataFile = new File(this.worldFolder, "mystlinkingbookdata.dat");
		try {
			if (dataFile.canRead()) {
				synchronized (this.datasProps) {
					this.datasProps.clear();
					this.datasProps.load(new FileInputStream(dataFile));
					float readDatasVersion = Float.parseFloat(this.datasProps.getProperty("Datas version"));
					int readDatasVersion_major = (int) readDatasVersion;
					if (readDatasVersion_major != this.datasVersion_major) {
						Mod.log("MystLinkingBook: datas Properties file version is incompatible with current version of MystLinkingBook");
						return false;
					}
					Scanner allIds = new Scanner(this.datasProps.getProperty("allIds", "").replace(',', ' '));
					while (allIds.hasNextInt()) {
						int index = allIds.nextInt();
						String indexString = Integer.toString(index);
						try {
							String prop = this.datasProps.getProperty(indexString + ".isDestinationSet");
							if (prop == null) throw new NullPointerException("MystLinkingBook: Could not load datas for linkingBook " + indexString);
							else {
								boolean isDestinationSet = Boolean.parseBoolean(prop);
								if (isDestinationSet) {
									double destinationPosX = Double.parseDouble(this.datasProps.getProperty(indexString + ".destinationPosX"));
									double destinationPosY = Double.parseDouble(this.datasProps.getProperty(indexString + ".destinationPosY"));
									double destinationPosZ = Double.parseDouble(this.datasProps.getProperty(indexString + ".destinationPosZ"));
									float destinationRotationYaw = Float.parseFloat(this.datasProps.getProperty(indexString + ".destinationRotationYaw"));
									float destinationRotationPitch = Float.parseFloat(this.datasProps.getProperty(indexString + ".destinationRotationPitch"));
									LinkingBook linkingBook = new LinkingBook(destinationPosX, destinationPosY, destinationPosZ, destinationRotationYaw, destinationRotationPitch);
									this.linkingBooks.put(index, linkingBook);
								}
							}
						}
						catch (NullPointerException e) {
							e.printStackTrace();
						}
						catch (NumberFormatException e) {
							Mod.log("MystLinkingBook: Could not load datas for linkingBook " + index);
							e.printStackTrace();
						}
					}
					return true;
				}
			}
			else if (!dataFile.exists()) {
				Mod.log("MystLinkingBook: No previous datas Properties file");
				return true;
			}
			else throw new IOException("MystLinkingBook: Unable to handle Properties file!");
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	boolean saveDatas() {
		if (!this.isWorking) return false;
		if (!this.datasNeedSaving) return true;
		File dataFile = new File(this.worldFolder, "mystlinkingbookdata.dat");
		try {
			if (!dataFile.exists()) {
				dataFile.createNewFile();
			}
			if (dataFile.canWrite()) {
				synchronized (this.datasProps) {
					// this.props.load(new FileInputStream(dataFile));
					// Mod.log("Saving Properties.");
					if (!this.datasNeedSaving) return true;
					this.datasProps.clear();
					this.datasProps.setProperty("MystLinkingBook version", this.getModVersion());
					this.datasProps.setProperty("Datas version", this.datasVersion);
					StringBuilder allIds = new StringBuilder(this.linkingBooks.size() * 4);
					for (Integer index : this.linkingBooks.keySet()) {
						allIds.append(index).append(", ");
						LinkingBook linkingBook = this.linkingBooks.get(index);
						String indexString = index.toString();
						this.datasProps.setProperty(indexString + ".isDestinationSet", Boolean.toString(linkingBook.isDestinationSet));
						if (linkingBook.isDestinationSet) {
							this.datasProps.setProperty(indexString + ".destinationPosX", Double.toString(linkingBook.destinationPosX));
							this.datasProps.setProperty(indexString + ".destinationPosY", Double.toString(linkingBook.destinationPosY));
							this.datasProps.setProperty(indexString + ".destinationPosZ", Double.toString(linkingBook.destinationPosZ));
							this.datasProps.setProperty(indexString + ".destinationRotationYaw", Float.toString(linkingBook.destinationRotationYaw));
							this.datasProps.setProperty(indexString + ".destinationRotationPitch", Float.toString(linkingBook.destinationRotationPitch));
						}
					}
					allIds.delete(allIds.length() - 2, allIds.length());
					this.datasProps.setProperty("allIds", allIds.toString());
					this.datasProps.store(new FileOutputStream(dataFile), "MystLinkingBook Datas Properties");
					this.datasNeedSaving = false;
					return true;
				}
			}
			else throw new IOException("MystLinkingBook: Unable to handle Properties file!");
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	*/
}
