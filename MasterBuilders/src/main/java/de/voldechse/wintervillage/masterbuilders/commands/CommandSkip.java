package de.voldechse.wintervillage.masterbuilders.commands;

import de.voldechse.wintervillage.masterbuilders.MasterBuilders;
import de.voldechse.wintervillage.masterbuilders.gamestate.Types;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandSkip implements CommandExecutor {
    
    private final MasterBuilders plugin;

    public static List<Player> skipVoteList = new ArrayList<Player>();

    public CommandSkip() {
        this.plugin = InjectionLayer.ext().instance(MasterBuilders.class);
        
        this.plugin.getInstance().getCommand("skip").setExecutor(this);
    }

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(this.plugin.serverPrefix + "§cUm diesen Befehl ausführen zu können, musst du ein Spieler sein");
            return true;
        }
        Player player = (Player) commandSender;

        Types gamePhase = this.plugin.gameStateManager.currentGameState().getGameStatePhase();
        if (gamePhase != Types.BUILDING_PHASE) {
            player.sendMessage(this.plugin.serverPrefix + "§cDas Spiel kann nur in der Bauzeit übersprungen werden!");
            return true;
        }

        if (this.plugin.gameManager.isSpectator(player)) return true;

        if (skipVoteList.contains(player)) {
            player.sendMessage(this.plugin.serverPrefix + "§cDu hast bereits vorgeschlagen, die Zeit zu überspringen!");
            return true;
        }

        if (skipVoteList.size() >= calculateRequiredPlayers(this.plugin.teamManager.getAssignedPlayerTeams().size())) {
            player.sendMessage(this.plugin.serverPrefix + "§cEs haben bereits genug Spieler abgestimmt!");
            return true;
        }

        skipVoteList.add(player);

        Bukkit.broadcastMessage(this.plugin.serverPrefix + "§eEin Spieler hat für das Überspringen der Bauzeit gestimmt §c§o/skip");
        this.plugin.gameManager.playSound(Sound.ENTITY_PLAYER_LEVELUP, 1.0f);
        this.plugin.scoreboardManager.updateScoreboard("voteSkips", " §d" + skipVoteList.size() + "§8/§7" + calculateRequiredPlayers(this.plugin.teamManager.getPlayerTeamList().size()) + " §7§o/skip", "");

        if (skipVoteList.size() == calculateRequiredPlayers(this.plugin.teamManager.getAssignedPlayerTeams().size())) {
            Bukkit.broadcastMessage(this.plugin.serverPrefix + "§eDie Zeit wird übersprungen, da genug Spieler abgestimmt haben!");
            this.plugin.gameManager.playSound(Sound.EVENT_RAID_HORN, 100.0F, 1.0F);
            this.plugin.gameStateManager.currentGameState().getCountdown().setCountdownTime(15);
        }
        return true;
    }

    public int calculateRequiredPlayers(int totalPlayers) {
        double percentage = this.plugin.configDocument.getDouble("percentageToSkipBuildingPhase");
        double requiredPlayers = (percentage / 100) * totalPlayers;
        return (int) Math.ceil(requiredPlayers);
    }
}