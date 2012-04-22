package net.minecraft.src.mystlinkingbook;

import java.awt.Color;
import java.awt.image.BufferedImage;

import javax.swing.event.EventListenerList;

import net.minecraft.src.Entity;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.mystlinkingbook.RessourcesManager.TextureRessource;

/**
 * 
 * @author ziliss
 * @since 0.9b
 */
public class LinkingBook {
	
	/**
	 * Reference to the mod instance.
	 */
	public Mod_MystLinkingBook mod_MLB;
	
	/**
	 * The datas for this Linking Book.
	 */
	public NBTTagCompound nbttagcompound;
	
	public EventListenerList listeners = new EventListenerList();
	
	public int bookX;
	public int bookY;
	public int bookZ;
	public int bookDim;
	
	protected AgesManagerListener agesManagerListener = new AgesManagerListener() {
		@Override
		public void updatePerformed(int dimension) {
			linksToDifferentAge = mod_MLB.linkingBookUtils.doLinkToDifferentAge(nbttagcompound, bookX, bookY, bookZ, bookDim);
			linkingPanel.updateState();
		}
	};
	
	protected boolean isWritten = false;
	protected String name;
	protected int nbPages;
	protected int maxPages;
	protected int colorCode;
	protected int color;
	protected Color colorObj;
	protected int brighterColor;
	protected Color brighterColorObj;
	
	/** Does the linking book need power to be able to link ? */
	protected boolean isUnstable;
	protected boolean isPowered = false;
	protected boolean stayOpen;
	protected String coverName;
	protected TextureRessource coverTexture;
	/** True if the linking book links to another Age. */
	protected boolean linksToDifferentAge;
	
	protected SpreadState spreadState;
	protected float bookSpread;
	
	public final LinkingPanel linkingPanel;
	
	public LinkingBook(NBTTagCompound nbttagcompound_linkingBook, TileEntity entity, boolean isPowered, float bookSpread, Mod_MystLinkingBook mod_MLB) {
		this(nbttagcompound_linkingBook, entity.xCoord, entity.yCoord, entity.zCoord, entity.worldObj.worldProvider.worldType, isPowered, mod_MLB);
	}
	
	public LinkingBook(NBTTagCompound nbttagcompound_linkingBook, int bookX, int bookY, int bookZ, int bookDim, boolean isPowered, Mod_MystLinkingBook mod_MLB) {
		this.nbttagcompound = nbttagcompound_linkingBook;
		this.mod_MLB = mod_MLB;
		
		this.bookX = bookX;
		this.bookY = bookY;
		this.bookZ = bookZ;
		this.bookDim = bookDim;
		
		name = mod_MLB.linkingBookUtils.getName(nbttagcompound);
		nbPages = mod_MLB.linkingBookUtils.getNbPages(nbttagcompound);
		maxPages = mod_MLB.linkingBookUtils.getMaxPages(nbttagcompound);
		colorCode = mod_MLB.linkingBookUtils.getColorCode(nbttagcompound);
		color = ItemPage.colorInts[colorCode];
		colorObj = ItemPage.colorTable[colorCode];
		brighterColor = ItemPage.brighterColorInts[colorCode];
		brighterColorObj = ItemPage.brighterColorTable[colorCode];
		isUnstable = mod_MLB.linkingBookUtils.isUnstable(nbttagcompound);
		stayOpen = mod_MLB.linkingBookUtils.getStayOpen(nbttagcompound);
		coverName = mod_MLB.linkingBookUtils.getCoverName(nbttagcompound);
		coverTexture = mod_MLB.getCover(coverName);
		
		this.isPowered = isPowered;
		spreadState = SpreadState.closed;
		bookSpread = 0f;
		
		linkingPanel = new LinkingPanel(this);
		
		agesManagerListener.updatePerformed(bookDim);
		mod_MLB.linkingBookUtils.agesManager.addListener(agesManagerListener, bookDim);
	}
	
	public void addListener(LinkingBookListener listener) {
		listeners.add(LinkingBookListener.class, listener);
	}
	
	public void removeListener(LinkingBookListener listener) {
		listeners.remove(LinkingBookListener.class, listener);
	}
	
