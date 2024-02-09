package de.voldechse.wintervillage.ttt.gamestate.list;

import com.mojang.datafixers.util.Pair;
import de.voldechse.wintervillage.library.countdown.Countdown;
import de.voldechse.wintervillage.library.countdown.CountdownListener;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.gamestate.GameState;
import de.voldechse.wintervillage.ttt.gamestate.Types;
import de.voldechse.wintervillage.ttt.listener.PlayerMoveListener;
import de.voldechse.wintervillage.ttt.roles.Role;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.KeybindComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.world.entity.EquipmentSlot;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.List;

public class GameStateIngame extends GameState {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);
    
    private Countdown countdown;

    public static StringBuilder stringBuilder;

    {
        stringBuilder = new StringBuilder();
    }

    @Override
    public void startCountdown() {
        this.countdown = new Countdown(this.plugin.getInstance(), new CountdownListener() {
            @Override
            public void start() {
                plugin.roleManager.setPlayersInRoles();

                Bukkit.getOnlinePlayers().forEach(online -> {
                    Scoreboard scoreboard = online.getScoreboard();
                    scoreboard.getTeams().forEach(team -> {
                        if (team.hasEntry(online.getName())) team.removeEntry(online.getName());
                        team.unregister();
                    });
                });

                for (Player player : plugin.roleManager.getPlayerList()) {
                    Role playersRole = plugin.roleManager.getRole(player);
                    if (playersRole == null) continue;

                    player.sendMessage(plugin.serverPrefix + "§eDu bist ein " + playersRole.getRolePrefix() + playersRole.getRoleName());
                    //player.getInventory().setHeldItemSlot(0);
                    if (player.getInventory().getItem(4) == null)
                        player.getInventory().setItem(4, plugin.shop);
                    else player.getInventory().addItem(plugin.shop);

                    if (playersRole.roleId == 0) {
                        player.getInventory().setChestplate(plugin.innocentChestplate);
                        //player.sendMessage(this.plugin.serverPrefix + "§eFinde und eliminiere durch Beobachten alle Traitor");
                        player.sendMessage(plugin.serverPrefix + "§eFinde und eliminiere durch Beobachten alle Krampusse");
                    } else if (playersRole.roleId == 1) {
                        player.getInventory().setChestplate(plugin.detectiveChestplate);

                        //player.sendMessage(this.plugin.serverPrefix + "§eFinde und eliminiere durch Beobachten alle Traitor");
                        player.sendMessage(plugin.serverPrefix + "§eFinde und eliminiere durch Beobachten alle Krampusse");
                        //player.sendMessage(this.plugin.serverPrefix + "§eNutze §9@d§e, um mit deinen Detective-Kollegen zu kommunizieren");
                        player.sendMessage(plugin.serverPrefix + "§eNutze §9@f§e, um mit deinen Mr. Frost-Kollegen zu kommunizieren");
                        //player.sendMessage(this.plugin.serverPrefix + "§aÖffne den Shop mit §e/shop");
                    } else if (playersRole.roleId == 2) {
                        //player.getInventory().setChestplate(this.plugin.traitorChestplate);
                        player.getInventory().setChestplate(plugin.innocentChestplate);

                        //player.sendMessage(this.plugin.serverPrefix + "§eTöte unaufällig alle Innocents und Detectives");
                        player.sendMessage(plugin.serverPrefix + "§eTöte unaufällig alle Elfen und Mr. Frosts");
                        //player.sendMessage(this.plugin.serverPrefix + "§eNutze §4@t§e, um mit deinen Traitor-Kollegen zu kommunizieren");
                        player.sendMessage(plugin.serverPrefix + "§eNutze §4@k§e, um mit deinen Krampus-Kollegen zu kommunizieren");
                        //player.sendMessage(this.plugin.serverPrefix + "§aÖffne den Shop mit §e/shop");
                    }

                    player.sendMessage(plugin.serverPrefix + "§aÖffne den Shop mit §e/shop");
                    player.sendTitle(playersRole.getRolePrefix() + playersRole.getRoleName(), playersRole.getDescription());
                    plugin.roleManager.changeShopPoints(player, 2);
                }

                Bukkit.broadcastMessage(plugin.serverPrefix + "§eDiese Runde startete mit §b" + plugin.roleManager.getPlayerList().size() + "§8/§b" + plugin.PLAYING + " §eSpieler");

                plugin.scoreboardManager.generateScoreboard();
                plugin.scoreboardManager.updateScoreboard("currentPlayers", " §a" + plugin.roleManager.getPlayerList().size() + "§8/§a" + plugin.PLAYING, "§a");

                List<Player> traitor = plugin.roleManager.getRole(2).getPlayers();
                if (!traitor.isEmpty()) {
                    for (int i = 0; i < traitor.size(); i++) {
                        stringBuilder.append("§4" + traitor.get(i).getName());
                        if (traitor.size() > 1 && i < traitor.size() - 1) stringBuilder.append("§8, ");
                    }
                }
            }

            @Override
            public void stop() {
                //Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle("§4Die Traitor", "§fhaben das Spiel gewonnen"));
                Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle("§aDie Elfen", "§fhaben das Spiel gewonnen"));
                Bukkit.broadcastMessage("§8-----------------------");
                //Bukkit.broadcastMessage(this.plugin.serverPrefix + "§4Die Traitor §7haben das Spiel gewonnen!");
                Bukkit.broadcastMessage(plugin.serverPrefix + "§7Die §aElfen §7haben das Spiel gewonnen!");
                //Bukkit.broadcastMessage(this.plugin.serverPrefix + "§7Die Traitor waren§8: " + GameStateIngame.stringBuilder.toString());
                Bukkit.broadcastMessage(plugin.serverPrefix + "§7Die Krampusse waren§8: " + GameStateIngame.stringBuilder.toString());

                endCountdown();
            }

            @Override
            public void second(int i) {
                for (Player traitor : plugin.roleManager.getRole(2).getPlayers()) {
                    if (traitor.hasMetadata("BUSTED_TRAITOR")) continue;
                    fake(traitor.getEntityId());
                }

                plugin.scoreboardManager.updateScoreboard("currentPlayers", " §a" + plugin.roleManager.getPlayerList().size() + "§8/§a" + plugin.PLAYING, "§a");
                plugin.scoreboardManager.updateScoreboard("ingameTimer", " §e" + String.format("%02d:%02d", (i / 60), (i % 60)), "§e");

                if ((i <= countdown.getInitializedTime() - 60) && (i % 60 == 0)) {
                    plugin.roleManager.getRole(0).getPlayers().forEach(player -> {
                        plugin.roleManager.changeShopPoints(player, 1);
                        plugin.scoreboardManager.updateScoreboard(player, "currentShopPoints", " " + plugin.roleManager.getRole(player).getRolePrefix() + plugin.roleManager.getShopPoints(player), "");

                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                        player.sendMessage(plugin.serverPrefix + "§e1 Minute überlebt §b+§e1 " + plugin.roleManager.getRole(0).getRolePrefix() + plugin.roleManager.getRole(0).getRoleName() + "-Punkt");
                    });
                }

                for (Player player : plugin.roleManager.getPlayerList()) {
                    Role role = plugin.roleManager.getRole(player);
                    if (role == null) continue;

                    if (PlayerMoveListener.nearCorpse.contains(player.getUniqueId())) {
                        KeybindComponent keybindComponent = new KeybindComponent("key.sneak");

                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                new ComponentBuilder("§7Drücke §f")
                                        .append(keybindComponent)
                                        .append("§7, um die Leiche zu identifizieren")
                                        .create()
                        );
                        continue;
                    }

                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(role.getRolePrefix() + role.getRoleName()));
                }

                switch (i) {
                    case 60, 30, 20, 15, 10, 5, 3, 2 -> {
                        //Bukkit.broadcastMessage(this.plugin.serverPrefix + "§cDie Traitor gewinnen in §e" + i + " §cSekunden!");
                        Bukkit.broadcastMessage(plugin.serverPrefix + "§cDie Elfen gewinnen in §e" + i + " §cSekunden!");
                        plugin.gameManager.playSound(Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F);
                    }
                    case 1 -> {
                        //Bukkit.broadcastMessage(this.plugin.serverPrefix + "§cDie Traitor gewinnen in §e" + i + " §cSekunde!");
                        Bukkit.broadcastMessage(plugin.serverPrefix + "§cDie Elfen gewinnen in §e" + i + " §cSekunde!");
                        plugin.gameManager.playSound(Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F);
                    }
                    default -> {
                    }
                }
            }

            @Override
            public void sleep() {
            }
        });
        this.countdown.startCountdown(this.plugin.ingameCountdown, false);
        this.countdown.setInitializedTime(this.plugin.ingameCountdown);
    }

    @Override
    public void endCountdown() {
        this.plugin.gameStateManager.nextGameState();
    }

    @Override
    public Types getGameStatePhase() {
        return Types.INGAME;
    }

    @Override
    public Countdown getCountdown() {
        return this.countdown;
    }

    private void fake(int entityId) {
        ClientboundSetEquipmentPacket packet = new ClientboundSetEquipmentPacket(entityId, List.of(new Pair<>(EquipmentSlot.CHEST, CraftItemStack.asNMSCopy(plugin.traitorChestplate))));
        for (Player traitor : this.plugin.roleManager.getRole(2).getPlayers()) {
            if (traitor.hasMetadata("BUSTED_TRAITOR")) return;
            ((CraftPlayer) traitor).getHandle().connection.send(packet);
        }
    }
}