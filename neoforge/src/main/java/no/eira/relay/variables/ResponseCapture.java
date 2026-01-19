package no.eira.relay.variables;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Configuration for capturing a value from an HTTP response.
 */
public class ResponseCapture {

    private String variableName;
    private String jsonPath;
    private String defaultValue;
    private VariableScope scope;

    public ResponseCapture() {
        this.variableName = "";
        this.jsonPath = "";
        this.defaultValue = "";
        this.scope = VariableScope.BLOCK;
    }

    public ResponseCapture(String variableName, String jsonPath) {
        this.variableName = variableName;
        this.jsonPath = jsonPath;
        this.defaultValue = "";
        this.scope = VariableScope.BLOCK;
    }

    public ResponseCapture(String variableName, String jsonPath, String defaultValue, VariableScope scope) {
        this.variableName = variableName;
        this.jsonPath = jsonPath;
        this.defaultValue = defaultValue;
        this.scope = scope;
    }

    // Getters and setters

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public String getJsonPath() {
        return jsonPath;
    }

    public void setJsonPath(String jsonPath) {
        this.jsonPath = jsonPath;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public VariableScope getScope() {
        return scope;
    }

    public void setScope(VariableScope scope) {
        this.scope = scope;
    }

    /**
     * Check if this capture configuration is valid (has required fields).
     */
    public boolean isValid() {
        return variableName != null && !variableName.isEmpty()
            && jsonPath != null && !jsonPath.isEmpty();
    }

    /**
     * Copy this capture configuration.
     */
    public ResponseCapture copy() {
        return new ResponseCapture(variableName, jsonPath, defaultValue, scope);
    }

    // NBT serialization

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", variableName);
        tag.putString("path", jsonPath);
        tag.putString("default", defaultValue);
        tag.putInt("scope", scope.ordinal());
        return tag;
    }

    public static ResponseCapture fromNBT(CompoundTag tag) {
        ResponseCapture capture = new ResponseCapture();
        capture.variableName = tag.getString("name");
        capture.jsonPath = tag.getString("path");
        capture.defaultValue = tag.getString("default");
        int scopeOrdinal = tag.getInt("scope");
        capture.scope = scopeOrdinal >= 0 && scopeOrdinal < VariableScope.values().length
            ? VariableScope.values()[scopeOrdinal]
            : VariableScope.BLOCK;
        return capture;
    }

    // Network serialization

    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeUtf(variableName);
        buf.writeUtf(jsonPath);
        buf.writeUtf(defaultValue);
        buf.writeEnum(scope);
    }

    public static ResponseCapture readFromBuffer(FriendlyByteBuf buf) {
        ResponseCapture capture = new ResponseCapture();
        capture.variableName = buf.readUtf();
        capture.jsonPath = buf.readUtf();
        capture.defaultValue = buf.readUtf();
        capture.scope = buf.readEnum(VariableScope.class);
        return capture;
    }

    @Override
    public String toString() {
        return String.format("ResponseCapture{name='%s', path='%s', default='%s', scope=%s}",
            variableName, jsonPath, defaultValue, scope);
    }

    /**
     * Scope for stored variables.
     */
    public enum VariableScope {
        BLOCK,      // Stored per block, accessible by nearby blocks
        GLOBAL,     // Server-wide, accessible from any block
        PLAYER      // Per-player (requires player context)
    }
}
