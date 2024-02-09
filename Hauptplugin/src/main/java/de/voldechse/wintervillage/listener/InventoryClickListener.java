package de.voldechse.wintervillage.listener;

import de.voldechse.wintervillage.WinterVillage;
import de.voldechse.wintervillage.database.ShopDatabase;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class InventoryClickListener implements Listener {

    @EventHandler
    public void execute(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        WinterVillage plugin = InjectionLayer.ext().instance(WinterVillage.class);

        if (event.getClickedInventory() != null && event.getView().getTitle().equalsIgnoreCase("§4Wiedergutmachung")) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null) return;

            ItemStack current = event.getCurrentItem();
            if (!current.hasItemMeta()) return;

            if (!plugin.compensationDatabase.saved(player.getUniqueId())) {
                event.getView().close();
                player.sendMessage(plugin.serverPrefix + "§cAn error occurred while searching your entry in database");
                return;
            }

            if (plugin.compensationDatabase.collected(player.getUniqueId())) {
                event.getView().close();
                player.sendMessage(plugin.serverPrefix + "§cDu hast deine Wiedergutmachung bereits erhalten");
                return;
            }

            if (current.getItemMeta().getDisplayName().equalsIgnoreCase("§cPaket 1")) {
                event.getView().close();

                ItemStack itemStack = new ItemStack(Material.SHULKER_BOX);
                ItemMeta itemMeta = itemStack.getItemMeta();

                if (itemMeta instanceof BlockStateMeta) {
                    BlockStateMeta blockStateMeta = (BlockStateMeta) itemMeta;
                    if (blockStateMeta.getBlockState() instanceof ShulkerBox) {
                        ShulkerBox shulkerBox = (ShulkerBox) blockStateMeta.getBlockState();

                        packetA().forEach(item -> shulkerBox.getInventory().addItem(item));

                        blockStateMeta.setBlockState(shulkerBox);

                        blockStateMeta.setLore(Arrays.asList("§4Wiedergutmachung"));
                        itemStack.setItemMeta(blockStateMeta);

                        player.getInventory().addItem(itemStack);
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                        player.sendMessage(plugin.serverPrefix + "§aDu hast deine Wiedergutmachung erhalten");

                        plugin.compensationDatabase.modify(player.getUniqueId(), true);
                        return;
                    }
                }

                player.sendMessage(plugin.serverPrefix + "§cAn error occurred while fetching itemmeta of shulkerbox");
                return;
            }

            if (current.getItemMeta().getDisplayName().equalsIgnoreCase("§cPaket 2")) {
                event.getView().close();

                ItemStack itemStack = new ItemStack(Material.SHULKER_BOX);
                ItemMeta itemMeta = itemStack.getItemMeta();

                if (itemMeta instanceof BlockStateMeta) {
                    BlockStateMeta blockStateMeta = (BlockStateMeta) itemMeta;
                    if (blockStateMeta.getBlockState() instanceof ShulkerBox) {
                        ShulkerBox shulkerBox = (ShulkerBox) blockStateMeta.getBlockState();

                        packetB().forEach(item -> shulkerBox.getInventory().addItem(item));

                        blockStateMeta.setBlockState(shulkerBox);

                        itemStack.setItemMeta(blockStateMeta);

                        player.getInventory().addItem(itemStack);
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                        player.sendMessage(plugin.serverPrefix + "§aDu hast deine Wiedergutmachung erhalten");

                        plugin.compensationDatabase.modify(player.getUniqueId(), true);
                        return;
                    }
                }

                player.sendMessage(plugin.serverPrefix + "§cAn error occurred while fetching itemmeta of shulkerbox");
                return;
            }

            if (current.getItemMeta().getDisplayName().equalsIgnoreCase("§cPaket 3")) {
                event.getView().close();

                ItemStack itemStack = new ItemStack(Material.SHULKER_BOX);
                ItemMeta itemMeta = itemStack.getItemMeta();

                if (itemMeta instanceof BlockStateMeta) {
                    BlockStateMeta blockStateMeta = (BlockStateMeta) itemMeta;
                    if (blockStateMeta.getBlockState() instanceof ShulkerBox) {
                        ShulkerBox shulkerBox = (ShulkerBox) blockStateMeta.getBlockState();

                        packetC().forEach(item -> shulkerBox.getInventory().addItem(item));

                        blockStateMeta.setBlockState(shulkerBox);

                        itemStack.setItemMeta(blockStateMeta);

                        player.getInventory().addItem(itemStack);
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                        player.sendMessage(plugin.serverPrefix + "§aDu hast deine Wiedergutmachung erhalten");

                        plugin.compensationDatabase.modify(player.getUniqueId(), true);
                        return;
                    }
                }

                player.sendMessage(plugin.serverPrefix + "§cAn error occurred while fetching itemmeta of shulkerbox");
                return;
            }
        }

        if (event.getClickedInventory() != null && event.getView().getTitle().startsWith("§0Shop")) {
            String[] name = event.getView().getTitle().split("§0Shop ");

            UUID identifierId = UUID.fromString(ChatColor.stripColor(name[1]));
            if (identifierId == null) {
                event.setCancelled(true);
                event.getView().close();
                player.sendMessage(plugin.serverPrefix + "§cCould not determine shop identifier id");
                return;
            }

            if (!plugin.shopDatabase.isSavedPlayerShop(identifierId)) return;

            ShopDatabase.PlayerShop playerShop = plugin.shopDatabase.shop(identifierId);

            if ((event.getCurrentItem() != null && event.getCurrentItem().getType() != playerShop.material)
                    || (event.getCursor().getType() != playerShop.material && event.getClick().isKeyboardClick())) {
                event.setCancelled(true);
                player.sendMessage(plugin.serverPrefix + "§cDu kannst nur §e" + playerShop.material.name().toUpperCase() + " §cin den Shop legen");
                player.playSound(player.getLocation(), Sound.ENTITY_PILLAGER_DEATH, 1.0f, 1.0f);
                return;
            }

            event.setCancelled(false);
            return;
        }

        if (event.getClickedInventory() != null && event.getView().getTitle().equalsIgnoreCase("§4Admin-Shop")) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null) return;

            if (event.getCurrentItem().getItemMeta().getLore() == null) return;

            if (event.getCurrentItem().getItemMeta().getLore().isEmpty()) return;

            String[] lore_1 = event.getCurrentItem().getItemMeta().getLore().get(1).split("§7Identifier-Id§8: ");

            UUID identifierId = UUID.fromString(ChatColor.stripColor(lore_1[1]));
            if (identifierId == null) {
                event.setCancelled(true);
                event.getView().close();
                player.sendMessage(plugin.serverPrefix + "§cCould not determine item identifier id");
                return;
            }

            if (player.hasPermission("wintervillage.edit.admin-shop") && event.getClick() == ClickType.SHIFT_RIGHT) {
                plugin.shopDatabase.removeAdminShopItem(identifierId);
                event.getInventory().remove(event.getCurrentItem());
                player.sendMessage(plugin.serverPrefix + "§eDas Item wurde entfernt");
                return;
            }

            ShopDatabase.AdminShopItem adminShopItem = plugin.shopDatabase.item(identifierId);

            if (!hasEnough(player, adminShopItem.itemStack.getType(), adminShopItem.amount.intValue())) {
                player.sendMessage(plugin.serverPrefix + "§cDu hast nicht genug von §e" + adminShopItem.itemStack.getType().name().toUpperCase() + " §c§omind. " + adminShopItem.amount.intValue());
                player.playSound(player.getLocation(), Sound.ENTITY_PILLAGER_DEATH, 1.0f, 1.0f);
                return;
            }

            if (player.getInventory().getItemInOffHand() != null
                    && player.getInventory().getItemInOffHand().getType() == adminShopItem.itemStack.getType()) {
                if (player.getInventory().getItemInOffHand().getAmount() < adminShopItem.amount.intValue()) {
                    player.sendMessage(plugin.serverPrefix + "§cDu hast nicht genug von §e" + adminShopItem.itemStack.getType().name().toUpperCase() + " §c§omind. " + adminShopItem.amount.intValue());
                    player.playSound(player.getLocation(), Sound.ENTITY_PILLAGER_DEATH, 1.0f, 1.0f);
                    return;
                }

                int amount = player.getInventory().getItemInOffHand().getAmount();
                if (amount > adminShopItem.amount.intValue()) {
                    ItemStack itemStack = player.getInventory().getItemInOffHand();
                    itemStack.setAmount(amount - adminShopItem.amount.intValue());
                    player.getInventory().setItemInOffHand(itemStack);
                } else player.getInventory().setItemInOffHand(null);

                plugin.scoreboardManager.updateScoreboard(player, "currentBalance", " §e" + plugin.formatBalance(plugin.balanceDatabase.balance(player.getUniqueId())) + " $", "");

                player.sendMessage(plugin.serverPrefix + "§7Du hast §a" + adminShopItem.amount.intValue() + " " + adminShopItem.itemStack.getType().name().toUpperCase() + " §7für §e" + plugin.formatBalance(adminShopItem.price) + " $ §7verkauft");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, .5f, 1.0f);
                return;
            }

            HashMap<Integer, ItemStack> items = player.getInventory().removeItem(new ItemStack(adminShopItem.itemStack.getType(), adminShopItem.amount.intValue()));
            int notRemoved = 0;
            for (ItemStack itemStack : items.values())
                notRemoved += itemStack.getAmount();

            plugin.balanceDatabase.modify(player.getUniqueId(), adminShopItem.price);

            plugin.scoreboardManager.updateScoreboard(player, "currentBalance", " §e" + plugin.formatBalance(plugin.balanceDatabase.balance(player.getUniqueId())) + " $", "");

            player.sendMessage(plugin.serverPrefix + "§7Du hast §a" + adminShopItem.amount.intValue() + " " + adminShopItem.itemStack.getType().name().toUpperCase() + " §7für §e" + plugin.formatBalance(adminShopItem.price) + " $ §7verkauft");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, .5f, 1.0f);
        }
    }

    private boolean hasEnough(Player player, Material material, int amount) {
        int a = 0;

        for (ItemStack itemStack : player.getInventory().getContents())
            if (itemStack != null && itemStack.getType() == material)
                a += itemStack.getAmount();

        return a >= amount;
    }

    private List<ItemStack> packetA() {
        List<ItemStack> list = new ArrayList<>();
        list.add(new ItemStack(Material.DIAMOND_SWORD));
        list.add(new ItemStack(Material.DIAMOND_PICKAXE));
        list.add(new ItemStack(Material.EXPERIENCE_BOTTLE, 64));
        list.add(new ItemStack(Material.IRON_INGOT, 64));
        list.add(new ItemStack(Material.DIAMOND, 32));
        list.add(new ItemStack(Material.NETHERITE_SCRAP, 4));
        list.add(new ItemStack(Material.COOKED_PORKCHOP, 64));
        list.add(new ItemStack(Material.COOKED_PORKCHOP, 64));
        list.add(new ItemStack(Material.GOLDEN_APPLE, 7));
        list.add(new ItemStack(Material.OAK_LOG, 64));
        list.add(new ItemStack(Material.OAK_LOG, 64));
        list.add(new ItemStack(Material.OAK_LOG, 64));
        list.add(new ItemStack(Material.STONE, 64));
        list.add(new ItemStack(Material.STONE, 64));
        list.add(new ItemStack(Material.STONE, 64));
        list.add(new ItemStack(Material.STONE, 64));
        list.add(new ItemStack(Material.STONE, 64));
        list.add(new ItemStack(Material.STONE, 64));
        list.add(new ItemStack(Material.STONE, 64));
        list.add(new ItemStack(Material.VILLAGER_SPAWN_EGG, 2));
        return list;
    }

    private List<ItemStack> packetB() {
        List<ItemStack> list = new ArrayList<>();
        list.add(new ItemStack(Material.OAK_LOG, 64));
        list.add(new ItemStack(Material.OAK_LOG, 64));
        list.add(new ItemStack(Material.OAK_LOG, 64));
        list.add(new ItemStack(Material.OAK_LOG, 64));
        list.add(new ItemStack(Material.EXPERIENCE_BOTTLE, 64));
        list.add(new ItemStack(Material.EXPERIENCE_BOTTLE, 64));
        list.add(new ItemStack(Material.DIAMOND, 32));
        list.add(new ItemStack(Material.LAPIS_LAZULI, 64));
        list.add(new ItemStack(Material.PRISMARINE, 64));
        list.add(new ItemStack(Material.OAK_SAPLING));
        list.add(new ItemStack(Material.SPRUCE_SAPLING));
        list.add(new ItemStack(Material.BIRCH_SAPLING));
        list.add(new ItemStack(Material.JUNGLE_SAPLING));
        list.add(new ItemStack(Material.ACACIA_SAPLING));
        list.add(new ItemStack(Material.DARK_OAK_SAPLING, 4));
        list.add(new ItemStack(Material.CHERRY_SAPLING));
        list.add(new ItemStack(Material.STONE, 64));
        list.add(new ItemStack(Material.STONE, 64));
        list.add(new ItemStack(Material.STONE, 64));
        list.add(new ItemStack(Material.STONE, 64));
        list.add(new ItemStack(Material.COOKED_PORKCHOP, 64));
        list.add(new ItemStack(Material.VILLAGER_SPAWN_EGG, 2));
        return list;
    }

    private List<ItemStack> packetC() {
        List<ItemStack> list = new ArrayList<>();
        list.add(new ItemStack(Material.REPEATER, 64));
        list.add(new ItemStack(Material.COMPARATOR, 64));
        list.add(new ItemStack(Material.REDSTONE, 64));
        list.add(new ItemStack(Material.DIAMOND, 32));
        list.add(new ItemStack(Material.HOPPER, 18));
        list.add(new ItemStack(Material.EXPERIENCE_BOTTLE, 64));
        list.add(new ItemStack(Material.OAK_LOG, 64));
        list.add(new ItemStack(Material.OAK_LOG, 64));
        list.add(new ItemStack(Material.OAK_LOG, 64));
        list.add(new ItemStack(Material.STONE, 64));
        list.add(new ItemStack(Material.STONE, 64));
        list.add(new ItemStack(Material.STONE, 64));
        list.add(new ItemStack(Material.STONE, 64));
        list.add(new ItemStack(Material.STONE, 64));
        list.add(new ItemStack(Material.STONE, 64));
        list.add(new ItemStack(Material.STONE, 64));
        list.add(new ItemStack(Material.TERRACOTTA, 64));
        list.add(new ItemStack(Material.TERRACOTTA, 64));
        list.add(new ItemStack(Material.TERRACOTTA, 64));
        list.add(new ItemStack(Material.DIAMOND_PICKAXE));
        list.add(new ItemStack(Material.COOKED_PORKCHOP, 64));
        list.add(new ItemStack(Material.COOKED_PORKCHOP, 64));
        return list;
    }
}