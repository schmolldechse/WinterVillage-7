package de.voldechse.wintervillage.ttt.game.tester;

import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.gamestate.Types;
import de.voldechse.wintervillage.ttt.roles.Role;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Powerable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class Tester implements Listener {
    
    private final TTT plugin;
    private boolean isUsed, trapActive;
    private long startTime_Tester, startTime_Trap;

    private final List<UUID> ALREADY_TESTED;

    public Tester() {
        this.plugin = InjectionLayer.ext().instance(TTT.class);
        
        this.isUsed = false;

        this.ALREADY_TESTED = new ArrayList<>();
    }

    @EventHandler
    public void activation(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getClickedBlock() != null
                && event.getClickedBlock().getType() == Material.STONE_BUTTON
                && event.getClickedBlock().getLocation().equals(this.plugin.testerSetup.activationButton)) {
            event.setCancelled(false);

            Powerable attachable = (Powerable) event.getClickedBlock().getBlockData();
            if (attachable.isPowered()) return;

            if (this.plugin.gameManager.isSpectator(player)) return;

            if (this.plugin.gameStateManager.currentGameState().getGameStatePhase() != Types.INGAME) return;

            if (this.plugin.roleManager.getRole(player) == null) {
                player.sendMessage(this.plugin.serverPrefix + "§cSomething seems to be not working. Try again");
                return;
            }
            Role role = this.plugin.roleManager.getRole(player);

            if (this.ALREADY_TESTED.contains(player.getUniqueId())) {
                player.sendMessage(this.plugin.serverPrefix + "§cDu bist schon im Tester gewesen");
                return;
            }

            if (role.roleId == 1) {
                player.sendMessage(this.plugin.serverPrefix + "§cDu kannst den Tester als §r" + role.getRolePrefix() + role.getRoleName() + " §cnicht betreten");
                return;
            }

            if (trapActive) {
                player.sendMessage(this.plugin.serverPrefix + "§cDu kannst den Tester aktuell nicht betreten");
                return;
            }

            if (isUsed) {
                player.sendMessage(this.plugin.serverPrefix + "§cDer Tester wird bereits genutzt");
                return;
            }

            long current = System.currentTimeMillis();
            BigDecimal remaining = BigDecimal.valueOf(current - startTime_Tester).divide(BigDecimal.valueOf(1000), 1, RoundingMode.HALF_UP);

            if (remaining.compareTo(BigDecimal.TEN) < 0) {
                player.sendMessage(this.plugin.serverPrefix + "§cDu musst noch §b" + BigDecimal.valueOf(10).subtract(remaining) + " §cSekunden warten, bis du den Tester betreten kannst");
                return;
            }

            player.teleport(this.plugin.testerSetup.playerSpawn);

            for (Entity nearby : player.getNearbyEntities(5, 5, 5)) {
                if (!(nearby instanceof Player)) continue;
                ((Player) nearby).playSound(player.getLocation(), Sound.BLOCK_PISTON_EXTEND, 1.0f, 1.0f);
                if (nearby.getUniqueId().equals(player.getUniqueId())) continue;

                nearby.teleport(this.plugin.testerSetup.outsideTester);
            }

            Bukkit.broadcastMessage(this.plugin.serverPrefix + "§a" + player.getName() + " §7hat den Tester betreten");
            isUsed = true;

            Arrays.stream(this.plugin.testerSetup.barrier).forEach(location -> location.getBlock().setType(this.plugin.testerSetup.barrierMaterial));

            Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(0, 255, 0), 1.5f);

            new BukkitRunnable() {
                double y = 2;
                double radius = 1;

                @Override
                public void run() {
                    for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 4) {
                        Location location = player.getLocation();
                        location.add(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
                        location.getWorld().spawnParticle(Particle.REDSTONE, location, 10, 0, 0, 0, 0.1, dustOptions);
                        //this.plugin.gameManager.spawnParticle(location, Particle.VILLAGER_HAPPY, 10, 20);
                    }

                    y -= 2.0 / 16;

                    if (y <= 0) cancel();
                }
            }.runTaskTimer(this.plugin.getInstance(), 0L, 10L);

            Bukkit.getScheduler().runTaskLater(this.plugin.getInstance(), new Runnable() {
                @Override
                public void run() {
                    ALREADY_TESTED.add(player.getUniqueId());

                    if (role.roleId == 2) {

                        if (player.hasMetadata("INNOCENT_TICKET")) {
                            double chance = new Random().nextDouble();
                            if (chance <= .75) {
                                for (Entity nearby : player.getNearbyEntities(5, 5, 5)) {
                                    if (!(nearby instanceof Player)) continue;
                                    ((Player) nearby).playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                                }

                                for (Location lamps : plugin.testerSetup.testerLamps) {
                                    lamps.getBlock().setType(plugin.testerSetup.tested);
                                }
                            } else {
                                for (Entity nearby : player.getNearbyEntities(5, 5, 5)) {
                                    if (!(nearby instanceof Player)) continue;
                                    ((Player) nearby).playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1.0f, 1.0f);
                                }

                                for (Location lamps : plugin.testerSetup.testerLamps) {
                                    lamps.getBlock().setType(plugin.testerSetup.busted);
                                }

                                plugin.setMetadata(player, "BUSTED_TRAITOR", true);

                                ItemStack traitorChestplate = new ItemBuilder(Material.LEATHER_CHESTPLATE, 1).build();
                                LeatherArmorMeta traitorMeta = (LeatherArmorMeta) traitorChestplate.getItemMeta();
                                traitorMeta.setDisplayName("§4ENTTARNT");
                                traitorMeta.setColor(Color.RED);
                                traitorMeta.setUnbreakable(true);
                                traitorChestplate.setItemMeta(traitorMeta);

                                player.getInventory().setChestplate(traitorChestplate);

                                plugin.scoreboardManager.generateScoreboard();

                                player.sendMessage(plugin.serverPrefix + "§cDu bist aufgeflogen!");
                                //player.sendMessage(this.plugin.serverPrefix + "§cAndere Spieler sehen dich nun als §4Traitor§c!");
                                player.sendMessage(plugin.serverPrefix + "§cAndere Spieler sehen dich nun als §r" + role.getRolePrefix() + role.getRoleName() + "§c!");
                            }

                            plugin.removeMetadata(player, "INNOCENT_TICKET");
                        } else {
                            for (Entity nearby : player.getNearbyEntities(5, 5, 5)) {
                                if (!(nearby instanceof Player)) continue;
                                ((Player) nearby).playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1.0f, 1.0f);
                            }

                            for (Location lamps : plugin.testerSetup.testerLamps) {
                                lamps.getBlock().setType(plugin.testerSetup.busted);
                            }

                            plugin.setMetadata(player, "BUSTED_TRAITOR", true);

                            ItemStack traitorChestplate = new ItemBuilder(Material.LEATHER_CHESTPLATE, 1).build();
                            LeatherArmorMeta traitorMeta = (LeatherArmorMeta) traitorChestplate.getItemMeta();
                            traitorMeta.setDisplayName("§4ENTTARNT");
                            traitorMeta.setColor(Color.RED);
                            traitorMeta.setUnbreakable(true);
                            traitorChestplate.setItemMeta(traitorMeta);

                            player.getInventory().setChestplate(traitorChestplate);

                            plugin.scoreboardManager.generateScoreboard();

                            player.sendMessage(plugin.serverPrefix + "§cDu bist aufgeflogen!");
                            player.sendMessage(plugin.serverPrefix + "§cAndere Spieler sehen dich nun als §r" + role.getRolePrefix() + role.getRoleName() + "§c!");
                        }

                    } else {
                        for (Entity nearby : player.getNearbyEntities(5, 5, 5)) {
                            if (!(nearby instanceof Player)) continue;
                            ((Player) nearby).playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                        }

                        for (Location lamps : plugin.testerSetup.testerLamps) {
                            lamps.getBlock().setType(plugin.testerSetup.tested);
                        }
                    }

                    for (Location barrier : plugin.testerSetup.barrier) {
                        barrier.getBlock().setType(Material.AIR);
                    }

                    for (Entity nearby : player.getNearbyEntities(5, 5, 5)) {
                        if (!(nearby instanceof Player)) continue;
                        ((Player) nearby).playSound(player.getLocation(), Sound.BLOCK_PISTON_CONTRACT, 1.0f, 1.0f);
                    }

                    startTime_Tester = System.currentTimeMillis();
                    isUsed = false;

                    Bukkit.getScheduler().runTaskLater(plugin.getInstance(), new Runnable() {
                        @Override
                        public void run() {
                            for (Location lamps : plugin.testerSetup.testerLamps)
                                lamps.getBlock().setType(plugin.testerSetup.idling);
                        }
                    }, 40L);
                }
            }, 8 * 20L);
        }

        if (event.getClickedBlock() != null
                && event.getClickedBlock().getType() == Material.STONE_BUTTON
                && event.getClickedBlock().getLocation().equals(this.plugin.testerSetup.traitorTrap)) {
            event.setCancelled(false);

            Powerable attachable = (Powerable) event.getClickedBlock().getBlockData();
            if (attachable.isPowered()) return;

            if (this.plugin.gameManager.isSpectator(player)) return;

            if (this.plugin.gameStateManager.currentGameState().getGameStatePhase() != Types.INGAME) return;

            if (this.plugin.roleManager.getRole(player) == null) return;
            Role role = this.plugin.roleManager.getRole(player);

            if (role.roleId != 2) return;

            if (player.hasMetadata("TRAP_TICKET")) return;

            long current = System.currentTimeMillis();
            BigDecimal remaining = BigDecimal.valueOf(current - startTime_Trap).divide(BigDecimal.valueOf(1000), 1, RoundingMode.HALF_UP);

            if (remaining.compareTo(BigDecimal.valueOf(30)) < 0) {
                player.sendMessage(this.plugin.serverPrefix + "§cDu musst noch §b" + BigDecimal.valueOf(30).subtract(remaining) + " §cSekunden warten, bis du die Falle aktivieren kannst");
                return;
            }

            if (trapActive) {
                player.sendMessage(this.plugin.serverPrefix + "§cDie Falle ist bereits aktiv");
                return;
            }

            trapActive = true;
            Bukkit.broadcastMessage(this.plugin.serverPrefix + "§eDie Falle wurde ausgelöst");
            Bukkit.getOnlinePlayers().forEach(online -> online.playSound(online.getLocation(), Sound.BLOCK_PISTON_EXTEND, 1.0f, 1.0f));

            fillRectangle(this.plugin.testerSetup.cornerA, this.plugin.testerSetup.cornerB, Material.AIR);

            new BukkitRunnable() {
                @Override
                public void run() {
                    fillRectangle(plugin.testerSetup.cornerA, plugin.testerSetup.cornerB, plugin.testerSetup.floorMaterial);
                    Bukkit.getOnlinePlayers().forEach(online -> online.playSound(online.getLocation(), Sound.BLOCK_PISTON_CONTRACT, 1.0f, 1.0f));

                    trapActive = false;
                    startTime_Trap = System.currentTimeMillis();
                }
            }.runTaskLater(this.plugin.getInstance(), 5 * 20L);
        }
    }

    @EventHandler
    public void execute(ProjectileHitEvent event) {
        if (event.getHitBlock() != null) {
            Location hitBlock = null;

            for (BlockFace blockFace : BlockFace.values()) {
                if (event.getHitBlock().getRelative(blockFace).getType() != Material.STONE_BUTTON) continue;
                hitBlock = event.getHitBlock().getRelative(blockFace).getLocation();
            }

            if (hitBlock == null) return;

            if (hitBlock.equals(this.plugin.testerSetup.traitorTrap)) {
                Player shooter = (Player) event.getEntity().getShooter();

                Powerable attachable = (Powerable) hitBlock.getBlock().getBlockData();
                if (attachable.isPowered()) return;

                if (this.plugin.gameManager.isSpectator(shooter)) return;

                if (this.plugin.gameStateManager.currentGameState().getGameStatePhase() != Types.INGAME) return;

                if (this.plugin.roleManager.getRole(shooter) == null) return;
                Role role = this.plugin.roleManager.getRole(shooter);

                if (role.roleId != 2) return;

                if (shooter.hasMetadata("TRAP_TICKET")) return;

                long current = System.currentTimeMillis();
                BigDecimal remaining = BigDecimal.valueOf(current - startTime_Trap).divide(BigDecimal.valueOf(1000), 1, RoundingMode.HALF_UP);

                if (remaining.compareTo(BigDecimal.valueOf(30)) < 0) {
                    shooter.sendMessage(this.plugin.serverPrefix + "§cDu musst noch §b" + BigDecimal.valueOf(30).subtract(remaining) + " §cSekunden warten, bis du die Falle aktivieren kannst");
                    return;
                }

                if (trapActive) {
                    shooter.sendMessage(this.plugin.serverPrefix + "§cDie Falle ist bereits aktiv");
                    return;
                }

                trapActive = true;
                Bukkit.broadcastMessage(this.plugin.serverPrefix + "§eDie Falle wurde ausgelöst");
                Bukkit.getOnlinePlayers().forEach(online -> online.playSound(online.getLocation(), Sound.BLOCK_PISTON_EXTEND, 1.0f, 1.0f));

                fillRectangle(this.plugin.testerSetup.cornerA, this.plugin.testerSetup.cornerB, Material.AIR);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        fillRectangle(plugin.testerSetup.cornerA, plugin.testerSetup.cornerB, plugin.testerSetup.floorMaterial);
                        Bukkit.getOnlinePlayers().forEach(online -> online.playSound(online.getLocation(), Sound.BLOCK_PISTON_CONTRACT, 1.0f, 1.0f));

                        trapActive = false;
                        startTime_Trap = System.currentTimeMillis();
                    }
                }.runTaskLater(this.plugin.getInstance(), 5 * 20L);
            }
        }
    }

    public static void fillRectangle(Location cornerA, Location cornerB, Material blockType) {
        double minX = Math.min(cornerA.getX(), cornerB.getX());
        double minY = Math.min(cornerA.getY(), cornerB.getY());
        double minZ = Math.min(cornerA.getZ(), cornerB.getZ());

        double maxX = Math.max(cornerA.getX(), cornerB.getX());
        double maxY = Math.max(cornerA.getY(), cornerB.getY());
        double maxZ = Math.max(cornerA.getZ(), cornerB.getZ());

        World world = cornerA.getWorld();

        for (int x = (int) minX; x <= maxX; x++) {
            for (int y = (int) minY; y <= maxY; y++) {
                for (int z = (int) minZ; z <= maxZ; z++) {
                    Location location = new Location(world, x, y, z);
                    Block block = location.getBlock();
                    block.setType(blockType);
                }
            }
        }
    }
}