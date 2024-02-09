package de.voldechse.wintervillage.potterwars.kit.list;

import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.potterwars.kit.Kit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Kit_Dumbledore extends Kit {

    @Override
    public int getId() {
        return 3;
    }

    @Override
    public String getName() {
        return "Dumbledore";
    }

    @Override
    public ItemStack getIcon() {
        return new ItemBuilder(Material.LEATHER_CHESTPLATE, 1, "§f" + this.getName())
                .lore(
                        "§7Name§8: §a" + this.getName(),
                        "",
                        "§7Beinhaltet folgende Items§8:",
                        "§8- §eEisenbrustplatte mit §bSchutz III",
                        "§8- §e16 Gebratenes Rindfleisch",
                        "",
                        "§7Besitzt folgende Fähigkeiten§8:",
                        "§8- §cDauerhaft Resistenz III",
                        //"",
                        //"§aKostenloses Kit",
                        ""
                ).build();
    }

    @Override
    public void loadItems(Player player) {
        player.getInventory().setChestplate(new ItemBuilder(Material.IRON_CHESTPLATE, 1).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 3).build());
        player.getInventory().addItem(new ItemBuilder(Material.COOKED_BEEF, 16).build());

        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 2, true, true));
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