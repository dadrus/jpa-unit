package eu.drus.jpa.unit.neo4j.dataset;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jgrapht.Graph;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import eu.drus.jpa.unit.spi.DataSetLoader;
import eu.drus.jpa.unit.spi.UnsupportedDataSetFormatException;

@RunWith(PowerMockRunner.class)
@PrepareForTest(EntityUtils.class)
public class DataSetLoaderProviderTest {

    private static final DataSetLoaderProvider LOADER_PROVIDER = new DataSetLoaderProvider(
            new GraphElementFactory(Collections.emptyList()));

    @SuppressWarnings("unchecked")
    @Before
    public void prepareMocks() throws Exception {
        mockStatic(EntityUtils.class);
        when(EntityUtils.getEntityClassFromNodeLabels(any(List.class), any(List.class))).thenReturn(DataSetLoaderProviderTest.class);
        when(EntityUtils.getNamesOfIdProperties(any(Class.class))).thenReturn(Arrays.asList());
    }

    private static File getFile(final String path) throws URISyntaxException {
        final URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        return new File(url.toURI());
    }

    @Test(expected = UnsupportedDataSetFormatException.class)
    public void testJsonLoaderNotSupported() {
        LOADER_PROVIDER.jsonLoader();
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
    public void testXmlLoaderLoadUsingProperResource() throws Exception {
        // WHEN
        final DataSetLoader<Graph<Node, Edge>> loader = LOADER_PROVIDER.xmlLoader();

        // THEN
        assertThat(loader, notNullValue());

        // WHEN
        final Graph<Node, Edge> graph = loader.load(getFile("test-data.xml"));

        // THEN
        assertThat(graph, notNullValue());

        // TODO: finalize me
    }

    @Test(expected = NullPointerException.class)
    public void testXmlLoaderLoadUsingNullFileName() throws IOException {
        // WHEN
        final DataSetLoader<Graph<Node, Edge>> loader = LOADER_PROVIDER.xmlLoader();

        // THEN
        assertThat(loader, notNullValue());

        // WHEN
        loader.load(null);

        // THEN
        // NullPointerException is thrown
    }

    @Test(expected = IOException.class)
    public void testXmlLoaderLoadUsingWrongResource() throws Exception {
        // WHEN
        final DataSetLoader<Graph<Node, Edge>> loader = LOADER_PROVIDER.xmlLoader();

        // THEN
        assertThat(loader, notNullValue());

        // WHEN
        loader.load(getFile("test-data.csv"));

        // THEN
        // Exception from the parser is thrown
    }
}
