package eu.drus.jpa.unit.neo4j.operation;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableMap;

import eu.drus.jpa.unit.neo4j.dataset.Edge;
import eu.drus.jpa.unit.neo4j.dataset.GraphElementFactory;
import eu.drus.jpa.unit.neo4j.dataset.Node;
import eu.drus.jpa.unit.neo4j.test.entities.A;
import eu.drus.jpa.unit.neo4j.test.entities.B;

@RunWith(MockitoJUnitRunner.class)
public class RefreshOperationTest {

    private GraphElementFactory graphElementFactory;

    @Mock
    private Connection connection;

    @Spy
    private RefreshOperation operation;

    @Before
    public void prepareMocks() throws SQLException {
        doAnswer(i -> null).when(operation).executeQuery(any(Connection.class), anyString());

        graphElementFactory = new GraphElementFactory(Arrays.asList(A.class, B.class));
    }

    @Test
    public void testExecute() throws Exception {
        // GIVEN
        final Node n1 = graphElementFactory.createNode("n1", Arrays.asList("A"),
                ImmutableMap.<String, Object>builder().put("id", 1l).put("value", "A").build());
        final Node n2 = graphElementFactory.createNode("n2", Arrays.asList("A"),
                ImmutableMap.<String, Object>builder().put("id", 2l).build());
        final Edge e1 = graphElementFactory.createEdge(n1, n2, "e1", Arrays.asList("E"),
                ImmutableMap.<String, Object>builder().put("id", 3l).put("value", "C").build());

        final Graph<Node, Edge> graph = new DefaultDirectedGraph<>(new ClassBasedEdgeFactory<>(Edge.class));
        graph.addVertex(n1);
        graph.addVertex(n2);
        graph.addEdge(e1.getSourceNode(), e1.getTargetNode(), e1);

        // WHEN
        operation.execute(connection, graph);

        // THEN
        final ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        verify(operation, times(3)).executeQuery(eq(connection), queryCaptor.capture());
        final List<String> queries = queryCaptor.getAllValues();
        assertThat(queries.get(0), containsString("MERGE (n1:A {id:1}) SET n1.value=\"A\""));
        assertThat(queries.get(1), containsString("MERGE (n2:A {id:2})"));
        assertThat(queries.get(2), containsString("MATCH (n1:A {id:1}),(n2:A {id:2}) MERGE (n1)-[e1:E]->(n2) SET e1.id=3,e1.value=\"C\""));
    }
}
