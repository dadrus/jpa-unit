package eu.drus.jpa.unit.core.dbunit.dataset;

import java.io.IOException;
import java.net.URI;

import org.dbunit.dataset.IDataSet;

@FunctionalInterface
public interface DataSetLoader {
    IDataSet load(URI path) throws IOException;
}
