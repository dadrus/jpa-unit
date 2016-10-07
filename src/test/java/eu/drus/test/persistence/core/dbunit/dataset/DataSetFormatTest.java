package eu.drus.test.persistence.core.dbunit.dataset;

import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import eu.drus.test.persistence.core.dbunit.DataSetFormat;

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
}
