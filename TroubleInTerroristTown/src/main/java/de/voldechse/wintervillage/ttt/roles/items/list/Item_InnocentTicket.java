package de.voldechse.wintervillage.ttt.roles.items.list;

import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.roles.items.RoleItem;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class Item_InnocentTicket extends RoleItem implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    @Override
    public String getName() {
        //return "§aInnocent-Ticket";
        return "§aElfen-Kostüm";
    }

    @Override
    public ItemStack getIcon(boolean traitor) {
        return new ItemBuilder(Material.LIME_STAINED_GLASS, 1, this.getName())
                .lore("",
                        "§7Wenn du dieses",
                        "§7Item aktivierst, wirst",
                        //"§7du beim nächsten Traitor-Test",
                        "§7du beim nächsten §4Krampus§7-Test",
                        "§7mit einer Wahrscheinlichkeit",
                        //"§7von 75 % als Innocent erkannt",
                        "§7von 75 % als §aElf §7erkannt",
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
        return player.getInventory().contains(Material.LIME_STAINED_GLASS);
    }

    @Override
    public int howOftenBuyable() {
        return 1;
    }

    @Override
    public void equipItems(Player player) {
        player.getInventory().addItem(new ItemBuilder(Material.LIME_STAINED_GLASS, 1, this.getName())
                .lore("",
                        "§7Wenn du dieses",
                        "§7Item aktivierst, wirst",
                        //"§7du beim nächsten Traitor-Test",
                        "§7du beim nächsten §4Krampus§7-Test",
                        "§7mit einer Wahrscheinlichkeit",
                        //"§7von 75 % als Innocent erkannt",
                        "§7von 75 % als §aElf §7erkannt")
                .build());
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

            if (!this.plugin.roleManager.isPlayerAssigned(player, 2)) return;

            if (player.hasMetadata("INNOCENT_TICKET")) {
                player.sendMessage(this.plugin.serverPrefix + "§cSomething seems to be not working. Try again");
                return;
            }

            player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);

            player.sendMessage(this.plugin.serverPrefix + "§aBeim nächsten Krampus-Test, wirst du zu einer Wahrscheinlichkeit von 75 % als Elf erkannt. Dies gilt auch für den §7Santas Little Helper§a, den ein §9Mr. Frost §aeinlösen kann");
            //player.sendMessage(this.plugin.serverPrefix + "§aBeim nächsten Traitor-Test wirst du zu einer Wahrscheinlichkeit von 75 % als Innocent erkannt. Dies gilt auch für den §7Random-Tester§a, den ein §9Detective §aeinlösen kann");
            this.plugin.setMetadata(player, "INNOCENT_TICKET", true);
        }
    }
}