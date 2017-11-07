package eu.drus.jpa.unit.neo4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DefaultDirectedGraph;

import eu.drus.jpa.unit.api.CleanupStrategy.StrategyProvider;
import eu.drus.jpa.unit.neo4j.dataset.DatabaseReader;
import eu.drus.jpa.unit.neo4j.dataset.Edge;
import eu.drus.jpa.unit.neo4j.dataset.GraphElementFactory;
import eu.drus.jpa.unit.neo4j.dataset.Node;
import eu.drus.jpa.unit.neo4j.operation.Neo4JOperations;
import eu.drus.jpa.unit.spi.CleanupStrategyExecutor;
import eu.drus.jpa.unit.spi.DbFeatureException;

public class CleanupStrategyProvider implements StrategyProvider<CleanupStrategyExecutor<Connection, Graph<Node, Edge>>> {

    private static final String UNABLE_TO_CLEAN_DATABASE = "Unable to clean database.";
    private DatabaseReader dbReader;

    public CleanupStrategyProvider(final GraphElementFactory factory) {
        dbReader = new DatabaseReader(factory);
    }

    @Override
    public CleanupStrategyExecutor<Connection, Graph<Node, Edge>> strictStrategy() {
        return (final Connection connection, final List<Graph<Node, Edge>> initialGraphs, final String... nodeTypesToRetain) -> {

            try {
                Neo4JOperations.DELETE_ALL.execute(connection, computeGraphToBeDeleted(dbReader.readGraph(connection), nodeTypesToRetain));
                connection.commit();
            } catch (final SQLException e) {
                throw new DbFeatureException(UNABLE_TO_CLEAN_DATABASE, e);
            }
        };
    }

    @Override
    public CleanupStrategyExecutor<Connection, Graph<Node, Edge>> usedTablesOnlyStrategy() {
        return (final Connection connection, final List<Graph<Node, Edge>> initialGraphs, final String... nodeTypesToRetain) -> {
            if (initialGraphs.isEmpty()) {
                return;
            }

            try {
                for (final Graph<Node, Edge> graph : initialGraphs) {
                    Neo4JOperations.DELETE_ALL.execute(connection, computeGraphToBeDeleted(graph, nodeTypesToRetain));
                }

                connection.commit();
            } catch (final SQLException e) {
                throw new DbFeatureException(UNABLE_TO_CLEAN_DATABASE, e);
            }
        };
    }

    @Override
    public CleanupStrategyExecutor<Connection, Graph<Node, Edge>> usedRowsOnlyStrategy() {
        return (final Connection connection, final List<Graph<Node, Edge>> initialGraphs, final String... nodeTypesToRetain) -> {
            if (initialGraphs.isEmpty()) {
                return;
            }

            try {
                for (final Graph<Node, Edge> graph : initialGraphs) {
                    Neo4JOperations.DELETE.execute(connection, computeGraphToBeDeleted(graph, nodeTypesToRetain));
                }

                connection.commit();
            } catch (final SQLException e) {
                throw new DbFeatureException(UNABLE_TO_CLEAN_DATABASE, e);
            }
        };
    }

    private Graph<Node, Edge> computeGraphToBeDeleted(final Graph<Node, Edge> graph, final String... nodeTypesToRetain) {
        final DirectedGraph<Node, Edge> toDelete = new DefaultDirectedGraph<>(new ClassBasedEdgeFactory<>(Edge.class));

        // copy graph to a destination, which we are going to modify
        Graphs.addGraph(toDelete, graph);

        // remove the nodes, we have to retain from the graph
        Graphs.removeVerticesAndPreserveConnectivity(toDelete, v -> shouldRetainNode(v, nodeTypesToRetain));

        return toDelete;
    }

    private boolean shouldRetainNode(final Node node, final String... nodeTypesToRetain) {
        for (final String nodeToExclude : nodeTypesToRetain) {
            if (node.getLabels().contains(nodeToExclude)) {
                return true;
            }
        }
        return false;
    }
}
