package no.eira.relay.client.gui;

import no.eira.relay.Constants;
import no.eira.relay.blockentity.HttpReceiverBlockEntity;
import no.eira.relay.enums.EnumPoweredType;
import no.eira.relay.enums.EnumTimerUnit;
import no.eira.relay.network.packet.SUpdateHttpReceiverValuesPacket;
import no.eira.relay.platform.Services;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class HttpReceiverSettingsScreen extends Screen {

    private static Component TITLE = Component.translatable("gui."+ Constants.MOD_ID + ".http_receiver_settings_screen");
    private static Component SAVE_TEXT = Component.translatable("gui."+ Constants.MOD_ID + ".http_receiver_startbutton");
    private static Component POWER_MODE_LABEL = Component.translatable("gui."+ Constants.MOD_ID + ".power_mode_label");
    private static Component TIMER_LABEL = Component.translatable("gui."+ Constants.MOD_ID + ".timer_label");
    private static Component PORT_LABEL = Component.translatable("gui."+ Constants.MOD_ID + ".port_label");
    private static Component IP_LABEL = Component.translatable("gui."+ Constants.MOD_ID + ".ip_label");

    private final int screenWidth;
    private final int screenHeight;
    private int leftPos;
    private int topPos;
    private HttpReceiverBlockEntity blockEntity;

    private Button saveButton;
    private EditBox endpoint;
    private Button powerModeButton;
    private EditBox timerInput;
    private Button timerUnitButton;

    private String endpointText;
    private EnumPoweredType poweredType;
    private float timerValue;
    private EnumTimerUnit timerUnit;


    public HttpReceiverSettingsScreen(HttpReceiverBlockEntity blockEntity) {
        super(TITLE);
        screenWidth = 176;
        screenHeight = 166;
        this.blockEntity = blockEntity;

        // Initialize from block entity values
        HttpReceiverBlockEntity.Values values = blockEntity.getValues();
        this.poweredType = values.poweredType;
        this.timerValue = values.timer;
        this.timerUnit = values.timerUnit;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - screenWidth) / 2;
        this.topPos = (this.height - screenHeight) / 2;

        // URL endpoint input
        this.endpoint = new EditBox(font, leftPos, topPos + 6, 198, 20, Component.empty());
        this.endpoint.setResponder(text -> {
            endpointText = text;
        });
        endpoint.insertText(blockEntity.getValues().url);
        addRenderableWidget(endpoint);

        // Power mode button
        this.powerModeButton = addRenderableWidget(Button.builder(
                this.poweredType.getComponent(), this::handlePowerModeButton)
                .bounds(leftPos, topPos + 32, 80, 20)
                .build()
        );

        // Timer value input (only visible when TIMER mode)
        this.timerInput = new EditBox(font, leftPos + 85, topPos + 32, 50, 20, Component.empty());
        this.timerInput.setResponder(text -> {
            try {
                timerValue = Float.parseFloat(text);
            } catch (NumberFormatException e) {
                // Keep previous value on invalid input
            }
        });
        timerInput.insertText(String.valueOf(timerValue));
        addRenderableWidget(timerInput);

        // Timer unit button (only visible when TIMER mode)
        this.timerUnitButton = addRenderableWidget(Button.builder(
                this.timerUnit.getComponent(), this::handleTimerUnitButton)
                .bounds(leftPos + 140, topPos + 32, 58, 20)
                .build()
        );

        // Save button
        this.saveButton = addRenderableWidget(Button.builder(
                SAVE_TEXT, this::handleSaveButton)
                .bounds(leftPos, topPos + 58, 50, 20)
                .build()
        );

        updateTimerVisibility();
    }

    private void updateTimerVisibility() {
        boolean isTimer = this.poweredType == EnumPoweredType.TIMER;
        this.timerInput.visible = isTimer;
        this.timerInput.active = isTimer;
        this.timerUnitButton.visible = isTimer;
        this.timerUnitButton.active = isTimer;
    }

    private void handlePowerModeButton(Button button) {
        // Cycle through power modes
        EnumPoweredType[] types = EnumPoweredType.values();
        int nextIndex = (this.poweredType.ordinal() + 1) % types.length;
        this.poweredType = types[nextIndex];
        button.setMessage(this.poweredType.getComponent());
        updateTimerVisibility();
    }

    private void handleTimerUnitButton(Button button) {
        // Cycle through timer units
        EnumTimerUnit[] units = EnumTimerUnit.values();
        int nextIndex = (this.timerUnit.ordinal() + 1) % units.length;
        this.timerUnit = units[nextIndex];
        button.setMessage(this.timerUnit.getComponent());
    }

    private void handleSaveButton(Button button){
        if(this.checkValues()){
            // Send update packet to server with all values
            HttpReceiverBlockEntity.Values values = new HttpReceiverBlockEntity.Values();
            values.url = this.endpointText;
            values.poweredType = this.poweredType;
            values.timer = this.timerValue;
            values.timerUnit = this.timerUnit;
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

        // Display port and IP info below save button
        int port = Services.HTTP_CONFIG.getPort();
        String localIp = getLocalIpAddress();

        int infoY = topPos + 85;
        guiGraphics.drawString(font, PORT_LABEL.getString() + ": " + port, leftPos, infoY, 0xAAAAAA);
        guiGraphics.drawString(font, IP_LABEL.getString() + ": " + localIp, leftPos, infoY + 12, 0xAAAAAA);
    }

    private String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof java.net.Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            // Fallback to localhost
        }
        return "127.0.0.1";
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
