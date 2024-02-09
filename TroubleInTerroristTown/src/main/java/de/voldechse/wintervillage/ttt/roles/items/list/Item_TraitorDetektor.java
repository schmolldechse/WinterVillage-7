package de.voldechse.wintervillage.ttt.roles.items.list;

import de.voldechse.wintervillage.library.countdown.Countdown;
import de.voldechse.wintervillage.library.countdown.CountdownListener;
import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.gamestate.Types;
import de.voldechse.wintervillage.ttt.roles.Role;
import de.voldechse.wintervillage.ttt.roles.items.RoleItem;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.data.Lightable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.List;

public class Item_TraitorDetektor extends RoleItem implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    private Countdown countdown;

    @Override
    public String getName() {
        //return "§bTraitor-Detektor";
        return "§bMr. Frost-Detektor";
    }

    @Override
    public ItemStack getIcon(boolean traitor) {
        return new ItemBuilder(Material.REDSTONE_LAMP, 1, this.getName())
                .lore("",
                        "§7Der Detektor schlägt Alarm,",
                        //"§7sobald sich ein Traitor im",
                        "§7sobald sich ein §4Krampus §7im",
                        "§7Umkreis von 15 Blöcken befindet.",
                        "§7Nach 60 Sekunden zerstört sich",
                        "§7der Detektor von selbst",
                        "",
                        "§ePreis: " + (traitor ? "§c" : "§a") + this.getNeededPoints() + " Punkte",
                        "",
                        "§a<Klicke zum Kaufen>")
                .build();
    }

    @Override
    public int getNeededPoints() {
        return 4;
    }

    @Override
    public int howOftenBuyable() {
        return 1;
    }

    @Override
    public boolean alradyInInventory(Player player) {
        return player.getInventory().contains(Material.REDSTONE_LAMP);
    }

    @Override
    public void equipItems(Player player) {
        player.getInventory().addItem(new ItemBuilder(Material.REDSTONE_LAMP, 1, this.getName())
                .lore("",
                        "§7Der Detektor schlägt Alarm,",
                        //"§7sobald sich ein Traitor im",
                        "§7sobald sich ein §4Krampus §7im",
                        "§7Umkreis von 15 Blöcken befindet.",
                        "§7Nach 60 Sekunden zerstört sich",
                        "§7der Detektor von selbst")
                .build());
    }

    @EventHandler
    public void execute(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (event.getItemInHand() != null
                && event.getItemInHand().getItemMeta().getDisplayName() != null
                && event.getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase(this.getName())) {

            if (this.plugin.gameManager.isSpectator(player)) {
                event.setCancelled(true);
                return;
            }

            if (this.plugin.roleManager.getRole(player) == null) {
                event.setCancelled(true);
                return;
            }

            if (!this.plugin.roleManager.isPlayerAssigned(player, 1)) {
                event.setCancelled(true);
                return;
            }

            Location testerCenter = calculateCenter(this.plugin.testerSetup.cornerA, this.plugin.testerSetup.cornerB);
            if (event.getBlock().getLocation().distanceSquared(testerCenter) <= 20) {
                event.setCancelled(true);
                return;
            }

            Location blockLocation = event.getBlock().getLocation();

            event.setCancelled(false);

            Lightable lightable = (Lightable) event.getBlock().getBlockData();
            lightable.setLit(false);
            event.getBlock().setBlockData(lightable);

            ArmorStand armorStand = blockLocation.getWorld().spawn(blockLocation.add(.5, -.5, .5), ArmorStand.class);

            armorStand.setVisible(false);
            armorStand.setInvulnerable(true);
            armorStand.setGravity(false);

            armorStand.setCustomNameVisible(true);
            armorStand.setCustomName("§b§l60");

            countdown = new Countdown(this.plugin.getInstance(), new CountdownListener() {
                @Override
                public void start() {
                }

                @Override
                public void stop() {
                    armorStand.remove();
                    event.getBlock().setType(Material.AIR);
                    blockLocation.getWorld().playSound(blockLocation, Sound.BLOCK_LODESTONE_BREAK, 1.0f, 1.0f);
                }

                @Override
                public void second(int v0) {
                    armorStand.setCustomName("§f§lSelbstzerstörung in §c§l" + v0);

                    if (plugin.gameStateManager.currentGameState().getGameStatePhase() != Types.INGAME) {
                        event.getBlock().setType(Material.AIR);
                        armorStand.remove();
                        countdown.stopCountdown(false);
                    }

                    for (Player traitor : getNearPlayers(blockLocation, 25)) {
                        if (plugin.gameManager.isSpectator(traitor)) continue;
                        if (!plugin.roleManager.isPlayerAssigned(traitor)) continue;
                        if (plugin.roleManager.getRole(traitor).roleId != 2) continue;
                        if (traitor.hasMetadata("TRAITOR_DETECTOR_WARNING")) continue;
                        if (traitor.hasMetadata("BUSTED_TRAITOR")) continue;

                        plugin.setMetadata(traitor, "TRAITOR_DETECTOR_WARNING", true);
                        traitor.sendMessage(plugin.serverPrefix + "§4§lAchtung! §cIn deiner Nähe befindet sich ein " + getName());
                        traitor.playSound(traitor.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                    }

                    for (Player traitor : getNearPlayers(blockLocation, 15)) {
                        if (plugin.gameManager.isSpectator(traitor)) continue;
                        if (!plugin.roleManager.isPlayerAssigned(traitor)) continue;
                        if (plugin.roleManager.getRole(traitor).roleId != 2) continue;
                        if (traitor.hasMetadata("BUSTED_TRAITOR")) continue;

                        Role detectedTraitor = plugin.roleManager.getRole(traitor);

                        plugin.roleManager.getRole(1).getPlayers().forEach(detective -> {
                            detective.sendMessage(plugin.serverPrefix + "§4" + traitor.getName() + " §ewurde von einem " + getName() + " §eenttarnt");
                            detective.playSound(detective.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                            plugin.roleManager.changeShopPoints(detective, 2);
                        });
                        plugin.setMetadata(traitor, "BUSTED_TRAITOR", true);

                        ItemStack traitorChestplate = new ItemBuilder(Material.LEATHER_CHESTPLATE, 1).build();
                        LeatherArmorMeta traitorMeta = (LeatherArmorMeta) traitorChestplate.getItemMeta();
                        traitorMeta.setDisplayName("§4ENTTARNT");
                        traitorMeta.setColor(Color.RED);
                        traitorMeta.setUnbreakable(true);
                        traitorChestplate.setItemMeta(traitorMeta);

                        traitor.getInventory().setChestplate(traitorChestplate);
                        plugin.scoreboardManager.generateScoreboard();

                        traitor.sendMessage(plugin.serverPrefix + "§4§lAchtung! §cDu wurdest von einem " + getName() + " §centtarnt!");
                        traitor.sendMessage(plugin.serverPrefix + "§cAndere Spieler sehen dich nun als §f" + detectedTraitor.getRolePrefix() + detectedTraitor.getRoleName() + "§c!");
                        traitor.playSound(traitor.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);

                        lightable.setLit(true);
                        event.getBlock().setBlockData(lightable);
                    }
                }

                @Override
                public void sleep() {
                }
            });
            countdown.startCountdown(60, false);
        }
    }

    private Location calculateCenter(Location cornerA, Location cornerB) {
        double minX = Math.min(cornerA.getX(), cornerB.getX());
        double minY = Math.min(cornerA.getY(), cornerB.getY());
        double minZ = Math.min(cornerA.getZ(), cornerB.getZ());

        double maxX = Math.max(cornerA.getX(), cornerB.getX());
        double maxY = Math.max(cornerA.getY(), cornerB.getY());
        double maxZ = Math.max(cornerA.getZ(), cornerB.getZ());

        double centerX = (minX + maxX) / 2.0;
        double centerY = (minY + maxY) / 2.0;
        double centerZ = (minZ + maxZ) / 2.0;

        return new Location(cornerA.getWorld(), centerX, centerY, centerZ);
    }

    private List<Player> getNearPlayers(Location location, double radius) {
        List<Player> nearbyPlayers = new ArrayList<>();
        double radiusSquared = radius * radius;

        this.plugin.roleManager.getPlayerList().forEach(player -> {
            if (player.getLocation().distanceSquared(location) <= radiusSquared) nearbyPlayers.add(player);
        });

        return nearbyPlayers;
    }
}