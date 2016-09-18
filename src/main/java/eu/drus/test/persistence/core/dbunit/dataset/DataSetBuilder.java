package eu.drus.test.persistence.core.dbunit.dataset;

import java.io.IOException;
import java.io.InputStream;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;

import eu.drus.test.persistence.core.dbunit.DataSetFormat;
import eu.drus.test.persistence.core.dbunit.dataset.json.JsonDataSet;
import eu.drus.test.persistence.core.dbunit.dataset.yaml.YamlDataSet;

public class DataSetBuilder {
    private final DataSetFormat format;

    private DataSetBuilder(final DataSetFormat format) {
        this.format = format;
    }

    public IDataSet build(final String file) {
        IDataSet dataSet;
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(file)) {
            switch (format) {
            case XML:
                final FlatXmlDataSetBuilder flatXmlDataSetBuilder = new FlatXmlDataSetBuilder();
                flatXmlDataSetBuilder.setColumnSensing(true);
                dataSet = flatXmlDataSetBuilder.build(in);
                break;
            case YAML:
                dataSet = new YamlDataSet(in);
                break;
            case JSON:
                dataSet = new JsonDataSet(in);
                break;
            default:
                throw new IllegalArgumentException("Unsupported data type " + format);
            }
        } catch (final DataSetException | IOException e) {
            throw new RuntimeException("Unable to load data set from given file: " + file, e);
        }

        return defineReplaceableExpressions(dataSet);
    }

    public static DataSetBuilder builderFor(final DataSetFormat format) {
        return new DataSetBuilder(format);
    }

    private IDataSet defineReplaceableExpressions(final IDataSet dataSet) {
        final ReplacementDataSet replacementDataSet = new ReplacementDataSet(dataSet);
        replacementDataSet.addReplacementObject("[null]", null);
        replacementDataSet.addReplacementObject("[NULL]", null);
        return replacementDataSet;
    }
}
