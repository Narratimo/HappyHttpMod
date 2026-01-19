package no.eira.relay.client.gui;

import no.eira.relay.Constants;
import no.eira.relay.blockentity.SceneSequencerBlockEntity;
import no.eira.relay.blockentity.SceneSequencerBlockEntity.SequenceStep;
import no.eira.relay.enums.*;
import no.eira.relay.network.packet.SUpdateSceneSequencerValuesPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SceneSequencerSettingsScreen extends Screen {

    private static final Component TITLE = Component.translatable("gui." + Constants.MOD_ID + ".sequencer_title");
    private static final Component SAVE_TEXT = Component.translatable("gui." + Constants.MOD_ID + ".save_button");
    private static final Component ADD_STEP_TEXT = Component.translatable("gui." + Constants.MOD_ID + ".add_step");
    private static final Component REMOVE_STEP_TEXT = Component.translatable("gui." + Constants.MOD_ID + ".remove_step");
    private static final Component URL_LABEL = Component.translatable("gui." + Constants.MOD_ID + ".url_label");
    private static final Component DELAY_LABEL = Component.translatable("gui." + Constants.MOD_ID + ".delay");
    private static final Component CONDITION_LABEL = Component.translatable("gui." + Constants.MOD_ID + ".condition");
    private static final Component LOOP_TEXT = Component.translatable("gui." + Constants.MOD_ID + ".loop");

    private final int screenWidth;
    private final int screenHeight;
    private int leftPos;
    private int topPos;
    private final SceneSequencerBlockEntity blockEntity;

    // Step list buttons (left side)
    private final List<Button> stepButtons = new ArrayList<>();
    private Button addStepButton;
    private Button removeStepButton;

    // Step editor (right side)
    private EditBox urlInput;
    private CycleButton<EnumHttpMethod> methodButton;
    private EditBox delayInput;
    private CycleButton<EnumStepCondition> conditionButton;
    private CycleButton<EnumAuthType> authTypeButton;
    private EditBox authValueInput;

    // Global settings
    private CycleButton<EnumStopBehavior> stopBehaviorButton;
    private Button loopButton;
    private Button saveButton;

    // Data
    private List<SequenceStep> steps;
    private int selectedStepIndex = 0;
    private EnumStopBehavior stopBehavior;
    private boolean looping;

    private static final int MAX_VISIBLE_STEPS = 8;

    public SceneSequencerSettingsScreen(SceneSequencerBlockEntity blockEntity) {
        super(TITLE);
        screenWidth = 320;
        screenHeight = 220;
        this.blockEntity = blockEntity;

        // Copy values from block entity
        SceneSequencerBlockEntity.Values values = blockEntity.getValues();
        this.steps = new ArrayList<>();
        for (SequenceStep step : values.steps) {
            this.steps.add(step.copy());
        }
        this.stopBehavior = values.stopBehavior;
        this.looping = values.looping;

        // Ensure at least one step exists for editing
        if (this.steps.isEmpty()) {
            this.steps.add(new SequenceStep());
        }
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - screenWidth) / 2;
        this.topPos = (this.height - screenHeight) / 2;

        int leftPanelX = leftPos + 5;
        int rightPanelX = leftPos + 110;
        int y = topPos + 20;

        // === Left Panel: Step List ===
        updateStepButtons();

        // Add/Remove buttons at bottom of step list
        int stepListBottom = topPos + 20 + (MAX_VISIBLE_STEPS * 18);
        addStepButton = Button.builder(ADD_STEP_TEXT, this::handleAddStep)
            .pos(leftPanelX, stepListBottom + 5)
            .size(45, 16)
            .build();
        addRenderableWidget(addStepButton);

        removeStepButton = Button.builder(REMOVE_STEP_TEXT, this::handleRemoveStep)
            .pos(leftPanelX + 50, stepListBottom + 5)
            .size(45, 16)
            .build();
        addRenderableWidget(removeStepButton);

        // === Right Panel: Step Editor ===
        y = topPos + 20;

        // URL input
        urlInput = new EditBox(this.font, rightPanelX, y, 195, 16, Component.literal("URL"));
        urlInput.setMaxLength(256);
        urlInput.setValue(getCurrentStep().url);
        urlInput.setResponder(s -> getCurrentStep().url = s);
        addRenderableWidget(urlInput);
        y += 22;

        // Method and Delay on same row
        methodButton = CycleButton.<EnumHttpMethod>builder(EnumHttpMethod::getComponent)
            .withValues(EnumHttpMethod.values())
            .withInitialValue(getCurrentStep().httpMethod)
            .create(rightPanelX, y, 60, 16, Component.empty(), (btn, val) -> getCurrentStep().httpMethod = val);
        addRenderableWidget(methodButton);

        delayInput = new EditBox(this.font, rightPanelX + 65, y, 50, 16, DELAY_LABEL);
        delayInput.setMaxLength(6);
        delayInput.setValue(String.valueOf(getCurrentStep().delayTicks));
        delayInput.setResponder(s -> {
            try {
                getCurrentStep().delayTicks = Integer.parseInt(s);
            } catch (NumberFormatException e) {
                // ignore invalid input
            }
        });
        addRenderableWidget(delayInput);
        y += 22;

        // Condition
        conditionButton = CycleButton.<EnumStepCondition>builder(EnumStepCondition::getComponent)
            .withValues(EnumStepCondition.values())
            .withInitialValue(getCurrentStep().condition)
            .create(rightPanelX, y, 195, 16, CONDITION_LABEL, (btn, val) -> getCurrentStep().condition = val);
        addRenderableWidget(conditionButton);
        y += 22;

        // Auth type
        authTypeButton = CycleButton.<EnumAuthType>builder(EnumAuthType::getComponent)
            .withValues(EnumAuthType.values())
            .withInitialValue(getCurrentStep().authType)
            .create(rightPanelX, y, 100, 16, Component.empty(), (btn, val) -> {
                getCurrentStep().authType = val;
                updateAuthVisibility();
            });
        addRenderableWidget(authTypeButton);
        y += 22;

        // Auth value input
        authValueInput = new EditBox(this.font, rightPanelX, y, 195, 16, Component.literal("Auth"));
        authValueInput.setMaxLength(256);
        authValueInput.setValue(getCurrentStep().authValue);
        authValueInput.setResponder(s -> getCurrentStep().authValue = s);
        addRenderableWidget(authValueInput);
        y += 28;

        // === Global Settings ===
        // Stop behavior
        stopBehaviorButton = CycleButton.<EnumStopBehavior>builder(EnumStopBehavior::getComponent)
            .withValues(EnumStopBehavior.values())
            .withInitialValue(stopBehavior)
            .create(rightPanelX, y, 120, 16, Component.empty(), (btn, val) -> stopBehavior = val);
        addRenderableWidget(stopBehaviorButton);

        // Loop toggle
        loopButton = Button.builder(getLoopText(), this::handleLoopToggle)
            .pos(rightPanelX + 125, y)
            .size(70, 16)
            .build();
        addRenderableWidget(loopButton);
        y += 25;

        // Save button
        saveButton = Button.builder(SAVE_TEXT, this::handleSave)
            .pos(rightPanelX + 60, y)
            .size(80, 20)
            .build();
        addRenderableWidget(saveButton);

        updateAuthVisibility();
    }

    private void updateStepButtons() {
        // Remove old step buttons
        for (Button btn : stepButtons) {
            removeWidget(btn);
        }
        stepButtons.clear();

        int leftPanelX = leftPos + 5;
        int y = topPos + 20;

        // Create step buttons
        for (int i = 0; i < Math.min(steps.size(), MAX_VISIBLE_STEPS); i++) {
            final int index = i;
            String label = (i + 1) + ": " + truncateUrl(steps.get(i).url, 10);
            Button btn = Button.builder(Component.literal(label), b -> selectStep(index))
                .pos(leftPanelX, y + (i * 18))
                .size(95, 16)
                .build();
            stepButtons.add(btn);
            addRenderableWidget(btn);
        }
    }

    private String truncateUrl(String url, int maxLen) {
        if (url.isEmpty()) return "(empty)";
        if (url.length() <= maxLen) return url;
        return url.substring(0, maxLen) + "...";
    }

    private void selectStep(int index) {
        // Save current step values before switching
        if (selectedStepIndex < steps.size()) {
            saveCurrentStepFromUI();
        }

        selectedStepIndex = index;
        loadStepToUI();
        updateStepButtons();
    }

    private void saveCurrentStepFromUI() {
        SequenceStep step = getCurrentStep();
        step.url = urlInput.getValue();
        step.httpMethod = methodButton.getValue();
        try {
            step.delayTicks = Integer.parseInt(delayInput.getValue());
        } catch (NumberFormatException e) {
            step.delayTicks = 20;
        }
        step.condition = conditionButton.getValue();
        step.authType = authTypeButton.getValue();
        step.authValue = authValueInput.getValue();
    }

    private void loadStepToUI() {
        SequenceStep step = getCurrentStep();
        urlInput.setValue(step.url);
        methodButton.setValue(step.httpMethod);
        delayInput.setValue(String.valueOf(step.delayTicks));
        conditionButton.setValue(step.condition);
        authTypeButton.setValue(step.authType);
        authValueInput.setValue(step.authValue);
        updateAuthVisibility();
    }

    private SequenceStep getCurrentStep() {
        if (selectedStepIndex >= steps.size()) {
            selectedStepIndex = steps.size() - 1;
        }
        if (selectedStepIndex < 0) {
            selectedStepIndex = 0;
        }
        if (steps.isEmpty()) {
            steps.add(new SequenceStep());
        }
        return steps.get(selectedStepIndex);
    }

    private void handleAddStep(Button button) {
        if (steps.size() >= MAX_VISIBLE_STEPS) return;
        saveCurrentStepFromUI();
        steps.add(new SequenceStep());
        selectedStepIndex = steps.size() - 1;
        loadStepToUI();
        updateStepButtons();
    }

    private void handleRemoveStep(Button button) {
        if (steps.size() <= 1) return;
        steps.remove(selectedStepIndex);
        if (selectedStepIndex >= steps.size()) {
            selectedStepIndex = steps.size() - 1;
        }
        loadStepToUI();
        updateStepButtons();
    }

    private void handleLoopToggle(Button button) {
        looping = !looping;
        loopButton.setMessage(getLoopText());
    }

    private Component getLoopText() {
        return Component.literal(looping ? "Loop: ON" : "Loop: OFF");
    }

    private void updateAuthVisibility() {
        boolean showAuth = authTypeButton.getValue() != EnumAuthType.NONE;
        authValueInput.visible = showAuth;
        authValueInput.active = showAuth;
    }

    private void handleSave(Button button) {
        saveCurrentStepFromUI();

        SceneSequencerBlockEntity.Values values = new SceneSequencerBlockEntity.Values();
        values.steps = new ArrayList<>();
        for (SequenceStep step : steps) {
            values.steps.add(step.copy());
        }
        values.stopBehavior = stopBehavior;
        values.looping = looping;

        PacketDistributor.sendToServer(new SUpdateSceneSequencerValuesPacket(
            blockEntity.getBlockPos(),
            values
        ));

        this.onClose();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        // Draw background panel
        guiGraphics.fill(leftPos, topPos, leftPos + screenWidth, topPos + screenHeight, 0xCC000000);
        guiGraphics.renderOutline(leftPos, topPos, screenWidth, screenHeight, 0xFF555555);

        // Draw title
        guiGraphics.drawCenteredString(this.font, this.title, leftPos + screenWidth / 2, topPos + 5, 0xFFFFFF);

        // Draw section divider
        guiGraphics.fill(leftPos + 105, topPos + 15, leftPos + 106, topPos + screenHeight - 5, 0xFF555555);

        // Labels
        int rightPanelX = leftPos + 110;
        guiGraphics.drawString(this.font, "Steps", leftPos + 35, topPos + 10, 0xAAAAAA);
        guiGraphics.drawString(this.font, "Step " + (selectedStepIndex + 1) + " of " + steps.size(), rightPanelX, topPos + 10, 0xAAAAAA);

        // Label for delay
        guiGraphics.drawString(this.font, "ticks", rightPanelX + 120, topPos + 44, 0x888888);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
