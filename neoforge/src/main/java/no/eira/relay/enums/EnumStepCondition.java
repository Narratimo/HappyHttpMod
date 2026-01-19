package no.eira.relay.enums;

import no.eira.relay.Constants;
import net.minecraft.network.chat.Component;

public enum EnumStepCondition {

    ALWAYS("gui." + Constants.MOD_ID + ".condition_always", 0),
    ON_SUCCESS("gui." + Constants.MOD_ID + ".condition_on_success", 1),
    ON_FAILURE("gui." + Constants.MOD_ID + ".condition_on_failure", 2);

    private final String text;
    private final int id;

    EnumStepCondition(String text, int id) {
        this.text = text;
        this.id = id;
    }

    public Component getComponent() {
        return Component.translatable(this.text);
    }

    public static EnumStepCondition getById(int id) {
        for (EnumStepCondition type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        return ALWAYS;
    }

    public int getId() {
        return id;
    }
}
