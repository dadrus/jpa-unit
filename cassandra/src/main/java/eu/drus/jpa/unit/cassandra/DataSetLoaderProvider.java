package eu.drus.jpa.unit.cassandra;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import eu.drus.jpa.unit.spi.DataSetFormat.LoaderProvider;
import eu.drus.jpa.unit.cassandra.dataset.DataSet;
import eu.drus.jpa.unit.cassandra.dataset.DataSetParser;
import eu.drus.jpa.unit.cassandra.dataset.ParsedDataSet;
import eu.drus.jpa.unit.cassandra.dataset.json.JsonDataSetParser;
import eu.drus.jpa.unit.spi.DataSetLoader;
import eu.drus.jpa.unit.spi.UnsupportedDataSetFormatException;

public class DataSetLoaderProvider implements LoaderProvider<DataSet> {

    @Override
    public DataSetLoader<DataSet> xmlLoader() {
        throw new UnsupportedDataSetFormatException("XML data sets are not supportred for Cassandra");
    }

    @Override
    public DataSetLoader<DataSet> yamlLoader() {
        throw new UnsupportedDataSetFormatException("YAML data sets are not supportred for Cassandra");
    }

    @Override
    public DataSetLoader<DataSet> jsonLoader() {
        return (final File path) -> {
            try (InputStream in = new FileInputStream(path)) {
                final ParsedDataSet ds = new ParsedDataSet();
                final DataSetParser parser = new JsonDataSetParser();
                parser.setContentHandler(ds);
                parser.parse(in);
                return ds;
            }
        };
    }

    @Override
    public DataSetLoader<DataSet> csvLoader() {
        throw new UnsupportedDataSetFormatException("CSV data sets are not supportred for Cassandra");
    }

    @Override
    public DataSetLoader<DataSet> xlsLoader() {
        throw new UnsupportedDataSetFormatException("XSL data sets are not supportred for Cassandra");
    }
}
