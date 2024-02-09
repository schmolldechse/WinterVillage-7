package de.voldechse.wintervillage.listener;

import de.voldechse.wintervillage.WinterVillage;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerBedEnterListener implements Listener {

    private double percent_skipNightUnder10 = .1D;
    private double percent_skipNightUnder25 = .12D;
    private double percent_skipNightUnder40 = .14D;
    private double percent_skipNightUnder60 = .15D;
    private double percent_skipNightUnder75 = .175D;

    private boolean SKIPPED = false;

    @EventHandler
    public void execute(PlayerBedEnterEvent event) {
        WinterVillage plugin = InjectionLayer.ext().instance(WinterVillage.class);

        if (event.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) return;

        Bukkit.getOnlinePlayers().forEach(player -> player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("")));

        Bukkit.getScheduler().runTaskLater(plugin.getInstance(), () -> {
            int needed = calculate(Bukkit.getOnlinePlayers().size());
            if (sleeping() <= needed)
                Bukkit.broadcastMessage(plugin.serverPrefix + "§c" + sleeping() + " / " + needed + " Spieler benötigt, um die Nacht zu überspringen");

            if (sleeping() >= needed && !this.SKIPPED) {
                this.SKIPPED = true;
                Bukkit.broadcastMessage(plugin.serverPrefix + "§eDie Nacht wird übersprungen!");

                Bukkit.getScheduler().runTaskLater(plugin.getInstance(), () -> {
                    this.SKIPPED = false;
                    Bukkit.getWorlds().forEach(world -> {
                        world.setTime(0);
                        if (world.hasStorm()) world.setStorm(false);
                    });
                }, 30L);
            }
        }, 10L);
    }

    private int calculate(int total) {
        if (total > 0 && total <= 10)
            return (int) Math.ceil(total * percent_skipNightUnder10);
        else if (total > 10 && total <= 25)
            return (int) Math.ceil(total * percent_skipNightUnder25);
        else if (total > 25 && total <= 40)
            return (int) Math.ceil(total * percent_skipNightUnder40);
        else if (total > 40 && total <= 60)
            return (int) Math.ceil(total * percent_skipNightUnder60);
        else if (total > 60 && total <= 75)
            return (int) Math.ceil(total * percent_skipNightUnder75);
        else return total;
    }

    private int sleeping() {
        return (int) Bukkit.getOnlinePlayers().stream().filter(Player::isSleeping).count();
    }
}