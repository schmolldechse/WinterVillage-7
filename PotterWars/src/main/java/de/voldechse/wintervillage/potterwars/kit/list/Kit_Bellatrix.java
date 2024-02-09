package de.voldechse.wintervillage.potterwars.kit.list;

import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.potterwars.kit.Kit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Kit_Bellatrix extends Kit {

    @Override
    public int getId() {
        return 1;
    }

    @Override
    public String getName() {
        return "Bellatrix";
    }

    @Override
    public ItemStack getIcon() {
        return new ItemBuilder(Material.COAL, 1, "§f" + this.getName())
                .lore(
                        "§7Name§8: §a" + this.getName(),
                        "",
                        "§7Beinhaltet folgende Items§8:",
                        "§8- §eKettenrüstung mit §bSchutz IV",
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
        player.getInventory().setHelmet(new ItemBuilder(Material.CHAINMAIL_HELMET, 1).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4).build());
        player.getInventory().setChestplate(new ItemBuilder(Material.CHAINMAIL_CHESTPLATE, 1).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4).build());
        player.getInventory().setLeggings(new ItemBuilder(Material.CHAINMAIL_LEGGINGS, 1).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4).build());
        player.getInventory().setBoots(new ItemBuilder(Material.CHAINMAIL_BOOTS, 1).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4).build());
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