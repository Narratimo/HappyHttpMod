package com.clapter.httpautomator.client.gui;

import com.clapter.httpautomator.Constants;
import com.clapter.httpautomator.blockentity.HttpSenderBlockEntity;
import com.clapter.httpautomator.enums.EnumHttpMethod;
import com.clapter.httpautomator.network.packet.SUpdateHttpSenderValuesPacket;
import com.clapter.httpautomator.platform.Services;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class HttpSenderSettingsScreen extends Screen {

    private static final Component TITLE = Component.translatable("gui." + Constants.MOD_ID + ".http_sender_settings_screen");
    private static final Component SAVE_TEXT = Component.translatable("gui." + Constants.MOD_ID + ".save_button");
    private static final Component URL_LABEL = Component.translatable("gui." + Constants.MOD_ID + ".url_label");
    private static final Component METHOD_LABEL = Component.translatable("gui." + Constants.MOD_ID + ".method_label");

    private final int screenWidth;
    private final int screenHeight;
    private int leftPos;
    private int topPos;
    private final HttpSenderBlockEntity blockEntity;

    private Button saveButton;
    private EditBox urlInput;
    private CycleButton<EnumHttpMethod> methodButton;

    private String urlText;
    private EnumHttpMethod httpMethod;

    public HttpSenderSettingsScreen(HttpSenderBlockEntity blockEntity) {
        super(TITLE);
        screenWidth = 250;
        screenHeight = 166;
        this.blockEntity = blockEntity;
        this.urlText = blockEntity.getValues().url;
        this.httpMethod = blockEntity.getValues().httpMethod;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - screenWidth) / 2;
        this.topPos = (this.height - screenHeight) / 2;

        // URL input
        this.urlInput = new EditBox(font, leftPos + 10, topPos + 30, 230, 20, Component.empty());
        this.urlInput.setMaxLength(256);
        this.urlInput.setResponder(text -> urlText = text);
        this.urlInput.setValue(blockEntity.getValues().url);
        addRenderableWidget(urlInput);

        // HTTP Method selector
        this.methodButton = addRenderableWidget(
            CycleButton.<EnumHttpMethod>builder(EnumHttpMethod::getComponent)
                .withValues(EnumHttpMethod.values())
                .withInitialValue(this.httpMethod)
                .create(leftPos + 10, topPos + 60, 80, 20, METHOD_LABEL, (button, value) -> {
                    this.httpMethod = value;
                })
        );

        // Save button
        this.saveButton = addRenderableWidget(Button.builder(
                SAVE_TEXT, this::handleSaveButton)
                .bounds(leftPos + 10, topPos + 130, 80, 20)
                .build()
        );
    }

    private void handleSaveButton(Button button) {
        if (this.checkValues()) {
            HttpSenderBlockEntity.Values values = blockEntity.getValues();
            values.url = this.urlText;
            values.httpMethod = this.httpMethod;

            Services.PACKET_HANDLER.sendPacketToServer(new SUpdateHttpSenderValuesPacket(
                    this.blockEntity.getBlockPos(),
                    values));

            this.onClose();
        }
    }

    private boolean checkValues() {
        return urlText != null && !urlText.isEmpty();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawString(font, URL_LABEL, leftPos + 10, topPos + 18, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
