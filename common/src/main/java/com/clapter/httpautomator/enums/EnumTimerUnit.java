package com.clapter.httpautomator.enums;

import com.clapter.httpautomator.Constants;
import net.minecraft.network.chat.Component;

public enum EnumTimerUnit {

    TICKS("gui."+ Constants.MOD_ID + ".timer_unit_ticks", 0),
    SECONDS("gui."+ Constants.MOD_ID + ".timer_unit_seconds", 1);

    private final String text;
    private final int id;

    EnumTimerUnit(String text, int id) {
        this.text = text;
        this.id = id;
    }

    public Component getComponent(){
        return Component.translatable(this.text);
    }

    public static EnumTimerUnit getById(int id){
        for(EnumTimerUnit type : values()){
            if(type.id == id){
                return type;
            }
        }
        return null;
    }

    public int getId(){
        return id;
    }

}
