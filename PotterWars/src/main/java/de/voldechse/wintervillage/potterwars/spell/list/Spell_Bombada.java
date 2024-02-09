package de.voldechse.wintervillage.potterwars.spell.list;

import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.potterwars.PotterWars;
import de.voldechse.wintervillage.potterwars.spell.Spell;
import de.voldechse.wintervillage.potterwars.spell.type.SpellEnum;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;

public class Spell_Bombada extends Spell implements Listener {

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
        return SpellEnum.BOMBADA;
    }

    @Override
    public boolean launchedSpell(Player player) {
        if (player.getLevel() < this.getLevel()) {
            int levelRequired = this.getLevel() - player.getLevel();

            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§cDu benötigst noch §a" + levelRequired + " §cLevel um §r" + this.getName() + " §czu benutzen!"));
            return false;
        }

        Snowball snowball = player.getWorld().spawn(player.getEyeLocation(), Snowball.class);
        snowball.setVelocity(player.getEyeLocation().getDirection().multiply(3));
        snowball.setShooter(player);

        PotterWars.getInstance().setMetadata(snowball, "BOMBADA", true);

        player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0f, 1.0f);

        this.removeLevel(player);
        return true;
    }

    @Override
    public void removeLevel(Player player) {
        player.setLevel(player.getLevel() - this.getLevel());
    }

    @EventHandler
    public void execute(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Snowball snowball
                && snowball.hasMetadata("BOMBADA")
                && snowball.getShooter() != null) {
            Player shooter = (Player) snowball.getShooter();

            Location hit = null;
            if (event.getHitBlock() != null) hit = event.getHitBlock().getLocation();
            if (event.getHitEntity() != null && event.getHitEntity() instanceof Player hitPlayer) {
                if (PotterWars.getInstance().gameManager.isSpectator(hitPlayer)) return;

                hit = event.getHitEntity().getLocation();
                PotterWars.getInstance().setMetadata(event.getHitEntity(), "LAST_DAMAGER", shooter.getUniqueId());
            }

            if (hit == null) return;

            event.getEntity().remove();

            hit.getWorld().createExplosion(hit, 7f, true);

            shooter.playSound(shooter.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
        }
    }
}