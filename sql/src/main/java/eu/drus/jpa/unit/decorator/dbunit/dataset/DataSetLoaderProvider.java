package eu.drus.jpa.unit.decorator.dbunit.dataset;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.dbunit.dataset.CachedDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.csv.CsvDataSet;
import org.dbunit.dataset.excel.XlsDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;

import eu.drus.jpa.unit.core.DataSetLoader;
import eu.drus.jpa.unit.core.DataSetFormat.LoaderProvider;

public class DataSetLoaderProvider implements LoaderProvider<DataSetLoader<IDataSet>> {

    private IDataSet defineReplaceableExpressions(final IDataSet dataSet) {
        final ReplacementDataSet replacementDataSet = new ReplacementDataSet(dataSet);
        replacementDataSet.addReplacementObject("[null]", null);
        replacementDataSet.addReplacementObject("[NULL]", null);
        return replacementDataSet;
    }

    @Override
    public DataSetLoader<IDataSet> xmlLoader() {
        return (final File path) -> {
            try (InputStream in = new FileInputStream(path)) {
                final FlatXmlDataSetBuilder flatXmlDataSetBuilder = new FlatXmlDataSetBuilder();
                flatXmlDataSetBuilder.setColumnSensing(true);
                return defineReplaceableExpressions(flatXmlDataSetBuilder.build(in));
            } catch (final DataSetException e) {
                throw new IOException(e);
            }
        };
    }

    @Override
    public DataSetLoader<IDataSet> yamlLoader() {
        return (final File path) -> {
            try (InputStream in = new FileInputStream(path)) {
                return defineReplaceableExpressions(new CachedDataSet(new YamlDataSetProducer(in), false));
            } catch (final DataSetException e) {
                throw new IOException(e);
            }
        };
    }

    @Override
    public DataSetLoader<IDataSet> jsonLoader() {
        return (final File path) -> {
            try (InputStream in = new FileInputStream(path)) {
                return defineReplaceableExpressions(new CachedDataSet(new JsonDataSetProducer(in), false));
            } catch (final DataSetException e) {
                throw new IOException(e);
            }
        };
    }

    @Override
    public DataSetLoader<IDataSet> csvLoader() {
        return (final File path) -> {
            try {
                return defineReplaceableExpressions(new CsvDataSet(path));
            } catch (final DataSetException e) {
                throw new IOException(e);
            }
        };
    }

    @Override
    public DataSetLoader<IDataSet> xlsLoader() {
        return (final File path) -> {
            try (InputStream in = new FileInputStream(path)) {
                return defineReplaceableExpressions(new XlsDataSet(in));
            } catch (final DataSetException e) {
                throw new IOException(e);
            }
        };
    }
}
