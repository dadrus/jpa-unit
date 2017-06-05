package eu.drus.jpa.unit.mongodb.operation;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.bson.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.mongodb.Block;
import com.mongodb.Function;
import com.mongodb.ServerAddress;
import com.mongodb.ServerCursor;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;

@RunWith(MockitoJUnitRunner.class)
public class DeleteAllOperationTest {

    private static final String USER_DEFINED_COLLECTION_NAME_1 = "some_collection_1";
    private static final String USER_DEFINED_COLLECTION_NAME_2 = "some_collection_2";
    private static final String SYSTEM_DEFINED_COLLECTION_NAME = "system.collection";

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
    public void testDataInSystemCollectionsIsNotDeleted() {
        // GIVEN
        when(connection.listCollectionNames()).thenReturn(new MongoIterableMock(SYSTEM_DEFINED_COLLECTION_NAME));

        // WHEN
        operation.execute(connection, new Document());

        // THEN
        verify(collection, never()).deleteMany(any(Document.class));
    }

    @Test
    public void testDataInUserDefinedCollectionsIsDeleted() {
        // GIVEN
        when(connection.listCollectionNames()).thenReturn(
                new MongoIterableMock(USER_DEFINED_COLLECTION_NAME_1, SYSTEM_DEFINED_COLLECTION_NAME, USER_DEFINED_COLLECTION_NAME_2));

        // WHEN
        operation.execute(connection, new Document());

        // THEN
        final ArgumentCaptor<String> collectionNameCaptor = ArgumentCaptor.forClass(String.class);
        verify(connection, times(2)).getCollection(collectionNameCaptor.capture());

        final List<String> capturedCollectionNames = collectionNameCaptor.getAllValues();
        assertThat(capturedCollectionNames.size(), equalTo(2));
        assertThat(capturedCollectionNames, hasItems(USER_DEFINED_COLLECTION_NAME_1, USER_DEFINED_COLLECTION_NAME_2));

        verify(collection, times(2)).deleteMany(eq(new Document()));
    }

    private static class MongoIterableMock implements MongoIterable<String> {

        private List<String> nameHolder;

        public MongoIterableMock(final String... name) {
            nameHolder = Arrays.asList(name);
        }

        @Override
        public MongoCursor<String> iterator() {
            final Iterator<String> it = nameHolder.iterator();
            return new MongoCursor<String>() {

                @Override
                public void close() {}

                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public String next() {
                    return it.next();
                }

                @Override
                public String tryNext() {
                    return null;
                }

                @Override
                public ServerCursor getServerCursor() {
                    return null;
                }

                @Override
                public ServerAddress getServerAddress() {
                    return null;
                }
            };
        }

        @Override
        public String first() {
            return null;
        }

        @Override
        public <U> MongoIterable<U> map(final Function<String, U> mapper) {
            return null;
        }

        @Override
        public void forEach(final Block<? super String> block) {}

        @Override
        public <A extends Collection<? super String>> A into(final A target) {
            return null;
        }

        @Override
        public MongoIterable<String> batchSize(final int batchSize) {
            return null;
        }

    }
}
