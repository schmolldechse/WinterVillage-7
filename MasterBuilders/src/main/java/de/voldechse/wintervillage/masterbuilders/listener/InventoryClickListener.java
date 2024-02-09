package de.voldechse.wintervillage.masterbuilders.listener;

import de.voldechse.wintervillage.masterbuilders.MasterBuilders;
import de.voldechse.wintervillage.masterbuilders.teams.Team;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryClickListener implements Listener {

    private final MasterBuilders plugin = InjectionLayer.ext().instance(MasterBuilders.class);

    public static List<Player> playersVoted = new ArrayList<Player>();

    public static Map<String, Integer> voteCounts = new HashMap<String, Integer>();

    public static Map<Player, Integer> clickedSlot = new HashMap<Player, Integer>();

    @EventHandler
    public void execute(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR)
            return;

        if (event.getView().getTitle().equalsIgnoreCase("§8Stimme für ein Thema ab")) {
            event.setCancelled(true);

            if (event.getCurrentItem().getType() == Material.CLOCK) return;

            ItemStack clickedItem = event.getCurrentItem();
            String themeName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

            if (this.plugin.gameManager.isSpectator(player)) {
                player.playSound(player.getLocation(), Sound.ENTITY_PILLAGER_DEATH, 1.0F, 1.0F);
                return;
            }

            if (playersVoted.contains(player)
                    || player.hasMetadata("ALREADY_VOTED_THEME")) {
                player.playSound(player.getLocation(), Sound.ENTITY_PILLAGER_DEATH, 1.0F, 1.0F);
                return;
            }

            if (voteCounts.containsKey(themeName)) {
                int currentVotes = voteCounts.get(themeName);
                voteCounts.put(themeName, currentVotes + 1);
                clickedSlot.put(player, event.getSlot());

                playersVoted.add(player);
                this.plugin.setMetadata(player, "ALREADY_VOTED_THEME", true);

                event.getCurrentItem().addUnsafeEnchantment(Enchantment.DURABILITY, 1);

                ItemMeta itemMeta = event.getCurrentItem().getItemMeta();
                List<String> lore = itemMeta.getLore();
                lore.addAll(List.of("", "§cAusgewählt"));

                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                itemMeta.setLore(lore);
                event.getCurrentItem().setItemMeta(itemMeta);

                event.getInventory().setItem(event.getSlot(), clickedItem);

                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
            }
        }

        if (event.getView().getTitle().equalsIgnoreCase("§aTeamauswahl")) {
            event.setCancelled(true);

            Team team = this.plugin.teamManager.getTeam(event.getSlot());
            if (this.plugin.teamManager.isPlayerInTeam(player, team)) {
                player.sendMessage(this.plugin.serverPrefix + "§cDu bist bereits im Team §r#" + team.teamId);
                return;
            }

            if (team.plotOwner.size() >= this.plugin.maxPlayersInTeam) {
                player.sendMessage(this.plugin.serverPrefix + "§cDas Team §r#" + team.teamId + " §cist bereits voll");
                return;
            }

            this.plugin.teamManager.setCurrentTeam(player, this.plugin.teamManager.getTeamId(player), team.teamId);

            player.sendMessage(this.plugin.serverPrefix + "§7Du bist dem Team §r#" + team.teamId + " §7beigetreten");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 20.f, 20.f);
            player.closeInventory();
        }

        if (event.getView().getTitle().equalsIgnoreCase("§bTeleporter")) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null) return;

            String displayName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

            Player toSpectate = Bukkit.getPlayer(displayName);
            if (toSpectate == null) {
                player.sendMessage(this.plugin.serverPrefix + "§cSomething seems to be not working. Try again");
                return;
            }

            if (this.plugin.gameManager.isSpectator(toSpectate)) {
                player.sendMessage(this.plugin.serverPrefix + "§cSomething seems to be not working. Try again");
                return;
            }

            player.teleport(toSpectate);
            player.sendMessage(this.plugin.serverPrefix + "§7Du wurdest du §b" + toSpectate.getName() + " §7teleportiert");
        }
    }
}