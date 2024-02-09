package de.voldechse.wintervillage.potterwars.spell.list;

import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.potterwars.PotterWars;
import de.voldechse.wintervillage.potterwars.kit.Kit;
import de.voldechse.wintervillage.potterwars.kit.list.Kit_Bellatrix;
import de.voldechse.wintervillage.potterwars.kit.list.Kit_Hermine;
import de.voldechse.wintervillage.potterwars.spell.Spell;
import de.voldechse.wintervillage.potterwars.spell.type.SpellEnum;
import de.voldechse.wintervillage.potterwars.team.Team;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class Spell_Expelliarmus extends Spell implements Listener {

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
        return SpellEnum.EXPELLIARMUS;
    }

    @Override
    public boolean launchedSpell(Player player) {
        if (player.getLevel() < this.getLevel()) {
            int levelRequired = this.getLevel() - player.getLevel();

            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§cDu benötigst noch §a" + levelRequired + " §cLevel um §r" + this.getName() + " §czu benutzen"));
            return false;
        }

        Snowball snowball = player.getWorld().spawn(player.getEyeLocation(), Snowball.class);
        snowball.setVelocity(player.getEyeLocation().getDirection().multiply(3));
        snowball.setShooter(player);

        PotterWars.getInstance().setMetadata(snowball, "EXPELLIARMUS", true);

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
                && snowball.hasMetadata("EXPELLIARMUS")
                && snowball.getShooter() != null
                && event.getHitEntity() != null
                && event.getHitEntity() instanceof Player hitPlayer) {
            Player shooter = (Player) snowball.getShooter();

            event.getEntity().remove();

            if (PotterWars.getInstance().gameManager.isSpectator(hitPlayer)) return;
            if (!PotterWars.getInstance().teamManager.isPlayerInTeam(hitPlayer)) return;

            Team team = PotterWars.getInstance().teamManager.getTeam(hitPlayer);
            if (team.players.contains(shooter)) {
                shooter.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§cDu kannst deinen Teammitgliedern keinen Schaden hinzufügen"));
                event.setCancelled(true);
                return;
            }

            if (hitPlayer.isBlocking()) {
                event.setCancelled(true);
                return;
            }

            hitPlayer.getInventory().setHeldItemSlot(new Random().nextInt(8));
            double damage;

            Kit kit = PotterWars.getInstance().kitManager.getKit(shooter);
            if (kit.equals(Kit_Bellatrix.class)) {
                damage = 6.5D;
            } else if (kit.equals(Kit_Hermine.class)) {
                damage = 8.5D;
            } else damage = 5D;

            hitPlayer.damage(damage);

            PotterWars.getInstance().setMetadata(hitPlayer, "LAST_DAMAGER", shooter.getUniqueId());
            shooter.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§cDu hast " + team.teamPrefix + hitPlayer.getName() + " §cmit §r" + this.getName() + " §cgetroffen"));

            shooter.playSound(shooter.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
            hitPlayer.playSound(shooter.getLocation(), Sound.ENTITY_ARROW_HIT, 1.0f, 1.0f);
        }
    }
}