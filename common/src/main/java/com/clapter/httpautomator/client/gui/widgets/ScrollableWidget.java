package com.clapter.httpautomator.client.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ScrollableWidget extends AbstractWidget {

    private final List<EditBox> inputBoxes;
    private int scrollOffset = 0;
    private static final int ITEM_HEIGHT = 30;
    private static final int VIEW_HEIGHT = 60;

    public ScrollableWidget(int x, int y, int width, int height, List<EditBox> list) {
        super(x, y, width, height, Component.empty());
        this.inputBoxes = list;
        //this.firstInputBox = new EditBox(this.font, x, y, width, 20, Component.literal("First Input"));
        //this.secondInputBox = new EditBox(minecraft.font, x, y + ITEM_HEIGHT, width, 20, Component.literal("Second Input"));
    }

    @Override
    public void renderWidget(GuiGraphics var1, int mouseX, int mouseY, float partialTick) {
        int startY = this.getY() - this.scrollOffset;

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        for(int i = 0; i < this.inputBoxes.size(); i+=2){
            int itemY = startY + (i / 2) * ITEM_HEIGHT;
            //CLIP ALL INVISIBLE ITEMS
            //TODO: IMPLEMENT THIS WITH GLSL SCISORS
            if (itemY >= this.getY() + this.height || itemY + ITEM_HEIGHT <= this.getY()) {
                continue;
            }
            EditBox firstInputBox = inputBoxes.get(i);

            var1.drawString(Minecraft.getInstance().font, "Param:", this.getX(), itemY, -1);
            firstInputBox.setY(itemY);
            firstInputBox.render(var1, mouseX, mouseY, partialTick);

            if (i + 1 < inputBoxes.size()) {
                EditBox secondInputBox = inputBoxes.get(i + 1);
                var1.drawString(Minecraft.getInstance().font, "Val:", this.getX()+ 100, itemY, -1);
                secondInputBox.setY(itemY);
                secondInputBox.render(var1, mouseX, mouseY, partialTick);
            }

        }
    }

    public void scroll(int amount) {
        this.scrollOffset = Math.max(0, Math.min(this.scrollOffset + amount, inputBoxes.size() * ITEM_HEIGHT/2));
    }

    @Override
    public boolean mouseScrolled(double x,double mouseX, double mouseY, double delta) {
        this.scroll((int) -delta * ITEM_HEIGHT / 2);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for(EditBox box : this.inputBoxes){
            box.setFocused(false);
        }
        for (EditBox inputBox : inputBoxes) {
            if (inputBox.mouseClicked(mouseX, mouseY, button)) {
                inputBox.setFocused(true);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        for (EditBox inputBox : inputBoxes) {
            if (inputBox.keyReleased(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        for (EditBox inputBox : inputBoxes) {
            if (inputBox.isFocused() && inputBox.charTyped(codePoint, modifiers)) {
                return true;
            }
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (EditBox inputBox : inputBoxes) {
            if (inputBox.isFocused() && inputBox.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }

    public void resize() {

    }

    public List<EditBox> getInputBoxes(){
        return this.inputBoxes;
    }

}
