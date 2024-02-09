package de.voldechse.wintervillage.ttt.listener;

import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.gamestate.Types;
import de.voldechse.wintervillage.ttt.roles.items.list.Item_ZweiteChance;
import de.voldechse.wintervillage.ttt.utils.position.PositionEntity;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class PlayerDropItemListener implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    @EventHandler
    public void execute(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        if (player.hasMetadata("BUILD_MODE")) {
            event.setCancelled(false);
            return;
        }

        Types gamePhase = this.plugin.gameStateManager.currentGameState().getGameStatePhase();
        if (!(gamePhase.equals(Types.INGAME) || gamePhase.equals(Types.PREPARING_START))) {
            event.setCancelled(true);
            return;
        }

        if (this.plugin.gameManager.isSpectator(player)) {
            event.setCancelled(true);
            return;
        }

        if (event.getItemDrop().getItemStack().getType() == Material.TOTEM_OF_UNDYING
                && event.getItemDrop().getItemStack().getItemMeta() != null
                //&& event.getItemDrop().getItemStack().getItemMeta().getDisplayName().equalsIgnoreCase("§bZweite Chance")) {
                && event.getItemDrop().getItemStack().getItemMeta().getDisplayName().equalsIgnoreCase("§bNordlicht")) {
            if (this.plugin.gameManager.isSpectator(player)
                    || this.plugin.roleManager.getRole(player) == null) {
                event.setCancelled(true);
                return;
            }

            if (player.hasMetadata("DROPPED_SECOND_CHANCE")) {
                event.setCancelled(false);
                event.getItemDrop().remove();

                this.plugin.removeMetadata(player, "DROPPED_SECOND_CHANCE");

                /**
                this.plugin.setMetadata(player, "SAVED_SECOND_CHANCE", new PositionEntity(
                        player.getLocation().getX(),
                        player.getLocation().getY(),
                        player.getLocation().getZ(),
                        player.getLocation().getYaw(),
                        player.getLocation().getPitch(),
                        player.getWorld().getName()
                ));
                 */
                this.plugin.setMetadata(player, "SAVED_RESPAWN_POSITION", player.getLocation());

                player.sendMessage(this.plugin.serverPrefix + "§eDu wirst bei Todesfall hier wiederbelebt");
                player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);

                Item_ZweiteChance.startTotemTask(player, player.getLocation().add(0, 2, 0));
                return;
            }

            event.setCancelled(true);
            this.plugin.setMetadata(player, "DROPPED_SECOND_CHANCE", true);
            //player.sendMessage(this.plugin.serverPrefix + "§eDroppe §bZweite Chance §enochmal, wenn du dir sicher bist, dass du hier respawnen möchtest");
            player.sendMessage(this.plugin.serverPrefix + "§eDroppe §bNordlicht §enochmal, wenn du dir sicher bist, dass du hier respawnen möchtest");
            return;
        }

        if (this.plugin.ALLOWED_ITEMS_TO_DROP.contains(event.getItemDrop().getItemStack().getType())) {
            event.setCancelled(false);
            return;
        }

        event.setCancelled(true);
    }
}