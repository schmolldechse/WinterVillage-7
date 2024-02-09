package de.voldechse.wintervillage.commands;

import de.voldechse.wintervillage.WinterVillage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandFarmwelt implements CommandExecutor {

    private final WinterVillage plugin;

    public CommandFarmwelt(WinterVillage plugin) {
        this.plugin = plugin;
        this.plugin.getInstance().getCommand("farmwelt").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(this.plugin.serverPrefix + "§cPlease execute this command as a player");
            return true;
        }

        switch (args.length) {
            case 1 -> {
                if (args[0].equalsIgnoreCase("world")) {
                    if (Bukkit.getWorld("world_farmwelt") == null) {
                        player.sendMessage(this.plugin.serverPrefix + "§cDie Farmwelt konnte nicht gefunden werden");
                        return true;
                    }

                    player.teleport(Bukkit.getWorld("world_farmwelt").getSpawnLocation());
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                    return true;
                }

                if (args[0].equalsIgnoreCase("nether")) {
                    if (Bukkit.getWorld("world_nether") == null) {
                        player.sendMessage(this.plugin.serverPrefix + "§cDer Nether konnte nicht gefunden werden");
                        return true;
                    }

                    player.teleport(Bukkit.getWorld("world_nether").getSpawnLocation());
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                    return true;
                }

                if (args[0].equalsIgnoreCase("end")) {
                    if (Bukkit.getWorld("world_the_end") == null) {
                        player.sendMessage(this.plugin.serverPrefix + "§cDas Ende konnte nicht gefunden werden");
                        return true;
                    }

                    player.teleport(Bukkit.getWorld("world_the_end").getSpawnLocation());
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                    return true;
                }
            }

            default -> {
                player.sendMessage(this.plugin.serverPrefix + "§cFalsche Syntax! Verwende folgendermaßen:");
                player.sendMessage(this.plugin.serverPrefix + "§a/farmwelt world §eum in die Farmwelt zu gelangen");
                player.sendMessage(this.plugin.serverPrefix + "§a/farmwelt nether §eum in den Nether zu gelangen");
                player.sendMessage(this.plugin.serverPrefix + "§a/farmwelt end §eum in das End zu gelangen");
            }
        }
        return true;
    }
}