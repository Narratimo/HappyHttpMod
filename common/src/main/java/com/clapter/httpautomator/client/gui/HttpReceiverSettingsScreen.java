package com.clapter.httpautomator.client.gui;

import com.clapter.httpautomator.Constants;
import com.clapter.httpautomator.blockentity.HttpReceiverBlockEntity;
import com.clapter.httpautomator.client.gui.widgets.ScrollableWidget;
import com.clapter.httpautomator.enums.EnumPoweredType;
import com.clapter.httpautomator.enums.EnumTimerUnit;
import com.clapter.httpautomator.network.packet.SUpdateHttpReceiverValuesPacket;
import com.clapter.httpautomator.platform.Services;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL12;

import java.util.*;
import java.util.List;

public class HttpReceiverSettingsScreen extends BaseBlockScreen {

    private static final Component TITLE = Component.translatable("gui."+ Constants.MOD_ID + ".http_receiver_settings_screen");
    private static final Component ENDPOINT_TEXT = Component.translatable("gui."+ Constants.MOD_ID + ".http_receiver_settings_screen_endpoint_text");
    private static final Component TYPE_TEXT = Component.translatable("gui."+ Constants.MOD_ID + ".http_receiver_settings_screen_power_type");
    private static final Component START_TEXT = Component.translatable("gui."+ Constants.MOD_ID + ".http_receiver_startbutton");
    private static final Component REDIRECT_TEXT = Component.translatable("gui."+ Constants.MOD_ID + ".http_receiver_redirect_text");
    private static final Component PARAMETERS_TEXT = Component.translatable("gui."+ Constants.MOD_ID + ".http_receiver_parameters_text");

    private HttpReceiverBlockEntity blockEntity;

    private boolean forceMapInit;
    private Button startButton;
    private EditBox endpoint;
    private EditBox timer;
    private EditBox redirectBox;
    private ScrollableWidget scrollablePanel;
    private Button saveParameterCountButton;
    private EditBox parameterCountInput;

    private String endpointText;
    private String timerText;
    private String redirectUrl;
    private EnumPoweredType poweredType = EnumPoweredType.SWITCH;
    private EnumTimerUnit timerUnit = EnumTimerUnit.TICKS;
    private int numberOfFields = 0;
    private String numberOfFieldsAsString = "";
    private String adress;

    public HttpReceiverSettingsScreen(HttpReceiverBlockEntity blockEntity, String adress) {
        super(TITLE);

    }

    @Override
    public void assignEntity(BlockEntity entity) {
        super.assignEntity(entity);
        this.blockEntity = (HttpReceiverBlockEntity)entity;
        this.endpointText = this.blockEntity.getValues().url;
        this.poweredType = this.blockEntity.getValues().poweredType;
        this.timerText = Float.toString(this.blockEntity.getValues().timer);
        this.timerUnit = this.blockEntity.getValues().timerUnit;
        this.redirectUrl = this.blockEntity.getValues().redirectClientUrl;
        this.forceMapInit = true;
        this.adress = adress;
    }

