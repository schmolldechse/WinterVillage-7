package de.voldechse.wintervillage.potterwars.kit.list;

import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.potterwars.kit.Kit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Kit_Rüstungsträger extends Kit {

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public String getName() {
        return "Rüstungsträger";
    }

    @Override
    public ItemStack getIcon() {
        return new ItemBuilder(Material.IRON_CHESTPLATE, 1, "§f" + this.getName())
                .lore(
                        "§7Name§8: §a" + this.getName(),
                        "",
                        "§7Beinhaltet folgende Items§8:",
                        "§8- §eNetherite Helm mit §bSchutz IV",
                        "§8- §eEisenbrustplatte mit §bSchutz IV",
                        "§8- §eEisenhose mit §bSchutz IV",
                        "§8- §eNetherite Schuhe mit §bSchutz IV",
                        //"",
                        //"§aKostenloses Kit",
                        ""
                ).build();
    }

    @Override
    public void loadItems(Player player) {
        player.getInventory().setHelmet(new ItemBuilder(Material.NETHERITE_HELMET, 1).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4).build());
        player.getInventory().setChestplate(new ItemBuilder(Material.IRON_CHESTPLATE, 1).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4).build());
        player.getInventory().setLeggings(new ItemBuilder(Material.IRON_LEGGINGS, 1).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4).build());
        player.getInventory().setBoots(new ItemBuilder(Material.NETHERITE_BOOTS, 1).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4).build());
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