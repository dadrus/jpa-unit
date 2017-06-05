package eu.drus.jpa.unit.mongodb.operation;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
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
public class DeleteOperationTest {

    private static final String COLLECTION_NAME_1 = "some_collection_1";
    private static final String COLLECTION_NAME_2 = "some_collection_2";
    private static final Document DOCUMENT_1 = new Document().append("a", "A");
    private static final Document DOCUMENT_2 = new Document().append("b", "B");

    @Mock
    private MongoDatabase connection;

    @Mock
    private MongoCollection<Document> collection;

    private DeleteOperation operation = new DeleteOperation();

    @Before
    public void prepareMocks() {
        when(connection.getCollection(anyString())).thenReturn(collection);
    }

    @Test
    public void testOperationExecution() {
        // GIVEN
        final List<Document> values1 = Arrays.asList(DOCUMENT_1);
        final List<Document> values2 = Arrays.asList(DOCUMENT_2);
        final Document data = new Document().append(COLLECTION_NAME_1, values1).append(COLLECTION_NAME_2, values2);

        // WHEN
        operation.execute(connection, data);

        // THEN
        final ArgumentCaptor<String> collectionNameCaptor = ArgumentCaptor.forClass(String.class);
        verify(connection, times(2)).getCollection(collectionNameCaptor.capture());

        final List<String> capturedCollectionNames = collectionNameCaptor.getAllValues();
        assertThat(capturedCollectionNames.size(), equalTo(2));
        assertThat(capturedCollectionNames, hasItems(COLLECTION_NAME_1, COLLECTION_NAME_2));

        final ArgumentCaptor<Document> documentCaptor = ArgumentCaptor.forClass(Document.class);
        verify(collection, times(2)).deleteOne(documentCaptor.capture());

        final List<Document> capturedDocuments = documentCaptor.getAllValues();
        assertThat(capturedDocuments.size(), equalTo(2));
        assertThat(capturedDocuments, hasItems(DOCUMENT_1, DOCUMENT_2));
    }
}
