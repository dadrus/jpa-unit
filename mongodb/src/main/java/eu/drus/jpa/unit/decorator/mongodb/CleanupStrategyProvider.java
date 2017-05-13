package eu.drus.jpa.unit.decorator.mongodb;

import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoDatabase;

import eu.drus.jpa.unit.api.CleanupStrategy.StrategyProvider;
import eu.drus.jpa.unit.core.CleanupStrategyExecutor;

public class CleanupStrategyProvider implements StrategyProvider<CleanupStrategyExecutor<MongoDatabase, Document>> {

    @Override
    public CleanupStrategyExecutor<MongoDatabase, Document> strictStrategy() {
        return (final MongoDatabase connection, final List<Document> initialDataSets, final String... tablesToExclude) -> {

        };
    }

    @Override
    public CleanupStrategyExecutor<MongoDatabase, Document> usedTablesOnlyStrategy() {
        return (final MongoDatabase connection, final List<Document> initialDataSets, final String... tablesToExclude) -> {

        };
    }

    @Override
    public CleanupStrategyExecutor<MongoDatabase, Document> usedRowsOnlyStrategy() {
        return (final MongoDatabase connection, final List<Document> initialDataSets, final String... tablesToExclude) -> {

        };
    }

}
