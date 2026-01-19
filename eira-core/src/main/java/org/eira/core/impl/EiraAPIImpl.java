package org.eira.core.impl;

import org.eira.core.api.EiraAPI;
import org.eira.core.api.events.EiraEventBus;

/**
 * Implementation of the Eira API.
 */
public class EiraAPIImpl implements EiraAPI {

    private final EiraEventBus eventBus;

    public EiraAPIImpl() {
        this.eventBus = new SimpleEventBus();
    }

    @Override
    public EiraEventBus events() {
        return eventBus;
    }
}
