package de.voldechse.wintervillage.commands;

import de.voldechse.wintervillage.WinterVillage;
import de.voldechse.wintervillage.database.ShopDatabase;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class CommandShop implements CommandExecutor {

    private final WinterVillage plugin;

    public CommandShop(WinterVillage plugin) {
        this.plugin = plugin;
        this.plugin.getInstance().getCommand("shop").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(this.plugin.serverPrefix + "§cPlease execute this command as a player");
            return true;
        }

        switch (args.length) {
            case 1 -> {
                if (get(player) == null) return true;

                ShopDatabase.PlayerShop playerShop = this.get(player);

                if (args[0].equalsIgnoreCase("delete")) {
                    if (!this.plugin.permissionManagement.containsUserAsync(playerShop.owner).join()) {
                        player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching the shop owner's entry in database");
                        return true;
                    }
                    PermissionUser permissionUser = this.plugin.permissionManagement.userAsync(playerShop.owner).join();
                    PermissionGroup permissionGroup = this.plugin.permissionManagement.highestPermissionGroup(permissionUser);
                    if (permissionGroup == null) {
                        player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching §b" + permissionUser.name() + "'s entry in database");
                        return true;
                    }

                    if (fromUniqueId(playerShop.location.getWorld(), playerShop.itemDisplay) != null)
                        fromUniqueId(playerShop.location.getWorld(), playerShop.itemDisplay).remove();

                    this.plugin.shopDatabase.removePlayerShop(playerShop.shopIdentifier);

                    for (int i = 1; i <= playerShop.currentAmount.intValue(); i++)
                        player.getWorld().dropItem(playerShop.location.add(0, 1, 0), new ItemStack(playerShop.material));

                    player.sendMessage(this.plugin.serverPrefix + "§aDer Shop wurde gelöscht");
                    return true;
                }

                if (args[0].equalsIgnoreCase("info")) {
                    if (!this.plugin.permissionManagement.containsUserAsync(playerShop.owner).join()) {
                        player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching the shop owner's entry in database");
                        return true;
                    }
                    PermissionUser permissionUser = this.plugin.permissionManagement.userAsync(playerShop.owner).join();
                    PermissionGroup permissionGroup = this.plugin.permissionManagement.highestPermissionGroup(permissionUser);
                    if (permissionGroup == null) {
                        player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching §b" + permissionUser.name() + "'s entry in database");
                        return true;
                    }

                    player.sendMessage(this.plugin.serverPrefix + "§7Dieser Shop gehört " + permissionGroup.color() + permissionUser.name());
                    player.sendMessage(this.plugin.serverPrefix + "§7Verkauft wird§8: §e" + playerShop.material.name().toUpperCase());
                    return true;
                }

                error(player);
                return true;
            }

            case 2 -> {
                String playerName = args[1];
                UUID fetched = this.plugin.mojangAPIFetcher.uuidFrom(playerName);
                if (fetched == null) {
                    player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while fetching the uuid from §b" + playerName);
                    return true;
                }

                if (args[0].equalsIgnoreCase("changeOwner")) {
                    if (get(player) == null) return true;
                    ShopDatabase.PlayerShop playerShop = this.get(player);

                    if (!this.plugin.permissionManagement.containsUserAsync(fetched).join()) {
                        player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching the shop owner's entry in database");
                        return true;
                    }

                    PermissionUser permissionUser = this.plugin.permissionManagement.userAsync(fetched).join();
                    PermissionGroup permissionGroup = this.plugin.permissionManagement.highestPermissionGroup(permissionUser);
                    if (permissionGroup == null) {
                        player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching §b" + permissionUser.name() + "'s entry in database");
                        return true;
                    }

                    this.plugin.shopDatabase.owner(playerShop.shopIdentifier, fetched);

                    player.sendMessage(this.plugin.serverPrefix + "§7Der Owner des Shops ist jetzt " + permissionGroup.color() + permissionUser.name());
                    return true;
                }

                if (args[0].equalsIgnoreCase("info")) {
                    if (!this.plugin.permissionManagement.containsUserAsync(fetched).join()) {
                        player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching the shop owner's entry in database");
                        return true;
                    }

                    PermissionUser permissionUser = this.plugin.permissionManagement.userAsync(fetched).join();
                    PermissionGroup permissionGroup = this.plugin.permissionManagement.highestPermissionGroup(permissionUser);
                    if (permissionGroup == null) {
                        player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching §b" + permissionUser.name() + "'s entry in database");
                        return true;
                    }

                    if (this.plugin.shopDatabase.shops(fetched).isEmpty()) {
                        player.sendMessage(this.plugin.serverPrefix + permissionGroup.color() + permissionUser.name() + " §cbesitzt keine Shops");
                        return true;
                    }

                    List<ShopDatabase.PlayerShop> shops = this.plugin.shopDatabase.shops(fetched);

                    player.sendMessage(this.plugin.serverPrefix + permissionGroup.color() + permissionUser.name() + " §7besitzt derzeit §a" + shops.size() + " §7Shop" + (shops.size() > 1 ? "s" : ""));

                    for (int i = 0; i < shops.size(); i++) {

                        TextComponent textComponent = new TextComponent("§c§oTELEPORT");
                        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + shops.get(0).location.getX() + " " + shops.get(i).location.getY() + " " + shops.get(i).location.getZ()));
                        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§fZum Shop teleportieren").create()));

                        player.spigot().sendMessage(ChatMessageType.CHAT, new ComponentBuilder("§8- §a" + (i + 1) + ". ")
                                .append(textComponent)
                                .append(" §8| §e" + shops.get(i).material.name().toUpperCase())
                                .create()
                        );
                    }

                    return true;
                }

                error(player);
            }

            case 3 -> {
                if (args[0].equalsIgnoreCase("create")) {
                    BigDecimal amount = BigDecimal.valueOf(0);
                    BigDecimal price = BigDecimal.valueOf(0);
                    try {
                        amount = new BigDecimal(args[1]);
                        price = new BigDecimal(args[2]);
                    } catch (NumberFormatException exception) {
                        player.sendMessage(this.plugin.serverPrefix + "§cUngültige Zahl");
                        return true;
                    }

                    if (player.getItemInHand().getType() == Material.AIR) {
                        player.sendMessage(this.plugin.serverPrefix + "§cDu musst in der Hand ein Item halten");
                        return true;
                    }

                    if (this.plugin.shopDatabase.items().size() >= 54) {
                        player.sendMessage(this.plugin.serverPrefix + "§cDas Limit wurde erreicht");
                        return true;
                    }

                    this.plugin.shopDatabase.save(new ShopDatabase.AdminShopItem(
                            player.getItemInHand().clone(),
                            UUID.randomUUID(),
                            price,
                            amount
                    ));

                    player.sendMessage(this.plugin.serverPrefix + "§aErstellt! §e" + this.plugin.shopDatabase.items().size() + "§8/§e54");
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
        player.sendMessage(this.plugin.serverPrefix + "§a/shop info (Spieler)");
        player.sendMessage(this.plugin.serverPrefix + "§a/shop delete");
        player.sendMessage(this.plugin.serverPrefix + "§a/shop changeOwner <Spieler>");
        player.sendMessage(this.plugin.serverPrefix + "§a/shop create <Anzahl> <Preis>");
    }

    private ShopDatabase.PlayerShop get(Player player) {
        if (!(player.getTargetBlock(null, 10).getState() instanceof Sign sign)) {
            player.sendMessage(this.plugin.serverPrefix + "§cDu musst auf ein Schild schauen");
            return null;
        }

        if (!this.plugin.shopDatabase.isSavedPlayerShop(sign.getLocation())) {
            player.sendMessage(this.plugin.serverPrefix + "§cHier ist kein Shop erstellt");
            return null;
        }

        return this.plugin.shopDatabase.shop(sign.getLocation());
    }

    private Entity fromUniqueId(World world, UUID uuid) {
        for (Entity entity : world.getEntities())
            if (entity.getUniqueId().equals(uuid))
                return entity;
        return null;
    }
}
