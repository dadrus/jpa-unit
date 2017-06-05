package eu.drus.jpa.unit.mongodb.operation;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;

@RunWith(MockitoJUnitRunner.class)
public class RefreshOperationTest {

    private static final String USER_DEFINED_COLLECTION_NAME_1 = "some_collection_1";
    private static final String USER_DEFINED_COLLECTION_NAME_2 = "some_collection_2";
    private static final Document DOCUMENT_1 = new Document().append("_id", "id1").append("key1", "val1");
    private static final Document DOCUMENT_2 = new Document().append("_id", "id2").append("key2", "val2");

    @Mock
    private MongoDatabase connection;

    @Mock
    private MongoCollection<Document> collection;

    @Mock
    private UpdateResult result;

    private RefreshOperation operation = new RefreshOperation();

    @Before
    public void prepareMocks() {
        when(connection.getCollection(anyString())).thenReturn(collection);
        when(collection.replaceOne(any(Bson.class), any(Document.class))).thenReturn(result);
    }

    @Test
    public void testDocumentIsInsertedIfNotKnown() {
        // GIVEN
        when(result.getMatchedCount()).thenReturn(Long.valueOf(0));

        final Document data = new Document().append(USER_DEFINED_COLLECTION_NAME_2, Arrays.asList(DOCUMENT_1))
                .append(USER_DEFINED_COLLECTION_NAME_1, Arrays.asList(DOCUMENT_2));

        // WHEN
        operation.execute(connection, data);

        // THEN
        verify(collection, times(2)).replaceOne(any(Bson.class), any(Document.class));

        final ArgumentCaptor<Document> documentCaptor = ArgumentCaptor.forClass(Document.class);
        verify(collection, times(2)).insertOne(documentCaptor.capture());

        final List<Document> capturedDocuments = documentCaptor.getAllValues();
        assertThat(capturedDocuments.size(), equalTo(2));
        assertThat(capturedDocuments, hasItems(DOCUMENT_1, DOCUMENT_2));
    }

    @Test
    public void testDocumentIsReplacedIfKnown() {
        // GIVEN
        when(result.getMatchedCount()).thenReturn(Long.valueOf(1));

        final Document data = new Document().append(USER_DEFINED_COLLECTION_NAME_2, Arrays.asList(DOCUMENT_1))
                .append(USER_DEFINED_COLLECTION_NAME_1, Arrays.asList(DOCUMENT_2));

        // WHEN
        operation.execute(connection, data);

        // THEN
        final ArgumentCaptor<Document> documentCaptor = ArgumentCaptor.forClass(Document.class);
        verify(collection, times(2)).replaceOne(any(Bson.class), documentCaptor.capture());

        final List<Document> capturedDocuments = documentCaptor.getAllValues();
        assertThat(capturedDocuments.size(), equalTo(2));
        assertThat(capturedDocuments, hasItems(DOCUMENT_1, DOCUMENT_2));

        verify(collection, never()).insertOne(any(Document.class));
    }
}
