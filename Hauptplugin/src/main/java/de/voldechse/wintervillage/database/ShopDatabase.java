package de.voldechse.wintervillage.database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import de.voldechse.wintervillage.WinterVillage;
import de.voldechse.wintervillage.library.util.ItemBuilder;
import net.minecraft.util.datafix.fixes.ItemStackSpawnEggFix;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.sign.Side;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.*;

public class ShopDatabase {

    private final WinterVillage plugin;
    private final MongoCollection<Document> playerShopCollection, adminShopCollection;

    public ShopDatabase(WinterVillage plugin) {
        this.plugin = plugin;
        this.playerShopCollection = plugin.database.getCollection("player_shop");
        this.adminShopCollection = plugin.database.getCollection("admin_shop");
    }

    public void save(PlayerShop playerShop) {
        Document document = new Document("shop_identifier", playerShop.shopIdentifier.toString())
                .append("owner", playerShop.owner.toString())
                .append("item_display", playerShop.itemDisplay.toString())
                .append("location", serialize(playerShop.location))
                .append("side", playerShop.side.name().toUpperCase())
                .append("material", playerShop.material.name().toUpperCase())
                .append("price", playerShop.price)
                .append("amount_per_price", playerShop.amountPerPrice)
                .append("current_amount", playerShop.currentAmount)
                .append("editing", playerShop.editing);

        this.playerShopCollection.insertOne(document);

        this.plugin.getInstance().getLogger().info("Saved player's shop entry [owner=" + playerShop.owner + "]");
    }

    public void removePlayerShop(UUID identifierId) {
        this.playerShopCollection.deleteOne(Filters.eq("shop_identifier", identifierId.toString()));

        this.plugin.getInstance().getLogger().info("Deleted player's shop entry [shop_identifier=" + identifierId + "]");
    }

    public void save(AdminShopItem adminShopItem) {
        Document document = new Document("item", serialize(adminShopItem.itemStack))
                .append("item_identifier", adminShopItem.identifierId.toString())
                .append("price", adminShopItem.price)
                .append("amount", adminShopItem.amount);

        this.adminShopCollection.insertOne(document);

        this.plugin.getInstance().getLogger().info("Saved admin shop's entry [item=" + adminShopItem.itemStack.getType().name().toUpperCase() + "]");
    }

    public void removeAdminShopItem(UUID identifierId) {
        this.adminShopCollection.deleteOne(Filters.eq("item_identifier", identifierId.toString()));

        this.plugin.getInstance().getLogger().info("Deleted admin shop's entry [shop_identifier=" + identifierId + "]");
    }

    public boolean isSavedPlayerShop(Location location) {
        Document query = new Document("location", serialize(location));
        return this.playerShopCollection.find(query).first() != null;
    }

    public boolean isSavedPlayerShop(UUID identifierId) {
        Document query = new Document("shop_identifier", identifierId.toString());
        return this.playerShopCollection.find(query).first() != null;
    }

    public boolean isSavedAdminShopItem(UUID identifierId) {
        Document query = new Document("item_identifier", identifierId.toString());
        return this.adminShopCollection.find(query).first() != null;
    }

    public void owner(UUID identifierId, UUID owner) {
        this.playerShopCollection.updateOne(Filters.eq("shop_identifier", identifierId.toString()),
                new Document("$set", new Document("owner", owner.toString())));

        this.plugin.getInstance().getLogger().info("Changed player's shop entry [identifierId=" + identifierId + "] [owner=" + owner + "]");
    }

    public void amount(UUID identifierId, BigDecimal amount) {
        this.playerShopCollection.updateOne(Filters.eq("shop_identifier", identifierId.toString()),
                new Document("$set", new Document("current_amount", amount)));

        this.plugin.getInstance().getLogger().info("Modified player's shop entry [identifierId=" + identifierId + "] [amount=" + amount + "]");
    }

