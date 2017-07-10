package eu.drus.jpa.unit.mongodb.operation;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;

import org.bson.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.mongodb.client.MongoDatabase;

@RunWith(MockitoJUnitRunner.class)
public class CompositeOperationTest {

    @Mock
    private MongoDatabase connection;

    @Mock
    private MongoDbOperation operation1;

    @Mock
    private MongoDbOperation operation2;

    @Mock
    private MongoDbOperation operation3;

    @Test
    public void testOperationExecution() {
        // GIVEN
        final CompositeOperation operation = new CompositeOperation(operation1, operation2, operation3);
        final Document data = new Document();

        // WHEN
        operation.execute(connection, data);

        // THEN
        final InOrder order = inOrder(operation1, operation2, operation3);
        order.verify(operation1).execute(eq(connection), eq(data));
        order.verify(operation2).execute(eq(connection), eq(data));
        order.verify(operation3).execute(eq(connection), eq(data));
    }
}
