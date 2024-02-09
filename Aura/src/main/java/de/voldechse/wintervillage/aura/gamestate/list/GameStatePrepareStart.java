package de.voldechse.wintervillage.aura.gamestate.list;

import de.voldechse.wintervillage.aura.Aura;
import de.voldechse.wintervillage.aura.gamestate.GameState;
import de.voldechse.wintervillage.aura.gamestate.Types;
import de.voldechse.wintervillage.library.countdown.Countdown;
import de.voldechse.wintervillage.library.countdown.CountdownListener;
import de.voldechse.wintervillage.library.document.Document;
import de.voldechse.wintervillage.library.util.ItemBuilder;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class GameStatePrepareStart extends GameState {

    private final Aura plugin = InjectionLayer.ext().instance(Aura.class);

    private Countdown countdown;

    @Override
    public void startCountdown() {
        this.countdown = new Countdown(this.plugin.getInstance(), new CountdownListener() {
            @Override
            public void start() {
                //TODO: items, teleport
                teleport();

                plugin.gameManager.getLivingPlayers().forEach(player -> {
                    player.getInventory().addItem(new ItemBuilder(Material.STICK, 1)
                            .enchant(Enchantment.KNOCKBACK, 4)
                            .build());

                    ItemStack enderpearls = new ItemStack(Material.ENDER_PEARL, 32);
                    player.getInventory().addItem(enderpearls);

                    player.getInventory().addItem(new ItemBuilder(Material.FISHING_ROD, 1)
                            .build());
                    player.getInventory().addItem(new ItemBuilder(Material.FLINT_AND_STEEL, 1)
                            .build());
                    player.getInventory().addItem(new ItemBuilder(Material.TNT, 64)
                            .build());
                    player.getInventory().addItem(new ItemBuilder(Material.CREEPER_SPAWN_EGG, 16)
                            .build());
                    player.getInventory().addItem(new ItemBuilder(Material.PUMPKIN_PIE, 32)
                            .build());
                    player.getInventory().addItem(new ItemBuilder(Material.GOLDEN_APPLE, 6)
                            .build());

                    ItemStack snowballs = new ItemStack(Material.SNOWBALL, 32);
                    player.getInventory().addItem(snowballs);

                    player.getInventory().setHeldItemSlot(0);
                    player.getInventory().setHelmet(new ItemBuilder(Material.IRON_HELMET).build());
                    player.getInventory().setChestplate(new ItemBuilder(Material.IRON_CHESTPLATE).build());
                    player.getInventory().setLeggings(new ItemBuilder(Material.IRON_LEGGINGS).build());
                    player.getInventory().setBoots(new ItemBuilder(Material.IRON_BOOTS)
                            .enchant(Enchantment.PROTECTION_FALL, 4).build());
                });

                plugin.PLAYING = plugin.gameManager.getLivingPlayers().size();
                plugin.scoreboardManager.generateScoreboard();
                plugin.scoreboardManager.updateScoreboard("currentPlayers", " §a" + plugin.gameManager.getLivingPlayers().size() + "§8/§a" + plugin.PLAYING, "§a");
            }

            @Override
            public void stop() {
                endCountdown();
            }

            @Override
            public void second(int i) {
                switch (i) {
                    case 3 -> {
                        Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle("§c§l3", ""));
                        plugin.gameManager.playSound(Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F);
                    }
                    case 2 -> {
                        Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle("§e§l2", ""));
                        plugin.gameManager.playSound(Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F);
                    }
                    case 1 -> {
                        Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle("§a§l1", ""));
                        plugin.gameManager.playSound(Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F);
                    }
                }
            }

            @Override
            public void sleep() {
            }
        });

        this.countdown.startCountdown(this.plugin.preparingStartCountdown, false);
    }

    @Override
    public void endCountdown() {
        this.plugin.gameStateManager.nextGameState();
    }

    @Override
    public Types getGameStatePhase() {
        return Types.PREPARING_START;
    }

    @Override
    public Countdown getCountdown() {
        return this.countdown;
    }

    private void teleport() {
        Document document = this.plugin.configDocument.getDocument("playerSpawn");
        Location location = new Location(
                Bukkit.getWorld(document.getString("world")),
                document.getDouble("x"),
                document.getDouble("y"),
                document.getDouble("z"),
                document.getFloat("yaw"),
                document.getFloat("pitch")
        );

        Bukkit.getOnlinePlayers().forEach(player -> player.teleport(location));
    }
}