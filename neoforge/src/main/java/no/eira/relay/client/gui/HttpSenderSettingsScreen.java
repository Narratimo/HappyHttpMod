package no.eira.relay.client.gui;

import no.eira.relay.Constants;
import no.eira.relay.blockentity.HttpSenderBlockEntity;
import no.eira.relay.enums.EnumAuthType;
import no.eira.relay.enums.EnumHttpMethod;
import no.eira.relay.enums.EnumPoweredType;
import no.eira.relay.enums.EnumTimerUnit;
import no.eira.relay.network.packet.SUpdateHttpSenderValuesPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

public class HttpSenderSettingsScreen extends Screen {

    private static final Component TITLE = Component.translatable("gui." + Constants.MOD_ID + ".http_sender_settings_screen");
    private static final Component SAVE_TEXT = Component.translatable("gui." + Constants.MOD_ID + ".save_button");
    private static final Component URL_LABEL = Component.translatable("gui." + Constants.MOD_ID + ".url_label");
    private static final Component METHOD_LABEL = Component.translatable("gui." + Constants.MOD_ID + ".method_label");
    private static final Component TEST_TEXT = Component.translatable("gui." + Constants.MOD_ID + ".test_button");
    private static final Component AUTH_LABEL = Component.translatable("gui." + Constants.MOD_ID + ".auth_label");
    private static final Component DISCORD_TEXT = Component.translatable("gui." + Constants.MOD_ID + ".discord_button");

    private final int screenWidth;
    private final int screenHeight;
    private int leftPos;
    private int topPos;
    private final HttpSenderBlockEntity blockEntity;

    private Button saveButton;
    private Button testButton;
    private Button discordButton;
    private EditBox urlInput;
    private CycleButton<EnumHttpMethod> methodButton;
    private Button powerModeButton;
    private EditBox timerInput;
    private Button timerUnitButton;
    private Button authTypeButton;
    private EditBox authValueInput;
    private EditBox customHeaderNameInput;
    private EditBox customHeaderValueInput;

    private String urlText;
    private String testResult = "";
    private int testResultColor = 0xAAAAAA;
    private EnumHttpMethod httpMethod;
    private EnumPoweredType poweredType;
    private float timerValue;
    private EnumTimerUnit timerUnit;
    private EnumAuthType authType;
    private String authValue;
    private String customHeaderName;
    private String customHeaderValue;

    public HttpSenderSettingsScreen(HttpSenderBlockEntity blockEntity) {
        super(TITLE);
        screenWidth = 250;
        screenHeight = 210;
        this.blockEntity = blockEntity;

        // Initialize from block entity values
        HttpSenderBlockEntity.Values values = blockEntity.getValues();
        this.urlText = values.url;
        this.httpMethod = values.httpMethod;
        this.poweredType = values.poweredType;
        this.timerValue = values.timer;
        this.timerUnit = values.timerUnit;
        this.authType = values.authType;
        this.authValue = values.authValue;
        this.customHeaderName = values.customHeaderName;
        this.customHeaderValue = values.customHeaderValue;
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

        // Power mode button
        this.powerModeButton = addRenderableWidget(Button.builder(
                this.poweredType.getComponent(), this::handlePowerModeButton)
                .bounds(leftPos + 10, topPos + 90, 80, 20)
                .build()
        );

        // Timer value input (only visible when TIMER mode)
        this.timerInput = new EditBox(font, leftPos + 95, topPos + 90, 50, 20, Component.empty());
        this.timerInput.setResponder(text -> {
            try {
                timerValue = Float.parseFloat(text);
            } catch (NumberFormatException e) {
                // Keep previous value on invalid input
            }
        });
        timerInput.setValue(String.valueOf(timerValue));
        addRenderableWidget(timerInput);

        // Timer unit button (only visible when TIMER mode)
        this.timerUnitButton = addRenderableWidget(Button.builder(
                this.timerUnit.getComponent(), this::handleTimerUnitButton)
                .bounds(leftPos + 150, topPos + 90, 58, 20)
                .build()
        );

        // Auth type button
        this.authTypeButton = addRenderableWidget(Button.builder(
                this.authType.getComponent(), this::handleAuthTypeButton)
                .bounds(leftPos + 10, topPos + 120, 80, 20)
                .build()
        );

        // Auth value input (token or user:pass)
        this.authValueInput = new EditBox(font, leftPos + 95, topPos + 120, 145, 20, Component.empty());
        this.authValueInput.setMaxLength(256);
        this.authValueInput.setResponder(text -> authValue = text);
        this.authValueInput.setValue(this.authValue);
        addRenderableWidget(authValueInput);

        // Custom header name input
        this.customHeaderNameInput = new EditBox(font, leftPos + 10, topPos + 145, 100, 20, Component.empty());
        this.customHeaderNameInput.setMaxLength(64);
        this.customHeaderNameInput.setResponder(text -> customHeaderName = text);
        this.customHeaderNameInput.setValue(this.customHeaderName);
        this.customHeaderNameInput.setHint(Component.literal("Header name"));
        addRenderableWidget(customHeaderNameInput);

        // Custom header value input
        this.customHeaderValueInput = new EditBox(font, leftPos + 115, topPos + 145, 125, 20, Component.empty());
        this.customHeaderValueInput.setMaxLength(256);
        this.customHeaderValueInput.setResponder(text -> customHeaderValue = text);
        this.customHeaderValueInput.setValue(this.customHeaderValue);
        this.customHeaderValueInput.setHint(Component.literal("Header value"));
        addRenderableWidget(customHeaderValueInput);

        // Save button
        this.saveButton = addRenderableWidget(Button.builder(
                SAVE_TEXT, this::handleSaveButton)
                .bounds(leftPos + 10, topPos + 175, 70, 20)
                .build()
        );

        // Test button
        this.testButton = addRenderableWidget(Button.builder(
                TEST_TEXT, this::handleTestButton)
                .bounds(leftPos + 85, topPos + 175, 70, 20)
                .build()
        );

        // Discord preset button
        this.discordButton = addRenderableWidget(Button.builder(
                DISCORD_TEXT, this::handleDiscordButton)
                .bounds(leftPos + 160, topPos + 175, 80, 20)
                .build()
        );

        updateTimerVisibility();
        updateAuthVisibility();
    }

