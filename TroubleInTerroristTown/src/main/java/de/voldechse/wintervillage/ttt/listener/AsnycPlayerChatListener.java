package de.voldechse.wintervillage.ttt.listener;

import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.gamestate.Types;
import de.voldechse.wintervillage.ttt.roles.Role;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AsnycPlayerChatListener implements Listener {
    
    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);
    
    @EventHandler
    public void execute(AsyncPlayerChatEvent event) {
        
        Player player = event.getPlayer();

        if (player.hasMetadata("SETUP_TESTER")) {
            event.setCancelled(true);

            switch (event.getMessage().toUpperCase()) {
                case "CORNER_A" -> {
                    this.plugin.testerSetup.update("CORNER_A", player.getLocation());

                    player.sendMessage(this.plugin.serverPrefix + "§aGespeichert!");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                    return;
                }
                case "CORNER_B" -> {
                    this.plugin.testerSetup.update("CORNER_B", player.getLocation());

                    player.sendMessage(this.plugin.serverPrefix + "§aGespeichert!");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                    return;
                }
                case "PLAYER_SPAWN" -> {
                    this.plugin.testerSetup.update("PLAYER_SPAWN", player.getLocation());

                    player.sendMessage(this.plugin.serverPrefix + "§aGespeichert!");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                    return;
                }
                case "OUTSIDE_TESTER" -> {
                    this.plugin.testerSetup.update("OUTSIDE_TESTER", player.getLocation());

                    player.sendMessage(this.plugin.serverPrefix + "§aGespeichert!");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                    return;
                }

                case "LEFT_LAMP" -> {
                    if (!player.hasMetadata("EDITING")) {
                        player.sendMessage(this.plugin.serverPrefix + "§cZerstöre den Block §fmit dem Item, dessen Position du speichern willst. §cSchreibe §fdann entweder §eBARRIER_1 §8| §eBARRIER_2 §8| §eBARRIER_3 §8| §eLEFT_LAMP §8| §eRIGHT_LAMP §8| §eACTIVATION_BUTTON §c(BUTTON) §foder §eTRAITOR_TRAP §c(BUTTON) §fin den Chat, um die Position zu speichern");
                        player.sendMessage(this.plugin.serverPrefix + "§cMit §aDONE §ckannst du den Bearbeitungsmodus verlassen");
                        return;
                    }

                    Location destroyed = (Location) player.getMetadata("EDITING").get(0).value();
                    this.plugin.testerSetup.update("LEFT_LAMP", destroyed);
                    this.plugin.removeMetadata(player, "EDITING");

                    player.sendMessage(this.plugin.serverPrefix + "§aGespeichert!");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                    return;
                }
                case "RIGHT_LAMP" -> {
                    if (!player.hasMetadata("EDITING")) {
                        player.sendMessage(this.plugin.serverPrefix + "§cZerstöre den Block §fmit dem Item, dessen Position du speichern willst. §cSchreibe §fdann entweder §eBARRIER_1 §8| §eBARRIER_2 §8| §eBARRIER_3 §8| §eLEFT_LAMP §8| §eRIGHT_LAMP §8| §eACTIVATION_BUTTON §c(BUTTON) §foder §eTRAITOR_TRAP §c(BUTTON) §fin den Chat, um die Position zu speichern");
                        player.sendMessage(this.plugin.serverPrefix + "§cMit §aDONE §ckannst du den Bearbeitungsmodus verlassen");
                        return;
                    }

                    Location destroyed = (Location) player.getMetadata("EDITING").get(0).value();
                    this.plugin.testerSetup.update("RIGHT_LAMP", destroyed);
                    this.plugin.removeMetadata(player, "EDITING");

                    player.sendMessage(this.plugin.serverPrefix + "§aGespeichert!");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                    return;
                }
                case "BARRIER_1" -> {
                    if (!player.hasMetadata("EDITING")) {
                        player.sendMessage(this.plugin.serverPrefix + "§cZerstöre den Block §fmit dem Item, dessen Position du speichern willst. §cSchreibe §fdann entweder §eBARRIER_1 §8| §eBARRIER_2 §8| §eBARRIER_3 §8| §eLEFT_LAMP §8| §eRIGHT_LAMP §8| §eACTIVATION_BUTTON §c(BUTTON) §foder §eTRAITOR_TRAP §c(BUTTON) §fin den Chat, um die Position zu speichern");
                        player.sendMessage(this.plugin.serverPrefix + "§cMit §aDONE §ckannst du den Bearbeitungsmodus verlassen");
                        return;
                    }

                    Location destroyed = (Location) player.getMetadata("EDITING").get(0).value();
                    this.plugin.testerSetup.update("BARRIER_1", destroyed);
                    this.plugin.removeMetadata(player, "EDITING");

                    player.sendMessage(this.plugin.serverPrefix + "§aGespeichert!");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                    return;
                }
                case "BARRIER_2" -> {
                    if (!player.hasMetadata("EDITING")) {
                        player.sendMessage(this.plugin.serverPrefix + "§cZerstöre den Block §fmit dem Item, dessen Position du speichern willst. §cSchreibe §fdann entweder §eBARRIER_1 §8| §eBARRIER_2 §8| §eBARRIER_3 §8| §eLEFT_LAMP §8| §eRIGHT_LAMP §8| §eACTIVATION_BUTTON §c(BUTTON) §foder §eTRAITOR_TRAP §c(BUTTON) §fin den Chat, um die Position zu speichern");
                        player.sendMessage(this.plugin.serverPrefix + "§cMit §aDONE §ckannst du den Bearbeitungsmodus verlassen");
                        return;
                    }

                    Location destroyed = (Location) player.getMetadata("EDITING").get(0).value();
                    this.plugin.testerSetup.update("BARRIER_2", destroyed);
                    this.plugin.removeMetadata(player, "EDITING");

                    player.sendMessage(this.plugin.serverPrefix + "§aGespeichert!");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                    return;
                }
                case "BARRIER_3" -> {
                    if (!player.hasMetadata("EDITING")) {
                        player.sendMessage(this.plugin.serverPrefix + "§cZerstöre den Block §fmit dem Item, dessen Position du speichern willst. §cSchreibe §fdann entweder §eBARRIER_1 §8| §eBARRIER_2 §8| §eBARRIER_3 §8| §eLEFT_LAMP §8| §eRIGHT_LAMP §8| §eACTIVATION_BUTTON §c(BUTTON) §foder §eTRAITOR_TRAP §c(BUTTON) §fin den Chat, um die Position zu speichern");
                        player.sendMessage(this.plugin.serverPrefix + "§cMit §aDONE §ckannst du den Bearbeitungsmodus verlassen");
                        return;
                    }

                    Location destroyed = (Location) player.getMetadata("EDITING").get(0).value();
                    this.plugin.testerSetup.update("BARRIER_3", destroyed);
                    this.plugin.removeMetadata(player, "EDITING");

                    player.sendMessage(this.plugin.serverPrefix + "§aGespeichert!");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                    return;
                }
                case "ACTIVATION_BUTTON" -> {
                    if (!player.hasMetadata("EDITING")) {
                        player.sendMessage(this.plugin.serverPrefix + "§cZerstöre den Block §fmit dem Item, dessen Position du speichern willst. §cSchreibe §fdann entweder §eBARRIER_1 §8| §eBARRIER_2 §8| §eBARRIER_3 §8| §eLEFT_LAMP §8| §eRIGHT_LAMP §8| §eACTIVATION_BUTTON §c(BUTTON) §foder §eTRAITOR_TRAP §c(BUTTON) §fin den Chat, um die Position zu speichern");
                        player.sendMessage(this.plugin.serverPrefix + "§cMit §aDONE §ckannst du den Bearbeitungsmodus verlassen");
                        return;
                    }

                    Location destroyed = (Location) player.getMetadata("EDITING").get(0).value();
                    this.plugin.testerSetup.update("ACTIVATION_BUTTON", destroyed);
                    this.plugin.removeMetadata(player, "EDITING");

                    player.sendMessage(this.plugin.serverPrefix + "§aGespeichert!");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                    return;
                }
                case "TRAITOR_TRAP" -> {
                    if (!player.hasMetadata("EDITING")) {
                        player.sendMessage(this.plugin.serverPrefix + "§cZerstöre den Block §fmit dem Item, dessen Position du speichern willst. §cSchreibe §fdann entweder §eBARRIER_1 §8| §eBARRIER_2 §8| §eBARRIER_3 §8| §eLEFT_LAMP §8| §eRIGHT_LAMP §8| §eACTIVATION_BUTTON §c(BUTTON) §foder §eTRAITOR_TRAP §c(BUTTON) §fin den Chat, um die Position zu speichern");
                        player.sendMessage(this.plugin.serverPrefix + "§cMit §aDONE §ckannst du den Bearbeitungsmodus verlassen");
                        return;
                    }

                    Location destroyed = (Location) player.getMetadata("EDITING").get(0).value();
                    this.plugin.testerSetup.update("TRAITOR_TRAP", destroyed);
                    this.plugin.removeMetadata(player, "EDITING");

                    player.sendMessage(this.plugin.serverPrefix + "§aGespeichert!");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                    return;
                }

                case "DONE" -> {
                    if (player.hasMetadata("EDITING")) this.plugin.removeMetadata(player, "EDITING");

                    player.sendMessage(this.plugin.serverPrefix + "§aDu hast den Bearbeitungsmodus verlassen");
                    player.sendMessage(this.plugin.serverPrefix + "§eLade ggf. die Config mit §c/admin RELOAD_CONFIG §eneu");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                    //player.chat("/admin RELOAD_CONFIG");

                    this.plugin.removeMetadata(player, "SETUP_TESTER");

                    Arrays.stream(this.plugin.testerSetup.testerLamps).forEach(location -> location.getBlock().setType(this.plugin.testerSetup.idling));
                    Arrays.stream(this.plugin.testerSetup.barrier).forEach(location -> location.getBlock().setType(Material.AIR));
                    return;
                }
                default -> {
                    player.sendMessage(this.plugin.serverPrefix + "§cFalsche Syntax! Verwende folgendermaßen:");
                    player.sendMessage(this.plugin.serverPrefix + "§cSchreibe §eCORNER_A §8| §eCORNER_B §8| §ePLAYER_SPAWN §8| §eOUTSIDE_TESTER §fin den Chat um die Position, an der du stehst, zu speichern.");
                    player.sendMessage(this.plugin.serverPrefix + "§cZerstöre den Block §fmit dem Item, dessen Position du speichern willst. §cSchreibe §fdann entweder §eBARRIER_1 §8| §eBARRIER_2 §8| §eBARRIER_3 §8| §eLEFT_LAMP §8| §eRIGHT_LAMP §8| §eACTIVATION_BUTTON §c(BUTTON) §foder §eTRAITOR_TRAP §c(BUTTON) §fin den Chat, um die Position zu speichern");
                    player.sendMessage(this.plugin.serverPrefix + "§fMit §aDONE §fkannst du den Bearbeitungsmodus verlassen");
                    player.playSound(player.getLocation(), Sound.ENTITY_PILLAGER_DEATH, 1.0F, 1.0F);
                    return;
                }
            }
        }

        if (player.hasMetadata("EDIT_SPAWNS")) {
            event.setCancelled(true);

            switch (event.getMessage().toUpperCase()) {
                case "ADD" -> {
                    this.plugin.positionManager.addSpawnConfig(player.getLocation());

                    player.sendMessage(this.plugin.serverPrefix + "§aGespeichert!");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                    return;
                }
                case "DONE" -> {
                    player.sendMessage(this.plugin.serverPrefix + "§aDu hast den Bearbeitungsmodus verlassen");
                    player.sendMessage(this.plugin.serverPrefix + "§eLade ggf. die Config mit §c/admin RELOAD_CONFIG §eneu");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                    //player.chat("/admin RELOAD_CONFIG");

                    this.plugin.removeMetadata(player, "EDIT_SPAWNS");
                    return;
                }
                default -> {
                    player.sendMessage(this.plugin.serverPrefix + "§cFalsche Syntax! Verwende folgendermaßen:");
                    player.sendMessage(this.plugin.serverPrefix + "§aDu kannst nun Spawns hinzufügen, indem du einfach §eADD §ain den Chat schreibst");
                    player.sendMessage(this.plugin.serverPrefix + "§eZerstöre einen ArmorStand, wenn du dessen Position löschen möchtest");
                    player.sendMessage(this.plugin.serverPrefix + "§fMit §aDONE §fkannst du den Bearbeitungsmodus verlassen");
                    player.playSound(player.getLocation(), Sound.ENTITY_PILLAGER_DEATH, 1.0F, 1.0F);
                    return;
                }
            }
        }

        if (player.hasMetadata("EDIT_CHESTS")) {
            event.setCancelled(true);

            switch (event.getMessage().toUpperCase()) {
                case "DONE" -> {
                    player.sendMessage(this.plugin.serverPrefix + "§aDu hast den Bearbeitungsmodus verlassen");
                    player.sendMessage(this.plugin.serverPrefix + "§eLade ggf. die Config mit §c/admin RELOAD_CONFIG §eneu");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                    //player.chat("/admin RELOAD_CONFIG");

                    this.plugin.removeMetadata(player, "EDIT_CHESTS");
                    return;
                }
                default -> {
                    player.sendMessage(this.plugin.serverPrefix + "§cFalsche Syntax! Verwende folgendermaßen:");
                    player.sendMessage(this.plugin.serverPrefix + "§aDu kannst Kisten hinzufügen, indem du diese einfach platzierst");
                    player.sendMessage(this.plugin.serverPrefix + "§eZerstöre eine Kiste, wenn du dessen Position löschen möchtest");
                    player.sendMessage(this.plugin.serverPrefix + "§fMit §aDONE §fkannst du den Bearbeitungsmodus verlassen");
                    player.playSound(player.getLocation(), Sound.ENTITY_PILLAGER_DEATH, 1.0F, 1.0F);
                    return;
                }
            }
        }

        if (!plugin.permissionManagement.containsUserAsync(player.getUniqueId()).join()) {
            event.setCancelled(true);
            player.sendMessage(plugin.serverPrefix + "§cAn error occurred while searching your entry in database");
            return;
        }

        PermissionUser permissionUser = plugin.permissionManagement.userAsync(player.getUniqueId()).join();
        if (permissionUser == null) {
            event.setCancelled(true);
            player.sendMessage(plugin.serverPrefix + "§cAn error occurred while searching your entry in database");
            return;
        }
        PermissionGroup permissionGroup = plugin.permissionManagement.highestPermissionGroup(permissionUser);
        if (permissionGroup == null) {
            event.setCancelled(true);
            player.sendMessage(plugin.serverPrefix + "§cAn error occurred while searching your entry in database");
            return;
        }

        Role role = null;
        if (this.plugin.roleManager.isPlayerAssigned(player))
            role = this.plugin.roleManager.getRole(player);

        Types gamePhase = this.plugin.gameStateManager.currentGameState().getGameStatePhase();
        switch (gamePhase) {
            case LOBBY, RESTART -> {
                event.getRecipients().clear();
                event.getRecipients().addAll(Bukkit.getOnlinePlayers());

                event.setFormat(" " + permissionGroup.color() + permissionGroup.name() + " §f%1$s" + " §8| §f%2$s");
            }

            case PREPARING_START -> {
                if (this.plugin.gameManager.isSpectator(player)) {
                    event.getRecipients().clear();
                    event.getRecipients().addAll(this.plugin.gameManager.getSpectatorList());

                    event.setFormat(" §4✘ " + permissionGroup.color() + "%1$s §8| §f%2$s");

                    //this.plugin.gameManager.getSpectatorList().forEach(spectators -> spectators.sendMessage("§8[§4✘§8] §f" + player.getName() + "§8: §f" + event.getMessage()));
                    //event.setCancelled(true);
                    return;
                }

                event.getRecipients().clear();
                event.getRecipients().addAll(Bukkit.getOnlinePlayers());

                event.setFormat(" " + permissionGroup.color() + permissionGroup.name() + " §f%1$s" + " §8| §f%2$s");
            }

            case INGAME -> {
                if (this.plugin.gameManager.isSpectator(player)) {
                    event.getRecipients().clear();
                    event.getRecipients().addAll(this.plugin.gameManager.getSpectatorList());

                    event.setFormat(" §4✘ " + permissionGroup.color() + "%1$s §8| §f%2$s");

                    //this.plugin.gameManager.getSpectatorList().forEach(spectators -> spectators.sendMessage("§8[§4✘§8] §f" + player.getName() + "§8: §f" + event.getMessage()));
                    //event.setCancelled(true);
                    return;
                }

                //if (event.getMessage().startsWith("@d ") && role != null && role.roleId == 1) {
                if (event.getMessage().startsWith("@f ") && role != null && role.roleId == 1) {
                        //event.setMessage(event.getMessage().replaceFirst("@d ", ""));
                        event.setMessage(event.getMessage().replaceFirst("@f ", ""));

                        event.getRecipients().clear();
                        event.getRecipients().addAll(role.getPlayers());

                        event.setFormat(" §8[§9Mr. Frost§8] %1$s §8| §f%2$s");
                        return;
                }

                //if (event.getMessage().startsWith("@t ") && role != null && role.roleId == 1) {
                if (event.getMessage().startsWith("@k ") && role != null && role.roleId == 2) {
                    //event.setMessage(event.getMessage().replaceFirst("@t ", ""));
                    event.setMessage(event.getMessage().replaceFirst("@k ", ""));

                    event.getRecipients().clear();
                    event.getRecipients().addAll(role.getPlayers());

                    event.setFormat(" §8[§4Krampus§8] %1$s §8| §f%2$s");
                    return;
                }

                if (role != null) {
                    event.getRecipients().clear();
                    event.getRecipients().addAll(Bukkit.getOnlinePlayers());

                    if (role.roleId == 2 && !player.hasMetadata("BUSTED_TRAITOR")) {
                        event.setCancelled(true);

                        for (Player traitor : role.getPlayers())
                            traitor.sendMessage(" " + role.getRolePrefix() + player.getName() + " §8| §f" + event.getMessage());
                        for (Player excluding : excluding(role.getPlayers()))
                            excluding.sendMessage(" §a" + player.getName() + " §8| §f" + event.getMessage());
                        return;
                    }

                    if (role.roleId == 2 && player.hasMetadata("BUSTED_TRAITOR")) {
                        event.setFormat(" §4%1$s §8| §f%2$s");
                        return;
                    }

                    event.setFormat(" " + role.getRolePrefix() + "%1$s §8| §f%2$s");
                    return;
                }
            }
        }
    }

    private List<Player> excluding(List<Player> excluded) {
        List<Player> allPlayers = new ArrayList<>(this.plugin.roleManager.getPlayerList());
        allPlayers.removeAll(excluded);
        return allPlayers;
    }
}