package eu.drus.jpa.unit.mongodb;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
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
        return (final File path) -> {
            try (InputStream in = new FileInputStream(path)) {
                return Document.parse(IOUtils.toString(in));
            } catch (final RuntimeException e) {
                throw new IOException(e);
            }
        };
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
