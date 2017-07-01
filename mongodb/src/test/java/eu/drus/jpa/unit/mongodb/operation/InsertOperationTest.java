package eu.drus.jpa.unit.mongodb.operation;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.bson.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;

@RunWith(MockitoJUnitRunner.class)
public class InsertOperationTest {

    private static final String COLLECTION_NAME_1 = "some_collection_1";
    private static final String COLLECTION_NAME_2 = "some_collection_2";
    private static final Document DOCUMENT_1 = new Document().append("a", "A").append("b", "B");
    private static final Document DOCUMENT_2 = new Document().append("c", "C").append("d", "D");

    @Mock
    private MongoDatabase connection;

    @Mock
    private MongoCollection<Document> collection;

    @Captor
    private ArgumentCaptor<String> collectionNameCaptor;

    @Captor
    private ArgumentCaptor<List<Document>> documentCaptor;

    private InsertOperation operation = new InsertOperation();

    @Before
    public void prepareMocks() {
        when(connection.getCollection(anyString())).thenReturn(collection);
    }

    @Test
    public void testInsertNotStructuredData() {
        // GIVEN
        final List<Document> values1 = Arrays.asList(DOCUMENT_1);
        final List<Document> values2 = Arrays.asList(DOCUMENT_2);
        final Document data = new Document().append(COLLECTION_NAME_1, values1).append(COLLECTION_NAME_2, values2);

        // WHEN
        operation.execute(connection, data);

        // THEN
        verify(connection, times(2)).getCollection(collectionNameCaptor.capture());

        final List<String> capturedCollectionNames = collectionNameCaptor.getAllValues();
        assertThat(capturedCollectionNames.size(), equalTo(2));
        assertThat(capturedCollectionNames, hasItems(COLLECTION_NAME_1, COLLECTION_NAME_2));

        verify(collection, times(2)).insertMany(documentCaptor.capture());

        final List<List<Document>> capturedDocuments = documentCaptor.getAllValues();
        assertThat(capturedDocuments.size(), equalTo(2));
        assertThat(capturedDocuments, hasItems(values1, values2));
    }

    @Test
    public void testInsertStructuredDataWithoutIndexDefinitions() {
        // GIVEN
        final List<Document> values1 = Arrays.asList(DOCUMENT_1);
        final Document doc1 = new Document().append("data", values1);
        final List<Document> values2 = Arrays.asList(DOCUMENT_2);
        final Document doc2 = new Document().append("data", values2);
        final Document data = new Document().append(COLLECTION_NAME_1, doc1).append(COLLECTION_NAME_2, doc2);

        // WHEN
        operation.execute(connection, data);

        // THEN
        verify(connection, times(2)).getCollection(collectionNameCaptor.capture());

        final List<String> capturedCollectionNames = collectionNameCaptor.getAllValues();
        assertThat(capturedCollectionNames.size(), equalTo(2));
        assertThat(capturedCollectionNames, hasItems(COLLECTION_NAME_1, COLLECTION_NAME_2));

        verify(collection, times(2)).insertMany(documentCaptor.capture());

        final List<List<Document>> capturedDocuments = documentCaptor.getAllValues();
        assertThat(capturedDocuments.size(), equalTo(2));
        assertThat(capturedDocuments, hasItems(values1, values2));
    }

