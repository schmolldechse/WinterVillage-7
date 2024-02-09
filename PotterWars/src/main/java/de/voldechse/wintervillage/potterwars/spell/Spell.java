package de.voldechse.wintervillage.potterwars.spell;

import de.voldechse.wintervillage.potterwars.spell.type.SpellEnum;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class Spell {

    public abstract int getLevel();

    public abstract String getName();

    public abstract ItemStack getSpellIcon();

    public abstract SpellEnum getSpellEnum();

    public abstract boolean launchedSpell(Player player);

    public abstract void removeLevel(Player player);
}