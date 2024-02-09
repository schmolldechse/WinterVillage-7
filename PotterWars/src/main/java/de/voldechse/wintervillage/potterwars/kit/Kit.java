package de.voldechse.wintervillage.potterwars.kit;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class Kit {

    public abstract int getId();

    public abstract String getName();

    public abstract boolean isStarter();

    public abstract boolean isUseable();

    public abstract ItemStack getIcon();

    public abstract void loadItems(Player player);
}