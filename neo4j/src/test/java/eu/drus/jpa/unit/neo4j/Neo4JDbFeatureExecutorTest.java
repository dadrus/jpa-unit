package eu.drus.jpa.unit.neo4j;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import eu.drus.jpa.unit.api.CleanupStrategy;
import eu.drus.jpa.unit.api.DataSeedStrategy;
import eu.drus.jpa.unit.api.ExpectedDataSets;
import eu.drus.jpa.unit.api.JpaUnitException;
import eu.drus.jpa.unit.neo4j.dataset.Edge;
import eu.drus.jpa.unit.neo4j.dataset.EntityUtils;
import eu.drus.jpa.unit.neo4j.dataset.Node;
import eu.drus.jpa.unit.neo4j.operation.Neo4JOperation;
import eu.drus.jpa.unit.spi.CleanupStrategyExecutor;
import eu.drus.jpa.unit.spi.DbFeature;
import eu.drus.jpa.unit.spi.DbFeatureException;
import eu.drus.jpa.unit.spi.FeatureResolver;

@RunWith(PowerMockRunner.class)
@PrepareForTest(EntityUtils.class)
public class Neo4JDbFeatureExecutorTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Mock
    private FeatureResolver featureResolver;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement ps;

    @Mock
    private ResultSet rs;

    @Mock
    private CleanupStrategy cleanupStrategy;

    @Mock
    private DataSeedStrategy dataSeedStrategy;

    @Mock
    private CleanupStrategyExecutor<Connection, Graph<Node, Edge>> cleanupStrategyExecutor;

    @Mock
    private Neo4JOperation operation;

    @Mock
    private ExpectedDataSets expectedDataSets;

    private Neo4JDbFeatureExecutor featureExecutor;

    @SuppressWarnings("unchecked")
    @Before
    public void setup() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        mockStatic(EntityUtils.class);
        when(EntityUtils.getEntityClassFromNodeLabels(any(List.class), any(List.class))).thenReturn(Neo4JDbFeatureExecutorTest.class);
        when(EntityUtils.getNamesOfIdProperties(any(Class.class))).thenReturn(Arrays.asList());

        featureExecutor = new Neo4JDbFeatureExecutor(featureResolver, Arrays.asList());
    }

    @Test
    public void testLoadDataSetsUsingAvailableFilePaths() {
        // GIVEN

        // WHEN
        final List<Graph<Node, Edge>> dataSetList = featureExecutor.loadDataSets(Arrays.asList("test-data.xml", "test-data.xml"));

        // THEN
        assertNotNull(dataSetList);
        assertThat(dataSetList.size(), equalTo(2));

        final Graph<Node, Edge> graph1 = dataSetList.get(0);
        assertNotNull(graph1);
        assertThat(graph1.vertexSet().size(), equalTo(6));
        assertThat(graph1.edgeSet().size(), equalTo(4));

        final Graph<Node, Edge> graph2 = dataSetList.get(1);
        assertNotNull(graph2);
        assertThat(graph2.vertexSet().size(), equalTo(6));
        assertThat(graph2.edgeSet().size(), equalTo(4));

        assertThat(graph1, equalTo(graph1));
    }

    @Test(expected = JpaUnitException.class)
    public void testLoadDataSetsUsingNotAvailableFilePaths() {
        // GIVEN

        // WHEN
        featureExecutor.loadDataSets(Arrays.asList("test-data1.xml"));

        // THEN
        // JpaUnitException is thrown
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCleanupFeatureExecution() throws DbFeatureException {
        // GIVEN
        when(cleanupStrategy.provide(any(CleanupStrategy.StrategyProvider.class))).thenReturn(cleanupStrategyExecutor);
        final List<Graph<Node, Edge>> initialDataSets = Arrays.asList(new DefaultDirectedGraph<>(new ClassBasedEdgeFactory<>(Edge.class)));

        // WHEN
        final DbFeature<Connection> feature = featureExecutor.createCleanupFeature(cleanupStrategy, initialDataSets);
        assertThat(feature, notNullValue());
        feature.execute(connection);

        // THEN
        verify(cleanupStrategyExecutor).execute(eq(connection), eq(initialDataSets));
    }

    @Test
    public void testApplyCustomScriptFeatureExecutionUsingAvailableFilePaths() throws DbFeatureException, SQLException {
        // GIVEN

        // WHEN
        final DbFeature<Connection> feature = featureExecutor.createApplyCustomScriptFeature(Arrays.asList("test.script", "test.script"));
        assertThat(feature, notNullValue());
        feature.execute(connection);

        // THEN
        verify(connection, times(2)).prepareStatement(any(String.class));
        verify(ps, times(2)).execute();
        verify(ps, times(2)).close();
    }

    @Test
    public void testApplyCustomScriptFeatureExecutionHandlesSQLExceptionsProperly() throws DbFeatureException, SQLException {
        // GIVEN
        when(ps.execute()).thenThrow(new SQLException("error"));

        // WHEN
        final DbFeature<Connection> feature = featureExecutor.createApplyCustomScriptFeature(Arrays.asList("test.script"));
        assertThat(feature, notNullValue());

        try {
            feature.execute(connection);
            fail("exception expected");
        } catch (final DbFeatureException e) {
            // expected
        }

        // THEN
        verify(connection).prepareStatement(any(String.class));
        verify(ps).execute();
        verify(ps).close();
    }

    @Test
    public void testApplyCustomScriptFeatureExecutionUsingEmpyFile() throws DbFeatureException, IOException, SQLException {
        // GIVEN
        final File tmpFile = tmpFolder.newFile();

        // WHEN
        final DbFeature<Connection> feature = featureExecutor.createApplyCustomScriptFeature(Arrays.asList(tmpFile.getPath()));
        assertThat(feature, notNullValue());
        feature.execute(connection);

        // THEN
        verify(connection, never()).prepareStatement(anyString());
    }

    @Test(expected = DbFeatureException.class)
    public void testApplyCustomScriptFeatureExecutionUsingNotAvailableFilePaths() throws DbFeatureException {
        // GIVEN

        // WHEN
        final DbFeature<Connection> feature = featureExecutor.createApplyCustomScriptFeature(Arrays.asList("test-data1.xml"));
        assertThat(feature, notNullValue());
        feature.execute(connection);

        // THEN
        // DbFeatureException is thrown
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSeedDataFeatureExecution() throws DbFeatureException, SQLException {
        // GIVEN
        final List<Graph<Node, Edge>> dataSets = featureExecutor.loadDataSets(Arrays.asList("test-data.xml"));
        when(dataSeedStrategy.provide(any(DataSeedStrategy.StrategyProvider.class))).thenReturn(operation);

        // WHEN
        final DbFeature<Connection> feature = featureExecutor.createSeedDataFeature(dataSeedStrategy, dataSets);
        assertThat(feature, notNullValue());
        feature.execute(connection);

        // THEN
        verify(operation).execute(eq(connection), any(Graph.class));
    }

    @Test
    public void testVerifyDataAfterFeatureExecution() throws DbFeatureException, SQLException {
        // GIVEN
        when(expectedDataSets.strict()).thenReturn(Boolean.FALSE);
        when(expectedDataSets.value()).thenReturn(new String[] {});
        when(expectedDataSets.orderBy()).thenReturn(new String[] {});
        when(expectedDataSets.excludeColumns()).thenReturn(new String[] {});

        // WHEN
        final DbFeature<Connection> feature = featureExecutor.createVerifyDataAfterFeature(expectedDataSets);
        assertThat(feature, notNullValue());
        feature.execute(connection);

        // THEN
        verify(connection, times(2)).prepareStatement(anyString());
        verify(ps, times(2)).executeQuery();
        verify(rs, times(2)).next();
        verify(rs, times(2)).close();
        verify(ps, times(2)).close();
        verifyNoMoreInteractions(connection);
    }
}
