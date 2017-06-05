package eu.drus.jpa.unit.mongodb;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.bson.Document;

import com.mongodb.client.MongoDatabase;

import eu.drus.jpa.unit.api.CleanupStrategy.StrategyProvider;
import eu.drus.jpa.unit.core.CleanupStrategyExecutor;
import eu.drus.jpa.unit.mongodb.operation.MongoDbOperations;

public class CleanupStrategyProvider implements StrategyProvider<CleanupStrategyExecutor<MongoDatabase, Document>> {

    @Override
    public CleanupStrategyExecutor<MongoDatabase, Document> strictStrategy() {
        return (final MongoDatabase connection, final List<Document> initialCollections, final String... collectionsToExclude) -> {
            final Document toDelete = excludeCollections(connection.listCollections(), collectionsToExclude);

            MongoDbOperations.DELETE_ALL.execute(connection, toDelete);
        };
    }

    @Override
    public CleanupStrategyExecutor<MongoDatabase, Document> usedTablesOnlyStrategy() {
        return (final MongoDatabase connection, final List<Document> initialCollections, final String... collectionsToExclude) -> {
            if (initialCollections.isEmpty()) {
                return;
            }

            final Document toDelete = excludeCollections(initialCollections, collectionsToExclude);

            MongoDbOperations.DELETE_ALL.execute(connection, toDelete);
        };
    }

    @Override
    public CleanupStrategyExecutor<MongoDatabase, Document> usedRowsOnlyStrategy() {
        return (final MongoDatabase connection, final List<Document> initialCollections, final String... collectionsToExclude) -> {
            if (initialCollections.isEmpty()) {
                return;
            }

            final Document toDelete = excludeCollections(initialCollections, collectionsToExclude);

            MongoDbOperations.DELETE.execute(connection, toDelete);
        };
    }

    private Document excludeCollections(final Iterable<Document> collections, final String... collectionsToExclude) {
        final List<String> toRetain = Arrays.asList(collectionsToExclude);

        final Document toDelete = new Document();

        for (final Document doc : collections) {
            if (!isSeedDocument(doc)) {
                final String collectionName = (String) doc.get("name");
                if (!toRetain.contains(collectionName) && !isSystemCollection(collectionName)) {
                    toDelete.put(collectionName, doc);
                }
            } else {
                for (final Entry<String, Object> childCollection : doc.entrySet()) {
                    if (!toRetain.contains(childCollection.getKey())) {
                        toDelete.put(childCollection.getKey(), childCollection.getValue());
                    }
                }
            }
        }

        return toDelete;
    }

    private boolean isSystemCollection(final String collectionName) {
        return collectionName.startsWith("system.");
    }

    private boolean isSeedDocument(final Document doc) {
        return doc.entrySet().stream().allMatch(e -> List.class.isAssignableFrom(e.getValue().getClass()));
    }
}
