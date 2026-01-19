package no.eira.relay.enums;

import no.eira.relay.Constants;
import net.minecraft.network.chat.Component;

public enum EnumStopBehavior {

    IGNORE("gui." + Constants.MOD_ID + ".stop_ignore", 0),
    STOP("gui." + Constants.MOD_ID + ".stop_stop", 1),
    RESET("gui." + Constants.MOD_ID + ".stop_reset", 2);

    private final String text;
    private final int id;

    EnumStopBehavior(String text, int id) {
        this.text = text;
        this.id = id;
    }

    public Component getComponent() {
        return Component.translatable(this.text);
    }

    public static EnumStopBehavior getById(int id) {
        for (EnumStopBehavior type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        return IGNORE;
    }

    public int getId() {
        return id;
    }
}
