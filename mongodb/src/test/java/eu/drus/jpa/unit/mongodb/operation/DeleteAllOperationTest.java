package eu.drus.jpa.unit.mongodb.operation;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

@RunWith(MockitoJUnitRunner.class)
public class DeleteAllOperationTest {

    private static final String USER_DEFINED_COLLECTION_NAME_1 = "some_collection_1";
    private static final String USER_DEFINED_COLLECTION_NAME_2 = "some_collection_2";

    @Mock
    private MongoDatabase connection;

    @Mock
    private MongoCollection<Document> collection;

    private DeleteAllOperation operation = new DeleteAllOperation();

    @Before
    public void prepareMocks() {
        when(connection.getCollection(anyString())).thenReturn(collection);
    }

    @Test
    public void testOperationExecution() {
        // GIVEN
        final Document data = new Document().append(USER_DEFINED_COLLECTION_NAME_1, Arrays.asList(new Document()))
                .append(USER_DEFINED_COLLECTION_NAME_2, Arrays.asList(new Document()));

        // WHEN
        operation.execute(connection, data);

        // THEN
        final ArgumentCaptor<String> collectionNameCaptor = ArgumentCaptor.forClass(String.class);
        verify(connection, times(2)).getCollection(collectionNameCaptor.capture());

        final List<String> capturedCollectionNames = collectionNameCaptor.getAllValues();
        assertThat(capturedCollectionNames.size(), equalTo(2));
        assertThat(capturedCollectionNames, hasItems(USER_DEFINED_COLLECTION_NAME_1, USER_DEFINED_COLLECTION_NAME_2));

        verify(collection, times(2)).deleteMany(eq(new Document()));
    }
}
