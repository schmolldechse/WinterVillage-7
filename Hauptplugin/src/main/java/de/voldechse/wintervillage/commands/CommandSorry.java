package de.voldechse.wintervillage.commands;

import de.voldechse.wintervillage.WinterVillage;
import de.voldechse.wintervillage.database.WhitelistDatabase;
import de.voldechse.wintervillage.library.util.ItemBuilder;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

public class CommandSorry implements CommandExecutor {

    private LocalDateTime check;

    private final WinterVillage plugin;

    public CommandSorry(WinterVillage plugin) {
        this.plugin = plugin;
        this.plugin.getInstance().getCommand("sorry").setExecutor(this);

        this.check = LocalDateTime.of(2023, 11, 26, 0, 0);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(this.plugin.serverPrefix + "§cPlease execute this command as a player");
            return true;
        }

        if (!this.plugin.whitelistDatabase.whitelisted(player.getUniqueId())) {
            player.sendMessage(this.plugin.serverPrefix + "§cDu bist nicht dazu berechtigt, das Entschädigungspaket einzusammeln");
            return true;
        }

        if (!this.plugin.secondCompensationDatabase.saved(player.getUniqueId())) {
            player.sendMessage(this.plugin.serverPrefix + "§cDu bist nicht dazu berechtigt, das Entschädigungspaket einzusammeln");
            return true;
        }

        WhitelistDatabase.WhitelistData whitelist = this.plugin.whitelistDatabase.data(player.getUniqueId());
        if (!whitelist.date.isBefore(check)) {
            player.sendMessage(this.plugin.serverPrefix + "§cDu bist nicht dazu berechtigt, das Entschädigungspaket einzusammeln");
            return true;
        }

        boolean collected = this.plugin.compensationDatabase.collected(player.getUniqueId());
        if (collected) {
            player.sendMessage(this.plugin.serverPrefix + "§cDu hast deine Wiedergutmachung bereits erhalten");
            return true;
        }

        Inventory inventory = Bukkit.createInventory(null, 9, "§4Wiedergutmachung");

        inventory.setItem(2, new ItemBuilder(Material.SHULKER_BOX, 1, "§cPaket 1")
                        .lore("",
                                "§fAls Wiedergutmachung für den schweren Start, kriegst du für dieses Paket",
                                "",
                                "§8- §fDiamantschwert§8, §fDiamantspitzhacke",
                                "§8- §f1 Stack XP-Flaschen",
                                "§8- §f2 Stacks Essen, 7 Goldäpfel",
                                "§8- §f32 Diamanten",
                                "§8- §f1 Stack Eisen",
                                "§8- §f4 Netherite",
                                "§8- §f3 Stacks Holz",
                                "§8- §f7 Stacks Stein",
                                "§8- §f2 Dorfbewohner Spawneier")
                .build());
        inventory.setItem(4, new ItemBuilder(Material.SHULKER_BOX, 1, "§cPaket 2")
                .lore("",
                        "§fAls Wiedergutmachung für den schweren Start, kriegst du für dieses Paket",
                        "",
                        "§8- §f4 Stacks Holz",
                        "§8- §f2 Stacks XP-Flaschen",
                        "§8- §f32 Diamanten",
                        "§8- §f1 Stack Lapis",
                        "§8- §f1 Eichensetzling",
                        "§8- §f1 Fichtensetzling",
                        "§8- §f1 Birkensetzling",
                        "§8- §f1 Tropenbaumsetzling",
                        "§8- §f1 Akaziensetzling",
                        "§8- §f4 Schwarzeichensetzlinge",
                        "§8- §f1 Kirschsetzling",
                        "§8- §f1 Stack Prismarin",
                        "§8- §f4 Stacks Stein",
                        "§8- §f1 Stack Essen",
                        "§8- §f2 Dorfbewohner Spawneier")
                .build());
        inventory.setItem(6, new ItemBuilder(Material.SHULKER_BOX, 1, "§cPaket 3")
                .lore("",
                        "§fAls Wiedergutmachung für den schweren Start, kriegst du für dieses Paket",
                        "",
                        "§8- §fDiamantspitzhacke",
                        "§8- §f1 Stack Redstone Repeater",
                        "§8- §f1 Stack Redstone Comparator",
                        "§8- §f1 Stack Redstone",
                        "§8- §f1 Stack Diamanten",
                        "§8- §f18 Trichter",
                        "§8- §f1 Stack XP-Flaschen",
                        "§8- §f3 Stacks Holz",
                        "§8- §f7 Stacks Stein",
                        "§8- §f3 Stacks Terracotta",
                        "§8- §f2 Stacks Essen")
                .build());

        player.openInventory(inventory);

        return false;
    }
}