    public void editing(UUID identifierId, boolean editing) {
        this.playerShopCollection.updateOne(Filters.eq("shop_identifier", identifierId.toString()),
                new Document("$set", new Document("editing", editing)));

        this.plugin.getInstance().getLogger().info("Modified player's shop entry [identifierId=" + identifierId + "] [editing=" + editing + "]");
    }

    public AdminShopItem item(UUID identifierId) {
        Document query = new Document("item_identifier", identifierId.toString());
        Document result = this.adminShopCollection.find(query).first();

        if (result == null) {
            this.plugin.getInstance().getLogger().severe("Could not find item identifier id [" + identifierId.toString() + "]");
            return null;
        }

        return toItem(result);
    }

    public PlayerShop shop(UUID identifierId) {
        Document query = new Document("shop_identifier", identifierId.toString());
        Document result = this.playerShopCollection.find(query).first();

        if (result == null) {
            this.plugin.getInstance().getLogger().severe("Could not find shop identifier id [" + identifierId.toString() + "]");
            return null;
        }

        return toPlayerShop(result);
    }

    public PlayerShop shop(Location location) {
        Document query = new Document("location", serialize(location));
        Document result = this.playerShopCollection.find(query).first();

        if (result == null) {
            this.plugin.getInstance().getLogger().severe("Could not find player shop [location=" + location.toString() + "]");
            return null;
        }

        return toPlayerShop(result);
    }

    public List<PlayerShop> shops(UUID owner) {
        List<PlayerShop> list = new ArrayList<>();

        Document query = new Document("owner", owner.toString());
        for (Document result : this.playerShopCollection.find(query)) {
            PlayerShop playerShop = toPlayerShop(result);
            list.add(playerShop);
        }

        return list;
    }

    public List<AdminShopItem> items() {
        List<AdminShopItem> list = new ArrayList<>();

        for (Document document : this.adminShopCollection.find()) {
            Document item = document.get("item", Document.class);
            ItemStack itemStack = deserializeItem(item);

            BigDecimal price = document.get("price", Decimal128.class).bigDecimalValue();
            BigDecimal amount = document.get("amount", Decimal128.class).bigDecimalValue();

            list.add(new AdminShopItem(itemStack, UUID.fromString(document.getString("item_identifier")), price, amount));
        }

        return list;
    }

    private AdminShopItem toItem(Document document) {
        return new AdminShopItem(
                deserializeItem(document.get("item", Document.class)),
                UUID.fromString(document.getString("item_identifier")),
                document.get("price", Decimal128.class).bigDecimalValue(),
                document.get("amount", Decimal128.class).bigDecimalValue()
        );
    }

    private PlayerShop toPlayerShop(Document document) {
        return new PlayerShop(
                UUID.fromString(document.getString("shop_identifier")),
                UUID.fromString(document.getString("owner")),
                UUID.fromString(document.getString("item_display")),
                deserializeLocation(document.get("location", Document.class)),
                Side.valueOf(document.getString("side")),
                Material.valueOf(document.getString("material")),
                document.get("price", Decimal128.class).bigDecimalValue(),
                document.get("amount_per_price", Decimal128.class).bigDecimalValue(),
                document.get("current_amount", Decimal128.class).bigDecimalValue(),
                document.getBoolean("editing")
        );
    }

    private Document serialize(Location location) {
        return new Document("x", location.getX())
                .append("y", location.getY())
                .append("z", location.getZ())
                .append("world", location.getWorld().getName());
    }

    private Location deserializeLocation(Document document) {
        double x = document.getDouble("x");
        double y = document.getDouble("y");
        double z = document.getDouble("z");
        String world = document.getString("world");

        return new Location(Bukkit.getWorld(world), x, y, z);
    }

