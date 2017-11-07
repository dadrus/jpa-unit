package eu.drus.jpa.unit.neo4j.operation;

import java.sql.Connection;
import java.sql.SQLException;

import org.jgrapht.Graph;

import eu.drus.jpa.unit.neo4j.dataset.Edge;
import eu.drus.jpa.unit.neo4j.dataset.Node;

public class CompositeOperation implements Neo4JOperation {

    private Neo4JOperation[] operations;

    public CompositeOperation(final Neo4JOperation... operations) {
        this.operations = operations;
    }

    @Override
    public void execute(final Connection connection, final Graph<Node, Edge> graph) throws SQLException {
        for (final Neo4JOperation operation : operations) {
            operation.execute(connection, graph);
        }
    }
}
