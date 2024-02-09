package de.voldechse.wintervillage.database;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import de.voldechse.wintervillage.WinterVillage;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Decimal128;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class TransactionsDatabase {

    private final WinterVillage plugin;
    private final MongoCollection<Document> transferCollection;

    public TransactionsDatabase(WinterVillage plugin) {
        this.plugin = plugin;
        this.transferCollection = plugin.database.getCollection("transactions");
    }

    public void save(TransactionData transactionData) {
        Document document = new Document("from", transactionData.from.toString())
                .append("to", transactionData.to.toString())
                .append("amount", transactionData.amount)
                .append("date", transactionData.date);

        this.transferCollection.insertOne(document);

        this.plugin.getInstance().getLogger().info("Saved transaction entry [from=" + transactionData.from + "] [to=" + transactionData.to + "]");
    }

    public List<TransactionData> data(UUID uuid) {
        List<TransactionData> list = new ArrayList<>();

        Bson filter = Filters.or(Filters.eq("from", uuid.toString()), Filters.eq("to", uuid.toString()));
        FindIterable<Document> documents = this.transferCollection.find(filter);

        documents.forEach(document -> {
            Decimal128 amount = document.get("amount", Decimal128.class);

            Date date = document.getDate("date");
            LocalDateTime transactionDate = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());

            UUID from = UUID.fromString(document.getString("from"));
            UUID to = UUID.fromString(document.getString("to"));

            TransactionData retrieved = new TransactionData(
                    from,
                    to,
                    amount.bigDecimalValue(),
                    transactionDate
            );

            list.add(retrieved);
        });

        return list;
    }

    public static class TransactionData {

        public UUID from, to;
        public BigDecimal amount;
        public LocalDateTime date;

        public TransactionData
                (
                        UUID from,
                        UUID to,
                        BigDecimal amount,
                        LocalDateTime date
                ) {
            this.from = from;
            this.to = to;
            this.amount = amount;
            this.date = date;
        }
    }
}