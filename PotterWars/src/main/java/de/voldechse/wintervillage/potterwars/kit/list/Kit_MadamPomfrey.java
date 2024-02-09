package de.voldechse.wintervillage.potterwars.kit.list;

import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.potterwars.kit.Kit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

public class Kit_MadamPomfrey extends Kit {

    @Override
    public int getId() {
        return 5;
    }

    @Override
    public String getName() {
        return "Madam-Pomfrey";
    }

    @Override
    public ItemStack getIcon() {
        return new ItemBuilder(Material.GOLDEN_APPLE, 1, "§f" + this.getName())
                .lore(
                        "§7Name§8: §a" + this.getName(),
                        "",
                        "§7Beinhaltet folgende Items§8:",
                        "§8- §e5 Werfbare Tränke mit §bHeilung II",
                        "§8- §e6 Goldene Äpfel",
                        //"",
                        //"§aKostenloses Kit",
                        ""
                ).build();
    }

    @Override
    public void loadItems(Player player) {
        ItemStack potion = new ItemStack(Material.SPLASH_POTION, 5);
        PotionMeta potionMeta = (PotionMeta) potion.getItemMeta();
        potionMeta.setBasePotionData(new PotionData(PotionType.INSTANT_HEAL, false, true));
        //potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.HEAL, 1, 2), true);
        potion.setItemMeta(potionMeta);

        player.getInventory().addItem(potion);
        player.getInventory().addItem(new ItemBuilder(Material.GOLDEN_APPLE, 6).build());
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