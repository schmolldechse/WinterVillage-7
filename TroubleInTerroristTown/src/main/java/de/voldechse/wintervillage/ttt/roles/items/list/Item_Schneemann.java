package de.voldechse.wintervillage.ttt.roles.items.list;

import de.voldechse.wintervillage.library.document.Document;
import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.gamestate.Types;
import de.voldechse.wintervillage.ttt.roles.items.RoleItem;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Item_Schneemann extends RoleItem implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    public static Map<Snowman, Integer> SNOWMAN_LIST = new HashMap<>();

    @Override
    public String getName() {
        return "§fSchneemann";
    }

    @Override
    public ItemStack getIcon(boolean traitor) {
        return new ItemBuilder(Material.SNOW_BLOCK, 1, this.getName())
                .lore("",
                        "§7Schieße einen Spieler",
                        "§7mit einem Pfeil ab, während",
                        "§7du diesen Schneeblock im",
                        "§7Inventar behältst.",
                        "§7Dieser verwandelt sich",
                        "§7nach kurzer Zeit in",
                        "§7einen Schneemann.",
                        "§7Nun erhält dieser kontinuierlich",
                        "§7Schaden, bis er stirbt",
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
    public boolean alradyInInventory(Player player) {
        return player.getInventory().contains(Material.SNOW_BLOCK);
    }

    @Override
    public int howOftenBuyable() {
        return 1;
    }

    @Override
    public void equipItems(Player player) {
        player.getInventory().addItem(new ItemBuilder(Material.SNOW_BLOCK, 1, this.getName())
                .lore("",
                        "§7Schieß einen Spieler",
                        "§7mit einem Pfeil ab, wärhend",
                        "§7du diesen Schneeblock im",
                        "§7Inventar behälst.",
                        "§7Dieser verwandelt sich",
                        "§7nach kurzer Zeit in",
                        "§7einen Schneemann.",
                        "§7Nun erhält dieser kontinuierlich",
                        "§7Schaden, bis er stirbt")
                .build());
    }

    @EventHandler
    public void execute(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        if (this.plugin.gameStateManager.currentGameState().getGameStatePhase() != Types.INGAME) return;

        if (check(player)) {
            ItemStack snowBlock = find(player, Material.SNOW_BLOCK);
            snowBlock.setAmount(snowBlock.getAmount() - 1);

            event.setConsumeItem(false);
            player.updateInventory();

            this.plugin.setMetadata(player, "SHOOT_SNOWMAN", true);
        }
    }

    @EventHandler
    public void execute(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof Arrow arrow
                && arrow.getShooter() instanceof Player shooter) {
            if (shooter.hasMetadata("SHOOT_SNOWMAN")) {
                this.plugin.removeMetadata(shooter, "SHOOT_SNOWMAN");

                this.plugin.setMetadata(event.getEntity(), "SNOWMAN_ARROW", true);
            }
        }
    }

    @EventHandler
    public void execute(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Arrow
                && event.getEntity().hasMetadata("SNOWMAN_ARROW")
                && event.getHitEntity() != null
                && event.getHitEntity() instanceof Player player) {

            if (this.plugin.gameManager.isSpectator(player)) return;

            if (this.plugin.roleManager.getRole(player) == null) return;

            if (player.hasMetadata("SNOWMAN")) return;

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.getFreezeTicks() >= 140) {
                        player.setCollidable(false);

                        Document document = new Document("defrosting", false);

                        plugin.setMetadata(player, "SNOWMAN", document);
                        plugin.setMetadata(player, "LAST_DAMAGER", ((Player) event.getEntity().getShooter()).getUniqueId());

                        Snowman snowman = player.getWorld().spawn(player.getLocation(), Snowman.class);
                        snowman.setCustomName("§b§lEINGEFROREN");
                        snowman.setCustomNameVisible(true);
                        snowman.setAI(false);
                        snowman.setAware(false);
                        snowman.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(player.getHealth());
                        snowman.setHealth(player.getHealth());

                        plugin.setMetadata(snowman, "PLAYER_UUID", player.getUniqueId());

                        Bukkit.getOnlinePlayers().forEach(online -> online.hidePlayer(plugin.getInstance(), player));

                        startTickingToDeath(snowman);

                        cancel();
                    } else player.setFreezeTicks(player.getFreezeTicks() + 5);
                }
            }.runTaskTimer(this.plugin.getInstance(), 5L, 1L);

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void execute(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player && player.hasMetadata("SNOWMAN")) {
            event.setCancelled(true);
            return;
        }

        if (event.getEntity() instanceof Snowman && Item_Schneemann.SNOWMAN_LIST.containsKey(event.getEntity())) {

            Types gamePhase = this.plugin.gameStateManager.currentGameState().getGameStatePhase();
            if (gamePhase != Types.INGAME) {
                event.setCancelled(true);
                return;
            }

            Entity damager = event.getDamager();

            if (damager instanceof Arrow) {
                Player shooter = Objects.requireNonNull((Player) ((Arrow) damager).getShooter());

                Snowman snowman = (Snowman) event.getEntity();
                if (!snowman.hasMetadata("PLAYER_UUID")) return;

                Player frozen = Bukkit.getPlayer(UUID.fromString(snowman.getMetadata("PLAYER_UUID").get(0).asString()));
                frozen.setHealth(snowman.getHealth());

                this.plugin.setMetadata(frozen, "LAST_DAMAGER", shooter.getUniqueId());
            } else if (damager instanceof Player) {
                Snowman snowman = (Snowman) event.getEntity();
                if (!snowman.hasMetadata("PLAYER_UUID")) return;

                Player frozen = Bukkit.getPlayer(UUID.fromString(snowman.getMetadata("PLAYER_UUID").get(0).asString()));
                frozen.setHealth(snowman.getHealth());

                this.plugin.setMetadata(frozen, "LAST_DAMAGER", damager.getUniqueId());
            }
        }
    }

    @EventHandler
    public void execute(EntityBlockFormEvent event) {
        if (event.getEntity() instanceof Snowman && event.getNewState().getType() == Material.SNOW)
            event.setCancelled(true);
    }

    @EventHandler
    public void execute(EntityDeathEvent event) {
        if (event.getEntity() instanceof Snowman) {
            event.setDroppedExp(0);
            event.getDrops().clear();
        }
    }

    @EventHandler
    public void execute(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Snowman) event.setCancelled(true);
        if (event.getEntity() instanceof Player player && player.hasMetadata("SNOWMAN")) event.setCancelled(true);
    }

    private void startTickingToDeath(Snowman snowman) {
        if (!snowman.hasMetadata("PLAYER_UUID")) return;

        Player player = Bukkit.getPlayer(UUID.fromString(snowman.getMetadata("PLAYER_UUID").get(0).asString()));
        if (player == null) return;

        BukkitRunnable tickingToDeath = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.hasMetadata("SNOWMAN")) {
                    SNOWMAN_LIST.remove(snowman);
                    cancel();
                    return;
                }

                if (snowman.getHealth() <= 1) {
                    snowman.setHealth(0);
                    player.setHealth(0);
                } else if (snowman.getHealth() > 1) {
                    snowman.setHealth(snowman.getHealth() - 1);
                    player.setHealth(snowman.getHealth());
                }

                player.setFreezeTicks(140);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.0f, 1.0f);

                if (snowman.getHealth() <= 0) {
                    plugin.removeMetadata(player, "SNOWMAN");
                    SNOWMAN_LIST.remove(snowman);
                    snowman.remove();
                    cancel();
                }
            }
        };
        tickingToDeath.runTaskTimer(this.plugin.getInstance(), 0L, 40L);

        SNOWMAN_LIST.put(snowman, tickingToDeath.getTaskId());
    }

    private ItemStack find(Player player, Material material) {
        for (ItemStack itemStack : player.getInventory().getContents())
            if (itemStack != null && itemStack.getType() == material) return itemStack;
        return null;
    }

    private boolean check(Player player) {
        ItemStack[] inventory = player.getInventory().getContents();
        int snowballBlockIndex = -1;
        int arrowIndex = -1;

        for (int i = 0; i < inventory.length; i++) {
            ItemStack itemStack = inventory[i];
            if (itemStack == null) continue;

            if (itemStack.getType() == Material.SNOW_BLOCK && snowballBlockIndex == -1) snowballBlockIndex = i;
            else if (itemStack.getType() == Material.ARROW && arrowIndex == -1) arrowIndex = i;

            if (snowballBlockIndex != -1 && arrowIndex != -1) break;
        }

        return snowballBlockIndex != -1 && arrowIndex != -1 && snowballBlockIndex < arrowIndex;
    }
}