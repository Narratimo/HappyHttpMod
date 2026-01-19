package org.eira.core;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.eira.core.api.EiraAPI;
import org.eira.core.api.events.*;
import org.eira.core.impl.EiraAPIImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Eira Core - Library mod providing shared APIs for the Eira ecosystem.
 *
 * Features:
 * - Event bus for cross-mod communication
 * - Team and player management (future)
 * - Server client connection (future)
 */
@Mod(EiraCore.MOD_ID)
public class EiraCore {

    public static final String MOD_ID = "eiracore";
    public static final String MOD_NAME = "Eira Core";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    private static EiraAPIImpl apiInstance;

    public EiraCore(IEventBus modEventBus, ModContainer modContainer) {
        // Initialize the API
        apiInstance = new EiraAPIImpl();

        // Register debug subscribers
        registerDebugSubscribers();

        LOG.info("Eira Core initialized - Event bus ready for cross-mod communication");
    }

    /**
     * Register debug subscribers to verify event publishing works.
     * These log events at DEBUG level for troubleshooting.
     */
    private void registerDebugSubscribers() {
        EiraEventBus events = apiInstance.events();

        events.subscribe(HttpReceivedEvent.class, event -> {
            LOG.debug("[Event] HttpReceived: endpoint={}, method={}, params={}",
                event.endpoint(), event.method(), event.params().size());
        });

        events.subscribe(ExternalTriggerEvent.class, event -> {
            LOG.debug("[Event] ExternalTrigger: source={}, triggerId={}, data={}",
                event.source(), event.triggerId(), event.data().size());
        });

        events.subscribe(RedstoneChangeEvent.class, event -> {
            LOG.debug("[Event] RedstoneChange: pos={}, {} -> {}",
                event.pos(), event.oldStrength(), event.newStrength());
        });

        events.subscribe(CheckpointCompletedEvent.class, event -> {
            LOG.debug("[Event] CheckpointCompleted: game={}, checkpoint={}, player={}",
                event.gameId(), event.checkpointId(), event.playerId());
        });

        events.subscribe(ServerCommandEvent.class, event -> {
            LOG.debug("[Event] ServerCommand: command={}, params={}",
                event.command(), event.params().size());
        });

        LOG.debug("Debug subscribers registered for all event types");
    }

    /**
     * Get the Eira API instance (internal use)
     */
    public static EiraAPI getAPI() {
        return apiInstance;
    }

    /**
     * Check if Eira Core is loaded
     */
    public static boolean isLoaded() {
        return apiInstance != null;
    }
}
