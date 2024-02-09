package de.voldechse.wintervillage.potterwars.kit.list;

import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.potterwars.kit.Kit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Kit_Bogenschütze extends Kit {

    @Override
    public int getId() {
        return 9;
    }

    @Override
    public String getName() {
        return "Bogenschütze";
    }

    @Override
    public ItemStack getIcon() {
        return new ItemBuilder(Material.ARROW, 1, "§f" + this.getName())
                .lore(
                        "§7Name§8: §a" + this.getName(),
                        "",
                        "§7Beinhaltet folgende Items§8:",
                        "§8- §e64x Pfeile",
                        //"",
                        //"§aKostenloses Kit",
                        ""
                ).build();
    }

    @Override
    public void loadItems(Player player) {
        player.getInventory().addItem(new ItemBuilder(Material.ARROW, 64).build());
    }

    @Override
    public boolean isStarter() {
        return true;
    }

    @Override
    public boolean isUseable() {
        return true;
    }
}