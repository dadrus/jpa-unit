package eu.drus.jpa.unit.mongodb;

import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class DeleteOperation extends AbstractDbOperation {

    @Override
    public void execute(final MongoDatabase connection, final Document data) {
        for (final String collectionName : data.keySet()) {
            final MongoCollection<Document> collection = connection.getCollection(collectionName);
            final List<Document> entry = data.get(collectionName, List.class);
            entry.forEach(collection::deleteOne);
        }
    }

}
