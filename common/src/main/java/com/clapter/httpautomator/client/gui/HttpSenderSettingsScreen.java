package com.clapter.httpautomator.client.gui;

import com.clapter.httpautomator.Constants;
import com.clapter.httpautomator.blockentity.HttpSenderBlockEntity;
import com.clapter.httpautomator.client.gui.widgets.ScrollableWidget;
import com.clapter.httpautomator.enums.EnumHttpMethod;
import com.clapter.httpautomator.enums.EnumPoweredType;
import com.clapter.httpautomator.network.packet.SUpdateHttpSenderValuesPacket;
import com.clapter.httpautomator.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpSenderSettingsScreen extends BaseBlockScreen {

    private static final Component TITLE = Component.translatable("gui."+ Constants.MOD_ID + ".http_sender_settings_screen");
    private static final Component START_TEXT = Component.translatable("gui."+ Constants.MOD_ID + ".http_sender_startbutton");
    private static final Component URL_TEXT = Component.translatable("gui."+ Constants.MOD_ID + ".http_sender_endpoint");
    private static final Component PARAMETERS_TEXT = Component.translatable("gui."+ Constants.MOD_ID + ".http_sender_parameters");

    private HttpSenderBlockEntity blockEntity;

    private EnumHttpMethod httpMethod;
    private boolean forceMapInit;
    private Button startButton;
    private ScrollableWidget scrollablePanel;
    private String countInputAsString = "";
    private Button saveCountButton;
    private EditBox countInput;
    private int numberOfFields = 0;
    private String endpointText;


    public HttpSenderSettingsScreen(HttpSenderBlockEntity blockEntity) {
        super(TITLE);

    }

    @Override
    public void assignEntity(BlockEntity entity) {
        this.blockEntity = (HttpSenderBlockEntity)entity;
        forceMapInit = true;
        this.httpMethod = this.blockEntity.getValues().httpMethod;
    }

    @Override
    protected void init() {
        super.init();
        this.parameterFieldsPosX = this.leftPos+40;
        this.parameterFieldsPosXOffset = this.leftPos+130;
        this.startButton = addRenderableWidget(startButton.builder(
                        START_TEXT, this::handleStartButton)
                .bounds(leftPos+74, topPos+180, 50, 20)
                .build()
        );
        if(this.forceMapInit) {
            this.readParameterMap(this.blockEntity.getValues().parameterMap);
            this.forceMapInit = false;
        }
        EditBox endpoint = new EditBox(font, leftPos, topPos + 6, 198, 20, Component.empty());
        endpoint.setMaxLength(1028);
        endpoint.setResponder(text -> {
            endpointText = text;
        });
        endpoint.setMaxLength(2056);
        endpoint.insertText(blockEntity.getValues().url);


        countInput = new EditBox(this.font, leftPos, topPos + 30, 100, 20, Component.literal("Count"));
        this.countInput.setResponder(text -> {
            countInputAsString = text;
        });

        this.saveCountButton = this.addRenderableWidget(saveCountButton.builder(
                Component.literal("Apply"),button -> {
                    if(countInputAsString.isEmpty())return;
                    numberOfFields = Integer.parseInt(countInput.getValue());
                    createInputFields(numberOfFields);
                }).bounds(leftPos + 100, topPos + 30, 100, 20).build());

        if(!this.parameterFields.isEmpty()) {
            this.drawParameters();
        }
        CycleButton<EnumHttpMethod> enumButton = CycleButton.builder(EnumHttpMethod::getComponent)
                .withValues(EnumHttpMethod.values())
                .withInitialValue(this.httpMethod)
                .create(leftPos+200, topPos+6, 30, 20, Component.empty()
                        , (but, value) -> { this.handleHttpMethodSwitch(value); });
        MultiLineTextWidget endpointText = new MultiLineTextWidget(leftPos-60, topPos + 13, URL_TEXT, this.font);
        MultiLineTextWidget parametersText = new MultiLineTextWidget(leftPos-60, topPos + 33, PARAMETERS_TEXT, this.font);
        this.addRenderableWidget(endpointText);
        this.addRenderableWidget(parametersText);
        this.addRenderableWidget(endpoint);
        this.addRenderableWidget(countInput);
        this.addRenderableWidget(enumButton);



    }

    private void handleHttpMethodSwitch(EnumHttpMethod method){
        this.httpMethod = method;
    }

    private void drawParameters(){
        this.scrollablePanel = new ScrollableWidget(leftPos, topPos + 60, 100, 100, this.parameterFields);
        this.addRenderableWidget(scrollablePanel);
    }

    private void handleStartButton(Button button){
        if(this.checkValues()){
            HttpSenderBlockEntity.Values values = blockEntity.getValues();
            values.url = this.endpointText;
            values.httpMethod = this.httpMethod;
            if(this.scrollablePanel != null){
                values.parameterMap = this.getParameterValues();
            }
            Services.PACKET_HANDLER.sendPacketToServer(new SUpdateHttpSenderValuesPacket(
                    this.blockEntity.getBlockPos(),
                    values));
        }
    }

    private Map<String, String> getParameterValues(){
        return getParameterValuesFromScrollable(this.scrollablePanel);
    }

    private boolean checkValues(){
        return endpointText != null && !endpointText.isEmpty();
    }

    @Override
    public void render(GuiGraphics $$0, int $$1, int $$2, float $$3) {
        super.render($$0, $$1, $$2, $$3);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

}
