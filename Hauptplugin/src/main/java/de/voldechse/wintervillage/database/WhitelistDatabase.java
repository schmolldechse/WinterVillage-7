package de.voldechse.wintervillage.database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import de.voldechse.wintervillage.WinterVillage;
import org.bson.Document;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class WhitelistDatabase {

    private final WinterVillage plugin;
    private final MongoCollection<Document> whitelistCollection;

    public WhitelistDatabase(WinterVillage plugin) {
        this.plugin = plugin;
        this.whitelistCollection = plugin.database.getCollection("whitelist");
    }

    public boolean whitelisted(UUID uuid) {
        Document document = this.whitelistCollection.find(Filters.eq("receiver", uuid.toString())).first();
        return document != null;
    }

    public WhitelistData data(UUID uuid) {
        Document document = this.whitelistCollection.find(Filters.eq("receiver", uuid.toString())).first();
        if (document == null) return null;

        Date date = document.getDate("granted");
        LocalDateTime granted = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());

        return new WhitelistData(uuid, UUID.fromString(document.getString("from")), granted);
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