package eu.drus.jpa.unit.core.dbunit.dataset;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import org.dbunit.dataset.CachedDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.csv.CsvDataSet;
import org.dbunit.dataset.excel.XlsDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;

import eu.drus.jpa.unit.core.dbunit.DataSetFormat.LoaderProvider;

public class DataSetLoaderProvider implements LoaderProvider<DataSetLoader> {

    private static final String COULD_NOT_OPEN_FILE = "Could not open file: ";

    private IDataSet defineReplaceableExpressions(final IDataSet dataSet) {
        final ReplacementDataSet replacementDataSet = new ReplacementDataSet(dataSet);
        replacementDataSet.addReplacementObject("[null]", null);
        replacementDataSet.addReplacementObject("[NULL]", null);
        return replacementDataSet;
    }

    private void validateStream(final InputStream in, final String message) throws IOException {
        if (in == null) {
            throw new IOException(message);
        }
    }

    @Override
    public DataSetLoader xmlLoader() {
        return (final String path) -> {
            try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
                validateStream(in, COULD_NOT_OPEN_FILE + path);
                final FlatXmlDataSetBuilder flatXmlDataSetBuilder = new FlatXmlDataSetBuilder();
                flatXmlDataSetBuilder.setColumnSensing(true);
                return defineReplaceableExpressions(flatXmlDataSetBuilder.build(in));
            } catch (final DataSetException e) {
                throw new IOException(e);
            }
        };
    }

    @Override
    public DataSetLoader yamlLoader() {
        return (final String path) -> {
            try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
                validateStream(in, COULD_NOT_OPEN_FILE + path);
                return defineReplaceableExpressions(new CachedDataSet(new YamlDataSetProducer(in), false));
            } catch (final DataSetException e) {
                throw new IOException(e);
            }
        };
    }

    @Override
    public DataSetLoader jsonLoader() {
        return (final String path) -> {
            try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
                validateStream(in, COULD_NOT_OPEN_FILE + path);
                return defineReplaceableExpressions(new CachedDataSet(new JsonDataSetProducer(in), false));
            } catch (final DataSetException e) {
                throw new IOException(e);
            }
        };
    }

    @Override
    public DataSetLoader csvLoader() {
        return (final String path) -> {
            final URL csvUrl = Thread.currentThread().getContextClassLoader().getResource(path);
            try {
                return defineReplaceableExpressions(new CsvDataSet(new File(csvUrl.toURI())));
            } catch (DataSetException | URISyntaxException e) {
                throw new IOException(e);
            }
        };
    }

    @Override
    public DataSetLoader xlsLoader() {
        return (final String path) -> {
            try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
                validateStream(in, COULD_NOT_OPEN_FILE + path);
                return defineReplaceableExpressions(new XlsDataSet(in));
            } catch (final DataSetException e) {
                throw new IOException(e);
            }
        };
    }
}
