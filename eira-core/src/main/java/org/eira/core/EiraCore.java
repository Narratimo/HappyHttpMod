package org.eira.core;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.eira.core.api.EiraAPI;
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

        LOG.info("Eira Core initialized - Event bus ready for cross-mod communication");
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
