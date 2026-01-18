package no.eira.relay.registry;

/**
 * Network packets are now registered via RegisterPayloadHandlersEvent in EiraRelay.java
 * This class is kept for backwards compatibility but no longer used for NeoForge 1.21+
 */
public class ModNetworkPackets {

    public static void registerPackets() {
        // No-op: packets are registered via RegisterPayloadHandlersEvent in EiraRelay.java
    }
}
