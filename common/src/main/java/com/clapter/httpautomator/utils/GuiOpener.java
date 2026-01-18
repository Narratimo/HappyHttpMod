package com.clapter.httpautomator.utils;

import com.clapter.httpautomator.client.gui.BaseBlockScreen;
import com.clapter.httpautomator.client.gui.HttpReceiverSettingsScreen;
import com.clapter.httpautomator.client.gui.HttpSenderSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashMap;
import java.util.Map;

//WORKAROUND CLASS, SINCE OPENING DIRECTLY FROM PACKET IS NOT ALLOWED (PHYSICAL SERVER CRASHES)
public class GuiOpener {

    private static final Map<String, BaseBlockScreen> guiMap = new HashMap<String, BaseBlockScreen>();

    static {
        guiMap.put("sender", new HttpSenderSettingsScreen(null));
        guiMap.put("receiver", new HttpReceiverSettingsScreen(null, ""));
    }

    public static void openGui(String name, BlockEntity blockEntity){
        BaseBlockScreen screen = guiMap.get(name);
        if(screen == null)return;
        screen.assignEntity(blockEntity);
        Minecraft.getInstance().setScreen(screen);
    }

}
