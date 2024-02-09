package de.voldechse.wintervillage.proxy.wildcard;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import de.voldechse.wintervillage.proxy.Proxy;
import org.bson.Document;
import org.bson.types.Decimal128;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class WhitelistDatabase {

    private final Proxy plugin;
    private final MongoCollection<Document> whitelistCollection;

    public WhitelistDatabase(Proxy plugin) {
        this.plugin = plugin;
        this.whitelistCollection = plugin.database.getCollection("whitelist");
    }

    public void whitelist(UUID receiver, UUID from, LocalDateTime date) {
        Document document = new Document("receiver", receiver.toString())
                .append("from", from.toString())
                .append("granted", date);
        this.whitelistCollection.insertOne(document);

        this.plugin.getInstance().getLogger().info("Saved whitelist entry [receiver=" + receiver + "] [from=" + from + "] [date=" + date.toString() + "]");
    }

    public void remove(UUID receiver) {
        this.whitelistCollection.deleteOne(Filters.eq("receiver", receiver.toString()));

        this.plugin.getInstance().getLogger().info("Deleted whitelist entry [receiver=" + receiver + "]");
    }

    public boolean whitelisted(UUID uuid) {
        Document document = this.whitelistCollection.find(Filters.eq("receiver", uuid.toString())).first();
        return document != null;
    }

    public List<WhitelistData> dataList(UUID from) {
        List<WhitelistData> list = new ArrayList<>();

        Document query = new Document("from", from.toString());
        for (Document result : this.whitelistCollection.find(query)) {
            WhitelistData whitelistData = data(result);
            list.add(whitelistData);
        }

        return list;
    }

    public WhitelistData data(UUID receiver) {
        Document document = this.whitelistCollection.find(Filters.eq("receiver", receiver.toString())).first();
        if (document == null) return null;

        Date date = document.getDate("granted");
        LocalDateTime granted = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());

        return new WhitelistData(receiver, UUID.fromString(document.getString("from")), granted);
    }

    public List<UUID> list() {
        List<UUID> wildcarders = new ArrayList<>();

        this.whitelistCollection.find().forEach(document -> {
            wildcarders.add(UUID.fromString(document.getString("receiver")));
        });

        return wildcarders;
    }

    private WhitelistData data(Document document) {
        return new WhitelistData(
                UUID.fromString(document.getString("receiver")),
                UUID.fromString(document.getString("from")),
                LocalDateTime.ofInstant(document.getDate("granted").toInstant(), ZoneId.systemDefault())
        );
    }

    public class WhitelistData {

        public UUID receiver, from;
        public LocalDateTime date;

        public WhitelistData(UUID receiver, UUID from, LocalDateTime date) {
            this.receiver = receiver;
            this.from = from;
            this.date = date;
        }
    }

}