    @Override
    protected void init() {
        super.init();
        this.topPos = (this.height - screenHeight) / 2 - 30;
        this.leftPos = (this.width - screenWidth) / 2 + 50;
        this.parameterFieldsPosX = this.leftPos+40;
        this.parameterFieldsPosXOffset = this.leftPos+130;
        this.startButton = addRenderableWidget(startButton.builder(
                START_TEXT, this::handleStartButton)
                .bounds(leftPos+27, topPos+200, 50, 20)
                .build()
        );
        //if(this.forceMapInit) {
            //this.readParameterMap(this.blockEntity.getValues().parameterMap);
        //    this.forceMapInit = false;
        //}
        this.endpoint = new EditBox(font, leftPos, topPos + 6, 198, 20, Component.empty());
        this.endpoint.setMaxLength(2056);
        this.endpoint.setResponder(text -> {
            endpointText = text;
        });
        MultiLineTextWidget endpointText = new MultiLineTextWidget(leftPos-50, topPos-5, ENDPOINT_TEXT, this.font);
        MultiLineTextWidget adressTextPublic = new MultiLineTextWidget(leftPos-140, topPos+5,
                Component.literal("External:"+this.blockEntity.getValues().publicAdress+"/"), this.font);
        MultiLineTextWidget adressTextPrivate = new MultiLineTextWidget(leftPos-130, topPos+ 15,
                Component.literal("Internal:"+this.blockEntity.getValues().privateAdress+"/"), this.font);
        adressTextPublic.setPosition(leftPos-adressTextPublic.getWidth(), topPos+5);
        adressTextPrivate.setPosition(leftPos-adressTextPrivate.getWidth(), topPos+15);

        MultiLineTextWidget typeText = new MultiLineTextWidget(leftPos-70, topPos + 47, TYPE_TEXT, this.font);
        MultiLineTextWidget parameterText = new MultiLineTextWidget(leftPos-70, topPos + 110, PARAMETERS_TEXT, this.font);
        MultiLineTextWidget redirectText = new MultiLineTextWidget(leftPos-70, topPos + 79, REDIRECT_TEXT, this.font);
        CycleButton<EnumPoweredType> enumButton = CycleButton.builder(EnumPoweredType::getComponent)
                .withValues(EnumPoweredType.values())
                .withInitialValue(this.poweredType)
                .create(leftPos, topPos + 40, 100, 20, Component.translatable("gui."+ Constants.MOD_ID + ".http_receiver_settings_screen_current_type")
                        , (but, value) -> { this.handlePoweredTypeSwitch(value); });
        this.redirectBox = new EditBox(font, leftPos, topPos + 74, 198, 20, Component.empty());
        this.redirectBox.setMaxLength(1028);
        this.redirectBox.setResponder(text -> {
            redirectUrl = text;
        });

        this.parameterCountInput = new EditBox(this.font, leftPos, topPos + 100, 80, 20, Component.literal("Count"));
        this.parameterCountInput.setResponder(text -> {
            numberOfFieldsAsString = text;
        });
        this.addRenderableWidget(endpointText);
        this.addRenderableWidget(adressTextPublic);
        this.addRenderableWidget(adressTextPrivate);
        this.addRenderableWidget(typeText);
        this.addRenderableWidget(parameterText);
        this.addRenderableWidget(enumButton);
        this.addRenderableWidget(endpoint);
        this.addRenderableWidget(redirectBox);
        this.addRenderableWidget(redirectText);
        this.addRenderableWidget(parameterCountInput);

        if(this.poweredType == EnumPoweredType.TIMER){
            //DRAW ADDITIONAL
            this.timer = new EditBox(font, leftPos+110, topPos + 40, 50, 20, Component.translatable("gui."+ Constants.MOD_ID + ".http_receiver_settings_screen_timer_input"));
            this.timer.setResponder(text -> {
                timerText = text;
            });
            addRenderableWidget(this.timer);

            CycleButton<EnumTimerUnit> enumButton1 = CycleButton.builder(EnumTimerUnit::getComponent)
                    .withValues(EnumTimerUnit.values())
                    .withInitialValue(this.timerUnit)
                    .create(leftPos+170, topPos + 40, 30, 20, Component.translatable("gui."+ Constants.MOD_ID + ".http_receiver_settings_screen_current_timer_unit")
                            , (but, value) -> { this.timerUnit = value; });
            addRenderableWidget(enumButton1);


            this.timer.insertText(this.timerText);
        }

        this.saveParameterCountButton = this.addRenderableWidget(saveParameterCountButton.builder(
                Component.literal("Apply"),(button) -> {
                    if(numberOfFieldsAsString.isEmpty())return;
                    numberOfFields = Integer.parseInt(numberOfFieldsAsString);
                    createInputFields(numberOfFields);
                }).bounds(leftPos+100, topPos + 100, 100, 20).build());
        if(!this.parameterFields.isEmpty()) {
            this.drawParameters();
        }
        this.endpoint.insertText(this.endpointText);
        this.redirectBox.insertText(this.redirectUrl);
    }

    private void drawParameters(){
        this.scrollablePanel = new ScrollableWidget(leftPos, topPos + 130, 100, 60, this.parameterFields);
        this.addRenderableWidget(scrollablePanel);
    }

    @Override
    public boolean mouseScrolled(double $$0, double $$1, double $$2, double $$3) {
        return super.mouseScrolled($$0, $$1, $$2, $$3);
    }

    private void handlePoweredTypeSwitch(EnumPoweredType type){
        this.poweredType = type;
        //REDRAW WIDGETS
        this.clearWidgets();
        this.init();
    }

    private void handleStartButton(Button button){
        if(this.checkValues()){
            //SEND UPDATE PACKET TO SERVER
            HttpReceiverBlockEntity.Values values = blockEntity.getValues();
            values.url = this.endpointText;
            values.poweredType = this.poweredType;
            if(this.timerText != null && !this.timerText.isEmpty())
                values.timer = Float.parseFloat(this.timerText);
            values.timerUnit = this.timerUnit;
            values.redirectClientUrl = redirectUrl;
            if(this.scrollablePanel != null){
                values.parameterMap = this.getParameterValues();
            }
            Services.PACKET_HANDLER.sendPacketToServer(new SUpdateHttpReceiverValuesPacket(
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
