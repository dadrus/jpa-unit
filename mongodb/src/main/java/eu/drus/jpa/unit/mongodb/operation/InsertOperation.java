package eu.drus.jpa.unit.mongodb.operation;

import static eu.drus.jpa.unit.mongodb.operation.IndexOptionsUtils.toIndexOptions;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class InsertOperation implements MongoDbOperation {

    @Override
    public void execute(final MongoDatabase connection, final Document data) {
        for (final Entry<String, Object> entry : data.entrySet()) {
            final String collectionName = entry.getKey();
            final Object content = entry.getValue();

            final List<Document> indexes = getIndexData(content);
            if (!indexes.isEmpty()) {
                insertIndexes(connection.getCollection(collectionName), indexes);
            }

            final List<Document> entries = getCollectionData(content);
            if (!entries.isEmpty()) {
                insertData(connection.getCollection(collectionName), entries);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<Document> getCollectionData(final Object obj) {
        if (List.class.isAssignableFrom(obj.getClass())) {
            return (List<Document>) obj;
        } else {
            return ((Document) obj).get("data", List.class);
        }
    }

    private void insertData(final MongoCollection<Document> collection, final List<Document> entries) {
        collection.insertMany(entries);
    }

    @SuppressWarnings("unchecked")
    private List<Document> getIndexData(final Object obj) {
        if (Document.class.isAssignableFrom(obj.getClass())) {
            final Document collection = (Document) obj;
            if (collection.containsKey("indexes")) {
                return collection.get("indexes", List.class);
            }
        }
        return Collections.emptyList();
    }

    private void insertIndexes(final MongoCollection<Document> collection, final List<Document> indexes) {
        for (final Document index : indexes) {

            final Document indexKeys = index.get("index", Document.class);

            if (index.containsKey("options")) {
                final Document indexOptions = index.get("options", Document.class);
                collection.createIndex(indexKeys, toIndexOptions(indexOptions));
            } else {
                collection.createIndex(indexKeys);
            }
        }
    }
}
