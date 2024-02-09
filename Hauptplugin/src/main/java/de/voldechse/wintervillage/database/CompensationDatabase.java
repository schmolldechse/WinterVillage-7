package de.voldechse.wintervillage.database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import de.voldechse.wintervillage.WinterVillage;
import org.bson.Document;

import java.util.UUID;

public class CompensationDatabase {

    private final WinterVillage plugin;
    private final MongoCollection<Document> compensationCollection;

    public CompensationDatabase(WinterVillage plugin) {
        this.plugin = plugin;
        this.compensationCollection = plugin.database.getCollection("compensation");
    }

    public void save(UUID uuid) {
        Document document = new Document("uuid", uuid.toString())
                .append("collected", false);
        this.compensationCollection.insertOne(document);

        this.plugin.getInstance().getLogger().info("Saved compensation entry [uuid=" + uuid + "]");
    }

    public void remove(UUID uuid) {
        this.compensationCollection.deleteOne(Filters.eq("uuid", uuid.toString()));

        this.plugin.getInstance().getLogger().info("Deleted compensation entry [uuid=" + uuid + "]");
    }

    public boolean saved(UUID uuid) {
        Document document = this.compensationCollection.find(Filters.eq("uuid", uuid.toString())).first();
        return document != null;
    }

    public boolean collected(UUID uuid) {
        Document document = this.compensationCollection.find(Filters.eq("uuid", uuid.toString())).first();
        if (document == null) return true;

        return document.getBoolean("collected");
    }

    public void modify(UUID uuid, boolean collected) {
        this.compensationCollection.updateOne(Filters.eq("uuid", uuid.toString()),
                new Document("$set", new Document("collected", collected)));

        this.plugin.getInstance().getLogger().info("Modified compensation entry [uuid=" + uuid + "] [collected=" + collected + "]");
    }
}
