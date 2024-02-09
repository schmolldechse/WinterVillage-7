package de.voldechse.wintervillage.potterwars.kit.list;

import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.potterwars.kit.Kit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Kit_Neville extends Kit {

    @Override
    public int getId() {
        return 6;
    }

    @Override
    public String getName() {
        return "Neville";
    }

    @Override
    public ItemStack getIcon() {
        return new ItemBuilder(Material.WOODEN_SWORD, 1, "§f" + this.getName())
                .lore(
                        "§7Name§8: §a" + this.getName(),
                        "",
                        "§7Beinhaltet folgende Items§8:",
                        "§8- §eHolzschwert mit §bSchärfe IV",
                        //"",
                        //"§aKostenloses Kit",
                        ""
                ).build();
    }

    @Override
    public void loadItems(Player player) {
        player.getInventory().addItem(new ItemBuilder(Material.WOODEN_SWORD, 1).durability((short) 28).enchant(Enchantment.DAMAGE_ALL, 4).build());
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