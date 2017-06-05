package eu.drus.jpa.unit.mongodb.operation;

import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class UpdateOperation implements MongoDbOperation {

    @Override
    public void execute(final MongoDatabase connection, final Document data) {
        for (final String collectionName : data.keySet()) {
            final MongoCollection<Document> collection = connection.getCollection(collectionName);

            final List<Document> documents = data.get(collectionName, List.class);
            documents.forEach(d -> collection.replaceOne(Filters.eq(d.get("_id")), d));
        }
    }

}
