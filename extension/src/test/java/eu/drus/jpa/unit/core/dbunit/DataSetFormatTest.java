package eu.drus.jpa.unit.core.dbunit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DataSetFormatTest {

    @Mock
    private DataSetFormat.LoaderProvider<Object> loaderProvider;

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
    public void testInferJsonFormatFromFile() {
        final DataSetFormat format = DataSetFormat.inferFromFile("test.json");

        assertThat(format, equalTo(DataSetFormat.JSON));
        assertThat(format.extension(), equalTo("json"));
    }

    @Test
    public void testInferXmlFormatFromFile() {
        final DataSetFormat format = DataSetFormat.inferFromFile("test.xml");

        assertThat(format, equalTo(DataSetFormat.XML));
        assertThat(format.extension(), equalTo("xml"));
    }

    @Test
    public void testInferYamlFormatFromFile() {
        final DataSetFormat format = DataSetFormat.inferFromFile("test.yaml");

        assertThat(format, equalTo(DataSetFormat.YAML));
        assertThat(format.extension(), equalTo("yaml"));
    }

    @Test
    public void testInferXlsFormatFromFile() {
        final DataSetFormat format = DataSetFormat.inferFromFile("test.xls");

        assertThat(format, equalTo(DataSetFormat.XLS));
        assertThat(format.extension(), equalTo("xls"));
    }

    @Test
    public void testInferXlsxFormatFromFile() {
        final DataSetFormat format = DataSetFormat.inferFromFile("test.xlsx");

        assertThat(format, equalTo(DataSetFormat.XLSX));
        assertThat(format.extension(), equalTo("xlsx"));
    }

    @Test
    public void testInferCsvFormatFromFile() {
        final DataSetFormat format = DataSetFormat.inferFromFile("test.csv");

        assertThat(format, equalTo(DataSetFormat.CSV));
        assertThat(format.extension(), equalTo("csv"));
    }

    @Test(expected = UnsupportedDataSetFormatException.class)
    public void testInferFormatFromFileWithUnknownFileExtension() {
        DataSetFormat.inferFromFile("test.foo");
    }
}
