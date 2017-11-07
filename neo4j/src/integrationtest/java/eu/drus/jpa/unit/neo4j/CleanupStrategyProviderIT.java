package eu.drus.jpa.unit.neo4j;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;

import eu.drus.jpa.unit.neo4j.dataset.Edge;
import eu.drus.jpa.unit.neo4j.dataset.GraphElementFactory;
import eu.drus.jpa.unit.neo4j.dataset.Node;
import eu.drus.jpa.unit.neo4j.dataset.graphml.GraphMLReader;
import eu.drus.jpa.unit.spi.DbFeatureException;
import eu.drus.jpa.unit.test.model.CookingRecipe;
import eu.drus.jpa.unit.test.model.Person;
import eu.drus.jpa.unit.test.model.Technology;

public class CleanupStrategyProviderIT {

    private static ServerControls server;
    private static String connectionUrl;

    private static GraphElementFactory factory = new GraphElementFactory(
            Arrays.asList(Person.class, Technology.class, CookingRecipe.class));

    @BeforeClass
    public static void startNeo4j() throws ClassNotFoundException {
        Class.forName("org.neo4j.jdbc.Driver");

        server = TestServerBuilders.newInProcessBuilder().withConfig("dbms.connector.bolt.address", "localhost:7687").newServer();
        connectionUrl = "jdbc:neo4j:" + server.boltURI().toString();
    }

    @AfterClass
    public static void stopNeo4j() {
        server.close();
    }

    private static List<Graph<Node, Edge>> loadDataSet(final String path) {
        try {
            final URL resource = Thread.currentThread().getContextClassLoader().getResource(path);
            final File file = new File(resource.toURI());

            final DefaultDirectedGraph<Node, Edge> graph = new DefaultDirectedGraph<>(new ClassBasedEdgeFactory<>(Edge.class));
            final GraphMLReader<Node, Edge> importer = new GraphMLReader<>(factory, factory);
            importer.importGraph(graph, file);

            return Arrays.asList(graph);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Graph<Node, Edge>> initialDataSets = loadDataSet("datasets/max-payne-data.xml");

    private static Connection openConnection() throws SQLException {
        final Connection connection = DriverManager.getConnection(connectionUrl, "neo4j", "test");
        connection.setAutoCommit(false);
        return connection;
    }

    @Before
    public void prepareDataBase() throws SQLException {
        try (final Connection connection = openConnection()) {

            try (PreparedStatement ps = connection.prepareStatement("MATCH (n) DETACH DELETE n")) {
                ps.executeUpdate();
            }

            try (PreparedStatement ps = connection.prepareStatement("CREATE (p1:Person {id:100, name:\"Max\", surname:\"Payne\"})"
                    + "-[:expertIn]->(:Technology {name: \"Detective work\"}),"
                    + "(p2:Person {id:101, name:\"Alex\", surname:\"Balder\"})-[:expertIn]->(:Technology {name: \"Weapons of all kinds\"}),"
                    + "(r:CookingRecipe {id: 200, name:\"Muffin\", description:\"A really tasty one\"})")) {
                ps.executeUpdate();
            }
            connection.commit();
        }
    }

    @After
    public void deleteDataBase() throws SQLException {
        try (final Connection connection = openConnection()) {
            try (PreparedStatement ps = connection.prepareStatement("MATCH (n) DETACH DELETE n")) {
                ps.executeUpdate();
            }
            connection.commit();
        }
    }

    private void verifyNoResult(final Connection connection, final String query) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            try (final ResultSet rs = ps.executeQuery()) {
                assertFalse(rs.next());
            }
        }
    }

    private void verifySingleResult(final Connection connection, final String query) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            try (final ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertFalse(rs.next());
            }
        }
    }

    @Test
    public void testStrictCleanupStrategy() throws SQLException, DbFeatureException {
        // GIVEN
        // definition from above and ...
        final CleanupStrategyProvider provider = new CleanupStrategyProvider(factory);
        try (final Connection connection = openConnection()) {

            // WHEN
            provider.strictStrategy().execute(connection, initialDataSets, "");

            // THEN
            verifyNoResult(connection, "MATCH (n) RETURN n");
            verifyNoResult(connection, "MATCH ()-[r]->() RETURN r");
        }
    }

    @Test
    public void testUsedRowsOnlyStrategy() throws SQLException, DbFeatureException {
        // GIVEN
        // definition from above and ...
        final CleanupStrategyProvider provider = new CleanupStrategyProvider(factory);
        try (final Connection connection = openConnection()) {

            // WHEN
            provider.usedRowsOnlyStrategy().execute(connection, initialDataSets, "");

            // THEN
            verifySingleResult(connection,
                    "MATCH (p:Person {id:101})-[:expertIn]->(:Technology {name: \"Weapons of all kinds\"}) RETURN p");
            verifySingleResult(connection, "MATCH (r:CookingRecipe) RETURN r");

            verifyNoResult(connection, "MATCH (p:Person {id:100}) RETURN p");
            verifyNoResult(connection, "MATCH (t:Technology {name: \"Detective work\"}) RETURN t");
        }
    }

    @Test
    public void testUsedTablesOnlyStrategy() throws SQLException, DbFeatureException {
        // GIVEN
        // definition from above and ...
        final CleanupStrategyProvider provider = new CleanupStrategyProvider(factory);
        try (final Connection connection = openConnection()) {

            // WHEN
            provider.usedTablesOnlyStrategy().execute(connection, initialDataSets, "");

            verifyNoResult(connection, "MATCH (n) WHERE not(n:CookingRecipe) RETURN n");
            verifySingleResult(connection, "MATCH (r:CookingRecipe) RETURN r");
        }
    }
}
