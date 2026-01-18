package com.clapter.httpautomator.client.gui;

import com.clapter.httpautomator.client.gui.widgets.ScrollableWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseBlockScreen extends Screen {

    protected List<EditBox> parameterFields;
    protected int parameterFieldsPosX;
    protected int parameterFieldsPosXOffset;
    protected final int screenWidth;
    protected final int screenHeight;
    protected int leftPos;
    protected int topPos;


    protected BaseBlockScreen(Component $$0) {
        super($$0);
        this.screenWidth = 176;
        this.screenHeight = 166;
        this.parameterFields = new ArrayList<>();
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - screenWidth) / 2;
        this.topPos = (this.height - screenHeight) / 2;
    }

    @NotNull
    protected Map<String, String> getParameterValuesFromScrollable(ScrollableWidget scrollablePanel) {
        List<EditBox> input = scrollablePanel.getInputBoxes();
        Map<String, String> parameterMap = new HashMap<String, String>();
        for(int i = 0; i < input.size(); i+=2){
            if(!input.get(i).getValue().isEmpty() && !input.get(i+1).getValue().isEmpty()){
                parameterMap.put(input.get(i).getValue(), input.get(i+1).getValue());
            }
        }
        return parameterMap;
    }

    @Override
    public void resize(Minecraft $$0, int $$1, int $$2) {
        this.repositionEditBoxes();
        super.resize($$0, $$1, $$2);

    }

    private void repositionEditBoxes() {
        if(!this.parameterFields.isEmpty()){
            for(int i = 0; i < this.parameterFields.size(); i+=2){
                this.parameterFields.get(i).setX(this.parameterFieldsPosX);
                this.parameterFields.get(i+1).setX(this.parameterFieldsPosXOffset);
            }

        }
    }

    protected void readParameterMap(Map<String, String> parameterMap) {
        if(!parameterMap.isEmpty()){
            for(Map.Entry<String, String> entry : parameterMap.entrySet()){
                EditBox parBox = new EditBox(this.font, parameterFieldsPosX, topPos + 100, 50, 20, Component.literal("Parameter"));
                EditBox valBox = new EditBox(this.font, parameterFieldsPosXOffset, topPos + 100, 50, 20, Component.literal("Value"));
                this.parameterFields.add(parBox);
                this.parameterFields.add(valBox);
                parBox.setValue(entry.getKey());
                valBox.setValue(entry.getValue());

            }
        }
    }

    protected void createInputFields(int numberOfFields){
        List<EditBox> containigBox = new ArrayList<>(this.parameterFields);
        this.parameterFields.clear();
        for(int i = 0; i < numberOfFields; i++){
            EditBox parBox = new EditBox(this.font, leftPos+40, topPos + 100, 50, 20, Component.literal("Parameter"));
            EditBox valBox = new EditBox(this.font, leftPos + 130, topPos + 100, 50, 20, Component.literal("Value"));
            this.parameterFields.add(parBox);
            this.parameterFields.add(valBox);
            if(containigBox.size()/2 > i){
                parBox.setValue(containigBox.get(i).getValue());
                valBox.setValue(containigBox.get(i+1).getValue());
            }
        }
        this.clearWidgets();
        this.init();
    }

    public void assignEntity(BlockEntity entity){

    }

}