    private void updateTimerVisibility() {
        boolean isTimer = this.poweredType == EnumPoweredType.TIMER;
        this.timerInput.visible = isTimer;
        this.timerInput.active = isTimer;
        this.timerUnitButton.visible = isTimer;
        this.timerUnitButton.active = isTimer;
    }

    private void updateAuthVisibility() {
        boolean showAuthValue = this.authType != EnumAuthType.NONE;
        boolean showCustomHeader = this.authType == EnumAuthType.CUSTOM_HEADER;

        this.authValueInput.visible = showAuthValue && !showCustomHeader;
        this.authValueInput.active = showAuthValue && !showCustomHeader;
        this.customHeaderNameInput.visible = showCustomHeader;
        this.customHeaderNameInput.active = showCustomHeader;
        this.customHeaderValueInput.visible = showCustomHeader;
        this.customHeaderValueInput.active = showCustomHeader;
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

    private void handleAuthTypeButton(Button button) {
        // Cycle through auth types
        EnumAuthType[] types = EnumAuthType.values();
        int nextIndex = (this.authType.ordinal() + 1) % types.length;
        this.authType = types[nextIndex];
        button.setMessage(this.authType.getComponent());
        updateAuthVisibility();
    }

    private void handleDiscordButton(Button button) {
        // Set method to POST (required for Discord webhooks)
        this.httpMethod = EnumHttpMethod.POST;
        this.methodButton.setValue(EnumHttpMethod.POST);

        // Discord webhooks don't need auth (URL contains token)
        this.authType = EnumAuthType.NONE;
        this.authTypeButton.setMessage(this.authType.getComponent());
        updateAuthVisibility();

        // Show hint
        testResult = "Set to POST. Add 'content' parameter for message.";
        testResultColor = 0x55FFFF;
    }

    private void handleSaveButton(Button button) {
        if (this.checkValues()) {
            HttpSenderBlockEntity.Values values = new HttpSenderBlockEntity.Values();
            values.url = this.urlText;
            values.httpMethod = this.httpMethod;
            values.poweredType = this.poweredType;
            values.timer = this.timerValue;
            values.timerUnit = this.timerUnit;
            values.authType = this.authType;
            values.authValue = this.authValue;
            values.customHeaderName = this.customHeaderName;
            values.customHeaderValue = this.customHeaderValue;
            // Preserve parameter map from block entity
            values.parameterMap = blockEntity.getValues().parameterMap;

            PacketDistributor.sendToServer(new SUpdateHttpSenderValuesPacket(
                    this.blockEntity.getBlockPos(),
                    values));

            this.onClose();
        }
    }

    private void handleTestButton(Button button) {
        if (urlText == null || urlText.isEmpty()) {
            testResult = "Error: URL is empty";
            testResultColor = 0xFF5555;
            return;
        }

        testResult = "Testing...";
        testResultColor = 0xFFFF55;
        testButton.active = false;

        CompletableFuture.runAsync(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                        .uri(new URI(urlText));

                // Add auth headers for testing
                switch (authType) {
                    case BEARER -> {
                        if (authValue != null && !authValue.isEmpty()) {
                            requestBuilder.header("Authorization", "Bearer " + authValue);
                        }
                    }
                    case BASIC -> {
                        if (authValue != null && !authValue.isEmpty()) {
                            String encoded = Base64.getEncoder().encodeToString(
                                authValue.getBytes(StandardCharsets.UTF_8));
                            requestBuilder.header("Authorization", "Basic " + encoded);
                        }
                    }
                    case CUSTOM_HEADER -> {
                        if (customHeaderName != null && !customHeaderName.isEmpty()) {
                            requestBuilder.header(customHeaderName, customHeaderValue != null ? customHeaderValue : "");
                        }
                    }
                }

                if (httpMethod == EnumHttpMethod.POST) {
                    requestBuilder.header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString("{}", StandardCharsets.UTF_8));
                } else {
                    requestBuilder.GET();
                }

                HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
                int status = response.statusCode();

                if (status >= 200 && status < 300) {
                    testResult = "OK: " + status;
                    testResultColor = 0x55FF55;
                } else {
                    testResult = "Error: " + status;
                    testResultColor = 0xFF5555;
                }
            } catch (Exception e) {
                testResult = "Error: " + e.getMessage();
                testResultColor = 0xFF5555;
            }
            testButton.active = true;
        });
    }

    private boolean checkValues() {
        return urlText != null && !urlText.isEmpty();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawString(font, URL_LABEL, leftPos + 10, topPos + 18, 0xFFFFFF);
        guiGraphics.drawString(font, AUTH_LABEL, leftPos + 10, topPos + 108, 0xFFFFFF);

        // Display test result below buttons
        if (!testResult.isEmpty()) {
            guiGraphics.drawString(font, testResult, leftPos + 10, topPos + 198, testResultColor);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
