package eu.drus.jpa.unit.decorator.dbunit.dataset;

import java.io.File;
import java.io.IOException;

import org.dbunit.dataset.IDataSet;

@FunctionalInterface
public interface DataSetLoader {
    IDataSet load(File path) throws IOException;
}
