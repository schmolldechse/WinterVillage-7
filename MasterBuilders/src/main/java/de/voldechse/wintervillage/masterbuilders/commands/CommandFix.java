package de.voldechse.wintervillage.masterbuilders.commands;

import de.voldechse.wintervillage.masterbuilders.MasterBuilders;
import de.voldechse.wintervillage.masterbuilders.gamestate.Types;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandFix implements CommandExecutor {
    
    private final MasterBuilders plugin;

    public CommandFix() {
        this.plugin = InjectionLayer.ext().instance(MasterBuilders.class);
        
        this.plugin.getInstance().getCommand("fix").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(this.plugin.serverPrefix + "§cUm diesen Befehl ausführen zu können, musst du ein Spieler sein");
            return true;
        }

        this.plugin.gameManager.updateVisibility(player);

        Types gamePhase = this.plugin.gameStateManager.currentGameState().getGameStatePhase();
        switch (gamePhase) {
            case BUILDING_PHASE -> {
                player.setGameMode(GameMode.CREATIVE);
                player.setAllowFlight(true);
                player.setFlying(true);
                player.setFlySpeed(0.2F);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
            }
            case VOTING_BUILDINGS -> {
                player.setGameMode(GameMode.SURVIVAL);
                player.setAllowFlight(true);
                player.setFlying(true);
                player.setFlySpeed(0.2F);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
            }
            default -> {
                player.playSound(player.getLocation(), Sound.ENTITY_PILLAGER_DEATH, 1.0f, 1.0f);
            }
        }
        return true;
    }
}