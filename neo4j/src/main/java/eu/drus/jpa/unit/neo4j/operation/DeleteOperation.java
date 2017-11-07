package eu.drus.jpa.unit.neo4j.operation;

import static java.util.stream.Collectors.toList;
import static org.neo4j.cypherdsl.CypherQuery.identifier;
import static org.neo4j.cypherdsl.CypherQuery.match;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.jgrapht.Graph;
import org.neo4j.cypherdsl.Path;
import org.neo4j.cypherdsl.expression.ReferenceExpression;
import org.neo4j.cypherdsl.grammar.UpdateNext;

import eu.drus.jpa.unit.neo4j.dataset.Edge;
import eu.drus.jpa.unit.neo4j.dataset.Node;

public class DeleteOperation extends AbstractNeo4JOperation {

    @Override
    public void execute(final Connection connection, final Graph<Node, Edge> graph) throws SQLException {
        final List<Path> nodes = graph.vertexSet().stream().map(n -> n.toPath().withAllAttributes().build()).collect(toList());
        final List<ReferenceExpression> nodeIds = graph.vertexSet().stream().map(n -> identifier(n.getId())).collect(toList());

        final List<Path> edges = graph.edgeSet().stream().map(e -> e.toPath().withAllAttributes().withNodeIdAttributes().build())
                .collect(toList());
        final List<ReferenceExpression> edgeIds = graph.edgeSet().stream().map(e -> identifier(e.getId())).collect(toList());

        final UpdateNext deleteEdgesQuery = match(edges.toArray(new Path[edges.size()])).delete(edgeIds);
        final UpdateNext deleteNodesQuery = match(nodes.toArray(new Path[nodes.size()])).delete(nodeIds);

        executeQuery(connection, deleteEdgesQuery.toString());
        executeQuery(connection, deleteNodesQuery.toString());
    }
}
