package eu.drus.jpa.unit.decorator.dbunit.dataset;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.junit.Test;

import eu.drus.jpa.unit.core.DataSetLoader;
import eu.drus.jpa.unit.sql.dbunit.dataset.DataSetLoaderProvider;

public class DataSetLoaderProviderTest {

    private static final DataSetLoaderProvider LOADER_PROVIDER = new DataSetLoaderProvider();

    private static File getFile(final String path) throws URISyntaxException {
        final URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        return new File(url.toURI());
    }

    @Test
    public void testJsonLoaderLoadUsingProperResource() throws Exception {
        // WHEN
        final DataSetLoader<IDataSet> loader = LOADER_PROVIDER.jsonLoader();

        // THEN
        assertThat(loader, notNullValue());

        // WHEN
        final IDataSet dataSet = loader.load(getFile("test-data.json"));

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
        final DataSetLoader<IDataSet> loader = LOADER_PROVIDER.jsonLoader();

        // THEN
        assertThat(loader, notNullValue());

        // WHEN
        loader.load(null);

        // THEN
        // IOException is thrown
    }

    @Test(expected = IOException.class)
    public void testJsonLoaderLoadUsingWrongResource() throws Exception {
        // WHEN
        final DataSetLoader<IDataSet> loader = LOADER_PROVIDER.jsonLoader();

        // THEN
        assertThat(loader, notNullValue());

        // WHEN
        loader.load(getFile("test-data.yaml"));

        // THEN
        // IOException is thrown
    }

    @Test
    public void testYamlLoaderLoadUsingProperResource() throws Exception {
        // WHEN
        final DataSetLoader<IDataSet> loader = LOADER_PROVIDER.yamlLoader();

        // THEN
        assertThat(loader, notNullValue());

        // WHEN
        final IDataSet dataSet = loader.load(getFile("test-data.yaml"));

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
        final DataSetLoader<IDataSet> loader = LOADER_PROVIDER.yamlLoader();

        // THEN
        assertThat(loader, notNullValue());

        // WHEN
        loader.load(null);

        // THEN
        // IOException is thrown
    }

    @Test(expected = IOException.class)
    public void testYamlLoaderLoadUsingWrongResource() throws Exception {
        // WHEN
        final DataSetLoader<IDataSet> loader = LOADER_PROVIDER.yamlLoader();

        // THEN
        assertThat(loader, notNullValue());

        // WHEN
        loader.load(getFile("test-data.json"));

        // THEN
        // IOException is thrown
    }

    @Test
    public void testXmlLoaderLoadUsingProperResource() throws Exception {
        // WHEN
        final DataSetLoader<IDataSet> loader = LOADER_PROVIDER.xmlLoader();

        // THEN
        assertThat(loader, notNullValue());

        // WHEN
        final IDataSet dataSet = loader.load(getFile("test-data.xml"));

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
        final DataSetLoader<IDataSet> loader = LOADER_PROVIDER.xmlLoader();

        // THEN
        assertThat(loader, notNullValue());

        // WHEN
        loader.load(null);

        // THEN
        // IOException is thrown
    }

    @Test(expected = IOException.class)
    public void testXmlLoaderLoadUsingWrongResource() throws Exception {
        // WHEN
        final DataSetLoader<IDataSet> loader = LOADER_PROVIDER.xmlLoader();

        // THEN
        assertThat(loader, notNullValue());

        // WHEN
        loader.load(getFile("test-data.json"));

        // THEN
        // IOException is thrown
    }

    @Test
    public void testXlsxLoaderLoadUsingProperResource() throws Exception {
        // WHEN
        final DataSetLoader<IDataSet> loader = LOADER_PROVIDER.xlsLoader();

        // THEN
        assertThat(loader, notNullValue());

        // WHEN
        final IDataSet dataSet = loader.load(getFile("test-data.xlsx"));

        // THEN
        assertThat(dataSet, notNullValue());

        final List<String> tableNames = Arrays.asList(dataSet.getTableNames());
        assertThat(tableNames.size(), equalTo(2));
        assertThat(tableNames, hasItems("XLS_TABLE_1", "XLS_TABLE_2"));

        final ITable table1 = dataSet.getTable("XLS_TABLE_1");
        assertThat(table1.getRowCount(), equalTo(3));

        final ITable table2 = dataSet.getTable("XLS_TABLE_2");
        assertThat(table2.getRowCount(), equalTo(1));
    }

