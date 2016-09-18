package eu.drus.test.persistence.core.dbunit.dataset.json;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.dbunit.dataset.CachedDataSet;
import org.dbunit.dataset.DataSetException;

public class JsonDataSet extends CachedDataSet {

    public JsonDataSet(final JsonDataSetProducer producer, final boolean caseSensitiveTableNames) throws DataSetException {
        super(producer, caseSensitiveTableNames);
    }

    public JsonDataSet(final File file, final boolean caseSensitiveTableNames) throws DataSetException, FileNotFoundException {
        this(new FileInputStream(file), caseSensitiveTableNames);
    }

    public JsonDataSet(final File file) throws IOException, DataSetException {
        this(new FileInputStream(file), false);
    }

    public JsonDataSet(final JsonDataSetProducer producer) throws DataSetException {
        this(producer, false);
    }

    public JsonDataSet(final InputStream inputStream) throws DataSetException {
        this(inputStream, false);
    }

    public JsonDataSet(final InputStream inputStream, final boolean caseSensitiveTableNames) throws DataSetException {
        this(new JsonDataSetProducer(inputStream), caseSensitiveTableNames);
    }

}
