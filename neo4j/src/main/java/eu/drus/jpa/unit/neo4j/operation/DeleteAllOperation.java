package eu.drus.jpa.unit.neo4j.operation;

import java.sql.Connection;
import java.sql.SQLException;

import org.jgrapht.Graph;
import org.neo4j.cypherdsl.core.Cypher;

import eu.drus.jpa.unit.neo4j.dataset.Edge;
import eu.drus.jpa.unit.neo4j.dataset.Node;

public class DeleteAllOperation extends AbstractNeo4JOperation {

    @Override
    public void execute(final Connection connection, final Graph<Node, Edge> graph) throws SQLException {
        for (final Node node : graph.vertexSet()) {
            executeQuery(connection,
                    Cypher.match(node.toPath().withId("n").build()).detachDelete(Cypher.name("n")).build().getCypher());
        }
    }
}
