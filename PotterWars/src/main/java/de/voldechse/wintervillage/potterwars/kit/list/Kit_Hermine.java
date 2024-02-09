package de.voldechse.wintervillage.potterwars.kit.list;

import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.potterwars.kit.Kit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Kit_Hermine extends Kit {

    @Override
    public int getId() {
        return 4;
    }

    @Override
    public String getName() {
        return "Hermine";
    }

    @Override
    public ItemStack getIcon() {
        return new ItemBuilder(Material.GLISTERING_MELON_SLICE, 1, "§f" + this.getName())
                .lore(
                        "§7Name§8: §a" + this.getName(),
                        "",
                        "§7Beinhaltet folgende Items§8:",
                        "§8- §eEisenhelm mit §bSchutz III",
                        "§8- §eDiamant Brustpanzer mit §bSchutz II",
                        "§8- §eEisenhose mit §bSchutz III",
                        "§8- §eEisenschuhe mit §bSchutz III",
                        "§8- §e5 Goldene Äpfel",
                        "",
                        "§7Besitzt folgende Fähigkeiten§8:",
                        "§8- §cZauber machen mehr Schaden",
                        //"",
                        //"§aKostenloses Kit",
                        ""
                ).build();
    }

    @Override
    public void loadItems(Player player) {
        player.getInventory().setHelmet(new ItemBuilder(Material.IRON_HELMET, 1).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 3).build());
        player.getInventory().setChestplate(new ItemBuilder(Material.DIAMOND_CHESTPLATE, 1).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2).build());
        player.getInventory().setLeggings(new ItemBuilder(Material.IRON_LEGGINGS, 1).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 3).build());
        player.getInventory().setBoots(new ItemBuilder(Material.IRON_BOOTS, 1).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 3).build());
        player.getInventory().addItem(new ItemBuilder(Material.GOLDEN_APPLE, 5).build());
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