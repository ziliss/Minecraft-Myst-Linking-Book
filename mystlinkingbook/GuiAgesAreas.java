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
public class GuiAgesAreas extends GuiScreen {
	
	/**
	 * Reference to the mod instance.
	 */
	public Mod_MystLinkingBook mod_MLB;
	
	/**
	 * The player opening the GUI.
	 */
	public EntityPlayer entityplayer;
	
	public AgesManager agesManager;
	
	protected AgesManagerListener agesManagerListener = new AgesManagerListener() {
		@Override
		public void updatePerformed(int dimension) {
			if (agesAreasList != null) {
				agesAreasList.cancel = true;
			}
			mc.displayGuiScreen(null);
			mc.setIngameFocus();
		}
	};
	
	public AgeAreasList agesAreasList;
	public MovingObjectPosition aimedBlock = null;
	public String aimedBlockStr;
	public String aimedBlockInAgesAreasStr = null;
	public String intersectingAgesAreasStr;
	protected String outlinedAgeAreaStr;
	
	public boolean canEdit = false;
	public AgeArea currentAgeArea = null;
	
	// Using coordinates relative to those to draw everything:
	protected int guiLeft;
	protected int guiTop;
	protected int guiWidth = 256;
	protected int guiHeight = 210;
	
	protected GuiSelect agesSelect;
	
	protected int outlinedAgeAreaId = -1;
	
	protected GuiTextField nameTextfield;
	protected GuiTextField pos1Textfield;
	protected GuiTextField pos2Textfield;
	protected GuiButtonStates enabledButton;
	protected GuiButton addButton;
	protected GuiButton removeButton;
	protected GuiButton saveButton;
	protected GuiButtonStates outlineButton;
	
	public GuiAgesAreas(EntityPlayer entityplayer, Mod_MystLinkingBook mod_MLB) {
		this.entityplayer = entityplayer;
		this.mod_MLB = mod_MLB;
		agesManager = mod_MLB.linkingBookUtils.agesManager;
		
		aimedBlock = entityplayer.rayTrace(mod_MLB.mc.playerController.getBlockReachDistance(), 0);
		if (aimedBlock.sideHit == -1) {
			aimedBlock = null;
		}
		else {
			aimedBlockStr = "- Position: " + aimedBlock.blockX + " " + aimedBlock.blockY + " " + aimedBlock.blockZ;
			List<AgeArea> list = mod_MLB.linkingBookUtils.agesManager.getAllReadyAgeAreaContaining(aimedBlock.blockX, aimedBlock.blockY, aimedBlock.blockZ, entityplayer.dimension);
			if (!list.isEmpty()) {
				StringBuilder builder = new StringBuilder("- In Ages areas Ids: ");
				for (AgeArea ageArea : list) {
					builder.append(ageArea.id + ", ");
				}
				builder.setLength(builder.length() - 2);
				aimedBlockInAgesAreasStr = builder.toString();
			}
			else {
				aimedBlockInAgesAreasStr = "- In no Age area.";
			}
		}
		
	}
	
