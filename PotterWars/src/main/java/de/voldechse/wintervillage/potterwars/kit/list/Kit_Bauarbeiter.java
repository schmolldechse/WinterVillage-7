package de.voldechse.wintervillage.potterwars.kit.list;

import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.potterwars.kit.Kit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Kit_Bauarbeiter extends Kit {

    @Override
    public int getId() {
        return 2;
    }

    @Override
    public String getName() {
        return "Bauarbeiter";
    }

    @Override
    public ItemStack getIcon() {
        return new ItemBuilder(Material.STONE_PICKAXE, 1, "§f" + this.getName())
                .lore(
                        "§7Name§8: §a" + this.getName(),
                        "",
                        "§7Beinhaltet folgende Items§8:",
                        "§8- §eSteinspitzhacke mit §bUnzerstörbarkeit §eund §bEffizienz XX",
                        "§8- §e3x64 Sandsteinblöcke",
                        //"",
                        //"§aKostenloses Kit",
                        ""
                ).build();
    }

    @Override
    public void loadItems(Player player) {
        ItemStack pickaxe = new ItemBuilder(Material.STONE_PICKAXE).build();
        ItemMeta pickaxeMeta = pickaxe.getItemMeta();
        pickaxeMeta.setUnbreakable(true);
        pickaxeMeta.setDisplayName("§f" + this.getName());
        pickaxeMeta.addEnchant(Enchantment.DIG_SPEED, 20, true);
        pickaxe.setItemMeta(pickaxeMeta);

        player.getInventory().addItem(pickaxe);

        player.getInventory().addItem(new ItemBuilder(Material.SANDSTONE, 64).build());
        player.getInventory().addItem(new ItemBuilder(Material.SANDSTONE, 64).build());
        player.getInventory().addItem(new ItemBuilder(Material.SANDSTONE, 64).build());
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