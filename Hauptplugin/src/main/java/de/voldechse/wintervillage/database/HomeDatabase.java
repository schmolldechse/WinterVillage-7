package de.voldechse.wintervillage.database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import de.voldechse.wintervillage.WinterVillage;
import de.voldechse.wintervillage.library.util.ItemBuilder;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.sign.Side;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.*;

public class HomeDatabase {

    private final WinterVillage plugin;
    private final MongoCollection<Document> homeCollection;

    public HomeDatabase(WinterVillage plugin) {
        this.plugin = plugin;
        this.homeCollection = plugin.database.getCollection("homes");
    }

    public void save(UUID uuid, Location location) {
        Document document = new Document("uuid", uuid)
                .append("location", serialize(location));

        this.homeCollection.insertOne(document);

        this.plugin.getInstance().getLogger().info("Saved home entry [uuid=" + uuid + "]");
    }

    public void remove(UUID uuid) {
        this.homeCollection.deleteOne(Filters.eq("uuid", uuid));

        this.plugin.getInstance().getLogger().info("Deleted home entry [uuid=" + uuid + "]");
    }

    public boolean saved(UUID uuid) {
        Document query = new Document("uuid", uuid);
        return this.homeCollection.find(query).first() != null;
    }

    public Location home(UUID uuid) {
        Document query = new Document("uuid", uuid);
        Document result = this.homeCollection.find(query).first();

        if (result == null) return null;

        return deserializeLocation(result.get("location", Document.class));
    }

    private Document serialize(Location location) {
        return new Document("x", location.getX())
                .append("y", location.getY())
                .append("z", location.getZ())
                .append("yaw", (double) location.getYaw())
                .append("pitch", (double) location.getPitch())
                .append("world", location.getWorld().getName());
    }

    private Location deserializeLocation(Document document) {
        double x = document.getDouble("x");
        double y = document.getDouble("y");
        double z = document.getDouble("z");

        double yaw = document.getDouble("yaw");
        double pitch = document.getDouble("pitch");

        String world = document.getString("world");

        return new Location(Bukkit.getWorld(world), x, y, z, (float) yaw, (float) pitch);
    }
}