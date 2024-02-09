package de.voldechse.wintervillage.ttt.roles.items.list;

import de.voldechse.wintervillage.library.countdown.Countdown;
import de.voldechse.wintervillage.library.countdown.CountdownListener;
import de.voldechse.wintervillage.library.document.Document;
import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.roles.items.RoleItem;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class Item_C4 extends RoleItem implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    public static Map<Player, ArmorStand> RECOGNIZE_PLACED_ARMORSTAND = new HashMap<>();

    @Override
    public String getName() {
        //return "§4C4";
        return "§4Explosive Nussknacker";
    }

    @Override
    public ItemStack getIcon(boolean traitor) {
        return new ItemBuilder(Material.TNT, 1, this.getName())
                .lore("",
                        "§7Platziere eine Bombe,",
                        "§7die du später per",
                        "§7Fernzünder auslösen kannst",
                        "",
                        "§ePreis: " + (traitor ? "§c" : "§a") + this.getNeededPoints() + " Punkte",
                        "",
                        "§a<Klicke zum Kaufen>")
                .build();
    }

    @Override
    public int getNeededPoints() {
        return 3;
    }

    @Override
    public void equipItems(Player player) {
        player.getInventory().addItem(new ItemBuilder(Material.TNT, 1, this.getName())
                .lore("",
                        "§7Platziere eine Bombe,",
                        "§7die du später per",
                        "§7Fernzünder auslösen kannst")
                .build());
        player.getInventory().addItem(new ItemBuilder(Material.LEVER, 1, "§7Fernzünder")
                .lore("",
                        "§7Lass deine platzierte Bombe",
                        "§7mit Rechtsklick explodieren")
                .build());
    }

    @Override
    public int howOftenBuyable() {
        return 2;
    }

    @Override
    public boolean alradyInInventory(Player player) {
        return player.getInventory().contains(Material.TNT) || player.getInventory().contains(Material.LEVER);
    }

    @EventHandler
    public void execute(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getItem() != null
                && event.getItem().getItemMeta().getDisplayName() != null
                && event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase("§7Fernzünder")) {
            event.setCancelled(true);

            if (this.plugin.gameManager.isSpectator(player)) return;

            if (this.plugin.roleManager.getRole(player) == null) return;

            if (!this.plugin.roleManager.isPlayerAssigned(player, 2)) return;

            if (!RECOGNIZE_PLACED_ARMORSTAND.containsKey(player)) {
                player.sendMessage(this.plugin.serverPrefix + "§cDu hast kein §f" + this.getName() + " §cplatziert");
                player.playSound(player.getLocation(), Sound.ENTITY_PILLAGER_DEATH, 1.0f, 1.0f);
                return;
            }

            if (player.hasMetadata("C4_COOLDOWN")) {
                long startTime = player.getMetadata("C4_COOLDOWN").get(0).asLong();
                long current = System.currentTimeMillis();

                BigDecimal remaining = BigDecimal.valueOf(current - startTime).divide(BigDecimal.valueOf(1000), 1, RoundingMode.HALF_UP);

                if (remaining.compareTo(BigDecimal.TEN) < 0) {
                    player.sendMessage(this.plugin.serverPrefix + "§cDu musst noch §b" + BigDecimal.valueOf(10).subtract(remaining) + " §cSekunden warten, bis du " + this.getName() + " §cnutzen kannst!");
                    return;
                }

                this.plugin.removeMetadata(player, "C4_COOLDOWN");
            }

            ArmorStand armorStand = RECOGNIZE_PLACED_ARMORSTAND.get(player);

            Document data = (Document) armorStand.getMetadata("C4_DATA").get(0).value();
            boolean detonated = data.getBoolean("detonated");
            if (detonated) return;

            this.plugin.setMetadata(armorStand, "C4_DATA",
                    new Document("placer", player.getName())
                            .append("detonated", true));

            player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);

            TNTPrimed tnt = (TNTPrimed) armorStand.getLocation().getWorld().spawnEntity(armorStand.getLocation().add(0, 1, 0), EntityType.PRIMED_TNT);
            tnt.setGravity(true);
            tnt.setIsIncendiary(false);
            tnt.setCustomNameVisible(true);
            tnt.setSource(player);

            this.plugin.setMetadata(tnt, "SUMMONED_BY", player.getUniqueId());

            Countdown countdown = new Countdown(this.plugin.getInstance(), new CountdownListener() {
                @Override
                public void start() {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§aGEZÜNDET!"));

                    tnt.setFuseTicks(8 * 20);
                    tnt.setYield(3.5F);

                    armorStand.remove();
                    RECOGNIZE_PLACED_ARMORSTAND.remove(player);
                }

                @Override
                public void stop() {
                }

                @Override
                public void second(int v0) {
                    tnt.setCustomName("§4§l" + v0);
                }

                @Override
                public void sleep() {
                }
            });
            countdown.startCountdown(8, false);
        }

        if (event.getItem() != null && event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase(this.getName())) {
            event.setCancelled(true);

            if (!this.plugin.roleManager.isPlayerAssigned(player, 2)) return;

            if (event.getClickedBlock() == null) return;

            if (RECOGNIZE_PLACED_ARMORSTAND.containsKey(player)) {
                player.sendMessage(this.plugin.serverPrefix + "§cDu kannst nur einmal " + this.getName() + " §cplatzieren!");
                return;
            }

            Location clickedBlock = event.getClickedBlock().getLocation().add(0, 1, 0);

            ArmorStand armorStand = (ArmorStand) player.getWorld().spawnEntity(clickedBlock.subtract(0, 0.5, 0), EntityType.ARMOR_STAND);

            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setInvulnerable(true);

            armorStand.setCustomNameVisible(false);
            armorStand.setHelmet(new ItemBuilder(Material.TNT, 1).build());

            player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);

            this.plugin.setMetadata(armorStand, "C4_DATA",
                    new Document("placer", player.getName())
                            .append("detonated", false));
            RECOGNIZE_PLACED_ARMORSTAND.put(player, armorStand);

            this.plugin.setMetadata(player, "C4_COOLDOWN", System.currentTimeMillis());
        }
    }
}