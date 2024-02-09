package de.voldechse.wintervillage.potterwars.listener;

import de.voldechse.wintervillage.potterwars.PotterWars;
import de.voldechse.wintervillage.potterwars.gamestate.Types;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class PlayerDropItemListener implements Listener {

    @EventHandler
    public void execute(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        if (PotterWars.getInstance().gameManager.isSpectator(player)) {
            event.setCancelled(true);
            return;
        }

        Types gamePhase = PotterWars.getInstance().gameStateManager.currentGameState().getGameStatePhase();
        if (!(gamePhase == Types.INGAME || gamePhase == Types.OVERTIME)) {
            event.setCancelled(true);
            return;
        }

        if (event.getItemDrop().getItemStack().getType() != Material.AIR
                && event.getItemDrop().getItemStack().hasItemMeta()
                && event.getItemDrop().getItemStack().getItemMeta().getDisplayName().equalsIgnoreCase("§cZauberstab")) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§cDu darfst deinen Zauberstab nicht wegwerfen!"));
            event.setCancelled(true);
            return;
        }

        event.setCancelled(false);
    }
}