	@Override
	public void initGui() {
		guiLeft = (width - guiWidth) / 2;
		guiTop = (height - guiHeight) / 2;
		
		if (agesAreasList == null) {
			agesAreasList = agesManager.getAgesAreasListWithEdition(entityplayer, entityplayer.dimension);
			canEdit = agesAreasList.canEdit;
			mod_MLB.linkingBookUtils.agesManager.addListener(agesManagerListener, agesAreasList.dimension);
		}
		
		controlList.clear();
		agesSelect = new GuiSelect(0, guiLeft + 5, guiTop + 17, 100, 7, 15) {
			@Override
			protected int getNbLines() {
				return agesAreasList.displayAgesAreas.size();
			}
			
			@Override
			protected int lineSelected(int line, int prevLine) {
				if (line >= 0 && line < agesAreasList.displayAgesAreas.size()) {
					displayAgeArea(agesAreasList.displayAgesAreas.get(line));
				}
				else {
					displayAgeArea(null);
				}
				return line;
			}
			
			@Override
			public void drawLine(int lineNb, int left, int top, int right, int bottom, boolean selected, boolean enabled, Tessellator tessellator) {
				super.drawLine(lineNb, left, top, right, bottom, selected, enabled, tessellator);
				AgeArea ageArea = agesAreasList.displayAgesAreas.get(lineNb);
				boolean isInvalid = !ageArea.isValid();
				boolean isDisabled = ageArea.disabled;
				int color = 0xe0e0e0;
				if (isDisabled) {
					color = 0x606060;
				}
				else if (isInvalid) {
					color = 0xe00000;
				}
				fontRenderer.drawStringWithShadow(ageArea.name, left + 2, top + 3, color);
			}
		};
		controlList.add(agesSelect);
		
		nameTextfield = new GuiTextField(fontRenderer, guiLeft + 140, guiTop + 17, 110, 14);
		nameTextfield.setMaxStringLength(16);
		// nameTextfield.setFocused(true);
		
		pos1Textfield = new GuiTextField(fontRenderer, guiLeft + 140, guiTop + 40, 110, 14);
		pos1Textfield.setMaxStringLength(18);
		
		pos2Textfield = new GuiTextField(fontRenderer, guiLeft + 140, guiTop + 63, 110, 14);
		pos2Textfield.setMaxStringLength(18);
		
		enabledButton = new GuiButtonStates(3, guiLeft + 155, guiTop + 84, 75, 18, "Enabled", new String[] { "NO", "YES" }) {
			@Override
			public void updateState() {
				setBooleanState(currentAgeArea == null ? false : !currentAgeArea.disabled);
			}
		};
		enabledButton.drawButton = false;
		controlList.add(enabledButton);
		
		addButton = new GuiButton(1, guiLeft + 5, agesSelect.yPosition + agesSelect.getHeight() + 5, 20, 20, "+");
		addButton.enabled = canEdit;
		controlList.add(addButton);
		removeButton = new GuiButton(2, guiLeft + 30, agesSelect.yPosition + agesSelect.getHeight() + 5, 20, 20, "-");
		removeButton.enabled = false;
		controlList.add(removeButton);
		
		saveButton = new GuiButton(4, guiLeft + 55, agesSelect.yPosition + agesSelect.getHeight() + 5, 85, 20, canEdit ? "Save and close" : "Close");
		controlList.add(saveButton);
		
		outlinedAgeAreaId = mod_MLB.outlineAgeArea.isAgeAreaSet() ? mod_MLB.outlineAgeArea.getAgeArea().id : -1;
		outlineButton = new GuiButtonStates(5, guiLeft + 5, guiTop + 153, 60, 18, null, new String[] { "Outline", "Un-outline" }) {
			@Override
			public void updateState() {
				if (outlinedAgeAreaId == -1) {
					setBooleanState(false);
					enabled = currentAgeArea != null;
					outlinedAgeAreaStr = "Outlined Age area id: none";
				}
				else {
					setBooleanState(true);
					outlinedAgeAreaStr = "Outlined Age area id: " + outlinedAgeAreaId;
				}
			}
		};
		controlList.add(outlineButton);
		
		if (canEdit) {
			agesSelect.setSelectedLine(0);
		}
	}
	
	public void displayAgeArea(AgeArea ageArea) {
		currentAgeArea = ageArea;
		if (currentAgeArea == null) {
			nameTextfield.setText("");
			PrivateAccesses.GuiTextField_isEnabled.setTo(nameTextfield, false);
			pos1Textfield.setText("");
			PrivateAccesses.GuiTextField_isEnabled.setTo(pos1Textfield, false);
			pos2Textfield.setText("");
			PrivateAccesses.GuiTextField_isEnabled.setTo(pos2Textfield, false);
			enabledButton.drawButton = false;
			enabledButton.enabled = false;
			intersectingAgesAreasStr = "";
			removeButton.enabled = false;
		}
		else {
			nameTextfield.setText(currentAgeArea.name);
			PrivateAccesses.GuiTextField_isEnabled.setTo(nameTextfield, canEdit);
			pos1Textfield.setText(currentAgeArea.getPos1());
			PrivateAccesses.GuiTextField_isEnabled.setTo(pos1Textfield, canEdit);
			pos2Textfield.setText(currentAgeArea.getPos2());
			PrivateAccesses.GuiTextField_isEnabled.setTo(pos2Textfield, canEdit);
			enabledButton.drawButton = true;
			enabledButton.enabled = canEdit;
			removeButton.enabled = canEdit;
			updateIntersetingAgesAreas();
		}
		enabledButton.updateState();
		outlineButton.updateState();
	}
	
