package de.voldechse.wintervillage.commands;

import de.voldechse.wintervillage.WinterVillage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CommandAdmin implements CommandExecutor {

    private final WinterVillage plugin;

    private final Map<UUID, ItemStack[]> inventoryMap;

    public CommandAdmin(WinterVillage plugin) {
        this.plugin = plugin;
        this.plugin.getInstance().getCommand("admin").setExecutor(this);

        inventoryMap = new HashMap<>();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(this.plugin.serverPrefix + "§cPlease execute this command as a player");
            return true;
        }

        switch (args.length) {
            case 0 -> {
                if (player.hasMetadata("ADMIN")) {
                    this.plugin.removeMetadata(player, "ADMIN");
                    player.getInventory().clear();

                    player.getInventory().setContents(this.inventoryMap.get(player.getUniqueId()));

                    this.inventoryMap.remove(player.getUniqueId());

                    player.setGameMode(GameMode.SURVIVAL);

                    player.sendMessage(this.plugin.serverPrefix + "§eAdminmodus verlassen");
                    return true;
                }

                this.plugin.setMetadata(player, "ADMIN", true);

                ItemStack[] clone = player.getInventory().getContents().clone();
                this.inventoryMap.put(player.getUniqueId(), clone);
                player.getInventory().clear();

                player.setGameMode(GameMode.CREATIVE);

                player.sendMessage(this.plugin.serverPrefix + "§eAdminmodus betreten");
                return true;
            }

            case 1 -> {
                if (!args[0].equalsIgnoreCase("vanish")) {
                    error(player);
                    return true;
                }

                if (player.hasMetadata("VANISH")) {
                    player.setGameMode(GameMode.SURVIVAL);
                    this.plugin.removeMetadata(player, "VANISH");
                    this.updateVisibility(player);

                    player.sendMessage(this.plugin.serverPrefix + "§eAndere Spieler sehen dich nun wieder");
                    return true;
                }

                player.setGameMode(GameMode.CREATIVE);
                this.plugin.setMetadata(player, "VANISH", true);
                this.updateVisibility(player);

                player.sendMessage(this.plugin.serverPrefix + "§eAndere Spieler können dich nun nicht mehr sehen");
            }

            default -> error(player);
        }

        return false;
    }

    private void error(Player player) {
        player.sendMessage(this.plugin.serverPrefix + "§cFalsche Syntax! Verwende folgendermaßen:");
        player.sendMessage(this.plugin.serverPrefix + "§a/admin (vanish)");
    }

    public void updateVisibility(Player player) {
        Bukkit.getOnlinePlayers().forEach(online -> {
            if (player.hasMetadata("VANISH") && !online.hasMetadata("VANISH")) {
                online.hidePlayer(this.plugin.getInstance(), player);
            } else {
                player.showPlayer(this.plugin.getInstance(), online);
                online.showPlayer(this.plugin.getInstance(), player);
            }
        });
    }
}
