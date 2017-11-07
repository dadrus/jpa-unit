package eu.drus.jpa.unit.neo4j.operation;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static org.neo4j.cypherdsl.CypherQuery.create;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.jgrapht.Graph;
import org.neo4j.cypherdsl.Path;
import org.neo4j.cypherdsl.grammar.UpdateNext;

import eu.drus.jpa.unit.neo4j.dataset.Edge;
import eu.drus.jpa.unit.neo4j.dataset.Node;

public class InsertOperation extends AbstractNeo4JOperation {

    @Override
    public void execute(final Connection connection, final Graph<Node, Edge> graph) throws SQLException {
        // create query to insert nodes and edges
        final List<Path> paths = graph.vertexSet().stream().map(n -> n.toPath().withAllAttributes().build()).collect(toList());
        graph.edgeSet().stream().map(e -> e.toPath().withAllAttributes().build()).collect(toCollection(() -> paths));

        final UpdateNext query = create(paths.toArray(new Path[paths.size()]));

        executeQuery(connection, query.toString());
    }
}
