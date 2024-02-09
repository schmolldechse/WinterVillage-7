package de.voldechse.wintervillage.ttt.roles.items.list;

import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.game.corpse.CorpseEntity;
import de.voldechse.wintervillage.ttt.roles.items.RoleItem;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Item_Kannibalismus extends RoleItem implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    @Override
    public String getName() {
        return "§cKannibalismus";
    }

    @Override
    public ItemStack getIcon(boolean traitor) {
        return new ItemBuilder(Material.ROTTEN_FLESH, 1, this.getName())
                .lore("",
                        "§7Iss eine Leiche",
                        "§7und erhalte dadurch 2",
                        "§7weitere Herzen,",
                        "§7die dir im nächsten Kampf",
                        "§7nützlich sein können",
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
        player.getInventory().addItem(new ItemBuilder(Material.ROTTEN_FLESH, 1, this.getName())
                .lore("",
                        "§7Iss eine Leiche",
                        "§7und erhalte dadurch 2",
                        "§7weitere Herzen,",
                        "§7die dir im nächsten Kamp",
                        "§7nützlich sein können")
                .build());
    }

    @Override
    public boolean alradyInInventory(Player player) {
        return player.getInventory().contains(Material.ROTTEN_FLESH);
    }

    @Override
    public int howOftenBuyable() {
        return 3;
    }

    @EventHandler
    public void execute(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();

        if (event.getRightClicked().getType() == EntityType.ARMOR_STAND) {
            event.setCancelled(true);

            if (player.getItemInHand().getType() == Material.AIR) return;
            if (!player.getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase(this.getName())) return;

            if (this.plugin.gameManager.isSpectator(player)) return;

            if (this.plugin.roleManager.getRole(player) == null) return;

            if (!this.plugin.roleManager.isPlayerAssigned(player, 2)) return;

            ArmorStand armorStand = (ArmorStand) event.getRightClicked();
            if (!armorStand.hasMetadata("CORPSE_entityId")) return;

            int entityId = armorStand.getMetadata("CORPSE_entityId").get(0).asInt();
            CorpseEntity corpse = this.plugin.CORPSES_MAP.get(entityId);
            if (corpse == null) return;

            player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
            if (corpse.corpseData.getDocument().contains("fakeCorpse")) {
                player.sendMessage(this.plugin.serverPrefix + "§eDir ist schwindelig geworden ...");
                player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 10 * 20, 2, true, false));
            }

            armorStand.remove();
            this.plugin.SNEAK_ARMORSTANDS_USELESS.remove(entityId);
            Bukkit.getOnlinePlayers().forEach(players -> corpse.despawnCorpse(players, entityId));
            this.plugin.CORPSES_MAP.remove(entityId);

            new BukkitRunnable() {
                int index = 0;

                @Override
                public void run() {
                    index++;

                    player.setHealthScale(player.getHealthScale() + 1);
                    player.playSound(corpse.corpseData.getLocation(), Sound.ENTITY_GENERIC_EAT, 1.0f, 1.0f);

                    if (index == 4) cancel();
                }
            }.runTaskTimer(this.plugin.getInstance(), 0L, 5L);
        }
    }
}