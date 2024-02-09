package de.voldechse.wintervillage.proxy.wildcard;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import de.voldechse.wintervillage.proxy.Proxy;
import eu.cloudnetservice.common.concurrent.Task;
import org.bson.Document;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BanDatabase {

    private final Proxy plugin;
    private final MongoCollection<Document> bansDatabase;

    public BanDatabase(Proxy plugin) {
        this.plugin = plugin;
        this.bansDatabase = this.plugin.database.getCollection("bans");
    }

    public void ban(UUID uuid) {
        Document document = new Document("uuid", uuid.toString());
        this.bansDatabase.insertOne(document);

        this.plugin.getInstance().getLogger().info("Saved ban entry [uuid=" + uuid + "]");
    }

    public void remove(UUID uuid) {
        this.bansDatabase.deleteOne(Filters.eq("uuid", uuid.toString()));

        this.plugin.getInstance().getLogger().info("Deleted ban entry [uuid=" + uuid + "]");
    }

    public boolean banned(UUID uuid) {
        Document document = this.bansDatabase.find(Filters.eq("uuid", uuid.toString())).first();
        return document != null;
    }
}