    private Document serialize(ItemStack itemStack) {
        Document enchantmentDocument = new Document();
        if (!itemStack.getEnchantments().isEmpty())
            itemStack.getEnchantments().forEach((enchantment, integer) -> enchantmentDocument.append(enchantment.getKey().getKey(), integer));

        Set<String> strings = new HashSet<>();
        itemStack.getItemMeta().getItemFlags().forEach(itemFlag -> strings.add(itemFlag.name()));

        return new Document("material", itemStack.getType().name().toUpperCase())
                .append("amount", itemStack.getAmount())
                .append("display_name", itemStack.getItemMeta().getDisplayName())
                .append("lore", itemStack.getItemMeta().getLore())
                .append("enchantments", enchantmentDocument)
                .append("item_flags", strings)
                .append("unbreakable", itemStack.getItemMeta().isUnbreakable());
    }

    private ItemStack deserializeItem(Document itemStack) {
        Material material = Material.valueOf(itemStack.getString("material"));

        int amount = itemStack.getInteger("amount");

        String displayName = itemStack.getString("display_name");
        List<String> lore = (List<String>) itemStack.get("lore");

        Document enchantmentDocument = itemStack.get("enchantments", Document.class);
        Map<Enchantment, Integer> enchantmentMap = new HashMap<>();
        if (enchantmentDocument != null) {
            for (String enchantmentKey : enchantmentDocument.keySet()) {
                Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantmentKey));
                int level = enchantmentDocument.getInteger(enchantmentKey);
                enchantmentMap.put(enchantment, level);
            }
        }

        List<ItemFlag> itemFlags = new ArrayList<>();
        List<String> strings = (List<String>) itemStack.get("item_flags");
        if (strings != null) {
            strings.forEach(s -> {
                ItemFlag itemFlag = ItemFlag.valueOf(String.valueOf(s));
                itemFlags.add(itemFlag);
            });
        }

        boolean unbreakable = itemStack.getBoolean("unbreakable");

        ItemBuilder itemBuilder = new ItemBuilder(material, amount, displayName);
        if (lore != null) itemBuilder.lore(lore);
        if (!enchantmentMap.isEmpty()) itemBuilder.enchant(enchantmentMap);
        if (!itemFlags.isEmpty()) itemBuilder.flag(itemFlags);
        itemBuilder.unbreakable(unbreakable);

        return itemBuilder.build();
    }

    public static class PlayerShop {

        public UUID shopIdentifier, owner, itemDisplay;
        public Location location;
        public Side side;
        public Material material;
        public BigDecimal price, amountPerPrice, currentAmount;
        public boolean editing;

        public PlayerShop(
                UUID shopIdentifier,
                UUID owner,
                UUID itemDisplay,
                Location location,
                Side side,
                Material material,
                BigDecimal price,
                BigDecimal amountPerPrice,
                BigDecimal currentAmount,
                boolean editing
        ) {
            this.shopIdentifier = shopIdentifier;
            this.owner = owner;
            this.itemDisplay = itemDisplay;
            this.location = location;
            this.side = side;
            this.material = material;
            this.price = price;
            this.amountPerPrice = amountPerPrice;
            this.currentAmount = currentAmount;
            this.editing = editing;
        }

        @Override
        public String toString() {
            return "PlayerShop{" +
                    "shopIdentifier=" + shopIdentifier +
                    ", owner=" + owner +
                    ", itemDisplay=" + itemDisplay +
                    ", side=" + side +
                    ", location=" + location +
                    ", material=" + material +
                    ", price=" + price +
                    ", amountPerPrice=" + amountPerPrice +
                    ", currentAmount=" + currentAmount +
                    "}";
        }
    }

    public static class AdminShopItem {

        public ItemStack itemStack;
        public UUID identifierId;
        public BigDecimal price;
        public BigDecimal amount;

        public AdminShopItem(
                ItemStack itemStack,
                UUID identifierId,
                BigDecimal price,
                BigDecimal amount
        ) {
            this.itemStack = itemStack;
            this.identifierId = identifierId;
            this.price = price;
            this.amount = amount;
        }

        @Override
        public String toString() {
            return "AdminShopItem{" +
                    "itemStack=" + itemStack +
                    ", identifierId=" + identifierId +
                    ", price=" + price +
                    ", amount=" + amount +
                    '}';
        }
    }
}