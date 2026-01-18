package no.eira.relay.client.gui;

import no.eira.relay.Constants;
import no.eira.relay.blockentity.HttpReceiverBlockEntity;
import no.eira.relay.network.packet.SUpdateHttpReceiverValuesPacket;
import no.eira.relay.platform.Services;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultilineTextField;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;

public class HttpReceiverSettingsScreen extends Screen {

    private static Component TITLE = Component.translatable("gui."+ Constants.MOD_ID + ".http_receiver_settings_screen");
    private static Component SAVE_TEXT = Component.translatable("gui."+ Constants.MOD_ID + ".http_receiver_startbutton");

    private final int screenWidth;
    private final int screenHeight;
    private int leftPos;
    private int topPos;
    private HttpReceiverBlockEntity blockEntity;

    private Button saveButton;
    private EditBox endpoint;

    private String endpointText;


    public HttpReceiverSettingsScreen(HttpReceiverBlockEntity blockEntity) {
        super(TITLE);
        screenWidth = 176;
        screenHeight = 166;
        this.blockEntity = blockEntity;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - screenWidth) / 2;
        this.topPos = (this.height - screenHeight) / 2;
        this.saveButton = addRenderableWidget(Button.builder(
                SAVE_TEXT, this::handleSaveButton)
                .bounds(leftPos, topPos + 10, 50, 20)
                .build()
        );
        this.endpoint = new EditBox(font, leftPos + 50, topPos + 30 - 24, 198, 20, Component.empty());
        this.endpoint.setResponder(text -> {
            endpointText = text;
        });
        endpoint.insertText(blockEntity.getValues().url);
        addRenderableWidget(endpoint);

    }

    private void handleSaveButton(Button button){
        if(this.checkValues()){
            // Send update packet to server
            HttpReceiverBlockEntity.Values values = blockEntity.getValues();
            values.url = this.endpointText;
            Services.PACKET_HANDLER.sendPacketToServer(new SUpdateHttpReceiverValuesPacket(
                    this.blockEntity.getBlockPos(),
                    values));
            
            // Close the screen after saving
            this.onClose();
        }
    }

    private boolean checkValues(){
        return endpointText != null && !endpointText.isEmpty();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