    @Test
    public void testInsertStructuredDataWithIndexDefinitionsWithoutOptions() {
        // GIVEN
        final List<Document> values1 = Arrays.asList(DOCUMENT_1);
        final Document index1 = new Document().append("a", "1");
        final List<Document> indexes1 = Arrays.asList(new Document().append("index", index1));
        final Document doc1 = new Document().append("data", values1).append("indexes", indexes1);
        final List<Document> values2 = Arrays.asList(DOCUMENT_2);
        final Document index2 = new Document().append("c", "1");
        final List<Document> indexes2 = Arrays.asList(new Document().append("index", index2));
        final Document doc2 = new Document().append("data", values2).append("indexes", indexes2);
        final Document data = new Document().append(COLLECTION_NAME_1, doc1).append(COLLECTION_NAME_2, doc2);

        // WHEN
        operation.execute(connection, data);

        // THEN
        verify(connection, times(4)).getCollection(collectionNameCaptor.capture());

        final Collection<String> capturedCollectionNames = new HashSet<>(collectionNameCaptor.getAllValues());
        assertThat(capturedCollectionNames.size(), equalTo(2));
        assertThat(capturedCollectionNames, hasItems(COLLECTION_NAME_1, COLLECTION_NAME_2));

        final ArgumentCaptor<Document> indexCaptor = ArgumentCaptor.forClass(Document.class);
        verify(collection, times(2)).createIndex(indexCaptor.capture());
        final List<Document> capturedIndexes = indexCaptor.getAllValues();
        assertThat(capturedIndexes.size(), equalTo(2));
        assertThat(capturedIndexes, hasItems(index1, index2));

        verify(collection, times(2)).insertMany(documentCaptor.capture());

        final List<List<Document>> capturedDocuments = documentCaptor.getAllValues();
        assertThat(capturedDocuments.size(), equalTo(2));
        assertThat(capturedDocuments, hasItems(values1, values2));
    }

    @Test
    public void testInsertStructuredDataWithIndexDefinitionsAndOptions() {
        // GIVEN
        final List<Document> values1 = Arrays.asList(DOCUMENT_1);
        final Document index1 = new Document().append("a", "1");
        final Document options1 = new Document().append("default_language", "english").append("collation",
                new Document().append("locale", "simple").append("strength", new Integer(1)).append("caseLevel", true).append("alternate",
                        "non-ignorable"));
        final List<Document> indexes1 = Arrays.asList(new Document().append("index", index1).append("options", options1));
        final Document doc1 = new Document().append("data", values1).append("indexes", indexes1);
        final List<Document> values2 = Arrays.asList(DOCUMENT_2);
        final Document index2 = new Document().append("c", 1).append("d", -1);
        final Document options2 = new Document().append("backgound", true).append("unique", true).append("name", "foo")
                .append("expireAfterSeconds", new Long(30));
        final List<Document> indexes2 = Arrays.asList(new Document().append("index", index2).append("options", options2));
        final Document doc2 = new Document().append("data", values2).append("indexes", indexes2);
        final Document data = new Document().append(COLLECTION_NAME_1, doc1).append(COLLECTION_NAME_2, doc2);

        // WHEN
        operation.execute(connection, data);

        // THEN
        verify(connection, times(4)).getCollection(collectionNameCaptor.capture());

        final Collection<String> capturedCollectionNames = new HashSet<>(collectionNameCaptor.getAllValues());
        assertThat(capturedCollectionNames.size(), equalTo(2));
        assertThat(capturedCollectionNames, hasItems(COLLECTION_NAME_1, COLLECTION_NAME_2));

        final ArgumentCaptor<Document> indexCaptor = ArgumentCaptor.forClass(Document.class);
        final ArgumentCaptor<IndexOptions> optionsCaptor = ArgumentCaptor.forClass(IndexOptions.class);
        verify(collection, times(2)).createIndex(indexCaptor.capture(), optionsCaptor.capture());
        final List<Document> capturedIndexes = indexCaptor.getAllValues();
        assertThat(capturedIndexes.size(), equalTo(2));
        assertThat(capturedIndexes, hasItems(index1, index2));

        final List<IndexOptions> indexOptions = optionsCaptor.getAllValues();
        assertThat(indexOptions.size(), equalTo(2));

        verify(collection, times(2)).insertMany(documentCaptor.capture());

        final List<List<Document>> capturedDocuments = documentCaptor.getAllValues();
        assertThat(capturedDocuments.size(), equalTo(2));
        assertThat(capturedDocuments, hasItems(values1, values2));
    }
}
