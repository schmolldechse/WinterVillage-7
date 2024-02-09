package de.voldechse.wintervillage.masterbuilders.listener;

import de.voldechse.wintervillage.masterbuilders.MasterBuilders;
import de.voldechse.wintervillage.masterbuilders.gamestate.Types;
import de.voldechse.wintervillage.masterbuilders.teams.Team;
import de.voldechse.wintervillage.masterbuilders.teams.voting.TemporaryVotepoints;
import de.voldechse.wintervillage.masterbuilders.utils.RectangleManager;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.voldechse.wintervillage.masterbuilders.gamestate.list.GameStateVoteBuildings.currentIndex;

public class PlayerInteractListener implements Listener {

    private final MasterBuilders plugin = InjectionLayer.ext().instance(MasterBuilders.class);

    public static Map<Player, TemporaryVotepoints> PLAYER_LAST_VOTED_DATA_PER_TEAM = new HashMap<Player, TemporaryVotepoints>();

    @EventHandler
    public void execute(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        Types gamePhase = this.plugin.gameStateManager.currentGameState().getGameStatePhase();

        if (event.getItem() == null) return;

        if (event.getItem().getType() == Material.ARMOR_STAND
                && event.getClickedBlock() != null) {
            if (player.hasMetadata("BUILD_MODE")) {
                event.setCancelled(false);
                return;
            }

            if (gamePhase != Types.BUILDING_PHASE) {
                event.setCancelled(true);
                return;
            }

            if (this.plugin.gameManager.isSpectator(player)) {
                event.setCancelled(true);
                return;
            }

            if (!this.plugin.teamManager.isPlayerInTeam(player)) {
                event.setCancelled(true);
                return;
            }

            if (!this.plugin.teamManager.isBlockInPlot(this.plugin.teamManager.getTeam(player).teamId, event.getClickedBlock().getLocation().add(0, 1, 0))) {
                event.setCancelled(true);
                return;
            }

            event.setCancelled(false);
        }

        if (event.getItem().getType() == Material.FIRE_CHARGE
                || event.getItem().getType() == Material.FLINT_AND_STEEL) {
            event.setCancelled(true);
            return;
        }

        if (event.getClickedBlock() != null
                && event.getAction() == Action.RIGHT_CLICK_BLOCK
                && (event.getClickedBlock().getType() == Material.CHEST
                || event.getClickedBlock().getType() == Material.TRAPPED_CHEST
                || event.getClickedBlock().getType() == Material.ENDER_CHEST)) {
            event.setCancelled(true);
            return;
        }

        if (event.getItem().getType().name().endsWith("SPAWN_EGG")) {
            event.setCancelled(true);
            return;
        }

        ItemMeta currentItemMeta = event.getItem().getItemMeta();

        if (event.getItem().hasItemMeta()
                && currentItemMeta.getDisplayName().equalsIgnoreCase(this.plugin.gameManager.getSpectatorCompass())) {
            if (!this.plugin.gameManager.isSpectator(player)) return;
            player.openInventory(this.plugin.gameManager.getSpectatorInventory());
        }

        if (event.getItem().hasItemMeta() && currentItemMeta.getDisplayName().equalsIgnoreCase("§aTeamauswahl")) {
            if (gamePhase != Types.LOBBY) return;
            if (this.plugin.gameManager.isSpectator(player)) return;

            player.openInventory(this.plugin.teamManager.getTeamSelectInventory());
            player.playSound(player.getLocation(), Sound.ENTITY_HORSE_SADDLE, 1.0f, 1.0f);
        }

        if (event.getItem().hasItemMeta() && currentItemMeta.getDisplayName().equalsIgnoreCase("§4ZURÜCKSETZEN")) {
            if (gamePhase != Types.BUILDING_PHASE) return;
            if (this.plugin.gameManager.isSpectator(player)) return;

            if (!player.hasMetadata("RESET_BUILDING")) {
                player.sendMessage(this.plugin.serverPrefix + "§cBist du dir sicher, dass du dein Plot zurücksetzen möchtest?");

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.hasMetadata("RESET_BUILDING"))
                            plugin.removeMetadata(player, "RESET_BUILDING");
                    }
                }.runTaskLater(this.plugin.getInstance(), 100L);

