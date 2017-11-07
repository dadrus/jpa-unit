package eu.drus.jpa.unit.neo4j.dataset;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.common.collect.ImmutableMap;

import eu.drus.jpa.unit.neo4j.test.entities.A;
import eu.drus.jpa.unit.neo4j.test.entities.B;
import eu.drus.jpa.unit.spi.AssertionErrorCollector;

@RunWith(PowerMockRunner.class)
@PrepareForTest(GraphComparator.class)
public class GraphComparatorTest {

    private GraphElementFactory graphElementFactory;

    @Mock
    private DatabaseReader dbReader;

    @Mock
    private Connection connection;

    @Mock
    private AssertionErrorCollector errorCollector;

    private Graph<Node, Edge> createGraph(final List<Node> nodes, final List<Edge> edges) {
        final Graph<Node, Edge> graph = new DefaultDirectedGraph<>(new ClassBasedEdgeFactory<>(Edge.class));
        nodes.forEach(graph::addVertex);
        edges.forEach(e -> graph.addEdge(e.getSourceNode(), e.getTargetNode(), e));
        return graph;
    }

    @Before
    public void prepareTest() throws Exception {
        whenNew(DatabaseReader.class).withAnyArguments().thenReturn(dbReader);

        graphElementFactory = new GraphElementFactory(Arrays.asList(A.class, B.class));
    }

