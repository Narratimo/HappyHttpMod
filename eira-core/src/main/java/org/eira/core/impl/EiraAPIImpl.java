package org.eira.core.impl;

import org.eira.core.api.EiraAPI;
import org.eira.core.api.adventure.AdventureManager;
import org.eira.core.api.events.EiraEventBus;
import org.eira.core.api.player.PlayerManager;
import org.eira.core.api.story.StoryManager;
import org.eira.core.api.team.TeamManager;

/**
 * Implementation of the Eira API.
 */
public class EiraAPIImpl implements EiraAPI {

    private final EiraEventBus eventBus;
    private final TeamManager teamManager;
    private final PlayerManager playerManager;
    private final StoryManager storyManager;
    private final AdventureManager adventureManager;

    public EiraAPIImpl() {
        this.eventBus = new SimpleEventBus();
        this.teamManager = new SimpleTeamManager(eventBus);
        this.playerManager = new SimplePlayerManager();
        this.storyManager = new SimpleStoryManager(eventBus);
        this.adventureManager = new SimpleAdventureManager(eventBus);
    }

    @Override
    public EiraEventBus events() {
        return eventBus;
    }

    @Override
    public TeamManager teams() {
        return teamManager;
    }

    @Override
    public PlayerManager players() {
        return playerManager;
    }

    @Override
    public StoryManager stories() {
        return storyManager;
    }

    @Override
    public AdventureManager adventures() {
        return adventureManager;
    }
}
