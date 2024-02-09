package de.voldechse.wintervillage.potterwars.kit.list;

import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.potterwars.kit.Kit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Kit_Voldemort extends Kit {

    @Override
    public int getId() {
        return 8;
    }

    @Override
    public String getName() {
        return "Voldemort";
    }

    @Override
    public ItemStack getIcon() {
        return new ItemBuilder(Material.FIRE_CHARGE, 1, "§f" + this.getName())
                .lore(
                        "§7Name§8: §a" + this.getName(),
                        "",
                        "§7Beinhaltet folgende Items§8:",
                        "§8- §eLederhelm mit §bSchutz IV",
                        "§8- §eNetherite Brustpanzer mit §bSchutz II",
                        "§8- §eLederhose mit §bSchutz IV",
                        "§8- §eLederschuhe mit §bSchutz IV",
                        "§8- §eFeuerzeug mit §bUnzerstörbarkeit",
                        "",
                        "§7Besitzt folgende Fähigkeiten§8:",
                        "§8- §cDauerhaft Resistenz II",
                        //"",
                        //"§aKostenloses Kit",
                        ""
                ).build();
    }

    @Override
    public void loadItems(Player player) {
        ItemStack helmet = new ItemBuilder(Material.LEATHER_HELMET).build();
        LeatherArmorMeta helmetMeta = (LeatherArmorMeta) helmet.getItemMeta();
        helmetMeta.setColor(Color.BLACK);
        helmetMeta.setDisplayName("§f" + this.getName());
        helmetMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4, true);
        helmet.setItemMeta(helmetMeta);

        player.getInventory().setHelmet(helmet);
        player.getInventory().setChestplate(new ItemBuilder(Material.NETHERITE_CHESTPLATE, 1).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2).build());

        ItemStack leggings = new ItemBuilder(Material.LEATHER_LEGGINGS).build();
        LeatherArmorMeta leggingsMeta = (LeatherArmorMeta) leggings.getItemMeta();
        leggingsMeta.setColor(Color.BLACK);
        leggingsMeta.setDisplayName("§f" + this.getName());
        leggingsMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4, true);
        leggings.setItemMeta(leggingsMeta);

        player.getInventory().setLeggings(leggings);

        ItemStack boots = new ItemBuilder(Material.LEATHER_BOOTS).build();
        LeatherArmorMeta bootsMeta = (LeatherArmorMeta) boots.getItemMeta();
        bootsMeta.setColor(Color.BLACK);
        bootsMeta.setDisplayName("§f" + this.getName());
        bootsMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4, true);
        boots.setItemMeta(bootsMeta);

        player.getInventory().setBoots(boots);

        ItemStack flintAndSteel = new ItemBuilder(Material.FLINT_AND_STEEL).build();
        ItemMeta flintAndSteelMeta = (ItemMeta) flintAndSteel.getItemMeta();
        flintAndSteelMeta.setDisplayName("§f" + this.getName());
        flintAndSteelMeta.setUnbreakable(true);
        flintAndSteel.setItemMeta(flintAndSteelMeta);

        player.getInventory().addItem(flintAndSteel);

        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 1, true, true));
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