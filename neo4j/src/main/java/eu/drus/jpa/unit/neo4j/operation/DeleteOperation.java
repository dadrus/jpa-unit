package eu.drus.jpa.unit.neo4j.operation;

import java.sql.Connection;
import java.sql.SQLException;

import org.jgrapht.Graph;
import org.neo4j.cypherdsl.core.Cypher;
import org.neo4j.cypherdsl.core.Expression;
import org.neo4j.cypherdsl.core.PatternElement;

import eu.drus.jpa.unit.neo4j.dataset.Edge;
import eu.drus.jpa.unit.neo4j.dataset.Node;

public class DeleteOperation extends AbstractNeo4JOperation {

    @Override
    public void execute(final Connection connection, final Graph<Node, Edge> graph) throws SQLException {
        final PatternElement[] edgePatterns = graph.edgeSet().stream()
                .map(e -> e.toPath().withAllAttributes().withNodeIdAttributes().build()).toArray(PatternElement[]::new);
        final Expression[] edgeIds = graph.edgeSet().stream().map(e -> Cypher.name(e.getId())).toArray(Expression[]::new);

        final PatternElement[] nodePatterns = graph.vertexSet().stream().map(n -> n.toPath().withAllAttributes().build())
                .toArray(PatternElement[]::new);
        final Expression[] nodeIds = graph.vertexSet().stream().map(n -> Cypher.name(n.getId())).toArray(Expression[]::new);

        executeQuery(connection, Cypher.match(edgePatterns).delete(edgeIds).build().getCypher());
        executeQuery(connection, Cypher.match(nodePatterns).delete(nodeIds).build().getCypher());
    }
}
