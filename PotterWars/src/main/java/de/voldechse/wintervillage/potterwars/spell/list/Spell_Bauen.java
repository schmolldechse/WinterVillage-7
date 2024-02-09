package de.voldechse.wintervillage.potterwars.spell.list;

import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.potterwars.spell.Spell;
import de.voldechse.wintervillage.potterwars.spell.type.SpellEnum;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Spell_Bauen extends Spell {

    public int getLevel() {
        return this.getSpellEnum().getLevelRequired();
    }

    @Override
    public String getName() {
        return this.getSpellEnum().getSpellName();
    }

    @Override
    public ItemStack getSpellIcon() {
        return new ItemBuilder(this.getSpellEnum().getSpellItem(), 1, this.getName())
                .lore(
                        "§7Zauber§8: §r" + this.getName(),
                        "§7Zauberkategorie§8: §c" + this.getSpellEnum().getSpellType().getSpellType(),
                        "§7Kosten des Zaubers§8: §e" + this.getLevel() + " Level",
                        "",
                        "§7Fähigkeiten des Zaubers§8:",
                        this.getSpellEnum().getSpellInformation()
                ).build();
    }

    @Override
    public SpellEnum getSpellEnum() {
        return SpellEnum.BAUEN;
    }

    @Override
    public boolean launchedSpell(Player player) {
        if (player.getLevel() < this.getLevel()) {
            int levelRequired = this.getLevel() - player.getLevel();

            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§cDu benötigst noch §a" + levelRequired + " §cLevel um §r" + this.getName() + " §czu benutzen!"));
            return false;
        }

        Block below = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
        if (!(below.getType().isAir() || below.getType().isTransparent())) return false;

        below.setType(Material.SANDSTONE);
        this.removeLevel(player);
        return true;
    }

    @Override
    public void removeLevel(Player player) {
        player.setLevel(player.getLevel() - this.getLevel());
    }
}