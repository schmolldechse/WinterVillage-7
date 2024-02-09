package de.voldechse.wintervillage.listener;

import de.voldechse.wintervillage.WinterVillage;
import de.voldechse.wintervillage.database.ShopDatabase;
import de.voldechse.wintervillage.library.document.Document;
import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.util.Position;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Transformation;

import java.math.BigDecimal;
import java.util.UUID;

public class PlayerInteractListener implements Listener {

    @EventHandler
    public void execute(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        WinterVillage plugin = InjectionLayer.ext().instance(WinterVillage.class);

        if (!plugin.permissionManagement.containsUserAsync(player.getUniqueId()).join()) {
            player.sendMessage(plugin.serverPrefix + "§cAn error occurred while searching your entry in database");
            event.setCancelled(true);
            return;
        }

        PermissionUser permissionUser = plugin.permissionManagement.userAsync(player.getUniqueId()).join();
        if (permissionUser == null) {
            player.sendMessage(plugin.serverPrefix + "§cAn error occurred while searching your entry in database");
            event.setCancelled(true);
            return;
        }

        PermissionGroup permissionGroup = plugin.permissionManagement.highestPermissionGroup(permissionUser);
        if (permissionGroup == null) {
            player.sendMessage(plugin.serverPrefix + "§cAn error occurred while searching your entry in database");
            event.setCancelled(true);
            return;
        }

        if (permissionGroup.potency() < plugin.potencyTeilnehmer) {
            player.sendMessage(plugin.serverPrefix + "§cDir fehlen die benötigten Berechtigungen");
            event.setCancelled(true);
            return;
        }

        if (event.getClickedBlock() != null
                && event.getClickedBlock().getState() instanceof Sign
                && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Sign sign = (Sign) event.getClickedBlock().getState();

            if (sign.getLine(0).isEmpty()
                    && sign.getLine(1).equalsIgnoreCase("§0[§4Admin-Shop§0]")
                    && sign.getLine(2).equalsIgnoreCase("§fItems verkaufen")
                    && sign.getLine(3).isEmpty()) {
                event.setUseInteractedBlock(Event.Result.DENY);

                if (plugin.shopDatabase.items().isEmpty()) {
                    player.sendMessage(plugin.serverPrefix + "§cEs gibt derzeit keine eingestellten Items, die verkauft werden können");
                    player.playSound(player.getLocation(), Sound.ENTITY_PILLAGER_DEATH, 1.0f, 1.0f);
                    return;
                }

                Inventory inventory = Bukkit.createInventory(null, 54, "§4Admin-Shop");

                for (ShopDatabase.AdminShopItem adminShopItem : plugin.shopDatabase.items()) {
                    inventory.addItem(new ItemBuilder(adminShopItem.itemStack.getType(), adminShopItem.amount.intValue())
                            .lore(
                                    "",
                                    "§7Identifier-Id§8: §c" + adminShopItem.identifierId.toString(),
                                    "",
                                    "§7Du kriegst §e" + plugin.formatBalance(adminShopItem.price) + " $§7, für",
                                    "§a" + adminShopItem.amount.intValue() + "x §e" + adminShopItem.itemStack.getType().name().toUpperCase(),
                                    ""
                            )
                            .build());
                }

                player.openInventory(inventory);
            }

            if (sign.getLine(0).isEmpty()
                    && sign.getLine(1).equalsIgnoreCase("§cSHOP")
                    && sign.getLine(2).equalsIgnoreCase("§cKONFIGURATION")
                    && sign.getLine(3).isEmpty()) {
                event.setUseInteractedBlock(Event.Result.DENY);

                if (!player.hasMetadata("SHOP_CONFIGURATION")) {
                    event.setCancelled(true);
                    player.sendMessage(plugin.serverPrefix + "§cDu kannst die Konfiguration nicht beenden, da dieser Shop dir nicht gehört");
                    player.playSound(player.getLocation(), Sound.ENTITY_PILLAGER_DEATH, 1.0f, 1.0f);
                    return;
                }

                Document document = (Document) player.getMetadata("SHOP_CONFIGURATION").get(0).value();

                if (document.get("shop_location") == null || !document.contains("shop_location")) {
                    event.setCancelled(true);
                    player.sendMessage(plugin.serverPrefix + "§cEin Fehler ist während des Erstellens deines Shops aufgetreten");
                    player.sendMessage(plugin.serverPrefix + "§bshop_location §cis null");
                    player.playSound(player.getLocation(), Sound.ENTITY_PILLAGER_DEATH, 1.0f, 1.0f);
                    return;
                }

                Document shop_location = document.getDocument("shop_location");
                Position position = new Position(shop_location.getDouble("x"), shop_location.getDouble("y"), shop_location.getDouble("z"), shop_location.getString("world"));

                if (!(event.getClickedBlock().getLocation().getX() == position.x
                        && event.getClickedBlock().getLocation().getY() == position.y
                        && event.getClickedBlock().getLocation().getZ() == position.z
                        && event.getClickedBlock().getLocation().getWorld().getName().equals(position.world))) {
                    event.setCancelled(true);
                    player.sendMessage(plugin.serverPrefix + "§cDieser Shop gehört nicht dir");
                    player.playSound(player.getLocation(), Sound.ENTITY_PILLAGER_DEATH, 1.0f, 1.0f);
                    return;
                }

                if (event.getItem() == null) {
                    event.setCancelled(true);
                    player.sendMessage(plugin.serverPrefix + "§cDu musst das Schild mit einem Item rechtsklicken");
                    player.playSound(player.getLocation(), Sound.ENTITY_PILLAGER_DEATH, 1.0f, 1.0f);
                    return;
                }

                if (event.getItem().getType() == Material.AIR) {
                    event.setCancelled(true);
                    player.sendMessage(plugin.serverPrefix + "§cDieses Item darfst du nicht verkaufen");
                    player.playSound(player.getLocation(), Sound.ENTITY_PILLAGER_DEATH, 1.0f, 1.0f);
                    return;
                }

                sign.setLine(0, "§0[§aShop§0]");
                sign.setLine(1, "§c" + document.getString("shop_name"));
                sign.setLine(2, "§f" + plugin.formatBalance(document.get("price").getAsBigDecimal()) + " $");
                sign.setLine(3, "§f" + document.get("amount").getAsBigDecimal() + " Stk.");
                sign.update();
                sign.setEditable(false);

                UUID itemDisplayUniqueId;
                if (event.getItem().getType().isBlock()) {
                    BlockDisplay blockDisplay = player.getWorld().spawn(event.getClickedBlock().getLocation().add(.25, 2, .25), BlockDisplay.class);
                    blockDisplay.setBlock(Bukkit.createBlockData(event.getItem().getType()));

                    Transformation transformation = blockDisplay.getTransformation();
                    transformation.getScale().set(.5D);

                    blockDisplay.setTransformation(transformation);

                    //blockDisplay.setBillboard(Display.Billboard.CENTER);
                    blockDisplay.setViewRange(0.2F);
                    blockDisplay.setShadowRadius(1F);

                    itemDisplayUniqueId = blockDisplay.getUniqueId();
                } else {
                    ItemDisplay itemDisplay = player.getWorld().spawn(event.getClickedBlock().getLocation().add(.5, 2, .5), ItemDisplay.class);
                    itemDisplay.setItemStack(new ItemStack(event.getItem().getType()));

                    Transformation transformation = itemDisplay.getTransformation();
                    transformation.getScale().set(.5D);

                    itemDisplay.setTransformation(transformation);

                    //itemDisplay.setBillboard(Display.Billboard.CENTER);
                    itemDisplay.setViewRange(0.2F);
                    itemDisplay.setShadowRadius(1F);

                    itemDisplayUniqueId = itemDisplay.getUniqueId();
                }

                plugin.removeMetadata(player, "SHOP_CONFIGURATION");
                plugin.shopDatabase.save(new ShopDatabase.PlayerShop(
                        UUID.randomUUID(),
                        player.getUniqueId(),
                        itemDisplayUniqueId,
                        event.getClickedBlock().getLocation(),
                        Side.valueOf(document.getString("sign_side")),
                        event.getItem().getType(),
                        document.get("price").getAsBigDecimal(),
                        document.get("amount").getAsBigDecimal(),
                        BigDecimal.valueOf(0),
                        false
                ));

                player.sendMessage(plugin.serverPrefix + "§aDu hast deinen §e[" + event.getItem().getType().name().toUpperCase() + "] §aShop erstellt");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

                return;
            }

            if (plugin.shopDatabase.isSavedPlayerShop(event.getClickedBlock().getLocation())) {
                event.setUseInteractedBlock(Event.Result.DENY);

                ShopDatabase.PlayerShop playerShop = plugin.shopDatabase.shop(event.getClickedBlock().getLocation());
                if (playerShop.owner.equals(player.getUniqueId())) {

                    plugin.shopDatabase.editing(playerShop.shopIdentifier, true);

                    Inventory inventory = Bukkit.createInventory(null, 27, "§0Shop " + playerShop.shopIdentifier);

                    for (int i = 1; i <= playerShop.currentAmount.intValue(); i++)
                        inventory.addItem(new ItemBuilder(playerShop.material).build());

                    player.openInventory(inventory);

                    player.playSound(player.getLocation(), Sound.ENTITY_HORSE_SADDLE, 1.0f, 1.0f);
                    return;
                }

                if (!plugin.permissionManagement.containsUserAsync(playerShop.owner).join()) {
                    player.sendMessage(plugin.serverPrefix + "§cCould not determine shop owner's user data");
                    return;
                }
                PermissionUser owner = plugin.permissionManagement.userAsync(playerShop.owner).join();
                PermissionGroup ownersGroup = plugin.permissionManagement.highestPermissionGroup(owner);
                if (ownersGroup == null) {
                    player.sendMessage(plugin.serverPrefix + "§cCould not determine §b" + owner.name() + " §crank data");
                    return;
                }

                if (playerShop.editing) {
                    player.sendMessage(plugin.serverPrefix + ownersGroup.color().replace("&", "§") + owner.name() + " §cbearbeitet gerade seinen Shop");
                    return;
                }

                if (playerShop.currentAmount.compareTo(playerShop.amountPerPrice) < 0) {
                    player.sendMessage(plugin.serverPrefix + "§cDer Shop ist nicht voll");
                    return;
                }

                if (plugin.balanceDatabase.balance(player.getUniqueId()).compareTo(playerShop.price) < 0) {
                    player.sendMessage(plugin.serverPrefix + "§cDu hast zu wenig Geld auf deinem Konto");
                    return;
                }

                if (!enoughSpace(player, playerShop.material, playerShop.amountPerPrice.intValue())) {
                    player.sendMessage(plugin.serverPrefix + "§cDein Inventar ist voll");
                    return;
                }

                plugin.balanceDatabase.modify(player.getUniqueId(), playerShop.price.negate());
                plugin.balanceDatabase.modify(playerShop.owner, playerShop.price);

                plugin.shopDatabase.amount(playerShop.shopIdentifier, playerShop.currentAmount.subtract(playerShop.amountPerPrice));

                plugin.scoreboardManager.updateScoreboard(player, "currentBalance", " §e" + plugin.formatBalance(plugin.balanceDatabase.balance(player.getUniqueId())) + " $", "");

                if (Bukkit.getPlayer(playerShop.owner) != null) {
                    Player ownerPlayer = Bukkit.getPlayer(playerShop.owner);
                    ownerPlayer.sendMessage(plugin.serverPrefix + "§fDu hast §8[§e" + playerShop.amountPerPrice.intValue() + "§8] §e" + playerShop.material.name().toUpperCase() + " §fverkauft");
                    plugin.scoreboardManager.updateScoreboard(ownerPlayer, "currentBalance", " §e" + plugin.formatBalance(plugin.balanceDatabase.balance(ownerPlayer.getUniqueId())) + " $", "");
                }

                player.sendMessage(plugin.serverPrefix + "§aDu hast §8[§e" + playerShop.amountPerPrice.intValue() + "§8] §e" + playerShop.material.name().toUpperCase() + " §agekauft");

                //for (int i = 1; i <= playerShop.amountPerPrice.intValue(); i++)
                //    player.getInventory().addItem(new ItemBuilder(playerShop.material, i).build());

                this.buy(player, playerShop.material, playerShop.amountPerPrice.intValue());
            }

            switch (sign.getLine(1)) {
                case "§0Zur §aFarmwelt" -> {
                    event.setUseInteractedBlock(Event.Result.DENY);

                    if (Bukkit.getWorld("world_farmwelt") == null) {
                        player.sendMessage(plugin.serverPrefix + "§cDie Farmwelt wurde nicht erstellt");
                        return;
                    }

                    player.teleport(Bukkit.getWorld("world_farmwelt").getSpawnLocation());
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                }

                case "§0In den §cNether" -> {
                    event.setUseInteractedBlock(Event.Result.DENY);

                    if (Bukkit.getWorld("world_nether") == null) {
                        player.sendMessage(plugin.serverPrefix + "§cDer Nether wurde nicht erstellt");
                        return;
                    }

                    player.teleport(Bukkit.getWorld("world_nether").getSpawnLocation());
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                }

                case "§0In das §5End" -> {
                    event.setUseInteractedBlock(Event.Result.DENY);

                    if (Bukkit.getWorld("world_the_end") == null) {
                        player.sendMessage(plugin.serverPrefix + "§cDas Ende wurde nicht erstellt");
                        return;
                    }

                    player.teleport(Bukkit.getWorld("world_the_end").getSpawnLocation());
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                }
            }
        }
    }

    private void buy(Player player, Material material, int amount) {
        if (amount <= 0) return;

        int x1 = amount / 64;
        int x2 = amount % 64;

        for (int i = 0; i < x1; i++) {
            ItemStack itemStack = new ItemStack(material, 64);
            player.getInventory().addItem(itemStack);
        }

        if (x2 > 0) {
            ItemStack itemStack = new ItemStack(material, x2);
            player.getInventory().addItem(itemStack);
        }
    }

    private boolean enoughSpace(Player player, Material material, int amount) {
        int freeSpace = 0;

        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (itemStack == null || itemStack.getType() == Material.AIR)
                freeSpace += material.getMaxStackSize();
            else if (itemStack.getType() == material && itemStack.getAmount() < itemStack.getMaxStackSize())
                freeSpace += itemStack.getMaxStackSize() - itemStack.getAmount();
        }

        return freeSpace >= amount;
    }
}