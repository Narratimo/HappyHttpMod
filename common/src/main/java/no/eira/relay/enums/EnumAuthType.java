package no.eira.relay.enums;

import no.eira.relay.Constants;
import net.minecraft.network.chat.Component;

public enum EnumAuthType {

    NONE("gui." + Constants.MOD_ID + ".auth_type_none", 0),
    BEARER("gui." + Constants.MOD_ID + ".auth_type_bearer", 1),
    BASIC("gui." + Constants.MOD_ID + ".auth_type_basic", 2),
    CUSTOM_HEADER("gui." + Constants.MOD_ID + ".auth_type_custom", 3);

    private final String text;
    private final int id;

    EnumAuthType(String text, int id) {
        this.text = text;
        this.id = id;
    }

    public Component getComponent() {
        return Component.translatable(this.text);
    }

    public static EnumAuthType getById(int id) {
        for (EnumAuthType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        return NONE; // Default fallback
    }

    public int getId() {
        return id;
    }
}
