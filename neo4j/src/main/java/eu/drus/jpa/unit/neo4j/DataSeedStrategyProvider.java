package eu.drus.jpa.unit.neo4j;

import eu.drus.jpa.unit.api.DataSeedStrategy.StrategyProvider;
import eu.drus.jpa.unit.neo4j.operation.Neo4JOperation;
import eu.drus.jpa.unit.neo4j.operation.Neo4JOperations;

public class DataSeedStrategyProvider implements StrategyProvider<Neo4JOperation> {

    @Override
    public Neo4JOperation insertStrategy() {
        return Neo4JOperations.INSERT;
    }

    @Override
    public Neo4JOperation cleanInsertStrategy() {
        return Neo4JOperations.CLEAN_INSERT;
    }

    @Override
    public Neo4JOperation refreshStrategy() {
        return Neo4JOperations.REFRESH;
    }

    @Override
    public Neo4JOperation updateStrategy() {
        return Neo4JOperations.UPDATE;
    }

}
