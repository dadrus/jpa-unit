package eu.drus.test.persistence.core.dbunit;

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

    @Test(expected = UnsupportedDataSetFormatException.class)
    public void testInferFormatFromFileWithUnknownFileExtension() {
        DataSetFormat.inferFromFile("test.foo");
    }
}
