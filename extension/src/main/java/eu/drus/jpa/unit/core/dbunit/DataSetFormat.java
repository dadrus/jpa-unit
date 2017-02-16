package eu.drus.jpa.unit.core.dbunit;

import java.io.File;

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

    private static final String TABLE_ORDERING_FILE = "table-ordering.txt";
    private final String fileExtension;

    private DataSetFormat(final String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String extension() {
        return fileExtension;
    }

    public abstract <T> T select(LoaderProvider<T> provider);

    public static DataSetFormat inferFromFile(final File file) {

        if (isCsvDirectory(file)) {
            return CSV;
        }

        for (final DataSetFormat format : values()) {
            if (file.getName().endsWith(format.fileExtension)) {
                return format;
            }
        }

        throw new UnsupportedDataSetFormatException("File " + file + " is not supported as data set format.");
    }

    private static boolean isCsvDirectory(final File file) {
        if (file.isDirectory()) {
            final File[] csvFiles = file.listFiles((final File pathname) -> pathname.getName().endsWith(CSV.fileExtension));
            final File[] metaFiles = file.listFiles((final File pathname) -> pathname.getName().equals(TABLE_ORDERING_FILE));

            return csvFiles.length != 0 && metaFiles.length != 0;
        }
        return false;
    }

    public interface LoaderProvider<T> {
        T xmlLoader();

        T yamlLoader();

        T jsonLoader();

        T csvLoader();

        T xlsLoader();
    }
}
