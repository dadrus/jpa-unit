package eu.drus.jpa.unit.mongodb.operation;

import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class DeleteOperation implements MongoDbOperation {

    @Override
    public void execute(final MongoDatabase connection, final Document data) {
        for (final String collectionName : data.keySet()) {
            @SuppressWarnings("unchecked")
            final List<Document> entry = data.get(collectionName, List.class);

            final MongoCollection<Document> collection = connection.getCollection(collectionName);
            entry.forEach(collection::deleteOne);
        }
    }

}
