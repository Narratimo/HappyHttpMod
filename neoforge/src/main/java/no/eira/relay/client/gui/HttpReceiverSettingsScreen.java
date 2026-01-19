package no.eira.relay.client.gui;

import no.eira.relay.Constants;
import no.eira.relay.blockentity.HttpReceiverBlockEntity;
import no.eira.relay.enums.EnumPoweredType;
import no.eira.relay.enums.EnumTimerUnit;
import no.eira.relay.network.packet.SUpdateHttpReceiverValuesPacket;
import no.eira.relay.platform.Services;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.SecureRandom;
import java.util.Enumeration;

public class HttpReceiverSettingsScreen extends Screen {

    private static Component TITLE = Component.translatable("gui."+ Constants.MOD_ID + ".http_receiver_settings_screen");
    private static Component SAVE_TEXT = Component.translatable("gui."+ Constants.MOD_ID + ".http_receiver_startbutton");
    private static Component POWER_MODE_LABEL = Component.translatable("gui."+ Constants.MOD_ID + ".power_mode_label");
    private static Component TIMER_LABEL = Component.translatable("gui."+ Constants.MOD_ID + ".timer_label");
    private static Component PORT_LABEL = Component.translatable("gui."+ Constants.MOD_ID + ".port_label");
    private static Component IP_LABEL = Component.translatable("gui."+ Constants.MOD_ID + ".ip_label");
    private static Component TOKEN_LABEL = Component.translatable("gui."+ Constants.MOD_ID + ".token_label");
    private static Component PLAYER_DETECTION_LABEL = Component.translatable("gui."+ Constants.MOD_ID + ".player_detection_label");
    private static Component RADIUS_LABEL = Component.translatable("gui."+ Constants.MOD_ID + ".radius_label");
    private static final Component COPY_TEXT = Component.literal("\u2398");
    private static final Component GENERATE_TEXT = Component.literal("\u2672");
    private static final Component SHOW_TEXT = Component.literal("\u25C9");
    private static final Component HIDE_TEXT = Component.literal("\u25CE");
    private static final Component ENABLED_TEXT = Component.translatable("gui."+ Constants.MOD_ID + ".enabled");
    private static final Component DISABLED_TEXT = Component.translatable("gui."+ Constants.MOD_ID + ".disabled");
    private static final String TOKEN_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

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
    private EditBox secretTokenInput;
    private Button tokenGenerateButton;
    private Button tokenCopyButton;
    private Button tokenShowButton;
    private Button playerDetectionButton;
    private EditBox radiusInput;
    private boolean tokenVisible = false;
    private String statusMessage = "";
    private int statusColor = 0xAAAAAA;

    private String endpointText;
    private EnumPoweredType poweredType;
    private float timerValue;
    private EnumTimerUnit timerUnit;
    private String secretTokenValue;
    private boolean playerDetection;
    private double playerDetectionRadius;


    public HttpReceiverSettingsScreen(HttpReceiverBlockEntity blockEntity) {
        super(TITLE);
        screenWidth = 176;
        screenHeight = 220;
        this.blockEntity = blockEntity;

        // Initialize from block entity values
        HttpReceiverBlockEntity.Values values = blockEntity.getValues();
        this.poweredType = values.poweredType;
        this.timerValue = values.timer;
        this.timerUnit = values.timerUnit;
        this.secretTokenValue = values.secretToken;
        this.playerDetection = values.playerDetection;
        this.playerDetectionRadius = values.playerDetectionRadius;
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

        // Secret token input
        this.secretTokenInput = new EditBox(font, leftPos + 45, topPos + 58, 88, 20, Component.empty());
        this.secretTokenInput.setResponder(text -> {
            // Only update if in visible/editable mode
            if (tokenVisible) {
                secretTokenValue = text;
            }
        });
        addRenderableWidget(secretTokenInput);

        // Generate token button
        this.tokenGenerateButton = addRenderableWidget(Button.builder(
                GENERATE_TEXT, this::handleGenerateButton)
                .bounds(leftPos + 136, topPos + 58, 20, 20)
                .build()
        );

        // Show/hide token button
        this.tokenShowButton = addRenderableWidget(Button.builder(
                SHOW_TEXT, this::handleShowButton)
                .bounds(leftPos + 158, topPos + 58, 20, 20)
                .build()
        );

        // Copy token button
        this.tokenCopyButton = addRenderableWidget(Button.builder(
                COPY_TEXT, this::handleCopyButton)
                .bounds(leftPos + 180, topPos + 58, 20, 20)
                .build()
        );

        updateTokenMasking();

        // Player detection toggle button
        this.playerDetectionButton = addRenderableWidget(Button.builder(
                this.playerDetection ? ENABLED_TEXT : DISABLED_TEXT, this::handlePlayerDetectionButton)
                .bounds(leftPos + 80, topPos + 84, 60, 20)
                .build()
        );

        // Radius input
        this.radiusInput = new EditBox(font, leftPos + 145, topPos + 84, 55, 20, Component.empty());
        this.radiusInput.setResponder(text -> {
            try {
                playerDetectionRadius = Double.parseDouble(text);
                if (playerDetectionRadius < 1) playerDetectionRadius = 1;
                if (playerDetectionRadius > 64) playerDetectionRadius = 64;
            } catch (NumberFormatException e) {
                // Keep previous value on invalid input
            }
        });
        radiusInput.insertText(String.valueOf((int) playerDetectionRadius));
        addRenderableWidget(radiusInput);

        updatePlayerDetectionVisibility();

        // Save button
        this.saveButton = addRenderableWidget(Button.builder(
                SAVE_TEXT, this::handleSaveButton)
                .bounds(leftPos, topPos + 110, 50, 20)
                .build()
        );

        updateTimerVisibility();
    }

