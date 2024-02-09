package de.voldechse.wintervillage.potterwars.listener;

import de.voldechse.wintervillage.potterwars.PotterWars;
import de.voldechse.wintervillage.potterwars.gamestate.Types;
import de.voldechse.wintervillage.potterwars.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class AsyncPlayerChatListener implements Listener {

    @EventHandler
    public void execute(final AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (player.hasMetadata("EDIT_TEAM")) {
            event.setCancelled(true);
            Team team = PotterWars.getInstance().teamManager.getTeam(player.getMetadata("EDIT_TEAM").get(0).asInt());

            String[] toSplit = event.getMessage().split(" ");

            switch (toSplit[0]) {
                case "TEAM_BLOCK" -> {
                    PotterWars.getInstance().teamManager.updateString(team.getTeamId(), "TEAM_BLOCK", player.getItemInHand().getType().name());
                    player.sendMessage(PotterWars.getInstance().serverPrefix + "§aGespeichert!");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                }

                case "TEAM_NAME" -> {
                    PotterWars.getInstance().teamManager.updateString(team.getTeamId(), "TEAM_NAME", toSplit[1]);
                    player.sendMessage(PotterWars.getInstance().serverPrefix + "§aGespeichert!");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                }

                case "TEAM_PREFIX" -> {
                    PotterWars.getInstance().teamManager.updateString(team.getTeamId(), "TEAM_PREFIX", toSplit[1]);
                    player.sendMessage(PotterWars.getInstance().serverPrefix + "§aGespeichert!");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                }

                case "PLAYER_SPAWN" -> {
                    PotterWars.getInstance().teamManager.updateLocation(team.getTeamId(), player.getLocation());
                    player.sendMessage(PotterWars.getInstance().serverPrefix + "§aGespeichert!");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                }

                case "DONE" -> {
                    player.sendMessage(PotterWars.getInstance().serverPrefix + "§aBearbeitungsmodus für Team §f" + team.getTeamId() + " §averlassen");
                    PotterWars.getInstance().removeMetadata(player, "EDIT_TEAM");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                }

                default -> {
                    player.sendMessage(PotterWars.getInstance().serverPrefix + "§fSchreibe §eTEAM_BLOCK §fwährend du ein Item in der Hand hältst, §eTEAM_NAME §8| §eTEAM_PREFIX §8<§eARGUMENT§8> §foder §ePLAYERSPAWN §fin den Chat, um die Position zu speichern oder §aDONE §fum den Bearbeitungsmodus zu verlassen");
                    player.playSound(player.getLocation(), Sound.ENTITY_PILLAGER_DEATH, 1.0F, 1.0F);
                }
            }

        }


        String message = event.getMessage();

        Team team = null;
        if (PotterWars.getInstance().teamManager.isPlayerInTeam(player))
            team = PotterWars.getInstance().teamManager.getTeam(player);

        Types gamePhase = PotterWars.getInstance().gameStateManager.currentGameState().getGameStatePhase();
        switch (gamePhase) {
            case LOBBY -> {
                event.getRecipients().clear();
                event.getRecipients().addAll(Bukkit.getOnlinePlayers());
                if (team != null)
                    event.setFormat(team.teamPrefix + team.teamName + " " + player.getName() + "§8: §f" + message);
                else event.setFormat("§f" + player.getName() + "§8: §f" + message);
            }

            case PREPARING_START, INGAME, OVERTIME -> {
                if (PotterWars.getInstance().gameManager.isSpectator(player)) {
                    event.getRecipients().clear();
                    event.getRecipients().addAll(PotterWars.getInstance().gameManager.getSpectatorList());
                    event.setFormat("§8[§4✘§8] §f%1$s§8: §f%2$s");

                    //TTT.getInstance().gameManager.getSpectatorList().forEach(spectators -> spectators.sendMessage("§8[§4✘§8] §f" + player.getName() + "§8: §f" + event.getMessage()));
                    //event.setCancelled(true);
                    return;
                }

                if (message.startsWith("@all ") || message.startsWith("@a ")) {
                    message = message.replaceFirst("@all ", "")
                            .replaceFirst("@a ", "");
                    event.getRecipients().clear();
                    event.getRecipients().addAll(Bukkit.getOnlinePlayers());
                    event.setFormat("§4ALL §8| §f" + team.teamPrefix + team.teamName + " " + player.getName() + "§8: §f" + message);
                    return;
                }

                event.getRecipients().clear();
                event.getRecipients().addAll(team.players);

                event.setFormat(team.teamPrefix + player.getName() + "§8: §f" + message);
            }

            case RESTART -> {
                event.getRecipients().clear();
                event.getRecipients().addAll(Bukkit.getOnlinePlayers());
                event.setFormat((PotterWars.getInstance().gameManager.isSpectator(player) ? "§8[§4✘§8]" : "") + " §f" + player.getName() + "§8: §f" + message);
            }
        }
    }
}