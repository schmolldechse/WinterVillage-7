package de.voldechse.wintervillage.ttt.roles.items.list;

import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.roles.items.RoleItem;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Item_FallenTicket extends RoleItem {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    @Override
    public String getName() {
        return "§aFallen-Ticket";
    }

    @Override
    public ItemStack getIcon(boolean traitor) {
        return new ItemBuilder(Material.OAK_BUTTON, 1, this.getName())
                .lore("",
                        "§7Durch den Kauf kannst du",
                        "§7die Falle für dich deaktivieren",
                        "§7Nur durch erneutes Kaufen",
                        "§7hast du die Möglichkeit, die",
                        "§7Falle wieder nutzen zu können",
                        "",
                        "§ePreis: " + (traitor ? "§c" : "§a") + this.getNeededPoints() + " Punkt",
                        "",
                        "§a<Klicke zum Kaufen>")
                .build();
    }

    @Override
    public int getNeededPoints() {
        return 1;
    }

    @Override
    public void equipItems(Player player) {
        if (!player.hasMetadata("TRAP_TICKET")) {
            this.plugin.setMetadata(player, "TRAP_TICKET", true);
            player.sendMessage(this.plugin.serverPrefix + "§eDie Falle ist für dich nun deaktiviert");
            return;
        }

        this.plugin.removeMetadata(player, "TRAP_TICKET");
        player.sendMessage(this.plugin.serverPrefix + "§eDie Falle ist für dich nun wieder aktiviert");
    }

    @Override
    public boolean alradyInInventory(Player player) {
        return false;
    }

    @Override
    public int howOftenBuyable() {
        return 4;
    }
}