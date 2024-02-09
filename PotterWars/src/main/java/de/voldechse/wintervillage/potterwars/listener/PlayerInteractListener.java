package de.voldechse.wintervillage.potterwars.listener;

import de.voldechse.wintervillage.potterwars.PotterWars;
import de.voldechse.wintervillage.potterwars.gamestate.Types;
import de.voldechse.wintervillage.potterwars.spell.Spell;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteractListener implements Listener {

    @EventHandler
    public void execute(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        Types gamePhase = PotterWars.getInstance().gameStateManager.currentGameState().getGameStatePhase();

        if (event.getClickedBlock() != null
                && event.getClickedBlock().getType() == Material.CHEST
                && event.getAction() == Action.RIGHT_CLICK_BLOCK) {

            if (PotterWars.getInstance().gameManager.isSpectator(player)) {
                event.setCancelled(true);
                return;
            }

            if (!(gamePhase == Types.PREPARING_START || gamePhase == Types.INGAME || gamePhase == Types.OVERTIME)) {
                event.setCancelled(true);
                return;
            }

            //TODO: Add this check later again
            //if (!PotterWarsGame.getInstance().isPotterWarsChest(event.getClickedBlock().getLocation())) return;

            if (BlockPlaceListener.blockedChestLocation.contains(event.getClickedBlock().getLocation())) return;

            event.setCancelled(true);

            PotterWars.getInstance().chestManager.addChest(player, event.getClickedBlock().getLocation());
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
            player.openInventory(PotterWars.getInstance().chestManager.getAt(event.getClickedBlock().getLocation()).getInventory());
            return;
        }

        if (event.getItem() != null
                && event.getItem().getType() == Material.ARROW) {
            event.setCancelled(true);

            if (!(gamePhase == Types.INGAME || gamePhase == Types.OVERTIME)) return;

            if (PotterWars.getInstance().gameManager.isSpectator(player)) return;

            if (!PotterWars.getInstance().PVP_ENABLED) return;

            Arrow arrow = player.getWorld().spawnArrow(player.getEyeLocation(), player.getEyeLocation().getDirection().multiply(3), 1.3f, 4);
            arrow.setShooter(player);

            player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
        }

        if (event.getItem() != null
                && event.getItem().hasItemMeta()
                && event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase("§bTeleporter")) {
            if (!PotterWars.getInstance().gameManager.isSpectator(player)) return;
            player.openInventory(PotterWars.getInstance().gameManager.getSpectatorInventory());
        }

        if (event.getItem() != null
                && event.getItem().hasItemMeta()
                && event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase("§aTeamauswahl")) {
            if (PotterWars.getInstance().gameManager.isSpectator(player)) return;

            player.openInventory(PotterWars.getInstance().teamManager.getTeamSelectInventory());
            player.playSound(player.getLocation(), Sound.ENTITY_HORSE_SADDLE, 1.0f, 1.0f);
            return;
        }


        if (event.getItem() != null
                && event.getItem().hasItemMeta()
                && event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase("§cWähle dein Kit")) {
            if (gamePhase != Types.LOBBY) return;
            if (PotterWars.getInstance().gameManager.isSpectator(player)) return;

            PotterWars.getInstance().kitManager.openInventory(player);
            player.playSound(player.getLocation(), Sound.ENTITY_HORSE_SADDLE, 1.0f, 1.0f);
            return;
        }

        if (event.getItem() != null
                && event.getItem().hasItemMeta()
                && event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase("§cZauberstab")) {

            if (event.getAction() == Action.LEFT_CLICK_AIR
                    || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                if (PotterWars.getInstance().gameManager.isSpectator(player)) return;
                if (!PotterWars.getInstance().spellManager.hasSpellChoosed(player)) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§cDu musst zuerst einen Zauber auswählen"));
                    return;
                }

                Spell spell = PotterWars.getInstance().spellManager.getCurrentSpell(player);
                if (spell == null) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§cDu musst zuerst einen Zauber auswählen"));
                    return;
                }

                if (!spell.launchedSpell(player)) return;

                return;
            }

            if (event.getAction() == Action.RIGHT_CLICK_AIR
                    || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (PotterWars.getInstance().gameManager.isSpectator(player)) return;
                if (!(gamePhase == Types.INGAME || gamePhase == Types.OVERTIME)) return;
                PotterWars.getInstance().spellManager.openInventory(player);
                return;
            }

            return;
        }
    }
}