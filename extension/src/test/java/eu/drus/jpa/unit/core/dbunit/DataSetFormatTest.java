package eu.drus.jpa.unit.core.dbunit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DataSetFormatTest {

    @Mock
    private DataSetFormat.LoaderProvider<Object> loaderProvider;

    private static File getFile(final String path) throws URISyntaxException {
        final URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        return new File(url.toURI());
    }

    @Test
    public void testVerifyJsonLoaderSelection() {
        final DataSetFormat format = DataSetFormat.JSON;

        format.select(loaderProvider);

        verify(loaderProvider).jsonLoader();
    }

    @Test
    public void testVerifyYamlLoaderSelection() {
        final DataSetFormat format = DataSetFormat.YAML;

        format.select(loaderProvider);

        verify(loaderProvider).yamlLoader();
    }

    @Test
    public void testVerifyXmlLoaderSelection() {
        final DataSetFormat format = DataSetFormat.XML;

        format.select(loaderProvider);

        verify(loaderProvider).xmlLoader();
    }

    @Test
    public void testVerifyXlsLoaderSelection() {
        final DataSetFormat format = DataSetFormat.XLS;

        format.select(loaderProvider);

        verify(loaderProvider).xlsLoader();
    }

    @Test
    public void testVerifyXlsxLoaderSelection() {
        final DataSetFormat format = DataSetFormat.XLSX;

        format.select(loaderProvider);

        verify(loaderProvider).xlsLoader();
    }

    @Test
    public void testVerifyCsvLoaderSelection() {
        final DataSetFormat format = DataSetFormat.CSV;

        format.select(loaderProvider);

        verify(loaderProvider).csvLoader();
    }

    @Test
    public void testInferJsonFormatFromFile() throws URISyntaxException {
        final DataSetFormat format = DataSetFormat.inferFromFile(getFile("test-data.json"));

        assertThat(format, equalTo(DataSetFormat.JSON));
        assertThat(format.extension(), equalTo("json"));
    }

    @Test
    public void testInferXmlFormatFromFile() throws URISyntaxException {
        final DataSetFormat format = DataSetFormat.inferFromFile(getFile("test-data.xml"));

        assertThat(format, equalTo(DataSetFormat.XML));
        assertThat(format.extension(), equalTo("xml"));
    }

    @Test
    public void testInferYamlFormatFromFile() throws URISyntaxException {
        final DataSetFormat format = DataSetFormat.inferFromFile(getFile("test-data.yaml"));

        assertThat(format, equalTo(DataSetFormat.YAML));
        assertThat(format.extension(), equalTo("yaml"));
    }

    @Test
    public void testInferXlsFormatFromFile() throws URISyntaxException {
        final DataSetFormat format = DataSetFormat.inferFromFile(getFile("test-data.xls"));

        assertThat(format, equalTo(DataSetFormat.XLS));
        assertThat(format.extension(), equalTo("xls"));
    }

    @Test
    public void testInferXlsxFormatFromFile() throws URISyntaxException {
        final DataSetFormat format = DataSetFormat.inferFromFile(getFile("test-data.xlsx"));

        assertThat(format, equalTo(DataSetFormat.XLSX));
        assertThat(format.extension(), equalTo("xlsx"));
    }

    @Test
    public void testInferCsvFormatFromFile() throws URISyntaxException {
        final DataSetFormat format = DataSetFormat.inferFromFile(getFile("test-data"));

        assertThat(format, equalTo(DataSetFormat.CSV));
        assertThat(format.extension(), equalTo("csv"));
    }

    @Test(expected = UnsupportedDataSetFormatException.class)
    public void testInferFormatFromFileWithUnknownFileExtension() throws URISyntaxException {
        DataSetFormat.inferFromFile(getFile("empty.file"));
    }
}
