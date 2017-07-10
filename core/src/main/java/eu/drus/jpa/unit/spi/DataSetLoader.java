package eu.drus.jpa.unit.spi;

import java.io.File;
import java.io.IOException;

@FunctionalInterface
public interface DataSetLoader<T> {
    T load(File path) throws IOException;
}
