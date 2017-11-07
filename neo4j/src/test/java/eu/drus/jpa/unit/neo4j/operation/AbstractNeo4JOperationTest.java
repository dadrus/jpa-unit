package eu.drus.jpa.unit.neo4j.operation;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AbstractNeo4JOperationTest {

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement ps;

    @Mock
    private ResultSet rs;

    @Spy
    private AbstractNeo4JOperation operation;

    @Before
    public void prepareMocks() throws SQLException {
        when(ps.executeQuery()).thenReturn(rs);
        when(connection.prepareStatement(anyString())).thenReturn(ps);
    }

    @Test
    public void testExecuteQuery() throws SQLException {
        // GIVEN
        final String query = "some query";

        // WHEN
        operation.executeQuery(connection, query);

        // THEN
        final InOrder order = inOrder(connection, ps, rs);
        order.verify(connection).prepareStatement(eq(query));
        order.verify(ps).executeQuery();
        order.verify(rs).close();
        order.verify(ps).close();
    }
}
