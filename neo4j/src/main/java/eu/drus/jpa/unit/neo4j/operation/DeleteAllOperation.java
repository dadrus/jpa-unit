package eu.drus.jpa.unit.neo4j.operation;

import static org.neo4j.cypherdsl.CypherQuery.match;

import java.sql.Connection;
import java.sql.SQLException;

import org.jgrapht.Graph;
import org.neo4j.cypherdsl.grammar.StartNext;

import eu.drus.jpa.unit.neo4j.dataset.Edge;
import eu.drus.jpa.unit.neo4j.dataset.Node;

public class DeleteAllOperation extends AbstractNeo4JOperation {

    @Override
    public void execute(final Connection connection, final Graph<Node, Edge> graph) throws SQLException {
        for (final Node node : graph.vertexSet()) {

            final StartNext match = match(node.toPath().withId("n").build());

            executeQuery(connection, match.toString() + " DETACH DELETE n");
        }
    }

}
