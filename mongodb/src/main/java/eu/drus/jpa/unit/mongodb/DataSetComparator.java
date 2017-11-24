package eu.drus.jpa.unit.mongodb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import eu.drus.jpa.unit.spi.AssertionErrorCollector;
import eu.drus.jpa.unit.spi.ColumnsHolder;

public class DataSetComparator {

    private static final Function<String, String> ID_MAPPER = (final String name) -> name;

    private ColumnsHolder toExclude;
    private boolean isStrict;

    public DataSetComparator(final String[] toExclude, final boolean isStrict) {
        this.toExclude = new ColumnsHolder(toExclude, ID_MAPPER);
        this.isStrict = isStrict;
    }

    public void compare(final MongoDatabase connection, final Document expectedDataSet, final AssertionErrorCollector errorCollector) {
        if (expectedDataSet.entrySet().isEmpty()) {
            shouldBeEmpty(connection, errorCollector);
        } else {
            compareContent(connection, expectedDataSet, errorCollector);
        }
    }

    private void shouldBeEmpty(final MongoDatabase connection, final AssertionErrorCollector errorCollector) {
        for (final String collectionName : connection.listCollectionNames()) {
            final long rowCount = connection.getCollection(collectionName).count();
            if (rowCount != 0) {
                errorCollector.collect(collectionName + " was expected to be empty, but has <" + rowCount + "> entries.");
            }
        }
    }

    private void compareContent(final MongoDatabase connection, final Document expectedDataSet,
            final AssertionErrorCollector errorCollector) {

        verifyCollectionNames(connection, expectedDataSet.keySet(), errorCollector);

        for (final String collectionName : expectedDataSet.keySet()) {
            verifyCollectionContent(connection, expectedDataSet, collectionName, errorCollector);
        }

        if (isStrict) {
            for (final String collectionName : connection.listCollectionNames()) {
                if (!expectedDataSet.keySet().contains(collectionName)) {
                    errorCollector.collect(collectionName + " was not expected, but is present");
                }
            }
        }
    }

    private void verifyCollectionNames(final MongoDatabase connection, final Set<String> expectedCollectionNames,
            final AssertionErrorCollector errorCollector) {
        final List<String> currentCollections = new ArrayList<>();
        connection.listCollectionNames().iterator().forEachRemaining(currentCollections::add);
        for (final String expectedCollectionName : expectedCollectionNames) {
            if (!currentCollections.contains(expectedCollectionName)) {
                errorCollector.collect(expectedCollectionName + " was expected to be present, but not found");
            }
        }
    }

    private void verifyCollectionContent(final MongoDatabase connection, final Document expectedDataSet, final String collectionName,
            final AssertionErrorCollector errorCollector) {

        final List<Document> expectedCollectionEntries = getCollectionData(expectedDataSet.get(collectionName));
        final List<String> columnsToExclude = toExclude.getColumns(collectionName);
        final List<Document> foundEntries = new ArrayList<>();

        final MongoCollection<Document> currentCollection = connection.getCollection(collectionName);
        for (final Document expectedEntry : expectedCollectionEntries) {

            final Document expected = filterRequest(expectedEntry, columnsToExclude);
            final FindIterable<Document> resultIt = currentCollection.find(expected);
            if (!resultIt.iterator().hasNext()) {
                errorCollector.collect(expectedEntry + " was expected in [" + collectionName + "], but is not present");
            }

            resultIt.iterator().forEachRemaining(foundEntries::add);
        }

        final FindIterable<Document> allEntries = currentCollection.find();
        for (final Document d : allEntries) {
            if (!foundEntries.contains(d)) {
                errorCollector.collect(d + " was not expected in [" + collectionName + "], but is present");
            }
        }
    }

    private Document filterRequest(final Document expectedEntry, final List<String> columnsToExclude) {
        final Document filtered = new Document();
        for (final Entry<String, Object> entry : expectedEntry.entrySet()) {
            if (!columnsToExclude.contains(entry.getKey())) {
                filtered.put(entry.getKey(), entry.getValue());
            }
        }
        return filtered;
    }

    @SuppressWarnings("unchecked")
    private List<Document> getCollectionData(final Object obj) {
        if (List.class.isAssignableFrom(obj.getClass())) {
            return (List<Document>) obj;
        } else {
            return ((Document) obj).get("data", List.class);
        }
    }
}