package de.voldechse.wintervillage.listener;

import com.sun.management.OperatingSystemMXBean;
import de.voldechse.wintervillage.WinterVillage;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import net.minecraft.server.MinecraftServer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

public class PlayerCommandPreprocessListener implements Listener {

    private final WinterVillage plugin = InjectionLayer.ext().instance(WinterVillage.class);

    @EventHandler
    public void execute(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (plugin.permissionManagement.containsUserAsync(player.getUniqueId()).join() == null) {
            event.setCancelled(true);
            return;
        }
        PermissionUser permissionUser = plugin.permissionManagement.userAsync(player.getUniqueId()).join();
        if (permissionUser == null) {
            event.setCancelled(true);
            return;
        }

        PermissionGroup permissionGroup = plugin.permissionManagement.highestPermissionGroup(permissionUser);
        if (permissionGroup == null) {
            event.setCancelled(true);
            return;
        }


        if (event.getMessage().startsWith("/tps")) {
            event.setCancelled(true);

            if (permissionGroup.potency() < this.plugin.potencySupporter) {
                player.sendMessage("§cIm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.");
                return;
            }

            double[] tps = MinecraftServer.getServer().recentTps;
            OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

            player.sendMessage("§fTPS in den letzten §e1min§f, §e5min §fund §e15min§8: §e" + format(tps[0]) + " §8| §e" + format(tps[1]) + " §8| §e" + format(tps[2]));

            player.sendMessage("§fRAM (Free/Max/Used)§8: "
                    + "§e" + Runtime.getRuntime().freeMemory() / 1024L / 1024
                    + "§8/"
                    + "§e" + Runtime.getRuntime().maxMemory() / 1024L / 1024L
                    + "§8/"
                    + "§e" + (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory()) / 1024L / 1024
                    + " MB");

            player.sendMessage("");
            player.sendMessage("§fCPU Auslastung je Kern§8:");
            for (int i = 0; i < operatingSystemMXBean.getAvailableProcessors(); i++) {
                BigDecimal load = BigDecimal.valueOf(operatingSystemMXBean.getProcessCpuLoad() * 100).setScale(2, RoundingMode.HALF_UP);
                player.sendMessage("§fCore §c" + i + " §f" + (load.intValue() >= 0 ? load : "N/A") + " %");
            }
            return;
        }

        if (permissionGroup.potency() >= plugin.potencyDeveloper) return;

        disabled().forEach(command -> {
            if (event.getMessage().startsWith(command)) {
                event.setCancelled(true);
                player.sendMessage("§cIm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.");
            }
        });
    }

    private List<String> disabled() {
        return List.of( "/?", "/about", "/help", "/pl", "/plugins", "/reload", "/rl", "/timings", "/ver", "/versions",
                "/bukkit:?", "/bukkit:about", "/bukkit:help", "/bukkit:pl", "/bukkit:plugins", "/bukkit:reload", "/bukkit:rl", "/bukkit:timings",
                "/icanhasbukkit",
                "/me", "/say", "/minecraft:me", "/minecraft:say");
    }

    private String format(double tps) {
        return ((tps > 18.0 ? "§a" : (tps > 16.0) ? "§e" : "§c") + ((tps > 20.0) ? "*" : "") + Math.min(Math.round(tps * 100.0) / 100.0, 20.0));
    }
}
