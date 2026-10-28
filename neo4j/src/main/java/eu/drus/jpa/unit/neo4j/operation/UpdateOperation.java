package eu.drus.jpa.unit.neo4j.operation;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import org.jgrapht.Graph;
import org.neo4j.cypherdsl.core.Cypher;
import org.neo4j.cypherdsl.core.Operation;
import org.neo4j.cypherdsl.core.Relationship;

import eu.drus.jpa.unit.neo4j.dataset.Attribute;
import eu.drus.jpa.unit.neo4j.dataset.Edge;
import eu.drus.jpa.unit.neo4j.dataset.GraphElement;
import eu.drus.jpa.unit.neo4j.dataset.Node;

public class UpdateOperation extends AbstractNeo4JOperation {

    @Override
    public void execute(final Connection connection, final Graph<Node, Edge> graph) throws SQLException {
        for (final Node node : graph.vertexSet()) {
            final org.neo4j.cypherdsl.core.Node pattern = node.toPath().withIdAttributes().build();
            final List<Attribute> attributes = node.getAttributes().stream().filter(a -> !a.isId()).collect(Collectors.toList());

            if (!attributes.isEmpty()) {
                executeQuery(connection,
                        Cypher.match(pattern).set(GraphElement.setOperations(pattern, attributes)).build().getCypher());
            }
        }

        for (final Edge edge : graph.edgeSet()) {
            final Relationship relationship = edge.toPath().build();
            final org.neo4j.cypherdsl.core.Node source = edge.getSourceNode().toPath().withIdAttributes().build();
            final org.neo4j.cypherdsl.core.Node target = edge.getTargetNode().toPath().withIdAttributes().build();
            final Operation[] operations = GraphElement.setOperations(relationship, edge.getAttributes());

            if (operations.length > 0) {
                executeQuery(connection, Cypher.match(source, target).merge(relationship).set(operations).build().getCypher());
            }
        }
    }
}
