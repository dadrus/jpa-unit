package eu.drus.jpa.unit.neo4j.operation;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.jgrapht.Graph;
import org.neo4j.cypherdsl.core.Cypher;
import org.neo4j.cypherdsl.core.PatternElement;

import eu.drus.jpa.unit.neo4j.dataset.Edge;
import eu.drus.jpa.unit.neo4j.dataset.Node;

public class InsertOperation extends AbstractNeo4JOperation {

    @Override
    public void execute(final Connection connection, final Graph<Node, Edge> graph) throws SQLException {
        final List<PatternElement> patterns = new ArrayList<>();
        graph.vertexSet().forEach(n -> patterns.add(n.toPath().withAllAttributes().build()));
        graph.edgeSet().forEach(e -> patterns.add(e.toPath().withAllAttributes().build()));

        executeQuery(connection, Cypher.create(patterns).build().getCypher());
    }
}
