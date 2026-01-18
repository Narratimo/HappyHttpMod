package no.eira.relay.enums;

import no.eira.relay.Constants;
import net.minecraft.network.chat.Component;

public enum EnumPoweredType {

    SWITCH("gui." + Constants.MOD_ID + ".powered_type_switch", 0),
    TIMER("gui." + Constants.MOD_ID + ".powered_type_timer", 1);

    private final String text;
    private final int id;

    EnumPoweredType(String text, int id) {
        this.text = text;
        this.id = id;
    }

    public Component getComponent() {
        return Component.translatable(this.text);
    }

    public static EnumPoweredType getById(int id) {
        for (EnumPoweredType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        return SWITCH; // Default fallback
    }

    public int getId() {
        return id;
    }
}