	public void updateIntersetingAgesAreas() {
		List<AgeArea> intersectingAgesAreas = agesAreasList.getIntersectingAgesAreasForDisplayedAgeArea(currentAgeArea);
		if (!intersectingAgesAreas.isEmpty()) {
			StringBuilder builder = new StringBuilder("Intersects: ");
			for (AgeArea intersecting : intersectingAgesAreas) {
				builder.append(intersecting.id + ", ");
			}
			builder.setLength(builder.length() - 2);
			intersectingAgesAreasStr = builder.toString();
		}
		else {
			intersectingAgesAreasStr = "";
		}
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
					updateIntersetingAgesAreas();
				}
			}
			else if (PrivateAccesses.GuiTextField_isEnabled.getFrom(pos2Textfield) && pos2Textfield.getIsFocused()) {
				if (pos2Textfield.textboxKeyTyped(c, i)) {
					currentAgeArea.setPos2(pos2Textfield.getText());
					updateIntersetingAgesAreas();
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
			agesAreasList.cancel = true;
		}
		super.keyTyped(c, i);
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if (guibutton == addButton) {
			agesAreasList.addNewDisplayedAgeArea();
			agesSelect.setSelectedLine(agesSelect.getNbLines() - 1);
		}
		else if (guibutton == removeButton) {
			if (currentAgeArea != null && outlinedAgeAreaId == currentAgeArea.id) {
				outlinedAgeAreaId = -1;
			}
			int selectedLine = agesSelect.getSelectedLine();
			agesAreasList.removeDisplayedAgeArea(selectedLine);
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
		else if (guibutton == outlineButton) {
			if (outlineButton.getBooleanState()) {
				outlinedAgeAreaId = -1;
			}
			else {
				outlinedAgeAreaId = currentAgeArea == null ? -1 : currentAgeArea.id;
				mod_MLB.outlineAgeArea.setAgeArea(outlineButton.getBooleanState() ? null : currentAgeArea);
			}
			outlineButton.updateState();
		}
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
	
	@Override
	public void onGuiClosed() {
		if (agesAreasList != null) {
			mod_MLB.linkingBookUtils.agesManager.removeListener(agesManagerListener, agesAreasList.dimension);
			agesManager.endEdition(entityplayer, agesAreasList);
			
			if (outlinedAgeAreaId == -1) {
				mod_MLB.outlineAgeArea.unsetAgeArea();
			}
			else {
				AgeArea outlined = agesManager.getAgeAreaWithId(agesAreasList.dimension, outlinedAgeAreaId);
				if (outlined != null) {
					mod_MLB.outlineAgeArea.setAgeArea(outlined.clone());
				}
				else {
					mod_MLB.outlineAgeArea.unsetAgeArea();
				}
			}
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		drawDefaultBackground();
		
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		
		// Inspired by GuiSlot.overlayBackground():
		Tessellator tessellator = Tessellator.instance;
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, mc.renderEngine.getTexture("/gui/background.png"));
		float textureSize = 32F; // Used to repeat the texture as a mosaic.
		tessellator.startDrawingQuads();
		tessellator.setColorOpaque_I(0x202020);
		tessellator.addVertexWithUV(guiLeft, guiTop + guiHeight, 0, 0D, guiHeight / textureSize);
		tessellator.addVertexWithUV(guiLeft + guiWidth, guiTop + guiHeight, 0, guiWidth / textureSize, guiHeight / textureSize);
		tessellator.addVertexWithUV(guiLeft + guiWidth, guiTop, 0, guiWidth / textureSize, 0D);
		tessellator.addVertexWithUV(guiLeft, guiTop, 0, 0D, 0D);
		tessellator.draw();
		
		fontRenderer.drawString("Ages areas for current dimension (" + agesAreasList.dimension + "):", guiLeft + 5, guiTop + 4, 0xe0e0e0);
		
		if (currentAgeArea != null) {
			int colorReady = canEdit ? 0xe0e0e0 : 0x404040;
			int colorInvalid = canEdit ? 0xe00000 : 0x400000;
			int pos1CubeColor = 0x00ff00;
			int pos2CubeColor = 0x0000ff;
			boolean outlined = outlinedAgeAreaId != -1 && outlinedAgeAreaId == currentAgeArea.id;
			int pos1Color = currentAgeArea.pos1Set ? outlined ? pos1CubeColor : colorReady : colorInvalid;
			int pos2Color = currentAgeArea.pos2Set ? outlined ? pos2CubeColor : colorReady : colorInvalid;
			fontRenderer.drawString("Name: ", guiLeft + 110, guiTop + 20, colorReady);
			nameTextfield.drawTextBox();
			fontRenderer.drawString("Pos1:", guiLeft + 110, guiTop + 43, pos1Color);
			pos1Textfield.drawTextBox();
			fontRenderer.drawString("Pos2:", guiLeft + 110, guiTop + 66, pos2Color);
			pos2Textfield.drawTextBox();
			fontRenderer.drawString("Id: " + currentAgeArea.id, guiLeft + 110, guiTop + 89, colorReady);
			fontRenderer.drawString(intersectingAgesAreasStr, guiLeft + 110, guiTop + 112, colorReady);
		}
		else if (!canEdit && agesAreasList.playerEditingName != null) {
			String alreadyBeingEditedStr = "Already being edited by:";
			int strWidth = fontRenderer.getStringWidth(alreadyBeingEditedStr);
			fontRenderer.drawString("Already being edited by:", guiLeft + 175 - strWidth / 2, guiTop + 60, 0xe0e0e0);
			strWidth = fontRenderer.getStringWidth(agesAreasList.playerEditingName);
			fontRenderer.drawString(agesAreasList.playerEditingName, guiLeft + 175 - strWidth / 2, guiTop + 70, 0xe0e0e0);
		}
		
		fontRenderer.drawString(outlinedAgeAreaStr, guiLeft + 68, guiTop + 158, 0xe0e0e0);
		
		int aimedBlockInfoTop = guiTop + 177;
		fontRenderer.drawString("Aimed block informations:", guiLeft + 5, aimedBlockInfoTop, 0xe0e0e0);
		if (aimedBlock != null) {
			fontRenderer.drawString(aimedBlockStr, guiLeft + 5, aimedBlockInfoTop + 10, 0xe0e0e0);
			fontRenderer.drawString(aimedBlockInAgesAreasStr, guiLeft + 5, aimedBlockInfoTop + 20, 0xe0e0e0);
		}
		else {
			fontRenderer.drawString("No aimed block.", guiLeft + 5, aimedBlockInfoTop + 20, 0xe0e0e0);
		}
		
		super.drawScreen(mouseX, mouseY, f);
	}
}
