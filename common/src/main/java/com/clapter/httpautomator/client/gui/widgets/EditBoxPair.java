package com.clapter.httpautomator.client.gui.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class EditBoxPair extends AbstractWidget {

    private EditBox box1;
    private EditBox box2;

    public EditBoxPair(Font font, int $$0, int $$1, int $$2, int $$3, Component $$4) {
        super($$0, $$1, $$2, $$3, $$4);
        this.box1 = new EditBox(font, $$0, $$1, $$4);
        this.box2 = new EditBox(font, $$0, $$1, $$4);
    }

    @Override
    protected void renderWidget(GuiGraphics var1, int var2, int var3, float var4) {
        System.out.println(var2);
        box1.render(var1, var2, var3, var4);
        box2.render(var1, var2, var3, var4);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput var1) {

    }
}
