package net.minecraft.src.mystlinkingbook;

import java.util.List;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiTextField;
import net.minecraft.src.MovingObjectPosition;
import net.minecraft.src.Tessellator;

import org.lwjgl.opengl.GL11;

/**
 * 
 * @author ziliss
 * @since 0.8b
 */
public class GuiAgesArea extends GuiScreen {
	
	/**
	 * Reference to the mod instance.
	 */
	public Mod_MystLinkingBook mod_MLB;
	
	/**
	 * The player opening the GUI.
	 */
	public EntityPlayer entityplayer;
	
	public AgesManager agesManager;
	
	public AgeAreasModifications ageAreasMods;
	public MovingObjectPosition aimedBlock = null;
	public String aimedBlockStr;
	public String aimedBlockAgesAreasStr = null;
	
	public boolean canEdit = false;
	public AgeArea currentAgeArea = null;
	
	// Using coordinates relative to those to draw everything:
	int guiLeft;
	int guiTop;
	int guiWidth = 250;
	int guiHeight = 180;
	
	GuiSelect agesSelect;
	
	GuiTextField nameTextfield;
	GuiTextField pos1Textfield;
	GuiTextField pos2Textfield;
	GuiButtonStates enabledButton;
	GuiButton addButton;
	GuiButton removeButton;
	GuiButton saveButton;
	
	public GuiAgesArea(EntityPlayer entityplayer, Mod_MystLinkingBook mod_MLB) {
		this.entityplayer = entityplayer;
		this.mod_MLB = mod_MLB;
		agesManager = mod_MLB.linkingBook.agesManager;
		
		aimedBlock = entityplayer.rayTrace(mod_MLB.mc.playerController.getBlockReachDistance(), 0);
		if (aimedBlock.sideHit == -1) {
			aimedBlock = null;
			aimedBlockStr = "Aimed block: none";
		}
		else {
			aimedBlockStr = "Aimed block: " + aimedBlock.blockX + " " + aimedBlock.blockY + " " + aimedBlock.blockZ;
			List<AgeArea> list = mod_MLB.linkingBook.agesManager.getAllReadyAgeAreaContaining(aimedBlock.blockX, aimedBlock.blockY, aimedBlock.blockZ, entityplayer.dimension);
			if (!list.isEmpty()) {
				StringBuilder builder = new StringBuilder("In Ages Areas Ids: ");
				for (AgeArea ageArea : list) {
					builder.append(ageArea.id + ", ");
				}
				builder.setLength(builder.length() - 2);
				aimedBlockAgesAreasStr = builder.toString();
			}
			else {
				aimedBlockAgesAreasStr = "In no Age Area.";
			}
		}
		
	}
	
	@Override
	public void initGui() {
		guiLeft = (width - guiWidth) / 2;
		guiTop = (height - guiHeight) / 2;
		
		if (ageAreasMods == null) {
			ageAreasMods = agesManager.startEdition(entityplayer, entityplayer.dimension);
		}
		
		controlList.clear();
		agesSelect = new GuiSelect(0, guiLeft + 5, guiTop + 5, 100, 4, 25) {
			@Override
			protected int getNbLines() {
				return ageAreasMods.displayAgeAreas.size();
			}
			
			@Override
			protected int lineSelected(int line, int prevLine) {
				if (line >= 0 && line < ageAreasMods.displayAgeAreas.size()) {
					displayAgeArea(ageAreasMods.displayAgeAreas.get(line));
				}
				else {
					displayAgeArea(null);
				}
				return line;
			}
			
			@Override
			public void drawLine(int lineNb, int left, int top, int right, int bottom, boolean selected, boolean enabled, Tessellator tessellator) {
				super.drawLine(lineNb, left, top, right, bottom, selected, enabled, tessellator);
				AgeArea ageArea = ageAreasMods.displayAgeAreas.get(lineNb);
				boolean isInvalid = !ageArea.isValid();
				boolean isDisabled = ageArea.disabled;
				int color = 0xe0e0e0;
				StringBuilder msg = new StringBuilder();
				if (isInvalid) {
					msg.append("Invalid ");
				}
				if (isDisabled) {
					msg.append("Disabled ");
				}
				if (msg.length() > 0) {
					color = 0xff0000;
					msg.setLength(msg.length() - 1);
					fontRenderer.drawStringWithShadow(msg.insert(0, " (").append(")").toString(), left + 2, top + 14, color);
				}
				fontRenderer.drawStringWithShadow(ageArea.name, left + 2, top + 2, color);
			}
		};
		controlList.add(agesSelect);
		
		nameTextfield = new GuiTextField(fontRenderer, guiLeft + 140, guiTop + 5, 120, 14);
		nameTextfield.setMaxStringLength(16);
		// nameTextfield.func_50033_b(true); // Was setFocused(boolean b) before MC 1.2.4
		
		pos1Textfield = new GuiTextField(fontRenderer, guiLeft + 140, guiTop + 30, 120, 14);
		pos1Textfield.setMaxStringLength(16);
		
		pos2Textfield = new GuiTextField(fontRenderer, guiLeft + 140, guiTop + 55, 120, 14);
		pos2Textfield.setMaxStringLength(16);
		
		enabledButton = new GuiButtonStates(3, guiLeft + 158, guiTop + 78, 75, 18, "Enabled", new String[] { "NO", "YES" }) {
			@Override
			public void updateState() {
				setBooleanState(currentAgeArea == null ? false : !currentAgeArea.disabled);
			}
		};
		controlList.add(enabledButton);
		
		addButton = new GuiButton(1, guiLeft + 5, agesSelect.yPosition + agesSelect.getHeight() + 5, 20, 20, "+");
		addButton.enabled = ageAreasMods.canEdit;
		controlList.add(addButton);
		removeButton = new GuiButton(2, guiLeft + 30, agesSelect.yPosition + agesSelect.getHeight() + 5, 20, 20, "-");
		controlList.add(removeButton);
		
		saveButton = new GuiButton(4, guiLeft + 55, agesSelect.yPosition + agesSelect.getHeight() + 5, 85, 20, "Save and close");
		controlList.add(saveButton);
		
		agesSelect.setSelectedLine(0);
	}
	
