package de.voldechse.wintervillage.potterwars.listener;

import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.potterwars.PotterWars;
import de.voldechse.wintervillage.potterwars.gamestate.Types;
import de.voldechse.wintervillage.potterwars.spell.list.Spell_PetrificusTotalus;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class PlayerItemConsumeListener implements Listener {

    @EventHandler
    public void execute(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();

        if (PotterWars.getInstance().gameManager.isSpectator(player)) {
            event.setCancelled(true);
            return;
        }

        if (event.getItem() == null) return;

        Types gamePhase = PotterWars.getInstance().gameStateManager.currentGameState().getGameStatePhase();

        if (!(gamePhase == Types.INGAME || gamePhase == Types.OVERTIME)) {
            event.setCancelled(true);
            return;
        }

        if (event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase("§1B§de§br§at§2i§3e §1B§4o§ct§9t§9s §8B§7o§1h§cn§ee")) {
            int randomZiffer = new Random().nextInt(15);
            switch (randomZiffer) {
                case 0 -> {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 480, 2));
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 50.0F, 1.0F);
                    player.sendTitle("§b+ §aSPRUNGKRAFT", "");
                }
                case 1 -> {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 440, 2));
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 50.0F, 1.0F);
                    player.sendTitle("§b+ §aSPEED", "");
                }
                case 2 -> {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 460, 2));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_GUITAR, 20.0F, 1.0F);
                    player.sendTitle("§4- §cBLINDHEIT", "");
                }
                case 3 -> {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 340, 2));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_GUITAR, 20.0F, 1.0F);
                    player.sendTitle("§4- §cÜBELKEIT", "");
                }
                case 4 -> {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_GUITAR, 20.0F, 1.0F);
                    Spell_PetrificusTotalus.petrificusTotalusList.add(player);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 220, 1));
                    player.sendTitle("§4- §cKEIN MOVEMENT", "");
                    Bukkit.getScheduler().runTaskLater(PotterWars.getInstance(), new Runnable() {
                        @Override
                        public void run() {
                            Spell_PetrificusTotalus.petrificusTotalusList.remove(player);
                        }
                    }, 200L);
                }
                case 5 -> {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 340, 2));
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 50.0F, 1.0F);
                    player.sendTitle("§b+ §aREGENERATION", "");
                }
                case 6 -> {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 340, 2));
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 50.0F, 1.0F);
                    player.sendTitle("§b+ §aRESISTENZ", "");
                }
                case 7 -> {
                    player.setHealth(player.getMaxHealth());
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 50.0F, 1.0F);
                    player.sendTitle("§b+ §aVOLLE LEBEN", "");
                }
                case 8 -> {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_GUITAR, 20.0F, 1.0F);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 280, 2));
                    player.sendTitle("§4- §cVERGIFTUNG", "");
                }
                case 9 -> {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_GUITAR, 20.0F, 1.0F);
                    player.setHealth(10.0D);
                    player.sendTitle("§4- §c5 HERZEN", "");
                }
                case 10 -> {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_GUITAR, 20.0F, 1.0F);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 280, 1));
                    player.sendTitle("§4- §cLANGSAMKEIT", "");
                }
                case 11 -> {
                    int newLevel = new Random().nextInt((3000 - 750) + 1) + 750;
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 50.0F, 1.0F);
                    if (player.getLevel() < 0) player.setLevel(0);
                    player.setLevel(player.getLevel() + newLevel);
                    player.sendTitle("§b+ §a" + newLevel + " LEVEL", "");
                }
                case 12 -> {
                    int newLevel = new Random().nextInt((1750 - 300) + 1) + 300;
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 50.0F, 1.0F);
                    if (player.getLevel() > 0 && player.getLevel() < newLevel) player.setLevel(0);
                    player.setLevel(player.getLevel() - newLevel);
                    player.sendTitle("§4- §c" + newLevel + " LEVEL", "");
                }
                case 13 -> {
                    int morePotato = new Random().nextInt(17 - 4) + 4;
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 50.0F, 1.0F);
                    player.getInventory().addItem(new ItemBuilder(Material.BAKED_POTATO, morePotato, "§1B§de§br§at§2i§3e §1B§4o§ct§9t§9s §8B§7o§1h§cn§ee").build());
                    player.sendTitle("§b+ §a" + morePotato + "x ZAUBERBOHNEN ", "");
                }
                case 14 -> {
                    player.setHealthScale(player.getHealthScale() + 10);
                    player.setMaxHealth(player.getMaxHealth() + 10);
                    player.setHealth(player.getHealth() + 10);
                    player.sendTitle("§b+ §a5 HERZEN", "");
                }
                default -> {
                }
            }
        }
    }
}