    private void handlePlayerDetectionButton(Button button) {
        this.playerDetection = !this.playerDetection;
        button.setMessage(this.playerDetection ? ENABLED_TEXT : DISABLED_TEXT);
        updatePlayerDetectionVisibility();
    }

    private void updatePlayerDetectionVisibility() {
        this.radiusInput.visible = this.playerDetection;
        this.radiusInput.active = this.playerDetection;
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

    private void handleGenerateButton(Button button) {
        SecureRandom random = new SecureRandom();
        StringBuilder token = new StringBuilder(32);
        for (int i = 0; i < 32; i++) {
            token.append(TOKEN_CHARS.charAt(random.nextInt(TOKEN_CHARS.length())));
        }
        secretTokenValue = token.toString();
        tokenVisible = true;
        updateTokenMasking();
        statusMessage = "Token generated";
        statusColor = 0x55FF55;
    }

    private void handleShowButton(Button button) {
        tokenVisible = !tokenVisible;
        if (tokenVisible) {
            // Restore actual value when revealing
            secretTokenInput.setValue(secretTokenValue != null ? secretTokenValue : "");
        }
        updateTokenMasking();
    }

    private void handleCopyButton(Button button) {
        if (secretTokenValue != null && !secretTokenValue.isEmpty()) {
            minecraft.keyboardHandler.setClipboard(secretTokenValue);
            statusMessage = "Copied to clipboard";
            statusColor = 0x55FF55;
        }
    }

    private void updateTokenMasking() {
        if (tokenVisible) {
            secretTokenInput.setEditable(true);
            secretTokenInput.setValue(secretTokenValue != null ? secretTokenValue : "");
            tokenShowButton.setMessage(HIDE_TEXT);
        } else {
            secretTokenInput.setEditable(false);
            String masked = (secretTokenValue != null && !secretTokenValue.isEmpty())
                ? "●".repeat(Math.min(secretTokenValue.length(), 12))
                : "";
            secretTokenInput.setValue(masked);
            tokenShowButton.setMessage(SHOW_TEXT);
        }
    }

    private void handleSaveButton(Button button){
        if(this.checkValues()){
            // Send update packet to server with all values
            HttpReceiverBlockEntity.Values values = new HttpReceiverBlockEntity.Values();
            values.url = this.endpointText;
            values.poweredType = this.poweredType;
            values.timer = this.timerValue;
            values.timerUnit = this.timerUnit;
            values.secretToken = this.secretTokenValue != null ? this.secretTokenValue : "";
            values.playerDetection = this.playerDetection;
            values.playerDetectionRadius = this.playerDetectionRadius;
            PacketDistributor.sendToServer(new SUpdateHttpReceiverValuesPacket(
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

        // Draw token label
        guiGraphics.drawString(font, TOKEN_LABEL, leftPos, topPos + 64, 0xFFFFFF);

        // Draw player detection label
        guiGraphics.drawString(font, PLAYER_DETECTION_LABEL, leftPos, topPos + 90, 0xFFFFFF);

        // Display port and IP info below save button
        int port = Services.HTTP_CONFIG.getPort();
        String localIp = getLocalIpAddress();

        int infoY = topPos + 137;
        guiGraphics.drawString(font, PORT_LABEL.getString() + ": " + port, leftPos, infoY, 0xAAAAAA);
        guiGraphics.drawString(font, IP_LABEL.getString() + ": " + localIp, leftPos, infoY + 12, 0xAAAAAA);

        // Display status message
        if (!statusMessage.isEmpty()) {
            guiGraphics.drawString(font, statusMessage, leftPos, infoY + 24, statusColor);
        }
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
