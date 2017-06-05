package eu.drus.jpa.unit.mongodb;

import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.UpdateResult;

public class RefreshOperation extends AbstractDbOperation {

    @Override
    public void execute(final MongoDatabase connection, final Document data) {
        for (final String collectionName : data.keySet()) {
            final MongoCollection<Document> collection = connection.getCollection(collectionName);

            final List<Document> documents = data.get(collectionName, List.class);

            for (final Document doc : documents) {
                final UpdateResult result = collection.replaceOne(Filters.eq(doc.get("_id")), doc);

                if (result.getMatchedCount() == 0) {
                    collection.insertOne(doc);
                }
            }
        }
    }

}
