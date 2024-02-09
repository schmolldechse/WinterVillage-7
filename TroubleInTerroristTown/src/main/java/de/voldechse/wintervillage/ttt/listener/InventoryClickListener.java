package de.voldechse.wintervillage.ttt.listener;

import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.gamestate.Types;
import de.voldechse.wintervillage.ttt.roles.Role;
import de.voldechse.wintervillage.ttt.roles.items.RoleItem;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryClickListener implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    @EventHandler
    public void execute(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        Types gamePhase = this.plugin.gameStateManager.currentGameState().getGameStatePhase();

        if (gamePhase == Types.LOBBY
                && !(player.hasMetadata("BUILD_MODE")
                || player.hasMetadata("SETUP_TESTER")
                || player.hasMetadata("EDIT_SPAWNS")
                || player.hasMetadata("EDIT_CHESTS"))) {
            event.setCancelled(true);
        }

        if (event.getClickedInventory() != null
                //&& (event.getView().getTitle().equalsIgnoreCase("§8Traitor-Shop")
                //|| event.getView().getTitle().equalsIgnoreCase("§8Detective-Shop"))) {
                && (event.getView().getTitle().equalsIgnoreCase("§8Krampus-Shop")
                || event.getView().getTitle().equalsIgnoreCase("§8Mr. Frost-Shop")
                || event.getView().getTitle().equalsIgnoreCase("§8Elfen-Shop"))) {
            if (event.getCurrentItem() == null) return;

            event.setCancelled(true);

            if (this.plugin.gameManager.isSpectator(player)) return;

            if (this.plugin.roleManager.getRole(player) == null) return;
            Role role = this.plugin.roleManager.getRole(player);

            //if (role.roleId == 0) return;

            RoleItem roleItem = this.plugin.roleItemManager.getItemByName(event.getCurrentItem().getItemMeta().getDisplayName());

            if (!player.hasMetadata("SHOP_POINTS")) return;
            int shopPoints = player.getMetadata("SHOP_POINTS").get(0).asInt();

            if (event.getCurrentItem().getType() == Material.CYAN_STAINED_GLASS_PANE) return;

            if (roleItem.getNeededPoints() > shopPoints) {
                event.getView().close();

                player.sendMessage(this.plugin.serverPrefix + "§cDu hast nicht genügend " + role.getRolePrefix() + role.getRoleName() + "-Punkte§c!");
                player.playSound(player.getLocation(), Sound.ENTITY_PILLAGER_DEATH, 1.0F, 1.0F);
                return;
            }

            if (roleItem.alradyInInventory(player)) {
                event.getView().close();
                player.sendMessage(this.plugin.serverPrefix + "§cDu kannst §f" + roleItem.getName() + " §cnur einmal aktiv haben!");
                return;
            }

            if (this.plugin.roleItemManager.purchaseCount(player, roleItem) >= roleItem.howOftenBuyable()) {
                event.getView().close();
                player.sendMessage(this.plugin.serverPrefix + roleItem.getName() + " §ckannst du nur §b" + roleItem.howOftenBuyable() + " §cMal kaufen!");
                return;
            }

            this.plugin.roleManager.changeShopPoints(player, -roleItem.getNeededPoints());
            this.plugin.roleItemManager.increasePurchaseCount(player, roleItem);
            this.plugin.scoreboardManager.updateScoreboard(player, "currentShopPoints", " " + role.getRolePrefix() + this.plugin.roleManager.getShopPoints(player), " §b");

            event.getView().close();
            roleItem.equipItems(player);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            player.sendMessage(this.plugin.serverPrefix + "§aDu hast " + roleItem.getName() + " §agekauft");
        }

        if (event.getClickedInventory() != null
                && event.getCurrentItem() != null
                && event.getCurrentItem().getType() == Material.LEATHER_CHESTPLATE
                && gamePhase == Types.INGAME
                && !player.hasMetadata("BUILD_MODE")) {
            event.setCancelled(true);
        }

        if (event.getClickedInventory() != null && event.getView().getTitle().equalsIgnoreCase("§bTeleporter")) {
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

            if (this.plugin.roleManager.getRole(toSpectate) == null) {
                player.sendMessage(this.plugin.serverPrefix + "§cSomething seems to be not working. Try again");
                return;
            }
            Role spectatingRole = this.plugin.roleManager.getRole(toSpectate);

            String prefix = "§a";
            switch (spectatingRole.roleId) {
                case 0 -> {
                    prefix = "§a";
                }
                case 1 -> {
                    prefix = "§9";
                }
                case 2 -> {
                    prefix = (toSpectate.hasMetadata("BUSTED_TRAITOR") ? "§4" : "§a");
                }
            }

            player.teleport(toSpectate);
            player.sendMessage(this.plugin.serverPrefix + "§7Du wurdest du " + prefix + toSpectate.getName() + " §7teleportiert");
        }
    }
}