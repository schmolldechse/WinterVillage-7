package de.voldechse.wintervillage.database;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import de.voldechse.wintervillage.WinterVillage;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DeathsDatabase {

    private final WinterVillage plugin;
    private final MongoCollection<Document> deathsCollection;

    public DeathsDatabase(WinterVillage plugin) {
        this.plugin = plugin;
        this.deathsCollection = plugin.database.getCollection("deaths");
    }

    public void save(UUID uuid) {
        Document document = new Document("uuid", uuid.toString())
                .append("amount", 0);
        this.deathsCollection.insertOne(document);

        this.plugin.getInstance().getLogger().info("Saved death entry [uuid=" + uuid + "]");
    }

    public void remove(UUID uuid) {
        this.deathsCollection.deleteOne(Filters.eq("uuid", uuid.toString()));

        this.plugin.getInstance().getLogger().info("Deleted death entry [uuid=" + uuid + "]");
    }

    public boolean isSaved(UUID uuid) {
        Document document = this.deathsCollection.find(Filters.eq("uuid", uuid.toString())).first();
        return document != null;
    }

    public int getDeaths(UUID uuid) {
        Document document = this.deathsCollection.find(Filters.eq("uuid", uuid.toString())).first();
        if (document == null) return 0;

        return document.getInteger("amount");
    }

    public void modify(UUID uuid, int amount) {
        this.deathsCollection.updateOne(Filters.eq("uuid", uuid.toString()),
                new Document("$inc", new Document("amount", amount)));

        this.plugin.getInstance().getLogger().info("Modified death entry [uuid=" + uuid + "] [deaths=" + amount + "]");
    }

    public List<DeathsData> top(int limit) {
        List<DeathsData> list = new ArrayList<>();

        FindIterable<Document> result = this.deathsCollection.find()
                .sort(Sorts.descending("amount"))
                .limit(limit);

        for (Document document : result)
            list.add(new DeathsData(
                    UUID.fromString(document.getString("uuid")),
                    document.getInteger("amount")
            ));

        return list;
    }

    public static class DeathsData implements Comparable<DeathsData> {

        public UUID uuid;
        public int deaths;

        public DeathsData(UUID uuid, int deaths) {
            this.uuid = uuid;
            this.deaths = deaths;
        }

        @Override
        public int compareTo(@NotNull DeathsDatabase.DeathsData o) {
            return Integer.compare(this.deaths, o.deaths);
        }

        @Override
        public String toString() {
            return "DeathData{" +
                    "uuid=" + uuid +
                    ",deaths=" + deaths +
                    "}";
        }
    }
}