package de.voldechse.wintervillage.proxy.wildcard;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import de.voldechse.wintervillage.proxy.Proxy;
import org.bson.Document;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class WildcardDatabase {

    private final Proxy plugin;

    private final MongoCollection<Document> wildcardsCollection;

    public WildcardDatabase(Proxy plugin) {
        this.plugin = plugin;
        this.wildcardsCollection = plugin.database.getCollection("wildcards");
    }

    public void save(UUID uuid, LocalDateTime date) {
        Document document = new Document("uuid", uuid.toString())
                .append("amount", 3)
                .append("last_reset", date);
        this.wildcardsCollection.insertOne(document);

        this.plugin.getInstance().getLogger().info("Saved wildcard entry [uuid=" + uuid + "]");
    }

    public List<UUID> list() {
        List<UUID> wildcarders = new ArrayList<>();

        this.wildcardsCollection.find().forEach(document -> {
            wildcarders.add(UUID.fromString(document.getString("uuid")));
        });

        return wildcarders;
    }

    public WildcardData data(UUID uuid) {
        Document document = this.wildcardsCollection.find(Filters.eq("uuid", uuid.toString())).first();
        if (document != null) return documentToWildcard(document);
        return null;
    }

    public int amount(UUID uuid) {
        Document document = this.wildcardsCollection.find(Filters.eq("uuid", uuid.toString())).first();
        if (document != null) return document.getInteger("amount", 0);
        return 0;
    }

    public void amount(UUID uuid, int amount) {
        this.wildcardsCollection.updateOne(Filters.eq("uuid", uuid.toString()),
                new Document("$set", new Document("amount", amount)));

        this.plugin.getInstance().getLogger().info("Modified wildcard entry [uuid=" + uuid + "] [amount=" + amount + "]");
    }

    public void amount(int amount) {
        this.wildcardsCollection.updateMany(new Document(),
                new Document("$set", new Document("amount", amount)));

        this.plugin.getInstance().getLogger().info("Modified wildcard entry [amount=" + amount + "]");
    }

    public void modify(UUID uuid, int delta) {
        this.wildcardsCollection.updateOne(Filters.eq("uuid", uuid.toString()),
                new Document("$inc", new Document("amount", delta)));

        this.plugin.getInstance().getLogger().info("Modified wildcard entry [uuid=" + uuid + "] [amount=" + delta + "]");
    }

    public void modify(int delta) {
        //this.collection.updateMany(new Document(), new Document("$inc", new Document("amount", delta)));
        this.wildcardsCollection.updateMany(new Document(), new Document("$inc", new Document("amount", delta)));

        this.plugin.getInstance().getLogger().info("Modified wildcard entry [amount=" + delta + "]");
    }

    public void reset(UUID uuid, LocalDateTime dateTime) {
        this.wildcardsCollection.updateOne(Filters.eq("uuid", uuid.toString()), new Document("$set", new Document("last_reset", dateTime)));

        this.plugin.getInstance().getLogger().info("Modified wildcard entry [uuid=" + uuid + "] [last_reset=" + dateTime + "]");
    }

    public void reset(LocalDateTime dateTime) {
        this.wildcardsCollection.updateMany(new Document(), new Document("$set", new Document("last_reset", dateTime)));

        this.plugin.getInstance().getLogger().info("Modified wildcard entry [last_reset=" + dateTime + "]");
    }

    public void reset(UUID uuid) {
        this.wildcardsCollection.updateOne(Filters.eq("uuid", uuid.toString()), Updates.set("amount", 3));

        this.plugin.getInstance().getLogger().info("Modified wildcard entry [uuid=" + uuid + "] [amount=3]");
    }

    public void reset() {
        this.wildcardsCollection.updateMany(new Document(), Updates.set("amount", 3));

        this.plugin.getInstance().getLogger().info("Modified wildcard entry [amount=3]");
    }

    public void delete(UUID uuid) {
        this.wildcardsCollection.deleteOne(Filters.eq("uuid", uuid.toString()));

        this.plugin.getInstance().getLogger().info("Deleted wildcard entry [uuid=" + uuid + "]");
    }

    public boolean saved(UUID uuid) {
        Document document = this.wildcardsCollection.find(Filters.eq("uuid", uuid.toString())).first();
        return document != null;
    }

    private WildcardData documentToWildcard(Document document) {
        UUID uuid = UUID.fromString(document.getString("uuid"));
        int amount = document.getInteger("amount", 3);

        Date date = document.getDate("last_reset");

        LocalDateTime lastReset = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());

        return new WildcardData(uuid, amount, lastReset);
    }

    public class WildcardData {

        public UUID uuid;
        public int amount;
        public LocalDateTime lastReset;

        public WildcardData(UUID uuid, int amount, LocalDateTime lastReset) {
            this.uuid = uuid;
            this.amount = amount;
            this.lastReset = lastReset;
        }
    }
}