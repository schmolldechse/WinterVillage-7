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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;

public class Item_Defuser extends RoleItem implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    public static Countdown countdown;

    @Override
    public String getName() {
        return "§7Defuser";
    }

    @Override
    public ItemStack getIcon(boolean traitor) {
        return new ItemBuilder(Material.SHEARS, 1, this.getName())
                .lore("",
                        "§7Halte Rechtsklick auf einen",
                        "§7Sprengkörper, um ihn zu entschärfen",
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
        player.getInventory().addItem(new ItemBuilder(Material.SHEARS, 1, this.getName())
                .lore("",
                        "§7Halte Rechtsklick auf einen",
                        "§7Sprengkörper, um ihn zu entschärfen")
                .build());
    }

    @Override
    public int howOftenBuyable() {
        return 1;
    }

    @Override
    public boolean alradyInInventory(Player player) {
        return player.getInventory().contains(Material.SHEARS);
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

            if (!this.plugin.roleManager.isPlayerAssigned(player, 1)) return;

            ArmorStand armorStand = (ArmorStand) event.getRightClicked();
            if (armorStand.getHelmet().getType() != Material.TNT) return;

            if (!armorStand.hasMetadata("C4_DATA")) return;
            Document data = (Document) armorStand.getMetadata("C4_DATA").get(0).value();

            if (player.hasMetadata("DEFUSING")) return;

            if (!isNear(player, armorStand.getLocation())) {
                //player.sendMessage(this.plugin.serverPrefix + "§cDu bist zu weit weg von der Bombe!");
                player.sendMessage(this.plugin.serverPrefix + "§cDu bist zu weit weg von den Explosiven Nussknacker!");
                return;
            }

            boolean detonated = data.getBoolean("detonated");
            if (detonated) {
                //player.sendMessage(this.plugin.serverPrefix + "§eDie Bombe wurde bereits detoniert. Du kannst sie nicht mehr entschärfen");
                player.sendMessage(this.plugin.serverPrefix + "§eDie Explosiven Nussknacker wurden bereits detoniert. Du kannst sie nicht mehr entschärfen");
                return;
            }

            countdown = new Countdown(this.plugin.getInstance(), new CountdownListener() {
                @Override
                public void start() {
                    plugin.setMetadata(player, "DEFUSING", true);
                }

                @Override
                public void stop() {
                    Player placer = Bukkit.getPlayer(data.getString("placer"));
                    if (plugin.roleManager.isPlayerAssigned(placer)) {
                        //placer.sendMessage(this.plugin.serverPrefix + "§eDeine Bombe wurde entschärft!");
                        placer.sendMessage(plugin.serverPrefix + "§eDeine Explosiven Nussknacker wurden entschärft!");
                        if (placer.getInventory().contains(Material.LEVER))
                            placer.getInventory().remove(Material.LEVER);
                    }

                    plugin.removeMetadata(armorStand, "C4_DATA");
                    if (Item_C4.RECOGNIZE_PLACED_ARMORSTAND.containsKey(placer))
                        Item_C4.RECOGNIZE_PLACED_ARMORSTAND.remove(placer);

                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                    armorStand.remove();

                    if (plugin.roleManager.isPlayerAssigned(player))
                        player.sendMessage(plugin.serverPrefix + "§eDu hast die Explosiven Nussknacker entschärft");
                    //player.sendMessage(this.plugin.serverPrefix + "§eDu hast die Bombe entschärft");

                    plugin.removeMetadata(player, "DEFUSING");
                }

                @Override
                public void second(int v0) {
                    if (plugin.roleManager.isPlayerAssigned(player)) {
                        plugin.removeMetadata(player, "DEFUSING");
                        countdown.stopCountdown(false);
                        return;
                    }

                    if (!isNear(player, armorStand.getLocation())) {
                        //player.sendMessage(this.plugin.serverPrefix + "§cDu hast dich zu weit weg von der Bombe entfernt");
                        player.sendMessage(plugin.serverPrefix + "§cDu hast dich zu weit weg von den Explosiven Nussknacker entfernt");
                        plugin.removeMetadata(player, "DEFUSING");

                        countdown.stopCountdown(false);
                        return;
                    }

                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(currentProgress(v0, 4)));
                }

                @Override
                public void sleep() {
                }
            });
            countdown.startCountdown(4, false);
        }
    }

    private boolean isNear(Player player, Location blockLocation) {
        if (!player.getLocation().getWorld().equals(blockLocation.getWorld())) return false;
        double distance = player.getLocation().distance(blockLocation);
        return distance <= 1.5D;
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