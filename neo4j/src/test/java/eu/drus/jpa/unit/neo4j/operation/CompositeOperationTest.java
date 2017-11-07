package eu.drus.jpa.unit.neo4j.operation;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;

import java.sql.Connection;
import java.sql.SQLException;

import org.jgrapht.Graph;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import eu.drus.jpa.unit.neo4j.dataset.Edge;
import eu.drus.jpa.unit.neo4j.dataset.Node;

@RunWith(MockitoJUnitRunner.class)
public class CompositeOperationTest {

    @Mock
    private Connection connection;

    @Mock
    private Neo4JOperation operation1;

    @Mock
    private Neo4JOperation operation2;

    @Mock
    private Neo4JOperation operation3;

    @Test
    public void testOperationExecution() throws SQLException {
        // GIVEN
        final Graph<Node, Edge> graph = new DefaultDirectedGraph<>(new ClassBasedEdgeFactory<>(Edge.class));
        final CompositeOperation operation = new CompositeOperation(operation1, operation2, operation3);

        // WHEN
        operation.execute(connection, graph);

        // THEN
        final InOrder order = inOrder(operation1, operation2, operation3);
        order.verify(operation1).execute(eq(connection), eq(graph));
        order.verify(operation2).execute(eq(connection), eq(graph));
        order.verify(operation3).execute(eq(connection), eq(graph));
    }
}