    @Test(expected = NullPointerException.class)
    public void testXlsxLoaderLoadUsingNullFileName() throws IOException, DataSetException {
        // WHEN
        final DataSetLoader<IDataSet> loader = LOADER_PROVIDER.xlsLoader();

        // THEN
        assertThat(loader, notNullValue());

        // WHEN
        loader.load(null);

        // THEN
        // IOException is thrown
    }

    @Test(expected = IOException.class)
    public void testXlsxLoaderLoadUsingWrongResource() throws Exception {
        // WHEN
        final DataSetLoader<IDataSet> loader = LOADER_PROVIDER.xlsLoader();

        // THEN
        assertThat(loader, notNullValue());

        // WHEN
        loader.load(getFile("test-data.json"));

        // THEN
        // IOException is thrown
    }

    @Test
    public void testXlsLoaderLoadUsingProperResource() throws Exception {
        // WHEN
        final DataSetLoader<IDataSet> loader = LOADER_PROVIDER.xlsLoader();

        // THEN
        assertThat(loader, notNullValue());

        // WHEN
        final IDataSet dataSet = loader.load(getFile("test-data.xls"));

        // THEN
        assertThat(dataSet, notNullValue());

        final List<String> tableNames = Arrays.asList(dataSet.getTableNames());
        assertThat(tableNames.size(), equalTo(2));
        assertThat(tableNames, hasItems("XLS_TABLE_1", "XLS_TABLE_2"));

        final ITable table1 = dataSet.getTable("XLS_TABLE_1");
        assertThat(table1.getRowCount(), equalTo(3));

        final ITable table2 = dataSet.getTable("XLS_TABLE_2");
        assertThat(table2.getRowCount(), equalTo(1));
    }

    @Test(expected = NullPointerException.class)
    public void testXlsLoaderLoadUsingNullFileName() throws IOException, DataSetException {
        // WHEN
        final DataSetLoader<IDataSet> loader = LOADER_PROVIDER.xlsLoader();

        // THEN
        assertThat(loader, notNullValue());

        // WHEN
        loader.load(null);

        // THEN
        // IOException is thrown
    }

    @Test(expected = IOException.class)
    public void testXlsLoaderLoadUsingWrongResource() throws Exception {
        // WHEN
        final DataSetLoader<IDataSet> loader = LOADER_PROVIDER.xlsLoader();

        // THEN
        assertThat(loader, notNullValue());

        // WHEN
        loader.load(getFile("test-data.json"));

        // THEN
        // IOException is thrown
    }

    @Test
    public void testCsvLoaderLoadUsingProperResource() throws Exception {
        // WHEN
        final DataSetLoader<IDataSet> loader = LOADER_PROVIDER.csvLoader();

        // THEN
        assertThat(loader, notNullValue());

        // WHEN
        final IDataSet dataSet = loader.load(getFile("test-data"));

        // THEN
        assertThat(dataSet, notNullValue());

        final List<String> tableNames = Arrays.asList(dataSet.getTableNames());
        assertThat(tableNames.size(), equalTo(2));
        assertThat(tableNames, hasItems("CSV_TABLE_1", "CSV_TABLE_2"));

        final ITable table1 = dataSet.getTable("CSV_TABLE_1");
        assertThat(table1.getRowCount(), equalTo(3));

        final ITable table2 = dataSet.getTable("CSV_TABLE_2");
        assertThat(table2.getRowCount(), equalTo(1));
    }

    @Test(expected = NullPointerException.class)
    public void testCsvLoaderLoadUsingNullFileName() throws IOException, DataSetException {
        // WHEN
        final DataSetLoader<IDataSet> loader = LOADER_PROVIDER.csvLoader();

        // THEN
        assertThat(loader, notNullValue());

        // WHEN
        loader.load(null);

        // THEN
        // IOException is thrown
    }

    @Test(expected = IOException.class)
    public void testCsvLoaderLoadUsingWrongFileResource() throws Exception {
        // WHEN
        final DataSetLoader<IDataSet> loader = LOADER_PROVIDER.csvLoader();

        // THEN
        assertThat(loader, notNullValue());

        // WHEN
        loader.load(getFile("test-data.json"));

        // THEN
        // IOException is thrown
    }

    @Test(expected = IOException.class)
    public void testCsvLoaderLoadUsingWrongDirectoryResource() throws Exception {
        // WHEN
        final DataSetLoader<IDataSet> loader = LOADER_PROVIDER.csvLoader();

        // THEN
        assertThat(loader, notNullValue());

        // WHEN
        loader.load(getFile("./META-INF"));

        // THEN
        // IOException is thrown
    }
}
