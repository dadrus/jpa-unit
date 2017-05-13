package eu.drus.jpa.unit.decorator.mongodb;

import java.io.File;

import org.bson.Document;

import eu.drus.jpa.unit.core.DataSetFormat.LoaderProvider;
import eu.drus.jpa.unit.core.DataSetLoader;
import eu.drus.jpa.unit.core.UnsupportedDataSetFormatException;

public class DataSetLoaderProvider implements LoaderProvider<DataSetLoader<Document>> {

    @Override
    public DataSetLoader<Document> xmlLoader() {
        throw new UnsupportedDataSetFormatException("XML data sets are not supportred for MongoDB");
    }

    @Override
    public DataSetLoader<Document> yamlLoader() {
        throw new UnsupportedDataSetFormatException("YAML data sets are not supportred for MongoDB");
    }

    @Override
    public DataSetLoader<Document> jsonLoader() {
        return (final File path) -> null;
    }

    @Override
    public DataSetLoader<Document> csvLoader() {
        throw new UnsupportedDataSetFormatException("CSV data sets are not supportred for MongoDB");
    }

    @Override
    public DataSetLoader<Document> xlsLoader() {
        throw new UnsupportedDataSetFormatException("XSL data sets are not supportred for MongoDB");
    }
}