    @Test
    public void testCompareGraphsForEmptyExpectedGraphAndEmptyGivenGraph() throws SQLException {
        // GIVEN
        final Graph<Node, Edge> expectedGraph = createGraph(Collections.emptyList(), Collections.emptyList());
        final Graph<Node, Edge> givenGraph = createGraph(Collections.emptyList(), Collections.emptyList());

        final GraphComparator comparator = new GraphComparator(graphElementFactory, new String[] {}, false);
        when(dbReader.readGraph(connection)).thenReturn(givenGraph);

        // WHEN
        comparator.compare(connection, expectedGraph, errorCollector);

        // THEN
        verifyZeroInteractions(errorCollector);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCompareGraphsForEmptyExpectedGraphAndNotEmptyGivenGraph() throws Exception {
        // GIVEN
        final Node a = graphElementFactory.createNode("a", Arrays.asList("A"),
                ImmutableMap.<String, Object>builder().put("id", 1l).build());
        final Node b = graphElementFactory.createNode("b", Arrays.asList("B"),
                ImmutableMap.<String, Object>builder().put("id", 2l).build());
        final Edge e = graphElementFactory.createEdge(a, b, "e", Arrays.asList("E"), Collections.emptyMap());

        final Graph<Node, Edge> expectedGraph = createGraph(Collections.emptyList(), Collections.emptyList());
        final Graph<Node, Edge> givenGraph = createGraph(Arrays.asList(a, b), Arrays.asList(e));

        final GraphComparator comparator = new GraphComparator(graphElementFactory, new String[] {}, false);
        when(dbReader.readGraph(connection)).thenReturn(givenGraph);

        // WHEN
        comparator.compare(connection, expectedGraph, errorCollector);

        // THEN
        final ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(errorCollector, times(2)).collect(stringCaptor.capture());

        final List<String> capturedMessages = stringCaptor.getAllValues();
        assertThat(capturedMessages, hasItems(containsString("No nodes with A labels were expected, but there are <1> nodes present"),
                containsString("No nodes with B labels were expected, but there are <1> nodes present")));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCompareGraphsForNotEmptyExpectedGraphButEmptyGivenGraph() throws Exception {
        // GIVEN
        final Node a = graphElementFactory.createNode("a", Arrays.asList("A"),
                ImmutableMap.<String, Object>builder().put("id", 1l).build());
        final Node b = graphElementFactory.createNode("b", Arrays.asList("B"),
                ImmutableMap.<String, Object>builder().put("id", 2l).build());
        final Edge e = graphElementFactory.createEdge(a, b, "e", Arrays.asList("E"), Collections.emptyMap());

        final Graph<Node, Edge> expectedGraph = createGraph(Arrays.asList(a, b), Arrays.asList(e));
        final Graph<Node, Edge> givenGraph = createGraph(Collections.emptyList(), Collections.emptyList());

        final GraphComparator comparator = new GraphComparator(graphElementFactory, new String[] {}, false);
        when(dbReader.readGraph(connection)).thenReturn(givenGraph);

        // WHEN
        comparator.compare(connection, expectedGraph, errorCollector);

        // THEN
        final ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(errorCollector, times(5)).collect(stringCaptor.capture());

        final List<String> capturedMessages = stringCaptor.getAllValues();
        assertThat(capturedMessages,
                hasItems(containsString("Nodes with A labels were expected to be present, but not found"),
                        containsString("Nodes with B labels were expected to be present, but not found"),
                        containsString(a.asString() + " was expected, but is not present"),
                        containsString(b.asString() + " was expected, but is not present"),
                        containsString(e.asString() + " was expected, but is not present")));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCompareFullyDistinctGraphsInNotStrictMode() throws Exception {
        // GIVEN
        final Node a1 = graphElementFactory.createNode("a1", Arrays.asList("A"),
                ImmutableMap.<String, Object>builder().put("id", 1l).build());
        final Node a2 = graphElementFactory.createNode("a2", Arrays.asList("A"),
                ImmutableMap.<String, Object>builder().put("id", 2l).build());
        final Edge e1 = graphElementFactory.createEdge(a1, a2, "e", Arrays.asList("E"), Collections.emptyMap());

        final Node b1 = graphElementFactory.createNode("b1", Arrays.asList("B"),
                ImmutableMap.<String, Object>builder().put("id", 1l).build());
        final Node b2 = graphElementFactory.createNode("b2", Arrays.asList("B"),
                ImmutableMap.<String, Object>builder().put("id", 2l).build());
        final Edge f2 = graphElementFactory.createEdge(b1, b2, "f", Arrays.asList("F"), Collections.emptyMap());

        final Graph<Node, Edge> expectedGraph = createGraph(Arrays.asList(a1, a2), Arrays.asList(e1));
        final Graph<Node, Edge> givenGraph = createGraph(Arrays.asList(b1, b2), Arrays.asList(f2));

        final GraphComparator comparator = new GraphComparator(graphElementFactory, new String[] {}, false);
        when(dbReader.readGraph(connection)).thenReturn(givenGraph);

        // WHEN
        comparator.compare(connection, expectedGraph, errorCollector);

        // THEN
        final ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(errorCollector, times(4)).collect(stringCaptor.capture());

        final List<String> capturedMessages = stringCaptor.getAllValues();
        assertThat(capturedMessages,
                hasItems(containsString("Nodes with A labels were expected to be present, but not found"),
                        containsString(a1.asString() + " was expected, but is not present"),
                        containsString(a2.asString() + " was expected, but is not present"),
                        containsString(e1.asString() + " was expected, but is not present")));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCompareFullyDistinctGraphsInStrictMode() throws Exception {
        // GIVEN
        final Node a1 = graphElementFactory.createNode("a1", Arrays.asList("A"),
                ImmutableMap.<String, Object>builder().put("id", 1l).build());
        final Node a2 = graphElementFactory.createNode("a2", Arrays.asList("A"),
                ImmutableMap.<String, Object>builder().put("id", 2l).build());
        final Edge e1 = graphElementFactory.createEdge(a1, a2, "e", Arrays.asList("E"), Collections.emptyMap());

        final Node b1 = graphElementFactory.createNode("b1", Arrays.asList("B"),
                ImmutableMap.<String, Object>builder().put("id", 1l).build());
        final Node b2 = graphElementFactory.createNode("b2", Arrays.asList("B"),
                ImmutableMap.<String, Object>builder().put("id", 2l).build());
        final Edge f2 = graphElementFactory.createEdge(b1, b2, "f", Arrays.asList("F"), Collections.emptyMap());

        final Graph<Node, Edge> expectedGraph = createGraph(Arrays.asList(a1, a2), Arrays.asList(e1));
        final Graph<Node, Edge> givenGraph = createGraph(Arrays.asList(b1, b2), Arrays.asList(f2));

        final GraphComparator comparator = new GraphComparator(graphElementFactory, new String[] {}, true);
        when(dbReader.readGraph(connection)).thenReturn(givenGraph);

        // WHEN
        comparator.compare(connection, expectedGraph, errorCollector);

        // THEN
        final ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(errorCollector, times(5)).collect(stringCaptor.capture());

        final List<String> capturedMessages = stringCaptor.getAllValues();
        assertThat(capturedMessages,
                hasItems(containsString("Nodes with A labels were expected to be present, but not found"),
                        containsString(a1.asString() + " was expected, but is not present"),
                        containsString(a2.asString() + " was expected, but is not present"),
                        containsString(e1.asString() + " was expected, but is not present"),
                        containsString("Nodes with B labels were not expected, but are present")));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCompareGivenGraphIsASubsetOfExpectedGraphOnNodeLevel() throws Exception {
        // GIVEN
        final Node a1 = graphElementFactory.createNode("a1", Arrays.asList("A"),
                ImmutableMap.<String, Object>builder().put("id", 1l).build());
        final Node a2 = graphElementFactory.createNode("a2", Arrays.asList("A"),
                ImmutableMap.<String, Object>builder().put("id", 2l).build());
        final Node a3 = graphElementFactory.createNode("a3", Arrays.asList("A"),
                ImmutableMap.<String, Object>builder().put("id", 3l).build());
        final Edge e1 = graphElementFactory.createEdge(a1, a2, "e", Arrays.asList("E"), Collections.emptyMap());

        final Graph<Node, Edge> expectedGraph = createGraph(Arrays.asList(a1, a2, a3), Arrays.asList(e1));
        final Graph<Node, Edge> givenGraph = createGraph(Arrays.asList(a1, a2), Arrays.asList(e1));

        final GraphComparator comparator = new GraphComparator(graphElementFactory, new String[] {}, false);
        when(dbReader.readGraph(connection)).thenReturn(givenGraph);

        // WHEN
        comparator.compare(connection, expectedGraph, errorCollector);

        // THEN
        final ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(errorCollector).collect(stringCaptor.capture());

        final List<String> capturedMessages = stringCaptor.getAllValues();
        assertThat(capturedMessages, hasItems(containsString(a3.asString() + " was expected, but is not present")));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCompareGivenGraphIsASubsetOfExpectedGraphOnEdgeLevel() throws Exception {
        // GIVEN
        final Node a1 = graphElementFactory.createNode("a1", Arrays.asList("A"),
                ImmutableMap.<String, Object>builder().put("id", 1l).build());
        final Node a2 = graphElementFactory.createNode("a2", Arrays.asList("A"),
                ImmutableMap.<String, Object>builder().put("id", 2l).build());
        final Node a3 = graphElementFactory.createNode("a3", Arrays.asList("A"),
                ImmutableMap.<String, Object>builder().put("id", 3l).build());
        final Edge e1 = graphElementFactory.createEdge(a1, a2, "e1", Arrays.asList("E"), Collections.emptyMap());
        final Edge e2 = graphElementFactory.createEdge(a2, a3, "e2", Arrays.asList("E"), Collections.emptyMap());

        final Graph<Node, Edge> expectedGraph = createGraph(Arrays.asList(a1, a2, a3), Arrays.asList(e1, e2));
        final Graph<Node, Edge> givenGraph = createGraph(Arrays.asList(a1, a2, a3), Arrays.asList(e1));

        final GraphComparator comparator = new GraphComparator(graphElementFactory, new String[] {}, false);
        when(dbReader.readGraph(connection)).thenReturn(givenGraph);

        // WHEN
        comparator.compare(connection, expectedGraph, errorCollector);

        // THEN
        final ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(errorCollector).collect(stringCaptor.capture());

        final List<String> capturedMessages = stringCaptor.getAllValues();
        assertThat(capturedMessages, hasItems(containsString(e2.asString() + " was expected, but is not present")));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCompareGivenGraphBuildsIntersectionWithTheExpectedGraphInNotStrictMode() throws Exception {
        // GIVEN
        final Node a1 = graphElementFactory.createNode("a1", Arrays.asList("A"),
                ImmutableMap.<String, Object>builder().put("id", 1l).build());
        final Node a2 = graphElementFactory.createNode("a2", Arrays.asList("A"),
                ImmutableMap.<String, Object>builder().put("id", 2l).build());
        final Node a3 = graphElementFactory.createNode("a3", Arrays.asList("A"),
                ImmutableMap.<String, Object>builder().put("id", 3l).build());
        final Node a4 = graphElementFactory.createNode("a4", Arrays.asList("A"),
                ImmutableMap.<String, Object>builder().put("id", 4l).build());
        final Node b1 = graphElementFactory.createNode("b1", Arrays.asList("B"),
                ImmutableMap.<String, Object>builder().put("id", 1l).build());
        final Node b2 = graphElementFactory.createNode("b2", Arrays.asList("B"),
                ImmutableMap.<String, Object>builder().put("id", 2l).build());
        final Edge e1 = graphElementFactory.createEdge(a1, a2, "e1", Arrays.asList("E"), Collections.emptyMap());
        final Edge e2 = graphElementFactory.createEdge(a2, a3, "e2", Arrays.asList("E"), Collections.emptyMap());
        final Edge e3 = graphElementFactory.createEdge(a4, a2, "e3", Arrays.asList("E"), Collections.emptyMap());
        final Edge f1 = graphElementFactory.createEdge(b1, b2, "f1", Arrays.asList("F"), Collections.emptyMap());
        final Edge f2 = graphElementFactory.createEdge(b2, a1, "f2", Arrays.asList("F"), Collections.emptyMap());

        final Graph<Node, Edge> expectedGraph = createGraph(Arrays.asList(a1, a2, a3), Arrays.asList(e1, e2));
        final Graph<Node, Edge> givenGraph = createGraph(Arrays.asList(a1, a2, a4, b1, b2), Arrays.asList(e1, e3, f1, f2));

        final GraphComparator comparator = new GraphComparator(graphElementFactory, new String[] {}, false);
        when(dbReader.readGraph(connection)).thenReturn(givenGraph);

        // WHEN
        comparator.compare(connection, expectedGraph, errorCollector);

        // THEN
        final ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(errorCollector, times(4)).collect(stringCaptor.capture());

        final List<String> capturedMessages = stringCaptor.getAllValues();
        assertThat(capturedMessages,
                hasItems(containsString(a4.asString() + " was not expected, but is present"),
                        containsString(a3.asString() + " was expected, but is not present"),
                        containsString(e3.asString() + " was not expected, but is present"),
                        containsString(e2.asString() + " was expected, but is not present")));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCompareGivenGraphBuildsIntersectionWithTheExpectedGraphInStrictMode() throws Exception {
        // GIVEN
        final Node a1 = graphElementFactory.createNode("a1", Arrays.asList("A"),
                ImmutableMap.<String, Object>builder().put("id", 1l).build());
        final Node a2 = graphElementFactory.createNode("a2", Arrays.asList("A"),
                ImmutableMap.<String, Object>builder().put("id", 2l).build());
        final Node a3 = graphElementFactory.createNode("a3", Arrays.asList("A"),
                ImmutableMap.<String, Object>builder().put("id", 3l).build());
        final Node a4 = graphElementFactory.createNode("a4", Arrays.asList("A"),
                ImmutableMap.<String, Object>builder().put("id", 4l).build());
        final Node b1 = graphElementFactory.createNode("b1", Arrays.asList("B"),
                ImmutableMap.<String, Object>builder().put("id", 1l).build());
        final Node b2 = graphElementFactory.createNode("b2", Arrays.asList("B"),
                ImmutableMap.<String, Object>builder().put("id", 2l).build());
        final Edge e1 = graphElementFactory.createEdge(a1, a2, "e1", Arrays.asList("E"), Collections.emptyMap());
        final Edge e2 = graphElementFactory.createEdge(a2, a3, "e2", Arrays.asList("E"), Collections.emptyMap());
        final Edge e3 = graphElementFactory.createEdge(a4, a2, "e3", Arrays.asList("E"), Collections.emptyMap());
        final Edge f1 = graphElementFactory.createEdge(b1, b2, "f1", Arrays.asList("F"), Collections.emptyMap());
        final Edge f2 = graphElementFactory.createEdge(b2, a1, "f2", Arrays.asList("F"), Collections.emptyMap());

        final Graph<Node, Edge> expectedGraph = createGraph(Arrays.asList(a1, a2, a3), Arrays.asList(e1, e2));
        final Graph<Node, Edge> givenGraph = createGraph(Arrays.asList(a1, a2, a4, b1, b2), Arrays.asList(e1, e3, f1, f2));

        final GraphComparator comparator = new GraphComparator(graphElementFactory, new String[] {}, true);
        when(dbReader.readGraph(connection)).thenReturn(givenGraph);

        // WHEN
        comparator.compare(connection, expectedGraph, errorCollector);

        // THEN
        final ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(errorCollector, times(5)).collect(stringCaptor.capture());

        final List<String> capturedMessages = stringCaptor.getAllValues();
        assertThat(capturedMessages,
                hasItems(containsString(e2.asString() + " was expected, but is not present"),
                        containsString(e3.asString() + " was not expected, but is present"),
                        containsString(a4.asString() + " was not expected, but is present"),
                        containsString(a3.asString() + " was expected, but is not present"),
                        containsString("Nodes with B labels were not expected, but are present")));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCompareGraphsWithAmbiguitiesOnNodeLevelGivenByFieldsToBeExcluded() throws Exception {
        // GIVEN
        final Node a1 = graphElementFactory.createNode("a1", Arrays.asList("A"),
                ImmutableMap.<String, Object>builder().put("id", 1l).build());
        final Node a2 = graphElementFactory.createNode("a2", Arrays.asList("A"),
                ImmutableMap.<String, Object>builder().put("id", 2l).build());
        final Node a3 = graphElementFactory.createNode("a3", Arrays.asList("A"),
                ImmutableMap.<String, Object>builder().put("id", 3l).build());
        final Edge e1 = graphElementFactory.createEdge(a1, a2, "e1", Arrays.asList("E"),
                ImmutableMap.<String, Object>builder().put("value", "foo").build());
        final Edge e2 = graphElementFactory.createEdge(a2, a3, "e2", Arrays.asList("E"), Collections.emptyMap());

        final Node b1 = graphElementFactory.createNode("b1", Arrays.asList("A"),
                ImmutableMap.<String, Object>builder().put("id", 1l).build());
        final Node b2 = graphElementFactory.createNode("b2", Arrays.asList("A"),
                ImmutableMap.<String, Object>builder().put("id", 2l).build());
        final Node b3 = graphElementFactory.createNode("b3", Arrays.asList("A"),
                ImmutableMap.<String, Object>builder().put("id", 3l).build());
        final Edge f1 = graphElementFactory.createEdge(b1, b2, "f1", Arrays.asList("E"),
                ImmutableMap.<String, Object>builder().put("value", "foo").build());
        final Edge f2 = graphElementFactory.createEdge(b2, b3, "f2", Arrays.asList("E"), Collections.emptyMap());

        final Graph<Node, Edge> expectedGraph = createGraph(Arrays.asList(a1, a2, a3), Arrays.asList(e1, e2));
        final Graph<Node, Edge> givenGraph = createGraph(Arrays.asList(b1, b2, b3), Arrays.asList(f1, f2));

        final GraphComparator comparator = new GraphComparator(graphElementFactory, new String[] {
                "id"
        }, false);
        when(dbReader.readGraph(connection)).thenReturn(givenGraph);

        // WHEN
        comparator.compare(connection, expectedGraph, errorCollector);

        // THEN
        final ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(errorCollector, times(4)).collect(stringCaptor.capture());

        final List<String> capturedMessages = stringCaptor.getAllValues();
        assertThat(capturedMessages,
                hasItems(containsString("Ambiguouty detected for node " + a1.asString()),
                        containsString("Ambiguouty detected for node " + a2.asString()),
                        containsString("Ambiguouty detected for node " + a3.asString()),
                        // this is because we filter the id attribute. Thus source and target nodes
                        // of f1 and f2 cannot be distinguished
                        containsString("Ambiguouty detected for edge " + e2.asString())));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCompareGraphsWithAmbiguitiesOnEdgeLevelGivenByFieldsToBeExcluded() throws Exception {
        // GIVEN
        final Node a1 = graphElementFactory.createNode("a1", Arrays.asList("A"),
                ImmutableMap.<String, Object>builder().put("id", 1l).build());
        final Node a2 = graphElementFactory.createNode("a2", Arrays.asList("A"),
                ImmutableMap.<String, Object>builder().put("id", 2l).build());
        final Node a3 = graphElementFactory.createNode("a3", Arrays.asList("A"),
                ImmutableMap.<String, Object>builder().put("id", 3l).build());
        final Edge e1 = graphElementFactory.createEdge(a1, a2, "e1", Arrays.asList("E"), Collections.emptyMap());
        final Edge e2 = graphElementFactory.createEdge(a2, a3, "e2", Arrays.asList("E"), Collections.emptyMap());

        final Node b1 = graphElementFactory.createNode("b1", Arrays.asList("A"),
                ImmutableMap.<String, Object>builder().put("id", 1l).build());
        final Node b2 = graphElementFactory.createNode("b2", Arrays.asList("A"),
                ImmutableMap.<String, Object>builder().put("id", 2l).build());
        final Node b3 = graphElementFactory.createNode("b3", Arrays.asList("A"),
                ImmutableMap.<String, Object>builder().put("id", 3l).build());
        final Edge f1 = graphElementFactory.createEdge(b1, b2, "f1", Arrays.asList("E"), Collections.emptyMap());
        final Edge f2 = graphElementFactory.createEdge(b2, b3, "f2", Arrays.asList("E"), Collections.emptyMap());

        final Graph<Node, Edge> expectedGraph = createGraph(Arrays.asList(a1, a2, a3), Arrays.asList(e1, e2));
        final Graph<Node, Edge> givenGraph = createGraph(Arrays.asList(b1, b2, b3), Arrays.asList(f1, f2));

        final GraphComparator comparator = new GraphComparator(graphElementFactory, new String[] {
                "id"
        }, false);
        when(dbReader.readGraph(connection)).thenReturn(givenGraph);

        // WHEN
        comparator.compare(connection, expectedGraph, errorCollector);

        // THEN
        final ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(errorCollector, times(5)).collect(stringCaptor.capture());

        final List<String> capturedMessages = stringCaptor.getAllValues();
        assertThat(capturedMessages,
                hasItems(containsString("Ambiguouty detected for node " + a1.asString()),
                        containsString("Ambiguouty detected for node " + a2.asString()),
                        containsString("Ambiguouty detected for node " + a3.asString()),
                        containsString("Ambiguouty detected for edge " + e1.asString()),
                        containsString("Ambiguouty detected for edge " + e2.asString())));
    }
}
