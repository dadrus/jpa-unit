package eu.drus.test.persistence.core.dbunit.dataset;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.junit.Test;

public class DataSetLoaderProviderTest {

    private static final DataSetLoaderProvider LOADER_PROVIDER = new DataSetLoaderProvider();

    @Test
    public void testJsonLoaderLoadUsingProperResource() throws IOException, DataSetException {
        // WHEN
        final DataSetLoader loader = LOADER_PROVIDER.jsonLoader();

        // THEN
        assertThat(loader, notNullValue());

        // WHEN
        final IDataSet dataSet = loader.load("test-data.json");

        // THEN
        assertThat(dataSet, notNullValue());

        final List<String> tableNames = Arrays.asList(dataSet.getTableNames());
        assertThat(tableNames.size(), equalTo(2));
        assertThat(tableNames, hasItems("JSON_TABLE_1", "JSON_TABLE_2"));

        final ITable table1 = dataSet.getTable("JSON_TABLE_1");
        assertThat(table1.getRowCount(), equalTo(3));

        final ITable table2 = dataSet.getTable("JSON_TABLE_2");
        assertThat(table2.getRowCount(), equalTo(1));
    }

    @Test(expected = NullPointerException.class)
    public void testJsonLoaderLoadUsingNullFileName() throws IOException, DataSetException {
        // WHEN
        final DataSetLoader loader = LOADER_PROVIDER.jsonLoader();

        // THEN
        assertThat(loader, notNullValue());

        // WHEN
        loader.load(null);

        // THEN
        // IOException is thrown
    }

    @Test(expected = IOException.class)
    public void testJsonLoaderLoadUsingWrongResource() throws IOException, DataSetException {
        // WHEN
        final DataSetLoader loader = LOADER_PROVIDER.jsonLoader();

        // THEN
        assertThat(loader, notNullValue());

        // WHEN
        loader.load("test-data.yaml");

        // THEN
        // IOException is thrown
    }

    @Test(expected = IOException.class)
    public void testJsonLoaderLoadUsingNotExistingResource() throws IOException, DataSetException {
        // WHEN
        final DataSetLoader loader = LOADER_PROVIDER.jsonLoader();

        // THEN
        assertThat(loader, notNullValue());

        // WHEN
        loader.load("does-not-exist");

        // THEN
        // IOException is thrown
    }

    @Test
    public void testYamlLoaderLoadUsingProperResource() throws IOException, DataSetException {
        // WHEN
        final DataSetLoader loader = LOADER_PROVIDER.yamlLoader();

        // THEN
        assertThat(loader, notNullValue());

        // WHEN
        final IDataSet dataSet = loader.load("test-data.yaml");

        // THEN
        assertThat(dataSet, notNullValue());

        final List<String> tableNames = Arrays.asList(dataSet.getTableNames());
        assertThat(tableNames.size(), equalTo(2));
        assertThat(tableNames, hasItems("YAML_TABLE_1", "YAML_TABLE_2"));

        final ITable table1 = dataSet.getTable("YAML_TABLE_1");
        assertThat(table1.getRowCount(), equalTo(3));

        final ITable table2 = dataSet.getTable("YAML_TABLE_2");
        assertThat(table2.getRowCount(), equalTo(1));
    }

    @Test(expected = NullPointerException.class)
    public void testYamlLoaderLoadUsingNullFileName() throws IOException, DataSetException {
        // WHEN
        final DataSetLoader loader = LOADER_PROVIDER.yamlLoader();

        // THEN
        assertThat(loader, notNullValue());

        // WHEN
        loader.load(null);

        // THEN
        // IOException is thrown
    }

    @Test(expected = IOException.class)
    public void testYamlLoaderLoadUsingWrongResource() throws IOException, DataSetException {
        // WHEN
        final DataSetLoader loader = LOADER_PROVIDER.yamlLoader();

        // THEN
        assertThat(loader, notNullValue());

        // WHEN
        loader.load("test-data.json");

        // THEN
        // IOException is thrown
    }

    @Test(expected = IOException.class)
    public void testYamlLoaderLoadUsingNotExistingResource() throws IOException, DataSetException {
        // WHEN
        final DataSetLoader loader = LOADER_PROVIDER.yamlLoader();

        // THEN
        assertThat(loader, notNullValue());

        // WHEN
        loader.load("does-not-exist");

        // THEN
        // IOException is thrown
    }

    @Test
    public void testXmlLoaderLoadUsingProperResource() throws IOException, DataSetException {
        // WHEN
        final DataSetLoader loader = LOADER_PROVIDER.xmlLoader();

        // THEN
        assertThat(loader, notNullValue());

        // WHEN
        final IDataSet dataSet = loader.load("test-data.xml");

        // THEN
        assertThat(dataSet, notNullValue());

        final List<String> tableNames = Arrays.asList(dataSet.getTableNames());
        assertThat(tableNames.size(), equalTo(2));
        assertThat(tableNames, hasItems("XML_TABLE_1", "XML_TABLE_2"));

        final ITable table1 = dataSet.getTable("XML_TABLE_1");
        assertThat(table1.getRowCount(), equalTo(3));

        final ITable table2 = dataSet.getTable("XML_TABLE_2");
        assertThat(table2.getRowCount(), equalTo(1));
    }

    @Test(expected = NullPointerException.class)
    public void testXmlLoaderLoadUsingNullFileName() throws IOException, DataSetException {
        // WHEN
        final DataSetLoader loader = LOADER_PROVIDER.xmlLoader();

        // THEN
        assertThat(loader, notNullValue());

        // WHEN
        loader.load(null);

        // THEN
        // IOException is thrown
    }

    @Test(expected = IOException.class)
    public void testXmlLoaderLoadUsingWrongResource() throws IOException, DataSetException {
        // WHEN
        final DataSetLoader loader = LOADER_PROVIDER.xmlLoader();

        // THEN
        assertThat(loader, notNullValue());

        // WHEN
        loader.load("test-data.json");

        // THEN
        // IOException is thrown
    }

    @Test(expected = IOException.class)
    public void testXmlLoaderLoadUsingNotExistingResource() throws IOException, DataSetException {
        // WHEN
        final DataSetLoader loader = LOADER_PROVIDER.xmlLoader();

        // THEN
        assertThat(loader, notNullValue());

        // WHEN
        loader.load("does-not-exist");

        // THEN
        // IOException is thrown
    }
}