	public void fireNbMissingPagesChanged(int nbPages, int nbMissingPages, int maxPages) {
		for (LinkingBookListener listener : listeners.getListeners(LinkingBookListener.class)) {
			listener.notifyNbMissingPagesChanged(nbPages, nbMissingPages, maxPages);
		}
	}
	
	public void fireColorChanged(int colorCode, int color, Color colorObj, int brighterColor, Color brighterColorObj) {
		for (LinkingBookListener listener : listeners.getListeners(LinkingBookListener.class)) {
			listener.notifyColorChanged(colorCode, color, colorObj, brighterColor, brighterColorObj);
		}
	}
	
	public void fireLinkingPanelStateChanged(LinkingPanel.State state) {
		for (LinkingBookListener listener : listeners.getListeners(LinkingBookListener.class)) {
			listener.notifyLinkingPanelStateChanged(state);
		}
	}
	
	public void fireStayOpenChanged(boolean stayOpen) {
		for (LinkingBookListener listener : listeners.getListeners(LinkingBookListener.class)) {
			listener.notifyStayOpenChanged(stayOpen);
		}
	}
	
	public void fireInvalidated() {
		for (LinkingBookListener listener : listeners.getListeners(LinkingBookListener.class)) {
			listener.notifyInvalidated();
		}
	}
	
	protected void notifyPoweredChanged(boolean isPowered) {
		if (this.isPowered != isPowered) {
			this.isPowered = isPowered;
			linkingPanel.updateState();
		}
	}
	
	protected void notifyBookSpreadChanged(SpreadState spreadState, float bookSpread) {
		this.spreadState = spreadState;
		this.bookSpread = bookSpread;
	}
	
	public NBTTagCompound getNBTTagCompound() {
		return nbttagcompound;
	}
	
	public SpreadState getBookSpreadState() {
		return spreadState;
	}
	
	public float getBookSpread() {
		return bookSpread;
	}
	
	public boolean canLink() {
		return maxPages - nbPages == 0 && (isUnstable ? isPowered : true) && doLinkToDifferentAge();
	}
	
	public void invalidate() {
		mod_MLB.linkingBookUtils.agesManager.removeListener(agesManagerListener, bookDim);
		fireInvalidated();
		listeners = null;
		linkingPanel.invalidate();
	}
	
