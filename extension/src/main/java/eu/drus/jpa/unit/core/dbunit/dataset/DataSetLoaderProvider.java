package eu.drus.jpa.unit.core.dbunit.dataset;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.dbunit.dataset.CachedDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.csv.CsvDataSet;
import org.dbunit.dataset.excel.XlsDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;

import eu.drus.jpa.unit.core.dbunit.DataSetFormat.LoaderProvider;

public class DataSetLoaderProvider implements LoaderProvider<DataSetLoader> {

    private IDataSet defineReplaceableExpressions(final IDataSet dataSet) {
        final ReplacementDataSet replacementDataSet = new ReplacementDataSet(dataSet);
        replacementDataSet.addReplacementObject("[null]", null);
        replacementDataSet.addReplacementObject("[NULL]", null);
        return replacementDataSet;
    }

    @Override
    public DataSetLoader xmlLoader() {
        return (final URI path) -> {
            try (InputStream in = new FileInputStream(new File(path))) {
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
        return (final URI path) -> {
            try (InputStream in = new FileInputStream(new File(path))) {
                return defineReplaceableExpressions(new CachedDataSet(new YamlDataSetProducer(in), false));
            } catch (final DataSetException e) {
                throw new IOException(e);
            }
        };
    }

    @Override
    public DataSetLoader jsonLoader() {
        return (final URI path) -> {
            try (InputStream in = new FileInputStream(new File(path))) {
                return defineReplaceableExpressions(new CachedDataSet(new JsonDataSetProducer(in), false));
            } catch (final DataSetException e) {
                throw new IOException(e);
            }
        };
    }

    @Override
    public DataSetLoader csvLoader() {
        return (final URI path) -> {
            try {
                return defineReplaceableExpressions(new CsvDataSet(new File(path)));
            } catch (final DataSetException e) {
                throw new IOException(e);
            }
        };
    }

    @Override
    public DataSetLoader xlsLoader() {
        return (final URI path) -> {
            try (InputStream in = new FileInputStream(new File(path))) {
                return defineReplaceableExpressions(new XlsDataSet(in));
            } catch (final DataSetException e) {
                throw new IOException(e);
            }
        };
    }
}
