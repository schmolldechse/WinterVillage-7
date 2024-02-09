package de.voldechse.wintervillage.ttt.roles.items.list;

import de.voldechse.wintervillage.library.countdown.Countdown;
import de.voldechse.wintervillage.library.countdown.CountdownListener;
import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.roles.Role;
import de.voldechse.wintervillage.ttt.roles.items.RoleItem;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Item_RandomTester extends RoleItem implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    private Countdown countdown;

    private final Random RANDOM = new Random();

    @Override
    public String getName() {
        //return "§7Random-Tester";
        return "§7Santas Little Helper";
    }

    @Override
    public ItemStack getIcon(boolean traitor) {
        return new ItemBuilder(Material.IRON_INGOT, 1, this.getName())
                .lore("",
                        "§7Testet einen zufälligen Spieler",
                        "§740 Sekunden nach Benutzung des Items.",
                        //"§7Ist er Traitor, so wird er vorgewarnt,",
                        "§7Ist er §4Krampus§7, so wird er vorgewarnt,",
                        "§7dass er bald enttarnt ist",
                        "",
                        "§ePreis: " + (traitor ? "§c" : "§a") + this.getNeededPoints() + " Punkte",
                        "",
                        "§a<Klicke zum Kaufen>")
                .build();
    }

    @Override
    public int getNeededPoints() {
        return 2;
    }

    @Override
    public void equipItems(Player player) {
        player.getInventory().addItem(new ItemBuilder(Material.IRON_INGOT, 1, this.getName())
                .lore("",
                        "§7Testet einen zufälligen Spieler",
                        "§740 Sekunden nach Benutzung des Items.",
                        //"§7Ist er Traitor, so wird er vorgewarnt,",
                        "§7Ist er §4Krampus§7, so wird er vorgewarnt,",
                        "§7dass er bald enttarnt ist")
                .build());
    }

    @Override
    public int howOftenBuyable() {
        return 1;
    }


    @Override
    public boolean alradyInInventory(Player player) {
        return player.getInventory().contains(Material.IRON_INGOT);
    }

    @EventHandler
    public void execute(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getItem() != null
                && event.getItem().getItemMeta().getDisplayName() != null
                && event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase(this.getName())) {
            event.setCancelled(true);

            if (this.plugin.gameManager.isSpectator(player)) return;

            if (this.plugin.roleManager.getRole(player) == null) return;

            if (!this.plugin.roleManager.isPlayerAssigned(player, 1)) return;

            Player randomPlayer = getRandomPlayer();
            if (randomPlayer == null) {
                player.sendMessage(this.plugin.serverPrefix + "§cSomething seems to be not working. Try again");
                return;
            }
            if (randomPlayer.getUniqueId() == player.getUniqueId()) return;

            if (player.hasMetadata("USED_RANDOMTESTER")) {
                player.sendMessage(this.plugin.serverPrefix + "§cDu kannst " + this.getName() + " §cnur §b" + this.howOftenBuyable() + "§cx nutzen!");
                return;
            }

            if (randomPlayer.hasMetadata("RANDOM_PLAYER_FOR_RANDOMTESTER")) {
                player.sendMessage(this.plugin.serverPrefix + "§cDie Detektei ermittelt gegen denselben Spieler! Versuche es erneut");
                return;
            }

            Role playersRole = this.plugin.roleManager.getRole(player);
            if (playersRole == null) {
                player.sendMessage(this.plugin.serverPrefix + "§cSomething seems to be not working. Try again");
                return;
            }

            Role detectedPlayersRole = this.plugin.roleManager.getRole(randomPlayer);
            if (detectedPlayersRole == null) {
                player.sendMessage(this.plugin.serverPrefix + "§cSomething seems to be not working. Try again");
                return;
            }

            countdown = new Countdown(this.plugin.getInstance(), new CountdownListener() {
                @Override
                public void start() {
                    plugin.setMetadata(randomPlayer, "RANDOM_PLAYER_FOR_RANDOMTESTER", true);
                    plugin.setMetadata(player, "USED_RANDOMTESTER", true);

                    player.sendMessage(plugin.serverPrefix + "§aDu hast einen " + getName() + " §aaktiviert!");
                    plugin.roleManager.getRole(1).getPlayers().forEach(detective -> detective.sendMessage(plugin.serverPrefix + "§aDie Detektei ermittelt nun im Auftrag von " + playersRole.getRolePrefix() + player.getName() + " §aaufgrund eines " + getName()));
                    player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
                }

                @Override
                public void stop() {
                    if (detectedPlayersRole.roleId == 2) {

                        if (randomPlayer.hasMetadata("INNOCENT_TICKET")) {
                            double chance = RANDOM.nextDouble();
                            if (chance <= 0.75) {
                                Bukkit.broadcastMessage(plugin.serverPrefix + detectedPlayersRole.getRolePrefix() + randomPlayer.getName() + " §ewurde vom " + getName() + " §eüberprüft");
                            } else {
                                Bukkit.broadcastMessage(plugin.serverPrefix + detectedPlayersRole.getRolePrefix() + randomPlayer.getName() + " §ewurde von einem " + getName() + " §eenttarnt");
                                plugin.setMetadata(randomPlayer, "BUSTED_TRAITOR", true);

                                ItemStack traitorChestplate = new ItemBuilder(Material.LEATHER_CHESTPLATE, 1).build();
                                LeatherArmorMeta traitorMeta = (LeatherArmorMeta) traitorChestplate.getItemMeta();
                                traitorMeta.setDisplayName("§4ENTTARNT");
                                traitorMeta.setColor(Color.RED);
                                traitorMeta.setUnbreakable(true);
                                traitorChestplate.setItemMeta(traitorMeta);

                                randomPlayer.getInventory().setChestplate(traitorChestplate);
                                plugin.scoreboardManager.generateScoreboard();

                                player.sendMessage(plugin.serverPrefix + "§cDu bist aufgeflogen!");
                                player.sendMessage(plugin.serverPrefix + "§cAndere Spieler sehen dich nun als §f" + detectedPlayersRole.getRolePrefix() + detectedPlayersRole.getRoleName() + "§c!");
                            }

                            plugin.removeMetadata(randomPlayer, "INNOCENT_TICKET");
                        } else {
                            Bukkit.broadcastMessage(plugin.serverPrefix + detectedPlayersRole.getRolePrefix() + randomPlayer.getName() + " §ewurde von einem " + getName() + " §eenttarnt");
                            plugin.setMetadata(randomPlayer, "BUSTED_TRAITOR", true);

                            ItemStack traitorChestplate = new ItemBuilder(Material.LEATHER_CHESTPLATE, 1).build();
                            LeatherArmorMeta traitorMeta = (LeatherArmorMeta) traitorChestplate.getItemMeta();
                            traitorMeta.setDisplayName("§4ENTTARNT");
                            traitorMeta.setColor(Color.RED);
                            traitorMeta.setUnbreakable(true);
                            traitorChestplate.setItemMeta(traitorMeta);

                            randomPlayer.getInventory().setChestplate(traitorChestplate);
                            plugin.scoreboardManager.generateScoreboard();
                        }
                    } else
                        Bukkit.broadcastMessage(plugin.serverPrefix + detectedPlayersRole.getRolePrefix() + randomPlayer.getName() + " §ewurde vom " + getName() + " §eüberprüft");
                }

                @Override
                public void second(int v0) {
                    if (!randomPlayer.hasMetadata("RANDOM_PLAYER_FOR_RANDOMTESTER")) {
                        plugin.roleManager.getRole(1).getPlayers().forEach(detective -> detective.sendMessage(plugin.serverPrefix + "§eDie Detektei stoppte die Ermittlung gegen §a" + randomPlayer.getName() + " §eaufgrund seines Todesfalls"));
                        countdown.stopCountdown(false);
                    }

                    switch (v0) {
                        case 20 -> {
                            if (detectedPlayersRole.roleId == 2) {
                                plugin.roleManager.getRole(2).getPlayers().forEach(traitor -> {
                                    traitor.sendMessage(plugin.serverPrefix +
                                            "§cDer " + getName() + " §cvon " +
                                            playersRole.getRolePrefix() + player.getName() +
                                            " §centtarnt in §e" + v0 + " §cSekunden " +
                                            detectedPlayersRole.getRolePrefix() + randomPlayer.getName() +
                                            "§c!");
                                    traitor.playSound(traitor.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 2.0f);
                                });
                            }
                        }
                    }
                }

                @Override
                public void sleep() {
                }
            });
            countdown.startCountdown(40, false);
        }
    }

    private Player getRandomPlayer() {
        List<Player> DETECTIVES_EXCLUDED = new ArrayList<>();
        DETECTIVES_EXCLUDED.addAll(this.plugin.roleManager.getRole(0).getPlayers());
        DETECTIVES_EXCLUDED.addAll(this.plugin.roleManager.getRole(2).getPlayers());

        if (DETECTIVES_EXCLUDED.isEmpty()) return null;

        int randomIndex = new Random().nextInt(DETECTIVES_EXCLUDED.size());
        return DETECTIVES_EXCLUDED.get(randomIndex);
    }
}