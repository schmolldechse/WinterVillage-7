package de.voldechse.wintervillage.commands;

import de.voldechse.wintervillage.WinterVillage;
import de.voldechse.wintervillage.database.SpawnEggDatabase;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CommandSpawnegg implements CommandExecutor {

    private final WinterVillage plugin;

    public CommandSpawnegg(WinterVillage plugin) {
        this.plugin = plugin;
        this.plugin.getInstance().getCommand("spawnegg").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(this.plugin.serverPrefix + "§cPlease execute this command as a player");
            return true;
        }

        switch (args.length) {
            case 1 -> {
                if (args[0].equalsIgnoreCase("list")) {
                    if (this.plugin.spawnEggDatabase.list().isEmpty()) {
                        player.sendMessage(this.plugin.serverPrefix + "§cAktuell sind keine Entities in der Datenbank gespeichert");
                        return true;
                    }

                    player.sendMessage("§8-------------------------");

                    List<SpawnEggDatabase.EntityData> list = this.plugin.spawnEggDatabase.list();
                    list.forEach(entityData -> {
                        player.sendMessage("§b" + entityData.type.name().toUpperCase() + " §8- §e" + entityData.probability + " %");
                    });

                    player.sendMessage("§8-------------------------");
                    return true;
                }

                String entity = args[0];
                if (EntityType.fromName(entity) == null) {
                    player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching the entity §b" + entity + " §cin Minecraft");
                    return true;
                }

                EntityType entityType = EntityType.fromName(entity);
                if (!this.plugin.spawnEggDatabase.isSaved(entityType)) {
                    player.sendMessage(this.plugin.serverPrefix + "§a" + entityType.name().toUpperCase() + " §cist nicht in der Datenbank gespeichert");
                    return true;
                }

                double probability = this.plugin.spawnEggDatabase.getProbability(entityType);
                player.sendMessage(this.plugin.serverPrefix + "§b" + entityType.name().toUpperCase() + " §8- §e" + probability + " %");
            }

            case 2 -> {
                String entity = args[1];
                if (EntityType.fromName(entity) == null) {
                    player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching the entity §b" + entity + " §cin Minecraft");
                    return true;
                }

                EntityType entityType = EntityType.fromName(entity);

                if (args[0].equalsIgnoreCase("remove")) {
                    if (!this.plugin.spawnEggDatabase.isSaved(entityType)) {
                        player.sendMessage(this.plugin.serverPrefix + "§a" + entityType.name().toUpperCase() + " §cist nicht in der Datenbank gespeichert");
                        return true;
                    }

                    this.plugin.spawnEggDatabase.remove(entityType);
                    player.sendMessage(this.plugin.serverPrefix + "§a" + entityType.name().toUpperCase() + " §7wurde aus der Datenbank entfernt");
                    return true;
                }

                error(player);
            }

            case 3 -> {
                String entity = args[1];
                if (EntityType.fromName(entity) == null) {
                    player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching the entity §b" + entity + " §cin Minecraft");
                    return true;
                }

                EntityType entityType = EntityType.fromName(entity);

                double probability = 0;
                try {
                    probability = Double.parseDouble(args[2]);
                } catch (NumberFormatException exception) {
                    player.sendMessage(this.plugin.serverPrefix + "§cUngültige Zahl");
                }

                if (probability < 0 || probability > 100) {
                    player.sendMessage(this.plugin.serverPrefix + "§cDie Wahrscheinlichkeit muss zwischen 0 und 100 liegen");
                    return true;
                }

                if (args[0].equalsIgnoreCase("create")) {
                    if (this.plugin.spawnEggDatabase.isSaved(entityType)) {
                        player.sendMessage(this.plugin.serverPrefix + "§a" + entityType.name().toUpperCase() + " §cist bereits in der Datenbank gespeichert");
                        return true;
                    }

                    this.plugin.spawnEggDatabase.save(new SpawnEggDatabase.EntityData(entityType, probability));
                    player.sendMessage(this.plugin.serverPrefix + "§b" + entityType.name().toUpperCase() + " §8[§e" + probability + " %§8] §7wurde gespeichert");
                    return true;
                }

                if (args[0].equalsIgnoreCase("modify")) {
                    if (!this.plugin.spawnEggDatabase.isSaved(entityType)) {
                        player.sendMessage(this.plugin.serverPrefix + "§a" + entityType.name().toUpperCase() + " §cist nicht in der Datenbank gespeichert");
                        return true;
                    }

                    this.plugin.spawnEggDatabase.modify(entityType, probability);
                    player.sendMessage(this.plugin.serverPrefix + "§b" + entityType.name().toUpperCase() + " §8[§e" + probability + " %§8] §7wurde gespeichert");
                    return true;
                }

                error(player);
            }

            default -> error(player);
        }

        return false;
    }

    private void error(Player player) {
        player.sendMessage(this.plugin.serverPrefix + "§cFalsche Syntax! Verwende folgendermaßen:");
        player.sendMessage(this.plugin.serverPrefix + "§a/spawnegg <Entity>");
        player.sendMessage(this.plugin.serverPrefix + "§a/spawnegg list");
        player.sendMessage(this.plugin.serverPrefix + "§a/spawnegg remove <Entity>");
        player.sendMessage(this.plugin.serverPrefix + "§a/spawnegg create <Entity> <Wahrscheinlichkeit 0 - 100>");
        player.sendMessage(this.plugin.serverPrefix + "§a/spawnegg modify <Entity> <Wahrscheinlichkeit 0 - 100>");
    }
}
