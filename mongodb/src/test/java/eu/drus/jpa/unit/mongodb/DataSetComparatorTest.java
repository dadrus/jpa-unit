package eu.drus.jpa.unit.mongodb;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import org.bson.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;

import eu.drus.jpa.unit.spi.AssertionErrorCollector;

@RunWith(MockitoJUnitRunner.class)
public class DataSetComparatorTest {

    private static final String COLLECTION_NAME_1 = "some_collection_1";
    private static final String COLLECTION_NAME_2 = "some_collection_2";
    private static final String COLLECTION_NAME_3 = "some_collection_3";
    private static final Document DOCUMENT_1 = new Document().append("a", "A").append("b", "B");
    private static final Document DOCUMENT_2 = new Document().append("c", "C").append("d", "D");
    private static final Document DOCUMENT_3 = new Document().append("e", "E").append("f", "F");

    @Mock
    private MongoDatabase mongoDatabase;

    private AssertionErrorCollector errorCollector;

    @Before
    public void prepareTest() {
        errorCollector = new AssertionErrorCollector();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCurrentDataSetContainsDataAndExpectedDataSetIsEmpty() {
        // GIVEN
        final List<String> collectionNames = Arrays.asList(COLLECTION_NAME_1);
        final Iterator<String> collectionNamesIt = collectionNames.iterator();
        final MongoCursor<String> iterator = mock(MongoCursor.class);
        when(iterator.hasNext()).thenAnswer((final InvocationOnMock invocation) -> collectionNamesIt.hasNext());
        when(iterator.next()).thenAnswer((final InvocationOnMock invocation) -> collectionNamesIt.next());

        final MongoIterable<String> collectionIterable = mock(MongoIterable.class);
        when(collectionIterable.iterator()).thenReturn(iterator);

        when(mongoDatabase.listCollectionNames()).thenReturn(collectionIterable);

        final MongoCollection<Document> collection = mock(MongoCollection.class);
        when(mongoDatabase.getCollection(eq(COLLECTION_NAME_1))).thenReturn(collection);
        when(collection.count()).thenReturn(1l);

        final String[] toExclude = new String[] {};
        final Document expectedDataSet = new Document();
        final DataSetComparator comparator = new DataSetComparator(toExclude, false);

        // WHEN
        comparator.compare(mongoDatabase, expectedDataSet, errorCollector);

        // THEN
        assertThat(errorCollector.amountOfErrors(), equalTo(1));

        try {
            errorCollector.report();
            fail("AssertionError expected");
        } catch (final AssertionError e) {
            assertThat(e.getMessage(), containsString("failed in 1 case"));
            assertThat(e.getMessage(), containsString(COLLECTION_NAME_1 + " was expected to be empty, but has <" + 1 + "> entries"));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCurrentDataSetIsEmptyAndExpectedDataContainsData() {
        // GIVEN
        final List<String> collectionNames = Collections.emptyList();
        final Iterator<String> collectionNamesIt = collectionNames.iterator();
        final MongoIterable<String> collectionIterable = mock(MongoIterable.class);
        final MongoCursor<String> iterator = mock(MongoCursor.class);
        when(mongoDatabase.listCollectionNames()).thenReturn(collectionIterable);
        when(collectionIterable.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenAnswer((final InvocationOnMock invocation) -> collectionNamesIt.hasNext());
        when(iterator.next()).thenAnswer((final InvocationOnMock invocation) -> collectionNamesIt.next());

        final MongoCursor<Document> documentIterator = mock(MongoCursor.class);
        when(documentIterator.hasNext()).thenReturn(Boolean.FALSE);

        final FindIterable<Document> findIterable = mock(FindIterable.class);
        when(findIterable.iterator()).thenReturn(documentIterator);

        final MongoCollection<Document> collection = mock(MongoCollection.class);
        when(collection.count()).thenReturn(0l);
        when(collection.find(any(Document.class))).thenReturn(findIterable);
        when(collection.find()).thenReturn(findIterable);
        when(mongoDatabase.getCollection(anyString())).thenReturn(collection);

        final String[] toExclude = new String[] {};
        final List<Document> values1 = Arrays.asList(DOCUMENT_1);
        final Document expectedDataSet = new Document().append(COLLECTION_NAME_1, values1);
        final DataSetComparator comparator = new DataSetComparator(toExclude, false);

        // WHEN
        comparator.compare(mongoDatabase, expectedDataSet, errorCollector);

        // THEN
        assertThat(errorCollector.amountOfErrors(), equalTo(2));

        try {
            errorCollector.report();
            fail("AssertionError expected");
        } catch (final AssertionError e) {
            assertThat(e.getMessage(), containsString("failed in 2 case"));
            assertThat(e.getMessage(), containsString(COLLECTION_NAME_1 + " was expected to be present, but not found"));
            assertThat(e.getMessage(), containsString(DOCUMENT_1 + " was expected in [" + COLLECTION_NAME_1 + "], but is not present"));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCurrentDataSetAndExpectedDataSetContainDataButAreFullyDisjunctiveUsingNotStrictMode() {
        // GIVEN
        final List<String> currentCollectionNames = Arrays.asList(COLLECTION_NAME_1);
        final Iterator<String> currentCollectionNamesIt = currentCollectionNames.iterator();
        final MongoCursor<String> currentCollectionNamesCursor = mock(MongoCursor.class);
        when(currentCollectionNamesCursor.hasNext()).thenAnswer((final InvocationOnMock invocation) -> currentCollectionNamesIt.hasNext());
        when(currentCollectionNamesCursor.next()).thenAnswer((final InvocationOnMock invocation) -> currentCollectionNamesIt.next());
        doAnswer((final InvocationOnMock invocation) -> {
            final Consumer<String> c = (Consumer<String>) invocation.getArguments()[0];
            for (final String name : currentCollectionNames) {
                c.accept(name);
            }
            return null;
        }).when(currentCollectionNamesCursor).forEachRemaining(any(Consumer.class));

        final MongoIterable<String> collectionIterable = mock(MongoIterable.class);
        when(collectionIterable.iterator()).thenReturn(currentCollectionNamesCursor);

        when(mongoDatabase.listCollectionNames()).thenReturn(collectionIterable);

        final MongoCursor<Document> documentIteratorWithoutEntries = mock(MongoCursor.class);
        when(documentIteratorWithoutEntries.hasNext()).thenReturn(Boolean.FALSE);

        final FindIterable<Document> findIterableWithoutEntries = mock(FindIterable.class);
        when(findIterableWithoutEntries.iterator()).thenReturn(documentIteratorWithoutEntries);

        final MongoCollection<Document> collectionWithoutEntries = mock(MongoCollection.class);
        when(collectionWithoutEntries.count()).thenReturn(0l);
        when(collectionWithoutEntries.find(any(Document.class))).thenReturn(findIterableWithoutEntries);
        when(collectionWithoutEntries.find()).thenReturn(findIterableWithoutEntries);
        when(mongoDatabase.getCollection(anyString())).thenReturn(collectionWithoutEntries);

        final List<Document> currentDocuments = Arrays.asList(DOCUMENT_1);
        final Iterator<Document> currentDocumentsIt = currentDocuments.iterator();
        final MongoCursor<Document> documentIteratorWithEntries = mock(MongoCursor.class);
        when(documentIteratorWithEntries.hasNext()).thenAnswer((final InvocationOnMock invocation) -> currentDocumentsIt.hasNext());
        when(documentIteratorWithEntries.next()).thenAnswer((final InvocationOnMock invocation) -> currentDocumentsIt.next());

        final FindIterable<Document> findIterableWithEntries = mock(FindIterable.class);
        when(findIterableWithEntries.iterator()).thenReturn(documentIteratorWithEntries);

        final MongoCollection<Document> collectionWithEntries = mock(MongoCollection.class);
        when(collectionWithEntries.count()).thenReturn(1l);
        when(collectionWithEntries.find(any(Document.class))).thenReturn(findIterableWithEntries);
        when(collectionWithEntries.find()).thenReturn(findIterableWithEntries);
        when(mongoDatabase.getCollection(eq(COLLECTION_NAME_1))).thenReturn(collectionWithEntries);

        final String[] toExclude = new String[] {};
        final List<Document> values2 = Arrays.asList(DOCUMENT_2);
        final Document expectedDataSet = new Document().append(COLLECTION_NAME_2, values2);

        final DataSetComparator comparator = new DataSetComparator(toExclude, false);

        // WHEN
        comparator.compare(mongoDatabase, expectedDataSet, errorCollector);

        // THEN
        assertThat(errorCollector.amountOfErrors(), equalTo(2));

        try {
            errorCollector.report();
            fail("AssertionError expected");
        } catch (final AssertionError e) {
            assertThat(e.getMessage(), containsString("failed in 2 cases"));
            assertThat(e.getMessage(), containsString(COLLECTION_NAME_2 + " was expected to be present, but not found"));
            assertThat(e.getMessage(), containsString(DOCUMENT_2 + " was expected in [" + COLLECTION_NAME_2 + "], but is not present"));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCurrentDataSetAndExpectedDataSetContainDataButAreFullyDisjunctiveUsingStrictMode() {
        // GIVEN
        final List<String> currentCollectionNames = Arrays.asList(COLLECTION_NAME_1);
        final Iterator<String> currentCollectionNamesIt = currentCollectionNames.iterator();
        final MongoCursor<String> currentCollectionNamesCursor = mock(MongoCursor.class);
        when(currentCollectionNamesCursor.hasNext()).thenAnswer((final InvocationOnMock invocation) -> currentCollectionNamesIt.hasNext());
        when(currentCollectionNamesCursor.next()).thenAnswer((final InvocationOnMock invocation) -> currentCollectionNamesIt.next());
        doAnswer((final InvocationOnMock invocation) -> {
            final Consumer<String> c = (Consumer<String>) invocation.getArguments()[0];
            for (final String name : currentCollectionNames) {
                c.accept(name);
            }
            return null;
        }).when(currentCollectionNamesCursor).forEachRemaining(any(Consumer.class));

        final MongoIterable<String> collectionIterable = mock(MongoIterable.class);
        when(collectionIterable.iterator()).thenReturn(currentCollectionNamesCursor);

        when(mongoDatabase.listCollectionNames()).thenReturn(collectionIterable);

        final MongoCursor<Document> documentIteratorWithoutEntries = mock(MongoCursor.class);
        when(documentIteratorWithoutEntries.hasNext()).thenReturn(Boolean.FALSE);

        final FindIterable<Document> findIterableWithoutEntries = mock(FindIterable.class);
        when(findIterableWithoutEntries.iterator()).thenReturn(documentIteratorWithoutEntries);

        final MongoCollection<Document> collectionWithoutEntries = mock(MongoCollection.class);
        when(collectionWithoutEntries.count()).thenReturn(0l);
        when(collectionWithoutEntries.find(any(Document.class))).thenReturn(findIterableWithoutEntries);
        when(collectionWithoutEntries.find()).thenReturn(findIterableWithoutEntries);
        when(mongoDatabase.getCollection(anyString())).thenReturn(collectionWithoutEntries);

        final List<Document> currentDocuments = Arrays.asList(DOCUMENT_1);
        final Iterator<Document> currentDocumentsIt = currentDocuments.iterator();
        final MongoCursor<Document> documentIteratorWithEntries = mock(MongoCursor.class);
        when(documentIteratorWithEntries.hasNext()).thenAnswer((final InvocationOnMock invocation) -> currentDocumentsIt.hasNext());
        when(documentIteratorWithEntries.next()).thenAnswer((final InvocationOnMock invocation) -> currentDocumentsIt.next());

        final FindIterable<Document> findIterableWithEntries = mock(FindIterable.class);
        when(findIterableWithEntries.iterator()).thenReturn(documentIteratorWithEntries);

        final MongoCollection<Document> collectionWithEntries = mock(MongoCollection.class);
        when(collectionWithEntries.count()).thenReturn(1l);
        when(collectionWithEntries.find(any(Document.class))).thenReturn(findIterableWithEntries);
        when(collectionWithEntries.find()).thenReturn(findIterableWithEntries);
        when(mongoDatabase.getCollection(eq(COLLECTION_NAME_1))).thenReturn(collectionWithEntries);

        final String[] toExclude = new String[] {};
        final List<Document> values2 = Arrays.asList(DOCUMENT_2);
        final Document expectedDataSet = new Document().append(COLLECTION_NAME_2, values2);

        final DataSetComparator comparator = new DataSetComparator(toExclude, true);

        // WHEN
        comparator.compare(mongoDatabase, expectedDataSet, errorCollector);

        // THEN
        assertThat(errorCollector.amountOfErrors(), equalTo(3));

        try {
            errorCollector.report();
            fail("AssertionError expected");
        } catch (final AssertionError e) {
            assertThat(e.getMessage(), containsString("failed in 3 cases"));
            assertThat(e.getMessage(), containsString(COLLECTION_NAME_2 + " was expected to be present, but not found"));
            assertThat(e.getMessage(), containsString(DOCUMENT_2 + " was expected in [" + COLLECTION_NAME_2 + "], but is not present"));
            assertThat(e.getMessage(), containsString(COLLECTION_NAME_1 + " was not expected, but is present"));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCurrentDataSetAndExpectedDataSetHaveOnlyOneCollectionInCommonUsingNotStrictMode() {
        // GIVEN
        final List<String> currentCollectionNames = Arrays.asList(COLLECTION_NAME_1, COLLECTION_NAME_2);
        final Iterator<String> currentCollectionNamesIt = currentCollectionNames.iterator();
        final MongoCursor<String> currentCollectionNamesCursor = mock(MongoCursor.class);
        when(currentCollectionNamesCursor.hasNext()).thenAnswer((final InvocationOnMock invocation) -> currentCollectionNamesIt.hasNext());
        when(currentCollectionNamesCursor.next()).thenAnswer((final InvocationOnMock invocation) -> currentCollectionNamesIt.next());
        doAnswer((final InvocationOnMock invocation) -> {
            final Consumer<String> c = (Consumer<String>) invocation.getArguments()[0];
            for (final String name : currentCollectionNames) {
                c.accept(name);
            }
            return null;
        }).when(currentCollectionNamesCursor).forEachRemaining(any(Consumer.class));

        final MongoIterable<String> collectionIterable = mock(MongoIterable.class);
        when(collectionIterable.iterator()).thenReturn(currentCollectionNamesCursor);

        when(mongoDatabase.listCollectionNames()).thenReturn(collectionIterable);

        final MongoCursor<Document> documentIteratorWithoutEntries = mock(MongoCursor.class);
        when(documentIteratorWithoutEntries.hasNext()).thenReturn(Boolean.FALSE);

        final FindIterable<Document> findIterableWithoutEntries = mock(FindIterable.class);
        when(findIterableWithoutEntries.iterator()).thenReturn(documentIteratorWithoutEntries);

        final MongoCollection<Document> collectionWithoutEntries = mock(MongoCollection.class);
        when(collectionWithoutEntries.count()).thenReturn(0l);
        when(collectionWithoutEntries.find(any(Document.class))).thenReturn(findIterableWithoutEntries);
        when(collectionWithoutEntries.find()).thenReturn(findIterableWithoutEntries);
        when(mongoDatabase.getCollection(anyString())).thenReturn(collectionWithoutEntries);

        final List<Document> currentDocuments = Arrays.asList(DOCUMENT_1, DOCUMENT_2);
        final Iterator<Document> currentDocumentsIt = currentDocuments.iterator();
        final MongoCursor<Document> documentIteratorWithEntries = mock(MongoCursor.class);
        when(documentIteratorWithEntries.hasNext()).thenAnswer((final InvocationOnMock invocation) -> currentDocumentsIt.hasNext());
        when(documentIteratorWithEntries.next()).thenAnswer((final InvocationOnMock invocation) -> currentDocumentsIt.next());

        final FindIterable<Document> findIterableWithEntries = mock(FindIterable.class);
        when(findIterableWithEntries.iterator()).thenReturn(documentIteratorWithEntries);

        final MongoCollection<Document> collectionWithEntries = mock(MongoCollection.class);
        when(collectionWithEntries.count()).thenReturn(1l);
        when(collectionWithEntries.find(any(Document.class))).thenReturn(findIterableWithEntries);
        when(collectionWithEntries.find()).thenReturn(findIterableWithEntries);
        when(mongoDatabase.getCollection(eq(COLLECTION_NAME_1))).thenReturn(collectionWithEntries);

        final String[] toExclude = new String[] {};
        final Document expectedDataSet = new Document().append(COLLECTION_NAME_2, Arrays.asList(DOCUMENT_2)).append(COLLECTION_NAME_3,
                Arrays.asList(DOCUMENT_3));

        final DataSetComparator comparator = new DataSetComparator(toExclude, false);

        // WHEN
        comparator.compare(mongoDatabase, expectedDataSet, errorCollector);

        // THEN
        assertThat(errorCollector.amountOfErrors(), equalTo(3));

        try {
            errorCollector.report();
            fail("AssertionError expected");
        } catch (final AssertionError e) {
            assertThat(e.getMessage(), containsString("failed in 3 case"));
            assertThat(e.getMessage(), containsString(COLLECTION_NAME_3 + " was expected to be present, but not found"));
            assertThat(e.getMessage(), containsString(DOCUMENT_2 + " was expected in [" + COLLECTION_NAME_2 + "], but is not present"));
            assertThat(e.getMessage(), containsString(DOCUMENT_3 + " was expected in [" + COLLECTION_NAME_3 + "], but is not present"));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCurrentDataSetAndExpectedDataSetHaveOnlyOneCollectionInCommonUsingStrictMode() {
        // GIVEN
        final List<String> currentCollectionNames = Arrays.asList(COLLECTION_NAME_1, COLLECTION_NAME_2);
        final Iterator<String> currentCollectionNamesIt = currentCollectionNames.iterator();
        final MongoCursor<String> currentCollectionNamesCursor = mock(MongoCursor.class);
        when(currentCollectionNamesCursor.hasNext()).thenAnswer((final InvocationOnMock invocation) -> currentCollectionNamesIt.hasNext());
        when(currentCollectionNamesCursor.next()).thenAnswer((final InvocationOnMock invocation) -> currentCollectionNamesIt.next());
        doAnswer((final InvocationOnMock invocation) -> {
            final Consumer<String> c = (Consumer<String>) invocation.getArguments()[0];
            for (final String name : currentCollectionNames) {
                c.accept(name);
            }
            return null;
        }).when(currentCollectionNamesCursor).forEachRemaining(any(Consumer.class));

        final MongoIterable<String> collectionIterable = mock(MongoIterable.class);
        when(collectionIterable.iterator()).thenReturn(currentCollectionNamesCursor);

        when(mongoDatabase.listCollectionNames()).thenReturn(collectionIterable);

        final MongoCursor<Document> documentIteratorWithoutEntries = mock(MongoCursor.class);
        when(documentIteratorWithoutEntries.hasNext()).thenReturn(Boolean.FALSE);

        final FindIterable<Document> findIterableWithoutEntries = mock(FindIterable.class);
        when(findIterableWithoutEntries.iterator()).thenReturn(documentIteratorWithoutEntries);

        final MongoCollection<Document> collectionWithoutEntries = mock(MongoCollection.class);
        when(collectionWithoutEntries.count()).thenReturn(0l);
        when(collectionWithoutEntries.find(any(Document.class))).thenReturn(findIterableWithoutEntries);
        when(collectionWithoutEntries.find()).thenReturn(findIterableWithoutEntries);
        when(mongoDatabase.getCollection(anyString())).thenReturn(collectionWithoutEntries);

        final List<Document> currentDocuments = Arrays.asList(DOCUMENT_1, DOCUMENT_2);
        final Iterator<Document> currentDocumentsIt = currentDocuments.iterator();
        final MongoCursor<Document> documentIteratorWithEntries = mock(MongoCursor.class);
        when(documentIteratorWithEntries.hasNext()).thenAnswer((final InvocationOnMock invocation) -> currentDocumentsIt.hasNext());
        when(documentIteratorWithEntries.next()).thenAnswer((final InvocationOnMock invocation) -> currentDocumentsIt.next());

        final FindIterable<Document> findIterableWithEntries = mock(FindIterable.class);
        when(findIterableWithEntries.iterator()).thenReturn(documentIteratorWithEntries);

        final MongoCollection<Document> collectionWithEntries = mock(MongoCollection.class);
        when(collectionWithEntries.count()).thenReturn(1l);
        when(collectionWithEntries.find(any(Document.class))).thenReturn(findIterableWithEntries);
        when(collectionWithEntries.find()).thenReturn(findIterableWithEntries);
        when(mongoDatabase.getCollection(eq(COLLECTION_NAME_1))).thenReturn(collectionWithEntries);

        final String[] toExclude = new String[] {};
        final Document expectedDataSet = new Document().append(COLLECTION_NAME_2, Arrays.asList(DOCUMENT_2)).append(COLLECTION_NAME_3,
                Arrays.asList(DOCUMENT_3));

        final DataSetComparator comparator = new DataSetComparator(toExclude, true);

        // WHEN
        comparator.compare(mongoDatabase, expectedDataSet, errorCollector);

        // THEN
        assertThat(errorCollector.amountOfErrors(), equalTo(4));

        try {
            errorCollector.report();
            fail("AssertionError expected");
        } catch (final AssertionError e) {
            assertThat(e.getMessage(), containsString("failed in 4 case"));
            assertThat(e.getMessage(), containsString(COLLECTION_NAME_3 + " was expected to be present, but not found"));
            assertThat(e.getMessage(), containsString(DOCUMENT_2 + " was expected in [" + COLLECTION_NAME_2 + "], but is not present"));
            assertThat(e.getMessage(), containsString(DOCUMENT_3 + " was expected in [" + COLLECTION_NAME_3 + "], but is not present"));
            assertThat(e.getMessage(), containsString(COLLECTION_NAME_1 + " was not expected, but is present"));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCurrentDataSetIsASubsetOfExpectedDataSet() {
        // GIVEN
        final List<String> currentCollectionNames = Arrays.asList(COLLECTION_NAME_1);
        final Iterator<String> currentCollectionNamesIt = currentCollectionNames.iterator();
        final MongoCursor<String> currentCollectionNamesCursor = mock(MongoCursor.class);
        when(currentCollectionNamesCursor.hasNext()).thenAnswer((final InvocationOnMock invocation) -> currentCollectionNamesIt.hasNext());
        when(currentCollectionNamesCursor.next()).thenAnswer((final InvocationOnMock invocation) -> currentCollectionNamesIt.next());
        doAnswer((final InvocationOnMock invocation) -> {
            final Consumer<String> c = (Consumer<String>) invocation.getArguments()[0];
            for (final String name : currentCollectionNames) {
                c.accept(name);
            }
            return null;
        }).when(currentCollectionNamesCursor).forEachRemaining(any(Consumer.class));

        final MongoIterable<String> collectionIterable = mock(MongoIterable.class);
        when(collectionIterable.iterator()).thenReturn(currentCollectionNamesCursor);

        when(mongoDatabase.listCollectionNames()).thenReturn(collectionIterable);

        final MongoCursor<Document> documentIteratorWithoutEntries = mock(MongoCursor.class);
        when(documentIteratorWithoutEntries.hasNext()).thenReturn(Boolean.FALSE);

        final FindIterable<Document> findIterableWithoutEntries = mock(FindIterable.class);
        when(findIterableWithoutEntries.iterator()).thenReturn(documentIteratorWithoutEntries);

        final MongoCollection<Document> collectionWithoutEntries = mock(MongoCollection.class);
        when(collectionWithoutEntries.count()).thenReturn(0l);
        when(collectionWithoutEntries.find(any(Document.class))).thenReturn(findIterableWithoutEntries);
        when(collectionWithoutEntries.find()).thenReturn(findIterableWithoutEntries);
        when(mongoDatabase.getCollection(anyString())).thenReturn(collectionWithoutEntries);

        final List<Document> currentDocuments = Arrays.asList(DOCUMENT_1);
        final Iterator<Document> currentDocumentsIt = currentDocuments.iterator();
        final MongoCursor<Document> documentIteratorWithEntries = mock(MongoCursor.class);
        when(documentIteratorWithEntries.hasNext()).thenAnswer((final InvocationOnMock invocation) -> currentDocumentsIt.hasNext());
        when(documentIteratorWithEntries.next()).thenAnswer((final InvocationOnMock invocation) -> currentDocumentsIt.next());
        doAnswer((final InvocationOnMock invocation) -> {
            final Consumer<Document> c = (Consumer<Document>) invocation.getArguments()[0];
            for (final Document doc : currentDocuments) {
                c.accept(doc);
            }
            return null;
        }).when(documentIteratorWithEntries).forEachRemaining(any(Consumer.class));

        final FindIterable<Document> findIterableWithEntries = mock(FindIterable.class);
        when(findIterableWithEntries.iterator()).thenReturn(documentIteratorWithEntries);

        final MongoCollection<Document> collectionWithEntries = mock(MongoCollection.class);
        when(collectionWithEntries.count()).thenReturn(1l);
        when(collectionWithEntries.find(any(Document.class))).thenReturn(findIterableWithEntries);
        when(collectionWithEntries.find()).thenReturn(findIterableWithEntries);
        when(mongoDatabase.getCollection(eq(COLLECTION_NAME_1))).thenReturn(collectionWithEntries);

        final String[] toExclude = new String[] {};
        final Document expectedDataSet = new Document().append(COLLECTION_NAME_1, Arrays.asList(DOCUMENT_1)).append(COLLECTION_NAME_2,
                Arrays.asList(DOCUMENT_2));

        final DataSetComparator comparator = new DataSetComparator(toExclude, false);

        // WHEN
        comparator.compare(mongoDatabase, expectedDataSet, errorCollector);

        // THEN
        assertThat(errorCollector.amountOfErrors(), equalTo(2));

        try {
            errorCollector.report();
            fail("AssertionError expected");
        } catch (final AssertionError e) {
            assertThat(e.getMessage(), containsString("failed in 2 case"));
            assertThat(e.getMessage(), containsString(COLLECTION_NAME_2 + " was expected to be present, but not found"));
            assertThat(e.getMessage(), containsString(DOCUMENT_2 + " was expected in [" + COLLECTION_NAME_2 + "], but is not present"));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testExpectedDataSetIsASubsetOfCurrentDataSetUsingNotStringMode() {
        // GIVEN
        final List<String> currentCollectionNames = Arrays.asList(COLLECTION_NAME_1, COLLECTION_NAME_2);
        final Iterator<String> currentCollectionNamesIt = currentCollectionNames.iterator();
        final MongoCursor<String> currentCollectionNamesCursor = mock(MongoCursor.class);
        when(currentCollectionNamesCursor.hasNext()).thenAnswer((final InvocationOnMock invocation) -> currentCollectionNamesIt.hasNext());
        when(currentCollectionNamesCursor.next()).thenAnswer((final InvocationOnMock invocation) -> currentCollectionNamesIt.next());
        doAnswer((final InvocationOnMock invocation) -> {
            final Consumer<String> c = (Consumer<String>) invocation.getArguments()[0];
            for (final String name : currentCollectionNames) {
                c.accept(name);
            }
            return null;
        }).when(currentCollectionNamesCursor).forEachRemaining(any(Consumer.class));

        final MongoIterable<String> collectionIterable = mock(MongoIterable.class);
        when(collectionIterable.iterator()).thenReturn(currentCollectionNamesCursor);

        when(mongoDatabase.listCollectionNames()).thenReturn(collectionIterable);

        final MongoCursor<Document> documentIteratorWithoutEntries = mock(MongoCursor.class);
        when(documentIteratorWithoutEntries.hasNext()).thenReturn(Boolean.FALSE);

        final FindIterable<Document> findIterableWithoutEntries = mock(FindIterable.class);
        when(findIterableWithoutEntries.iterator()).thenReturn(documentIteratorWithoutEntries);

        final MongoCollection<Document> collectionWithoutEntries = mock(MongoCollection.class);
        when(collectionWithoutEntries.count()).thenReturn(0l);
        when(collectionWithoutEntries.find(any(Document.class))).thenReturn(findIterableWithoutEntries);
        when(collectionWithoutEntries.find()).thenReturn(findIterableWithoutEntries);
        when(mongoDatabase.getCollection(anyString())).thenReturn(collectionWithoutEntries);

        final List<Document> collection1Documents = Arrays.asList(DOCUMENT_1);
        final Iterator<Document> colleciton1DocumentsIt = collection1Documents.iterator();
        final MongoCursor<Document> collection1DocumentIterator = mock(MongoCursor.class);
        when(collection1DocumentIterator.hasNext()).thenAnswer((final InvocationOnMock invocation) -> colleciton1DocumentsIt.hasNext());
        when(collection1DocumentIterator.next()).thenAnswer((final InvocationOnMock invocation) -> colleciton1DocumentsIt.next());
        doAnswer((final InvocationOnMock invocation) -> {
            final Consumer<Document> c = (Consumer<Document>) invocation.getArguments()[0];
            for (final Document doc : collection1Documents) {
                c.accept(doc);
            }
            return null;
        }).when(collection1DocumentIterator).forEachRemaining(any(Consumer.class));

        final FindIterable<Document> collection1Iterable = mock(FindIterable.class);
        when(collection1Iterable.iterator()).thenReturn(collection1DocumentIterator);

        final MongoCollection<Document> collection1 = mock(MongoCollection.class);
        when(collection1.count()).thenReturn(1l);
        when(collection1.find(any(Document.class))).thenReturn(collection1Iterable);
        when(collection1.find()).thenReturn(collection1Iterable);
        when(mongoDatabase.getCollection(eq(COLLECTION_NAME_1))).thenReturn(collection1);

        final List<Document> collection2Documents = Arrays.asList(DOCUMENT_2);
        final Iterator<Document> colleciton2DocumentsIt = collection2Documents.iterator();
        final MongoCursor<Document> collection2DocumentIterator = mock(MongoCursor.class);
        when(collection2DocumentIterator.hasNext()).thenAnswer((final InvocationOnMock invocation) -> colleciton2DocumentsIt.hasNext());
        when(collection2DocumentIterator.next()).thenAnswer((final InvocationOnMock invocation) -> colleciton2DocumentsIt.next());
        doAnswer((final InvocationOnMock invocation) -> {
            final Consumer<Document> c = (Consumer<Document>) invocation.getArguments()[0];
            for (final Document doc : collection2Documents) {
                c.accept(doc);
            }
            return null;
        }).when(collection2DocumentIterator).forEachRemaining(any(Consumer.class));

        final FindIterable<Document> collection2Iterable = mock(FindIterable.class);
        when(collection2Iterable.iterator()).thenReturn(collection2DocumentIterator);

        final MongoCollection<Document> collection2 = mock(MongoCollection.class);
        when(collection1.count()).thenReturn(1l);
        when(collection1.find(any(Document.class))).thenReturn(collection2Iterable);
        when(collection1.find()).thenReturn(collection2Iterable);
        when(mongoDatabase.getCollection(eq(COLLECTION_NAME_2))).thenReturn(collection2);

        final String[] toExclude = new String[] {};
        final Document expectedDataSet = new Document().append(COLLECTION_NAME_1, Arrays.asList(DOCUMENT_1));

        final DataSetComparator comparator = new DataSetComparator(toExclude, false);

        // WHEN
        comparator.compare(mongoDatabase, expectedDataSet, errorCollector);

        // THEN
        assertThat(errorCollector.amountOfErrors(), equalTo(0));

        errorCollector.report();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testExpectedDataSetIsASubsetOfCurrentDataSetUsingStringMode() {
        // GIVEN
        final List<String> currentCollectionNames = Arrays.asList(COLLECTION_NAME_1, COLLECTION_NAME_2);
        final Iterator<String> currentCollectionNamesIt = currentCollectionNames.iterator();
        final MongoCursor<String> currentCollectionNamesCursor = mock(MongoCursor.class);
        when(currentCollectionNamesCursor.hasNext()).thenAnswer((final InvocationOnMock invocation) -> currentCollectionNamesIt.hasNext());
        when(currentCollectionNamesCursor.next()).thenAnswer((final InvocationOnMock invocation) -> currentCollectionNamesIt.next());
        doAnswer((final InvocationOnMock invocation) -> {
            final Consumer<String> c = (Consumer<String>) invocation.getArguments()[0];
            for (final String name : currentCollectionNames) {
                c.accept(name);
            }
            return null;
        }).when(currentCollectionNamesCursor).forEachRemaining(any(Consumer.class));

        final MongoIterable<String> collectionIterable = mock(MongoIterable.class);
        when(collectionIterable.iterator()).thenReturn(currentCollectionNamesCursor);

        when(mongoDatabase.listCollectionNames()).thenReturn(collectionIterable);

        final MongoCursor<Document> documentIteratorWithoutEntries = mock(MongoCursor.class);
        when(documentIteratorWithoutEntries.hasNext()).thenReturn(Boolean.FALSE);

        final FindIterable<Document> findIterableWithoutEntries = mock(FindIterable.class);
        when(findIterableWithoutEntries.iterator()).thenReturn(documentIteratorWithoutEntries);

        final MongoCollection<Document> collectionWithoutEntries = mock(MongoCollection.class);
        when(collectionWithoutEntries.count()).thenReturn(0l);
        when(collectionWithoutEntries.find(any(Document.class))).thenReturn(findIterableWithoutEntries);
        when(collectionWithoutEntries.find()).thenReturn(findIterableWithoutEntries);
        when(mongoDatabase.getCollection(anyString())).thenReturn(collectionWithoutEntries);

        final List<Document> collection1Documents = Arrays.asList(DOCUMENT_1);
        final Iterator<Document> colleciton1DocumentsIt = collection1Documents.iterator();
        final MongoCursor<Document> collection1DocumentIterator = mock(MongoCursor.class);
        when(collection1DocumentIterator.hasNext()).thenAnswer((final InvocationOnMock invocation) -> colleciton1DocumentsIt.hasNext());
        when(collection1DocumentIterator.next()).thenAnswer((final InvocationOnMock invocation) -> colleciton1DocumentsIt.next());
        doAnswer((final InvocationOnMock invocation) -> {
            final Consumer<Document> c = (Consumer<Document>) invocation.getArguments()[0];
            for (final Document doc : collection1Documents) {
                c.accept(doc);
            }
            return null;
        }).when(collection1DocumentIterator).forEachRemaining(any(Consumer.class));

        final FindIterable<Document> collection1Iterable = mock(FindIterable.class);
        when(collection1Iterable.iterator()).thenReturn(collection1DocumentIterator);

        final MongoCollection<Document> collection1 = mock(MongoCollection.class);
        when(collection1.count()).thenReturn(1l);
        when(collection1.find(any(Document.class))).thenReturn(collection1Iterable);
        when(collection1.find()).thenReturn(collection1Iterable);
        when(mongoDatabase.getCollection(eq(COLLECTION_NAME_1))).thenReturn(collection1);

        final List<Document> collection2Documents = Arrays.asList(DOCUMENT_2);
        final Iterator<Document> colleciton2DocumentsIt = collection2Documents.iterator();
        final MongoCursor<Document> collection2DocumentIterator = mock(MongoCursor.class);
        when(collection2DocumentIterator.hasNext()).thenAnswer((final InvocationOnMock invocation) -> colleciton2DocumentsIt.hasNext());
        when(collection2DocumentIterator.next()).thenAnswer((final InvocationOnMock invocation) -> colleciton2DocumentsIt.next());
        doAnswer((final InvocationOnMock invocation) -> {
            final Consumer<Document> c = (Consumer<Document>) invocation.getArguments()[0];
            for (final Document doc : collection2Documents) {
                c.accept(doc);
            }
            return null;
        }).when(collection2DocumentIterator).forEachRemaining(any(Consumer.class));

        final FindIterable<Document> collection2Iterable = mock(FindIterable.class);
        when(collection2Iterable.iterator()).thenReturn(collection2DocumentIterator);

        final MongoCollection<Document> collection2 = mock(MongoCollection.class);
        when(collection1.count()).thenReturn(1l);
        when(collection1.find(any(Document.class))).thenReturn(collection2Iterable);
        when(collection1.find()).thenReturn(collection2Iterable);
        when(mongoDatabase.getCollection(eq(COLLECTION_NAME_2))).thenReturn(collection2);

        final String[] toExclude = new String[] {};
        final Document expectedDataSet = new Document().append(COLLECTION_NAME_1, Arrays.asList(DOCUMENT_1));

        final DataSetComparator comparator = new DataSetComparator(toExclude, true);

        // WHEN
        comparator.compare(mongoDatabase, expectedDataSet, errorCollector);

        // THEN
        assertThat(errorCollector.amountOfErrors(), equalTo(1));

        try {
            errorCollector.report();
            fail("AssertionError expected");
        } catch (final AssertionError e) {
            assertThat(e.getMessage(), containsString("failed in 1 case"));
            assertThat(e.getMessage(), containsString(COLLECTION_NAME_2 + " was not expected, but is present"));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCurrentDataSetAndExpectedDataSetDifferInTableRecords() {
        // GIVEN
        final List<String> currentCollectionNames = Arrays.asList(COLLECTION_NAME_1);
        final Iterator<String> currentCollectionNamesIt = currentCollectionNames.iterator();
        final MongoCursor<String> currentCollectionNamesCursor = mock(MongoCursor.class);
        when(currentCollectionNamesCursor.hasNext()).thenAnswer((final InvocationOnMock invocation) -> currentCollectionNamesIt.hasNext());
        when(currentCollectionNamesCursor.next()).thenAnswer((final InvocationOnMock invocation) -> currentCollectionNamesIt.next());
        doAnswer((final InvocationOnMock invocation) -> {
            final Consumer<String> c = (Consumer<String>) invocation.getArguments()[0];
            for (final String name : currentCollectionNames) {
                c.accept(name);
            }
            return null;
        }).when(currentCollectionNamesCursor).forEachRemaining(any(Consumer.class));

        final MongoIterable<String> collectionIterable = mock(MongoIterable.class);
        when(collectionIterable.iterator()).thenReturn(currentCollectionNamesCursor);

        when(mongoDatabase.listCollectionNames()).thenReturn(collectionIterable);

        final MongoCursor<Document> documentIteratorWithoutEntries = mock(MongoCursor.class);
        when(documentIteratorWithoutEntries.hasNext()).thenReturn(Boolean.FALSE);

        final FindIterable<Document> findIterableWithoutEntries = mock(FindIterable.class);
        when(findIterableWithoutEntries.iterator()).thenReturn(documentIteratorWithoutEntries);

        final MongoCollection<Document> collectionWithoutEntries = mock(MongoCollection.class);
        when(collectionWithoutEntries.count()).thenReturn(0l);
        when(collectionWithoutEntries.find(any(Document.class))).thenReturn(findIterableWithoutEntries);
        when(collectionWithoutEntries.find()).thenReturn(findIterableWithoutEntries);
        when(mongoDatabase.getCollection(anyString())).thenReturn(collectionWithoutEntries);

        final List<Document> collection1Documents = Arrays.asList(DOCUMENT_1);
        final Iterator<Document> colleciton1DocumentsIt = collection1Documents.iterator();
        final MongoCursor<Document> collection1DocumentIterator = mock(MongoCursor.class);
        when(collection1DocumentIterator.hasNext()).thenAnswer((final InvocationOnMock invocation) -> colleciton1DocumentsIt.hasNext());
        when(collection1DocumentIterator.next()).thenAnswer((final InvocationOnMock invocation) -> colleciton1DocumentsIt.next());
        doAnswer((final InvocationOnMock invocation) -> {
            final Consumer<Document> c = (Consumer<Document>) invocation.getArguments()[0];
            for (final Document doc : collection1Documents) {
                c.accept(doc);
            }
            return null;
        }).when(collection1DocumentIterator).forEachRemaining(any(Consumer.class));

        final FindIterable<Document> collection1Iterable = mock(FindIterable.class);
        when(collection1Iterable.iterator()).thenReturn(collection1DocumentIterator);

        final FindIterable<Document> collection1EmptyIterable = mock(FindIterable.class);
        final MongoCursor<Document> collection1EmptyDocumentIterator = mock(MongoCursor.class);
        when(collection1EmptyDocumentIterator.hasNext()).thenReturn(Boolean.FALSE);
        when(collection1EmptyIterable.iterator()).thenReturn(collection1EmptyDocumentIterator);

        final MongoCollection<Document> collection1 = mock(MongoCollection.class);
        when(collection1.find(any(Document.class))).thenReturn(collection1EmptyIterable);

        when(collection1.count()).thenReturn(1l);
        when(collection1.find(eq(DOCUMENT_1))).thenReturn(collection1Iterable);
        when(collection1.find()).thenReturn(collection1Iterable);
        when(mongoDatabase.getCollection(eq(COLLECTION_NAME_1))).thenReturn(collection1);

        final String[] toExclude = new String[] {};
        final Document expectedDataSet = new Document().append(COLLECTION_NAME_1, Arrays.asList(DOCUMENT_2));

        final DataSetComparator comparator = new DataSetComparator(toExclude, false);

        // WHEN
        comparator.compare(mongoDatabase, expectedDataSet, errorCollector);

        // THEN
        assertThat(errorCollector.amountOfErrors(), equalTo(2));

        try {
            errorCollector.report();
            fail("AssertionError expected");
        } catch (final AssertionError e) {
            assertThat(e.getMessage(), containsString("failed in 2 cases"));
            assertThat(e.getMessage(), containsString(DOCUMENT_2 + " was expected in [" + COLLECTION_NAME_1 + "], but is not present"));
            assertThat(e.getMessage(), containsString(DOCUMENT_1 + " was not expected in [" + COLLECTION_NAME_1 + "], but is present"));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCurrentDataSetAndExpectedDataSetAreEqual() {
        // GIVEN
        final List<String> currentCollectionNames = Arrays.asList(COLLECTION_NAME_1);
        final Iterator<String> currentCollectionNamesIt = currentCollectionNames.iterator();
        final MongoCursor<String> currentCollectionNamesCursor = mock(MongoCursor.class);
        when(currentCollectionNamesCursor.hasNext()).thenAnswer((final InvocationOnMock invocation) -> currentCollectionNamesIt.hasNext());
        when(currentCollectionNamesCursor.next()).thenAnswer((final InvocationOnMock invocation) -> currentCollectionNamesIt.next());
        doAnswer((final InvocationOnMock invocation) -> {
            final Consumer<String> c = (Consumer<String>) invocation.getArguments()[0];
            for (final String name : currentCollectionNames) {
                c.accept(name);
            }
            return null;
        }).when(currentCollectionNamesCursor).forEachRemaining(any(Consumer.class));

        final MongoIterable<String> collectionIterable = mock(MongoIterable.class);
        when(collectionIterable.iterator()).thenReturn(currentCollectionNamesCursor);

        when(mongoDatabase.listCollectionNames()).thenReturn(collectionIterable);

        final MongoCursor<Document> documentIteratorWithoutEntries = mock(MongoCursor.class);
        when(documentIteratorWithoutEntries.hasNext()).thenReturn(Boolean.FALSE);

        final FindIterable<Document> findIterableWithoutEntries = mock(FindIterable.class);
        when(findIterableWithoutEntries.iterator()).thenReturn(documentIteratorWithoutEntries);

        final MongoCollection<Document> collectionWithoutEntries = mock(MongoCollection.class);
        when(collectionWithoutEntries.count()).thenReturn(0l);
        when(collectionWithoutEntries.find(any(Document.class))).thenReturn(findIterableWithoutEntries);
        when(collectionWithoutEntries.find()).thenReturn(findIterableWithoutEntries);
        when(mongoDatabase.getCollection(anyString())).thenReturn(collectionWithoutEntries);

        final List<Document> collection1Documents = Arrays.asList(DOCUMENT_1);
        final Iterator<Document> colleciton1DocumentsIt = collection1Documents.iterator();
        final MongoCursor<Document> collection1DocumentIterator = mock(MongoCursor.class);
        when(collection1DocumentIterator.hasNext()).thenAnswer((final InvocationOnMock invocation) -> colleciton1DocumentsIt.hasNext());
        when(collection1DocumentIterator.next()).thenAnswer((final InvocationOnMock invocation) -> colleciton1DocumentsIt.next());
        doAnswer((final InvocationOnMock invocation) -> {
            final Consumer<Document> c = (Consumer<Document>) invocation.getArguments()[0];
            for (final Document doc : collection1Documents) {
                c.accept(doc);
            }
            return null;
        }).when(collection1DocumentIterator).forEachRemaining(any(Consumer.class));

        final FindIterable<Document> collection1Iterable = mock(FindIterable.class);
        when(collection1Iterable.iterator()).thenReturn(collection1DocumentIterator);

        final FindIterable<Document> collection1EmptyIterable = mock(FindIterable.class);
        final MongoCursor<Document> collection1EmptyDocumentIterator = mock(MongoCursor.class);
        when(collection1EmptyDocumentIterator.hasNext()).thenReturn(Boolean.FALSE);
        when(collection1EmptyIterable.iterator()).thenReturn(collection1EmptyDocumentIterator);

        final MongoCollection<Document> collection1 = mock(MongoCollection.class);
        when(collection1.find(any(Document.class))).thenReturn(collection1EmptyIterable);

        when(collection1.count()).thenReturn(1l);
        when(collection1.find(eq(DOCUMENT_1))).thenReturn(collection1Iterable);
        when(collection1.find()).thenReturn(collection1Iterable);
        when(mongoDatabase.getCollection(eq(COLLECTION_NAME_1))).thenReturn(collection1);

        final String[] toExclude = new String[] {};
        final Document expectedDataSet = new Document().append(COLLECTION_NAME_1, Arrays.asList(DOCUMENT_1));

        final DataSetComparator comparator = new DataSetComparator(toExclude, false);

        // WHEN
        comparator.compare(mongoDatabase, expectedDataSet, errorCollector);

        // THEN
        assertThat(errorCollector.amountOfErrors(), equalTo(0));

        errorCollector.report();
    }
}
