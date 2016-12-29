package eu.drus.jpa.unit.core.dbunit;

import java.io.File;
import java.net.URI;

public enum DataSetFormat {
    XML("xml") {
        @Override
        public <T> T select(final LoaderProvider<T> provider) {
            return provider.xmlLoader();
        }
    },
    YAML("yaml") {
        @Override
        public <T> T select(final LoaderProvider<T> provider) {
            return provider.yamlLoader();
        }
    },
    JSON("json") {
        @Override
        public <T> T select(final LoaderProvider<T> provider) {
            return provider.jsonLoader();
        }
    },
    CSV("csv") {
        @Override
        public <T> T select(final LoaderProvider<T> provider) {
            return provider.csvLoader();
        }
    },
    XLS("xls") {
        @Override
        public <T> T select(final LoaderProvider<T> provider) {
            return provider.xlsLoader();
        }
    },
    XLSX("xlsx") {
        @Override
        public <T> T select(final LoaderProvider<T> provider) {
            return provider.xlsLoader();
        }
    };

    private final String fileExtension;

    private DataSetFormat(final String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String extension() {
        return fileExtension;
    }

    public abstract <T> T select(LoaderProvider<T> provider);

    public static DataSetFormat inferFromFile(final URI fileName) {

        final File dataSetFile = new File(fileName);
        if (dataSetFile.isDirectory()) {
            // assuming CSV
            return CSV;
        }

        for (final DataSetFormat format : values()) {
            if (dataSetFile.getName().endsWith(format.fileExtension)) {
                return format;
            }
        }

        throw new UnsupportedDataSetFormatException("File " + fileName + " is not supported as data set format.");
    }

    public interface LoaderProvider<T> {
        T xmlLoader();

        T yamlLoader();

        T jsonLoader();

        T csvLoader();

        T xlsLoader();
    }
}