	public void displayAgeArea(AgeArea ageArea) {
		currentAgeArea = ageArea;
		if (currentAgeArea == null) {
			nameTextfield.setText("");
			pos1Textfield.setText("");
			pos2Textfield.setText("");
			enabledButton.drawButton = false;
		}
		else {
			nameTextfield.setText(currentAgeArea.name);
			pos1Textfield.setText(currentAgeArea.getPos1());
			pos2Textfield.setText(currentAgeArea.getPos2());
			enabledButton.drawButton = true;
		}
		enabledButton.updateState();
		enabledButton.enabled = ageAreasMods.canEdit;
		removeButton.enabled = ageAreasMods.canEdit && currentAgeArea != null;
	}
	
	@Override
	protected void mouseClicked(int i, int j, int k) {
		if (currentAgeArea != null) {
			nameTextfield.mouseClicked(i, j, k);
			pos1Textfield.mouseClicked(i, j, k);
			pos2Textfield.mouseClicked(i, j, k);
		}
		super.mouseClicked(i, j, k);
	}
	
	@Override
	protected void keyTyped(char c, int i) {
		boolean typedInTextField = false;
		if (currentAgeArea != null) {
			typedInTextField = true;
			if (PrivateAccesses.GuiTextField_isEnabled.getFrom(nameTextfield) && nameTextfield.getIsFocused()) {
				if (nameTextfield.textboxKeyTyped(c, i)) {
					currentAgeArea.name = nameTextfield.getText();
				}
			}
			else if (PrivateAccesses.GuiTextField_isEnabled.getFrom(pos1Textfield) && pos1Textfield.getIsFocused()) {
				if (pos1Textfield.textboxKeyTyped(c, i)) {
					currentAgeArea.setPos1(pos1Textfield.getText());
				}
			}
			else if (PrivateAccesses.GuiTextField_isEnabled.getFrom(pos2Textfield) && pos2Textfield.getIsFocused()) {
				if (pos2Textfield.textboxKeyTyped(c, i)) {
					currentAgeArea.setPos2(pos2Textfield.getText());
				}
			}
			else {
				typedInTextField = false;
			}
		}
		
		if (!typedInTextField) {
			if (i == mc.gameSettings.keyBindInventory.keyCode) {
				mc.displayGuiScreen(null);
				mc.setIngameFocus();
			}
		}
		if (i == 1) { // Esc
			ageAreasMods.cancel = true;
		}
		super.keyTyped(c, i);
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if (guibutton == addButton) {
			ageAreasMods.addNewDisplayedAgeArea();
		}
		else if (guibutton == removeButton) {
			int selectedLine = agesSelect.getSelectedLine();
			ageAreasMods.removeDisplayedAgeArea(selectedLine);
			agesSelect.setSelectedLine(selectedLine > 0 ? selectedLine - 1 : 0);
		}
		else if (guibutton == enabledButton) {
			if (currentAgeArea != null) {
				currentAgeArea.disabled = enabledButton.getBooleanState();
				enabledButton.updateState();
			}
		}
		else if (guibutton == saveButton) {
			mc.displayGuiScreen(null);
		}
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
	
	@Override
	public void onGuiClosed() {
		if (ageAreasMods != null) {
			agesManager.endEdition(entityplayer, ageAreasMods);
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		drawDefaultBackground();
		
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		drawRect(guiLeft, guiTop, guiLeft + guiWidth, guiTop + guiHeight, 0xff000000);
		
		if (currentAgeArea != null) {
			int color = ageAreasMods.canEdit ? 0xe0e0e0 : 0x404040;
			fontRenderer.drawString("Name: ", guiLeft + 110, guiTop + 8, color);
			nameTextfield.drawTextBox();
			fontRenderer.drawString("Pos1:", guiLeft + 110, guiTop + 33, color);
			pos1Textfield.drawTextBox();
			fontRenderer.drawString("Pos2:", guiLeft + 110, guiTop + 58, color);
			pos2Textfield.drawTextBox();
			// 3, guiLeft + 138, guiTop + 78, 80, 18,
			fontRenderer.drawString("Id: " + currentAgeArea.id, guiLeft + 110, guiTop + 83, color);
		}
		
		fontRenderer.drawString(aimedBlockStr, guiLeft + 5, guiTop + 160, 0xe0e0e0);
		fontRenderer.drawString(aimedBlockAgesAreasStr, guiLeft + 5, guiTop + 170, 0xe0e0e0);
		
		super.drawScreen(mouseX, mouseY, f);
	}
}
