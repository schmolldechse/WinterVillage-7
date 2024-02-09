package de.voldechse.wintervillage.ttt.roles.items;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class RoleItem {

    public abstract String getName();

    public abstract ItemStack getIcon(boolean traitor);

    public abstract int getNeededPoints();

    public abstract void equipItems(Player player);

    public abstract int howOftenBuyable();

    public abstract boolean alradyInInventory(Player player);
}