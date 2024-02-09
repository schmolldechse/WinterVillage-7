package de.voldechse.wintervillage.ttt.roles.items.list;

import de.voldechse.wintervillage.library.document.Document;
import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.roles.Role;
import de.voldechse.wintervillage.ttt.roles.items.RoleItem;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;
import java.util.UUID;

public class Item_Enteiser extends RoleItem implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    @Override
    public String getName() {
        return "§eEnteiser";
    }

    @Override
    public ItemStack getIcon(boolean traitor) {
        return new ItemBuilder(Material.FLINT_AND_STEEL, 1, this.getName())
                .lore("",
                        "§7Taue einen Schneemann auf",
                        "§7und bewahre den Spieler damit",
                        "§7vor dem sicheren Tod",
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
    public int howOftenBuyable() {
        return 2;
    }

    @Override
    public boolean alradyInInventory(Player player) {
        return player.getInventory().contains(Material.FLINT_AND_STEEL);
    }

    @Override
    public void equipItems(Player player) {
        player.getInventory().addItem(new ItemBuilder(Material.FLINT_AND_STEEL, 1, this.getName())
                .lore("",
                        "§7Taue einen Schneemann auf",
                        "§7und bewahre den Spieler damit",
                        "§7vor dem sicheren Tod")
                .build());
    }

    @EventHandler
    public void execute(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();

        if (event.getRightClicked().getType() == EntityType.SNOWMAN) {
            event.setCancelled(true);

            if (player.getItemInHand().getType() == Material.AIR) return;
            if (!player.getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase(this.getName())) return;

            if (this.plugin.gameManager.isSpectator(player)) return;

            if (this.plugin.roleManager.getRole(player) == null) return;

            if (!this.plugin.roleManager.isPlayerAssigned(player, 1)) return;

            Snowman snowman = (Snowman) event.getRightClicked();
            if (!snowman.hasMetadata("PLAYER_UUID")) return;

            UUID uuid = Objects.requireNonNull(UUID.fromString(snowman.getMetadata("PLAYER_UUID").get(0).asString()));
            if (Bukkit.getPlayer(uuid) == null) return;

            Player frosted = Bukkit.getPlayer(uuid);
            if (this.plugin.gameManager.isSpectator(frosted)) return;

            if (!frosted.hasMetadata("SNOWMAN")) return;

            Document document = (Document) frosted.getMetadata("SNOWMAN").get(0).value();
            if (!document.contains("defrosting")) return;
            boolean defrosting = document.getBoolean("defrosting");
            if (defrosting) return;

            document.remove("defrosting");
            document.append("defrosting", true);
            this.plugin.setMetadata(frosted, "SNOWMAN", document);

            this.plugin.setMetadata(player, "DEFROSTING", true);
            tickDefrosting(player, frosted, snowman);
        } else if (event.getRightClicked().getType() != EntityType.SNOWMAN && player.hasMetadata("DEFROSTING"))
            this.plugin.removeMetadata(player, "DEFROSTING");
    }

    private void tickDefrosting(Player player, Player frosted, Snowman snowman) {
        if (!Item_Schneemann.SNOWMAN_LIST.containsKey(snowman)) return;
        int tickingToDeathTask = Item_Schneemann.SNOWMAN_LIST.get(snowman);

        BukkitRunnable defrostingTask = new BukkitRunnable() {
            int seconds = 0;

            @Override
            public void run() {
                seconds++;

                if (!plugin.roleManager.isPlayerAssigned(player)
                        || !player.hasMetadata("DEFROSTING")) {
                    if (!snowman.isDead()) {
                        snowman.setCustomName("§b§lEINGEFROREN");
                        plugin.setMetadata(frosted, "SNOWMAN", new Document("defrosting", false));
                    }

                    if (Bukkit.getScheduler().isCurrentlyRunning(tickingToDeathTask))
                        Bukkit.getScheduler().cancelTask(tickingToDeathTask);

                    cancel();
                    return;
                }

                if (!frosted.hasMetadata("SNOWMAN")) {
                    if (!snowman.isDead()) snowman.remove();
                    if (Bukkit.getScheduler().isCurrentlyRunning(tickingToDeathTask))
                        Bukkit.getScheduler().cancelTask(tickingToDeathTask);

                    cancel();
                    return;
                }

                snowman.setCustomName(currentProgress(seconds, 5));
            }
        };
        defrostingTask.runTaskTimer(this.plugin.getInstance(), 0L, 20L);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.roleManager.isPlayerAssigned(player)
                        || !player.hasMetadata("DEFROSTING")) {
                    if (!snowman.isDead()) {
                        snowman.setCustomName("§b§lEINGEFROREN");
                        plugin.setMetadata(frosted, "SNOWMAN", new Document("defrosting", false));
                    }

                    if (Bukkit.getScheduler().isCurrentlyRunning(tickingToDeathTask))
                        Bukkit.getScheduler().cancelTask(tickingToDeathTask);

                    cancel();
                    return;
                }

                if (!frosted.hasMetadata("SNOWMAN")) {
                    if (!snowman.isDead()) snowman.remove();

                    if (Bukkit.getScheduler().isCurrentlyRunning(tickingToDeathTask))
                        Bukkit.getScheduler().cancelTask(tickingToDeathTask);

                    cancel();
                    return;
                }

                if (Bukkit.getScheduler().isCurrentlyRunning(defrostingTask.getTaskId())) defrostingTask.cancel();
                if (Bukkit.getScheduler().isCurrentlyRunning(tickingToDeathTask))
                    Bukkit.getScheduler().cancelTask(tickingToDeathTask);

                Role defrostingPlayersRole = plugin.roleManager.getRole(player);

                if (plugin.hasMetadata(player, "DEFROSTING"))
                    plugin.removeMetadata(player, "DEFROSTING");
                if (plugin.hasMetadata(frosted, "SNOWMAN"))
                    plugin.removeMetadata(frosted, "SNOWMAN");

                player.sendMessage(plugin.serverPrefix + "§aDu hast " + frosted.getName() + " §agerettet");

                frosted.sendMessage(plugin.serverPrefix + defrostingPlayersRole.getRolePrefix() + player.getName() + " §ahat dich gerettet");
                frosted.playSound(frosted.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);
                frosted.playEffect(EntityEffect.TOTEM_RESURRECT);
                frosted.setFreezeTicks(0);

                Bukkit.getOnlinePlayers().forEach(online -> online.showPlayer(plugin.getInstance(), frosted));

                if (defrostingPlayersRole.roleId == 2 && !frosted.hasMetadata("BUSTED_TRAITOR")) {
                    ItemStack traitorChestplate = new ItemBuilder(Material.LEATHER_CHESTPLATE, 1).build();
                    LeatherArmorMeta traitorMeta = (LeatherArmorMeta) traitorChestplate.getItemMeta();
                    traitorMeta.setDisplayName("§f");
                    traitorMeta.setColor(Color.RED);
                    traitorMeta.setUnbreakable(true);
                    traitorChestplate.setItemMeta(traitorMeta);

                    player.getInventory().setChestplate(traitorChestplate);
                }

                if (!snowman.isDead()) snowman.remove();

                cancel();
            }
        }.runTaskLater(this.plugin.getInstance(), 5 * 20L);
    }

    @EventHandler
    public void execute(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if (player.hasMetadata("DEFROSTING")
                && player.getInventory().getItem(event.getNewSlot()) != null
                && player.getInventory().getItem(event.getNewSlot()).getItemMeta() != null
                && player.getInventory().getItem(event.getNewSlot()).getItemMeta().getDisplayName().equalsIgnoreCase(this.getName()))
            this.plugin.removeMetadata(player, "DEFROSTING");
    }

    private String currentProgress(int currentSeconds, int initialized) {
        if (currentSeconds < 0) {
            currentSeconds = 0;
        } else if (currentSeconds > initialized) {
            currentSeconds = initialized;
        }

        int progress = (int) Math.round((1 - (double) currentSeconds / initialized) * 10);
        StringBuilder progressBar = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            if (i < progress) {
                progressBar.append("§a▓ ");
            } else {
                progressBar.append("§c▒ ");
            }
        }

        return "§8[" + progressBar.toString().trim() + "§8]";
    }
}