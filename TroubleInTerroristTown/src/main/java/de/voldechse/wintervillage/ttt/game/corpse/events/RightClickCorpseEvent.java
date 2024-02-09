package de.voldechse.wintervillage.ttt.game.corpse.events;

import de.voldechse.wintervillage.ttt.game.corpse.player.CorpseData;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RightClickCorpseEvent extends Event {

    private final Player player;
    private final CorpseData corpseData;

    private static final HandlerList HANDLERS = new HandlerList();

    public RightClickCorpseEvent(Player player, CorpseData corpseData) {
        this.player = player;
        this.corpseData = corpseData;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public Player getPlayer() {
        return player;
    }

    public CorpseData getCorpseData() {
        return corpseData;
    }
}