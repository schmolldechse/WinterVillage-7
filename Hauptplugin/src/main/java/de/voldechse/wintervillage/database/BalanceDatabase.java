package de.voldechse.wintervillage.database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import de.voldechse.wintervillage.WinterVillage;
import org.bson.Document;
import org.bson.types.Decimal128;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class BalanceDatabase {

    private final WinterVillage plugin;
    private final MongoCollection<Document> balanceCollection;

    public BalanceDatabase(WinterVillage plugin) {
        this.plugin = plugin;
        this.balanceCollection = plugin.database.getCollection("balance");
    }

    public void save(UUID uuid) {
        Document document = new Document("uuid", uuid.toString())
                .append("balance", new Decimal128(new BigDecimal("0")))
                .append("last_daily_reward", System.currentTimeMillis());
        this.balanceCollection.insertOne(document);

        this.plugin.getInstance().getLogger().info("Saved balance entry [uuid=" + uuid + "]");
    }

    public void remove(UUID uuid) {
        this.balanceCollection.deleteOne(Filters.eq("uuid", uuid.toString()));

        this.plugin.getInstance().getLogger().info("Deleted balance entry [uuid=" + uuid + "]");
    }

    public boolean saved(UUID uuid) {
        Document document = this.balanceCollection.find(Filters.eq("uuid", uuid.toString())).first();
        return document != null;
    }


    public BigDecimal balance(UUID uuid) {
        Document document = this.balanceCollection.find(Filters.eq("uuid", uuid.toString())).first();
        if (document == null) return BigDecimal.ZERO;

        Decimal128 retrievedBalance = document.get("balance", Decimal128.class);
        return retrievedBalance.bigDecimalValue();
    }

    public void modify(UUID uuid, BigDecimal bigDecimal) {
        this.balanceCollection.updateOne(Filters.eq("uuid", uuid.toString()),
                new Document("$inc", new Document("balance", new Decimal128(bigDecimal))));

        this.plugin.getInstance().getLogger().info("Modified balance entry [uuid=" + uuid + "] [balance=" + bigDecimal + "]");
    }

    public void modify(UUID uuid, long millis) {
        this.balanceCollection.updateOne(Filters.eq("uuid", uuid.toString()),
                new Document("$set", new Document("last_daily_reward", millis)));

        this.plugin.getInstance().getLogger().info("Modified balance entry [uuid=" + uuid + "] [last_daily_reward=" + millis + "]");
    }

    public boolean canReceive(UUID uuid) {
        Document document = this.balanceCollection.find(new Document("uuid", uuid.toString())).first();
        if (document == null) return true;

        long last = document.getLong("last_daily_reward");
        long currentTime = System.currentTimeMillis();

        if (!sameDay(last, currentTime)) return true;

        return false;
    }

    private boolean sameDay(long var0, long var1) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        return dateFormat.format(new Date(var0)).equals(dateFormat.format(new Date(var1)));
    }
}
