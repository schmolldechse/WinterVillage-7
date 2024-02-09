package de.voldechse.wintervillage.potterwars.listener;

import de.voldechse.wintervillage.potterwars.PotterWars;
import de.voldechse.wintervillage.potterwars.gamestate.Types;
import de.voldechse.wintervillage.potterwars.team.Team;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EntityDamageByEntityListener implements Listener {

    @EventHandler
    public void execute(EntityDamageByEntityEvent event) {
        if (!PotterWars.getInstance().PVP_ENABLED) {
            event.setCancelled(true);
            return;
        }

        Types gamePhase = PotterWars.getInstance().gameStateManager.currentGameState().getGameStatePhase();
        if (!(gamePhase == Types.INGAME || gamePhase == Types.OVERTIME)) {
            event.setCancelled(true);
            return;
        }

        if (event.getEntity() instanceof Player receiver && event.getDamager() instanceof Player damager) {
            if (PotterWars.getInstance().gameManager.isSpectator(receiver)
                    || PotterWars.getInstance().gameManager.isSpectator(damager)
                    || PotterWars.getInstance().teamManager.getTeam(receiver) == null
                    || PotterWars.getInstance().teamManager.getTeam(damager) == null) {
                event.setCancelled(true);
                return;
            }

            Team team = PotterWars.getInstance().teamManager.getTeam(receiver);
            if (team.players.contains(damager)) {
                damager.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§cDu kannst deinen Teammitgliedern keinen Schaden hinzufügen"));
                event.setCancelled(true);
                return;
            }
        }
    }
}