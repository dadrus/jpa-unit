package eu.drus.test.persistence.core.dbunit.dataset;

import java.io.IOException;

import org.dbunit.dataset.IDataSet;

@FunctionalInterface
public interface DataSetLoader {
    IDataSet load(String path) throws IOException;
}
