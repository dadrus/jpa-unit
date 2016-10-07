package eu.drus.test.persistence.core.dbunit.dataset;

import java.io.IOException;

import org.dbunit.dataset.IDataSet;

public interface DataSetLoader {

    IDataSet load(String path) throws IOException;
}
