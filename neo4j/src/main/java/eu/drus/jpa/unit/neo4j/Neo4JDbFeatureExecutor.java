package eu.drus.jpa.unit.neo4j;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DefaultDirectedGraph;

import eu.drus.jpa.unit.api.CleanupStrategy;
import eu.drus.jpa.unit.api.DataSeedStrategy;
import eu.drus.jpa.unit.api.ExpectedDataSets;
import eu.drus.jpa.unit.api.JpaUnitException;
import eu.drus.jpa.unit.neo4j.dataset.DataSetLoaderProvider;
import eu.drus.jpa.unit.neo4j.dataset.Edge;
import eu.drus.jpa.unit.neo4j.dataset.GraphComparator;
import eu.drus.jpa.unit.neo4j.dataset.GraphElementFactory;
import eu.drus.jpa.unit.neo4j.dataset.Node;
import eu.drus.jpa.unit.neo4j.operation.Neo4JOperation;
import eu.drus.jpa.unit.spi.AbstractDbFeatureExecutor;
import eu.drus.jpa.unit.spi.AssertionErrorCollector;
import eu.drus.jpa.unit.spi.CleanupStrategyExecutor;
import eu.drus.jpa.unit.spi.DataSetFormat;
import eu.drus.jpa.unit.spi.DataSetLoader;
import eu.drus.jpa.unit.spi.DbFeature;
import eu.drus.jpa.unit.spi.DbFeatureException;
import eu.drus.jpa.unit.spi.FeatureResolver;

public class Neo4JDbFeatureExecutor extends AbstractDbFeatureExecutor<Graph<Node, Edge>, Connection> {

    private GraphElementFactory graphElementFactory;

    protected Neo4JDbFeatureExecutor(final FeatureResolver featureResolver, final List<Class<?>> entityClasses) {
        super(featureResolver);
        graphElementFactory = new GraphElementFactory(entityClasses);
    }

    private static URI toUri(final String path) throws URISyntaxException {
        final URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        if (url == null) {
            throw new JpaUnitException(path + " not found");
        }

        return url.toURI();
    }

    @Override
    protected List<Graph<Node, Edge>> loadDataSets(final List<String> paths) {
        final List<Graph<Node, Edge>> dataSets = new ArrayList<>();
        try {
            for (final String path : paths) {
                final File file = new File(toUri(path));
                final DataSetLoader<Graph<Node, Edge>> loader = DataSetFormat.inferFromFile(file)
                        .select(new DataSetLoaderProvider(graphElementFactory));
                dataSets.add(loader.load(file));
            }
        } catch (final IOException | URISyntaxException e) {
            throw new JpaUnitException("Could not load initial data sets", e);
        }
        return dataSets;
    }

    @Override
    protected DbFeature<Connection> createCleanupFeature(final CleanupStrategy cleanupStrategy,
            final List<Graph<Node, Edge>> initialDataSets) {
        return (final Connection connection) -> {
            final CleanupStrategyExecutor<Connection, Graph<Node, Edge>> executor = cleanupStrategy
                    .provide(new CleanupStrategyProvider(graphElementFactory));
            executor.execute(connection, initialDataSets);
        };
    }

    @Override
    protected DbFeature<Connection> createApplyCustomScriptFeature(final List<String> scriptPaths) {
        return (final Connection connection) -> {
            try {
                for (final String scriptPath : scriptPaths) {
                    executeScript(loadScript(scriptPath), connection);
                }
                connection.commit();
            } catch (IOException | URISyntaxException | SQLException e) {
                throw new DbFeatureException("Could not apply custom scripts feature", e);
            }
        };
    }

    @Override
    protected DbFeature<Connection> createSeedDataFeature(final DataSeedStrategy dataSeedStrategy,
            final List<Graph<Node, Edge>> initialDataSets) {
        return (final Connection connection) -> {
            final Graph<Node, Edge> mergedGraph = mergeGraphs(initialDataSets);

            final Neo4JOperation operation = dataSeedStrategy.provide(new DataSeedStrategyProvider());
            try {
                operation.execute(connection, mergedGraph);
                connection.commit();
            } catch (final SQLException e) {
                throw new DbFeatureException("Could not seed data base", e);
            }
        };
    }

    @Override
    protected DbFeature<Connection> createVerifyDataAfterFeature(final ExpectedDataSets expectedDataSets) {
        return (final Connection connection) -> {
            final Graph<Node, Edge> mergedGraph = mergeGraphs(loadDataSets(Arrays.asList(expectedDataSets.value())));

            final GraphComparator graphComparator = new GraphComparator(graphElementFactory, expectedDataSets.excludeColumns(),
                    expectedDataSets.strict());

            final AssertionErrorCollector errorCollector = new AssertionErrorCollector();
            graphComparator.compare(connection, mergedGraph, errorCollector);

            errorCollector.report();
        };
    }

    private Graph<Node, Edge> mergeGraphs(final List<Graph<Node, Edge>> graphs) {
        final Graph<Node, Edge> mergedGraph = new DefaultDirectedGraph<>(new ClassBasedEdgeFactory<>(Edge.class));

        for (final Graph<Node, Edge> graph : graphs) {
            Graphs.addGraph(mergedGraph, graph);
        }

        return mergedGraph;
    }

    private void executeScript(final String script, final Connection connection) throws SQLException {
        if (script.isEmpty()) {
            return;
        }

        try (PreparedStatement ps = connection.prepareStatement(script)) {
            ps.execute();
        }
    }

}