                this.plugin.setMetadata(player, "RESET_BUILDING", true);
                return;
            }

            if (!plugin.permissionManagement.containsUserAsync(player.getUniqueId()).join()) return;

            PermissionUser permissionUser = plugin.permissionManagement.userAsync(player.getUniqueId()).join();
            if (permissionUser == null) return;
            PermissionGroup permissionGroup = plugin.permissionManagement.highestPermissionGroup(permissionUser);
            if (permissionGroup == null) return;

            Team team = this.plugin.teamManager.getTeam(player);
            List<Player> plotOwner = team.plotOwner;
            plotOwner.forEach(players -> players.sendMessage(this.plugin.serverPrefix + permissionGroup.color() + player.getName() + " §chat das Plot zurückgesetzt"));

            this.plugin.removeMetadata(player, "RESET_BUILDING");

            Location cornerA = new Location(
                    Bukkit.getWorld(team.cornerA.getWorld()),
                    team.cornerA.getX(),
                    team.cornerA.getY() + 1,
                    team.cornerA.getZ()
            );
            Location cornerB = new Location(
                    Bukkit.getWorld(team.cornerB.getWorld()),
                    team.cornerB.getX(),
                    team.cornerB.getY() + this.plugin.configDocument.getInt("allowedHeightDifference"),
                    team.cornerB.getZ()
            );

            RectangleManager.fillRectangle(cornerA, cornerB, Material.AIR);
            return;
        }

        if (event.getItem().hasItemMeta() && currentItemMeta.getDisplayName().equalsIgnoreCase("§cDU DARFST DAS NICHT BEWERTEN")) {
            player.playSound(player.getLocation(), Sound.ENTITY_PILLAGER_DEATH, 1.0F, 1.0F);
            return;
        }

        if (event.getItem().hasItemMeta() && (currentItemMeta.getDisplayName().equalsIgnoreCase("§4GRAUENVOLL")
                || currentItemMeta.getDisplayName().equalsIgnoreCase("§6SCHLECHT")
                || currentItemMeta.getDisplayName().equalsIgnoreCase("§eOKAY")
                || currentItemMeta.getDisplayName().equalsIgnoreCase("§aGUT")
                || currentItemMeta.getDisplayName().equalsIgnoreCase("§9SUPER")
                || currentItemMeta.getDisplayName().equalsIgnoreCase("§5PERFEKT"))) {
            if (gamePhase != Types.VOTING_BUILDINGS) return;
            if (this.plugin.gameManager.isSpectator(player)) return;

            Team currentTeam = this.plugin.teamManager.getTeamList().get(currentIndex);

            ItemStack itemStack = event.getItem();
            if (PLAYER_LAST_VOTED_DATA_PER_TEAM.containsKey(player)) {
                TemporaryVotepoints temporaryVotepoints = PLAYER_LAST_VOTED_DATA_PER_TEAM.get(player);

                if (itemStack.getItemMeta().getDisplayName().equals(temporaryVotepoints.getVotedWith().getItemMeta().getDisplayName()))
                    return;

                temporaryVotepoints.getVotedWith().removeEnchantment(Enchantment.DURABILITY);
                player.getInventory().setItem(temporaryVotepoints.getRawSlot(), temporaryVotepoints.getVotedWith());

                removeVotepoints(currentTeam, temporaryVotepoints.getLastGivenPoints());

                PLAYER_LAST_VOTED_DATA_PER_TEAM.remove(player);
            }

            TemporaryVotepoints temporaryVotepoints = new TemporaryVotepoints(player.getInventory().getHeldItemSlot(), getVotepoints(itemStack.getItemMeta().getDisplayName()), itemStack);
            PLAYER_LAST_VOTED_DATA_PER_TEAM.put(player, temporaryVotepoints);

            player.getInventory().getItemInHand().addUnsafeEnchantment(Enchantment.DURABILITY, 1);
            player.sendTitle(currentItemMeta.getDisplayName(), "");

            addVotepoints(currentTeam, temporaryVotepoints.getLastGivenPoints());
        }
    }

    public static void addVotepoints(Team beingVoted, int pointsFromPane) {
        beingVoted.setEarnedVotepoints(beingVoted.earnedVotepoints + pointsFromPane);
    }

    private void removeVotepoints(Team beingVoted, int pointsFromPane) {
        int currentPoints = beingVoted.earnedVotepoints;
        int newPoints = Math.max(0, currentPoints - pointsFromPane);
        beingVoted.setEarnedVotepoints(newPoints);
    }

    public int getVotepoints(String panelName) {
        panelName = ChatColor.stripColor(panelName);
        return switch (panelName) {
            case "GRAUVENVOLL" -> 1;
            case "SCHLECHT" -> 2;
            case "OKAY" -> 3;
            case "GUT" -> 4;
            case "SUPER" -> 5;
            case "PERFEKT" -> 6;
            default -> 1;
        };
    }
}