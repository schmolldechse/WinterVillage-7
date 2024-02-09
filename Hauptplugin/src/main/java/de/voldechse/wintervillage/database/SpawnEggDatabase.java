package de.voldechse.wintervillage.database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import de.voldechse.wintervillage.WinterVillage;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class SpawnEggDatabase {

    private final WinterVillage plugin;
    private final MongoCollection<Document> spawnEggsCollection;

    public SpawnEggDatabase(WinterVillage plugin) {
        this.plugin = plugin;
        this.spawnEggsCollection = plugin.database.getCollection("spawn_eggs");
    }

    public void save(EntityData entityData) {
        Document document = new Document("entity_type", entityData.type.name().toUpperCase())
                .append("probability", entityData.probability);
        this.spawnEggsCollection.insertOne(document);

        this.plugin.getInstance().getLogger().info("Saved entity type entry [entity=" + entityData.type.name().toUpperCase() + "] [probability=" + entityData.probability + " %]");
    }

    public void remove(EntityType entityType) {
        this.spawnEggsCollection.deleteOne(Filters.eq("entity_type", entityType.name().toUpperCase()));

        this.plugin.getInstance().getLogger().info("Deleted entity type entry [entity=" + entityType.name().toUpperCase() + "]");
    }

    public boolean isSaved(EntityType entityType) {
        Document document = this.spawnEggsCollection.find(Filters.eq("entity_type", entityType.name().toUpperCase())).first();
        return document != null;
    }

    public List<EntityData> list() {
        List<EntityData> list = new ArrayList<>();

        for (Document document : this.spawnEggsCollection.find()) {
            EntityType entityType = EntityType.valueOf(document.getString("entity_type"));
            double probability = document.getDouble("probability");

            list.add(new EntityData(entityType, probability));
        }

        return list;
    }

    public double getProbability(EntityType entityType) {
        Document document = this.spawnEggsCollection.find(Filters.eq("entity_type", entityType.name().toUpperCase())).first();
        if (document == null) return 0.0;

        return document.getDouble("probability");
    }

    public void modify(EntityType entityType, double probability) {
        this.spawnEggsCollection.updateOne(Filters.eq("entity_type", entityType.name().toUpperCase()),
                new Document("$set", new Document("probability", probability)));

        this.plugin.getInstance().getLogger().info("Modified entity type entry [entity=" + entityType.name().toUpperCase() + "] [probability=" + probability + " %]");
    }

    public static class EntityData {

        public EntityType type;
        public double probability;

        public EntityData(EntityType type, double probability) {
            this.type = type;
            this.probability = probability;
        }
    }
}
