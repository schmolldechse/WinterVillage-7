package de.voldechse.wintervillage.potterwars.listener;

import de.voldechse.wintervillage.potterwars.PotterWars;
import de.voldechse.wintervillage.potterwars.gamestate.Types;
import de.voldechse.wintervillage.potterwars.kit.Kit;
import de.voldechse.wintervillage.potterwars.spell.Spell;
import de.voldechse.wintervillage.potterwars.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryClickListener implements Listener {

    @EventHandler
    public void execute(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR)
            return;

        Types gamePhase = PotterWars.getInstance().gameStateManager.currentGameState().getGameStatePhase();
        if (gamePhase == Types.LOBBY
                && !player.hasMetadata("BUILD_MODE")) {
            event.setCancelled(true);
        }

        ItemStack currentItem = event.getCurrentItem();

        if (event.getView().getTitle().equalsIgnoreCase("§cKit auswählen")) {
            event.setCancelled(true);
            event.getView().close();

            Kit kit = PotterWars.getInstance().kitManager.getById(event.getSlot());
            PotterWars.getInstance().kitManager.setKit(player, kit);

            player.getInventory().setItem(8, kit.getIcon());
            player.sendMessage(PotterWars.getInstance().serverPrefix + "§eDu hast das Kit §c" + kit.getName() + " §eausgewählt");

            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 20.0f, 20.0f);
            return;
        }

        if (event.getView().getTitle().equalsIgnoreCase("§aTeamauswahl")) {
            event.setCancelled(true);
            event.getView().close();

            Team team = PotterWars.getInstance().teamManager.getTeam(event.getSlot());
            if (team == null) return;

            if (PotterWars.getInstance().teamManager.isPlayerInTeam(player, team)) {
                player.sendMessage(PotterWars.getInstance().serverPrefix + "§cDu bist bereits im Team §r" + team.teamPrefix + team.teamName);
                return;
            }

            if (team.players.size() >= PotterWars.getInstance().maxPlayersInTeam) {
                player.sendMessage(PotterWars.getInstance().serverPrefix + "§cDas Team §r" + team.teamPrefix + team.teamName + " §cist bereits voll");
                return;
            }

            PotterWars.getInstance().teamManager.setCurrentTeam(player, team.getTeamId());

            player.sendMessage(PotterWars.getInstance().serverPrefix + "§7Du bist dem Team §r" + team.teamPrefix + team.teamName + " §7beigetreten");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 20.0f, 20.0f);
            player.closeInventory();
        }

        if (event.getView().getTitle().equalsIgnoreCase("§bTeleporter")) {
            event.setCancelled(true);

            String displayName = ChatColor.stripColor(currentItem.getItemMeta().getDisplayName());

            Player toSpectate = Bukkit.getPlayer(displayName);
            if (toSpectate == null) {
                player.sendMessage(PotterWars.getInstance().serverPrefix + "§cSomething seems to be not working. Try again");
                return;
            }

            if (PotterWars.getInstance().gameManager.isSpectator(toSpectate)) {
                player.sendMessage(PotterWars.getInstance().serverPrefix + "§cSomething seems to be not working. Try again");
                return;
            }

            if (PotterWars.getInstance().teamManager.getTeam(toSpectate) == null) {
                player.sendMessage(PotterWars.getInstance().serverPrefix + "§cSomething seems to be not working. Try again");
                return;
            }

            Team spectatingRole = PotterWars.getInstance().teamManager.getTeam(toSpectate);

            player.teleport(toSpectate);
            player.sendMessage(PotterWars.getInstance().serverPrefix + "§7Du wurdest du " + spectatingRole.teamPrefix + toSpectate.getName() + " §7teleportiert");
        }

        if (event.getView().getTitle().equalsIgnoreCase("§cZauber")) {
            event.setCancelled(true);
            event.getView().close();

            Spell spell = PotterWars.getInstance().spellManager.getSpellByName(currentItem.getItemMeta().getDisplayName());

            if (spell == null) {
                player.sendMessage(PotterWars.getInstance().serverPrefix + "§cDieser Zauber existiert nicht");
                return;
            }

            PotterWars.getInstance().spellManager.setCurrentSpell(player, spell);
            PotterWars.getInstance().scoreboardManager.updateScoreboard(player, "currentSpell", " " + (PotterWars.getInstance().spellManager.hasSpellChoosed(player) ? PotterWars.getInstance().spellManager.getCurrentSpell(player).getName() : "§cKeinen ausgewählt"), "");
            player.sendMessage(PotterWars.getInstance().serverPrefix + "§eDu hast den Zauber §r" + spell.getName() + " §eausgewählt");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }
    }
}