	public boolean isWritten() {
		if (!isWritten && mod_MLB.linkingBookUtils.isWritten(nbttagcompound)) {
			isWritten = true;
		}
		return isWritten;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean setName(String name) {
		boolean res = mod_MLB.linkingBookUtils.setName(nbttagcompound, name);
		if (res) {
			this.name = name;
		}
		return res;
	}
	
	public int getNbPages() {
		return nbPages;
	}
	
	public int getMaxPages() {
		return maxPages;
	}
	
	public int getNbMissingPages() {
		return maxPages - nbPages;
	}
	
	public int addPages(ItemStack itemstack) {
		int res = mod_MLB.linkingBookUtils.addPages(nbttagcompound, itemstack);
		if (res != 0) {
			nbPages += res;
			int nbMissingPages = maxPages - nbPages;
			linkingPanel.notifyNbMissingPagesChanged();
			fireNbMissingPagesChanged(nbPages, nbMissingPages, maxPages);
		}
		return res;
	}
	
	public int addPages(ItemStack itemstack, int max) {
		int res = mod_MLB.linkingBookUtils.addPages(nbttagcompound, itemstack, max);
		if (res != 0) {
			nbPages += res;
			int nbMissingPages = maxPages - nbPages;
			linkingPanel.notifyNbMissingPagesChanged();
			fireNbMissingPagesChanged(nbPages, nbMissingPages, maxPages);
		}
		return res;
	}
	
	public int addPages(int nb) {
		int res = mod_MLB.linkingBookUtils.addPages(nbttagcompound, nb);
		if (res != 0) {
			nbPages += res;
			int nbMissingPages = maxPages - nbPages;
			linkingPanel.notifyNbMissingPagesChanged();
			fireNbMissingPagesChanged(nbPages, nbMissingPages, maxPages);
		}
		return res;
	}
	
	public ItemStack removePages() {
		ItemStack res = mod_MLB.linkingBookUtils.removePages(nbttagcompound);
		if (res != null) {
			nbPages -= res.stackSize;
			int nbMissingPages = maxPages - nbPages;
			linkingPanel.notifyNbMissingPagesChanged();
			fireNbMissingPagesChanged(nbPages, nbMissingPages, maxPages);
		}
		return res;
	}
	
	public ItemStack removePages(int max) {
		ItemStack res = mod_MLB.linkingBookUtils.removePages(nbttagcompound, max);
		if (res != null) {
			nbPages -= res.stackSize;
			int nbMissingPages = maxPages - nbPages;
			linkingPanel.notifyNbMissingPagesChanged();
			fireNbMissingPagesChanged(nbPages, nbMissingPages, maxPages);
		}
		return res;
	}
	
	public int getColorCode() {
		return colorCode;
	}
	
	public int getColor() {
		return color;
	}
	
	public Color getColorObj() {
		return colorObj;
	}
	
	public int getBrighterColor() {
		return brighterColor;
	}
	
	public Color getBrighterColorObj() {
		return brighterColorObj;
	}
	
	public void setPagesColorFromDye(int dyeColor) {
		mod_MLB.linkingBookUtils.setPagesColorCodeFromDye(nbttagcompound, dyeColor);
		colorCode = mod_MLB.linkingBookUtils.getColorCode(nbttagcompound);
		color = ItemPage.colorInts[colorCode];
		colorObj = ItemPage.colorTable[colorCode];
		brighterColor = ItemPage.brighterColorInts[colorCode];
		brighterColorObj = ItemPage.brighterColorTable[colorCode];
		fireColorChanged(colorCode, color, colorObj, brighterColor, brighterColorObj);
	}
	
	public boolean isUnstable() {
		return isUnstable;
	}
	
	public boolean getStayOpen() {
		return stayOpen;
	}
	
	public void setStayOpen(boolean stayOpen) {
		mod_MLB.linkingBookUtils.setStayOpen(nbttagcompound, stayOpen);
		this.stayOpen = stayOpen;
		fireStayOpenChanged(stayOpen);
	}
	
	public String getCoverName() {
		return coverName;
	}
	
	public String setCoverName(String coverName) {
		coverName = mod_MLB.linkingBookUtils.setCoverName(nbttagcompound, coverName);
		if (!this.coverName.equals(coverName)) {
			this.coverName = coverName;
			coverTexture = mod_MLB.getCover(coverName);
		}
		return coverName;
	}
	
	public void bindModelCoverTexture() {
		mod_MLB.mc.renderEngine.bindTexture(coverTexture.getTextureId());
	}
	
	public void bindModelPagesTexture() {
		mod_MLB.mc.renderEngine.bindTexture(mod_MLB.texture_modelLinkingBookPages.getTextureId());
	}
	
	public BufferedImage getLinkingPanelImage() {
		return mod_MLB.linkingBookUtils.getLinkingPanelImage(nbttagcompound);
	}
	
	public void setLinkingPanelImage(BufferedImage image) {
		mod_MLB.linkingBookUtils.setLinkingPanelImage(nbttagcompound, image);
		linkingPanel.notifyLinkingPanelImageChanged();
	}
	
	public boolean doLinkToDifferentAge() {
		return linksToDifferentAge;
	}
	
	public boolean doLinkChangesDimension(EntityPlayer entityplayer) {
		return mod_MLB.linkingBookUtils.doLinkChangesDimension(nbttagcompound, entityplayer);
	}
	
	public void prepareLinking(EntityPlayer entityplayer) {
		mod_MLB.linkingBookUtils.prepareLinking(nbttagcompound, entityplayer);
	}
	
	public boolean link(Entity entity) {
		return mod_MLB.linkingBookUtils.link(nbttagcompound, entity);
	}
	
	public static enum SpreadState {
		/** When closed, ie: we cannot see what is in the book */
		closed,
		
		/** When not closed, and spreading is increasing */
		opening,
		
		/** When not closed and spreading doesn't change */
		open,
		
		/** When not closed and spreading is decreasing */
		closing
	}
}
