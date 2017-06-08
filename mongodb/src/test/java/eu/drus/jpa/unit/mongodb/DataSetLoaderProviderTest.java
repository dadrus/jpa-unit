package eu.drus.jpa.unit.mongodb;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import org.bson.Document;
import org.junit.Test;

import eu.drus.jpa.unit.core.DataSetLoader;
import eu.drus.jpa.unit.core.UnsupportedDataSetFormatException;

public class DataSetLoaderProviderTest {

    private static final DataSetLoaderProvider LOADER_PROVIDER = new DataSetLoaderProvider();

    private static File getFile(final String path) throws URISyntaxException {
        final URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        return new File(url.toURI());
    }

    @Test(expected = UnsupportedDataSetFormatException.class)
    public void testXmlLoaderNotSupported() {
        LOADER_PROVIDER.xmlLoader();
    }

    @Test(expected = UnsupportedDataSetFormatException.class)
    public void testYamlLoaderNotSupported() {
        LOADER_PROVIDER.yamlLoader();
    }

    @Test(expected = UnsupportedDataSetFormatException.class)
    public void testCsvLoaderNotSupported() {
        LOADER_PROVIDER.csvLoader();
    }

    @Test(expected = UnsupportedDataSetFormatException.class)
    public void testXlsLoaderNotSupported() {
        LOADER_PROVIDER.xlsLoader();
    }

    @Test
    public void testJsonLoaderLoadUsingProperResource() throws Exception {
        // WHEN
        final DataSetLoader<Document> loader = LOADER_PROVIDER.jsonLoader();

        // THEN
        assertThat(loader, notNullValue());

        // WHEN
        final Document document = loader.load(getFile("test-data.json"));

        // THEN
        assertThat(document, notNullValue());

        final Set<String> tableNames = document.keySet();
        assertThat(tableNames.size(), equalTo(2));
        assertThat(tableNames, hasItems("JSON_COLLECTION_1", "JSON_COLLECTION_2"));

        final List<Document> collection1 = document.get("JSON_COLLECTION_1", List.class);
        assertThat(collection1.size(), equalTo(3));

        final List<Document> collection2 = document.get("JSON_COLLECTION_2", List.class);
        assertThat(collection2.size(), equalTo(1));
    }

    @Test(expected = NullPointerException.class)
    public void testJsonLoaderLoadUsingNullFileName() throws IOException {
        // WHEN
        final DataSetLoader<Document> loader = LOADER_PROVIDER.jsonLoader();

        // THEN
        assertThat(loader, notNullValue());

        // WHEN
        loader.load(null);

        // THEN
        // NullPointerException is thrown
    }

    @Test(expected = Exception.class)
    public void testJsonLoaderLoadUsingWrongResource() throws Exception {
        // WHEN
        final DataSetLoader<Document> loader = LOADER_PROVIDER.jsonLoader();

        // THEN
        assertThat(loader, notNullValue());

        // WHEN
        loader.load(getFile("test-data.yaml"));

        // THEN
        // Exception from the parser is thrown
    }
}
