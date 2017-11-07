package eu.drus.jpa.unit.neo4j.operation;

import static java.util.stream.Collectors.toList;
import static org.neo4j.cypherdsl.CypherQuery.identifier;
import static org.neo4j.cypherdsl.CypherQuery.literal;
import static org.neo4j.cypherdsl.CypherQuery.match;
import static org.neo4j.cypherdsl.CypherQuery.merge;
import static org.neo4j.cypherdsl.CypherQuery.property;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.jgrapht.Graph;
import org.neo4j.cypherdsl.expression.SetExpression;
import org.neo4j.cypherdsl.grammar.UpdateNext;

import eu.drus.jpa.unit.neo4j.dataset.Edge;
import eu.drus.jpa.unit.neo4j.dataset.Node;

public class RefreshOperation extends AbstractNeo4JOperation {

    @Override
    public void execute(final Connection connection, final Graph<Node, Edge> graph) throws SQLException {
        for (final Node node : graph.vertexSet()) {

            final List<SetExpression> attributes = node.getAttributes().stream().filter(a -> !a.isId())
                    .map(a -> property(identifier(node.getId()).property(a.getName()), literal(a.getValue()))).collect(toList());

            final UpdateNext query = merge(node.toPath().withIdAttributes().build()).set(attributes);

            executeQuery(connection, query.toString());
        }

        for (final Edge edge : graph.edgeSet()) {
            final Node sourceNode = edge.getSourceNode();
            final Node targetNode = edge.getTargetNode();

            final List<SetExpression> attributes = edge.getAttributes().stream()
                    .map(a -> property(identifier(edge.getId()).property(a.getName()), literal(a.getValue()))).collect(toList());

            final UpdateNext query = match(sourceNode.toPath().withIdAttributes().build(), targetNode.toPath().withIdAttributes().build())
                    .merge(edge.toPath().build()).set(attributes);

            executeQuery(connection, query.toString());
        }
    }
}
