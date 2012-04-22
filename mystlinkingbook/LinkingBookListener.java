package net.minecraft.src.mystlinkingbook;

import java.awt.Color;
import java.util.EventListener;

/**
 * 
 * @author ziliss
 * @since 0.9b
 */
public class LinkingBookListener implements EventListener {
	
	public void notifyNbMissingPagesChanged(int nbPages, int nbMissingPages, int maxPages) {
	}
	
	public void notifyColorChanged(int colorCode, int color, Color colorObj, int brighterColor, Color brighterColorObj) {
	}
	
	public void notifyLinkingPanelStateChanged(LinkingPanel.State state) {
	}
	
	public void notifyStayOpenChanged(boolean stayOpen) {
	}
	
	public void notifyInvalidated() {
	}
}
