package de.voldechse.wintervillage.potterwars.spell;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.voldechse.wintervillage.potterwars.PotterWars;
import de.voldechse.wintervillage.potterwars.spell.list.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.Map;

public class SpellManager {

    private final List<Spell> spells = Lists.newArrayList();
    private final Map<Player, Spell> choosedSpell = Maps.newHashMap();

    private Inventory inventory;

    public SpellManager() {
        this.spells.add(new Spell_Expelliarmus());
        this.spells.add(new Spell_Crucio());
        this.spells.add(new Spell_Stupor());
        this.spells.add(new Spell_Incendio());
        this.spells.add(new Spell_PetrificusTotalus());
        this.spells.add(new Spell_Bauen());
        this.spells.add(new Spell_Bombada());
        //this.spells.add(new ProtegoSpell());

        int inventorySize = ((this.spells.size() / 9) + 1) * 9;

        inventory = Bukkit.createInventory(null, inventorySize, "§cZauber");
        this.spells.forEach(spell -> inventory.addItem(spell.getSpellIcon()));

        Bukkit.getPluginManager().registerEvents(new Spell_Expelliarmus(), PotterWars.getInstance());
        Bukkit.getPluginManager().registerEvents(new Spell_Crucio(), PotterWars.getInstance());
        Bukkit.getPluginManager().registerEvents(new Spell_Stupor(), PotterWars.getInstance());
        Bukkit.getPluginManager().registerEvents(new Spell_Incendio(), PotterWars.getInstance());
        Bukkit.getPluginManager().registerEvents(new Spell_PetrificusTotalus(), PotterWars.getInstance());
        Bukkit.getPluginManager().registerEvents(new Spell_Bombada(), PotterWars.getInstance());
    }

    public void setCurrentSpell(Player player, Spell spell) {
        if (!this.spells.contains(spell)) return;
        if (this.hasSpellChoosed(player)) this.removeChoosedSpell(player);
        this.choosedSpell.put(player, spell);
    }

    public void removeChoosedSpell(Player player) {
        if (!hasSpellChoosed(player)) return;
        this.choosedSpell.remove(player);
    }

    public boolean hasSpellChoosed(Player player) {
        return this.choosedSpell.containsKey(player);
    }

    public Spell getCurrentSpell(Player player) {
        if (!hasSpellChoosed(player)) return null;
        return this.choosedSpell.get(player);
    }

    public Spell getSpellByName(String spellName) {
        Spell spell = null;
        for (Spell schpell : this.spells)
            if (schpell.getName().equals(spellName))
                spell = schpell;
        return spell;
    }

    public void openInventory(Player player) {
        player.openInventory(inventory);
    }
}