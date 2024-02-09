package de.voldechse.wintervillage.commands;

import de.voldechse.wintervillage.WinterVillage;
import de.voldechse.wintervillage.database.WhitelistDatabase;
import de.voldechse.wintervillage.library.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.ShulkerBox;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandServerproblem implements CommandExecutor {

    private LocalDateTime check;

    private final WinterVillage plugin;

    public CommandServerproblem(WinterVillage plugin) {
        this.plugin = plugin;
        this.plugin.getInstance().getCommand("serverproblem").setExecutor(this);

        this.check = LocalDateTime.of(2023, 11, 29, 0, 0);
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

        boolean collected = this.plugin.secondCompensationDatabase.collected(player.getUniqueId());
        if (collected) {
            player.sendMessage(this.plugin.serverPrefix + "§cDu hast deine Wiedergutmachung bereits erhalten");
            return true;
        }

        ItemStack box1 = new ItemStack(Material.SHULKER_BOX);
        ItemMeta box1Meta = box1.getItemMeta();

        if (box1Meta instanceof BlockStateMeta) {
            BlockStateMeta blockStateMeta = (BlockStateMeta) box1Meta;
            if (blockStateMeta.getBlockState() instanceof ShulkerBox) {
                ShulkerBox shulkerBox = (ShulkerBox) blockStateMeta.getBlockState();

                boxA().forEach(item -> shulkerBox.getInventory().addItem(item));

                blockStateMeta.setBlockState(shulkerBox);

                box1.setItemMeta(blockStateMeta);
            }
        }

        ItemStack box2 = new ItemStack(Material.SHULKER_BOX);
        ItemMeta box2Meta = box2.getItemMeta();

        if (box2Meta instanceof BlockStateMeta) {
            BlockStateMeta blockStateMeta = (BlockStateMeta) box2Meta;
            if (blockStateMeta.getBlockState() instanceof ShulkerBox) {
                ShulkerBox shulkerBox = (ShulkerBox) blockStateMeta.getBlockState();

                boxB().forEach(item -> shulkerBox.getInventory().addItem(item));

                blockStateMeta.setBlockState(shulkerBox);

                box2.setItemMeta(blockStateMeta);
            }
        }
        player.getInventory().addItem(box1);
        player.getInventory().addItem(box2);

        player.sendMessage(this.plugin.serverPrefix + "§aDu hast deine Wiedergutmachung erhalten");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        this.plugin.secondCompensationDatabase.modify(player.getUniqueId(), true);

        return false;
    }

    private List<ItemStack> boxA() {
        List<ItemStack> list = new ArrayList<>();
        list.add(new ItemStack(Material.OAK_LOG, 64));
        list.add(new ItemStack(Material.OAK_LOG, 64));
        list.add(new ItemStack(Material.SPRUCE_LOG, 64));
        list.add(new ItemStack(Material.SPRUCE_LOG, 64));
        list.add(new ItemStack(Material.BIRCH_LOG, 64));
        list.add(new ItemStack(Material.BIRCH_LOG, 64));
        list.add(new ItemStack(Material.DARK_OAK_LOG, 64));
        list.add(new ItemStack(Material.DARK_OAK_LOG, 64));
        list.add(new ItemStack(Material.COBBLESTONE, 64));
        list.add(new ItemStack(Material.COBBLESTONE, 64));
        list.add(new ItemStack(Material.COBBLESTONE, 64));
        list.add(new ItemStack(Material.COBBLESTONE, 64));
        list.add(new ItemStack(Material.COBBLESTONE, 64));
        list.add(new ItemStack(Material.COBBLESTONE, 64));
        list.add(new ItemStack(Material.STONE, 64));
        list.add(new ItemStack(Material.STONE, 64));
        list.add(new ItemStack(Material.STONE, 64));
        list.add(new ItemStack(Material.STONE, 64));
        list.add(new ItemStack(Material.WHITE_WOOL, 64));
        list.add(new ItemStack(Material.WHITE_WOOL, 64));
        list.add(new ItemStack(Material.RED_WOOL, 64));
        list.add(new ItemStack(Material.BLACK_WOOL, 64));
        list.add(new ItemStack(Material.GRASS_BLOCK, 64));
        list.add(new ItemStack(Material.GRASS_BLOCK, 64));
        list.add(new ItemStack(Material.GRASS_BLOCK, 64));
        list.add(new ItemStack(Material.GRASS_BLOCK, 64));
        list.add(new ItemStack(Material.GRASS_BLOCK, 64));
        return list;
    }

    private List<ItemStack> boxB() {
        List<ItemStack> list = new ArrayList<>();

        ItemStack firework = new ItemBuilder(Material.FIREWORK_ROCKET, 64).build();
        FireworkMeta fireworkMeta = (FireworkMeta) firework.getItemMeta();
        fireworkMeta.setPower(3);
        firework.setItemMeta(fireworkMeta);

        list.add(firework);
        list.add(firework);
        list.add(firework);
        list.add(firework);
        list.add(firework);
        list.add(firework);

        list.add(new ItemStack(Material.REPEATER, 64));
        list.add(new ItemStack(Material.COMPARATOR, 64));
        list.add(new ItemStack(Material.HOPPER, 64));
        list.add(new ItemStack(Material.DROPPER, 64));
        list.add(new ItemStack(Material.OBSERVER, 64));
        list.add(new ItemStack(Material.DISPENSER, 64));
        list.add(new ItemStack(Material.NETHERITE_INGOT, 7));

        ItemStack unbreaking3Book = new ItemBuilder(Material.ENCHANTED_BOOK, 1).build();
        EnchantmentStorageMeta unbreaking3BookMeta = (EnchantmentStorageMeta) unbreaking3Book.getItemMeta();
        unbreaking3BookMeta.addStoredEnchant(Enchantment.DURABILITY, 3, true);
        unbreaking3Book.setItemMeta(unbreaking3BookMeta);

        list.add(unbreaking3Book);
        list.add(unbreaking3Book);
        list.add(unbreaking3Book);
        list.add(unbreaking3Book);

        list.add(new ItemStack(Material.REDSTONE_BLOCK, 64));

        ItemStack efficiency5Book = new ItemBuilder(Material.ENCHANTED_BOOK, 1).build();
        EnchantmentStorageMeta efficiency5BookMeta = (EnchantmentStorageMeta) efficiency5Book.getItemMeta();
        efficiency5BookMeta.addStoredEnchant(Enchantment.DIG_SPEED, 5, true);
        efficiency5Book.setItemMeta(efficiency5BookMeta);

        list.add(efficiency5Book);
        list.add(efficiency5Book);
        list.add(efficiency5Book);
        list.add(efficiency5Book);

        ItemStack mendingBook = new ItemBuilder(Material.ENCHANTED_BOOK, 1).build();
        EnchantmentStorageMeta mendingBookMeta = (EnchantmentStorageMeta) mendingBook.getItemMeta();
        mendingBookMeta.addStoredEnchant(Enchantment.MENDING, 1, true);
        mendingBook.setItemMeta(mendingBookMeta);

        list.add(mendingBook);
        list.add(mendingBook);
        list.add(mendingBook);

        list.add(new ItemStack(Material.BOOKSHELF, 64));
        list.add(new ItemStack(Material.SLIME_BLOCK, 64));
        return list;
    }
}