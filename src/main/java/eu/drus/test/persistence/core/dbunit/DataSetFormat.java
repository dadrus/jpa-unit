package eu.drus.test.persistence.core.dbunit;

public enum DataSetFormat {
    XML("xml"), YAML("yml"), JSON("json");

    private final String fileExtension;

    private DataSetFormat(final String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String extension() {
        return fileExtension;
    }

    public static DataSetFormat inferFromFile(final String fileName) {

        for (final DataSetFormat format : values()) {
            if (fileName.endsWith(format.fileExtension)) {
                return format;
            }
        }

        throw new UnsupportedDataSetFormatException("File " + fileName + " is not supported as data set format.");
    }
}
