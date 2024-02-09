package de.voldechse.wintervillage.potterwars.kit.list;

import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.potterwars.kit.Kit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

public class Kit_Snape extends Kit {

    @Override
    public int getId() {
        return 7;
    }

    @Override
    public String getName() {
        return "Snape";
    }

    @Override
    public ItemStack getIcon() {
        return new ItemBuilder(Material.GHAST_TEAR, 1, "§f" + this.getName())
                .lore(
                        "§7Name§8: §a" + this.getName(),
                        "",
                        "§7Beinhaltet folgende Items§8:",
                        "§8- §e3 Werfbare Tränke mit §bSchaden II",
                        "§8- §e2 Werfbare Tränke mit §bVergiftung",
                        "§8- §e2 Werfbare Tränke mit §bHeilung II",
                        "§8- §e2 Werfbare Tränke mit §bGeschwindigkeit",
                        //"",
                        //"§aKostenloses Kit",
                        ""
                ).build();
    }

    @Override
    public void loadItems(Player player) {
        ItemStack damagePotion = new ItemStack(Material.SPLASH_POTION, 3);
        PotionMeta damagePotionMeta = (PotionMeta) damagePotion.getItemMeta();
        damagePotionMeta.setBasePotionData(new PotionData(PotionType.INSTANT_DAMAGE, false, true));
        //damagePotionMeta.addCustomEffect(new PotionEffect(PotionEffectType.HARM, 1, 0), true);
        damagePotion.setItemMeta(damagePotionMeta);

        ItemStack speedPotion = new ItemStack(Material.SPLASH_POTION, 2);
        PotionMeta speedPotionMeta = (PotionMeta) speedPotion.getItemMeta();
        speedPotionMeta.setBasePotionData(new PotionData(PotionType.SPEED, false, false));
        //speedPotionMeta.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 1, 1), true);
        speedPotion.setItemMeta(speedPotionMeta);

        ItemStack healPotion = new ItemStack(Material.SPLASH_POTION, 2);
        PotionMeta healPotionMeta = (PotionMeta) healPotion.getItemMeta();
        healPotionMeta.setBasePotionData(new PotionData(PotionType.INSTANT_HEAL, false, true));
        //healPotionMeta.addCustomEffect(new PotionEffect(PotionEffectType.HEAL, 1, 2), true);
        healPotion.setItemMeta(healPotionMeta);

        ItemStack poisonPotion = new ItemStack(Material.SPLASH_POTION, 2);
        PotionMeta poisonPotionMeta = (PotionMeta) poisonPotion.getItemMeta();
        poisonPotionMeta.setBasePotionData(new PotionData(PotionType.POISON, false, false));
        //poisonPotionMeta.addCustomEffect(new PotionEffect(PotionEffectType.POISON, 1, 2), true);
        poisonPotion.setItemMeta(poisonPotionMeta);

        player.getInventory().addItem(damagePotion);
        player.getInventory().addItem(poisonPotion);
        player.getInventory().addItem(healPotion);
        player.getInventory().addItem(speedPotion);
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